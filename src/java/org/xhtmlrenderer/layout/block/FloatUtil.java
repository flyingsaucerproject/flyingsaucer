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

import java.awt.Point;

import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.Box;


/**
 * Description of the Class
 *
 * @author Torbjörn Gannholm
 */
public class FloatUtil {

    public static void preChildrenLayout(LayoutContext c, Box block) {
        BlockFormattingContext bfc = new BlockFormattingContext(block, c);
        bfc.setWidth(block.getWidth());
        c.pushBFC(bfc);
    }

    public static void postChildrenLayout(LayoutContext c) {
        c.getBlockFormattingContext().doFinalAdjustments();
        c.popBFC();
    }

    public static void setupFloat(LayoutContext c, Box box) {
        if (box.getStyle().isFloated()) {
            box.y = (int)c.getFloatingY();
            if (box.getStyle().isFloatedLeft()) {
                positionBoxLeft(c, box);
                c.getBlockFormattingContext().pushDownLeft(box);
                //Uu.p("final box = " + box);
                c.getBlockFormattingContext().addLeftFloat(box);
            } else if (box.getStyle().isFloatedRight()) {
                positionBoxRight(c, box);
                c.getBlockFormattingContext().pushDownRight(box);
                //Uu.p("final box = " + box);
                c.getBlockFormattingContext().addRightFloat(box);
            }
        }
    }

    private static void positionBoxLeft(LayoutContext c, Box box) {
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        box.x = 0;
        Box floater = bfc.getLeftFloatX(c, box);
        if (floater != null) {
            box.x = floater.x + floater.getWidth();
    
            if (box.getStyle().isClearLeft() || (box.x + box.getWidth() > box.getContainingBlock().contentWidth &&
                    box.getWidth() <= box.getContainingBlock().contentWidth)) {
                box.x = 0;
                box.y = (int)bfc.getClearDelta(c, box, floater);
                positionBoxLeft(c, box);
            }
        }
    }

    private static void positionBoxRight(LayoutContext c, Box box) {
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        box.x = box.getContainingBlock().contentWidth - box.getWidth();
        Box floater = bfc.getRightFloatX(c, box);
        if (floater != null) {
            box.x = floater.x - box.getWidth();
    
            if (box.getStyle().isClearRight() || (box.x < 0 &&
                    box.getWidth() <= c.getExtents().width)) {
                // move the box to be below the last float and
                // try it again
                box.x = box.getContainingBlock().contentWidth - box.getWidth();
                box.y = (int)bfc.getClearDelta(c, box, floater);
                positionBoxRight(c, box);
            }
        }
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.26  2005/11/02 18:15:30  peterbrant
 * First merge of Tobe's and my stacking context work / Rework float code (not done yet)
 *
 * Revision 1.25  2005/10/27 00:08:54  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.24  2005/10/18 20:56:58  tobega
 * Patch from Peter Brant
 *
 * Revision 1.23  2005/10/15 23:39:16  tobega
 * patch from Peter Brant
 *
 * Revision 1.22  2005/10/06 03:20:19  tobega
 * Prettier incremental rendering. Ran into more trouble than expected and some creepy crawlies and a few pages don't look right (forms.xhtml, splash.xhtml)
 *
 * Revision 1.21  2005/09/28 05:19:08  tobega
 * Patch from Peter Brant fixing floats and some other minor things
 *
 * Revision 1.20  2005/07/14 22:25:15  joshy
 * major updates to float code. should fix *most* issues.
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
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
