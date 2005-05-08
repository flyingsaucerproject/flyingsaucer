/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjšrn Gannholm
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

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.BlockFormattingContext;
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

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;


/**
 * Description of the Class
 *
 * @author Joshua Marinacci
 * @author Torbjörn Gannholm
 */
public class BoxRendering {

    /**
     * Description of the Method
     *
     * @param c           PARAM
     * @param box         PARAM
     * @param stylePushed
     * @param restyle
     */
    //HACK: the stylePushed is because we need to set style for inline blocks earlier
    public static void paint(Context c, Box box, boolean stylePushed, boolean restyle) {
        Box block = (Box) box;
        restyle = restyle || box.restyle;//cascade it down
        box.restyle = false;//reset

        //set the current style
        CascadedStyle style = null;
        if (!stylePushed && block.element != null) {
            style = c.getCss().getCascadedStyle(block.element, restyle);
        }
        if (style != null) {
            c.pushStyle(style);
        }

        // copy the bounds to we don't mess it up
        Rectangle oldBounds = new Rectangle(c.getExtents());

        if (Relative.isRelative(c)) {
            paintRelative(c, block, restyle);
        } else if (block.fixed) {
            paintFixed(c, block, restyle);
        } else if (block.absolute) {
            paintAbsoluteBox(c, block, restyle);
        } else {
            paintNormal(c, block, restyle);
        }

        //Uu.p("here it's : " + c.getListCounter());
        if (ContentUtil.isListItem(style)) {
            paintListItem(c, box);
        }

        // move the origin down to account for the contents plus the margin, borders, and padding
        if (!box.absolute) {
            oldBounds.y = oldBounds.y + block.height;
            c.setExtents(oldBounds);
        }

        //reset style
        if (style != null) {
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
     * @param c       PARAM
     * @param block   PARAM
     * @param restyle
     */
    public static void paintNormal(Context c, Box block, boolean restyle) {
        paintBackground(c, block);

        if (!(block instanceof AnonymousBlockBox)) {
            c.translateInsets(block);
        }
        if (block instanceof TableBox) {
            TableRendering.paintTable(c, (TableBox) block, restyle);
        } else if (isInlineLayedOut(block)) {
            InlineRendering.paintInlineContext(c, block, restyle);
        } else {
            BlockRendering.paintBlockContext(c, block, restyle);
        }
        if (!(block instanceof AnonymousBlockBox)) {
            c.untranslateInsets(block);
        }

        if (!(block instanceof AnonymousBlockBox)) {
            int width = block.getWidth();
            int height = block.getHeight();
            Border margin = c.getCurrentStyle().getMarginWidth(width, height, c.getCtx());

            Rectangle bounds = new Rectangle(block.x + margin.left,
                    block.y + margin.top,
                    block.width - margin.left - margin.right,
                    block.height - margin.top - margin.bottom);
            BorderPainter.paint(c, bounds, BorderPainter.ALL);
        }
    }

    // adjustments for relative painting
    /**
     * Description of the Method
     *
     * @param ctx     PARAM
     * @param block   PARAM
     * @param restyle
     */
    public static void paintRelative(Context ctx, Box block, boolean restyle) {
        Relative.translateRelative(ctx);
        paintNormal(ctx, block, restyle);
        Relative.untranslateRelative(ctx);
    }

    // adjustments for fixed painting
    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param block   PARAM
     * @param restyle
     */
    public static void paintFixed(Context c, Box block, boolean restyle) {
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
        paintNormal(c, block, restyle);
        c.translate(-xoff, -yoff);

    }

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param block   PARAM
     * @param restyle
     */
    //HACK: more or less copied paintFixed - tobe
    //TODO: paint fixed & absolute are duplicates code blocks--need to decide how they differ, or leave as common method (PWW 25-01-05)
    //Fixed to use the BFC to calculate the absolute position
    public static void paintAbsoluteBox(Context c, Box block, boolean restyle) {
        Rectangle rect = c.getExtents();
        //why this?
		
        int xoff = 0;
        int yoff = 0;
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        xoff += bfc.getX();
        yoff += bfc.getY();
        xoff += bfc.getMaster().getPaddingEdge().left;
        yoff += bfc.getMaster().getPaddingEdge().top;


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
        paintNormal(c, block, restyle);
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

        // cache the background color
        getBackgroundColor(c);

        // get the css properties
        CalculatedStyle style = c.getCurrentStyle();
        String back_image = style.getStringProperty(CSSName.BACKGROUND_IMAGE);
        block.repeat = style.getIdent(CSSName.BACKGROUND_REPEAT);
        block.attachment = style.getIdent(CSSName.BACKGROUND_ATTACHMENT);

        // load the background image
        block.background_image = null;
        int backImageWidth = 0;
        int backImageHeight = 0;
        if (back_image != null && !"none".equals(back_image)) {
            try {
                block.background_image = ImageUtil.loadImage(c, back_image);
                block.background_uri = back_image;
                backImageWidth = block.background_image.getWidth(null);
                backImageHeight = block.background_image.getHeight(null);
            } catch (Exception ex) {
                ex.printStackTrace();
                Uu.p(ex);
            }
        }

        // handle image positioning issues
        // need to update this to support vert and horz, not just vert
        if (style.hasProperty(CSSName.BACKGROUND_POSITION)) {
            float width = c.getBlockFormattingContext().getWidth();
            float height = c.getBlockFormattingContext().getHeight();

            Point pt = style.getBackgroundPosition(width - backImageWidth, height - backImageHeight, c.getCtx());
            block.background_position_horizontal = (int) pt.getX();
            block.background_position_vertical = (int) pt.getY();
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
    public static void paintListItem(Context c, Box box) {
        ListItemPainter.paint(c, box);
    }

    /**
     * Gets the backgroundColor attribute of the BoxRendering class
     *
     * @param c PARAM
     * @return The backgroundColor value
     */
    public static Color getBackgroundColor(Context c) {
        return Boxing.getBackgroundColor(c);
    }

    //TODO: check the logic here
    /**
     * Gets the inlineLayedOut attribute of the BoxRendering class
     *
     * @param box PARAM
     * @return The inlineLayedOut value
     */
    public static boolean isInlineLayedOut(Box box) {
        if (box.getChildCount() == 0) {
            return false;
        }//have to return something, it shouldn't matter
        for (int i = 0; i < box.getChildCount(); i++) {
            Box child = box.getChild(i);
            if (child instanceof LineBox) {
                return true;
            }
        }
        return false;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.21  2005/05/08 14:36:58  tobega
 * Refactored away the need for having a context in a CalculatedStyle
 *
 * Revision 1.20  2005/04/21 18:16:08  tobega
 * Improved handling of inline padding. Also fixed first-line handling according to spec.
 *
 * Revision 1.19  2005/04/19 17:51:18  joshy
 * fixed absolute positioning bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.18  2005/03/26 11:53:30  pdoubleya
 * paintFixed() was badly refactored before--now again a duplicate of paintAbsolute; added check for absolute boxes on paint(), so that they are not moved after render operation.
 *
 *
 */
