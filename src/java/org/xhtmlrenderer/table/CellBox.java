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

import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.util.Uu;


/**
 * Description of the Class
 *
 * @author empty
 */
public class CellBox extends BlockBox {

    /**
     * Description of the Field
     */
    public Box sub_box;

    /**
     * Description of the Field
     */
    public RowBox rb;

    /**
     * Description of the Field
     */
    private boolean virtual = false;

    /**
     * Description of the Field
     */
    private CellBox real_box = null;

    /**
     * Constructor for the CellBox object
     *
     * @param x      PARAM
     * @param y      PARAM
     * @param width  PARAM
     * @param height PARAM
     */
    public CellBox(int x, int y, int width, int height) {

        super(x, y, width, height);

    }


    /**
     * Gets the real attribute of the CellBox object
     *
     * @return The real value
     */
    public boolean isReal() {

        return !virtual;
    }

    /**
     * Gets the real attribute of the CellBox object
     *
     * @return The real value
     */
    public CellBox getReal() {

        return real_box;
    }


    /**
     * Description of the Method
     *
     * @param real PARAM
     * @return Returns
     */
    public static CellBox createVirtual(CellBox real) {

        if (real == null) {

            Uu.p("WARNING: real is null!!!");

        }

        CellBox cb = new CellBox(0, 0, 0, 0);

        cb.virtual = true;

        cb.real_box = real;

        return cb;
    }

}

/*
   $Id$
   $Log$
   Revision 1.4  2004/12/12 03:33:03  tobega
   Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it

   Revision 1.3  2004/10/23 13:59:17  pdoubleya
   Re-formatted using JavaStyle tool.
   Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
   Added CVS log comments at bottom.

  */

