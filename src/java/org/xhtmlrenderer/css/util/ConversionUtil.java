/*
 * {{{ header & license
 * ConversionUtil.java
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
package org.xhtmlrenderer.css.util;

import java.awt.Color;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.RGBColor;


/**
 * Utility methods for data conversion.
 *
 * @author   empty
 */
public class ConversionUtil {
    /**
     * Copied from Josh M.'s CSSAccessor class
     *
     * @param rgbcol  PARAM
     * @return        Returns
     */
    public static Color rgbToColor( RGBColor rgbcol ) {
        return new java.awt.Color( rgbcol.getRed().getFloatValue( CSSPrimitiveValue.CSS_NUMBER ) / 255f,
                rgbcol.getGreen().getFloatValue( CSSPrimitiveValue.CSS_NUMBER ) / 255f,
                rgbcol.getBlue().getFloatValue( CSSPrimitiveValue.CSS_NUMBER ) / 255f );
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2005/01/29 20:22:23  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.2  2004/10/23 13:30:42  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

