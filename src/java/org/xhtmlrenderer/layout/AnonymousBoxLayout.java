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

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.AnonymousBlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.util.Uu;

import java.awt.Rectangle;


/**
 * Description of the Class
 *
 * @author empty
 */
public class AnonymousBoxLayout extends InlineLayout {

    /**
     * Constructor for the AnonymousBoxLayout object
     */
    public AnonymousBoxLayout() {
    }


    public Box layout(Context c, Box block) {
        //OK, first set up the current style. All depends on this...
        //CascadedStyle pushed = block.content.getStyle();
        //if (pushed != null) c.pushStyle(pushed);
        // this is to keep track of when we are inside of a form
        //TODO: rethink: saveForm(c, (Element) block.getNode());

        // install a block formatting context for the body,
        // ie. if it's null.

        // set up the outtermost bfc
        /*boolean set_bfc = false;
        if (c.getBlockFormattingContext() == null) {
            BlockFormattingContext bfc = new BlockFormattingContext(block);
            c.pushBFC(bfc);
            set_bfc = true;
            bfc.setWidth((int) c.getExtents().getWidth());
        } */


        // copy the extents
        Rectangle oe = c.getExtents();
        c.setExtents(new Rectangle(oe));

        // calculate the width and height as much as possible
        //adjustWidth(c, block);
        //adjustHeight(c, block);
        block.x = c.getExtents().x;
        block.y = c.getExtents().y;

        // prepare the box w/ styles
        //CHECK: an anonymous box shouldn't have stryles?
        //prepareBox(c, block);
        //HACK: set empty styles here
        //block.margin = new Border();
        block.padding = new Border();
        //block.border = new Border();
        //block.background_color = new Color(0, 0, 0, 0);//transparent

        // set up a float bfc
        //FloatUtil.preChildrenLayout(c, block);

        // set up an absolute bfc
        //Absolute.preChildrenLayout(c, block);

        // save height incase fixed height
        int original_height = block.height;

        // do children's layout
        boolean old_sub = c.isSubBlock();
        c.setSubBlock(false);
        //int tx = block.totalLeftPadding();
        //int ty = block.totalTopPadding();
        //c.translate(tx, ty);
        layoutChildren(c, block);//when this is really an anonymous, InlineLayout.layoutChildren is called
        //c.translate(-tx, -ty);
        c.setSubBlock(old_sub);

        // restore height incase fixed height
        if (block.auto_height == false) {
            Uu.p("restoring original height");
            block.height = original_height;
        }

        // remove the float bfc
        //FloatUtil.postChildrenLayout(c, block);

        // remove the absolute bfc
        //Absolute.postChildrenLayout(c, block);

        // calculate the total outer width
        //block.width = block.totalHorizontalPadding() + block.width;
        //block.height = block.totalVerticalPadding() + block.height;

        //restore the extents
        c.setExtents(oe);

        // account for special positioning
        // need to add bfc/unbfc code for absolutes
        //Relative.setupRelative(block, c);
        // need to add bfc/unbfc code for absolutes
        //Absolute.setupAbsolute(block, c);
        //Fixed.setupFixed(c, block);
        //FloatUtil.setupFloat(c, block);
        //TODO: rethink: setupForm(c, block);
        this.contents_height = block.height;

        // remove the outtermost bfc
        /*if (set_bfc) {
            c.getBlockFormattingContext().doFinalAdjustments();
            c.popBFC();
        } */

        //and now, back to previous style
        //if (pushed != null) c.popStyle();

        // Uu.p("BoxLayout: finished with block: " + block);
        return block;
    }

    // use the passed in 'text'  since that's what we are

    // really laying out instead of the 'node', which is really the

    // parent element.

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content
     * @return Returns
     */
    public Box createBox(Context c, Content content) {

        AnonymousBlockBox block = new AnonymousBlockBox(content);

        return block;
    }

    /**
     * Description of the Method
     *
     * @param box  PARAM
     * @param c    PARAM
     */
    /*
   public void prepareBox( Box box, Context c ) {

       box.border = new Border();

       box.padding = new Border();

       box.margin = new Border();

   }
   */


    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param parent PARAM
     * @param text   PARAM
     * @return Returns
     */
    //CHECK: shouldn't be needed
    /*public Box layout(Context c, Element parent, Node text) {//called by BoxLayout.layoutChildren
        this.parent = parent;
        this.text = text;
        //Box box = new AnonymousBlockBox(text);
        Box box = super.layout(c, new BlockContent(parent, c.css.getStyle(parent)));//BoxLayout
        //Uu.p("AnonymousBoxLayout.layout: returning: " + box);
        return box;
    }*/

    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     * @return     Returns
     */
    /*
   public Box layoutChildren( Context c, Box box ) {
       //Uu.p("AnonymousBoxLayout.layoutChildren() noop" + box);
       return super.layoutChildren( c, box );
       //return box;
   }*/

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.12  2004/12/27 09:40:47  tobega
 * Moved more styling to render stage. Now inlines have backgrounds and borders again.
 *
 * Revision 1.11  2004/12/27 07:43:30  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.10  2004/12/26 10:14:45  tobega
 * Starting to get some semblance of order concerning floats. Still needs more work.
 *
 * Revision 1.9  2004/12/12 03:32:57  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.8  2004/12/10 06:51:01  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.7  2004/12/09 21:18:52  tobega
 * precaution: code still works
 *
 * Revision 1.6  2004/12/09 00:11:51  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.5  2004/12/08 00:42:34  tobega
 * More cleaning of use of Node, more preparation for Content-based inline generation. Also fixed 2 irritating bugs!
 *
 * Revision 1.4  2004/11/18 18:49:48  joshy
 * fixed the float issue.
 * commented out more dead code
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 13:46:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

