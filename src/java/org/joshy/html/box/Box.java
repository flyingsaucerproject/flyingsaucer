package org.joshy.html.box;

import java.awt.Color;
import java.awt.Image;
import org.joshy.html.Border;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.List;
import java.util.ArrayList;

public class Box {
    // dimensions stuff
    public int x;
    public int y;
    public int width;
    public int height;

    /** Return true of the target coordinates are inside of this box. The
    target coordinates are already translated to be relative to the origin
    of this box. ie x=0 & y=0. Thus the point 100,100 in a box with coordinates
    20,20 x 90x90 would have the target coordinates passed in as 80,80 and
    the function would return true.
    */
     
    public boolean contains(int x, int y) {
        if((x >= 0) && (x<= 0 + this.width)) {
            if((y>=0) && (y<=0 + this.height)) {
                return true;
            }
        }
/*
        if((x >= this.x) && (x<= this.x + this.width)) {
            if((y>=this.y) && (y<=this.y + this.height)) {
                return true;
            }
        }
        */
        return false;
    }

    // element stuff
    public Node node;

    // display stuff
    public boolean display_none = false;

    // position stuff
    public boolean relative = false;
    public boolean fixed = false;
    public int top = 0;
    public int right = 0;
    public boolean right_set = false;
    public int bottom = 0;
    public boolean bottom_set = false;
    public int left = 0;
    public boolean floated = false;

    // margins, borders, and padding stuff
    public Color border_color;
    public Border padding;
    public Border border;
    public Border margin;
    public String border_style;
    
    public Box click_styles;

    // foreground stuff
    public Color color;
    
    // background stuff
    public Color background_color;
    public Image background_image;
    public String repeat;
    public String attachment;
    public int background_position_vertical = 0;
    public int background_position_horizontal = 0;
    public boolean clicked = false;

    // children stuff
    public List boxes;
    
    
    // element stuff
    public Element getElement() {
        return (Element)node;
    }
    public boolean isElement() {
        if(node.getNodeType() == node.ELEMENT_NODE) {
            return true;
        }
        return false;
    }
    public boolean isAnonymous() {
        return false;
    }

    // printing stuff and constructor
    public Box() {
        this(true);
    }
    
    public Box(boolean create_substyles) {
        boxes = new ArrayList();
        if(create_substyles) {
            this.click_styles = new Box(false);
        }
    }
    
    public Box(int x, int y, int width, int height) {
        this();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    
    public int totalVerticalPadding() {
        int pd = 0;
        if(this.margin != null) {
            pd+= this.margin.top + this.margin.bottom;
        }
        if(this.padding != null) {
            pd+= this.padding.top + this.padding.bottom;
        }
        if(this.border != null) {
            pd+= this.border.top + this.border.bottom;
        }
        return pd;
    }
    
    public int totalTopPadding() {
        int pd = 0;
        if(this.margin != null) {
            pd+= this.margin.top;
        }
        if(this.padding != null) {
            pd+= this.padding.top;
        }
        if(this.border != null) {
            pd+= this.border.top;
        }
        return pd;
    }
    
    public int totalLeftPadding() {
        int pd = 0;
        if(this.margin != null) { pd+= this.margin.left; }
        if(this.padding != null) { pd+= this.padding.left; }
        if(this.border != null) { pd+= this.border.left; }
        return pd;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Box: ");
        if(node == null) {
            sb.append(" null node, ");
        } else {
            sb.append(node.getNodeName());
        }
        sb.append(" (" + x + ","+y+")->("+width+" x "+height+")");
        return sb.toString();
    }

}
