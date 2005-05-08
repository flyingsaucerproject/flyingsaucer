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
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.render.Box;

import java.awt.Point;
import java.awt.Rectangle;


/**
 * Description of the Class
 *
 * @author Torbjörn Gannholm
 */
public class Fixed {
    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public static void positionFixedChild(Context c, Box box) {
        if (LayoutUtil.isFixed(c.getCss().getCascadedStyle(box.element, false))) {//already restyled by ContentUtil
            Point origin = c.getOriginOffset();
            box.x = 0;
            box.y = 0;
            box.x -= origin.x;
            box.y -= origin.y;
        }
    }

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public static void setupFixed(Context c, Box box) {
        if (c.getCurrentStyle().isIdent(CSSName.POSITION, IdentValue.FIXED)) {
            box.fixed = true;
            box.setChildrenExceedBounds(true);
            Rectangle rect = c.getFixedRectangle();

            CalculatedStyle style = c.getCurrentStyle();
            if (style.hasProperty(CSSName.TOP)) {
                box.top = (int) style.getFloatPropertyProportionalHeight(CSSName.TOP, (float) (rect.getHeight()), c.getCtx());
                box.top_set = true;
            }
            if (style.hasProperty(CSSName.RIGHT)) {
                box.right = (int) style.getFloatPropertyProportionalWidth(CSSName.RIGHT, (float) (rect.getWidth()), c.getCtx());
                box.right_set = true;
            }
            if (style.hasProperty(CSSName.BOTTOM)) {
                box.bottom = (int) style.getFloatPropertyProportionalHeight(CSSName.BOTTOM, (float) (rect.getHeight()), c.getCtx());
                box.bottom_set = true;
            }
            if (style.hasProperty(CSSName.LEFT)) {
                box.left = (int) style.getFloatPropertyProportionalWidth(CSSName.LEFT, (float) (rect.getWidth()), c.getCtx());
                box.left_set = true;
            }
        }
    }
}

