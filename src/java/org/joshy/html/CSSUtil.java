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

