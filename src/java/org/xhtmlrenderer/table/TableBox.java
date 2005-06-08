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

import org.w3c.dom.Element;
import org.xhtmlrenderer.render.BlockBox;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Description of the Class
 *
 * @author empty
 */
public class TableBox extends BlockBox {

    /**
     * Description of the Field
     */
    public List rows = new ArrayList();

    /**
     * Description of the Field
     */
    public Element elem;

    /**
     * Description of the Field
     */
    public Point spacing;
    public int[] columns;

    /**
     * Constructor for the TableBox object
     */
    public TableBox() {

        super();

    }

    /**
     * Constructor for the TableBox object
     *
     * @param x      PARAM
     * @param y      PARAM
     * @param width  PARAM
     * @param height PARAM
     */
    public TableBox(int x, int y, int width, int height) {

        super(x, y, width, height);

    }

}

/*
 * $Id$
 * $Log$
 * Revision 1.4  2005/06/08 19:48:55  tobega
 * Rock 'n roll! Report looks quite good!
 *
 * Revision 1.3  2005/01/29 20:18:42  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.2  2004/10/23 13:59:18  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 */

