/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
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
package org.xhtmlrenderer.layout;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;

import java.awt.*;


/**
 * Description of the Class
 *
 * @author empty
 */
public class CustomBlockLayout extends BoxLayout {
    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param node PARAM
     * @return Returns
     */
    public Box createBox(Context c, Node node) {
        BlockBox box = new BlockBox();
        box.setNode(node);
        return box;
    }

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content PARAM
     * @return Returns
     */
    public Box layout(Context c, Content content) {
        //TODO: temporary hack
        BlockBox block = (BlockBox) createBox(c, content.getElement());
        // load the image

        Border border = getBorder(c, block);
        Border padding = getPadding(c, block);
        Border margin = getMargin(c, block);

        Dimension dim = this.getIntrinsicDimensions(c, content.getElement());

        // calculate new contents
        block.width = (int) dim.getWidth();
        block.height = (int) dim.getHeight();

        // calculate the inner width
        block.width = margin.left + border.left + padding.left + block.width +
                padding.right + border.right + margin.right;
        block.height = margin.top + border.top + padding.top + block.height +
                padding.bottom + border.bottom + margin.bottom;

        block.x = c.getExtents().x;
        block.y = c.getExtents().y;
        return block;
    }


    /**
     * override this method to return the proper dimensions of your custom page
     * element.
     *
     * @param c    PARAM
     * @param elem PARAM
     * @return The intrinsicDimensions value
     */
    public Dimension getIntrinsicDimensions(Context c, Element elem) {
        return new Dimension(50, 50);
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.6  2004/12/09 00:11:51  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.5  2004/12/05 00:48:57  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.4  2004/10/28 02:13:40  joshy
 * finished moving the painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 13:46:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

