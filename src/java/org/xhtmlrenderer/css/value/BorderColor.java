/*
 * {{{ header & license
 * BorderColor.java
 * Copyright (c) 2004 Patrick Wright
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
 * @author    Patrick Wright
 *
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


