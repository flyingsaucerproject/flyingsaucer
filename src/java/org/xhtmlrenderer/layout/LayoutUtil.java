/*
 * {{{ header & license
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
 * }}}
 */
package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.render.Box;


/**
 * Description of the Class
 *
 * @author   Torbjörn Gannholm
 */
public class LayoutUtil {

    /**
     * Gets the display attribute of the LayoutUtil class
     *
     * @param style  PARAM
     * @return       The display value
     */
    public static IdentValue getDisplay( CascadedStyle style ) {
        // Uu.p("checking: " + child);
        IdentValue display = style.getIdent( CSSName.DISPLAY );
        return ( isFloated( style ) ? IdentValue.BLOCK : display );
    }


    /**
     * Gets the outsideNormalFlow attribute of the LayoutUtil class
     *
     * @param box  PARAM
     * @return     The outsideNormalFlow value
     */
    public static boolean isOutsideNormalFlow( Box box ) {
        if ( box.fixed ) {
            return true;
        }
        if ( box.absolute ) {
            //Uu.p("box is abs: " + box);
            return true;
        }
        if ( box.floated ) {
            return true;
        }
        return false;
    }

    /**
     * Gets the fixed attribute of the DefaultLayout object
     *
     * @param style
     * @return       The fixed value
     */
    public static boolean isFixed( CascadedStyle style ) {
        IdentValue position = getPosition( style );
        return position != null && position == IdentValue.FIXED;
    }


    /**
     * Gets the position attribute of the DefaultLayout class
     *
     * @param style
     * @return       The position value
     */
    public static IdentValue getPosition( CascadedStyle style ) {
        if ( style == null ) {
            return null;
        }//TODO: this should not be necessary?
        IdentValue position = style.getIdent( CSSName.POSITION );
        return position;
    }


    /**
     * Gets the floated attribute of the DefaultLayout class
     *
     * @param style
     * @return       The floated value
     */
    public static boolean isFloated( CascadedStyle style ) {
        if ( style == null ) {
            return false;
        }//TODO: this should be unnecessary?
        IdentValue floatVal = style.getIdent( CSSName.FLOAT );
        return ( floatVal == IdentValue.LEFT || floatVal == IdentValue.RIGHT );
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.37  2005/02/03 23:09:17  pdoubleya
 * .
 *
 *
 */
