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

import java.awt.Font;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class InlineBox extends Box {
    
    public InlineBox() {
    }
    public InlineBox(InlineBox box) {
        super(box);
        sub_block = box.sub_block;
        font = box.font;
        text = box.text;
        underline = box.underline;
        overline = box.overline;
        strikethrough = box.strikethrough;
    }

    // if we are an inline block, then this is

    // the reference to the real block inside

    /** Description of the Field */
    public BlockBox sub_block = null;

    /** Description of the Field */
    public boolean replaced = false;


    // line breaking stuff

    /** Description of the Field */
    public boolean break_after = false;

    /** Description of the Field */
    public boolean break_before = false;


    // decoration stuff

    /** Description of the Field */
    public boolean underline = false;

    /** Description of the Field */
    public boolean strikethrough = false;

    /** Description of the Field */
    public boolean overline = false;


    // vertical alignment stuff

    /** Description of the Field */
    public int baseline;

    /** Description of the Field */
    public int lineheight;

    /** Description of the Field */
    public boolean vset = false;

    /** Description of the Field */
    public boolean top_align = false;

    /** Description of the Field */
    public boolean bottom_align = false;

    /** Description of the Field */
    public boolean is_break = false;


    // text stuff

    /** Description of the Field */
    public int start_index = -1;

    /** Description of the Field */
    public int end_index = -1;

    /** Description of the Field */
    private String text;


    /** Description of the Field */
    private Font font;


    /**
     * Converts to a String representation of the object.
     *
     * @return   A string representation of the object.
     */
    public String toString() {

        return "InlineBox text = \"" + getText() +
                "\" bnds = " + x + "," + y + " - " + width + "x" + height +
                " start = " + this.start_index + " end = " + this.end_index +
                " baseline = " + this.baseline + " vset = " + this.vset +
        // CLN: (PWW 13/08/04)
                " color: " + color + " background-color: " + background_color +
                " font: " + font;
    }

    /**
     * Sets the font attribute of the InlineBox object
     *
     * @param font  The new font value
     */
    public void setFont( Font font ) {
        this.font = font;
    }

    /**
     * Sets the text attribute of the InlineBox object
     *
     * @param text  The new text value
     */
    public void setText( String text ) {
        this.text = text;
    }


    /**
     * Gets the substring attribute of the InlineBox object
     *
     * @return   The substring value
     */
    public String getSubstring() {
        if(text == null) return "";
        String txt = text.substring( start_index, end_index );
        return txt;
    }
    public void setSubstring(String text) {
        this.text = text;
        start_index = 0;
        end_index = text.length();
    }

    /**
     * Gets the font attribute of the InlineBox object
     *
     * @return   The font value
     */
    public Font getFont() {
        return font;
    }


    /**
     * Gets the text attribute of the InlineBox object
     *
     * @return   The text value
     */
    public String getText() {
        return this.text;
    }

}

/*
 * $Id$
 *
 * $Log$
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

