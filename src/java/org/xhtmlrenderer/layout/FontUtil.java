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
package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.Box;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;


/**
 * Description of the Class
 *
 * @author empty
 */
public class FontUtil {

    /**
     * Description of the Method
     *
     * @param str  PARAM
     * @param font PARAM
     * @return Returns the length of the string in graphics units
     */
    public static int len(String str, Font font, TextRenderer tr, Graphics2D g2) {
        return (int) Math.ceil(tr.getLogicalBounds(g2, font, str).getWidth());
    }

    /**
     * Description of the Method
     *
     * @param c PARAM
     * @return Returns
     */
    public static int lineHeight(Box box, TextRenderer textRenderer, Graphics2D g2, CssContext c, BlockFormattingContext bfc) {
        CalculatedStyle style = box.getStyle().getCalculatedStyle();
        int val = (int) Math.ceil(textRenderer.getLogicalBounds(g2,
                box.getStyle().getFont(c), "Test").getHeight());
        if (!style.isIdent(CSSName.LINE_HEIGHT, IdentValue.NORMAL)) {
            val = (int) style.getFloatPropertyProportionalHeight(CSSName.LINE_HEIGHT, 0, c);
        }
        return val;
    }

    //TODO: this is usually not interesting unless it is an InlineTextBox!
    /**
     * Gets the lineMetrics attribute of the FontUtil class
     *
     * @param font
     * @param box  PARAM
     * @return The lineMetrics value
     */
    /*
    public static LineMetrics getLineMetrics(Font font, InlineBox box, TextRenderer tr, Graphics2D g2) {
        String sample = "Test";
        if ((box instanceof InlineTextBox) && !((InlineTextBox) box).getSubstring().equals("")) {
            sample = ((InlineTextBox) box).getSubstring();
        }
        return tr.getLineMetrics(g2,
                font, sample);
    }
    */

    /**
     * Gets the textBounds attribute of the FontUtil class
     *
     * @param c   PARAM
     * @param box PARAM
     * @return The textBounds value
     */
    /*
    public static Rectangle2D getTextBounds(CssContext c, InlineTextBox box, TextRenderer tr, Graphics2D g2) {
        return tr.getLogicalBounds(g2,
                box.getStyle().getFont(c), box.getSubstring());
    }
    */
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.44  2005/11/25 16:57:14  peterbrant
 * Initial commit of inline content refactoring
 *
 * Revision 1.43  2005/11/08 19:54:15  peterbrant
 * line-height proportional values are calculated internally now
 *
 * Revision 1.42  2005/11/07 00:07:34  tobega
 * Got text-decoration and relative inlines kind-of working
 *
 * Revision 1.41  2005/10/27 00:08:59  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.40  2005/10/18 20:57:03  tobega
 * Patch from Peter Brant
 *
 * Revision 1.39  2005/10/08 17:40:20  tobega
 * Patch from Peter Brant
 *
 * Revision 1.38  2005/06/22 23:48:44  tobega
 * Refactored the css package to allow a clean separation from the core.
 *
 * Revision 1.37  2005/06/16 07:24:50  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.36  2005/05/29 16:39:01  tobega
 * Handling of ex values should now be working well. Handling of em values improved. Is it correct?
 * Also started defining dividing responsibilities between Context and RenderingContext.
 *
 * Revision 1.35  2005/05/09 20:35:39  tobega
 * Caching fonts in CalculatedStyle
 *
 * Revision 1.34  2005/05/09 18:51:50  tobega
 * Issue number:  72
 * Fixed
 *
 * Revision 1.33  2005/05/08 14:36:57  tobega
 * Refactored away the need for having a context in a CalculatedStyle
 *
 * Revision 1.32  2005/01/29 20:21:06  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.31  2005/01/25 15:47:19  pdoubleya
 * Now uses IdentValue to check for LINE_HEIGHT.
 *
 * Revision 1.30  2005/01/25 14:45:56  pdoubleya
 * Added support for IdentValue mapping on property declarations. On both CascadedStyle and PropertyDeclaration you can now request the value as an IdentValue, for object-object comparisons. Updated 99% of references that used to get the string value of PD to return the IdentValue instead; remaining cases are for pseudo-elements where the PD content needs to be manipulated as a String.
 *
 * Revision 1.29  2005/01/24 22:46:43  pdoubleya
 * Added support for ident-checks using IdentValue instead of string comparisons.
 *
 * Revision 1.28  2005/01/24 19:01:04  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.27  2005/01/24 14:36:32  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.26  2005/01/10 01:58:36  tobega
 * Simplified (and hopefully improved) handling of vertical-align. Added support for line-height. As always, provoked a few bugs in the process.
 *
 * Revision 1.25  2005/01/07 00:29:29  tobega
 * Removed Content reference from Box (mainly to reduce memory footprint). In the process stumbled over and cleaned up some messy stuff.
 *
 * Revision 1.24  2005/01/06 09:49:37  tobega
 * More cleanup, aiming to remove Content reference in box
 *
 * Revision 1.23  2005/01/05 17:56:34  tobega
 * Reduced memory more, especially by using WeakHashMap for caching Mappers. Look over other caching to use similar schemes (cache when memory available).
 *
 * Revision 1.22  2004/12/29 10:39:32  tobega
 * Separated current state Context into LayoutContext and the rest into SharedContext.
 *
 * Revision 1.21  2004/12/14 00:32:20  tobega
 * Cleaned and fixed line breaking. Renamed BodyContent to DomToplevelNode
 *
 * Revision 1.20  2004/12/12 04:18:56  tobega
 * Now the core compiles at least. Now we must make it work right. Table layout is one point that really needs to be looked over
 *
 * Revision 1.19  2004/12/05 14:35:39  tobega
 * Cleaned up some usages of Node (and removed unused stuff) in layout code. The goal is to pass "better" objects than Node wherever possible in an attempt to shake out the bugs in tree-traversal (probably often unnecessary tree-traversal)
 *
 * Revision 1.18  2004/12/05 00:48:57  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.17  2004/11/27 15:46:38  joshy
 * lots of cleanup to make the code clearer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/11/23 02:41:59  joshy
 * fixed vertical-align support for first-letter pseudos
 * tested first-line w/ new breaking routines
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2004/11/23 02:11:24  joshy
 * re-enabled text-decoration
 * moved it to it's own class
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/11/18 23:29:37  joshy
 * fixed xml bug
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/11/12 20:25:18  joshy
 * added hover support to the browser
 * created hover demo
 * fixed bug with inline borders
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2004/11/10 17:28:54  joshy
 * initial support for anti-aliased text w/ minium
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.11  2004/11/09 16:07:57  joshy
 * moved vertical align code
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/09 02:04:23  joshy
 * support for text-align: justify
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/09 00:36:08  joshy
 * fixed more text alignment
 * added menu item to show font metrics
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/08 21:18:21  joshy
 * preliminary small-caps implementation
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2004/11/08 15:10:10  joshy
 * added support for styling :first-letter inline boxes
 * updated the absolute positioning tests
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/06 01:50:40  joshy
 * support for line-height
 * cleaned up the alice demo
 * added unit tests for font family selection and line-height
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/04 15:35:45  joshy
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
 * Revision 1.4  2004/10/23 13:46:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

