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
package org.xhtmlrenderer.table;

import org.w3c.dom.Node;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class Cell {
    /** Description of the Field  */
    public Node node;

    /** Description of the Field  */
    public int col_span = 1;
    /** Description of the Field  */
    public int row_span = 1;

    /** Description of the Field  */
    public CellBox cb;

    /**
     * Gets the columnSpan attribute of the Cell object
     *
     * @return   The columnSpan value
     */
    int getColumnSpan() {
        return col_span;
    }
}

/*
 * $Id$
 * $Log$
 * Revision 1.4  2005/01/29 20:22:25  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.3  2005/01/05 01:10:16  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 * Revision 1.2  2004/10/23 13:59:17  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 */

