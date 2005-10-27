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
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.RenderingContext;


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
    public static void translateRelative(RenderingContext c, CalculatedStyle activeStyle, boolean rendering) {
        int top = 0;
        int left = 0;
        int topLeft[] = null;
        topLeft = extractLeftTopRelative(c, c.getBlockFormattingContext(), activeStyle);
        if (topLeft != null) {
            top = topLeft[0];
            left = topLeft[1];
            c.translate(left, top);
            if (rendering) {
                c.getGraphics().translate(left, top);
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param c PARAM
     */
    public static void translateRelative(LayoutContext c, CalculatedStyle activeStyle, boolean rendering) {
        int top = 0;
        int left = 0;
        int topLeft[] = null;
        topLeft = extractLeftTopRelative(c, c.getBlockFormattingContext(), activeStyle);
        if (topLeft != null) {
            top = topLeft[0];
            left = topLeft[1];
            c.translate(left, top);
            if (rendering) {
                c.getGraphics().translate(left, top);
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param c PARAM
     */
    public static void untranslateRelative(RenderingContext c, CalculatedStyle activeStyle, boolean rendering) {
        int top = 0;
        int left = 0;
        int topLeft[] = null;
        topLeft = extractLeftTopRelative(c, c.getBlockFormattingContext(), activeStyle);
        if (topLeft != null) {
            top = topLeft[0];
            left = topLeft[1];
            c.translate(-left, -top);
            if (rendering) {
                c.getGraphics().translate(-left, -top);
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param c PARAM
     */
    public static void untranslateRelative(LayoutContext c, CalculatedStyle activeStyle, boolean rendering) {
        int top = 0;
        int left = 0;
        int topLeft[] = null;
        topLeft = extractLeftTopRelative(c, c.getBlockFormattingContext(), activeStyle);
        if (topLeft != null) {
            top = topLeft[0];
            left = topLeft[1];
            c.translate(-left, -top);
            if (rendering) {
                c.getGraphics().translate(-left, -top);
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param c PARAM
     * @return Returns
     */
    private static int[] extractLeftTopRelative(CssContext c, BlockFormattingContext bfc, CalculatedStyle style) {
        int top = 0;
        int left = 0;
        int topLeft[] = null;
        if (style.isIdent(CSSName.POSITION, IdentValue.RELATIVE)) {
            if (!style.isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
                left = -(int) style.getFloatPropertyProportionalWidth(CSSName.RIGHT, bfc.getWidth(), c);
            }
            if (!style.isIdent(CSSName.BOTTOM, IdentValue.AUTO)) {
                top = -(int) style.getFloatPropertyProportionalHeight(CSSName.BOTTOM, bfc.getHeight(), c);
            }
            if (!style.isIdent(CSSName.TOP, IdentValue.AUTO)) {
                top = (int) style.getFloatPropertyProportionalHeight(CSSName.TOP, bfc.getHeight(), c);
            }
            if (!style.isIdent(CSSName.LEFT, IdentValue.AUTO)) {
                left = (int) style.getFloatPropertyProportionalWidth(CSSName.LEFT, bfc.getWidth(), c);
            }
            topLeft = new int[]{top, left};
        }
        return topLeft;
    }
}

