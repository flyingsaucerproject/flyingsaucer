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
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.Boxing;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.util.Uu;

import java.awt.Rectangle;


/**
 * Description of the Class
 *
 * @author Torbjörn Gannholm
 */
public class Absolute {

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param block PARAM
     */
    public static void preChildrenLayout(LayoutContext c, Box block) {
        BlockFormattingContext bfc = new BlockFormattingContext(block, c);
        bfc.setWidth(block.getWidth());
        c.pushBFC(bfc);
    }

    /**
     * Description of the Method
     *
     * @param c PARAM
     */
    public static void postChildrenLayout(LayoutContext c) {
        c.getBlockFormattingContext().doFinalAdjustments();
        c.popBFC();
    }

    /**
     * Description of the Method
     *
     * @param c         PARAM
     * @param child_box PARAM
     */
    public static void positionAbsoluteChild(LayoutContext c, Box child_box) {
        Uu.p("positioning an absolute child: " + child_box);
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        // handle the left and right
        if (child_box.right_set) {
            child_box.x = -bfc.getX() + bfc.getWidth() - child_box.right - child_box.getWidth()
                    - bfc.getInsets().right;
        } else {
            child_box.x = bfc.getX() + child_box.left;
        }

        // handle the top and bottom
        if (child_box.bottom_set) {
            // can't actually do this part yet, so save for later
            bfc.addAbsoluteBottomBox(child_box);
        } else {
            // top positioning
            child_box.y = bfc.getY() + child_box.top;
        }
    }


    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content PARAM
     * @return Returns
     */
    public static Box generateAbsoluteBox(LayoutContext c, Content content) {
        //Uu.p("generate absolute block inline box: avail = " + content);
        Rectangle oe = c.getExtents();// copy the extents for safety
        c.setExtents(new Rectangle(oe));

        Box box = Boxing.layout(c, content);

        //Uu.p("got a block box from the sub layout: " + box);
        c.setExtents(oe);

        return box;
    }

    /**
     * Description of the Method
     *
     * @param box PARAM
     * @param c   PARAM
     */
    public static void setupAbsolute(Box box, LayoutContext c) {
        //Uu.p("setting up an abs for box: " +box);
        CalculatedStyle style = c.getCurrentStyle();
        if (style.isIdent(CSSName.POSITION, IdentValue.ABSOLUTE)) {
            //Uu.p("is absolute pos");
            if (!style.isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
                box.right = (int) style.getFloatPropertyProportionalWidth(CSSName.RIGHT, c.getBlockFormattingContext().getWidth(), c);
                box.right_set = true;
                //Uu.p("right set to : " + box.right);
            }
            if (!style.isIdent(CSSName.LEFT, IdentValue.AUTO)) {
                box.left = (int) style.getFloatPropertyProportionalWidth(CSSName.LEFT, c.getBlockFormattingContext().getWidth(), c);
                box.left_set = true;
                //Uu.p("left set to : " + box.left);
            }

            if (!style.isIdent(CSSName.BOTTOM, IdentValue.AUTO)) {
                box.top = (int) style.getFloatPropertyProportionalHeight(CSSName.BOTTOM, c.getBlockFormattingContext().getHeight(), c);
                box.bottom_set = true;
            }
            if (!style.isIdent(CSSName.TOP, IdentValue.AUTO)) {
                box.top = (int) style.getFloatPropertyProportionalHeight(CSSName.TOP, c.getBlockFormattingContext().getHeight(), c);
                box.top_set = true;
                //Uu.p("set top to: " + box.top + " " + box.top_set);
            }
            
            // if right and left are set calculate width
            if (box.right_set && box.left_set) {
                //TODO: do this right
                box.contentWidth = box.contentWidth - box.right - box.left;
            }
        }
    }
}

