
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

package org.joshy.html.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import org.joshy.html.box.Box;

public class GraphicsUtil {
    public static void drawBox(Graphics g, Box box, Color color) {
        Color oc = g.getColor();
        g.setColor(color);
        //g.drawLine(-5,-5,5,5);
        //g.drawLine(-5,5,5,-5);
        g.drawRect(box.x,box.y,box.width,box.height);
        g.setColor(oc);
    }
    public static void draw(Graphics g, Rectangle box, Color color) {
        Color oc = g.getColor();
        g.setColor(color);
        g.drawRect(box.x,box.y,box.width,box.height);
        g.setColor(oc);
    }
}

