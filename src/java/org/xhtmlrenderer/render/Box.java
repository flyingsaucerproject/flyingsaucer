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

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.layout.content.Content;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Description of the Class
 *
 * @author empty
 */
public class Box {
    /**
     * Constructor for the Box object
     */
    public Box() {
        this(true);
    }

    /**
     * Constructor for the Box object
     *
     * @param create_substyles PARAM
     */
    public Box(boolean create_substyles) {
        boxes = new ArrayList();
        /*if (create_substyles) {
            this.click_styles = new Box(false);
        }*/
    }

    /**
     * Constructor for the Box object
     *
     * @param x      PARAM
     * @param y      PARAM
     * @param width  PARAM
     * @param height PARAM
     */
    public Box(int x, int y, int width, int height) {
        this();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    protected BlockFormattingContext blockFormattingContext = null;

    public BlockFormattingContext getBlockFormattingContext() {
        return blockFormattingContext;
    }

    public void setBlockFormattingContext(BlockFormattingContext blockFormattingContext) {
        this.blockFormattingContext = blockFormattingContext;
    }

    public Content content;

    public JComponent component = null;

    //public boolean restyle;//used during render if things have changed because of e.g. hover
    //public boolean hover;//dynamic attribute set

    // dimensions stuff
    /**
     * Description of the Field
     */
    public int x;
    /**
     * Description of the Field
     */
    public int y;
    /**
     * Description of the Field
     */
    public int width;
    /**
     * Description of the Field
     */
    public int height;

    // position stuff
    /**
     * Description of the Field
     */
    //public boolean relative = false;
    public boolean fixed = false;
    public boolean absolute = false;

    public boolean floated = false;

    /**
     * Description of the Field
     */
    public int top = 0;
    public boolean top_set = false;
    /**
     * Description of the Field
     */
    public int right = 0;
    /**
     * Description of the Field
     */
    public boolean right_set = false;
    /**
     * Description of the Field
     */
    public int bottom = 0;
    /**
     * Description of the Field
     */
    public boolean bottom_set = false;
    /**
     * Description of the Field
     */
    public int left = 0;
    public boolean left_set = false;
    

    // margins, borders, and padding stuff
    /**
     * Description of the Field
     */
//    public BorderColor border_color;

/*    public BorderColor getBorderColor() {
        return border_color;
    }*/

    /**
     * Description of the Field
     */
    //public Border padding;
    /**
     * Description of the Field
     */
    //public Border border;
    /**
     * Description of the Field
     */
    //public Border margin;
    /**
     * Description of the Field
     */
//    public String border_style;

    /**
     * Description of the Field
     */
    //public Box click_styles;

    // foreground stuff
    /**
     * Description of the Field
     */
    //public Color color;

    // background stuff
    /**
     * Description of the Field
     */
    //public Color background_color;
    /**
     * Description of the Field
     */
    public Image background_image;
    /**
     * Description of the Field
     */
    public String repeat;
    /**
     * Description of the Field
     */
    public String attachment;
    /**
     * Description of the Field
     */
    public int background_position_vertical = 0;
    /**
     * Description of the Field
     */
    public int background_position_horizontal = 0;
// --Commented out by Inspection START (2005-01-05 01:06):
//    /**
//     * Description of the Field
//     */
//    public boolean clicked = false;
// --Commented out by Inspection STOP (2005-01-05 01:06)

    // list stuff
    /**
     * Description of the Field
     */
    public int list_count = -1;
    /**
     * Description of the Field
     */
    private Box parent;

    // children stuff
    /**
     * Description of the Field
     */
    private List boxes;

    // printing stuff
// --Commented out by Inspection START (2005-01-05 01:05):
//    /**
//     * Description of the Field
//     */
//    public boolean auto_width = true;
// --Commented out by Inspection STOP (2005-01-05 01:05)

    /**
     * Description of the Field
     */
    public boolean auto_height = true;


    /**
     * Return true if the target coordinates are inside of this box. The target
     * coordinates are already translated to be relative to the origin of this
     * box. ie x=0 & y=0. Thus the point 100,100 in a box with coordinates 20,20
     * x 90x90 would have the target coordinates passed in as 80,80 and the
     * function would return true.
     *
     * @param x PARAM
     * @param y PARAM
     * @return Returns
     */

    public boolean contains(int x, int y) {
        if ((x >= 0) && (x <= 0 + this.width)) {
            if ((y >= 0) && (y <= 0 + this.height)) {
                return true;
            }
        }
        /*
         * if((Xx >= this.Xx) && (Xx<= this.Xx + this.width)) {
         * if((y>=this.y) && (y<=this.y + this.height)) {
         * return true;
         * }
         * }
         */
        return false;
    }

    /**
     * Description of the Method
     *
     * @param style
     * @return Returns
     */
    public int totalHorizontalPadding(CalculatedStyle style) {
        int pd = 0;
        Border margin = style.getMarginWidth();
        if (margin != null) {
            pd += margin.left + margin.right;
        }
        Border padding = style.getPaddingWidth();
        if (padding != null) {
            pd += padding.left + padding.right;
        }
        Border border = LayoutUtil.getBorder(this, style);
        if (border != null) {
            pd += border.left + border.right;
        }
        return pd;
    }

    /**
     * Description of the Method
     *
     * @param style
     * @return Returns
     */
    public int totalVerticalPadding(CalculatedStyle style) {
        int pd = 0;
        Border margin = style.getMarginWidth();
        if (margin != null) {
            pd += margin.top + margin.bottom;
        }
        Border padding = style.getPaddingWidth();
        if (padding != null) {
            pd += padding.top + padding.bottom;
        }
        Border border = LayoutUtil.getBorder(this, style);
        if (border != null) {
            pd += border.top + border.bottom;
        }
        return pd;
    }

    /**
     * Description of the Method
     *
     * @param style
     * @return Returns
     */
    public int totalTopPadding(CalculatedStyle style) {
        int pd = 0;
        Border margin = style.getMarginWidth();
        if (margin != null) {
            pd += margin.top;
        }
        Border padding = style.getPaddingWidth();
        if (padding != null) {
            pd += padding.top;
        }
        Border border = LayoutUtil.getBorder(this, style);
        if (border != null) {
            pd += border.top;
        }
        return pd;
    }

    /**
     * Description of the Method
     *
     * @param style
     * @return Returns
     */
    public int totalLeftPadding(CalculatedStyle style) {
        int pd = 0;
        Border margin = style.getMarginWidth();
        if (margin != null) {
            pd += margin.left;
        }
        Border padding = style.getPaddingWidth();
        if (padding != null) {
            pd += padding.left;
        }
        Border border = LayoutUtil.getBorder(this, style);
        if (border != null) {
            pd += border.left;
        }
        return pd;
    }

    public int totalRightPadding(CalculatedStyle style) {
        int pd = 0;
        Border margin = style.getMarginWidth();
        if (margin != null) {
            pd += margin.right;
        }
        Border padding = style.getPaddingWidth();
        if (padding != null) {
            pd += padding.right;
        }
        Border border = LayoutUtil.getBorder(this, style);
        if (border != null) {
            pd += border.right;
        }
        return pd;
    }


    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Box: ");
        if (content == null) {
            sb.append(" null content, ");
        } else {
            String nm = content.getClass().getName();
            nm = nm.substring(nm.lastIndexOf('.') + 1, nm.length());
            sb.append(nm);// + " (" + content.hashCode() + ")");
            sb.append("-");
            if (content.getElement() != null) {
                sb.append(content.getElement().getNodeName());
            }
        }
        sb.append(" (" + x + "," + y + ")->(" + width + " x " + height + ")");
        // CLN: (PWW 13/08/04)
        //sb.append(" color: " + color + " background-color: " + background_color + " ");
        return sb.toString();
    }

    /**
     * Sets the parent attribute of the Box object
     *
     * @param box The new parent value
     */
    public void setParent(Box box) {
        this.parent = box;
    }

    /**
     * Gets the height attribute of the Box object
     *
     * @return The height value
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the width attribute of the Box object
     *
     * @return The width value
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the parent attribute of the Box object
     *
     * @return The parent value
     */
    public Box getParent() {
        return parent;
    }

    /**
     * Gets the childCount attribute of the Box object
     *
     * @return The childCount value
     */
    public int getChildCount() {
        return boxes.size();
    }

    /**
     * Gets the child attribute of the Box object
     *
     * @param i PARAM
     * @return The child value
     */
    public Box getChild(int i) {
        return (Box) boxes.get(i);
    }

    /**
     * Adds a feature to the Child attribute of the Box object
     *
     * @param child The feature to be added to the Child attribute
     */
    public void addChild(Box child) {
        if (child == null) throw new NullPointerException("trying to add null child");
        child.setParent(this);
        boxes.add(child);
        if (child.isChildrenExceedBounds()) {
            setChildrenExceedBounds(true);
        }
    }

    /**
     * Gets the childIterator attribute of the Box object
     *
     * @return The childIterator value
     */
    public Iterator getChildIterator() {
        return boxes.iterator();
    }

    public void removeAllChildren() {
        boxes.clear();
    }

// --Commented out by Inspection START (2005-01-05 01:06):
//    /**
//     * Gets the anonymous attribute of the Box object
//     *
//     * @return The anonymous value
//     */
//    public boolean isAnonymous() {
//        return false;
//    }
// --Commented out by Inspection STOP (2005-01-05 01:06)

    private boolean children_exceeds;

    public boolean isChildrenExceedBounds() {
        return children_exceeds;
    }

    public void setChildrenExceedBounds(boolean children_exceeds) {
        this.children_exceeds = children_exceeds;
    }


    /**
     * This generates a string which fully represents every facet of the
     * rendered box (or at least as much as possible without actually drawing
     * it). This includes dimensions, location, color, backgrounds, images,
     * text, and pretty much everything else. The test string is used by the
     * regression tests.
     *
     * @return The testString value
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
        if (this instanceof LineBox) {
            sb.append("line:");
        } else if (this instanceof InlineBox) {
            sb.append("inline:");
        } else {
            sb.append("box:");
        }

        // element
        //sb.append("-content:" + this.getContent());

        // dimensions and location
        sb.append("-box(" + x + "," + y + ")-(" + width + "x" + height + ")");

        // positioning info
        /*if (relative) {
            sb.append("-relative");
        }*/
        if (fixed) {
            sb.append("-fixed");
        }
        sb.append("-pos(" + top + "," + right + "," + bottom + "," + left + ")");
        if (floated) {
            sb.append("-floated");
        }

        // colors and insets
        //sb.append("-colors(for" + getColorTestString(color));
        //sb.append("-bor" + getColorTestString(border_color));
        //sb.append("-bak" + getColorTestString(background_color) + ")");
        //sb.append("-style(" + border_style + ")");
        //sb.append("-insets(mar" + getBorderTestString(margin));
        //sb.append("-bor" + getBorderTestString(border));
        //sb.append("-pad" + getBorderTestString(padding) + ")");

        // background images
        sb.append("-backimg(" + background_image);
        sb.append("-" + repeat);
        sb.append("-" + attachment);
        sb.append("-" + background_position_vertical);
        sb.append("-" + background_position_horizontal + ")");

        //sb.append("-value:" + this.getClosestNode().getNodeValue());
        return sb.toString();
    }

// --Commented out by Inspection START (2005-01-05 01:06):
//    /**
//     * Gets the colorTestString attribute of the Box object
//     *
//     * @param c PARAM
//     * @return The colorTestString value
//     */
//    public String getColorTestString(Color c) {
//        if (c == null) {
//            return "[null]";
//        }
//        return "#" + Integer.toHexString(c.getRGB());
//    }
// --Commented out by Inspection STOP (2005-01-05 01:06)

// --Commented out by Inspection START (2005-01-05 01:06):
//    /**
//     * Gets the borderTestString attribute of the Box object
//     *
//     * @param b PARAM
//     * @return The borderTestString value
//     */
//    public String getBorderTestString(Border b) {
//        if (b == null) {
//            return "[null]";
//        }
//        return "(" + b.top + "," + b.right + "," + b.bottom + "," + b.left + ")";
//    }
// --Commented out by Inspection STOP (2005-01-05 01:06)

    /**
     * If this box represents the text of an inline element then returns true.
     * Thus, the text "<i>some text</i>" if the following example would be an inline element:
     * <pre>
     * &lt;p&gt; text &lt;b&gt;some text&lt;/b&gt; text &lt;/p&gt;
     * </pre>
     * The text "<i>some text</i>" in the next example <b>would not</b> be an inline element,
     * because it is merely the text child of a block element
     * <p/>
     * <pre>
     * &lt;p&gt; some text &lt;/p&gt;
     * </pre>
     * </p>
     */
    public boolean isInlineElement() {
        return false;
    }

    public CascadedStyle firstLineStyle;
    public CascadedStyle firstLetterStyle;
}

/*
 * $Id$
 *
 * $Log$
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

