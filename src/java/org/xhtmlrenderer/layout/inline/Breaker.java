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
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.layout.content.WhitespaceStripper;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.InlineTextBox;
import org.xhtmlrenderer.util.Uu;

import java.awt.*;


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
    public static void breakText(Context c, InlineTextBox inline, InlineBox prev_align, int avail, Font font) {
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
        int n = 0;
        int possibleWrap;
        boolean lastLenCheck = false;

        while (true) {
            possibleWrap = n;
            n = currentString.indexOf(WhitespaceStripper.SPACE, n + 1);
            if (n == -1) {
                break;
            }
            lastLenCheck = FontUtil.len(c, currentString.substring(0, n), font) > avail;
            if (lastLenCheck) {
                break;
            }
        }

        if (n == -1 && FontUtil.len(c, currentString, font) <= avail) {
            return;
        }

        if (possibleWrap == 0) {
            if (lastLenCheck) {
                n = 0;
            } else {
                possibleWrap = currentString.length();
            }
        }
        
        // (0 is a boundary condition when the first was a space)
        if (n <= 0) {//unbreakable string
            inline.setSubstring(inline.start_index, inline.start_index + possibleWrap);//the best we can do
            if (prev_align != null && !prev_align.break_after) {
                inline.break_before = true;
            }//else {//I think this else should be here?
            inline.break_after = true;
            //}
        } else {//found a place to wrap
            inline.setSubstring(inline.start_index, inline.start_index + possibleWrap);
            inline.break_after = true;
        }
        return;
    }

}

