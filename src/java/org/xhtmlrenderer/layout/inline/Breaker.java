package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.util.Uu;

import java.awt.Font;

public class Breaker {
    public static void breakText(Context c, InlineBox inline, InlineBox prev_align, int avail, int max, Font font) {
        boolean db = false;
        if (db) {
            Uu.p("=========================");
            Uu.p("breaking: " + inline);
            //Uu.p("breaking : '" + inline.getSubstring() + "'");
            //Uu.p("avail = " + avail);
            //Uu.p("max = " + max);
            //Uu.p("prev align = " + prev_align);
        }

        inline.setFont(font);
        
        // ====== handle nowrap
        if (inline.whitespace.equals("nowrap")) {//we can't touch it
            return;
        }

        //check if we should break on the next newline
        if (inline.whitespace.equals("pre") ||
                inline.whitespace.equals("pre-wrap") ||
                inline.whitespace.equals("pre-line")) {
            // Uu.p("doing a pre line");
            int n = inline.getSubstring().indexOf(WhitespaceStripper.EOL);
            // Uu.p("got eol at: " + n);
            if (n > -1) {
                inline.end_index = inline.start_index + n + 1;
                inline.break_after = true;
            }
        }

        //check if we may wrap
        if (inline.whitespace.equals("pre")) {//we can't do anymore
            return;
        }

        //check if it all fits on this line
        if (FontUtil.len(c, inline.getSubstring(), font) < avail) {
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
        } while (n > 0 && FontUtil.len(c, currentString.substring(0, n), font) >= avail);
        
        // (0 is a boundary condition when the first was a space)
        if (n <= 0) {//unbreakable string
            inline.setSubstring(inline.start_index, inline.start_index + possibleWrap);//the best we can do
            if (prev_align != null && !prev_align.break_after) {
                inline.break_before = true;
            } //else {//I think this else should be here?
            inline.break_after = true;
            //}
        } else {//found a place to wrap
            inline.setSubstring(inline.start_index, inline.start_index + n);
            inline.break_after = true;
        }
        return;

    }

}
