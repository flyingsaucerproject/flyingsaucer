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

import org.w3c.dom.css.RGBColor;

import java.awt.*;

import static org.w3c.dom.css.CSSPrimitiveValue.CSS_NUMBER;


/**
 * Utility methods for data conversion
 */
public class ConversionUtil {
    /**
     * Copied from Josh M.'s CSSAccessor class
     */
    public static Color rgbToColor(RGBColor color) {
        return new java.awt.Color( color.getRed().getFloatValue( CSS_NUMBER ) / 255.0f,
                color.getGreen().getFloatValue( CSS_NUMBER ) / 255.0f,
                color.getBlue().getFloatValue( CSS_NUMBER ) / 255.0f);
    }
}
