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
package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.layout.inline.VerticalAlign;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.InlineBox;

import javax.swing.*;
import java.awt.*;

/**
 * Description of the Class
 *
 * @author empty
 */
public class LineBreaker {


    // change this to use the existing block instead of a new one
    /**
     * Description of the Method
     *
     * @param c          PARAM
     * @param content    PARAM
     * @param avail      PARAM
     * @param prev_align PARAM
     * @return Returns
     */
    public static InlineBox generateReplacedInlineBox(Context c, Content content, int avail, InlineBox prev_align) {
        //Uu.p("generating replaced Inline Box");
        Rectangle bounds = null;
        BlockBox block = null;
        JComponent cc = c.getNamespaceHandler().getCustomComponent(content.getElement(), c);
        if (cc != null) {
            bounds = cc.getBounds();
        } else {
            block = (BlockBox) Boxing.layout(c, content);
            //Uu.p("got a block box from the sub layout: " + block);
            bounds = new Rectangle(block.x, block.y, block.width, block.height);
            //Uu.p("bounds = " + bounds);
        }
        /*
         * joshy: change this to just modify the existing block instead of creating
         * a  new one
         */
        // create new inline (null text is safe!)
        InlineBox box = new InlineBox();
        box.content = content;
        box.width = bounds.width;
        box.height = bounds.height;
        box = styleBox(c, prev_align, box);
        //joshy: activate this: box.block = block
        //Uu.p("created a new inline box");
        //box.replaced = true;
        box.sub_block = block;
        if (block != null) block.setParent(box);
        box.component = cc;

        // set up the extents
        box.width = bounds.width + box.totalHorizontalPadding(c.getCurrentStyle());
        box.height = bounds.height + box.totalVerticalPadding(c.getCurrentStyle());
        box.break_after = false;

        // if it won't fit on this line, then put it on the next one
        if (box.width > avail) {
            box.break_before = true;
            box.x = 0;
        }

        // return
        //Uu.p("last replaced = " + box);
        return box;
    }

    //Only called for FloatedBlock and InlineBlock!
    public static InlineBox styleBox(Context c, InlineBox prev_align, InlineBox box) {
        CalculatedStyle style = c.getCurrentStyle();
        // use the prev_align to calculate the Xx
        if (prev_align != null && !prev_align.break_after) {
            box.x = prev_align.x + prev_align.width;
        } else {
            box.x = 0;
        }

        box.y = 0;// it's relative to the line
        box.break_after = true;

        // do vertical alignment
        VerticalAlign.setupVerticalAlign(c, style, box);
        // adjust width based on borders and padding
        box.width += box.totalHorizontalPadding(c.getCurrentStyle());
        //box.height += box.totalVerticalPadding();
        
        return box;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.47  2005/01/06 00:58:41  tobega
 * Cleanup of code. Aiming to get rid of references to Content in boxes
 *
 * Revision 1.46  2005/01/05 17:56:35  tobega
 * Reduced memory more, especially by using WeakHashMap for caching Mappers. Look over other caching to use similar schemes (cache when memory available).
 *
 * Revision 1.45  2005/01/05 01:10:15  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.44  2005/01/02 12:22:19  tobega
 * Cleaned out old layout code
 *
 * Revision 1.43  2005/01/02 02:12:48  tobega
 * img tags now handled as custom components.
 *
 * Revision 1.42  2004/12/29 10:39:33  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.41  2004/12/28 02:15:18  tobega
 * More cleaning.
 *
 * Revision 1.40  2004/12/27 09:40:47  tobega
 * Moved more styling to render stage. Now inlines have backgrounds and borders again.
 *
 * Revision 1.39  2004/12/27 07:43:31  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.38  2004/12/15 00:53:40  tobega
 * Started playing a bit with inline box, provoked a few nasties, probably created some, seems to work now
 *
 * Revision 1.37  2004/12/12 21:02:37  tobega
 * Images working again
 *
 * Revision 1.36  2004/12/12 04:18:57  tobega
 * Now the core compiles at least. Now we must make it work right. Table layout is one point that really needs to be looked over
 *
 * Revision 1.35  2004/12/12 03:32:59  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.34  2004/12/12 03:18:34  tobega
 * Making progress
 *
 * Revision 1.33  2004/12/11 23:36:49  tobega
 * Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.32  2004/12/11 21:14:48  tobega
 * Prepared for handling run-in content (OK, I know, a side-track). Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.31  2004/12/10 06:51:02  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.30  2004/12/09 00:11:52  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.29  2004/12/05 18:11:38  tobega
 * Now uses style cache for pseudo-element styles. Also started preparing to replace inline node handling with inline content handling.
 *
 * Revision 1.28  2004/12/05 14:35:39  tobega
 * Cleaned up some usages of Node (and removed unused stuff) in layout code. The goal is to pass "better" objects than Node wherever possible in an attempt to shake out the bugs in tree-traversal (probably often unnecessary tree-traversal)
 *
 * Revision 1.27  2004/12/05 00:48:58  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.26  2004/12/01 01:57:01  joshy
 * more updates for float support.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.25  2004/11/27 15:46:39  joshy
 * lots of cleanup to make the code clearer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.24  2004/11/23 02:41:59  joshy
 * fixed vertical-align support for first-letter pseudos
 * tested first-line w/ new breaking routines
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.23  2004/11/23 02:11:25  joshy
 * re-enabled text-decoration
 * moved it to it's own class
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.22  2004/11/18 02:37:26  joshy
 * moved most of default layout into layout util or box layout
 *
 * start spliting parts of box layout into the block subpackage
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.21  2004/11/15 14:33:10  joshy
 * fixed line breaking bug with certain kinds of unbreakable lines
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.20  2004/11/14 16:40:58  joshy
 * refactored layout factory
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.19  2004/11/10 14:54:43  joshy
 * code cleanup on aisle 6
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.18  2004/11/10 14:31:15  joshy
 * removed commented out lines
 *
 * Revision 1.17  2004/11/09 16:24:30  joshy
 * moved float code into separate class
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/11/09 16:07:57  joshy
 * moved vertical align code
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2004/11/09 15:53:49  joshy
 * initial support for hover (currently disabled)
 * moved justification code into it's own class in a new subpackage for inline
 * layout (because it's so blooming complicated)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/11/09 00:41:44  joshy
 * fixed merge error
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/11/09 00:36:54  tobega
 * Fixed some NPEs
 *
 * Revision 1.12  2004/11/09 00:36:08  joshy
 * fixed more text alignment
 * added menu item to show font metrics
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.11  2004/11/08 23:53:27  joshy
 * update for first-line support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/08 23:15:56  tobega
 * Changed pseudo-element styling to just return CascadedStyle
 *
 * Revision 1.9  2004/11/08 22:08:00  joshy
 * improved inline border formatting and text drawing
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/08 20:50:59  joshy
 * improved float support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2004/11/08 16:56:52  joshy
 * added first-line pseudo-class support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/08 15:10:10  joshy
 * added support for styling :first-letter inline boxes
 * updated the absolute positioning tests
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/06 22:49:52  joshy
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
 * Revision 1.4  2004/11/04 15:35:45  joshy
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
 * Revision 1.3  2004/10/23 13:46:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

