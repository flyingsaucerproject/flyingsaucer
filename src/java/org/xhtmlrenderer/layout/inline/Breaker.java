package org.xhtmlrenderer.layout.inline;

import org.w3c.dom.*;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.layout.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Color;
import java.awt.*;
import java.util.regex.*;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;

public class Breaker {
    public static boolean breakText(Context c, InlineBox inline, InlineBox prev, InlineBox prev_align, int avail, int max, Font font) {
        boolean db = false;
        if(db) {
         u.p("=========================");
         u.p("breaking: " + inline);
         //u.p("breaking : '" + inline.getSubstring() + "'");
         //u.p("avail = " + avail);
         //u.p("max = " + max);
         //u.p("prev align = " + prev_align);
        }
        
        inline.setFont(font);
        
        // ======= handle pre
        if(inline.whitespace.equals("pre")) {
            preBreak(inline);
            return true;
        }
        
        // ====== handle nowrap
        if(inline.whitespace.equals("nowrap")) {
            nowrapBreak(inline);
            return true;
        }
        
        // all of the text fits on the current line so just return it
        // NOTE. this code may not be needed, though it's probably
        // an optimization for when you have lots of small inlines
        // JMM 11/23/04
        if(!breakAtNewLines(inline)) {
            if(FontUtil.len(c,inline.getSubstring(),font) < avail) {
                fitsOnLine(inline);
                return true;
            }
        }


        
        /*
          there are lots of excess substrings generated in here!!!
          this should all be done with indexes
        */
        //text too long to fit on this line
        int n = 0;
        int pn = 0;
        // loop until we find a substring that fits in the avail space
        while(true) {
            // look for a space
            int space_n = inline.getSubstring().indexOf(WhitespaceStripper.SPACE,n+1);
            // look for a linefeed (End Of Line character)
            // NOTE: this should be updated to handle non-unix line feeds
            int eol_n = inline.getSubstring().indexOf(WhitespaceStripper.EOL,n+1);
            n = space_n;

            // if we should also look for newlines, and the newline is 
            // closer, then use that instead of the space
            // NOTE: I'm not sure this part is needed. the part at the
            // end of the loop might actually handle it all. this is
            // a case for the unit tests to confirm. JMM 11/23/04
            if(breakAtNewLines(inline)) {
                if(eol_n != -1) {
                    if(eol_n < space_n || space_n == -1) {
                        n = eol_n;
                    }
                }
            }
            
            // a single unbreakable string that can't fit on the line
            if(n == -1 && pn == 0) {
                unbreakableNextLine(inline,prev_align);
                return true;
            }
            
            // make a tenative breaking string
            String tenative = null;
            if(n == -1) {
                tenative = inline.getSubstring();
            } else {
                tenative = inline.getSubstring().substring(0,n);
            }
            if(db) { u.p("tenative: " + tenative); }
            // if the string is too long to fit in the available space
            int len = FontUtil.len(c,tenative,font);
            if(db) { u.p("len = " + len + " avail = " + avail); }

            // we reached the end of the string
            if(len < avail && n==-1) {
                // u.p("hit end of string");
                normalLineBreak(c,inline,inline.getSubstring().length());
                return true;
            }
            
            if(len >= avail) {
                // if the previous tenative string would work
                // then do a normal break
                if(pn > 0) {
                    normalLineBreak(c,inline,pn);
                    return true;
                }
                unbreakableOnNextLine(c,inline,n,max);
                return true;
            }

            // if the linebreak would have happened
            if(breakAtNewLines(inline)) {
                if(eol_n != -1) {
                    if(eol_n < space_n || space_n == -1) {
                        n = eol_n;
                        // u.p("purposely breaking at a newline");
                        inline.setSubstringLength(n);
                        inline.break_after = true;
                        return true;
                    }
                }
            }
            
            // loop
            pn = n;
            if(db) { u.p("loop " + n + " '" + tenative + "' avail = " + avail + " len = " + len); }
        }
        
    }

    public static boolean breakAtNewLines(InlineBox inline) {
        if(inline.whitespace.equals("pre")) {
            return true;
        }
        if(inline.whitespace.equals("pre-wrap")) {
            return true;
        }
        if(inline.whitespace.equals("pre-line")) {
            return true;
        }
        return false;
    }

    public static void preBreak(InlineBox inline) {
        // u.p("doing a pre line");
        int n = inline.getSubstring().indexOf(WhitespaceStripper.EOL);
        // u.p("got eol at: " + n);
        if(n == -1) {
            //inline.setSubstring(inline.start_index,inline.start_index + inline.getSubstring().length());
            inline.setSubstringLength(inline.getSubstring().length());
        } else {
            //inline.setSubstring(inline.start_index,inline.start_index + n+1);
            inline.setSubstringLength(n+1);
        }
        inline.break_after = true;
    }

    
    // no wrap means collapse whitespace as w/ 'normal' but
    // supress all line breaking.
    public static void nowrapBreak(InlineBox inline) {
        // just make it one big inline
        inline.setSubstringLength(inline.getSubstring().length());
    }
    
    public static void fitsOnLine(InlineBox inline) {
        String txt = inline.getSubstring();
        inline.break_after = false;
    }
    
    //put the box on this line and break after
    public static void normalLineBreak(Context c, InlineBox inline, int n) {
        inline.setSubstring( inline.start_index, inline.start_index + n);
        inline.break_after = true;
    }
    
    public static void unbreakableNextLine(InlineBox inline, InlineBox prev_align) {
        if(prev_align != null) {
            inline.break_before = true;
        }
        
        // make unbreakable to end of this inline
        inline.setSubstringLength(inline.getSubstring().length());
    }
    
    public static void unbreakableOnNextLine(Context c, InlineBox inline, int n, int max) {
        // if this is an unbreakable word so put it on the next line
        // by itself
        
        // HACK. not sure the better way to do this
        // if there is another space after this, then tack it on
        if(inline.getSubstring().length() > n && inline.getSubstring().charAt(n) == ' ') {
            inline.setSubstring(inline.start_index, inline.start_index + n+1);
        } else {
            inline.setSubstring(inline.start_index, inline.start_index + n);
        }
        inline.break_before = true;
        // if it's so long that it fills up the next line too
        if(FontUtil.len(c,inline.getSubstring(),inline.getFont()) > max) {
            inline.break_after = true;
        }
    }
}
