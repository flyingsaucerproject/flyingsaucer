
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

import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;
import org.w3c.dom.Node;
//import java.util.ArrayList;
//import java.util.List;

public class BlockBox extends Box {
    public boolean auto_width = true;
    public boolean auto_height = true;
    //public boolean inline = false;
    
    public boolean display_block = true;
    public boolean display_inline_block = false;
    /*
    public boolean isInline() {
        return inline;
    }
    public boolean isBlock() {
        return !inline;
    }
    */
    
    public BlockBox() {
        super();
    }
    public BlockBox(int x, int y, int w, int h) {
        super(x,y,w,h);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("BlockBox:");
        sb.append(super.toString());
        if(this.fixed) {
            sb.append(" position: fixed");
        }
        if(this.right_set) {
            sb.append(" right = " + this.right);
        }
        //+ " right = " + this.right;
        // + " width = " + auto_width + " height = " + auto_height;
        return sb.toString();
    }

}
