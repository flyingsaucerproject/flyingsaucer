/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
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
import org.xhtmlrenderer.layout.content.TextContent;
import org.xhtmlrenderer.util.Uu;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.List;


/**
 * Description of the Class
 *
 * @author empty
 */
public class InlineBox extends Box {

    public InlineBox() {
    }

    public InlineBox(InlineBox box) {
        this();
        x = box.x;
        y = box.y;
        width = box.width;
        height = box.height;
        border = box.border;
        margin = box.margin;
        padding = box.padding;
        color = box.color;
        content = box.content;
        master = box.master;
        sub_block = box.sub_block;
        font = box.font;
        underline = box.underline;
        overline = box.overline;
        strikethrough = box.strikethrough;
    }

    //might need to push styles before rendering this box
    public List pushstyles;
    //might need to pop styles after rendering this box
    public List popstyles;

    // if we are an inline block, then this is

    // the reference to the real block inside

    /**
     * Description of the Field
     */
    public BlockBox sub_block = null;

    /**
     * Description of the Field
     */
    public boolean replaced = false;


    // line breaking stuff

    /**
     * Description of the Field
     */
    public boolean break_after = false;

    /**
     * Description of the Field
     */
    public boolean break_before = false;


    // decoration stuff

    /**
     * Description of the Field
     */
    public boolean underline = false;

    /**
     * Description of the Field
     */
    public boolean strikethrough = false;

    /**
     * Description of the Field
     */
    public boolean overline = false;


    // vertical alignment stuff

    /**
     * Description of the Field
     */
    public int baseline;

    /**
     * Description of the Field
     */
    public int lineheight;

    /**
     * Description of the Field
     */
    public boolean vset = false;

    /**
     * Description of the Field
     */
    public boolean top_align = false;

    /**
     * Description of the Field
     */
    public boolean bottom_align = false;

    // text stuff

    /**
     * Description of the Field
     */
    public int start_index = -1;

    /**
     * Description of the Field
     */
    public int end_index = -1;

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {

        return "InlineBox text = \"" + getSubstring() +
                "\" bnds = " + x + "," + y + " - " + width + "Xx" + height +
                " start = " + this.start_index + " end = " + this.end_index +
                " baseline = " + this.baseline + " vset = " + this.vset +
                // CLN: (PWW 13/08/04)
                " color: " + color + " background-color: " + background_color +
                " font: " + font;
    }

    /**
     * Description of the Field
     */
    private Font font;


    /**
     * Gets the font attribute of the InlineBox object
     *
     * @return The font value
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font attribute of the InlineBox object
     *
     * @param font The new font value
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Gets the substring attribute of the InlineBox object
     *
     * @return The substring value
     */
    public String getSubstring() {
        // new code for whitepsace handling
        if (getMasterText() != null) {
            if (start_index == -1 || end_index == -1) {
                throw new RuntimeException("negative index in InlineBox");
                //return getMasterText();
            }
            if (end_index < start_index) {
                throw new RuntimeException("end is less than start");
                //Uu.p("warning: end is less than start: " + end_index + " < " + start_index);
                //Uu.p("master = " + getMasterText());
                //return getMasterText();
            }
            return getMasterText().substring(start_index, end_index);
        } else {
            if (content instanceof TextContent) {
                throw new RuntimeException("No master text set!");
                //XRLog.render(Level.WARNING, "No master text set!");
            }
            return "";
        }

    }

    /* not used: public void setSubstring(String text) {
        this.master = text;
        start_index = 0;
        end_index = text.length();
    }*/
    public int getStart_index() {
        return start_index;
    }

    public void setStart_index(int start_index) {
        this.start_index = start_index;
    }

    public int getEnd_index() {
        return end_index;
    }

    public void setEnd_index(int end_index) {
        this.end_index = end_index;
    }

    public void setSubstring(int start, int end) {
        if (end < start) {
            Uu.p("setting substring to: " + start + " " + end);
            throw new RuntimeException("set substring length too long: " + this);
        } else if (end < 0 || start < 0) {
            throw new RuntimeException("Trying to set negative index to inline box");
        }
        start_index = start;
        end_index = end;
    }

    public void setSubstringLength(int len) {
        end_index = start_index + len;
        if (end_index > getMasterText().length()) {
            Uu.p("just set substring length to : " + len);
            Uu.p("so indexes = " + start_index + " -> " + end_index);
            Uu.p("longer than master: " + getMasterText());
            throw new RuntimeException("set substring length too long: " + this);
        }
    }

    private String master;

    public void setMasterText(String master) {
        //Uu.p("set master text to: \"" + master + "\"");
        this.master = master;
    }

    public String getMasterText() {
        return master;
    }

    public String whitespace = "normal";

    public int getTextIndex(Context ctx, int x) {
        Font font = getFont();
        String str = getSubstring();
        char[] chars = new char[str.length()];
        getSubstring().getChars(0, str.length(), chars, 0);
        FontMetrics fm = ctx.getGraphics().getFontMetrics(font);

        for (int i = 0; i < chars.length; i++) {

            if (fm.charsWidth(chars, 0, i) >= x) {
                return i;
            }
        }

        return 0;
    }

    public int getAdvance(Context ctx, int x) {
        Font font = getFont();
        String str = getSubstring();
        str = str.substring(0, x);
        //Uu.p("substring = " + str);
        char[] chars = new char[str.length()];
        getSubstring().getChars(0, str.length(), chars, 0);
        FontMetrics fm = ctx.getGraphics().getFontMetrics(font);
        //Uu.p("getting advance: " + Xx + " chars = " + chars);
        return fm.charsWidth(chars, 0, x);
    }


    public LineMetrics line_metrics;
    public Rectangle2D text_bounds;

    private CalculatedStyle style;

    public void setStyle(CalculatedStyle style) {
        this.style = style;
    }

    public CalculatedStyle getStyle() {
        return this.style;
    }

    public boolean isInlineElement() {
        /* just to get it to compile, for now
        if(this.getRealElement() == this.getParent().getParent().getRealElement()) {
            return false;
        }*/
        if (this.content == null) return false;
        if (this.getParent() == null) return true;
        if (this.getParent().getParent() == null) return true;
        if (this.getParent().getParent().content == null) return true;
        if (this.content.getElement() == this.getParent().getParent().content.getElement()) {
            return false;
        }
        return true;
    }


}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.22  2004/12/15 00:53:40  tobega
 * Started playing a bit with inline box, provoked a few nasties, probably created some, seems to work now
 *
 * Revision 1.21  2004/12/13 02:12:53  tobega
 * Borders are working again
 *
 * Revision 1.20  2004/12/12 23:19:26  tobega
 * Tried to get hover working. Something happens, but not all that's supposed to happen.
 *
 * Revision 1.19  2004/12/12 05:51:49  tobega
 * Now things run. But there is a lot to do before it looks as nice as it did. At least we now have :before and :after content and handling of breaks by css.
 *
 * Revision 1.18  2004/12/12 03:33:00  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.17  2004/12/11 23:36:49  tobega
 * Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.16  2004/12/09 18:00:05  joshy
 * fixed hover bugs
 * fixed li's not being blocks bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2004/11/27 15:46:40  joshy
 * lots of cleanup to make the code clearer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/11/23 15:19:23  joshy
 * split breaking into it's own class
 * added support for the other values of whitespace (pre, pre-line, etc)
 * more unit tests
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/11/23 01:53:30  joshy
 * re-enabled vertical align
 * added unit tests for various text-align and indent forms
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2004/11/22 21:34:04  joshy
 * created new whitespace handler.
 * new whitespace routines only work if you set a special property. it's
 * off by default.
 *
 * turned off fractional font metrics
 *
 * fixed some bugs in Uu and Xx
 *
 * - j
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.11  2004/11/18 14:12:44  joshy
 * added whitespace test
 * cleaned up some code, spacing, and comments
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/12 22:02:01  joshy
 * initial support for mouse copy selection
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/10 14:54:43  joshy
 * code cleanup on aisle 6
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/10 14:34:20  joshy
 * more hover support
 *
 * Revision 1.7  2004/11/09 15:53:50  joshy
 * initial support for hover (currently disabled)
 * moved justification code into it's own class in a new subpackage for inline
 * layout (because it's so blooming complicated)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/09 02:04:24  joshy
 * support for text-align: justify
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/09 00:36:54  tobega
 * Fixed some NPEs
 *
 * Revision 1.4  2004/11/08 16:56:52  joshy
 * added first-line pseudo-class support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/11/04 15:35:45  joshy
 * initial float support
 * includes right and left float
 * cannot have more than one float per line per side
 * floats do not extend beyond enclosing block
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.2  2004/10/23 13:50:26  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

