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

import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;

/**
 * Description of the Class
 *
 * @author empty
 */
public class BodyLayout extends InlineLayout {
//InlineLayout does work better. Tobe 2004-12-07

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content PARAM
     * @return Returns
     */

    public Box layout(Context c, Content content) {
        //set the current style
        c.initializeStyles(new EmptyStyle());
        //Box bodybox = createBox(c, content);
        BlockBox block = (BlockBox) createBox(c, content);
        return layout(c, block);
        /*List children = content.getChildContent(c);
        //TODO: fix this properly
        Box childbox = (new InlineLayout()).layout(c, (Content) children.get(0));
        bodybox.addChild(childbox);
        // Uu.p("done laying it all out");
        // Uu.p("\n=====================\n\n\n");
        return bodybox;*/
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.16  2005/01/01 23:38:37  tobega
 * Cleaned out old rendering code
 *
 * Revision 1.15  2004/12/29 10:39:32  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.14  2004/12/13 01:29:40  tobega
 * Got the scrollbars back (by accident), and now we should be able to display DocumentFragments as well as Documents, if someone finds that useful.
 *
 * Revision 1.13  2004/12/12 05:51:48  tobega
 * Now things run. But there is a lot to do before it looks as nice as it did. At least we now have :before and :after content and handling of breaks by css.
 *
 * Revision 1.12  2004/12/12 03:32:57  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.11  2004/12/09 00:11:51  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.10  2004/12/08 00:42:34  tobega
 * More cleaning of use of Node, more preparation for Content-based inline generation. Also fixed 2 irritating bugs!
 *
 * Revision 1.9  2004/12/06 23:41:13  tobega
 * More cleaning of use of Node, more preparation for Content-based inline generation.
 *
 * Revision 1.8  2004/12/01 01:57:00  joshy
 * more updates for float support.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2004/11/27 15:46:38  joshy
 * lots of cleanup to make the code clearer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/12 02:42:18  joshy
 * context cleanup
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/03 23:54:33  joshy
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
 * Revision 1.4  2004/10/27 13:39:56  joshy
 * moved more rendering code out of the layouts
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

