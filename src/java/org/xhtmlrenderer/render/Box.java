/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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

import java.awt.Image;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.util.XRRuntimeException;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class Box {

    /** Description of the Field */
    public Element element;

    /** Description of the Field */
    public JComponent component = null;

    /** Description of the Field */
    public boolean restyle = false;

    // dimensions stuff
    /** Box x-pos. */
    public int x;
    
    /** Box y-pos. */
    public int y;
    
    /** Box width. */
    public int width;
    
    /** Box height. */
    public int height;

    // position stuff
    /** True if the box is in fixed position. */
    public boolean fixed = false;
    
    /** True if the box is absolute-positioned. */
    public boolean absolute = false;

    /** True if the box is floated. */
    public boolean floated = false;

    /** Description of the Field */
    public int top = 0;
    
    /** Description of the Field */
    public boolean top_set = false;
    
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
    public boolean left_set = false;

    /** The Image shown as the Box's background. */
    public Image background_image;
    
    /**
     * The URI for a background image; used in debugging (so we know which bg is
     * being painted)
     */
    public String background_uri;
    
    /** The background-repeat IdentValue. */
    public IdentValue repeat;
    
    /** The background-attachment IdentValue. */
    public IdentValue attachment;

    /** Description of the Field */
    public int background_position_vertical = 0;
    
    /** Description of the Field */
    public int background_position_horizontal = 0;

    // list stuff
    /** Description of the Field */
    public int list_count = -1;

    // printing stuff
    /** Description of the Field */
    public boolean auto_height = true;

    /** Description of the Field */
    public CascadedStyle firstLineStyle;
    
    /** Description of the Field */
    public CascadedStyle firstLetterStyle;

    /** Description of the Field */
    protected BlockFormattingContext blockFormattingContext = null;

    /** Description of the Field */
    private boolean haveBorders;
    
    /** Description of the Field */
    private Border margin;
    
    /** Description of the Field */
    private Border padding;
    
    /** Description of the Field */
    private Border border;
    
    /** Description of the Field */
    private Box parent;

    // children stuff
    /** Description of the Field */
    private List boxes;

    /** Description of the Field */
    private boolean children_exceeds;

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
    }

    /**
     * Constructor for the Box object
     *
     * @param x       x-pos
     * @param y       y-pos
     * @param width   width
     * @param height  height
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
        return false;
    }

    /**
     * Description of the Method
     *
     * @param style
     * @return       Returns
     */
    public int totalHorizontalPadding( CalculatedStyle style ) {
        calcBorders( style );
        return totalHorizontalPadding();
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public int totalHorizontalPadding() {
        int pd = 0;
        if ( haveBorders ) {
            pd += margin.left + margin.right;
            pd += padding.left + padding.right;
            pd += border.left + border.right;
        }
        return pd;
    }

    /**
     * Description of the Method
     *
     * @param style
     * @return       Returns
     */
    public int totalVerticalPadding( CalculatedStyle style ) {
        calcBorders( style );
        return totalVerticalPadding();
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public int totalVerticalPadding() {
        int pd = 0;
        if ( haveBorders ) {
            pd += margin.top + margin.bottom;
            pd += padding.top + padding.bottom;
            pd += border.top + border.bottom;
        }
        return pd;
    }


    /**
     * Description of the Method
     *
     * @param style
     * @return       Returns
     */
    public int totalTopPadding( CalculatedStyle style ) {
        calcBorders( style );
        return totalTopPadding();
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public int totalTopPadding() {
        int pd = 0;
        if ( haveBorders ) {
            pd += margin.top;
            pd += padding.top;
            pd += border.top;
        }
        return pd;
    }

    /**
     * Description of the Method
     *
     * @param style
     * @return       Returns
     */
    public int totalLeftPadding( CalculatedStyle style ) {
        calcBorders( style );
        return totalLeftPadding();
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public int totalLeftPadding() {
        int pd = 0;
        if ( haveBorders ) {
            pd += margin.left;
            pd += padding.left;
            pd += border.left;
        }
        return pd;
    }

    /**
     * Description of the Method
     *
     * @param style  PARAM
     * @return       Returns
     */
    public int totalRightPadding( CalculatedStyle style ) {
        calcBorders( style );
        return totalRightPadding();
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public int totalRightPadding() {
        int pd = 0;
        if ( haveBorders ) {
            pd += margin.right;
            pd += padding.right;
            pd += border.right;
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
        sb.append( " (" + x + "," + y + ")->(" + width + " x " + height + ")" );
        return sb.toString();
    }

    /**
     * Adds a feature to the Child attribute of the Box object
     *
     * @param child  The feature to be added to the Child attribute
     */
    public void addChild( Box child ) {
        if ( child == null ) {
            throw new NullPointerException( "trying to add null child" );
        }
        child.setParent( this );
        boxes.add( child );
        if ( child.isChildrenExceedBounds() ) {
            setChildrenExceedBounds( true );
        }
    }

    /** Description of the Method */
    public void removeAllChildren() {
        boxes.clear();
    }

    /**
     * Sets the blockFormattingContext attribute of the Box object
     *
     * @param blockFormattingContext  The new blockFormattingContext value
     */
    public void setBlockFormattingContext( BlockFormattingContext blockFormattingContext ) {
        this.blockFormattingContext = blockFormattingContext;
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
     * Sets the childrenExceedBounds attribute of the Box object
     *
     * @param children_exceeds  The new childrenExceedBounds value
     */
    public void setChildrenExceedBounds( boolean children_exceeds ) {
        this.children_exceeds = children_exceeds;
    }

    /**
     * Gets the blockFormattingContext attribute of the Box object
     *
     * @return   The blockFormattingContext value
     */
    public BlockFormattingContext getBlockFormattingContext() {
        return blockFormattingContext;
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

    /**
     * Gets the childrenExceedBounds attribute of the Box object
     *
     * @return   The childrenExceedBounds value
     */
    public boolean isChildrenExceedBounds() {
        return children_exceeds;
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

        // dimensions and location
        sb.append( "-box(" + x + "," + y + ")-(" + width + "x" + height + ")" );

        if ( fixed ) {
            sb.append( "-fixed" );
        }
        sb.append( "-pos(" + top + "," + right + "," + bottom + "," + left + ")" );
        if ( floated ) {
            sb.append( "-floated" );
        }

        // background images
        sb.append( "-backimg(" + background_image );
        sb.append( "-" + repeat );
        sb.append( "-" + attachment );
        sb.append( "-" + background_position_vertical );
        sb.append( "-" + background_position_horizontal + ")" );

        return sb.toString();
    }

    /**
     * If this box represents the text of an inline element then returns true.
     * Thus, the text "<i>some text</i> " if the following example would be an
     * inline element: <pre>
     * &lt;p&gt; text &lt;b&gt;some text&lt;/b&gt; text &lt;/p&gt;
     * </pre> The text "<i>some text</i> " in the next example <b>would not</b>
     * be an inline element, because it is merely the text child of a block
     * element <p/>
     *
     * <pre>
     * &lt;p&gt; some text &lt;/p&gt;
     * </pre> </p>
     *
     * @return   The inlineElement value
     */
    public boolean isInlineElement() {
        return false;
    }

    /**
     * Description of the Method
     *
     * @param style  PARAM
     */
    private void calcBorders( CalculatedStyle style ) {
        // HACK: may need to remove this check if sizes can change dynamically on resize (patrick)
        if ( haveBorders ) {
            return;
        }
        if ( style == null ) {
            throw new XRRuntimeException( "cant calc borders with no style" );
        }
        // ASK: where do we get the containing height/width here
        margin = style.getMarginWidth( width, height );
        padding = style.getPaddingWidth( width, height );
        border = style.getBorderWidth( width, height );
        haveBorders = true;// this flag is not checked, ask patrick
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.46  2005/02/03 23:16:16  pdoubleya
 * .
 *
 * Revision 1.45  2005/01/31 22:51:35  pdoubleya
 * Added caching for padding/margin/border calcs, plus alternate calls to get their totals (with and without style available). Reformatted.
 *
 * Revision 1.44  2005/01/29 20:24:23  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.43  2005/01/24 22:46:42  pdoubleya
 * Added support for ident-checks using IdentValue instead of string comparisons.
 *
 * Revision 1.42  2005/01/24 19:01:03  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.41  2005/01/24 14:36:35  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.40  2005/01/16 18:50:05  tobega
 * Re-introduced caching of styles, which make hamlet and alice scroll nicely again. Background painting still slow though.
 *
 * Revision 1.39  2005/01/09 15:22:50  tobega
 * Prepared improved handling of margins, borders and padding.
 *
 * Revision 1.38  2005/01/07 00:29:29  tobega
 * Removed Content reference from Box (mainly to reduce memory footprint). In the process stumbled over and cleaned up some messy stuff.
 *
 * Revision 1.37  2005/01/05 23:15:09  tobega
 * Got rid of some redundant code for hover-styling
 *
 * Revision 1.36  2005/01/05 01:10:15  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.35  2005/01/02 01:00:09  tobega
 * Started sketching in code for handling replaced elements in the NamespaceHandler
 *
 * Revision 1.34  2004/12/29 12:57:27  tobega
 * Trying to handle BFC:s right
 *
 * Revision 1.33  2004/12/28 02:15:19  tobega
 * More cleaning.
 *
 * Revision 1.32  2004/12/28 01:48:24  tobega
 * More cleaning. Magically, the financial report demo is starting to look reasonable, without any effort being put on it.
 *
 * Revision 1.31  2004/12/27 09:40:48  tobega
 * Moved more styling to render stage. Now inlines have backgrounds and borders again.
 *
 * Revision 1.30  2004/12/27 07:43:32  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.29  2004/12/16 15:53:10  joshy
 * fixes for absolute layout
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.28  2004/12/13 15:15:57  joshy
 * fixed bug where inlines would pick up parent styles when they aren't supposed to
 * fixed extra Xx's in printed text
 * added conf boolean to turn on box outlines
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.27  2004/12/12 23:19:26  tobega
 * Tried to get hover working. Something happens, but not all that's supposed to happen.
 *
 * Revision 1.26  2004/12/12 03:33:00  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.25  2004/12/11 23:36:49  tobega
 * Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.24  2004/12/11 18:18:11  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
 * Revision 1.23  2004/12/10 06:51:04  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.22  2004/12/09 21:18:53  tobega
 * precaution: code still works
 *
 * Revision 1.21  2004/12/09 18:00:05  joshy
 * fixed hover bugs
 * fixed li's not being blocks bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.20  2004/12/05 05:22:36  joshy
 * fixed NPEs in selection listener
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.19  2004/12/05 05:18:02  joshy
 * made bullets be anti-aliased
 * fixed bug in link listener that caused NPEs
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.18  2004/12/05 00:48:59  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.17  2004/12/01 01:57:02  joshy
 * more updates for float support.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/11/18 16:45:13  joshy
 * improved the float code a bit.
 * now floats are automatically forced to be blocks
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2004/11/17 00:44:54  joshy
 * fixed bug in the history manager
 * added cursor support to the link listener
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/11/15 15:20:39  joshy
 * fixes for absolute layout
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/11/12 20:25:18  joshy
 * added hover support to the browser
 * created hover demo
 * fixed bug with inline borders
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2004/11/12 17:05:25  joshy
 * support for fixed positioning
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.11  2004/11/09 02:04:23  joshy
 * support for text-align: justify
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/08 15:10:10  joshy
 * added support for styling :first-letter inline boxes
 * updated the absolute positioning tests
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/07 16:23:18  joshy
 * added support for lighten and darken to bordercolor
 * added support for different colored sides
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/06 22:49:52  joshy
 * cleaned up alice
 * initial support for inline borders and backgrounds
 * moved all of inlinepainter back into inlinerenderer, where it belongs.
 *
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
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

