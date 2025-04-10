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

import com.google.errorprone.annotations.CheckReturnValue;

import java.util.Objects;

public class FSRGBColor implements FSColor {
    public static final FSRGBColor TRANSPARENT = new FSRGBColor(0, 0, 0);
    public static final FSRGBColor RED = new FSRGBColor(255, 0, 0);
    public static final FSRGBColor GREEN = new FSRGBColor(0, 255, 0);
    public static final FSRGBColor BLUE = new FSRGBColor(0, 0, 255);

    private final int _red;
    private final int _green;
    private final int _blue;
    private float _alpha;

    public FSRGBColor(int red, int green, int blue) {
        this(red, green, blue, 1.0f);
    }

    public FSRGBColor(int red, int green, int blue, float alpha) {
        if (alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException();
        }
        _red = validateColor("Red", red);
        _green = validateColor("Green", green);
        _blue = validateColor("Blue", blue);
        _alpha = alpha;
    }

    private int validateColor(String name, int color) {
        if (color < 0 || color > 255) {
            throw new IllegalArgumentException(String.format("%s %s is out of range [0, 255]", name, color));
        }
        return color;
    }

    public FSRGBColor(int color) {
        this(((color & 0xff0000) >> 16),((color & 0x00ff00) >> 8), color & 0xff);
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

    public float getAlpha() {
    	return _alpha;
    }


    @Override
    public String toString() {
    	if (_alpha != 1) {
    		return "rgba("+_red+","+_green+","+_blue+","+_alpha+")";
    	} else {
    		return '#' + toString(_red) + toString(_green) + toString(_blue);
    	}
    }

    private String toString(int color) {
        String result = Integer.toHexString(color);
        if (result.length() == 1) {
            return "0" + result;
        } else {
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FSRGBColor that)) return false;

        return _blue == that._blue && _green == that._green && _red == that._red && _alpha == that._alpha;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_red, _green, _blue);
    }

    @CheckReturnValue
    @Override
    public FSColor lightenColor() {
        HSBColor hsb = toHSB();
        float sLighter = 0.35f * hsb.brightness() * hsb.saturation();
        float bLighter = 0.6999f + 0.3f * hsb.brightness();
        return new HSBColor(hsb.hue(), sLighter, bLighter).toRGB();
    }

    @CheckReturnValue
    @Override
    public FSColor darkenColor() {
        HSBColor hsb = toHSB();
        float hBase = hsb.hue();
        float sBase = hsb.saturation();
        float bBase = hsb.brightness();
        float bDarker = 0.56f * bBase;

        return new HSBColor(hBase, sBase, bDarker).toRGB();
    }

    private HSBColor toHSB() {
        return RGBtoHSB(getRed(), getGreen(), getBlue());
    }

    // Taken from java.awt.Color to avoid dependency on it
    private static HSBColor RGBtoHSB(int r, int g, int b) {
        float hue, saturation, brightness;
        int cmax = Math.max(r, g);
        if (b > cmax)
            cmax = b;
        int cmin = Math.min(r, g);
        if (b < cmin)
            cmin = b;

        brightness = ((float) cmax) / 255.0f;
        if (cmax != 0)
            saturation = ((float) (cmax - cmin)) / ((float) cmax);
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }

        return new HSBColor(hue, saturation, brightness);
    }
}
