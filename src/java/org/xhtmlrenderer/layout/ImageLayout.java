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

import java.awt.Image;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.render.*;

/**
 * Description of the Class
 *
 * @author   empty
 */
public class ImageLayout extends BoxLayout {

    //Image img;

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param node  PARAM
     * @return      Returns
     */
    public Box createBox( Context c, Node node ) {

        BlockBox box = new BlockBox();

        box.node = node;

        return box;
    }

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param elem  PARAM
     * @return      Returns
     */
    public Box layout( Context c, Element elem ) {

        BlockBox block = (BlockBox)createBox( c, elem );

        // load the image

        Border border = getBorder( c, block );

        Border padding = getPadding( c, block );

        Border margin = getMargin( c, block );


        Image img = getImage( c, elem );

        // calculate new contents

        if ( img != null ) {

            block.width = img.getWidth( null );

            block.height = img.getHeight( null );

        } else {

            block.width = 50;

            block.height = 50;

        }

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
     * c.getExtents().x = box.x;
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
     * @param c     PARAM
     * @param node  PARAM
     * @return      The image value
     */
    protected Image getImage( Context c, Node node ) {

        if ( node.getNodeType() != node.ELEMENT_NODE ) {

            return null;
        }

        String src = ( (Element)node ).getAttribute( "src" );

        Image img = null;

        try {

            img = ImageUtil.loadImage( c, src );

        } catch ( Exception ex ) {

            u.p( ex );

        }

        return img;
    }
    
    public Renderer getRenderer() {
        return new ImageRenderer();
    }

    /*
     * public void paintComponent(Context c, Element elem, InlineBox box) {
     * c.getGraphics().drawImage(img,box.x,box.y,null);
     * }
     */

}

/*
 * $Id$
 *
 * $Log$
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

