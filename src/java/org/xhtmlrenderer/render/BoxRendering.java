/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci, Torbjšrn Gannholm
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

import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Boxing;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.block.Relative;
import org.xhtmlrenderer.layout.content.ContentUtil;
import org.xhtmlrenderer.table.TableBox;
import org.xhtmlrenderer.table.TableRendering;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.GraphicsUtil;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.Uu;

import java.awt.*;

public class BoxRendering {

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public static void paint(Context c, Box box) {
        // Uu.p("BoxRenderer.paint " + box);
        Box block = (Box) box;

        //set the current style
        //CascadedStyle hoverStyle = null;
        CascadedStyle style = null;
        if (block.element != null) style = c.getCss().getCascadedStyle(block.element);
        if (style != null) {
            c.pushStyle(style);
            /*if (block.hover) {
                hoverStyle = c.getCss().getPseudoElementStyle(block.content.getElement(), "hover");
                if (hoverStyle != null) c.pushStyle(hoverStyle);
            }*/
        }

        // copy the bounds to we don't mess it up
        Rectangle oldBounds = new Rectangle(c.getExtents());

        if (Relative.isRelative(c)) {
            paintRelative(c, block);
        } else if (block.fixed) {
            paintFixed(c, block);
        } else if (block.absolute) {
            paintAbsoluteBox(c, block);
        } else {
            paintNormal(c, block);
        }

        //Uu.p("here it's : " + c.getListCounter());
        if (ContentUtil.isListItem(style)) {
            paintListItem(c, box);
        }

        // move the origin down to account for the contents plus the margin, borders, and padding
        oldBounds.y = oldBounds.y + block.height;
        c.setExtents(oldBounds);

        //reset style
        if (style != null) {
            //if (hoverStyle != null) c.popStyle();
            c.popStyle();
        }


        if (c.debugDrawBoxes() ||
                Configuration.isTrue("xr.renderer.debug.box-outlines", true)) {
            GraphicsUtil.drawBox(c.getGraphics(), block, Color.red);
        }
    }


    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param block PARAM
     */
    public static void paintNormal(Context c, Box block) {
        paintBackground(c, block);

        if (!(block instanceof AnonymousBlockBox)) c.translateInsets(block);
        //paintComponent(c, block);
        //paintChildren(c, block);
        if (block instanceof TableBox) {
            TableRendering.paintTable(c, (TableBox) block);
        } else if (isInlineLayedOut(block)) {
            InlineRendering.paintInlineContext(c, block);
        } else {
            BlockRendering.paintBlockContext(c, block);
        }
        if (!(block instanceof AnonymousBlockBox)) c.untranslateInsets(block);

        if (!(block instanceof AnonymousBlockBox)) paintBorder(c, block);
    }

    // adjustments for relative painting
    /**
     * Description of the Method
     *
     * @param ctx   PARAM
     * @param block PARAM
     */
    public static void paintRelative(Context ctx, Box block) {
        //ctx.translate(block.left, block.top);
        Relative.translateRelative(ctx);
        paintNormal(ctx, block);
        //ctx.translate(-block.left, -block.top);
        Relative.untranslateRelative(ctx);
    }

    // adjustments for fixed painting
    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param block PARAM
     */
    public static void paintFixed(Context c, Box block) {
        Rectangle rect = c.getFixedRectangle();
        //Uu.p("rect = " + rect);
        //Graphics g = c.getGraphics();
        int xoff = -rect.x;
        int yoff = -rect.y;

        if (block.top_set) {
            yoff += block.top;
        }
        if (block.right_set) {
            xoff = -rect.x + rect.width - block.width - block.right;
        }
        if (block.left_set) {
            xoff = block.left;
        }
        if (block.bottom_set) {
            yoff = -rect.y + rect.height - block.height - block.bottom;
        }
        c.translate(xoff, yoff);
        paintNormal(c, block);
        c.translate(-xoff, -yoff);
    }

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param block PARAM
     */
    //HACK: more or less copied paintFixed - tobe
    public static void paintAbsoluteBox(Context c, Box block) {
        Rectangle rect = c.getExtents();
        //why this?
        int xoff = -rect.x;
        int yoff = -rect.y;

        if (block.top_set) {
            yoff += block.top;
        }
        if (block.right_set) {
            xoff = -rect.x + rect.width - block.width - block.right;
        }
        if (block.left_set) {
            xoff = block.left;
        }
        if (block.bottom_set) {
            yoff = -rect.y + rect.height - block.height - block.bottom;
        }
        c.translate(xoff, yoff);
        paintNormal(c, block);
        c.translate(-xoff, -yoff);
    }


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public static void paintBackground(Context c, Box box) {
        Box block = box;

        /*if (!LayoutUtil.shouldDrawBackground(block)) {
            //Uu.p("skipping: " + block);
            return;
        }*/


        // cache the background color
        getBackgroundColor(c, block);

        // get the css properties
        CalculatedStyle style = c.getCurrentStyle();
        String back_image = style.getStringProperty("background-image");
        block.repeat = style.getStringProperty("background-repeat");
        block.attachment = style.getStringProperty("background-attachment");
        // handle image positioning issues
        // need to update this to support vert and horz, not just vert
        if (style.hasProperty("background-position")) {
            Point pt = style.getBackgroundPosition();
            block.background_position_horizontal = (int) pt.getX();
            block.background_position_vertical = (int) pt.getY();
        }

        // load the background image
        block.background_image = null;
        if (back_image != null && !"none".equals(back_image)) {
            try {
                block.background_image = ImageUtil.loadImage(c, back_image);
            } catch (Exception ex) {
                ex.printStackTrace();
                Uu.p(ex);
            }
            /*
             * ImageIcon icon = new ImageIcon(back_image);
             * if(icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
             * block.background_image = icon.getImage();
             * }
             */
        }

        // actually paint the background
        BackgroundPainter.paint(c, block);
    }

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    /*public void paintChildren(Context c, Box box) {
        if (box.getBlockFormattingContext() != null) c.pushBFC(box.getBlockFormattingContext());
        c.translate(box.x, box.y);
        super.paintChildren(c, box);
        c.translate(-box.x, -box.y);
        if (box.getBlockFormattingContext() != null) c.popBFC();
    }*/


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public static void paintBorder(Context c, Box box) {
        Box block = box;
        // get the border parts

        // paint the border
        BorderPainter bp = new BorderPainter();

        // adjust to a fixed height, if necessary
        //if (!block.auto_height) {
        //bnds.y = block.height - block.margin.top - block.margin.bottom;
        //}

        bp.paint(c, block);
    }

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public static void paintListItem(Context c, Box box) {
        ListItemPainter.paint(c, box);
    }

// --Commented out by Inspection START (2005-01-05 01:06):
//    public static Border getBorder(Context c, Box box) {
//        return Boxing.getBorder(c, box);
//    }
// --Commented out by Inspection STOP (2005-01-05 01:06)

// --Commented out by Inspection START (2005-01-05 01:06):
//    public static Border getPadding(Context c, Box box) {
//        return Boxing.getPadding(c, box);
//    }
// --Commented out by Inspection STOP (2005-01-05 01:06)

    public static Color getBackgroundColor(Context c, Box box) {
        return Boxing.getBackgroundColor(c, box);
    }

// --Commented out by Inspection START (2005-01-05 01:06):
//    public static Border getMargin(Context c, Box box) {
//        return Boxing.getMargin(c, box);
//    }
// --Commented out by Inspection STOP (2005-01-05 01:06)


// --Commented out by Inspection START (2005-01-05 01:06):
//    //TODO: check the logic here
//    public static boolean isBlockLayedOut(Box box) {
//        if (box.getChildCount() == 0) return false;//have to return something, it shouldn't matter
//        for (int i = 0; i < box.getChildCount(); i++) {
//            Box child = box.getChild(i);
//            if (child instanceof LineBox) return false;
//            if (child instanceof InlineBox) return false;
//            if (child instanceof InlineBlockBox) return false;
//        }
//        return true;
//    }
// --Commented out by Inspection STOP (2005-01-05 01:06)

    //TODO: check the logic here
    public static boolean isInlineLayedOut(Box box) {
        if (box.getChildCount() == 0) return false;//have to return something, it shouldn't matter
        for (int i = 0; i < box.getChildCount(); i++) {
            Box child = box.getChild(i);
            if (child instanceof LineBox) return true;
        }
        return false;
    }
}
