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

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.value.Border;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.PersistentBFC;

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
     * Description of the Field
     */
    public Element element;

    /**
     * Description of the Field
     */
    public JComponent component = null;

    /**
     * Description of the Field
     */
    public boolean restyle = false;

    // dimensions stuff
    /**
     * Box x-pos.
     */
    public int x;

    /**
     * Box y-pos.
     */
    public int y;

    /**
     * Box width.
     */
    //public int width;
    public int contentWidth;
    public int rightPadding = 0;
    public int leftPadding = 0;

    public double paginationTranslation = 0;

    private Border marginWidth;

    /**
     * Gets the width attribute of the Box object
     *
     * @return The width value
     */
    public int getWidth() {
        return contentWidth + leftPadding + rightPadding;
    }

    /**
     * Box height.
     */
    public int height;

    // position stuff
    /**
     * True if the box is in fixed position.
     */
    public boolean fixed = false;

    /**
     * True if the box is absolute-positioned.
     */
    public boolean absolute = false;

    /**
     * True if the box is floated.
     */
    public boolean floated = false;

    /**
     * True if the box is relatively positioned
     */
    public boolean relative = false;

    /**
     * Description of the Field
     */
    public int top = 0;

    /**
     * Description of the Field
     */
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

    /**
     * Description of the Field
     */
    public boolean left_set = false;

    /**
     * The Image shown as the Box's background.
     */
    public Image background_image;

    /**
     * The URI for a background image; used in debugging (so we know which bg is
     * being painted)
     */
    public String background_uri;

    /**
     * The background-repeat IdentValue.
     */
    public IdentValue repeat;

    /**
     * The background-attachment IdentValue.
     */
    public IdentValue attachment;

    /**
     * Description of the Field
     */
    public int background_position_vertical = 0;

    /**
     * Description of the Field
     */
    public int background_position_horizontal = 0;

    // list stuff
    /**
     * Description of the Field
     */
    public int list_count = -1;

    // printing stuff
    /**
     * Description of the Field
     */
    public boolean auto_height = true;
    public boolean auto_width = true;

    /**
     * Description of the Field
     */
    public CascadedStyle firstLineStyle;

    /**
     * Description of the Field
     */
    public CascadedStyle firstLetterStyle;

    /**
     * Description of the Field
     */
    protected PersistentBFC persistentBFC = null;

    /**
     * Description of the Field
     */
    private Box parent;

    // children stuff
    /**
     * Description of the Field
     */
    private List boxes;

    /**
     * Description of the Field
     */
    private boolean children_exceeds;
    private boolean fixed_descendant;

    /**
     * Keep track of the start of childrens containing block.
     * Needed for hover.
     */
    public int tx;
    public int ty;


    public boolean clear_left = false;
    public boolean clear_right = false;

    private float marginTopOverride;
    private boolean marginTopOverrideSet = false;

    private float marginBottomOverride;
    private boolean marginBottomOverrideSet = false;

    private boolean movedPastPageBreak;

    /**
     * Constructor for the Box object
     */
    public Box() {
        boxes = new ArrayList();
    }

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
        if ((x >= 0) && (x <= 0 + this.getWidth())) {
            if ((y >= 0) && (y <= 0 + this.height)) {
                return true;
            }
        }
        return false;
    }

    public void adjustWidthForChild(int childWidth) {
        //do nothing, generally
    }


    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Box: ");
        sb.append(" (" + x + "," + y + ")->(" + getWidth() + " x " + height + ")");
        return sb.toString();
    }

    /**
     * Adds a feature to the Child attribute of the Box object
     *
     * @param child The feature to be added to the Child attribute
     */
    public void addChild(Box child) {
        if (child == null) {
            throw new NullPointerException("trying to add null child");
        }
        child.setParent(this);
        boxes.add(child);
        propagateChildProperties(child);
    }

    public void propagateChildProperties(Box child) {
        if (child.isChildrenExceedBounds()) {
            setChildrenExceedBounds(true);
        }
        if (child.isFixedDescendant()) {
            setFixedDescendant(true);
        }
    }

    /**
     * Description of the Method
     */
    public void removeAllChildren() {
        boxes.clear();
    }

    public void reset() {
        removeAllChildren();
        height = tx = ty = 0;
    }

    /**
     * Sets the persistentBFC attribute of the Box object
     *
     * @param persistentBFC The new persistentBFC value
     */
    public void setPersistentBFC(PersistentBFC persistentBFC) {
        this.persistentBFC = persistentBFC;
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
     * Sets the childrenExceedBounds attribute of the Box object
     *
     * @param children_exceeds The new childrenExceedBounds value
     */
    public void setChildrenExceedBounds(boolean children_exceeds) {
        this.children_exceeds = children_exceeds;
    }

    public void setFixedDescendant(boolean fixed_descendant) {
        this.fixed_descendant = fixed_descendant;
        if (this.getParent() != null && fixed_descendant) {
            this.getParent().setFixedDescendant(true);
        }
    }

    /**
     * Gets the persistentBFC attribute of the Box object
     *
     * @return The persistentBFC value
     */
    public PersistentBFC getPersistentBFC() {
        return persistentBFC;
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
     * Gets the childIterator attribute of the Box object
     *
     * @return The childIterator value
     */
    public Iterator getChildIterator() {
        return boxes.iterator();
    }

    /**
     * Gets the childrenExceedBounds attribute of the Box object
     *
     * @return The childrenExceedBounds value
     */
    public boolean isChildrenExceedBounds() {
        return children_exceeds;
    }

    public boolean isFixedDescendant() {
        return fixed_descendant;
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
        sb.append(" " + this.hashCode() + " ");
        if (this instanceof LineBox) {
            sb.append("line:");
        } else if (this instanceof InlineBox) {
            sb.append("inline:");
        } else {
            sb.append("box:");
        }

        sb.append("element:");
        if (this.element != null) {
            sb.append(this.element.getNodeName());
        } else {
            sb.append("null");
        }

        // dimensions and location
        sb.append("-box(" + x + "," + y + ")-(" + getWidth() + "x" + height + ")");

        if (fixed) {
            sb.append("-fixed");
        }
        if (absolute) {
            sb.append("-absolute");
        }
        sb.append("-pos(" + top + "," + right + "," + bottom + "," + left + ")");
        if (floated) {
            sb.append("-floated");
        }

        // no color support yet. wait for later

        // insets
        /*sb.append("insets(");
        sb.append("mar(" + this.margin.top + "," + this.margin.left + "," + this.margin.bottom + "," + this.margin.right + ")");
        sb.append("-bor(" + this.border.top + "," + this.border.left + "," + this.border.bottom + "," + this.border.right + ")");
        sb.append("-pad(" + this.padding.top + "," + this.padding.left + "," + this.padding.bottom + "," + this.padding.right + ")");
        sb.append(")"); */
		
		
        // background images
        sb.append("-backimg(" + background_image);
        sb.append("-" + repeat);
        sb.append("-" + attachment);
        sb.append("-" + background_position_vertical);
        sb.append("-" + background_position_horizontal + ")");

        sb.append("-value:");
        if (element != null) {
            sb.append(element.getNodeValue());
        } else {
            sb.append("null");
        }

        return sb.toString();
    }


    public static final int NOTHING = 0;
    public static final int FLUX = 1;
    public static final int CHILDREN_FLUX = 2;
    public static final int DONE = 3;
    private int state = NOTHING;

    public synchronized int getState() {
        return this.state;
    }

    public synchronized void setState(int state) {
        this.state = state;
    }

    public static String stateToString(int state) {
        switch (state) {
            case NOTHING:
                return "NOTHING";
            case FLUX:
                return "FLUX";
            case CHILDREN_FLUX:
                return "CHILDREN_FLUX";
            case DONE:
                return "DONE";
            default:
                return "unknown";
        }
    }

    public float getMarginTopOverride() {
        return this.marginTopOverride;
    }

    public void setMarginTopOverride(float marginTopOverride) {
        this.marginTopOverride = marginTopOverride;
        this.marginTopOverrideSet = true;
    }


    public float getMarginBottomOverride() {
        return this.marginBottomOverride;
    }

    public void setMarginBottomOverride(float marginBottomOverride) {
        this.marginBottomOverride = marginBottomOverride;
        this.marginBottomOverrideSet = true;
    }

    public boolean crossesPageBreak(Context c) {
        double absTop = getAbsY(c);
        double absBottom = absTop + height;

        double pageHeight = c.getPageInfo().getContentHeight();

        return absBottom > absTop && (int) (absTop / pageHeight) != (int) (absBottom / pageHeight);
    }

    protected double getAbsY(Context c) {
        return c.getOriginOffset().y;
    }

    protected double getDistanceFromPageBreak(Context c, boolean considerTy) {
        double absTop = getAbsY(c);

        if (considerTy) {
            absTop -= ty;
        }

        double pageHeight = c.getPageInfo().getContentHeight();
        double delta = pageHeight - (absTop % pageHeight);

        return delta;
    }

    public void moveToNextPage(Context c, boolean considerTy) {
        double delta = getDistanceFromPageBreak(c, considerTy);
        y += delta;
        paginationTranslation = delta;
        if (considerTy) {
            c.translate(0, (int) delta);
        }
        this.movedPastPageBreak = true;
    }

    public boolean isMovedPastPageBreak() {
        return movedPastPageBreak;
    }

    public void setMovedPastPageBreak(boolean movedPastPageBreak) {
        this.movedPastPageBreak = movedPastPageBreak;
    }

    public Border getMarginWidth() {
        if (marginWidth == null) {
            throw new NullPointerException("internal error: marginWidth is null");
        }
        if (this.marginTopOverrideSet) {
            marginWidth.top = (int) this.marginTopOverride;
        }
        if (this.marginBottomOverrideSet) {
            marginWidth.bottom = (int) this.marginBottomOverride;
        }
        return marginWidth;
    }

    public void setMarginWidth(Context c, float parentWidth) {
        this.marginWidth = c.getCurrentStyle().getMarginWidth(parentWidth, parentWidth, c.getCtx());
    }

    public void setMarginWidth(Border margin) {
        this.marginWidth = new Border(margin);
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.64  2005/10/15 23:39:18  tobega
 * patch from Peter Brant
 *
 * Revision 1.63  2005/10/12 21:17:13  tobega
 * patch from Peter Brant
 *
 * Revision 1.62  2005/10/08 17:40:21  tobega
 * Patch from Peter Brant
 *
 * Revision 1.61  2005/10/06 03:20:22  tobega
 * Prettier incremental rendering. Ran into more trouble than expected and some creepy crawlies and a few pages don't look right (forms.xhtml, splash.xhtml)
 *
 * Revision 1.60  2005/10/02 21:30:00  tobega
 * Fixed a lot of concurrency (and other) issues from incremental rendering. Also some house-cleaning.
 *
 * Revision 1.59  2005/09/30 04:58:05  joshy
 * fixed garbage when showing a document with a fixed positioned block
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.58  2005/09/29 21:34:04  joshy
 * minor updates to a lot of files. pulling in more incremental rendering code.
 * fixed another resize bug
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.57  2005/09/26 22:40:21  tobega
 * Applied patch from Peter Brant concerning margin collapsing
 *
 * Revision 1.56  2005/07/04 00:12:12  tobega
 * text-align now works for table-cells too (is done in render, not in layout)
 *
 * Revision 1.55  2005/06/16 07:24:51  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.54  2005/06/16 04:31:30  joshy
 * added clear support to the box
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.53  2005/05/13 15:23:55  tobega
 * Done refactoring box borders, margin and padding. Hover is working again.
 *
 * Revision 1.52  2005/05/13 11:49:59  tobega
 * Started to fix up borders on inlines. Got caught up in refactoring.
 * Boxes shouldn't cache borders and stuff unless necessary. Started to remove unnecessary references.
 * Hover is not working completely well now, might get better when I'm done.
 *
 * Revision 1.51  2005/05/08 14:36:58  tobega
 * Refactored away the need for having a context in a CalculatedStyle
 *
 * Revision 1.50  2005/04/22 17:19:19  joshy
 * resovled conflicts in Box
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.49  2005/04/21 18:16:08  tobega
 * Improved handling of inline padding. Also fixed first-line handling according to spec.
 *
 * Revision 1.48  2005/04/19 17:51:18  joshy
 * fixed absolute positioning bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.47  2005/04/19 13:59:48  pdoubleya
 * Added defaults for margin, padding, border.
 *
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

