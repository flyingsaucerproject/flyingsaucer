/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjšrn Gannholm
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

import java.awt.*;
import java.util.List;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.AnonymousBlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.util.Uu;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class AnonymousBoxing {

    /** Constructor for the AnonymousBoxLayout object  */
    private AnonymousBoxing() { }


    /**
     * Description of the Method
     *
     * @param c        PARAM
     * @param content  PARAM
     * @return         Returns
     */
    public static Box layout( Context c, Content content ) {
        Box block = new AnonymousBlockBox( content );
        // copy the extents
        Rectangle oe = c.getExtents();
        c.setExtents( new Rectangle( oe ) );

        block.x = c.getExtents().x;
        block.y = c.getExtents().y;

        // save height incase fixed height
        int original_height = block.height;

        // do children's layout
        boolean old_sub = c.isSubBlock();
        c.setSubBlock( false );
        List contentList = content.getChildContent( c );
        if ( contentList != null && contentList.size() != 0 ) {
            InlineBoxing.layoutContent( c, block, content.getChildContent( c ) );//when this is really an anonymous, InlineLayout.layoutChildren is called
        }
        c.setSubBlock( old_sub );

        // restore height incase fixed height
        if ( block.auto_height == false ) {
            Uu.p( "restoring original height" );
            block.height = original_height;
        }

        //restore the extents
        c.setExtents( oe );

        return block;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2005/01/29 20:24:27  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.1  2005/01/02 09:32:40  tobega
 * Now using mostly static methods for layout
 *
 * Revision 1.15  2004/12/29 15:06:41  tobega
 * Referencing Context instead of SharedContext where it was wrongly set before.
 *
 * Revision 1.14  2004/12/29 10:39:32  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.13  2004/12/28 01:48:23  tobega
 * More cleaning. Magically, the financial report demo is starting to look reasonable, without any effort being put on it.
 *
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

