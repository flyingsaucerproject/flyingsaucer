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
package org.xhtmlrenderer.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JComponent;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.render.AbstractOutputDevice;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.BorderPainter;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.Uu;

public class Java2DOutputDevice extends AbstractOutputDevice implements OutputDevice {
    private Graphics2D _graphics;
    
    public Java2DOutputDevice(Graphics2D graphics) {
        _graphics = graphics;
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
                box.getStyle().getCalculatedStyle(), _graphics, c, 0);
    }
    

    private Image getBackgroundImage(RenderingContext c, Box box) {
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
        Image backgroundImage = getBackgroundImage(c, box);
        
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
            Shape oldclip = (Shape) _graphics.getClip();
    
            if (box.getStyle().isFixedBackground()) {
                yoff = c.getCanvas().getLocation().y;
                _graphics.setClip(c.getCanvas().getVisibleRect());
            }
    
            _graphics.clip(backgroundBounds);
    
            int imageWidth = backgroundImage.getWidth(null);
            int imageHeight = backgroundImage.getHeight(null);
    
            Point bgOffset = box.getStyle().getCalculatedStyle().getBackgroundPosition(backgroundBounds.width - imageWidth,
                    backgroundBounds.height - imageHeight, c);
            xoff += bgOffset.x;
            yoff -= bgOffset.y;
    
            tileFill(backgroundImage,
                    backgroundBounds,
                    xoff, -yoff,
                    box.getStyle().isHorizontalBackgroundRepeat(),
                    box.getStyle().isVerticalBackgroundRepeat());
            _graphics.setClip(oldclip);
        }
    } 
    
    private void tileFill(Image img, Rectangle rect, int xoff, int yoff, boolean horiz, boolean vert) {
        int iwidth = img.getWidth(null);
        int iheight = img.getHeight(null);
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
                _graphics.drawImage(img, i + rect.x + xoff, j + rect.y + yoff, null);
            }
        }
    
    }

    public void paintReplacedElement(RenderingContext c, BlockBox box) {
        JComponent component = ((SwingReplacedElement)box.getReplacedElement()).getJComponent();
        component.paint(_graphics);
    }
    
    public void setColor(Color color) {
        _graphics.setColor(color);
    }
    
    public void drawLine(int x1, int y1, int x2, int y2) {
        _graphics.drawLine(x1, y1, x2, y2);
    }
    
    public void drawRect(int x, int y, int width, int height) {
        _graphics.drawRect(x, y, width, height);
    }
    
    public void fillRect(int x, int y, int width, int height) {
        _graphics.fillRect(x, y, width, height);
    }
}
