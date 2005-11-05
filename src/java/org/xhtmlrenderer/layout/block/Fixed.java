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
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.Box;

import java.awt.Point;
import java.awt.Rectangle;


/**
 * @author Torbjörn Gannholm
 */
public class Fixed {
    public static void positionFixedChild(LayoutContext c, Box box) {
        if (box.getStyle().isFixed()) {//already restyled by ContentUtil
            Point origin = c.getOriginOffset();
            box.x = 0;
            box.y = 0;
            box.x -= origin.x;
            box.y -= origin.y;
            box.setFixedDescendant(true);
        }
    }

    public static void setupFixed(LayoutContext c, Box box) {
        if (box.getStyle().isFixed()) {
            box.setFixedDescendant(true);
            Rectangle rect = c.getFixedRectangle();

            CalculatedStyle style = c.getCurrentStyle();
            if (!style.isIdent(CSSName.TOP, IdentValue.AUTO)) {
                box.top = (int) style.getFloatPropertyProportionalHeight(CSSName.TOP, (float) (rect.getHeight()), c);
                box.top_set = true;
            }
            if (!style.isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
                box.right = (int) style.getFloatPropertyProportionalWidth(CSSName.RIGHT, (float) (rect.getWidth()), c);
                box.right_set = true;
            }
            if (!style.isIdent(CSSName.BOTTOM, IdentValue.AUTO)) {
                box.bottom = (int) style.getFloatPropertyProportionalHeight(CSSName.BOTTOM, (float) (rect.getHeight()), c);
                box.bottom_set = true;
            }
            if (!style.isIdent(CSSName.LEFT, IdentValue.AUTO)) {
                box.left = (int) style.getFloatPropertyProportionalWidth(CSSName.LEFT, (float) (rect.getWidth()), c);
                box.left_set = true;
            }
        }
    }
}

