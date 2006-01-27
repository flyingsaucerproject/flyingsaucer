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
import java.awt.Rectangle;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.extend.OutputDevice;

public abstract class AbstractOutputDevice implements OutputDevice {
    public void drawText(RenderingContext c, InlineText inlineText) {
        InlineBox iB = inlineText.getParent();
        String text = inlineText.getSubstring();

        if (text != null && text.length() > 0) {
            c.getFontContext().configureFor(c, iB.getStyle().getCalculatedStyle(), true);
            c.getTextRenderer().drawString(c.getFontContext(), text, 
                    iB.getAbsX() + inlineText.getX(), iB.getAbsY() + iB.getBaseline());
        }

        if (c.debugDrawFontMetrics()) {
            drawFontMetrics(c, inlineText);
        }
    }

    private void drawFontMetrics(RenderingContext c, InlineText inlineText) {
        InlineBox iB = inlineText.getParent();
        String text = inlineText.getSubstring();
        
        setColor(new Color(0xFF, 0x33, 0xFF));
        
        FSFontMetrics fm = iB.getStyle().getFSFontMetrics(null);
        int width = c.getTextRenderer().getWidth(c.getFontContext(), text);
        int x = iB.getAbsX() + inlineText.getX();
        int y = iB.getAbsY() + iB.getBaseline();
        
        drawLine(x, y, x + width, y);
        
        y += (int) Math.ceil(fm.getDescent());
        drawLine(x, y, x + width, y);
        
        y -= (int) Math.ceil(fm.getDescent());
        y -= (int) Math.ceil(fm.getAscent());
        drawLine(x, y, x + width, y);
    }
    
    public void drawTextDecoration(RenderingContext c, InlineBox iB) {
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
}
