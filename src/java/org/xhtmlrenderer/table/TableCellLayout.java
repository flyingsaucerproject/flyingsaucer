
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

package org.xhtmlrenderer.table;

import java.util.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Rectangle;

import org.w3c.dom.*;
import org.xhtmlrenderer.util.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.layout.*;

/** a table cell is a normal inline layout box, except that
when it does the actual painting it uses the height of the bounds
instead of it's intrinsic height. (hopefully this will change to be less
clunky and more explict when I redesign it all to use a separate Box
pass. 
*/
public class TableCellLayout extends InlineLayout {

    
    public void paintBackground(Context c, Box box) {
        //contents.height = c.getExtents().height;
        //u.p("painting a cell background: " + box);
        super.paintBackground(c,box);
    }

}

