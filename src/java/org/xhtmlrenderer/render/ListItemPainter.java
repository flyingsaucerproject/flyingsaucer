/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 * Copyright (c) 2005 Wisconsin Court System
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

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.FontUtil;

import java.awt.Font;
import java.awt.Image;
import java.awt.RenderingHints;

public class ListItemPainter {
    public static void paint(RenderingContext c, BlockBox box) {
        CalculatedStyle style = box.getStyle().getCalculatedStyle();
        IdentValue listStyle = style.getIdent(CSSName.LIST_STYLE_TYPE);

        if (listStyle == IdentValue.NONE) {
            return;
        }

        String image = style.getStringProperty(CSSName.LIST_STYLE_IMAGE);
        Image img = null;
        if (! image.equals("none")) {
            img = c.getUac().getImageResource(image).getImage();
            if (img != null) {
                StrutMetrics strutMetrics = box.getStructMetrics();
                if (img.getHeight(null) > strutMetrics.getAscent()) {
                    img = img.getScaledInstance(-1, (int)strutMetrics.getAscent(), Image.SCALE_FAST);
                }
                c.getGraphics().drawImage(img, 
                        box.getAbsX() - img.getWidth(null) * 5/4, 
                        (int)(box.getAbsY() + box.ty + strutMetrics.getBaseline()
                            - strutMetrics.getAscent() / 2 - img.getHeight(null) / 2),
                        null);
            }
        } else {
            // prep the color
            c.getGraphics().setColor(style.getColor());
    
            if (listStyle == IdentValue.DISC || listStyle == IdentValue.SQUARE ||
                    listStyle == IdentValue.CIRCLE) {
                drawGlyph(c, box, style, listStyle);
            } else {
                drawText(c, box, listStyle);
            }
        }
    }

    private static void drawGlyph(RenderingContext c, BlockBox box, CalculatedStyle style, IdentValue listStyle) {
        // save the old AntiAliasing setting, then force it on
        Object aa_key = c.getGraphics().getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        c.getGraphics().setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // calculations for bullets
        StrutMetrics strutMetrics = box.getStructMetrics();
        int h = (int) Math.round(strutMetrics.getAscent());
        int rad = (int)((strutMetrics.getAscent() + strutMetrics.getDescent()) / 3);
        int x = box.getAbsX() - rad * 5 / 2;
        int y = box.getAbsY() + box.ty + strutMetrics.getBaseline() - h / 2 - rad / 2;
        if (listStyle == IdentValue.DISC) {
            c.getGraphics().fillOval(x, y, rad, rad);
        } else if (listStyle == IdentValue.SQUARE) {
            c.getGraphics().fillRect(x, y, rad, rad);
        } else if (listStyle == IdentValue.CIRCLE) {
            c.getGraphics().drawOval(x, y, rad, rad);
        }

        // restore the old AntiAliasing setting
        c.getGraphics().setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                aa_key);
    }

    protected static String toLatin(int val) {
        if (val > 26) {
            int val1 = val % 26;
            int val2 = val / 26;
            return toLatin(val2) + toLatin(val1);
        }
        return ((char) (val + 64)) + "";
    }

    protected static String toRoman(int val) {
        int[] ints = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] nums = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ints.length; i++) {
            int count = (int) (val / ints[i]);
            for (int j = 0; j < count; j++) {
                sb.append(nums[i]);
            }
            val -= ints[i] * count;
        }
        return sb.toString();
    }

    private static void drawText(RenderingContext c, BlockBox box, IdentValue listStyle) {
        String text = "";

        if (listStyle == IdentValue.LOWER_LATIN || listStyle == IdentValue.LOWER_ALPHA) {
            text = toLatin(box.list_count).toLowerCase() + ".";
        } else if (listStyle == IdentValue.UPPER_LATIN || listStyle == IdentValue.UPPER_ALPHA) {
            text = toLatin(box.list_count).toUpperCase() + ".";
        } else if (listStyle == IdentValue.LOWER_ROMAN) {
            text = toRoman(box.list_count).toLowerCase() + ".";
        } else if (listStyle == IdentValue.UPPER_ROMAN) {
            text = toRoman(box.list_count).toUpperCase() + ".";
        } else if (listStyle == IdentValue.DECIMAL) {
            text = box.list_count + ".";
        }
        
        text += "  ";

        Font font = box.getStyle().getFont(c);
        StrutMetrics strutMetrics = box.getStructMetrics();
        
        int w = FontUtil.len(text, font, c.getTextRenderer(), c.getGraphics());
        
        int x = box.getAbsX() - w;
        int y = box.getAbsY() + box.ty + strutMetrics.getBaseline();
        
        c.getGraphics().setFont(font);
        c.getGraphics().drawString(text, x, y);
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.25  2005/12/09 17:59:29  peterbrant
 * Make text and glyph positioning closer to Firefox
 *
 * Revision 1.24  2005/12/05 00:13:53  peterbrant
 * Improve list-item support (marker positioning is now correct) / Start support for relative inline layers
 *
 * Revision 1.23  2005/11/08 22:53:46  tobega
 * added getLineHeight method to CalculatedStyle and hacked in some list-item support
 *
 * Revision 1.22  2005/10/27 00:09:04  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.21  2005/10/18 20:57:07  tobega
 * Patch from Peter Brant
 *
 * Revision 1.20  2005/06/25 17:23:34  tobega
 * first refactoring of UAC: ImageResource
 *
 * Revision 1.19  2005/06/22 23:48:46  tobega
 * Refactored the css package to allow a clean separation from the core.
 *
 * Revision 1.18  2005/06/01 21:36:40  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.17  2005/05/30 01:04:00  tobega
 * Fixed a regression on list-item numbering (basically getting rid of unnecessary style-pushes and style-pops)
 *
 * Revision 1.16  2005/01/29 20:21:04  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.15  2005/01/24 22:46:42  pdoubleya
 * Added support for ident-checks using IdentValue instead of string comparisons.
 *
 * Revision 1.14  2005/01/24 19:01:03  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.13  2005/01/10 01:58:37  tobega
 * Simplified (and hopefully improved) handling of vertical-align. Added support for line-height. As always, provoked a few bugs in the process.
 *
 * Revision 1.12  2004/12/29 10:39:35  tobega
 * Separated current state Context into LayoutContext and the rest into SharedContext.
 *
 * Revision 1.11  2004/12/28 02:15:19  tobega
 * More cleaning.
 *
 * Revision 1.10  2004/12/12 04:18:58  tobega
 * Now the core compiles at least. Now we must make it work right. Table layout is one point that really needs to be looked over
 *
 * Revision 1.9  2004/12/12 03:33:01  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.8  2004/12/05 14:35:40  tobega
 * Cleaned up some usages of Node (and removed unused stuff) in layout code. The goal is to pass "better" objects than Node wherever possible in an attempt to shake out the bugs in tree-traversal (probably often unnecessary tree-traversal)
 *
 * Revision 1.7  2004/12/05 05:18:02  joshy
 * made bullets be anti-aliased
 * fixed bug in link listener that caused NPEs
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/12/05 00:48:59  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.5  2004/11/27 15:46:40  joshy
 * lots of cleanup to make the code clearer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/11/10 16:28:53  joshy
 * fixes for list items
 * bullets and numbers are sized correctly now
 * numbers have periods after them
 * adjusted ul/ol margins to use ems
 * positioned bullets and numbers on the top line
 * adjusted splash page
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 13:50:27  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

