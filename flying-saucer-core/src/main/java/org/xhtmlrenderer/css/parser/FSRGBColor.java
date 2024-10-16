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

    public FSRGBColor(int red, int green, int blue) {
        _red = validateColor("Red", red);
        _green = validateColor("Green", green);
        _blue = validateColor("Blue", blue);
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

    @Override
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FSRGBColor that)) return false;

        return _blue == that._blue && _green == that._green && _red == that._red;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_red, _green, _blue);
    }

    @CheckReturnValue
    @Override
    public FSColor lightenColor() {
        float[] hsb = RGBtoHSB(getRed(), getGreen(), getBlue(), null);
        float hBase = hsb[0];
        float sBase = hsb[1];
        float bBase = hsb[2];

        float sLighter = 0.35f*bBase*sBase;
        float bLighter = 0.6999f + 0.3f*bBase;

        int[] rgb = HSBtoRGB(hBase, sLighter, bLighter);
        return new FSRGBColor(rgb[0], rgb[1], rgb[2]);
    }

    @CheckReturnValue
    @Override
    public FSColor darkenColor() {
        float[] hsb = RGBtoHSB(getRed(), getGreen(), getBlue(), null);
        float hBase = hsb[0];
        float sBase = hsb[1];
        float bBase = hsb[2];
        float bDarker = 0.56f * bBase;

        int[] rgb = HSBtoRGB(hBase, sBase, bDarker);
        return new FSRGBColor(rgb[0], rgb[1], rgb[2]);
    }

    // Taken from java.awt.Color to avoid dependency on it
    private static float[] RGBtoHSB(int r, int g, int b, float @Nullable [] hsbvals) {
        float hue, saturation, brightness;
        if (hsbvals == null) {
            hsbvals = new float[3];
        }
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
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    // Taken from java.awt.Color to avoid dependency on it
    private static int[] HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                    break;
            }
        }
        return new int[] { r, g, b };
    }
}
