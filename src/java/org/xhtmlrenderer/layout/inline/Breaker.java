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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.FontUtil;
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
     * @param max        PARAM
     * @param font       PARAM
     */
    public static void breakText(Context c, InlineTextBox inline, InlineBox prev_align, int avail, int max, Font font) {
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

        //check if it all fits on this line
        if (FontUtil.len(c, inline.getSubstring(), font) <= avail) {
            return;
        }

        //all newlines are already taken care of
        //text too long to fit on this line, we may wrap
        //just find a space that works
        String currentString = inline.getSubstring();
        int n = currentString.length();
        int possibleWrap;
        do {
            possibleWrap = n;
            n = currentString.lastIndexOf(WhitespaceStripper.SPACE, possibleWrap - 1);
        } while (n > 0 && FontUtil.len(c, currentString.substring(0, n), font) > avail);

        // (0 is a boundary condition when the first was a space)
        if (n <= 0) {//unbreakable string
            inline.setSubstring(inline.start_index, inline.start_index + possibleWrap);//the best we can do
            if (prev_align != null && !prev_align.break_after) {
                inline.break_before = true;
            }//else {//I think this else should be here?
            inline.break_after = true;
            //}
        } else {//found a place to wrap
            inline.setSubstring(inline.start_index, inline.start_index + n);
            inline.break_after = true;
        }
        return;
    }

}

