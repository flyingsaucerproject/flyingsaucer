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

import org.xhtmlrenderer.css.value.Border;
import org.xhtmlrenderer.layout.Context;

import java.awt.Graphics2D;
import java.awt.Rectangle;


/**
 * Description of the Class
 *
 * @author empty
 */
public class BlockBox extends Box implements Renderable {
    public double absY;
    public int renderIndex;
    public double absX;


    /**
     * Constructor for the BlockBox object
     */
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


    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("BlockBox:");
        sb.append(super.toString());

        if (getStyle().isFixed()) {
            sb.append(" position: fixed");
        }
        if (this.right_set) {
            sb.append(" right = " + this.right);
        }
        return sb.toString();
    }

    public int getIndex() {
        return renderIndex;
    }

    //HACK: Context should not be necessary
    public void render(Context c, Graphics2D g2) {
        //HACK:
        g2.translate(absX - x, absY - y);
        int width = getWidth();
        int height = getHeight();
        if (getState() != Box.DONE) {
            height += c.getCanvas().getHeight();
        }
        Border margin = getStyle().getMarginWidth();

        Rectangle bounds = new Rectangle(x + margin.left,
                y + margin.top,
                width - margin.left - margin.right,
                height - margin.top - margin.bottom);
        BoxRendering.paintBackground(c, this, bounds);
        g2.translate(x - absX, y - absY);
    }

    public double getAbsTop() {
        return absY;
    }

    public double getAbsBottom() {
        return absY + height;
    }
}

/*
 * $Id$
 *
 * $Log$
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

