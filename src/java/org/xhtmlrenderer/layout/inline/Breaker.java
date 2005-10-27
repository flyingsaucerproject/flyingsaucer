/*
 * Breaker.java
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.content.WhitespaceStripper;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.InlineTextBox;
import org.xhtmlrenderer.util.Uu;

import java.awt.Font;


/**
 * Description of the Class
 *
 * @author Torbjörn Gannholm
 */
public class Breaker {
    /**
     * Description of the Method
     *
     * @param c          PARAM
     * @param inline     PARAM
     * @param prev_align PARAM
     * @param avail      PARAM
     * @param font       PARAM
     */
    public static void breakText(LayoutContext c, InlineTextBox inline, InlineBox prev_align, int avail, Font font) {
        boolean db = false;
        if (db) {
            Uu.p("=========================");
            Uu.p("breaking: " + inline);
            //Uu.p("breaking : '" + inline.getSubstring() + "'");
            //Uu.p("avail = " + avail);
            //Uu.p("max = " + max);
            //Uu.p("prev align = " + prev_align);
        }

        //inline.setFont(font);

        // ====== handle nowrap
        if (inline.whitespace == IdentValue.NOWRAP) {
            return;
        }

        //check if we should break on the next newline
        if (inline.whitespace == IdentValue.PRE ||
                inline.whitespace == IdentValue.PRE_WRAP ||
                inline.whitespace == IdentValue.PRE_LINE) {
            // Uu.p("doing a pre line");
            int n = inline.getSubstring().indexOf(WhitespaceStripper.EOL);
            // Uu.p("got eol at: " + n);
            if (n > -1) {
                inline.end_index = inline.start_index + n + 1;
                inline.break_after = true;
            }
        }

        //check if we may wrap
        if (inline.whitespace == IdentValue.PRE) {
            return;
        }

        String currentString = inline.getSubstring();
        int left = 0;
        int right = currentString.indexOf(WhitespaceStripper.SPACE, left + 1);
        int lastWrap = 0;
        int graphicsLength = 0;

        while (right > 0 && graphicsLength <= avail) {
            graphicsLength += FontUtil.len(currentString.substring(left, right), font, c.getTextRenderer(), c.getGraphics());
            lastWrap = left;
            left = right;
            right = currentString.indexOf(WhitespaceStripper.SPACE, left + 1);
        }

        if (graphicsLength <= avail) {
            //try for the last bit too!
            lastWrap = left;
            graphicsLength += FontUtil.len(currentString.substring(left), font, c.getTextRenderer(), c.getGraphics());
        }

        if (graphicsLength <= avail) {
            //It fit!
            return;
        }

        if (lastWrap != 0) {//found a place to wrap
            inline.setSubstring(inline.start_index, inline.start_index + lastWrap);
            inline.break_after = true;
        } else {//unbreakable string
            if (left == 0) left = currentString.length();
            inline.setSubstring(inline.start_index, inline.start_index + left);//the best we can do
            if (prev_align != null && !prev_align.break_after) {
                //will break the line and try to regenerate this inline
                inline.break_before = true;
            } else {
                //this inline was at the start of a line, so we just have to make do with it
                inline.break_after = true;
            }
        }
        return;
    }

}

