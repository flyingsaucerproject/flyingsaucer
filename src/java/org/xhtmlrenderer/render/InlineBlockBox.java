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



/**
 * Description of the Class
 *
 * @author   Joshua Marinacci
 */
public class InlineBlockBox extends InlineBox {

    /** Description of the Field */
    public BlockBox sub_block = null;

    /** Constructor for the InlineBlockBox object */
    public InlineBlockBox() {
        super();
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public InlineBox copy() {
        InlineBlockBox newBox = new InlineBlockBox();
        InlineBlockBox box = this;
        newBox.x = box.x;
        newBox.y = box.y;
        newBox.width = box.width;
        newBox.height = box.height;
        newBox.element = box.element;
        newBox.sub_block = box.sub_block;
        return newBox;
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return   A string representation of the object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "InlineBlockBox:" );
        sb.append( super.toString() );
        return sb.toString();
    }

    /**
     * Gets the endOfParentContent attribute of the InlineBlockBox object
     *
     * @return   The endOfParentContent value
     */
    public boolean isEndOfParentContent() {
        return true;
    }

}

