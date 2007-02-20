/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Patrick Wright
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
package org.xhtmlrenderer.css.parser.property;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.parser.PropertyValue;

public class Conversions {
    private static final Map COLORS = new HashMap();
    private static final Map NUMERIC_FONT_WEIGHTS = new HashMap();
    private static final Map BORDER_WIDTHS = new HashMap();
    
    static {
        COLORS.put("maroon", new FSRGBColor(0x80, 0x00, 0x00));
        COLORS.put("red", new FSRGBColor(0xff, 0x00, 0x00));
        COLORS.put("orange", new FSRGBColor(0xff, 0xa5, 0x00));
        COLORS.put("yellow", new FSRGBColor(0xff, 0xff, 0x00));
        COLORS.put("olive", new FSRGBColor(0x80, 0x80, 0x00));
        COLORS.put("purple", new FSRGBColor(0x80, 0x00, 0x80));
        COLORS.put("fuchsia", new FSRGBColor(0xff, 0x00, 0xff));
        COLORS.put("white", new FSRGBColor(0xff, 0xff, 0xff));
        COLORS.put("lime", new FSRGBColor(0x00, 0xff, 0x00));
        COLORS.put("green", new FSRGBColor(0x00, 0x80, 0x00));
        COLORS.put("navy", new FSRGBColor(0x00, 0x00, 0x80));
        COLORS.put("blue", new FSRGBColor(0x00, 0x00, 0xff));
        COLORS.put("aqua", new FSRGBColor(0x00, 0xff, 0xff));
        COLORS.put("teal", new FSRGBColor(0x00, 0x80, 0x80));
        COLORS.put("black", new FSRGBColor(0x00, 0x00, 0x00));
        COLORS.put("silver", new FSRGBColor(0xc0, 0xc0, 0xc0));
        COLORS.put("gray", new FSRGBColor(0x80, 0x80, 0x80));
    }
    
    static {
        NUMERIC_FONT_WEIGHTS.put(new Float(100f), IdentValue.FONT_WEIGHT_100);
        NUMERIC_FONT_WEIGHTS.put(new Float(200f), IdentValue.FONT_WEIGHT_200);
        NUMERIC_FONT_WEIGHTS.put(new Float(300f), IdentValue.FONT_WEIGHT_300);
        NUMERIC_FONT_WEIGHTS.put(new Float(400f), IdentValue.FONT_WEIGHT_400);
        NUMERIC_FONT_WEIGHTS.put(new Float(500f), IdentValue.FONT_WEIGHT_500);
        NUMERIC_FONT_WEIGHTS.put(new Float(600f), IdentValue.FONT_WEIGHT_600);
        NUMERIC_FONT_WEIGHTS.put(new Float(700f), IdentValue.FONT_WEIGHT_700);
        NUMERIC_FONT_WEIGHTS.put(new Float(800f), IdentValue.FONT_WEIGHT_800);
        NUMERIC_FONT_WEIGHTS.put(new Float(900f), IdentValue.FONT_WEIGHT_900);
    }
    
    static {
        BORDER_WIDTHS.put("thin", new PropertyValue(CSSPrimitiveValue.CSS_PX, 1.0f, "1px"));
        BORDER_WIDTHS.put("medium", new PropertyValue(CSSPrimitiveValue.CSS_PX, 2.0f, "2px"));
        BORDER_WIDTHS.put("thick", new PropertyValue(CSSPrimitiveValue.CSS_PX, 3.0f, "3px"));
    }
    
    public static FSRGBColor getColor(String ident) {
        return (FSRGBColor)COLORS.get(ident);
    }
    
    public static IdentValue getNumericFontWeight(float weight) {
        return (IdentValue)NUMERIC_FONT_WEIGHTS.get(new Float(weight));
    }
    
    public static PropertyValue getBorderWidth(String ident) {
        return (PropertyValue)BORDER_WIDTHS.get(ident);
    }
    
}
