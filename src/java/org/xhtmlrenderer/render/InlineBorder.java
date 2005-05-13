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
    private int start;
    private int end = -1;
    private CalculatedStyle style;
    private LineMetrics lm;
    private int top;
    private int height;
    private int y;

    /**
     * @param start
     * @param y      inline.y
     * @param top    inline.y+inline.margin.top
     * @param height inline.height - inline.margin.top - inline.margin.bottom
     * @param style
     * @param lm
     */
    InlineBorder(int start, int y, int top, int height, CalculatedStyle style, LineMetrics lm) {
        this.start = start;
        this.y = y;
        this.top = top;
        this.height = height;
        this.style = style;
        this.lm = lm;
    }

    void setEnd(int end) {
        this.end = end;
    }

    void paint(Context c, LineBox line) {
        int ty = line.getBaseline() - y - height + line.y;
        ty += (int) lm.getDescent();
        c.translate(0, ty);
        int left = (isStarted() ? start : 0);
        int width = (isEnded() ? end : line.width);
        if (start > 0) {
            width -= start;
        }
        Rectangle bounds = new Rectangle(left,
                top,
                width,
                height);
        BorderPainter.paint(bounds, BorderPainter.TOP + BorderPainter.BOTTOM, style, c.getGraphics(), c.getCtx());
        c.translate(0, -ty);
        start = -1;
    }

    public boolean isEnded() {
        return end >= 0;
    }

    public boolean isStarted() {
        return start >= 0;
    }
}
