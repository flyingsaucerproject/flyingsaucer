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

import java.util.*;
import org.w3c.dom.Node;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class Row {
    /** Description of the Field */
    public Node node;
    /** Description of the Field */
    List cells = new ArrayList();

    /**
     * Adds a feature to the Cell attribute of the Row object
     *
     * @param cell  The feature to be added to the Cell attribute
     */
    public void addCell( Cell cell ) {
        cells.add( cell );
    }

    /**
     * Gets the cellIterator attribute of the Row object
     *
     * @return   The cellIterator value
     */
    public Iterator getCellIterator() {
        return cells.iterator();
    }
}

/*
   $Id$
   $Log$
   Revision 1.2  2004/10/23 13:59:17  pdoubleya
   Re-formatted using JavaStyle tool.
   Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
   Added CVS log comments at bottom.

  */

