
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

package org.joshy.html;

import java.awt.*;
import org.w3c.dom.*;
import org.joshy.*;

class CSSUtil {
    public static Color getColor(String val) {
        return getColor(val,Color.black);
    }
    public static Color getColor(String val, Color default_color) {
        if(val == null) {
            return default_color;
        }
        if(val.equals("")) {
            return default_color;
        }
        return Color.decode(val);
    }
    public static int getWidth(String val, int parent) {
        if(val == null) { return 0; }
        if(val.equals("")) { return 0; }
        if(val.endsWith("px")) {
            return Integer.parseInt(val.substring(0,val.length()-2));
        }
        if(val.endsWith("%")) {
            int v2 = Integer.parseInt(val.substring(0,val.length()-1));
            return (int)((((float)v2)/100)*parent);
        }
        //u.p("returning" + Integer.parseInt(val));
        return Integer.parseInt(val);
    }
    public static int getWidth(String val) {
        if(val == null) { return 0; }
        if(val.equals("")) { return 0; }
        if(val.endsWith("px")) {
            return Integer.parseInt(val.substring(0,val.length()-2));
        }
        //u.p("returning" + Integer.parseInt(val));
        return Integer.parseInt(val);
    }
    public static int getSize(String val, int default_size) {
        if(val == null) { return default_size; }
        if(val.equals("")) { return default_size; }
        if(val.endsWith("pt")) {
            return Integer.parseInt(val.substring(0,val.length()-2));
        }
        //u.p("returning" + Integer.parseInt(val));
        return Integer.parseInt(val);
    }
}

