/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
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
package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;


/**
 * Description of the Class
 *
 * @author Torbjörn Gannholm
 */
public class Relative {
    /**
     * Description of the Method
     *
     * @param c PARAM
     */
    public static void translateRelative(Context c) {
        int top = 0;
        int left = 0;
        int topLeft[] = null;
        topLeft = extractLeftTopRelative(c);
        if (topLeft != null) {
            top = topLeft[0];
            left = topLeft[1];
            c.translate(left, top);
        }
    }

    /**
     * Description of the Method
     *
     * @param c PARAM
     */
    public static void untranslateRelative(Context c) {
        int top = 0;
        int left = 0;
        int topLeft[] = null;
        topLeft = extractLeftTopRelative(c);
        if (topLeft != null) {
            top = topLeft[0];
            left = topLeft[1];
            c.translate(-left, -top);
        }
    }

    /**
     * Gets the relative attribute of the Relative class
     *
     * @param c PARAM
     * @return The relative value
     */
    public static boolean isRelative(Context c) {
        return c.getCurrentStyle().isIdent(CSSName.POSITION, IdentValue.RELATIVE);
    }

    /**
     * Description of the Method
     *
     * @param c PARAM
     * @return Returns
     */
    private static int[] extractLeftTopRelative(Context c) {
        CalculatedStyle style = c.getCurrentStyle();
        int top = 0;
        int left = 0;
        int topLeft[] = null;
        if (style.isIdent(CSSName.POSITION, IdentValue.RELATIVE)) {
            if (!style.isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
                left = -(int) style.getFloatPropertyProportionalWidth(CSSName.RIGHT, c.getBlockFormattingContext().getWidth(), c.getCtx());
            }
            if (!style.isIdent(CSSName.BOTTOM, IdentValue.AUTO)) {
                top = -(int) style.getFloatPropertyProportionalHeight(CSSName.BOTTOM, c.getBlockFormattingContext().getHeight(), c.getCtx());
            }
            if (!style.isIdent(CSSName.TOP, IdentValue.AUTO)) {
                top = (int) style.getFloatPropertyProportionalHeight(CSSName.TOP, c.getBlockFormattingContext().getHeight(), c.getCtx());
            }
            if (!style.isIdent(CSSName.LEFT, IdentValue.AUTO)) {
                left = (int) style.getFloatPropertyProportionalWidth(CSSName.LEFT, c.getBlockFormattingContext().getWidth(), c.getCtx());
            }
            topLeft = new int[]{top, left};
        }
        return topLeft;
    }
}

