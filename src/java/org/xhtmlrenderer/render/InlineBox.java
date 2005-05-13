/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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

import java.util.List;


/**
 * Description of the Class
 *
 * @author empty
 */
public abstract class InlineBox extends Box {

    //might need to push styles before rendering this box
    /**
     * Description of the Field
     */
    public List pushstyles;
    //might need to pop styles after rendering this box
    /**
     * Description of the Field
     */
    public int popstyles = 0;

    //to keep track of right padding to be added by closing inline elements
    public int rightPadding = 0;

    public int leftPadding = 0;
    
    // if we are an inline block, then this is
    // the reference to the real block inside

    /** Description of the Field  */

    // line breaking stuff

    /**
     * Description of the Field
     */
    public boolean break_after = false;

    /**
     * Description of the Field
     */
    public boolean break_before = false;


    // vertical alignment stuff
    /**
     * Description of the Field
     */
    public int start_index = -1;

    /**
     * Description of the Field
     */
    public int end_index = -1;

    /**
     * Constructor for the InlineBox object
     */
    public InlineBox() {
    }

    /**
     * Description of the Method
     *
     * @return Returns
     */
    public abstract InlineBox copy();

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {

        return "InlineBox " +
                "bnds = " + x + "," + y + " - " + width + "Xx" + height +
                " start = " + this.start_index + " end = " + this.end_index;
    }

    /**
     * Gets the endOfParentContent attribute of the InlineBox object
     *
     * @return The endOfParentContent value
     */
    public abstract boolean isEndOfParentContent();

    //TODO: figure out another way to find the font, if we still need this method
    /*
     * public int getAdvance(int x, Graphics g) {
     * Font font = getFont();
     * String str = getSubstring();
     * str = str.substring(0, x);
     * //Uu.p("substring = " + str);
     * char[] chars = new char[str.length()];
     * getSubstring().getChars(0, str.length(), chars, 0);
     * FontMetrics fm = g.getFontMetrics(font);
     * //Uu.p("getting advance: " + Xx + " chars = " + chars);
     * return fm.charsWidth(chars, 0, x);
     * }
     */
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.37  2005/05/13 15:23:55  tobega
 * Done refactoring box borders, margin and padding. Hover is working again.
 *
 * Revision 1.36  2005/05/08 13:02:40  tobega
 * Fixed a bug whereby styles could get lost for inline elements, notably if root element was inline. Did a few other things which probably has no importance at this moment, e.g. refactored out some unused stuff.
 *
 * Revision 1.35  2005/04/21 18:16:08  tobega
 * Improved handling of inline padding. Also fixed first-line handling according to spec.
 *
 * Revision 1.34  2005/01/29 20:21:05  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.33  2005/01/10 01:58:37  tobega
 * Simplified (and hopefully improved) handling of vertical-align. Added support for line-height. As always, provoked a few bugs in the process.
 *
 * Revision 1.32  2005/01/07 00:29:30  tobega
 * Removed Content reference from Box (mainly to reduce memory footprint). In the process stumbled over and cleaned up some messy stuff.
 *
 * Revision 1.31  2005/01/06 21:54:32  tobega
 * Text decoration now handled in rendering only
 *
 * Revision 1.30  2005/01/06 09:49:38  tobega
 * More cleanup, aiming to remove Content reference in box
 *
 * Revision 1.29  2005/01/05 17:56:35  tobega
 * Reduced memory more, especially by using WeakHashMap for caching Mappers. Look over other caching to use similar schemes (cache when memory available).
 *
 * Revision 1.28  2005/01/05 01:10:16  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.27  2004/12/29 10:39:35  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.26  2004/12/28 02:15:19  tobega
 * More cleaning.
 *
 * Revision 1.25  2004/12/28 01:48:24  tobega
 * More cleaning. Magically, the financial report demo is starting to look reasonable, without any effort being put on it.
 *
 * Revision 1.24  2004/12/27 09:40:48  tobega
 * Moved more styling to render stage. Now inlines have backgrounds and borders again.
 *
 * Revision 1.23  2004/12/27 07:43:32  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
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

