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

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
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

import java.awt.*;
import java.awt.RenderingHints.Key;

public interface OutputDevice {
    void drawText(RenderingContext c, InlineText inlineText);
    void drawSelection(RenderingContext c, InlineText inlineText);

    void drawTextDecoration(RenderingContext c, LineBox lineBox);
    void drawTextDecoration(
            RenderingContext c, InlineLayoutBox iB, TextDecoration decoration);

    void paintBorder(RenderingContext c, Box box);
    void paintBorder(RenderingContext c, CalculatedStyle style,
                     Rectangle edge, int sides);
    void paintCollapsedBorder(
            RenderingContext c, BorderPropertySet border, Rectangle bounds, int side);

    void paintBackground(RenderingContext c, Box box);
    void paintBackground(
            RenderingContext c, CalculatedStyle style,
            Rectangle bounds, Rectangle bgImageContainer,
            BorderPropertySet border);

    void paintReplacedElement(RenderingContext c, BlockBox box);

    void drawDebugOutline(RenderingContext c, Box box, FSColor color);

    void setFont(FSFont font);

    void setColor(FSColor color);

    void drawRect(int x, int y, int width, int height);
    void drawOval(int x, int y, int width, int height);

    void drawBorderLine(Shape bounds, int side, int width, boolean solid);

    void drawImage(FSImage image, int x, int y);

    void draw(Shape s);
    void fill(Shape s);
    void fillRect(int x, int y, int width, int height);
    void fillOval(int x, int y, int width, int height);

    void clip(Shape s);
    Shape getClip();
    void setClip(Shape s);

    void translate(double tx, double ty);

    void setStroke(Stroke s);
    Stroke getStroke();

    @Nullable
    @CheckReturnValue
    Object getRenderingHint(Key key);
    void setRenderingHint(Key key, Object value);

    boolean isSupportsSelection();

    boolean isSupportsCMYKColors();
}
