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

import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.layout.Context;

import java.awt.*;
import java.awt.font.LineMetrics;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-maj-11
 * Time: 01:58:43
 * To change this template use File | Settings | File Templates.
 */
class TextDecoration {
    private IdentValue decoration;
    private int start;
    private int end = -1;
    private Color color;
    private LineMetrics lm;

    TextDecoration(IdentValue decoration, int start, Color color, LineMetrics lm) {
        this.decoration = decoration;
        this.start = start;
        this.color = color;
        this.lm = lm;
    }

    void setEnd(int end) {
        this.end = end;
    }

    void paint(Context c, LineBox line) {
        //NONEs are not added. if(decoration == IdentValue.NONE) return;
        Graphics g = c.getGraphics();
        Color oldcolor = c.getGraphics().getColor();
        c.getGraphics().setColor(color);

        int ix = line.x;
        int iy = line.y + line.getBaseline();
        int width = (isEnded() ? end : line.contentWidth);
        if (start > 0) {
            ix += start;
            width -= start;
        }

        if (decoration == IdentValue.UNDERLINE) {
            float down = lm.getUnderlineOffset();
            float thick = lm.getUnderlineThickness();

            // correct the vertical pos of the underline by 2px, as without that
            // the underline sticks right against the text.
            g.fillRect(ix, iy - (int) down + 2, width, (int) thick);
        } else if (decoration == IdentValue.LINE_THROUGH) {
            float down = lm.getStrikethroughOffset();
            float thick = lm.getStrikethroughThickness();
            g.fillRect(ix, iy + (int) down, width, (int) thick);
        } else if (decoration == IdentValue.OVERLINE) {
            float down = lm.getAscent();
            float thick = lm.getUnderlineThickness();
            g.fillRect(ix, iy - (int) down, width, (int) thick);
        }

        c.getGraphics().setColor(oldcolor);
        //reset start to 0 for next line, in case not ended
        start = 0;
    }

    public boolean isEnded() {
        return end >= 0;
    }

    public TextDecoration getRestarted(int restart) {
        return new TextDecoration(decoration, restart, color, lm);
    }
}
