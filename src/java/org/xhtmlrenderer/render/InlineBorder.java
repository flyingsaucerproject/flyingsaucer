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

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;

import java.awt.*;
import java.awt.font.LineMetrics;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-maj-13
 * Time: 02:17:15
 * To change this template use File | Settings | File Templates.
 */
public class InlineBorder {
    private CalculatedStyle style;
    private LineMetrics lm;
    private Border margin;
    private Border border;
    private Border padding;
    private int y;
    private int height;

    /**
     * @param y
     * @param height
     * @param margin
     * @param border
     * @param padding
     * @param style
     * @param lm
     */
    InlineBorder(int y, int height, Border margin, Border border, Border padding, CalculatedStyle style, LineMetrics lm) {
        this.y = y;
        this.height = height;
        this.margin = margin;
        this.border = border;
        this.padding = padding;
        this.style = style;
        this.lm = lm;
    }

    void paint(Context c, LineBox line, int start, int width, int sides) {
        int ty = line.getBaseline() - y - height - margin.top - border.top - padding.top + line.y;
        ty += (int) lm.getDescent();
        c.translate(0, ty);
        Rectangle bounds = new Rectangle(start,
                y + margin.top,
                width,
                height + border.top + padding.top + padding.bottom + border.bottom);
        BorderPainter.paint(bounds, sides, style, c.getGraphics(), c.getCtx());
        c.translate(0, -ty);
    }

}
