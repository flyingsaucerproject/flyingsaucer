/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci, Torbjšrn Gannholm
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
package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.block.Absolute;
import org.xhtmlrenderer.layout.block.Fixed;
import org.xhtmlrenderer.layout.block.FloatUtil;
import org.xhtmlrenderer.layout.content.AnonymousBlockContent;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.layout.content.ContentUtil;
import org.xhtmlrenderer.layout.content.TableContent;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.table.TableBoxing;
import org.xhtmlrenderer.util.Uu;

import java.awt.*;
import java.util.List;


/**
 * Description of the Class
 *
 * @author empty
 */
public class Boxing {

    /**
     * Constructor for the BoxLayout object
     */
    private Boxing() {
    }


    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content PARAM
     * @return Returns
     */
    public static Box layout(Context c, Content content) {
        Box block = null;
        if (content instanceof AnonymousBlockContent) {
            return AnonymousBoxing.layout(c, content);
        } else if (content instanceof TableContent) {
            return TableBoxing.layout(c, content);
        } else {
            block = new BlockBox();
        }
        block.content = content;
        return layout(c, block);
    }

    public static Box layout(Context c, Box block) {
        //OK, first set up the current style. All depends on this...
        CascadedStyle pushed = block.content.getStyle();
        if (pushed != null) c.pushStyle(pushed);
        // this is to keep track of when we are inside of a form
        //TODO: rethink: saveForm(c, (Element) block.getNode());

        String attachment = c.getCurrentStyle().getStringProperty(CSSName.BACKGROUND_ATTACHMENT);
        if (attachment != null && attachment.equals("fixed")) {
            block.setChildrenExceedBounds(true);
        }
        // install a block formatting context for the body,
        // ie. if it's null.
        
        // set up the outtermost bfc
        boolean set_bfc = false;
        if (c.getBlockFormattingContext() == null) {
            BlockFormattingContext bfc = new BlockFormattingContext(block);
            c.pushBFC(bfc);
            set_bfc = true;
            bfc.setWidth((int) c.getExtents().getWidth());
        }


        // copy the extents
        Rectangle oe = c.getExtents();
        c.setExtents(new Rectangle(oe));
        
        // calculate the width and height as much as possible
        adjustWidth(c, block);
        adjustHeight(c, block);
        block.x = c.getExtents().x;
        block.y = c.getExtents().y;

        // set up a float bfc
        FloatUtil.preChildrenLayout(c, block);
        
        // set up an absolute bfc
        Absolute.preChildrenLayout(c, block);
        
        // save height incase fixed height
        int original_height = block.height;

        // do children's layout
        boolean old_sub = c.isSubBlock();
        c.setSubBlock(false);
        int tx = block.totalLeftPadding(c.getCurrentStyle());
        int ty = block.totalTopPadding(c.getCurrentStyle());
        c.translate(tx, ty);
        layoutChildren(c, block);//when this is really an anonymous, InlineLayout.layoutChildren is called
        c.translate(-tx, -ty);
        c.setSubBlock(old_sub);

        // restore height incase fixed height
        if (block.auto_height == false) {
            Uu.p("restoring original height");
            block.height = original_height;
        }

        // remove the float bfc
        FloatUtil.postChildrenLayout(c, block);
        
        // remove the absolute bfc
        Absolute.postChildrenLayout(c, block);
        
        // calculate the total outer width
        block.width = block.totalHorizontalPadding(c.getCurrentStyle()) + block.width;
        block.height = block.totalVerticalPadding(c.getCurrentStyle()) + block.height;
        
        //restore the extents
        c.setExtents(oe);

        // account for special positioning
        // need to add bfc/unbfc code for absolutes
        //Relative.setupRelative(block, c);
        // need to add bfc/unbfc code for absolutes
        Absolute.setupAbsolute(block, c);
        Fixed.setupFixed(c, block);
        FloatUtil.setupFloat(c, block);
        //TODO: rethink: setupForm(c, block);

        // remove the outtermost bfc
        if (set_bfc) {
            c.getBlockFormattingContext().doFinalAdjustments();
            c.popBFC();
        }

        //and now, back to previous style
        if (pushed != null) c.popStyle();

        // Uu.p("BoxLayout: finished with block: " + block);
        return block;
    }

    // calculate the width based on css and available space
    private static void adjustWidth(Context c, Box block) {
        if (block.content instanceof AnonymousBlockContent) {
            return;
        }
        // initalize the width to all the available space
        //block.width = c.getExtents().width;
        CalculatedStyle style = c.getCurrentStyle();
        //if (c.css.hasProperty(elem, "width", false)) {
        if (style.hasProperty("width")) {
            // if it is a sub block then don't mess with the width
            if (c.isSubBlock()) {
                /*if (!elem.getNodeName().equals("td")) {
                    Uu.p("ERRRRRRRRRRORRRR!!! in a sub block that's not a TD!!!!");
                }*/
                return;
            }
            //float new_width = c.css.getFloatProperty(elem, "width", c.getExtents().width, false);
            float new_width = style.getFloatPropertyRelative("width", c.getExtents().width);
            c.getExtents().width = (int) new_width;
            block.width = (int) new_width;
            //block.auto_width = false;
        }
    }

    // calculate the height based on css and available space
    private static void adjustHeight(Context c, Box block) {
        if (block.content instanceof AnonymousBlockContent) {
            return;
        }
        CalculatedStyle style = c.getCurrentStyle();
        if (style.hasProperty("height")) {
            float new_height = style.getFloatPropertyRelative("height", c.getExtents().height);
            c.getExtents().height = (int) new_height;
            block.height = (int) new_height;
            block.auto_height = false;
        }
    }


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     * @return Returns
     */
    public static Box layoutChildren(Context c, Box box) {
        List contentList = box.content.getChildContent(c);
        if (contentList == null) return box;
        if (contentList.size() == 0) return box;//we can do this if there is no content, right?

        if (ContentUtil.hasBlockContent(contentList)) {//this should be block layed out
            BlockBoxing.layoutContent(c, box, contentList, box);
        } else {
            InlineBoxing.layoutContent(c, box, contentList);
        }
        return box;
    }

    /**
     * Gets the padding attribute of the BoxLayout object
     *
     * @param c   PARAM
     * @param box PARAM
     * @return The padding value
     */
    public static Border getPadding(Context c, Box box) {
        Border padding = c.getCurrentStyle().getPaddingWidth();
        return padding;
    }


    /**
     * Gets the margin attribute of the BoxLayout object
     *
     * @param c   PARAM
     * @param box PARAM
     * @return The margin value
     */
    public static Border getMargin(Context c, Box box) {
        Border margin = c.getCurrentStyle().getMarginWidth();
        return margin;
    }

    public static Border getBorder(Context c, Box block) {
        Border border = LayoutUtil.getBorder(block, c.getCurrentStyle());
        return border;
    }

    /**
     * Gets the backgroundColor attribute of the BoxLayout object
     *
     * @param c   PARAM
     * @param box PARAM
     * @return The backgroundColor value
     */
    public static Color getBackgroundColor(Context c, Box box) {
        Color bgc = new Color(0, 0, 0, 0);
        CalculatedStyle style = c.getCurrentStyle();
        if (style.isIdentifier(CSSName.BACKGROUND_COLOR)) {
            String value = style.getStringProperty("background-color");
            if (value.equals("transparent")) {
                return bgc;
            }
        }
        bgc = style.getBackgroundColor();
        return bgc;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2005/01/05 01:10:14  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.2  2005/01/02 12:22:16  tobega
 * Cleaned out old layout code
 *
 * Revision 1.1  2005/01/02 09:32:41  tobega
 * Now using mostly static methods for layout
 *
 * Revision 1.61  2005/01/02 01:00:08  tobega
 * Started sketching in code for handling replaced elements in the NamespaceHandler
 *
 * Revision 1.60  2005/01/01 23:38:37  tobega
 * Cleaned out old rendering code
 *
 * Revision 1.59  2005/01/01 22:37:43  tobega
 * Started adding in the table support.
 *
 * Revision 1.58  2004/12/29 10:39:32  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.57  2004/12/28 01:48:23  tobega
 * More cleaning. Magically, the financial report demo is starting to look reasonable, without any effort being put on it.
 *
 * Revision 1.56  2004/12/27 09:40:47  tobega
 * Moved more styling to render stage. Now inlines have backgrounds and borders again.
 *
 * Revision 1.55  2004/12/27 07:43:30  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.54  2004/12/24 08:46:49  tobega
 * Starting to get some semblance of order concerning floats. Still needs more work.
 *
 * Revision 1.53  2004/12/20 23:25:31  tobega
 * Cleaned up handling of absolute boxes and went back to correct use of anonymous boxes in ContentUtil
 *
 * Revision 1.52  2004/12/16 17:22:25  joshy
 * minor code cleanup
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.51  2004/12/16 17:10:41  joshy
 * fixed box bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.50  2004/12/16 15:53:08  joshy
 * fixes for absolute layout
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.49  2004/12/14 02:28:47  joshy
 * removed some comments
 * some bugs with the backgrounds still
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.48  2004/12/14 01:56:22  joshy
 * fixed layout width bugs
 * fixed extra border on document bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.47  2004/12/13 15:15:56  joshy
 * fixed bug where inlines would pick up parent styles when they aren't supposed to
 * fixed extra Xx's in printed text
 * added conf boolean to turn on box outlines
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.46  2004/12/13 01:29:40  tobega
 * Got the scrollbars back (by accident), and now we should be able to display DocumentFragments as well as Documents, if someone finds that useful.
 *
 * Revision 1.45  2004/12/12 18:06:51  tobega
 * Made simple layout (inline and box) a bit easier to understand
 *
 * Revision 1.44  2004/12/12 05:51:48  tobega
 * Now things run. But there is a lot to do before it looks as nice as it did. At least we now have :before and :after content and handling of breaks by css.
 *
 * Revision 1.43  2004/12/12 04:18:56  tobega
 * Now the core compiles at least. Now we must make it work right. Table layout is one point that really needs to be looked over
 *
 * Revision 1.42  2004/12/12 03:32:58  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.41  2004/12/12 03:04:31  tobega
 * Making progress
 *
 * Revision 1.40  2004/12/11 23:36:48  tobega
 * Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.39  2004/12/11 21:14:48  tobega
 * Prepared for handling run-in content (OK, I know, a side-track). Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.38  2004/12/11 18:18:10  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
 * Revision 1.37  2004/12/10 06:51:02  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.36  2004/12/09 21:18:52  tobega
 * precaution: code still works
 *
 * Revision 1.35  2004/12/09 00:11:51  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.34  2004/12/08 00:42:34  tobega
 * More cleaning of use of Node, more preparation for Content-based inline generation. Also fixed 2 irritating bugs!
 *
 * Revision 1.33  2004/12/06 23:41:14  tobega
 * More cleaning of use of Node, more preparation for Content-based inline generation.
 *
 * Revision 1.32  2004/12/05 05:00:39  joshy
 * fixed bug that prevented explict box heights from working.
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.31  2004/12/05 00:48:57  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.30  2004/12/01 01:57:00  joshy
 * more updates for float support.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.29  2004/11/30 20:38:49  joshy
 * cleaned up the float and absolute interfaces a bit
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.28  2004/11/30 20:28:27  joshy
 * support for multiple floats on a single line.
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.27  2004/11/27 15:46:38  joshy
 * lots of cleanup to make the code clearer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.26  2004/11/18 19:10:04  joshy
 * added bottom support to absolute positioning
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.25  2004/11/18 18:49:48  joshy
 * fixed the float issue.
 * commented out more dead code
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.24  2004/11/18 14:26:22  joshy
 * more code cleanup
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.23  2004/11/18 02:51:14  joshy
 * moved more code out of the box into custom classes
 * added more preload logic to the default layout's preparebox method
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.21  2004/11/16 07:25:09  tobega
 * Renamed HTMLPanel to BasicPanel
 *
 * Revision 1.20  2004/11/15 15:20:38  joshy
 * fixes for absolute layout
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.19  2004/11/14 16:40:58  joshy
 * refactored layout factory
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.18  2004/11/12 18:51:00  joshy
 * fixed repainting issue for background-attachment: fixed
 * added static util methods and get minimum size to graphics 2d renderer
 * added test for graphics 2d renderer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.17  2004/11/12 17:05:24  joshy
 * support for fixed positioning
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/11/09 15:53:48  joshy
 * initial support for hover (currently disabled)
 * moved justification code into it's own class in a new subpackage for inline
 * layout (because it's so blooming complicated)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2004/11/08 20:50:58  joshy
 * improved float support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/11/08 15:10:10  joshy
 * added support for styling :first-letter inline boxes
 * updated the absolute positioning tests
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/11/07 13:39:17  joshy
 * fixed missing borders on the table
 * changed td and th to display:table-cell
 * updated isBlockLayout() code to fix double border problem with tables
 *
 * -j
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2004/11/06 22:49:51  joshy
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
 * Revision 1.11  2004/11/05 18:45:14  joshy
 * support for floated blocks (not just inline blocks)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/04 15:35:44  joshy
 * initial float support
 * includes right and left float
 * cannot have more than one float per line per side
 * floats do not extend beyond enclosing block
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/03 23:54:33  joshy
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
 * Revision 1.8  2004/11/03 15:17:04  joshy
 * added intial support for absolute positioning
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2004/10/28 02:13:40  joshy
 * finished moving the painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/10/28 01:34:23  joshy
 * moved more painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/10/26 00:13:14  joshy
 * added threaded layout support to the BasicPanel
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/23 13:46:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

