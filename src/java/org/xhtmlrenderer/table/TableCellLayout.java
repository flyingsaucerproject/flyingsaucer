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

package org.xhtmlrenderer.table;

import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.InlineLayout;
import org.xhtmlrenderer.render.Box;



/**
 * a table cell is a normal inline layout box, except that when it does the
 * actual painting it uses the height of the bounds instead of it's intrinsic
 * height. (hopefully this will change to be less clunky and more explict when I
 * redesign it all to use a separate Box pass.
 *
 * @author   empty
 */

public class TableCellLayout extends InlineLayout {





}

/*
   $Id$
   $Log$
   Revision 1.4  2004/10/28 02:13:43  joshy
   finished moving the painting code into the renderers

   Issue number:
   Obtained from:
   Submitted by:
   Reviewed by:

   Revision 1.3  2004/10/23 13:59:18  pdoubleya
   Re-formatted using JavaStyle tool.
   Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
   Added CVS log comments at bottom.

  */

