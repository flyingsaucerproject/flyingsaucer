
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

package org.xhtmlrenderer.test;

import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.css.*;
import java.awt.Dimension;
import org.w3c.dom.*;
import org.joshy.u;
public class XLayout extends CustomBlockLayout {

    public Dimension getIntrinsicDimensions(Context c, Element elem) {
        return new Dimension(50,50);
    }
    
    public void paintComponent(Context c, Box box) {
        Dimension dim = box.getInternalDimension();
        u.p("dim = " + dim);
        c.getGraphics().drawLine(
            box.x,
            box.y,
            box.x+(int)dim.getWidth(),
            box.y+(int)dim.getHeight());
        c.getGraphics().drawLine(
            box.x,
            box.y+(int)dim.getHeight(),
            box.x+(int)dim.getWidth(),
            box.y);
    }

}

