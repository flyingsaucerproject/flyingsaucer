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
package org.xhtmlrenderer.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import org.xhtmlrenderer.render.Box;

/**
 * Description of the Class
 *
 * @author   empty
 */
public class GraphicsUtil {

    /**
     * Description of the Method
     *
     * @param g      PARAM
     * @param box    PARAM
     * @param color  PARAM
     */
    public static void drawBox( Graphics g, Box box, Color color ) {

        Color oc = g.getColor();

        g.setColor( color );

        //g.drawLine(-5,-5,5,5);

        //g.drawLine(-5,5,5,-5);

        g.drawRect( box.x, box.y, box.width, box.height );

        g.setColor( oc );

    }

    /**
     * Description of the Method
     *
     * @param g      PARAM
     * @param box    PARAM
     * @param color  PARAM
     */
    public static void draw( Graphics g, Rectangle box, Color color ) {

        Color oc = g.getColor();

        g.setColor( color );

        g.drawRect( box.x, box.y, box.width, box.height );

        g.setColor( oc );

    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2004/10/23 14:06:57  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

