/*
 * {{{ header & license
 * Copyright (c) 2005 Patrick Wright
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.css.style.derived;

import java.awt.Color;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.style.DerivedValue;

public class ColorValue extends DerivedValue {
    public static final Color COLOR_TRANSPARENT = new Color(0, 0, 0, 0);

    private Color _derivedColor;

    public ColorValue(CSSName name, PropertyValue value) {
        super(name, value.getPrimitiveType(), value.getCssText(), value.getCssText());
        
        _derivedColor = value.getFSRGBColor().toAWTColor();
    }

    /**
     * Returns the value as a Color, if it is a color.
     *
     * @return The rGBColorValue value
     */
    public Color asColor() {
        return _derivedColor;
    }
    
    public static Color lightenColor(Color color) {
        if (color == null) {
            return null;
        }
        
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float hBase = hsb[0];
        float sBase = hsb[1];
        float bBase = hsb[2];
        
        float hLighter = hBase;
        float sLighter = 0.35f*bBase*sBase;
        float bLighter = 0.6999f + 0.3f*bBase;
        
        return Color.getHSBColor(hLighter, sLighter, bLighter);
    }
    
    public static Color darkenColor(Color color) {
        if (color == null) {
            return null;
        }
        
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float hBase = hsb[0];
        float sBase = hsb[1];
        float bBase = hsb[2];
        
        float hDarker = hBase;
        float sDarker = sBase;
        float bDarker = 0.56f*bBase;
        
        return Color.getHSBColor(hDarker, sDarker, bDarker);
    }    
}
