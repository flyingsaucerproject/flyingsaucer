/*
 * {{{ header & license
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
package org.xhtmlrenderer.css.constants;

import org.xhtmlrenderer.util.XRRuntimeException;

import java.util.Map;
import java.util.TreeMap;


/**
 * An IdentValue represents a string that you can assign to a CSS property,
 * where the string is one of several enumerated values. For example,
 * "whitespace" can take the values "nowrap", "pre" and "normal". There is a
 * static instance for all idents in the CSS 2 spec, which you can retrieve
 * using the {@link #getByIdentString(String)} method. The instance doesn't have
 * any behavior: it's just a marker so that you can retrieve an ident from a
 * DerivedValue or CalculatedStyle, then compare to the instance here. For
 * example: <pre>
 * CalculatedStyle style = ...getstyle from somewhere
 * IdentValue whitespace = style.getIdent(CSSName.WHITESPACE);
 * if ( whitespace == IdentValue.NORMAL ) {
 *      // perform normal spacing
 * } else if ( whitespace == IdentValue.NOWRAP ) {
 *      // space with no wrapping
 * } else if ( whitespace == IdentValue.PRE ) {
 *      // preserve spacing
 * }
 * </pre> All static instances are instantiated automatically, and are
 * Singletons, so you can compare using a simple Object comparison using <code>==</code>
 * .
 *
 * @author Patrick Wright
 */
public class IdentValue {
    /**
     * Description of the Field
     */
    private String ident;
    /**
     * Description of the Field
     */
    public final static IdentValue ABSOLUTE = addValue("absolute");
    /**
     * Description of the Field
     */
    public final static IdentValue ARMENIAN = addValue("armenian");
    /**
     * Description of the Field
     */
    public final static IdentValue AUTO = addValue("auto");
    /**
     * Description of the Field
     */
    public final static IdentValue BASELINE = addValue("baseline");
    /**
     * Description of the Field
     */
    public final static IdentValue BLINK = addValue("blink");
    /**
     * Description of the Field
     */
    public final static IdentValue BLOCK = addValue("block");
    /**
     * Description of the Field
     */
    public final static IdentValue BOLD = addValue("bold");
    /**
     * Description of the Field
     */
    public final static IdentValue BOTH = addValue("both");
    /**
     * Description of the Field
     */
    public final static IdentValue BOLDER = addValue("bolder");
    /**
     * Description of the Field
     */
    public final static IdentValue BOTTOM = addValue("bottom");
    /**
     * Description of the Field
     */
    public final static IdentValue CAPITALIZE = addValue("capitalize");
    /**
     * Description of the Field
     */
    public final static IdentValue CENTER = addValue("center");
    /**
     * Description of the Field
     */
    public final static IdentValue CIRCLE = addValue("circle");
    /**
     * Description of the Field
     */
    public final static IdentValue CJK_IDEOGRAPHIC = addValue("cjk-ideographic");
    /**
     * Description of the Field
     */
    public final static IdentValue COLLAPSE = addValue("collapse");
    /**
     * Description of the Field
     */
    public final static IdentValue COMPACT = addValue("compact");
    /**
     * Description of the Field
     */
    public final static IdentValue DASHED = addValue("dashed");
    /**
     * Description of the Field
     */
    public final static IdentValue DECIMAL = addValue("decimal");
    /**
     * Description of the Field
     */
    public final static IdentValue DECIMAL_LEADING_ZERO = addValue("decimal-leading-zero");
    /**
     * Description of the Field
     */
    public final static IdentValue DISC = addValue("disc");
    /**
     * Description of the Field
     */
    public final static IdentValue DOTTED = addValue("dotted");
    /**
     * Description of the Field
     */
    public final static IdentValue DOUBLE = addValue("double");
    /**
     * Description of the Field
     */
    public final static IdentValue FIXED = addValue("fixed");
    /**
     * Description of the Field
     */
    public final static IdentValue FONT_WEIGHT_100 = addValue("100");
    /**
     * Description of the Field
     */
    public final static IdentValue FONT_WEIGHT_200 = addValue("200");
    /**
     * Description of the Field
     */
    public final static IdentValue FONT_WEIGHT_300 = addValue("300");
    /**
     * Description of the Field
     */
    public final static IdentValue FONT_WEIGHT_400 = addValue("400");
    /**
     * Description of the Field
     */
    public final static IdentValue FONT_WEIGHT_500 = addValue("500");
    /**
     * Description of the Field
     */
    public final static IdentValue FONT_WEIGHT_600 = addValue("600");
    /**
     * Description of the Field
     */
    public final static IdentValue FONT_WEIGHT_700 = addValue("700");
    /**
     * Description of the Field
     */
    public final static IdentValue FONT_WEIGHT_800 = addValue("800");
    /**
     * Description of the Field
     */
    public final static IdentValue FONT_WEIGHT_900 = addValue("900");
    /**
     * Description of the Field
     */
    public final static IdentValue GEORGIAN = addValue("georgian");
    /**
     * Description of the Field
     */
    public final static IdentValue GROOVE = addValue("groove");
    /**
     * Description of the Field
     */
    public final static IdentValue HEBREW = addValue("hebrew");
    /**
     * Description of the Field
     */
    public final static IdentValue HIDDEN = addValue("hidden");
    /**
     * Description of the Field
     */
    public final static IdentValue HIRAGANA = addValue("hiragana");
    /**
     * Description of the Field
     */
    public final static IdentValue HIRAGANA_IROHA = addValue("hiragana-iroha");
    /**
     * Description of the Field
     */
    public final static IdentValue INLINE = addValue("inline");
    // HACK: inline-block is not a valid CSS value, but was being used in CSS/demos. should prob be inline or block (PWW 25-01-05)
    /**
     * Description of the Field
     */
    public final static IdentValue INLINE_BLOCK = addValue("inline-block");
    /**
     * Description of the Field
     */
    public final static IdentValue INLINE_TABLE = addValue("inline-table");
    /**
     * Description of the Field
     */
    public final static IdentValue INSET = addValue("inset");
    /**
     * Description of the Field
     */
    public final static IdentValue ITALIC = addValue("italic");
    /**
     * Description of the Field
     */
    public final static IdentValue JUSTIFY = addValue("justify");
    /**
     * Description of the Field
     */
    public final static IdentValue KATAKANA = addValue("katakana");
    /**
     * Description of the Field
     */
    public final static IdentValue KATAKANA_IROHA = addValue("katakana-iroha");
    /**
     * Description of the Field
     */
    public final static IdentValue LEFT = addValue("left");
    /**
     * Description of the Field
     */
    public final static IdentValue LIGHTER = addValue("lighter");
    /**
     * Description of the Field
     */
    public final static IdentValue LINE_THROUGH = addValue("line-through");
    /**
     * Description of the Field
     */
    public final static IdentValue LIST_ITEM = addValue("list-item");
    /**
     * Description of the Field
     */
    public final static IdentValue LOWERCASE = addValue("lowercase");
    /**
     * Description of the Field
     */
    public final static IdentValue LOWER_ALPHA = addValue("lower-alpha");
    /**
     * Description of the Field
     */
    public final static IdentValue LOWER_GREEK = addValue("lower-greek");
    /**
     * Description of the Field
     */
    public final static IdentValue LOWER_LATIN = addValue("lower-latin");
    /**
     * Description of the Field
     */
    public final static IdentValue LOWER_ROMAN = addValue("lower-roman");
    /**
     * Description of the Field
     */
    public final static IdentValue MARKER = addValue("marker");
    /**
     * Description of the Field
     */
    public final static IdentValue MIDDLE = addValue("middle");
    /**
     * Description of the Field
     */
    public final static IdentValue NONE = addValue("none");
    /**
     * Description of the Field
     */
    public final static IdentValue NORMAL = addValue("normal");
    /**
     * Description of the Field
     */
    public final static IdentValue NOWRAP = addValue("nowrap");
    /**
     * Description of the Field
     */
    public final static IdentValue NO_REPEAT = addValue("no-repeat");
    /**
     * Description of the Field
     */
    public final static IdentValue OBLIQUE = addValue("oblique");
    /**
     * Description of the Field
     */
    public final static IdentValue OUTSET = addValue("outset");
    /**
     * Description of the Field
     */
    public final static IdentValue OVERLINE = addValue("overline");
    /**
     * Description of the Field
     */
    public final static IdentValue PRE = addValue("pre");
    // HACK: pre-line/pre-wrap are not valid CSS values, but being used in CSS/demos. should prob be pre (PWW 25-01-05)
    /**
     * Description of the Field
     */
    public final static IdentValue PRE_LINE = addValue("pre-line");
    /**
     * Description of the Field
     */
    public final static IdentValue PRE_WRAP = addValue("pre-wrap");
    /**
     * Description of the Field
     */
    public final static IdentValue RELATIVE = addValue("relative");
    /**
     * Description of the Field
     */
    public final static IdentValue REPEAT = addValue("repeat");
    /**
     * Description of the Field
     */
    public final static IdentValue REPEAT_X = addValue("repeat-x");
    /**
     * Description of the Field
     */
    public final static IdentValue REPEAT_Y = addValue("repeat-y");
    /**
     * Description of the Field
     */
    public final static IdentValue RIDGE = addValue("ridge");
    /**
     * Description of the Field
     */
    public final static IdentValue RIGHT = addValue("right");
    /**
     * Description of the Field
     */
    public final static IdentValue RUN_IN = addValue("run-in");
    /**
     * Description of the Field
     */
    public final static IdentValue SCROLL = addValue("scroll");
    /**
     * Description of the Field
     */
    public final static IdentValue SEPARATE = addValue("separate");
    /**
     * Description of the Field
     */
    public final static IdentValue SMALL_CAPS = addValue("small-caps");
    /**
     * Description of the Field
     */
    public final static IdentValue SOLID = addValue("solid");
    /**
     * Description of the Field
     */
    public final static IdentValue SQUARE = addValue("square");
    /**
     * Description of the Field
     */
    public final static IdentValue STATIC = addValue("static");
    /**
     * Description of the Field
     */
    public final static IdentValue SUB = addValue("sub");
    /**
     * Description of the Field
     */
    public final static IdentValue SUPER = addValue("super");
    /**
     * Description of the Field
     */
    public final static IdentValue TABLE = addValue("table");
    /**
     * Description of the Field
     */
    public final static IdentValue TABLE_CAPTION = addValue("table-caption");
    /**
     * Description of the Field
     */
    public final static IdentValue TABLE_CELL = addValue("table-cell");
    /**
     * Description of the Field
     */
    public final static IdentValue TABLE_COLUMN = addValue("table-column");
    /**
     * Description of the Field
     */
    public final static IdentValue TABLE_COLUMN_GROUP = addValue("table-column-group");
    /**
     * Description of the Field
     */
    public final static IdentValue TABLE_FOOTER_GROUP = addValue("table-footer-group");
    /**
     * Description of the Field
     */
    public final static IdentValue TABLE_HEADER_GROUP = addValue("table-header-group");
    /**
     * Description of the Field
     */
    public final static IdentValue TABLE_ROW = addValue("table-row");
    /**
     * Description of the Field
     */
    public final static IdentValue TABLE_ROW_GROUP = addValue("table-row-group");
    /**
     * Description of the Field
     */
    public final static IdentValue TEXT_BOTTOM = addValue("text-bottom");
    /**
     * Description of the Field
     */
    public final static IdentValue TEXT_TOP = addValue("text-top");
    /**
     * Description of the Field
     */
    public final static IdentValue TOP = addValue("top");
    /**
     * Description of the Field
     */
    public final static IdentValue UNDERLINE = addValue("underline");
    /**
     * Description of the Field
     */
    public final static IdentValue UPPERCASE = addValue("uppercase");
    /**
     * Description of the Field
     */
    public final static IdentValue UPPER_ALPHA = addValue("upper-alpha");
    /**
     * Description of the Field
     */
    public final static IdentValue UPPER_LATIN = addValue("upper-latin");
    /**
     * Description of the Field
     */
    public final static IdentValue UPPER_ROMAN = addValue("upper-roman");

    /**
     * Description of the Field
     */
    private static Map ALL_IDENT_VALUES;

    /**
     * Constructor for the IdentValue object
     *
     * @param ident PARAM
     */
    private IdentValue(String ident) {
        this.ident = ident;
    }

    /**
     * Returns a string representation of the object, in this case, the ident as
     * a string (as it appears in the CSS spec).
     *
     * @return a string representation of the object.
     */
    public String toString() {
        return ident;
    }

    /**
     * Returns the Singleton IdentValue that corresponds to the given string,
     * e.g. for "normal" will return IdentValue.NORMAL. Use this when you have
     * the string but need to look up the Singleton. If the string doesn't match
     * an ident in the CSS spec, a runtime exception is thrown.
     *
     * @param ident The identifier to retrieve the Singleton IdentValue for.
     * @return see desc.
     */
    public static IdentValue getByIdentString(String ident) {
        IdentValue val = (IdentValue) ALL_IDENT_VALUES.get(ident);
        if (val == null) {
            throw new XRRuntimeException("Ident named " + ident + " has no IdentValue instance assigned to it.");
        }
        return val;
    }

    /**
     * Adds a feature to the Value attribute of the IdentValue class
     *
     * @param ident The feature to be added to the Value attribute
     * @return Returns
     */
    private final static synchronized IdentValue addValue(String ident) {
        if (ALL_IDENT_VALUES == null) {
            ALL_IDENT_VALUES = new TreeMap();
        }
        IdentValue val = new IdentValue(ident);
        ALL_IDENT_VALUES.put(ident, val);
        return val;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.8  2005/06/19 23:02:38  tobega
 * Implemented calculation of minimum cell-widths.
 * Implemented border-spacing.
 *
 * Revision 1.7  2005/06/15 10:01:36  pdoubleya
 * Added IV for both, used in clear.
 *
 * Revision 1.6  2005/04/20 19:13:17  tobega
 * Fixed vertical align. Middle works and all look pretty much like in firefox
 *
 * Revision 1.5  2005/01/29 20:21:09  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.4  2005/01/25 15:24:38  pdoubleya
 * Temporary support for pre-line and pre-wrap.
 *
 * Revision 1.3  2005/01/25 14:38:03  pdoubleya
 * Added temporary support for invalid identifier "inline-block" to support existing code.
 *
 *
 */
