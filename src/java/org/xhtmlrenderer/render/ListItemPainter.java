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
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.Uu;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;

/**
 * Description of the Class
 *
 * @author empty
 */
public class ListItemPainter {
    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public static void paint(Context c, Box box) {
        String type = c.css.getStyle(box.getNode()).getStringProperty("list-style-type");

        if (type.equals("none")) {
            return;
        }
        if (type.equals("lower-greek")) {
            type = "decimal";
        }
        if (type.equals("decimal-leading-zero")) {
            type = "decimal";
        }

        String image = c.css.getStyle(box.getNode()).getStringProperty("list-style-image");
        Image img = null;
        if (!image.equals("none")) {
            try {
                //Uu.p("loading: " + image);
                img = ImageUtil.loadImage(c, image);
            } catch (Exception ex) {
                Uu.p(ex);
            }
            //Uu.p("image = " + img);
            if (img != null) {
                int rad = 8;
                int baseline = box.height;
                c.getGraphics().drawImage(img, box.x - img.getWidth(null) - 2, box.y + baseline / 2 - img.getHeight(null) / 2 + 2, null);
                return;
            }
        }

        // prep the color
        box.color = c.css.getStyle(box.getRealElement()).getColor();
        c.getGraphics().setColor(box.color);
        
        // save the old AntiAliasing setting, then force it on
        Object aa_key = c.getGraphics().getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        c.getGraphics().setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        
        // calculations for bullets
        int rad = 8;// change this to use the glyph height
        //not used: int baseline = box.height;// change this to use the real baseline
        CalculatedStyle style = c.css.getStyle(box.getRealElement());
        //not used: Font font = FontUtil.getFont(c, style);
        //Font font = FontUtil.getFont(c, box.getRealElement());
        int h = FontUtil.lineHeight(c, style);
        rad = h / 3;
        int x = box.x - rad - rad / 2;
        int y = box.y + (h - rad / 2) / 2;
        if (type.equals("disc")) {
            c.getGraphics().fillOval(x, y, rad, rad);
            return;
        }
        if (type.equals("square")) {
            c.getGraphics().fillRect(x, y, rad, rad);
            return;
        }
        if (type.equals("circle")) {
            c.getGraphics().drawOval(x, y, rad, rad);
            return;
        }

        // restore the old AntiAliasing setting
        c.getGraphics().setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                aa_key);



        // calculations for text
        if (type.equals("decimal")) {
            drawText(c, box, type);
            return;
        }

        if (type.equals("lower-latin")) {
            drawText(c, box, type);
            return;
        }

        if (type.equals("upper-latin")) {
            drawText(c, box, type);
            return;
        }

        if (type.equals("lower-roman")) {
            drawText(c, box, type);
            return;
        }

        if (type.equals("upper-roman")) {
            drawText(c, box, type);
            return;
        }
    }

    private static void drawText(Context c, Box box, String type) {
        String text = "";
        if (type.equals("decimal")) {
            text = box.list_count + ".";
        }
        if (type.equals("lower-latin")) {
            text = toLatin(box.list_count).toLowerCase() + ".";
        }

        if (type.equals("upper-latin")) {
            text = toLatin(box.list_count).toUpperCase() + ".";
        }

        if (type.equals("lower-roman")) {
            text = toRoman(box.list_count).toLowerCase() + ".";
        }

        if (type.equals("upper-roman")) {
            text = toRoman(box.list_count).toUpperCase() + ".";
        }

        CalculatedStyle style = c.css.getStyle(box.getRealElement());
        Font font = FontUtil.getFont(c, style);
        LineMetrics lm = font.getLineMetrics(text, ((Graphics2D) c.getGraphics()).getFontRenderContext());
        int w = FontUtil.len(c, text, font);
        int h = FontUtil.lineHeight(c, style);
        int x = box.x - w - 2;
        int y = box.y + h;
        y -= (int) lm.getDescent();
        c.getGraphics().setFont(font);
        c.getGraphics().drawString(text, x, y);
    }

    /**
     * Description of the Method
     *
     * @param val PARAM
     * @return Returns
     */
    protected static String toLatin(int val) {
        if (val > 26) {
            int val1 = val % 26;
            int val2 = val / 26;
            return toLatin(val2) + toLatin(val1);
        }
        return ((char) (val + 64)) + "";
    }

    /**
     * Description of the Method
     *
     * @param val PARAM
     * @return Returns
     */
    protected static String toRoman(int val) {
        int[] ints = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] nums = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "Xx", "IX", "V", "IV", "I"};
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
}

/*
 * $Id$
 *
 * $Log$
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
 *  bullets and numbers are sized correctly now
 *  numbers have periods after them
 *  adjusted ul/ol margins to use ems
 *  positioned bullets and numbers on the top line
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

