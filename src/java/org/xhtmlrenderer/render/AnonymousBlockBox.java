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

import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.layout.content.Content;

public class AnonymousBlockBox extends BlockBox {
    public AnonymousBlockBox(Content content) {
        super();
        this.element = content.getElement();

    }

    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append("AnonymousBlockBox:");

        sb.append(super.toString());

        return sb.toString();
    }

    public int getContentWidth() {
        return getContainingBlock().getContentWidth();
    }
    
    public Box find(CssContext cssCtx, int absX, int absY) {
        Box result = super.find(cssCtx, absX, absY);
        if (result == this) {
            return getParent();
        } else {
            return result;
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.16  2006/02/22 02:20:18  peterbrant
 * Links and hover work again
 *
 * Revision 1.15  2005/11/25 16:57:19  peterbrant
 * Initial commit of inline content refactoring
 *
 * Revision 1.14  2005/11/12 21:55:27  tobega
 * Inline enhancements: block box text decorations, correct line-height when it is a number, better first-letter handling
 *
 * Revision 1.13  2005/11/05 03:29:59  peterbrant
 * Start work on painting order and improved positioning implementation
 *
 * Revision 1.12  2005/01/29 20:24:24  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.11  2005/01/07 00:29:29  tobega
 * Removed Content reference from Box (mainly to reduce memory footprint). In the process stumbled over and cleaned up some messy stuff.
 *
 * Revision 1.10  2005/01/05 01:10:15  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.9  2004/12/12 03:32:59  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.8  2004/12/11 23:36:49  tobega
 * Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.7  2004/12/10 06:51:04  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.6  2004/12/05 19:42:44  tobega
 * Made recursion in InlineUtil easier to understand
 *
 * Revision 1.5  2004/12/05 18:11:38  tobega
 * Now uses style cache for pseudo-element styles. Also started preparing to replace inline node handling with inline content handling.
 *
 * Revision 1.4  2004/12/05 00:48:58  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.3  2004/10/23 13:50:26  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

