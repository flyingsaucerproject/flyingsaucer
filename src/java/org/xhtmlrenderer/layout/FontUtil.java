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
package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.render.InlineBox;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;


/**
 * Description of the Class
 *
 * @author empty
 */
public class FontUtil {

    /**
     * Description of the Field
     */
    static boolean quick = false;

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param str  PARAM
     * @param font PARAM
     * @return Returns
     */
    public static int len(Context c, String str, Font font) {
        //return c.getGraphics().getFontMetrics( font ).stringWidth( str );
        return (int) Math.ceil(c.getTextRenderer().getLogicalBounds(c.getGraphics(), font, str).getWidth());
    }

    public static int len(Context c, InlineBox box) {
        //return c.getGraphics().getFontMetrics(box.getFont()).stringWidth(box.getSubstring());
        return (int) Math.ceil(c.getTextRenderer().getLogicalBounds(c.getGraphics(), box.getFont(), box.getSubstring()).getWidth());
    }

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param style
     * @return Returns
     */
    public static int lineHeight(Context c, CalculatedStyle style) {
        int val = (int) Math.ceil(c.getTextRenderer().getLogicalBounds(c.getGraphics(), getFont(c, style), "Test").getHeight());
        if (style.hasProperty(CSSName.LINE_HEIGHT)) {
            val = (int) style.getFloatPropertyRelative(CSSName.LINE_HEIGHT, val);
        }
        return val;
    }

    public static int lineHeight(Context c, CalculatedStyle style, InlineBox box) {
        if (style.hasProperty(CSSName.LINE_HEIGHT)) {
            return (int) style.propertyByName(CSSName.LINE_HEIGHT).computedValue().asFloat();
        } else {
            //return c.getGraphics().getFontMetrics( box.getFont() ).getHeight();
            return (int) Math.ceil(c.getTextRenderer().getLineMetrics(c.getGraphics(), box.getFont(), "Test").getHeight());
        }
    }

    public static int lineHeight(Context c, InlineBox box) {
        if (box.line_metrics != null) {
            return (int) box.line_metrics.getHeight();
        } else {
            CalculatedStyle style = c.css.getStyle(box.getRealElement());
            return lineHeight(c, style);
        }
    }

    /**
     * PWW ADDED 14/08/04 Should be used to resolve ex properly
     *
     * @param context PARAM
     * @param style
     * @return Returns
     */
    //TODO: use this method!
    public static int fontXHeightForElement(Context context, CalculatedStyle style) {
        return lineHeight(context, style);
    }
    //TODO: add method to get font-size for a specific XHeight

    public static Font getFont(Context c, InlineBox box) {
        if (box.getFont() != null) {
            return box.getFont();
        }
        CalculatedStyle style = c.css.getStyle(box.getRealElement());
        return getFont(c, style);
    }

    public static Font getFont(Context c, CalculatedStyle style) {
        // CalculatedStyle should take care of all iheritance and default values
        // if a change is made to the basic font (by user action, for example), restyle
        Font f = c.getGraphics().getFont();
        if (quick) {
            return f;
        }

        float size = style.propertyByName(CSSName.FONT_SIZE).computedValue().asFloat();

        String weight = style.propertyByName(CSSName.FONT_WEIGHT).computedValue().asString();
        String[] families = style.propertyByName(CSSName.FONT_FAMILY).computedValue().asStringArray();

        String fstyle = style.propertyByName(CSSName.FONT_STYLE).computedValue().asString();
        String variant = style.propertyByName(CSSName.FONT_VARIANT).computedValue().asString();
        f = c.getFontResolver().resolveFont(c, families, size, weight, fstyle, variant);

        return f;
    }


    public static LineMetrics getLineMetrics(Context c, InlineBox box) {
        return c.getTextRenderer().getLineMetrics(c.getGraphics(),
                box.getFont(), box.getSubstring());
    }

    public static Rectangle2D getTextBounds(Context c, InlineBox box) {
        CalculatedStyle style = c.css.getStyle(box.getRealElement());
        return c.getTextRenderer().getLogicalBounds(c.getGraphics(),
                getFont(c, style), box.getSubstring());
    }

    public static float getDescent(Context c, InlineBox box, Font font) {
        if (box.line_metrics != null) {
            return box.line_metrics.getDescent();
        } else {
            FontMetrics fm = c.getGraphics().getFontMetrics(font);
            return fm.getDescent();
        }
    }
}

/*
 * $Id$
 *
 * $Log$
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

