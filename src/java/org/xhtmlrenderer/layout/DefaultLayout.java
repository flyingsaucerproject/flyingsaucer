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


import org.w3c.dom.Node;
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.DefaultRenderer;
import org.xhtmlrenderer.render.Renderer;
import org.xhtmlrenderer.util.u;

import java.awt.*;


/**
 * Description of the Class
 *
 * @author empty
 */
public class DefaultLayout implements Layout {

    /*
     * ============= layout code ===================
     */
    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content PARAM
     * @return Returns
     */

    public Box layout(Context c, Content content) {
        //TODO: temporary hack
        Box box = createBox(c, content.getElement());
        return box;//layoutChildren( c, box );
    }


    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param node PARAM
     * @return Returns
     */

    public Box createBox(Context c, Node node) {
        Box box = new Box();
        box.setNode(node);
        return box;
    }


    public Renderer getRenderer() {
        return new DefaultRenderer();
    }


    public void restyle(Context ctx, Box box) {
        box.color = ctx.css.getStyle(box.getRealElement()).getColor();
        box.setBorderColor(ctx.css.getStyle(box.getRealElement()).getBorderColor());
        box.border_style = ctx.css.getStyle(box.getRealElement()).getStringProperty("border-top-style");
        box.background_color = ctx.css.getStyle(box.getRealElement()).getBackgroundColor();
        restyleChildren(ctx,box);
    }
    
    private void restyleChildren(Context ctx, Box box) {
        for(int i=0; i<box.getChildCount(); i++) {
            Box child = box.getChild(i);
            if(child.hasNode()) {
                Layout lt = ctx.getLayout(child.getRealElement());
                if (lt instanceof InlineLayout) {
                    //u.p("restyling: " + child);
                    ((InlineLayout) lt).restyle(ctx, child);
                }
            }
            restyleChildren(ctx,child);
        }
    }
    
    
    /* prepare box and it's support code. 
    
    MORE OF THE CSS SHOULD GO HERE
    This is the best place to look at pre-caching the common css properties,
    especially once we start store larger objects like Border instead of individual
    properties.
    
    - JMM: 11/18/04
    
    */
    /**
     * Pre-load the most common css properties into the box. Eventually
     * we will need to depend on this code being here.
     *
     * @param box PARAM
     * @param c   PARAM
     */
    public void prepareBox(Context c, Box box) {
        getBorder(c, box);
        getPadding(c, box);
        getMargin(c, box);
        getBackgroundColor(c, box);
    }

    /**
     * Gets the listItem attribute of the BoxLayout object
     *
     * @param c   PARAM
     * @param box PARAM
     * @return The listItem value
     */

    // === caching accessors =========
    /**
     * Gets the padding attribute of the BoxLayout object
     *
     * @param c   PARAM
     * @param box PARAM
     * @return The padding value
     */
    public static Border getPadding(Context c, Box box) {
        if (LayoutUtil.isBlockOrInlineElementBox(c, box)) {
            if (box.padding == null) {
                box.padding = c.css.getStyle(box.getRealElement()).getPaddingWidth();
            }
        }
        return box.padding;
    }


    /**
     * Gets the margin attribute of the BoxLayout object
     *
     * @param c   PARAM
     * @param box PARAM
     * @return The margin value
     */
    public static Border getMargin(Context c, Box box) {
        if (LayoutUtil.isBlockOrInlineElementBox(c, box)) {
            if (box.margin == null) {
                box.margin = c.css.getStyle(box.getRealElement()).getMarginWidth();
            }
        }
        return box.margin;
    }

    public static Border getBorder(Context c, Box block) {
        Border border = LayoutUtil.getBorder(c, block);
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
        if (LayoutUtil.isBlockOrInlineElementBox(c, box)) {
            if (box.background_color == null) {
                if (c.css.getStyle(box.getRealElement()).isIdentifier(CSSName.BACKGROUND_COLOR)) {
                    String value = c.css.getStyle(box.getRealElement()).getStringProperty("background-color");
                    //u.p("got : " + obj);
                    if (value.equals("transparent")) {
                        box.background_color = new Color(0, 0, 0, 0);
                        return box.background_color;
                    }
                }
                box.background_color = c.css.getStyle(box.getRealElement()).getBackgroundColor();
            }
        }
        return box.background_color;
    }


}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.28  2004/12/09 18:00:04  joshy
 * fixed hover bugs
 * fixed li's not being blocks bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.27  2004/12/09 00:11:51  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.26  2004/12/05 00:48:57  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.25  2004/11/18 18:49:49  joshy
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
 * Revision 1.23  2004/11/18 02:51:15  joshy
 * moved more code out of the box into custom classes
 * added more preload logic to the default layout's preparebox method
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.22  2004/11/18 02:37:25  joshy
 * moved most of default layout into layout util or box layout
 *
 * start spliting parts of box layout into the block subpackage
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.21  2004/11/15 15:20:38  joshy
 * fixes for absolute layout
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
 * Revision 1.19  2004/11/12 18:51:00  joshy
 * fixed repainting issue for background-attachment: fixed
 * added static util methods and get minimum size to graphics 2d renderer
 * added test for graphics 2d renderer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.18  2004/11/12 17:05:24  joshy
 * support for fixed positioning
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.17  2004/11/12 02:42:19  joshy
 * context cleanup
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/11/12 00:20:29  tobega
 * Set up the HoverListener to work properly. Colors are changing!
 *
 * Revision 1.15  2004/11/09 15:53:48  joshy
 * initial support for hover (currently disabled)
 * moved justification code into it's own class in a new subpackage for inline
 * layout (because it's so blooming complicated)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/11/08 20:50:58  joshy
 * improved float support
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
 * Revision 1.12  2004/11/05 18:45:14  joshy
 * support for floated blocks (not just inline blocks)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.11  2004/11/05 16:39:34  joshy
 * more float support
 * added border bug test
 * -j
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/04 15:35:45  joshy
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
 * Revision 1.7  2004/11/02 20:44:55  joshy
 * put in some prep work for float support
 * removed some dead debugging code
 * moved isBlock code to LayoutFactory
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/10/28 02:13:41  joshy
 * finished moving the painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/10/28 01:34:23  joshy
 * moved more painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/27 14:03:37  joshy
 * added initial viewport repainting support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/27 13:17:00  joshy
 * beginning to split out rendering code
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.2  2004/10/23 13:46:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

