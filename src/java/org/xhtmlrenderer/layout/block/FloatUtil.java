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
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.content.ContentUtil;
import org.xhtmlrenderer.render.Box;


/**
 * Description of the Class
 *
 * @author Torbjörn Gannholm
 */
public class FloatUtil {

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param block PARAM
     */
    public static void preChildrenLayout(Context c, Box block) {
        BlockFormattingContext bfc = new BlockFormattingContext(block, c);
        bfc.setWidth(block.width);
        c.pushBFC(bfc);
    }

    /**
     * Description of the Method
     *
     * @param c PARAM
     */
    public static void postChildrenLayout(Context c) {
        c.getBlockFormattingContext().doFinalAdjustments();
        c.popBFC();
    }

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param box   PARAM
     * @param style PARAM
     */
    public static void setupFloat(Context c, Box box, CascadedStyle style) {
        if (ContentUtil.isFloated(style)) {
            // Uu.p("==== setup float ====");
            IdentValue floatVal = style.getIdent(CSSName.FLOAT);
            if (floatVal == IdentValue.NONE) {
                return;
            }
            box.floated = true;
            if (floatVal == IdentValue.LEFT) {
                positionBoxLeft(c, box);
                c.getBlockFormattingContext().pushDownLeft(box);
                // Uu.p("final box = " + box);
                c.getBlockFormattingContext().addLeftFloat(box);
            }
            if (floatVal == IdentValue.RIGHT) {
                positionBoxRight(c, box);
                c.getBlockFormattingContext().pushDownRight(box);
                // Uu.p("final box = " + box);
                c.getBlockFormattingContext().addRightFloat(box);
            }
            // Uu.p("box = " + box);
            // Uu.p("==== end setup ====");
        }
    }

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    private static void positionBoxLeft(Context c, Box box) {
        // Uu.p("positionBoxLeft()");
        // Uu.p("calling the new float routine");
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        Box floater = bfc.getLeftFloatX(box);
        // Uu.p("floater = " + floater);
        // Uu.p("extents = " + c.getExtents());
        if (floater == null) {
            // Uu.p("no floater blocked. returning");
            box.x = 0;
            return;
        }

        box.x = floater.x + floater.width;

        if (box.x + box.width > c.getExtents().width &&
                box.width <= c.getExtents().width) {
            // Uu.p("not enough room!!!");
            // move the box to be below the last float and
            // try it again
            box.y = floater.y + floater.height;
            // Uu.p("trying again with box: " + box);
            positionBoxLeft(c, box);
            // Uu.p("final box = " + box);
        }
    }

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    private static void positionBoxRight(Context c, Box box) {
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        Box floater = bfc.getRightFloatX(box);
        if (floater == null) {
            // Uu.p("floaters are null");
            // Uu.p("extents = " + c.getExtents().width);
            box.x = c.getExtents().width - box.width;
            return;
        }

        box.x = floater.x - box.width;

        if (box.x < 0 &&
                box.width <= c.getExtents().width) {
            // Uu.p("not enough room!!!");
            // move the box to be below the last float and
            // try it again
            box.y = floater.y + floater.height;
            positionBoxRight(c, box);
        }
        // Uu.p("final box = " + box);
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.19  2005/06/16 07:24:49  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.18  2005/05/13 15:23:53  tobega
 * Done refactoring box borders, margin and padding. Hover is working again.
 *
 * Revision 1.17  2005/02/03 23:14:53  pdoubleya
 * .
 *
 *
 */
