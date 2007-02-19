/*
 * {{{ header & license
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
package org.xhtmlrenderer.css.parser;

import java.awt.Color;

public class FSRGBColor {
    private int _red;
    private int _green;
    private int _blue;
    
    public FSRGBColor(int red, int green, int blue) {
        if (red < 0 || red > 255) {
            throw new IllegalArgumentException();
        }
        if (green < 0 || green > 255) {
            throw new IllegalArgumentException();
        }
        if (blue < 0 || blue > 255) {
            throw new IllegalArgumentException();
        }        
        _red = red;
        _green = green;
        _blue = blue;
    }

    public int getBlue() {
        return _blue;
    }

    public int getGreen() {
        return _green;
    }

    public int getRed() {
        return _red;
    }
    
    public String toString() {
        return '#' + toString(_red) + toString(_green) + toString(_blue);
    }
    
    private String toString(int color) {
        String result = Integer.toHexString(color);
        if (result.length() == 1) {
            return "0" + result;
        } else {
            return result;
        }
    }
    
    public Color toAWTColor() {
        return new Color(_red, _green, _blue);
    }
}
