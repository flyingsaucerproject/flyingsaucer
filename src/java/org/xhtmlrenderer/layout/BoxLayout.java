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

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.block.Absolute;
import org.xhtmlrenderer.layout.block.Fixed;
import org.xhtmlrenderer.layout.block.FloatUtil;
import org.xhtmlrenderer.layout.block.Relative;
import org.xhtmlrenderer.layout.content.*;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.BoxRenderer;
import org.xhtmlrenderer.render.Renderer;
import org.xhtmlrenderer.util.u;

import java.awt.*;
import java.util.Iterator;


/**
 * Description of the Class
 *
 * @author empty
 */
public class BoxLayout extends DefaultLayout {

    /**
     * Description of the Field
     */
    public int contents_height;

    /**
     * Constructor for the BoxLayout object
     */
    public BoxLayout() {
    }

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content
     * @return Returns
     */
    public Box createBox(Context c, Content content) {
        BlockBox block = new BlockBox();
        block.setNode(content.getElement());
        block.setContent(content);
        //String attachment = c.css.getStringProperty(block.getRealElement(), "background-attachment", false);
        String attachment = c.css.getStyle(block.getRealElement()).propertyByName("background-attachment").computedValue().asString();
        if (attachment != null && attachment.equals("fixed")) {
            block.setChildrenExceedBounds(true);
        }
        return block;
    }


    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content PARAM
     * @return Returns
     */
    public Box layout(Context c, Content content) {
        BlockBox block = (BlockBox) createBox(c, content);
        return layout(c, block);
    }

    public Box layout(Context c, Box block) {
        // this is to keep track of when we are inside of a form
        saveForm(c, (Element) block.getNode());

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

        //u.p("elem = " + block.node);

        // copy the extents
        Rectangle oe = c.getExtents();
        c.setExtents(new Rectangle(oe));
        
        // calculate the width and height as much as possible
        adjustWidth(c, block);
        adjustHeight(c, block);
        block.x = c.getExtents().x;
        block.y = c.getExtents().y;

        // prepare the box w/ styles
        prepareBox(c, block);

        // set up a float bfc
        FloatUtil.preChildrenLayout(c, block);
        
        // set up an absolute bfc
        Absolute.preChildrenLayout(c, block);
        
        // save height incase fixed height
        int original_height = block.height;

        // do children's layout
        boolean old_sub = c.isSubBlock();
        c.setSubBlock(false);
        int tx = block.totalLeftPadding();
        int ty = block.totalTopPadding();
        c.translate(tx, ty);
        layoutChildren(c, block);//when this is really an anonymous, InlineLayout.layoutChildren is called
        c.translate(-tx, -ty);
        c.setSubBlock(old_sub);

        // restore height incase fixed height
        if (block.auto_height == false) {
            u.p("restoring original height");
            block.height = original_height;
        }

        // remove the float bfc
        FloatUtil.postChildrenLayout(c, block);
        
        // remove the absolute bfc
        Absolute.postChildrenLayout(c, block);
        
        // calculate the total outer width
        block.width = block.totalHorizontalPadding() + block.width;
        block.height = block.totalVerticalPadding() + block.height;
        
        //restore the extents
        c.setExtents(oe);

        // account for special positioning
        // need to add bfc/unbfc code for absolutes
        Relative.setupRelative(block);
        // need to add bfc/unbfc code for absolutes
        Absolute.setupAbsolute(block);
        Fixed.setupFixed(c, block);
        FloatUtil.setupFloat(c, block);
        setupForm(c, block);
        this.contents_height = block.height;
        
        // remove the outtermost bfc
        if (set_bfc) {
            c.getBlockFormattingContext().doFinalAdjustments();
            c.popBFC();
        }

        return block;
    }

    // calculate the width based on css and available space
    private void adjustWidth(Context c, Box block) {
        if (!(block.getContent() instanceof BlockContent)) {//then it must be anonymous
            return;
        }
        // initalize the width to all the available space
        //block.width = c.getExtents().width;
        Element elem = block.getElement();
        CalculatedStyle style = block.getContent().getStyle();
        //if (c.css.hasProperty(elem, "width", false)) {
        if (style.hasProperty("width")) {
            // if it is a sub block then don't mess with the width
            if (c.isSubBlock()) {
                if (!elem.getNodeName().equals("td")) {
                    u.p("ERRRRRRRRRRORRRR!!! in a sub block that's not a TD!!!!");
                }
                return;
            }
            //float new_width = c.css.getFloatProperty(elem, "width", c.getExtents().width, false);
            float new_width = style.getFloatPropertyRelative("width", c.getExtents().width);
            c.getExtents().width = (int) new_width;
            block.width = (int) new_width;
            block.auto_width = false;
        }
    }

    // calculate the height based on css and available space
    private void adjustHeight(Context c, Box block) {
        if (!(block.getContent() instanceof BlockContent)) {//then it must be anonymous
            return;
        }
        Element elem = block.getElement();
        CalculatedStyle style = block.getContent().getStyle();
        if (style.hasProperty("height")) {
            //float new_height = c.css.getFloatProperty(elem, "height", c.getExtents().height);
            float new_height = style.getFloatPropertyRelative("height", c.getExtents().height);
            c.getExtents().height = (int) new_height;
            block.height = (int) new_height;
            block.auto_height = false;
            u.p("set height to: " + block.height);
        }
    }


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     * @return Returns
     */
    public Box layoutChildren(Context c, Box box) {
        BlockBox block = (BlockBox) box;
        c.shrinkExtents(block);

        // save the original height in case it
        // has a fixed height
        //not used: int original_height = block.height;

        // prepare for the list items
        int old_counter = c.getListCounter();
        c.setListCounter(0);

        java.util.List contentList = box.getContent().getContent(c);
        if (contentList.size() == 0) return box;//we can do this if there is no content, right?
        Iterator contentIterator = contentList.iterator();

        //TODO: how to pass these on to first child?
        CascadedStyle firstLineStyle = null;
        CascadedStyle firstLetterStyle = null;

        while (contentIterator.hasNext()) {
            Object o = contentIterator.next();
            //TODO: can we do this? contentIterator.remove();//chop it off, no need to keep it?
            if (o instanceof FirstLineStyle) {//can actually only be the first object in list
                firstLineStyle = ((FirstLineStyle) o).getStyle();
                continue;
            }
            if (o instanceof FirstLetterStyle) {//can actually only be the first or second object in list
                firstLetterStyle = ((FirstLetterStyle) o).getStyle();
                continue;
            }
            Content currentContent = (Content) o;

            Box child_box = null;
            if (!(currentContent instanceof AnonymousBlockContent)) {
                Layout layout = c.getLayout(currentContent.getElement());
                // update the counter for printing OL list items
                c.setListCounter(c.getListCounter() + 1);

                // execute the layout and get the return bounds
                //c.parent_box = box;
                c.placement_point = new Point(0, box.height);
                c.getBlockFormattingContext().translate(0, box.height);
                child_box = layout.layout(c, currentContent);
                c.getBlockFormattingContext().translate(0, -box.height);
                child_box.list_count = c.getListCounter();
            } else { //AnonymousBlockContent, fail fast if not
                AnonymousBlockContent anonymous = (AnonymousBlockContent) currentContent;
                Layout layout = new AnonymousBoxLayout();
                child_box = layout.layout(c, anonymous);
            }

            box.addChild(child_box);
            // set the child_box location
            child_box.x = 0;
            child_box.y = box.height;

            //joshy fix the 'fixed' stuff later
            // if fixed or abs then don't modify the final layout bounds
            // because fixed elements are removed from normal flow
            if (child_box.fixed) {
                // put fixed positioning in later
                Fixed.positionFixedChild(c, child_box);
            }

            if (child_box.isAbsolute()) {
                Absolute.positionAbsoluteChild(c, child_box);
            }
            
            // skip adjusting the parent box if the child
            // doesn't affect flow layout
            if (LayoutUtil.isOutsideNormalFlow(child_box)) {
                continue;
            }

            // increase the final layout width if the child was greater
            if (child_box.width > box.width) {
                box.width = child_box.width;
            }

            // increase the final layout height by the height of the child
            box.height += child_box.height;
        }
        c.addMaxWidth(box.width);

        c.setListCounter(old_counter);

        c.unshrinkExtents(block);

        return block;
    }


    private void saveForm(Context c, Element elem) {
        if (c.getRenderingContext().getLayoutFactory().isForm(elem)) {
            if (elem.hasAttribute("name")) {
                String name = elem.getAttribute("name");
                String action = elem.getAttribute("action");
                c.setForm(name, action);
            }
        }
    }

    private void setupForm(Context c, Box block) {
        if (c.getRenderingContext().getLayoutFactory().isForm(block.getRealElement())) {
            if (block.getRealElement().hasAttribute("name")) {
                c.setForm(null, null);
            }
        }
    }


    public Renderer getRenderer() {
        return new BoxRenderer();
    }
}

/*
 * $Id$
 *
 * $Log$
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

