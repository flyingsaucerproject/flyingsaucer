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
package org.xhtmlrenderer.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import org.xhtmlrenderer.render.Box;
import java.awt.image.BufferedImage;
import java.awt.Image;

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

	public static Image cleanImage(Image img) {
		//System.out.println("cleaning up " + img);
		return img.getScaledInstance(img.getWidth(null),img.getHeight(null),Image.SCALE_FAST);
		/*

		BufferedImage buf = new BufferedImage(img.getWidth(null), 
			img.getHeight(null),
			BufferedImage.TYPE_INT_RGB);
		Graphics g = buf.getGraphics();
		g.drawImage(img,0,0,null);
		g.setColor(Color.green);
		g.drawLine(0,0,300,300);
		g.dispose();
		return buf;
		*/
	}

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2005/06/20 23:45:56  joshy
 * hack to fix the mangled background images on osx
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2005/01/29 20:21:08  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.2  2004/10/23 14:06:57  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

