/*
 * {{{ header & license
 * BorderColor.java
 * Copyright (c) 2004, 2005 Patrick Wright
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
package org.xhtmlrenderer.css.value;

import java.awt.Color;


/**
 * Adapted from org.xhtmlrenderer.css.Border by Josh M.
 *
 * @author   Patrick Wright
 */
public class BorderColor {
    /** Color for top of the border. */
    public Color topColor;

    /** Color for bottom of the border. */
    public Color bottomColor;

    /** Color for left of the border. */
    public Color leftColor;

    /** Color for right of the border. */
    public Color rightColor;

    /** Constructor for the BorderColor object */
    public BorderColor() { }

    /**
     * Constructor for the BorderColor object
     *
     * @param color  PARAM
     */
    public BorderColor( Color color ) {
        this.topColor = color;
        this.bottomColor = color;
        this.rightColor = color;
        this.leftColor = color;
    }

    /**
     * Constructor for the BorderColor object
     *
     * @param top     PARAM
     * @param right   PARAM
     * @param bottom  PARAM
     * @param left    PARAM
     */
    public BorderColor( Color top, Color right, Color bottom, Color left ) {
        this.topColor = top;
        this.bottomColor = bottom;
        this.rightColor = right;
        this.leftColor = left;
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public BorderColor brighter() {
        BorderColor bc = new BorderColor();
        bc.topColor = topColor.brighter();
        bc.bottomColor = bottomColor.brighter();
        bc.leftColor = leftColor.brighter();
        bc.rightColor = rightColor.brighter();
        return bc;
    }

    /**
     * Description of the Method
     *
     * @return   Returns
     */
    public BorderColor darker() {
        BorderColor bc = new BorderColor();
        bc.topColor = topColor.darker();
        bc.bottomColor = bottomColor.darker();
        bc.leftColor = leftColor.darker();
        bc.rightColor = rightColor.darker();
        return bc;
    }

    /**
     * ...
     *
     * @return   Returns
     */
    public String toString() {

        return "BorderColor:\n" +
                "    topColor = " + topColor +
                "    rightColor = " + rightColor +
                "    bottomColor = " + bottomColor +
                "    leftColor = " + leftColor;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2005/01/29 20:24:26  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.4  2004/11/09 15:53:48  joshy
 * initial support for hover (currently disabled)
 * moved justification code into it's own class in a new subpackage for inline
 * layout (because it's so blooming complicated)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/11/07 16:23:17  joshy
 * added support for lighten and darken to bordercolor
 * added support for different colored sides
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.2  2004/10/23 13:32:01  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

