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
import java.awt.Dimension;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.Border;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class Box {
    // dimensions stuff
    /** Description of the Field */
    public int x;
    /** Description of the Field */
    public int y;
    /** Description of the Field */
    public int width;
    /** Description of the Field */
    public int height;

    // element stuff
    /** Description of the Field */
    public Node node;

    // display stuff
    /** Description of the Field */
    public boolean display_none = false;

    // position stuff
    /** Description of the Field */
    public boolean relative = false;
    /** Description of the Field */
    public boolean fixed = false;
    public boolean absolute = false;
    /** Description of the Field */
    public int top = 0;
    /** Description of the Field */
    public int right = 0;
    /** Description of the Field */
    public boolean right_set = false;
    /** Description of the Field */
    public int bottom = 0;
    /** Description of the Field */
    public boolean bottom_set = false;
    /** Description of the Field */
    public int left = 0;
    /** Description of the Field */
    public boolean floated = false;

    // margins, borders, and padding stuff
    /** Description of the Field */
    public Color border_color;
    /** Description of the Field */
    public Border padding;
    /** Description of the Field */
    public Border border;
    /** Description of the Field */
    public Border margin;
    /** Description of the Field */
    public String border_style;

    /** Description of the Field */
    public Box click_styles;

    // foreground stuff
    /** Description of the Field */
    public Color color;

    // background stuff
    /** Description of the Field */
    public Color background_color;
    /** Description of the Field */
    public Image background_image;
    /** Description of the Field */
    public String repeat;
    /** Description of the Field */
    public String attachment;
    /** Description of the Field */
    public int background_position_vertical = 0;
    /** Description of the Field */
    public int background_position_horizontal = 0;
    /** Description of the Field */
    public boolean clicked = false;

    // list stuff
    /** Description of the Field */
    public int list_count = -1;
    /** Description of the Field */
    private Box parent;

    // children stuff
    /** Description of the Field */
    private List boxes;

    // printing stuff and constructor
    /** Constructor for the Box object */
    public Box() {
        this( true );
    }

    /**
     * Constructor for the Box object
     *
     * @param create_substyles  PARAM
     */
    public Box( boolean create_substyles ) {
        boxes = new ArrayList();
        if ( create_substyles ) {
            this.click_styles = new Box( false );
        }
    }

    /**
     * Constructor for the Box object
     *
     * @param x       PARAM
     * @param y       PARAM
     * @param width   PARAM
     * @param height  PARAM
     */
    public Box( int x, int y, int width, int height ) {
        this();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Return true if the target coordinates are inside of this box. The target
     * coordinates are already translated to be relative to the origin of this
     * box. ie x=0 & y=0. Thus the point 100,100 in a box with coordinates 20,20
     * x 90x90 would have the target coordinates passed in as 80,80 and the
     * function would return true.
     *
     * @param x  PARAM
     * @param y  PARAM
     * @return   Returns
     */

    public boolean contains( int x, int y ) {
        if ( ( x >= 0 ) && ( x <= 0 + this.width ) ) {
            if ( ( y >= 0 ) && ( y <= 0 + this.height ) ) {
                return true;
            }
        }
        /*
         * if((x >= this.x) && (x<= this.x + this.width)) {
         * if((y>=this.y) && (y<=this.y + this.height)) {
         * return true;
         * }
         * }
         */
        return false;
    }

    /**
     * Adds a feature to the Child attribute of the Box object
     *
     * @param child  The feature to be added to the Child attribute
     */
    public void addChild( Box child ) {
        child.setParent( this );
        boxes.add( child );
        //u.p("added child: " + child + " to " + this);
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public int totalHorizontalPadding() {
        int pd = 0;
        if ( this.margin != null ) {
            pd += this.margin.left + this.margin.right;
        }
        if ( this.padding != null ) {
            pd += this.padding.left + this.padding.right;
        }
        if ( this.border != null ) {
            pd += this.border.left + this.border.right;
        }
        return pd;
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public int totalVerticalPadding() {
        int pd = 0;
        if ( this.margin != null ) {
            pd += this.margin.top + this.margin.bottom;
        }
        if ( this.padding != null ) {
            pd += this.padding.top + this.padding.bottom;
        }
        if ( this.border != null ) {
            pd += this.border.top + this.border.bottom;
        }
        return pd;
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public int totalTopPadding() {
        int pd = 0;
        if ( this.margin != null ) {
            pd += this.margin.top;
        }
        if ( this.padding != null ) {
            pd += this.padding.top;
        }
        if ( this.border != null ) {
            pd += this.border.top;
        }
        return pd;
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public int totalLeftPadding() {
        int pd = 0;
        if ( this.margin != null ) {
            pd += this.margin.left;
        }
        if ( this.padding != null ) {
            pd += this.padding.left;
        }
        if ( this.border != null ) {
            pd += this.border.left;
        }
        return pd;
    }
    public int totalRightPadding() {
        int pd = 0;
        if ( this.margin != null ) {
            pd += this.margin.right;
        }
        if ( this.padding != null ) {
            pd += this.padding.right;
        }
        if ( this.border != null ) {
            pd += this.border.right;
        }
        return pd;
    }


    /**
     * Converts to a String representation of the object.
     *
     * @return   A string representation of the object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Box: " );
        if ( node == null ) {
            sb.append( " null node, " );
        } else {
            sb.append( node.getNodeName() + " (" + node.hashCode() + ")" );
        }
        sb.append( " (" + x + "," + y + ")->(" + width + " x " + height + ")" );
        // CLN: (PWW 13/08/04)
        sb.append( " color: " + color + " background-color: " + background_color + " " );
        return sb.toString();
    }

    /**
     * Sets the parent attribute of the Box object
     *
     * @param box  The new parent value
     */
    public void setParent( Box box ) {
        this.parent = box;
    }

    /**
     * Gets the height attribute of the Box object
     *
     * @return   The height value
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the width attribute of the Box object
     *
     * @return   The width value
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the parent attribute of the Box object
     *
     * @return   The parent value
     */
    public Box getParent() {
        return parent;
    }

    /**
     * Gets the childCount attribute of the Box object
     *
     * @return   The childCount value
     */
    public int getChildCount() {
        return boxes.size();
    }

    /**
     * Gets the child attribute of the Box object
     *
     * @param i  PARAM
     * @return   The child value
     */
    public Box getChild( int i ) {
        return (Box)boxes.get( i );
    }

    /**
     * Gets the childIterator attribute of the Box object
     *
     * @return   The childIterator value
     */
    public Iterator getChildIterator() {
        return boxes.iterator();
    }


    // element stuff
    /**
     * Gets the element attribute of the Box object
     *
     * @return   The element value
     */
    public Element getElement() {
        return (Element)node;
    }

    /**
     * Gets the closestNode attribute of the Box object
     *
     * @return   The closestNode value
     */
    public Node getClosestNode() {
        if ( node != null ) {
            return node;
        }
        return getParent().getClosestNode();
    }

    /**
     * Gets the element attribute of the Box object
     *
     * @return   The element value
     */
    public boolean isElement() {
        if ( node.getNodeType() == node.ELEMENT_NODE ) {
            return true;
        }
        return false;
    }

    /**
     * Gets the anonymous attribute of the Box object
     *
     * @return   The anonymous value
     */
    public boolean isAnonymous() {
        return false;
    }

    /**
     * Gets the internalDimension attribute of the Box object
     *
     * @return   The internalDimension value
     */
    public Dimension getInternalDimension() {
        int w = this.getWidth() - totalHorizontalPadding();
        int h = this.getHeight() - totalVerticalPadding();
        return new Dimension( w, h );
    }


    /**
     * This generates a string which fully represents every facet of the
     * rendered box (or at least as much as possible without actually drawing
     * it). This includes dimensions, location, color, backgrounds, images,
     * text, and pretty much everything else. The test string is used by the
     * regression tests.
     *
     * @return   The testString value
     */
    /*
     * display_none
     * relative
     * fixed
     * top
     * right
     * bottom
     * left
     * floated
     * border_color
     * padding
     * border
     * margin
     * border_style
     * color
     * background_color
     * background_image
     * repeat
     * attachment
     * back_pos_vert
     * back_pos_horiz
     */
    public String getTestString() {
        StringBuffer sb = new StringBuffer();
        // type
        if ( this instanceof LineBox ) {
            sb.append( "line:" );
        } else if ( this instanceof InlineBox ) {
            sb.append( "inline:" );
        } else {
            sb.append( "box:" );
        }

        // element
        sb.append( "-element:" + this.getClosestNode().getNodeName() );

        // dimensions and location
        sb.append( "-box(" + x + "," + y + ")-(" + width + "x" + height + ")" );

        // positioning info
        if ( relative ) {
            sb.append( "-relative" );
        }
        if ( fixed ) {
            sb.append( "-fixed" );
        }
        sb.append( "-pos(" + top + "," + right + "," + bottom + "," + left + ")" );
        if ( floated ) {
            sb.append( "-floated" );
        }

        // colors and insets
        sb.append( "-colors(for" + getColorTestString( color ) );
        sb.append( "-bor" + getColorTestString( border_color ) );
        sb.append( "-bak" + getColorTestString( background_color ) + ")" );
        sb.append( "-style(" + border_style + ")" );
        sb.append( "-insets(mar" + getBorderTestString( margin ) );
        sb.append( "-bor" + getBorderTestString( border ) );
        sb.append( "-pad" + getBorderTestString( padding ) + ")" );

        // background images
        sb.append( "-backimg(" + background_image );
        sb.append( "-" + repeat );
        sb.append( "-" + attachment );
        sb.append( "-" + background_position_vertical );
        sb.append( "-" + background_position_horizontal + ")" );

        sb.append( "-value:" + this.getClosestNode().getNodeValue() );
        return sb.toString();
    }

    /**
     * Gets the colorTestString attribute of the Box object
     *
     * @param c  PARAM
     * @return   The colorTestString value
     */
    public String getColorTestString( Color c ) {
        if ( c == null ) {
            return "[null]";
        }
        return "#" + Integer.toHexString( c.getRGB() );
    }

    /**
     * Gets the borderTestString attribute of the Box object
     *
     * @param b  PARAM
     * @return   The borderTestString value
     */
    public String getBorderTestString( Border b ) {
        if ( b == null ) {
            return "[null]";
        }
        return "(" + b.top + "," + b.right + "," + b.bottom + "," + b.left + ")";
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.7  2004/11/03 23:54:34  joshy
 * added hamlet and tables to the browser
 * more support for absolute layout
 * added absolute layout unit tests
 * removed more dead code and moved code into layout factory
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/03 15:17:05  joshy
 * added intial support for absolute positioning
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/10/23 13:50:26  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

