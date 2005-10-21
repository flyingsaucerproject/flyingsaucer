/*
 * {{{ header & license
 * Copyright (c) 2005 Torbjšrn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.render;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.Context;

import java.awt.*;
import java.awt.font.LineMetrics;

/**
 * Keeps track of and paints nested inline backgrounds and borders
 */
public class InlineBorder {
    private CalculatedStyle style;
    private LineMetrics lm;
    // CLEAN:
    private RectPropertySet margin;
    private BorderPropertySet border;
    private RectPropertySet padding;
    private int y;
    private int height;
    private Color background_color;
    /**
     * borders are painted in sections, in horizontal order.
     * Keep track of how much is painted for horizontal pattern phase
     */
    private int xOffset = 0;

    /**
     * @param y
     * @param height
     * @param margin
     * @param border
     * @param padding
     * @param style
     * @param lm
     * @param background_color
     */
    InlineBorder(int y, int height, RectPropertySet margin, BorderPropertySet border, RectPropertySet padding, CalculatedStyle style, LineMetrics lm, Color background_color) {
        this.y = y;
        this.height = height;
        this.margin = margin;
        this.border = border;
        this.padding = padding;
        this.style = style;
        this.lm = lm;
        this.background_color = background_color;
    }

    void paint(Context c, LineBox line, int start, int width, int sides) {
        if (width <= 0) return;
        int ty = line.getBaseline() - y - height - (int)margin.top() - (int)border.top() - (int)padding.top() + line.y;
        ty += (int) lm.getDescent();
        c.translate(0, ty);
        c.getGraphics().translate(0, ty);
        // CLEAN: cast to int
        Rectangle bounds = new Rectangle(start,
                y + (int)margin.top(),
                width,
                height + (int)border.top() + (int)padding.top() + (int)padding.bottom() + (int)border.bottom());
        //first the background
        if (background_color != null) {
            // skip transparent background
            if (!background_color.equals(BackgroundPainter.transparent)) {
                //TODO. make conf controlled Uu.p("filling a background");
                c.getGraphics().setColor(background_color);
                c.getGraphics().fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }

        //then the border
        BorderPainter.paint(bounds, sides, style, c.getGraphics(), c.getCtx(), xOffset);
        c.getGraphics().translate(0, -ty);
        c.translate(0, -ty);
        xOffset += width;
    }

}
