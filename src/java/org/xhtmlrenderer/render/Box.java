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

import java.awt.Color;
import java.awt.Image;
import org.xhtmlrenderer.css.Border;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.List;
import java.util.ArrayList;
import java.awt.Dimension;
import java.util.Iterator;
import org.xhtmlrenderer.util.u;

public class Box {
    // dimensions stuff
    public int x;
    public int y;
    public int width;
    public int height;
    
    public int getHeight() {
        return height;
    }
    
    public int getWidth() {
        return width;
    }

    /** Return true if the target coordinates are inside of this box. The
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
    private Box parent;
    
    public Box getParent() {
        return parent;
    }
    public void setParent(Box box) {
        this.parent = box;
    }

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
    
    // list stuff
    public int list_count = -1;

    // children stuff
    private List boxes;
    public void addChild(Box child) {
        child.setParent(this);
        boxes.add(child);
        //u.p("added child: " + child + " to " + this);
    }
    
    public int getChildCount() {
        return boxes.size();
    }
    
    public Box getChild(int i) {
        return (Box) boxes.get(i);
    }
    
    public Iterator getChildIterator() {
        return boxes.iterator();
    }
    
    
    // element stuff
    public Element getElement() {
        return (Element)node;
    }
    public Node getClosestNode() {
        if(node != null) {
            return node;
        }
        return getParent().getClosestNode();
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

    public Dimension getInternalDimension() {
       int w = this.getWidth() - totalHorizontalPadding();
       int h = this.getHeight() - totalVerticalPadding();
       return new Dimension(w,h);
    }
    public int totalHorizontalPadding() {
        int pd = 0;
        if(this.margin != null) {
            pd+= this.margin.left + this.margin.right;
        }
        if(this.padding != null) {
            pd+= this.padding.left + this.padding.right;
        }
        if(this.border != null) {
            pd+= this.border.left + this.border.right;
        }
        return pd;
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
    
    
    /** This generates a string which fully represents every facet of the
    rendered box (or at least as much as possible without actually drawing
    it).  This includes dimensions, location, color, backgrounds, images, text, and pretty
    much everything else. The test string is used by the regression tests.
    */
    /*
     display_none
     relative
     fixed
     top
     right
     bottom
     left
     floated
     border_color
     padding
     border
     margin
     border_style
     color
     background_color
     background_image
     repeat
     attachment
     back_pos_vert
     back_pos_horiz
     
    */
    public String getTestString() {
        StringBuffer sb = new StringBuffer();
        // type
        if(this instanceof LineBox) {
            sb.append("line:");
        } else if(this instanceof InlineBox) {
            sb.append("inline:");
        } else {
            sb.append("box:");
        }
        
        // element
        sb.append("-element:"+this.getClosestNode().getNodeName());
        
        // dimensions and location
        sb.append("-box("+x+","+y+")-("+width+"x"+height+")");
        
        // positioning info
        if(relative) {
            sb.append("-relative");
        }
        if(fixed) {
            sb.append("-fixed");
        }
        sb.append("-pos("+top+","+right+","+bottom+","+left+")");
        if(floated) {
            sb.append("-floated");
        }
        
        // colors and insets
        sb.append("-colors(for"+getColorTestString(color));
        sb.append("-bor"+getColorTestString(border_color));
        sb.append("-bak"+getColorTestString(background_color)+")");
        sb.append("-style("+border_style+")");
        sb.append("-insets(mar"+getBorderTestString(margin));
        sb.append("-bor"+getBorderTestString(border));
        sb.append("-pad"+getBorderTestString(padding)+")");
        
        // background images
        sb.append("-backimg("+background_image);
        sb.append("-"+repeat);
        sb.append("-"+attachment);
        sb.append("-"+background_position_vertical);
        sb.append("-"+background_position_horizontal+")");
        
        sb.append("-value:"+this.getClosestNode().getNodeValue());
        return sb.toString();
    }
    
    public String getColorTestString(Color c) {
        if(c == null) {
            return "[null]";
        }
        return "#"+Integer.toHexString(c.getRGB());
    }
    public String getBorderTestString(Border b) {
        if(b == null) {
            return "[null]";
        }
        return "("+b.top+","+b.right+","+b.bottom+","+b.left+")";
    }
    
    
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Box: ");
        if(node == null) {
            sb.append(" null node, ");
        } else {
            sb.append(node.getNodeName() + " (" + node.hashCode() + ")");
        }
        sb.append(" (" + x + ","+y+")->("+width+" x "+height+")");
        // CLN: (PWW 13/08/04)
        sb.append(" color: " + color + " background-color: " + background_color + " ");
        return sb.toString();
    }

}
