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
package org.xhtmlrenderer.css;

import java.awt.Color;


/**
 * Description of the Class
 *
 * @author   empty
 */
class CSSUtil {

    /**
     * Gets the color attribute of the CSSUtil class
     *
     * @param val  PARAM
     * @return     The color value
     */
    public static Color getColor( String val ) {

        return getColor( val, Color.black );
    }

    /**
     * Gets the color attribute of the CSSUtil class
     *
     * @param val            PARAM
     * @param default_color  PARAM
     * @return               The color value
     */
    public static Color getColor( String val, Color default_color ) {

        if ( val == null ) {

            return default_color;
        }

        if ( val.equals( "" ) ) {

            return default_color;
        }

        return Color.decode( val );
    }

    /**
     * Gets the width attribute of the CSSUtil class
     *
     * @param val     PARAM
     * @param parent  PARAM
     * @return        The width value
     */
    public static int getWidth( String val, int parent ) {

        if ( val == null ) {
            return 0;
        }

        if ( val.equals( "" ) ) {
            return 0;
        }

        if ( val.endsWith( "px" ) ) {

            return Integer.parseInt( val.substring( 0, val.length() - 2 ) );
        }

        if ( val.endsWith( "%" ) ) {

            int v2 = Integer.parseInt( val.substring( 0, val.length() - 1 ) );

            return (int)( ( ( (float)v2 ) / 100 ) * parent );
        }

        //u.p("returning" + Integer.parseInt(val));

        return Integer.parseInt( val );
    }

    /**
     * Gets the width attribute of the CSSUtil class
     *
     * @param val  PARAM
     * @return     The width value
     */
    public static int getWidth( String val ) {

        if ( val == null ) {
            return 0;
        }

        if ( val.equals( "" ) ) {
            return 0;
        }

        if ( val.endsWith( "px" ) ) {

            return Integer.parseInt( val.substring( 0, val.length() - 2 ) );
        }

        //u.p("returning" + Integer.parseInt(val));

        return Integer.parseInt( val );
    }

    /**
     * Gets the size attribute of the CSSUtil class
     *
     * @param val           PARAM
     * @param default_size  PARAM
     * @return              The size value
     */
    public static int getSize( String val, int default_size ) {

        if ( val == null ) {
            return default_size;
        }

        if ( val.equals( "" ) ) {
            return default_size;
        }

        if ( val.endsWith( "pt" ) ) {

            return Integer.parseInt( val.substring( 0, val.length() - 2 ) );
        }

        //u.p("returning" + Integer.parseInt(val));

        return Integer.parseInt( val );
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2004/10/23 13:03:45  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

