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
package org.xhtmlrenderer.render;

import org.xhtmlrenderer.layout.Context;


/**
 * Description of the Interface
 *
 * @author empty
 */
public interface Renderer {
    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paint(Context c, Box box);

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintBackground(Context c, Box box);

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintComponent(Context c, Box box);

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintBorder(Context c, Box box);

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintChildren(Context c, Box box);

    /**
     * Do the type of restyling allowed from dynamic events
     *
     * @param c
     * @param box
     */
    public void restyle(Context c, Box box);

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2004/12/12 03:33:01  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.2  2004/10/23 13:50:27  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

