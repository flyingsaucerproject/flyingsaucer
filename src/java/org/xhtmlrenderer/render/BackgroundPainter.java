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

import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.util.Configuration;

import java.awt.*;

public class BackgroundPainter {

    public final static Color transparent = new Color(0, 0, 0, 0);

    public static void paint(RenderingContext c, Box block) {

        // don't draw if the backgrounds are turned off
        if (!Configuration.isTrue("xr.renderer.draw.backgrounds", true)) {
            return;
        }

        int width = block.getWidth();
        int height = block.getHeight();
        if (block.getState() != Box.DONE) {
            height += c.getCanvas().getHeight();
        }
        CalculatedStyle currentStyle = block.getStyle().getCalculatedStyle();
        BorderPropertySet border = currentStyle.getBorder(c);
        if (border == null) {
            return;
        }
        RectPropertySet margin = block.getStyle().getMarginWidth(c);
        // CLEAN: cast to int
        Rectangle box = new Rectangle(block.x + (int) margin.left() + (int) border.left(),
                block.y + (int) margin.top() + (int) border.top(),
                width - (int) margin.left() - (int) margin.right() - (int) border.left() - (int) border.right(),
                height - (int) margin.top() - (int) border.top() - (int) border.bottom() - (int) margin.bottom());

        // paint the background
        Color background_color = currentStyle.getBackgroundColor();
        if (background_color != null) {
            // skip transparent background
            if (!background_color.equals(transparent)) {
                c.getGraphics().setColor(background_color);
                c.getGraphics().fillRect(box.x, box.y, box.width + 1, box.height + 1);
            }
        }

        int xoff = 0;
        int yoff = 0;
        
        if (block.background_image != null) {
            int left_insets = box.x;
            int top_insets = box.y;
            int back_width = box.width;
            int back_height = box.height;
            Shape oldclip = (Shape) c.getGraphics().getClip();
            
            if (block.getStyle().getBackgroundAttachment() == IdentValue.FIXED) {
                yoff = c.getCanvas().getLocation().y;
                c.getGraphics().setClip(c.getCanvas().getVisibleRect());
            }

            Rectangle new_clip = new Rectangle(left_insets, top_insets, back_width, back_height);
            c.getGraphics().clip(new_clip);

            xoff += block.background_position_horizontal;
            yoff -= block.background_position_vertical;

            // tile the image as appropriate
            // do fixed tile image

            boolean horiz = false;
            boolean vert = false;
            if (block.getStyle().getBackgroundRepeat() == IdentValue.REPEAT_X) {
                horiz = true;
                vert = false;
            } else if (block.getStyle().getBackgroundRepeat() == IdentValue.REPEAT_Y) {
                horiz = false;
                vert = true;
            } else if (block.getStyle().getBackgroundRepeat() == IdentValue.REPEAT) {
                horiz = true;
                vert = true;
            }

            tileFill(c.getGraphics(), block.background_image,
                    new Rectangle(left_insets, top_insets, back_width, back_height),
                    xoff, -yoff, horiz, vert);
            c.getGraphics().setClip(oldclip);
        }
    }

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
 * Revision 1.39  2005/11/06 14:15:55  peterbrant
 * Make sure any changes to clip region are undone
 *
 * Revision 1.38  2005/10/27 00:09:02  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.37  2005/10/21 18:10:52  pdoubleya
 * Support for cachable borders. Still buggy on some pages, but getting there.
 *
 * Revision 1.36  2005/10/21 13:17:14  pdoubleya
 * Rename some methods in RectPropertySet, cleanup.
 *
 * Revision 1.35  2005/10/21 12:01:19  pdoubleya
 * Added cachable rect property for margin, cleanup minor in styling.
 *
 * Revision 1.34  2005/10/18 20:57:04  tobega
 * Patch from Peter Brant
 *
 * Revision 1.33  2005/10/15 23:39:17  tobega
 * patch from Peter Brant
 *
 * Revision 1.32  2005/10/02 21:29:59  tobega
 * Fixed a lot of concurrency (and other) issues from incremental rendering. Also some house-cleaning.
 *
 * Revision 1.31  2005/09/26 22:40:20  tobega
 * Applied patch from Peter Brant concerning margin collapsing
 *
 * Revision 1.30  2005/06/22 23:48:45  tobega
 * Refactored the css package to allow a clean separation from the core.
 *
 * Revision 1.29  2005/06/16 07:24:51  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.28  2005/06/13 06:50:15  tobega
 * Fixed a bug in table content resolution.
 * Various "tweaks" in other stuff.
 *
 * Revision 1.27  2005/06/04 13:28:34  tobega
 * better??
 *
 * Revision 1.26  2005/06/03 23:19:43  tobega
 * Removed padding on div in default.css to conform to HTML4.0, and fixed clipregion for painting bgimage, all to pass tests/eeze/t0805-c5512-brdr-rw-01-b-g.xhtml
 *
 * Revision 1.25  2005/05/13 11:49:59  tobega
 * Started to fix up borders on inlines. Got caught up in refactoring.
 * Boxes shouldn't cache borders and stuff unless necessary. Started to remove unnecessary references.
 * Hover is not working completely well now, might get better when I'm done.
 *
 * Revision 1.24  2005/05/12 06:24:15  joshy
 * more very minor border and background tweaks
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
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
 * Separated current state Context into LayoutContext and the rest into SharedContext.
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

