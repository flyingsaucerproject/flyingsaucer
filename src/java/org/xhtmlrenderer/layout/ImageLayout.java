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
import org.xhtmlrenderer.render.ImageBox;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.Uu;

import java.awt.*;

/**
 * Description of the Class
 *
 * @author empty
 */
public class ImageLayout extends BoxLayout {

    //Image img;

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content
     * @return Returns
     */
    public Box createBox(Context c, Content content) {

        BlockBox box = new ImageBox();

        box.content = content;

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
        ImageBox block = (ImageBox) createBox(c, content);

        // load the image

        Border border = getBorder(c, block);

        Border padding = getPadding(c, block);

        Border margin = getMargin(c, block);


        Image img = getImage(c, content.getElement());

        // calculate new contents

        if (img != null) {

            block.width = img.getWidth(null);

            block.height = img.getHeight(null);

        } else {

            block.width = 50;

            block.height = 50;

        }

        block.img = img;

        //Rectangle contents = new Rectangle(0,0,img.getWidth(null),img.getHeight(null));

        /*
         * block.width = this.getMargin(c,elem).left + this.getBorder(c,elem).left + this.getPadding(c,elem).left +
         * block.width +
         * this.getMargin(c,elem).right + this.getBorder(c,elem).right + this.getPadding(c,elem).right;
         * block.height = this.getMargin(c,elem).top + this.getBorder(c,elem).top + this.getPadding(c,elem).top +
         * block.height +
         * this.getMargin(c,elem).bottom + this.getBorder(c,elem).bottom + this.getPadding(c,elem).bottom;
         */
        // calculate the inner width

        block.width = margin.left + border.left + padding.left + block.width +
                padding.right + border.right + margin.right;

        block.height = margin.top + border.top + padding.top + block.height +
                padding.bottom + border.bottom + margin.bottom;

        // create the new block box

        //block = new BlockBox();

        block.x = c.getExtents().x;

        block.y = c.getExtents().y;

        //block.width = contents.width;

        //block.height = contents.height;

        return block;
    }


    /*
     * public void paint(Context c, InlineBox box) {
     * // save the old extents
     * Rectangle oldExtents = new Rectangle(c.getExtents());
     * Element elem = (Element)box.node;
     * // set the contents size
     * Rectangle contents = layout(c,elem);
     * // get the border and padding
     * Border border = getBorder(c,elem);
     * Border padding = getPadding(c,elem);
     * Border margin = c.css.getMarginWidth(elem);
     * // calculate the insets
     * int top_inset = margin.top + border.top + padding.top;
     * int left_inset = margin.left + border.left + padding.left;
     * // shrink the bounds to be based on the contents
     * c.getExtents().width = contents.width;
     * // do all of the painting
     * // set the origin to the origin of our box
     * c.getExtents().y = box.y;
     * c.getExtents().Xx = box.Xx;
     * paintBackground(c,elem,contents);
     * // move the contents in to account for the insets
     * c.getExtents().translate(left_inset,top_inset);
     * paintComponent(c,elem,contents);
     * c.getExtents().translate(-left_inset,-top_inset);
     * paintBorder(c,elem,contents);
     * // restore the old extents
     * c.setExtents(oldExtents);
     * }
     */


    /**
     * Gets the image attribute of the ImageLayout object
     *
     * @param c    PARAM
     * @param node PARAM
     * @return The image value
     */
    public static Image getImage(Context c, Node node) {

        if (node.getNodeType() != Node.ELEMENT_NODE) {

            return null;
        }

        String src = ((Element) node).getAttribute("src");

        Image img = null;

        try {

            img = ImageUtil.loadImage(c, src);

        } catch (Exception ex) {

            Uu.p(ex);

        }

        return img;
    }

    /*
     * public void paintComponent(Context c, Element elem, InlineBox box) {
     * c.getGraphics().drawImage(img,box.Xx,box.y,null);
     * }
     */

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.14  2005/01/01 23:38:38  tobega
 * Cleaned out old rendering code
 *
 * Revision 1.13  2004/12/31 11:14:09  tobega
 * Rendering now mainly through a few static methods. Need to clean up still
 *
 * Revision 1.12  2004/12/29 15:06:41  tobega
 * Referencing Context instead of SharedContext where it was wrongly set before.
 *
 * Revision 1.11  2004/12/29 10:39:33  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.10  2004/12/12 03:32:58  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.9  2004/12/10 06:51:02  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.8  2004/12/09 21:18:52  tobega
 * precaution: code still works
 *
 * Revision 1.7  2004/12/09 00:11:51  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.6  2004/12/05 00:48:57  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.5  2004/10/28 01:34:23  joshy
 * moved more painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/27 13:39:56  joshy
 * moved more rendering code out of the layouts
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 13:46:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

