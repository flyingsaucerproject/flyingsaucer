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
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.Box;

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
        Box box = createBox(c, content);
        return box;//layoutChildren( c, box );
    }


    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content
     * @return Returns
     */

    public Box createBox(Context c, Content content) {
        Box box = new Box();
        box.content = content;
        return box;
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
        //getBorder(c, box);
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
        Border padding = c.getCurrentStyle().getPaddingWidth();
        /*if (LayoutUtil.isBlockOrInlineElementBox(box)) {
            if (box.padding == null) {
                box.padding = c.getCurrentStyle().getPaddingWidth();
            }
        }*/
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
        /*if (LayoutUtil.isBlockOrInlineElementBox(box)) {
            if (box.margin == null) {
                box.margin = c.getCurrentStyle().getMarginWidth();
            }
        }*/
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
        //if (LayoutUtil.isBlockOrInlineElementBox(box)) {
        //if (box.background_color == null) {
        CalculatedStyle style = c.getCurrentStyle();
        if (style.isIdentifier(CSSName.BACKGROUND_COLOR)) {
            String value = style.getStringProperty("background-color");
            if (value.equals("transparent")) {
                //box.background_color = new Color(0, 0, 0, 0);
                //return box.background_color;
                return bgc;
            }
        }
        //box.background_color = style.getBackgroundColor();
        bgc = style.getBackgroundColor();
        //           }
        //       }
        //return box.background_color;
        return bgc;
    }


}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.41  2005/01/01 23:38:38  tobega
 * Cleaned out old rendering code
 *
 * Revision 1.40  2004/12/29 10:39:32  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.39  2004/12/28 01:48:23  tobega
 * More cleaning. Magically, the financial report demo is starting to look reasonable, without any effort being put on it.
 *
 * Revision 1.38  2004/12/27 09:40:47  tobega
 * Moved more styling to render stage. Now inlines have backgrounds and borders again.
 *
 * Revision 1.37  2004/12/27 07:43:31  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.36  2004/12/14 02:28:48  joshy
 * removed some comments
 * some bugs with the backgrounds still
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.35  2004/12/13 15:15:57  joshy
 * fixed bug where inlines would pick up parent styles when they aren't supposed to
 * fixed extra Xx's in printed text
 * added conf boolean to turn on box outlines
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.34  2004/12/13 02:12:53  tobega
 * Borders are working again
 *
 * Revision 1.33  2004/12/12 03:32:58  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.32  2004/12/11 23:36:48  tobega
 * Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.31  2004/12/11 18:18:11  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
 * Revision 1.30  2004/12/10 06:51:02  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.29  2004/12/09 21:18:52  tobega
 * precaution: code still works
 *
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

