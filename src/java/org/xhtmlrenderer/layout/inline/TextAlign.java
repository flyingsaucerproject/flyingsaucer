/*
 * TextAlign.java
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
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.render.LineBox;


/**
 * Description of the Class
 *
 * @author   Torbjörn Gannholm
 */
public class TextAlign {

    /**
     * Description of the Method
     *
     * @param c             PARAM
     * @param style         PARAM
     * @param line_to_save  PARAM
     * @param width         PARAM
     * @param x             PARAM
     * @param last          PARAM
     */
    public static void adjustTextAlignment( Context c, CalculatedStyle style, LineBox line_to_save, int width, int x, boolean last ) {
        IdentValue textAlign = c.getCurrentStyle().getIdent( CSSName.TEXT_ALIGN );
        if ( textAlign == IdentValue.RIGHT ) {
            line_to_save.x = x + width - line_to_save.width;
        }
        if ( textAlign == IdentValue.CENTER ) {
            line_to_save.x = x + ( width - line_to_save.width ) / 2;
        }
        if ( TextAlignJustify.isJustified( style ) ) {
            if ( !last ) {
                TextAlignJustify.justifyLine( c, line_to_save, width );
            }
        }
    }

}

