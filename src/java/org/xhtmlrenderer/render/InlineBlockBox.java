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
package org.xhtmlrenderer.render;

public class InlineBlockBox extends InlineBox {
    public InlineBlockBox() {
        super();
    }

    public InlineBox copy() {
        InlineBlockBox newBox = new InlineBlockBox();
        InlineBlockBox box = this;
        newBox.x = box.x;
        newBox.y = box.y;
        newBox.width = box.width;
        newBox.height = box.height;
        //border = box.border;
        //margin = box.margin;
        //padding = box.padding;
        //color = box.color;
        newBox.content = box.content;
        newBox.sub_block = box.sub_block;
        //font = box.font;
        //newBox.underline = box.underline;
        //newBox.overline = box.overline;
        //newBox.strikethrough = box.strikethrough;
        return newBox;
    }

    public BlockBox sub_block = null;

    public boolean isEndOfParentContent() {
        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("InlineBlockBox:");
        sb.append(super.toString());
        return sb.toString();
    }

}
