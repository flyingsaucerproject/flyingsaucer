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
package org.xhtmlrenderer.extend;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;

import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.render.TextDecoration;

public interface OutputDevice {
    public void drawText(RenderingContext c, InlineText inlineText);
    public void drawSelection(RenderingContext c, InlineText inlineText);
    
    public void drawTextDecoration(RenderingContext c, LineBox lineBox);
    public void drawTextDecoration(
            RenderingContext c, InlineLayoutBox iB, TextDecoration decoration);
    
    public void paintBorder(RenderingContext c, Box box);
    public void paintBorder(RenderingContext c, CalculatedStyle style, 
            Rectangle edge, int sides);
    public void paintCollapsedBorder(
            RenderingContext c, BorderPropertySet border, Rectangle bounds, int side);
    
    public void paintBackground(RenderingContext c, Box box);
    public void paintBackground(
            RenderingContext c, CalculatedStyle style, 
            Rectangle bounds, Rectangle bgImageContainer,
            BorderPropertySet border);
    
    public void paintReplacedElement(RenderingContext c, BlockBox box);
    
    public void drawDebugOutline(RenderingContext c, Box box, FSColor color);
    
    public void setFont(FSFont font);
    
    public void setColor(FSColor color);
    
    public void drawRect(int x, int y, int width, int height);
    public void drawOval(int x, int y, int width, int height);
    
    public void drawBorderLine(Shape bounds, int side, int width, boolean solid);
    
    public void drawImage(FSImage image, int x, int y);

    public void draw(Shape s);
    public void fill(Shape s);
    public void fillRect(int x, int y, int width, int height);
    public void fillOval(int x, int y, int width, int height);
    
    public void clip(Shape s);
    public Shape getClip();
    public void setClip(Shape s);
    
    public void translate(double tx, double ty);
    
    public void setStroke(Stroke s);
    public Stroke getStroke();

    public Object getRenderingHint(Key key);
    public void setRenderingHint(Key key, Object value);
    
    public boolean isSupportsSelection();
    
    public boolean isSupportsCMYKColors();
}
