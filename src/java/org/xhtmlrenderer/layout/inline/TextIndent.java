/*
 * TextIndent.java
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
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
 *
 */
package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.render.LineBox;


/**
 * Description of the Class
 *
 * @author   Torbjörn Gannholm
 */
public class TextIndent {
    /**
     * Description of the Method
     *
     * @param style       PARAM
     * @param width       PARAM
     * @param first_line  PARAM
     * @return            Returns
     */
    public static int doTextIndent( CalculatedStyle style, int width, LineBox first_line ) {
        if ( style.hasProperty( CSSName.TEXT_INDENT ) ) {
            float indent = style.getFloatPropertyProportionalWidth( CSSName.TEXT_INDENT, width );
            width = width - (int)indent;
            first_line.x = first_line.x + (int)indent;
        }
        return width;
    }
}

