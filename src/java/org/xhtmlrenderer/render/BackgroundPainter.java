/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.Configuration;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;


/**
 * Description of the Class
 *
 * @author empty
 */
public class BackgroundPainter {
    /**
     * Description of the Field
     */
    public final static Color transparent = new Color(0, 0, 0, 0);


    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param block PARAM
     */
    public static void paint(Context c, Box block) {

        // don't draw if the backgrounds are turned off
        if (!Configuration.isTrue("xr.renderer.draw.backgrounds", true)) {
            return;
        }

        int width = block.getWidth();
        int height = block.getHeight();
        Border border = c.getCurrentStyle().getBorderWidth(width, height, c.getCtx());
        if (border == null) {
            return;
        }
        Border margin = c.getCurrentStyle().getMarginWidth(width, height, c.getCtx());
        Rectangle box = new Rectangle(block.x + margin.left + border.left,
                block.y + margin.top + border.top,
                block.width - margin.left - margin.right - border.left - border.right,
                block.height - margin.top - border.top - border.bottom - margin.bottom);

        // paint the background
        Color background_color = BoxRendering.getBackgroundColor(c);
        if (background_color != null) {
            // skip transparent background
            if (!background_color.equals(transparent)) {
                //TODO. make conf controlled Uu.p("filling a background");
                c.getGraphics().setColor(background_color);
                c.getGraphics().fillRect(box.x, box.y, box.width, box.height);
            }
        }

        int xoff = 0;
        int yoff = 0;

        if (block.attachment == IdentValue.FIXED) {
            yoff = c.getCanvas().getLocation().y;
            //TODO. make conf controlled Uu.p("setting the clip rect for fixed background");
            c.getGraphics().setClip(c.getCanvas().getVisibleRect());
        }

        if (block.background_image != null) {
            int left_insets = box.x;
            int top_insets = box.y;
            int back_width = box.width;
            int back_height = box.height;
            Rectangle2D oldclip = (Rectangle2D) c.getGraphics().getClip();
            Rectangle new_clip = new Rectangle(left_insets, top_insets, back_width, back_height);
            c.getGraphics().setClip(oldclip.createIntersection(new_clip));

            xoff += block.background_position_horizontal;
            yoff -= block.background_position_vertical;

            // tile the image as appropriate
            // do fixed tile image

            boolean horiz = false;
            boolean vert = false;
            if (block.repeat == IdentValue.REPEAT_X) {
                horiz = true;
                vert = false;
            }
            if (block.repeat == IdentValue.REPEAT_Y) {
                horiz = false;
                vert = true;
            }
            if (block.repeat == IdentValue.REPEAT) { 
                horiz = true;
                vert = true;
            }

            //TODO. make conf controlled Uu.p("filling background with an image");
            // fixed tiled image
            if (block.attachment != null && block.attachment.equals("fixed")) {
                tileFill(c.getGraphics(), block.background_image,
                        new Rectangle(left_insets, top_insets, back_width, back_height),
                        xoff, -yoff, horiz, vert);
            } else {
                // do normal tile image
                tileFill(c.getGraphics(), block.background_image,
                        new Rectangle(left_insets, top_insets, back_width, back_height),
                        xoff, -yoff, horiz, vert);
            }
            //TODO. make conf controlled Uu.p("setting the clip rect");
            c.getGraphics().setClip(oldclip);
        }
    }


    /**
     * Description of the Method
     *
     * @param g     PARAM
     * @param img   PARAM
     * @param rect  PARAM
     * @param xoff  PARAM
     * @param yoff  PARAM
     * @param horiz PARAM
     * @param vert  PARAM
     */
    private static void tileFill(Graphics g, Image img, Rectangle rect, int xoff, int yoff, boolean horiz, boolean vert) {
        int iwidth = img.getWidth(null);
        int iheight = img.getHeight(null);
        int rwidth = rect.width;
        int rheight = rect.height;

        if (!horiz) {
            rwidth = iwidth;
        }
        if (!vert) {
            rheight = iheight;
        }

        if (horiz) {
            xoff = xoff % iwidth - iwidth;
            rwidth += iwidth;
        }
        if (vert) {
            yoff = yoff % iheight - iheight;
            rheight += iheight;
        }

        for (int i = 0; i < rwidth; i += iwidth) {
            for (int j = 0; j < rheight; j += iheight) {
                g.drawImage(img, i + rect.x + xoff, j + rect.y + yoff, null);
            }
        }

    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.23  2005/05/11 20:09:22  joshy
 * fixed the image repeat bug
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.22  2005/05/08 14:36:58  tobega
 * Refactored away the need for having a context in a CalculatedStyle
 *
 * Revision 1.21  2005/04/21 18:16:07  tobega
 * Improved handling of inline padding. Also fixed first-line handling according to spec.
 *
 * Revision 1.20  2005/03/16 19:27:26  pdoubleya
 * Patches from Kevin: fix parsing for background-position property, allowing for 0 values with no using and percentage values. Fixes apply to rendering bg images as well.
 *
 * Revision 1.19  2005/01/29 20:24:23  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.18  2005/01/24 19:01:02  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.17  2005/01/24 14:36:34  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.16  2005/01/10 01:58:37  tobega
 * Simplified (and hopefully improved) handling of vertical-align. Added support for line-height. As always, provoked a few bugs in the process.
 *
 * Revision 1.15  2005/01/09 15:22:49  tobega
 * Prepared improved handling of margins, borders and padding.
 *
 * Revision 1.14  2005/01/02 12:22:19  tobega
 * Cleaned out old layout code
 *
 * Revision 1.13  2004/12/29 10:39:34  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.12  2004/12/29 07:35:38  tobega
 * Prepared for cloned Context instances by encapsulating fields
 *
 * Revision 1.11  2004/12/27 09:40:48  tobega
 * Moved more styling to render stage. Now inlines have backgrounds and borders again.
 *
 * Revision 1.10  2004/12/27 07:43:32  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.9  2004/12/14 02:28:49  joshy
 * removed some comments
 * some bugs with the backgrounds still
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/12/12 03:32:59  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.7  2004/11/16 15:38:44  joshy
 * removed background printing which speeds it up considerably
 * added boolean in conf to turn off backgrounds for testing
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/10 17:28:55  joshy
 * initial support for anti-aliased text w/ minium
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/06 22:49:52  joshy
 * cleaned up alice
 * initial support for inline borders and backgrounds
 * moved all of inlinepainter back into inlinerenderer, where it belongs.
 *
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/27 14:03:38  joshy
 * added initial viewport repainting support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 13:50:26  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

