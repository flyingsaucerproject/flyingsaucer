/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.render;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.Uu;

public abstract class AbstractOutputDevice implements OutputDevice {
    protected abstract void drawLine(int x1, int y1, int x2, int y2);
    
    public void drawText(RenderingContext c, InlineText inlineText) {
        InlineLayoutBox iB = inlineText.getParent();
        String text = inlineText.getSubstring();

        if (text != null && text.length() > 0) {
            setColor(iB.getStyle().getCalculatedStyle().getColor());
            setFont(iB.getStyle().getCalculatedStyle().getFSFont(c));
            c.getTextRenderer().drawString(
                    c.getOutputDevice(),
                    text,
                    iB.getAbsX() + inlineText.getX(), iB.getAbsY() + iB.getBaseline());
        }

        if (c.debugDrawFontMetrics()) {
            drawFontMetrics(c, inlineText);
        }
    }

    private void drawFontMetrics(RenderingContext c, InlineText inlineText) {
        InlineLayoutBox iB = inlineText.getParent();
        String text = inlineText.getSubstring();
        
        setColor(new Color(0xFF, 0x33, 0xFF));
        
        FSFontMetrics fm = iB.getStyle().getFSFontMetrics(null);
        int width = c.getTextRenderer().getWidth(
                c.getFontContext(), 
                iB.getStyle().getCalculatedStyle().getFSFont(c), text);
        int x = iB.getAbsX() + inlineText.getX();
        int y = iB.getAbsY() + iB.getBaseline();
        
        drawLine(x, y, x + width, y);
        
        y += (int) Math.ceil(fm.getDescent());
        drawLine(x, y, x + width, y);
        
        y -= (int) Math.ceil(fm.getDescent());
        y -= (int) Math.ceil(fm.getAscent());
        drawLine(x, y, x + width, y);
    }
    
    public void drawTextDecoration(RenderingContext c, InlineLayoutBox iB) {
        setColor(iB.getStyle().getCalculatedStyle().getColor());
        
        Rectangle edge = iB.getContentAreaEdge(iB.getAbsX(), iB.getAbsY(), c);
        
        fillRect(edge.x, iB.getAbsY() + iB.getTextDecoration().getOffset(),
                    edge.width, iB.getTextDecoration().getThickness());
    }
    
    public void drawTextDecoration(RenderingContext c, LineBox lineBox) {
        setColor(lineBox.getStyle().getCalculatedStyle().getColor());
        Box parent = lineBox.getParent();
        TextDecoration textDecoration = lineBox.getTextDecoration();
        if (parent.getStyle().getCalculatedStyle().isIdent(
                CSSName.FS_TEXT_DECORATION_EXTENT, IdentValue.BLOCK)) {
            fillRect(
                lineBox.getAbsX(), 
                lineBox.getAbsY() + textDecoration.getOffset(),
                parent.getAbsX() + parent.tx + parent.getContentWidth() - lineBox.getAbsX(), 
                textDecoration.getThickness());
        } else {
            fillRect(
                lineBox.getAbsX(), lineBox.getAbsY() + textDecoration.getOffset(),
                lineBox.getContentWidth(),
                textDecoration.getThickness());
        }
    }
    
    public void drawDebugOutline(RenderingContext c, Box box, Color color) {
        setColor(color);
        Rectangle rect = box.getBounds(box.getAbsX(), box.getAbsY(), c, 0, 0);
        rect.height -= 1;
        rect.width -= 1;
        drawRect(rect.x, rect.y, rect.width, rect.height);
    }
    

    public void paintBorder(RenderingContext c, Box box) {
        if (! box.getStyle().isVisible()) {
            return;
        }
        
        Rectangle borderBounds = box.getPaintingBorderEdge(c);
        if (! c.isPrint() && box.getState() != Box.DONE) {
            borderBounds.height += c.getCanvas().getHeight();
        }
    
        BorderPainter.paint(borderBounds, box.getBorderSides(),
                box.getStyle().getCalculatedStyle(), c, 0);
    }
    
    public void paintBorder(RenderingContext c, CalculatedStyle style, Rectangle edge, int sides) {
        BorderPainter.paint(edge, sides, style, c, 0);
    }
    
    private FSImage getBackgroundImage(RenderingContext c, Box box) {
        String uri = box.getStyle().getCalculatedStyle().getStringProperty(CSSName.BACKGROUND_IMAGE);
        if (! uri.equals("none")) {
            try {
                return c.getUac().getImageResource(uri).getImage();
            } catch (Exception ex) {
                ex.printStackTrace();
                Uu.p(ex);
            }
        }
        return null;
    }

    public void paintBackground(RenderingContext c, Box box) {
        if (!Configuration.isTrue("xr.renderer.draw.backgrounds", true)) {
            return;
        }
        
        if (! box.getStyle().isVisible()) {
            return;
        }
        
        Color backgroundColor = box.getStyle().getCalculatedStyle().getBackgroundColor();
        FSImage backgroundImage = getBackgroundImage(c, box);
        
        if ( (backgroundColor == null || backgroundColor.equals(TRANSPARENT)) &&
                backgroundImage == null) {
            return;
        }
    
        Rectangle backgroundBounds = box.getPaintingBorderEdge(c);
        if (! c.isPrint() && box.getState() != Box.DONE) {
            backgroundBounds.height += c.getCanvas().getHeight();
        }
        
        if (backgroundColor != null && ! backgroundColor.equals(TRANSPARENT)) {
            setColor(backgroundColor);
            fillRect(backgroundBounds.x, backgroundBounds.y, backgroundBounds.width, backgroundBounds.height);
        }
    
        int xoff = 0;
        int yoff = 0;
        
        if (backgroundImage != null) {
            Shape oldclip = getClip();
    
            if (box.getStyle().isFixedBackground() && ! c.isPrint()) {
                yoff = c.getCanvas().getLocation().y;
                setClip(c.getCanvas().getVisibleRect());
            }
    
            clip(backgroundBounds);
    
            int imageWidth = backgroundImage.getWidth();
            int imageHeight = backgroundImage.getHeight();
    
            Point bgOffset = box.getStyle().getCalculatedStyle().getBackgroundPosition(backgroundBounds.width - imageWidth,
                    backgroundBounds.height - imageHeight, c);
            xoff += bgOffset.x;
            yoff -= bgOffset.y;
    
            tileFill(backgroundImage,
                    backgroundBounds,
                    xoff, -yoff,
                    box.getStyle().isHorizontalBackgroundRepeat(),
                    box.getStyle().isVerticalBackgroundRepeat());
            setClip(oldclip);
        }
    } 
    
    private void tileFill(FSImage img, Rectangle rect, int xoff, int yoff, boolean horiz, boolean vert) {
        int iwidth = img.getWidth();
        int iheight = img.getHeight();
        int rwidth = rect.width;
        int rheight = rect.height;
    
        if (horiz) {
            xoff = xoff % iwidth - iwidth;
            rwidth += iwidth;
        } else {
            rwidth = iwidth;
        }
    
        if (vert) {
            yoff = yoff % iheight - iheight;
            rheight += iheight;
        } else {
            rheight = iheight;
        }
    
        for (int i = 0; i < rwidth; i += iwidth) {
            for (int j = 0; j < rheight; j += iheight) {
                drawImage(img, i + rect.x + xoff, j + rect.y + yoff);
            }
        }
    }    
}
