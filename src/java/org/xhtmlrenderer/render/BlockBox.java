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

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.content.ContentUtil;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.Uu;

import java.awt.*;

public class BlockBox extends Box implements Renderable {

    private final static Color TRANSPARENT = new Color(0, 0, 0, 0);

    public int renderIndex;

    public BlockBox() {
        super();
    }

    public void adjustWidthForChild(int childWidth) {
        if (getStyle().isAutoWidth() && childWidth > contentWidth) {
            contentWidth = childWidth;
        }
        if (getParent() != null) {
            getParent().adjustWidthForChild(getWidth());
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("BlockBox:");
        sb.append(super.toString());

        if (getStyle().isFixed()) {
            sb.append(" position: fixed");
        }
        return sb.toString();
    }

    public int getIndex() {
        return renderIndex;
    }

    //HACK: Context should not be necessary
    public void render(RenderingContext c, Graphics2D g2) {
        //HACK:
        g2.translate(getAbsX() - x, getAbsY() - y);
        int width = getWidth();
        int height = getHeight();
        if (getState() != Box.DONE) {
            height += c.getCanvas().getHeight();
        }
        RectPropertySet margin = getStyle().getMarginWidth(c);

        // CLEAN: cast to int
        Rectangle bounds = new Rectangle(x + (int) margin.left(),
                y + (int) margin.top(),
                width - (int) margin.left() - (int) margin.right(),
                height - (int) margin.top() - (int) margin.bottom());
        BoxRendering.paintBackground(c, this, bounds);
        g2.translate(x - getAbsX(), y - getAbsY());
    }

    public double getAbsTop() {
        return getAbsY();
    }

    public double getAbsBottom() {
        return getAbsY() + height;
    }


    public void paintBorder(RenderingContext c) {
        Rectangle borderBounds = getBorderEdge(getAbsX(), getAbsY(), c);
        if (getState() != Box.DONE) {
            borderBounds.height += c.getCanvas().getHeight();
        }

        BorderPainter.paint(borderBounds, BorderPainter.ALL,
                getStyle().getCalculatedStyle(), c.getGraphics(), c, 0);
    }

    private Image getBackgroundImage(RenderingContext c) {
        String uri = getStyle().getCalculatedStyle().getStringProperty(CSSName.BACKGROUND_IMAGE);
        if (!uri.equals("none")) {
            try {
                return c.getUac().getImageResource(uri).getImage();
            } catch (Exception ex) {
                ex.printStackTrace();
                Uu.p(ex);
            }
        }
        return null;
    }

    public void paintBackground(RenderingContext c) {
        if (!Configuration.isTrue("xr.renderer.draw.backgrounds", true)) {
            return;
        }

        Rectangle backgroundBounds = getBorderEdge(getAbsX(), getAbsY(), c);
        if (getState() != Box.DONE) {
            backgroundBounds.height += c.getCanvas().getHeight();
        }

        Color backgroundColor = getStyle().getCalculatedStyle().getBackgroundColor();
        if (backgroundColor != null && !backgroundColor.equals(TRANSPARENT)) {
            c.getGraphics().setColor(backgroundColor);
            c.getGraphics().fillRect(backgroundBounds.x, backgroundBounds.y, backgroundBounds.width, backgroundBounds.height);
        }

        int xoff = 0;
        int yoff = 0;

        Image backgroundImage = getBackgroundImage(c);
        if (backgroundImage != null) {
            Shape oldclip = (Shape) c.getGraphics().getClip();

            if (getStyle().isFixedBackground()) {
                yoff = c.getCanvas().getLocation().y;
                c.getGraphics().setClip(c.getCanvas().getVisibleRect());
            }

            c.getGraphics().clip(backgroundBounds);

            int imageWidth = backgroundImage.getWidth(null);
            int imageHeight = backgroundImage.getHeight(null);

            Point bgOffset = getStyle().getCalculatedStyle().getBackgroundPosition(backgroundBounds.width - imageWidth,
                    backgroundBounds.height - imageHeight, c);
            xoff += bgOffset.x;
            yoff -= bgOffset.y;

            tileFill(c.getGraphics(), backgroundImage,
                    backgroundBounds,
                    xoff, -yoff,
                    getStyle().isHorizontalBackgroundRepeat(),
                    getStyle().isVerticalBackgroundRepeat());
            c.getGraphics().setClip(oldclip);
        }
    }

    private static void tileFill(Graphics g, Image img, Rectangle rect, int xoff, int yoff, boolean horiz, boolean vert) {
        int iwidth = img.getWidth(null);
        int iheight = img.getHeight(null);
        int rwidth = rect.width;
        int rheight = rect.height;

        if (horiz) {
            xoff = xoff % iwidth - iwidth;
            rwidth += iwidth;
        } else {
            rwidth = iwidth;
        }

        if (vert) {
            yoff = yoff % iheight - iheight;
            rheight += iheight;
        } else {
            rheight = iheight;
        }

        for (int i = 0; i < rwidth; i += iwidth) {
            for (int j = 0; j < rheight; j += iheight) {
                g.drawImage(img, i + rect.x + xoff, j + rect.y + yoff, null);
            }
        }

    }

    public void paintListStyles(RenderingContext c) {
        //HACK:
        if (ContentUtil.isListItem(getStyle().getCalculatedStyle())) {
            ListItemPainter.paint(c, this);
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.19  2005/11/08 22:53:46  tobega
 * added getLineHeight method to CalculatedStyle and hacked in some list-item support
 *
 * Revision 1.18  2005/11/08 20:03:57  peterbrant
 * Further progress on painting order / improved positioning implementation
 *
 * Revision 1.17  2005/11/05 18:45:06  peterbrant
 * General cleanup / Remove obsolete code
 *
 * Revision 1.16  2005/10/27 00:09:02  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.15  2005/10/21 13:17:15  pdoubleya
 * Rename some methods in RectPropertySet, cleanup.
 *
 * Revision 1.14  2005/10/21 12:01:20  pdoubleya
 * Added cachable rect property for margin, cleanup minor in styling.
 *
 * Revision 1.13  2005/10/21 05:52:10  tobega
 * A little more experimenting with flattened render tree
 *
 * Revision 1.12  2005/10/18 20:57:04  tobega
 * Patch from Peter Brant
 *
 * Revision 1.11  2005/10/16 23:57:16  tobega
 * Starting experiment with flat representation of render tree
 *
 * Revision 1.10  2005/10/06 03:20:22  tobega
 * Prettier incremental rendering. Ran into more trouble than expected and some creepy crawlies and a few pages don't look right (forms.xhtml, splash.xhtml)
 *
 * Revision 1.9  2005/10/02 21:29:59  tobega
 * Fixed a lot of concurrency (and other) issues from incremental rendering. Also some house-cleaning.
 *
 * Revision 1.8  2005/09/26 22:40:20  tobega
 * Applied patch from Peter Brant concerning margin collapsing
 *
 * Revision 1.7  2005/01/29 20:24:23  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.6  2004/12/16 15:53:10  joshy
 * fixes for absolute layout
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/12/11 23:36:49  tobega
 * Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.4  2004/12/01 01:57:02  joshy
 * more updates for float support.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/11/18 16:45:12  joshy
 * improved the float code a bit.
 * now floats are automatically forced to be blocks
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.2  2004/10/23 13:50:26  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

