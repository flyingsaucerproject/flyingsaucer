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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.css.constants;

import java.util.HashMap;
import java.util.Map;

import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.util.XRRuntimeException;


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
public class IdentValue implements FSDerivedValue {
    private static int maxAssigned = 0;

    /**
     * Description of the Field
     */
    private final String ident;

    public final int FS_ID;

    public final static IdentValue ABSOLUTE = addValue("absolute");
    public final static IdentValue ALWAYS = addValue("always");
    public final static IdentValue ARMENIAN = addValue("armenian");
    public final static IdentValue AUTO = addValue("auto");
    public final static IdentValue AVOID = addValue("avoid");
    public final static IdentValue BASELINE = addValue("baseline");
    public final static IdentValue BLINK = addValue("blink");
    public final static IdentValue BLOCK = addValue("block");
    public final static IdentValue BOLD = addValue("bold");
    public final static IdentValue BOLDER = addValue("bolder");
    public static final IdentValue BORDER_BOX = addValue("border-box");
    public final static IdentValue BOTH = addValue("both");
    public final static IdentValue BOTTOM = addValue("bottom");
    public final static IdentValue CAPITALIZE = addValue("capitalize");
    public final static IdentValue CENTER = addValue("center");
    public final static IdentValue CIRCLE = addValue("circle");
    public final static IdentValue CJK_IDEOGRAPHIC = addValue("cjk-ideographic");
    public final static IdentValue CLOSE_QUOTE = addValue("close-quote");
    public final static IdentValue COLLAPSE = addValue("collapse");
    public final static IdentValue COMPACT = addValue("compact");
    public final static IdentValue CONTAIN = addValue("contain");
    public static final IdentValue CONTENT_BOX = addValue("content-box");
    public final static IdentValue COVER = addValue("cover");
    public final static IdentValue CREATE = addValue("create");
    public final static IdentValue DASHED = addValue("dashed");
    public final static IdentValue DECIMAL = addValue("decimal");
    public final static IdentValue DECIMAL_LEADING_ZERO = addValue("decimal-leading-zero");
    public final static IdentValue DISC = addValue("disc");
    public final static IdentValue DOTTED = addValue("dotted");
    public final static IdentValue DOUBLE = addValue("double");
    public final static IdentValue DYNAMIC = addValue("dynamic");
    public final static IdentValue FIXED = addValue("fixed");
    public final static IdentValue FONT_WEIGHT_100 = addValue("100");
    public final static IdentValue FONT_WEIGHT_200 = addValue("200");
    public final static IdentValue FONT_WEIGHT_300 = addValue("300");
    public final static IdentValue FONT_WEIGHT_400 = addValue("400");
    public final static IdentValue FONT_WEIGHT_500 = addValue("500");
    public final static IdentValue FONT_WEIGHT_600 = addValue("600");
    public final static IdentValue FONT_WEIGHT_700 = addValue("700");
    public final static IdentValue FONT_WEIGHT_800 = addValue("800");
    public final static IdentValue FONT_WEIGHT_900 = addValue("900");
    public final static IdentValue FS_CONTENT_PLACEHOLDER = addValue("-fs-content-placeholder");
    public final static IdentValue FS_INITIAL_VALUE = addValue("-fs-initial-value");
    public final static IdentValue GEORGIAN = addValue("georgian");
    public final static IdentValue GROOVE = addValue("groove");
    public final static IdentValue HEBREW = addValue("hebrew");
    public final static IdentValue HIDDEN = addValue("hidden");
    public final static IdentValue HIDE = addValue("hide");
    public final static IdentValue HIRAGANA = addValue("hiragana");
    public final static IdentValue HIRAGANA_IROHA = addValue("hiragana-iroha");
    public final static IdentValue INHERIT = addValue("inherit");
    public final static IdentValue INLINE = addValue("inline");
    public final static IdentValue INLINE_BLOCK = addValue("inline-block");
    public final static IdentValue INLINE_TABLE = addValue("inline-table");
    public final static IdentValue INSET = addValue("inset");
    public final static IdentValue INSIDE = addValue("inside");
    public final static IdentValue ITALIC = addValue("italic");
    public final static IdentValue JUSTIFY = addValue("justify");
    public final static IdentValue KATAKANA = addValue("katakana");
    public final static IdentValue KATAKANA_IROHA = addValue("katakana-iroha");
    public final static IdentValue KEEP = addValue("keep");
    public final static IdentValue LANDSCAPE = addValue("landscape");
    public final static IdentValue LEFT = addValue("left");
    public final static IdentValue LIGHTER = addValue("lighter");
    public final static IdentValue LINE = addValue("line");
    public final static IdentValue LINE_THROUGH = addValue("line-through");
    public final static IdentValue LIST_ITEM = addValue("list-item");
    public final static IdentValue LOWER_ALPHA = addValue("lower-alpha");
    public final static IdentValue LOWER_GREEK = addValue("lower-greek");
    public final static IdentValue LOWER_LATIN = addValue("lower-latin");
    public final static IdentValue LOWER_ROMAN = addValue("lower-roman");
    public final static IdentValue LOWERCASE = addValue("lowercase");
    public final static IdentValue LTR = addValue("ltr");
    public final static IdentValue MARKER = addValue("marker");
    public final static IdentValue MIDDLE = addValue("middle");
    public final static IdentValue NO_CLOSE_QUOTE = addValue("no-close-quote");
    public final static IdentValue NO_OPEN_QUOTE = addValue("no-open-quote");
    public final static IdentValue NO_REPEAT = addValue("no-repeat");
    public final static IdentValue NONE = addValue("none");
    public final static IdentValue NORMAL = addValue("normal");
    public final static IdentValue NOWRAP = addValue("nowrap");
    public final static IdentValue BREAK_WORD = addValue("break-word");
    public final static IdentValue OBLIQUE = addValue("oblique");
    public final static IdentValue OPEN_QUOTE = addValue("open-quote");
    public final static IdentValue OUTSET = addValue("outset");
    public final static IdentValue OUTSIDE = addValue("outside");
    public final static IdentValue OVERLINE = addValue("overline");
    public final static IdentValue PAGINATE = addValue("paginate");
    public final static IdentValue POINTER = addValue("pointer");
    public final static IdentValue PORTRAIT = addValue("portrait");
    public final static IdentValue PRE = addValue("pre");
    public final static IdentValue PRE_LINE = addValue("pre-line");
    public final static IdentValue PRE_WRAP = addValue("pre-wrap");
    public final static IdentValue RELATIVE = addValue("relative");
    public final static IdentValue REPEAT = addValue("repeat");
    public final static IdentValue REPEAT_X = addValue("repeat-x");
    public final static IdentValue REPEAT_Y = addValue("repeat-y");
    public final static IdentValue RIDGE = addValue("ridge");
    public final static IdentValue RIGHT = addValue("right");
    public final static IdentValue RUN_IN = addValue("run-in");
    public final static IdentValue SCROLL = addValue("scroll");
    public final static IdentValue SEPARATE = addValue("separate");
    public final static IdentValue SHOW = addValue("show");
    public final static IdentValue SMALL_CAPS = addValue("small-caps");
    public final static IdentValue SOLID = addValue("solid");
    public final static IdentValue SQUARE = addValue("square");
    public final static IdentValue STATIC = addValue("static");
    public final static IdentValue SUB = addValue("sub");
    public final static IdentValue SUPER = addValue("super");
    public final static IdentValue TABLE = addValue("table");
    public final static IdentValue TABLE_CAPTION = addValue("table-caption");
    public final static IdentValue TABLE_CELL = addValue("table-cell");
    public final static IdentValue TABLE_COLUMN = addValue("table-column");
    public final static IdentValue TABLE_COLUMN_GROUP = addValue("table-column-group");
    public final static IdentValue TABLE_FOOTER_GROUP = addValue("table-footer-group");
    public final static IdentValue TABLE_HEADER_GROUP = addValue("table-header-group");
    public final static IdentValue TABLE_ROW = addValue("table-row");
    public final static IdentValue TABLE_ROW_GROUP = addValue("table-row-group");
    public final static IdentValue TEXT_BOTTOM = addValue("text-bottom");
    public final static IdentValue TEXT_TOP = addValue("text-top");
    public final static IdentValue THICK = addValue("thick");
    public final static IdentValue THIN = addValue("thin");
    public final static IdentValue TOP = addValue("top");
    public final static IdentValue TRANSPARENT = addValue("transparent");
    public final static IdentValue UNDERLINE = addValue("underline");
    public final static IdentValue UPPER_ALPHA = addValue("upper-alpha");
    public final static IdentValue UPPER_LATIN = addValue("upper-latin");
    public final static IdentValue UPPER_ROMAN = addValue("upper-roman");
    public final static IdentValue UPPERCASE = addValue("uppercase");
    public final static IdentValue VISIBLE = addValue("visible");
    public final static IdentValue CROSSHAIR = addValue("crosshair");
    public final static IdentValue DEFAULT = addValue("default");
    public final static IdentValue EMBED = addValue("embed");
    public final static IdentValue E_RESIZE = addValue("e-resize");
    public final static IdentValue HELP = addValue("help");
    public final static IdentValue LARGE = addValue("large");
    public final static IdentValue LARGER = addValue("larger");
    public final static IdentValue MEDIUM = addValue("medium");
    public final static IdentValue MOVE = addValue("move");
    public final static IdentValue N_RESIZE = addValue("n-resize");
    public final static IdentValue NE_RESIZE = addValue("ne-resize");
    public final static IdentValue NW_RESIZE = addValue("nw-resize");
    public final static IdentValue PROGRESS = addValue("progress");
    public final static IdentValue S_RESIZE = addValue("s-resize");
    public final static IdentValue SE_RESIZE = addValue("se-resize");
    public final static IdentValue SMALL = addValue("small");
    public final static IdentValue SMALLER = addValue("smaller");
    public final static IdentValue START = addValue("start");
    public final static IdentValue SW_RESIZE = addValue("sw-resize");
    public final static IdentValue TEXT = addValue("text");
    public final static IdentValue W_RESIZE = addValue("w-resize");
    public final static IdentValue WAIT = addValue("wait");
    public final static IdentValue X_LARGE = addValue("x-large");
    public final static IdentValue X_SMALL = addValue("x-small");
    public final static IdentValue XX_LARGE = addValue("xx-large");
    public final static IdentValue XX_SMALL = addValue("xx-small");
	public final static IdentValue MANUAL = addValue("manual");

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
        this.FS_ID = IdentValue.maxAssigned++;
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
     * TODO: doc
     */
    public static boolean looksLikeIdent(String ident) {
        return (IdentValue) ALL_IDENT_VALUES.get(ident) != null;
    }

    public static IdentValue valueOf(String ident) {
        return (IdentValue)ALL_IDENT_VALUES.get(ident);
    }

    public static int getIdentCount() {
        return ALL_IDENT_VALUES.size();
    }

    /**
     * Adds a feature to the Value attribute of the IdentValue class
     *
     * @param ident The feature to be added to the Value attribute
     * @return Returns
     */
    private final static synchronized IdentValue addValue(String ident) {
        if (ALL_IDENT_VALUES == null) {
            ALL_IDENT_VALUES = new HashMap();
        }
        IdentValue val = new IdentValue(ident);
        ALL_IDENT_VALUES.put(ident, val);
        return val;
    }

    /*
     * METHODS USED TO SUPPORT IdentValue as an FSDerivedValue, used in CalculatedStyle.
     * Most of these throw exceptions--makes use of the interface easier in CS (avoids casting)
     */

    public boolean isDeclaredInherit() {
        return this == INHERIT;
    }

    public FSDerivedValue computedValue() {
        return this;
    }

    public float asFloat() {
        throw new XRRuntimeException("Ident value is never a float; wrong class used for derived value.");
    }

    public FSColor asColor() {
        throw new XRRuntimeException("Ident value is never a color; wrong class used for derived value.");
    }

    public float getFloatProportionalTo(CSSName cssName,
                                        float baseValue,
                                        CssContext ctx) {
        throw new XRRuntimeException("Ident value (" + toString() + ") is never a length; wrong class used for derived value.");
    }

    public String asString() {
        return toString();
    }

    public String[] asStringArray() {
        throw new XRRuntimeException("Ident value is never a string array; wrong class used for derived value.");
    }

    public IdentValue asIdentValue() {
        return this;
    }

    public boolean hasAbsoluteUnit() {
        // log and return false
        throw new XRRuntimeException("Ident value is never an absolute unit; wrong class used for derived value; this " +
                "ident value is a " + this.asString());
    }

    public boolean isIdent() {
        return true;
    }

    public boolean isDependentOnFontSize() {
        return false;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.35  2008/12/14 13:53:31  peterbrant
 * Implement -fs-keep-with-inline: keep property that instructs FS to try to avoid breaking a box so that only borders and padding appear on a page
 *
 * Revision 1.34  2008/07/27 00:21:46  peterbrant
 * Implement CMYK color support for PDF output, starting with patch from Mykola Gurov / Banish java.awt.Color from FS core layout classes
 *
 * Revision 1.33  2007/10/31 23:14:41  peterbrant
 * Add rudimentary support for @font-face rules
 *
 * Revision 1.32  2007/08/19 22:22:51  peterbrant
 * Merge R8pbrant changes to HEAD
 *
 * Revision 1.31.2.3  2007/08/15 21:29:31  peterbrant
 * Initial draft of support for running headers and footers on tables
 *
 * Revision 1.31.2.2  2007/08/08 21:44:09  peterbrant
 * Implement more flexible page numbering
 *
 * Revision 1.31.2.1  2007/06/30 00:25:29  peterbrant
 * Remove obsolete comments (pre-line and pre-wrap are part of CSS21)
 *
 * Revision 1.31  2007/05/20 23:25:32  peterbrant
 * Various code cleanups (e.g. remove unused imports)
 *
 * Patch from Sean Bright
 *
 * Revision 1.30  2007/03/08 01:43:47  peterbrant
 * Don't cache rectangles with em or ex dimensions.  They aren't constant (even ignoring DPI related issues)
 *
 * Revision 1.29  2007/02/23 21:04:27  peterbrant
 * Implement complete support for background-position and background-attachment
 *
 * Revision 1.28  2007/02/23 16:54:38  peterbrant
 * Allow special ident -fs-intial-value to reset a property value to its initial value (used by border related shorthand properties as 'color' won't be known at property construction time)
 *
 * Revision 1.27  2007/02/20 20:05:40  peterbrant
 * Complete support for absolute and relative font sizes
 *
 * Revision 1.26  2007/02/19 23:18:40  peterbrant
 * Further work on new CSS parser / Misc. bug fixes
 *
 * Revision 1.25  2007/02/19 14:53:36  peterbrant
 * Integrate new CSS parser
 *
 * Revision 1.24  2007/02/07 16:33:36  peterbrant
 * Initial commit of rewritten table support and associated refactorings
 *
 * Revision 1.23  2007/01/16 16:11:38  peterbrant
 * Don't copy derived values as they propagate down the style tree (don't need to anymore
 * now that we don't cache length values in LengthValue and PointValue)
 *
 * Revision 1.22  2006/07/28 10:08:55  pdoubleya
 * Additional work for support of parsing content and quotes.
 *
 * Revision 1.21  2006/07/27 15:17:26  pdoubleya
 * Added missing idents for cursor and font-size, removed useless comments to make sorting easier.
 *
 * Revision 1.20  2006/05/08 21:24:24  pdoubleya
 * Log, don't throw exception, if we check for an absolute unit but it doesn't make sense to do so (IdentValue.hasAbsoluteUnit()).
 *
 * Revision 1.19  2005/12/28 00:50:51  peterbrant
 * Continue ripping out first try at pagination / Minor method name refactoring
 *
 * Revision 1.18  2005/12/13 20:46:09  peterbrant
 * Improve list support (implement list-style-position: inside, marker "sticks" to first line box even if there are other block boxes in between, plus other minor fixes) / Experimental support for optionally extending text decorations to box edge vs line edge
 *
 * Revision 1.17  2005/11/08 22:53:44  tobega
 * added getLineHeight method to CalculatedStyle and hacked in some list-item support
 *
 * Revision 1.16  2005/10/29 14:36:26  pdoubleya
 * isDeclaredInherit() was not implemented correctly, always returning false.
 *
 * Revision 1.15  2005/10/24 10:19:39  pdoubleya
 * CSSName FS_ID is now public and final, allowing direct access to the id, bypassing getAssignedID(); micro-optimization :); getAssignedID() and setAssignedID() have been removed. IdentValue string property is also final (as should have been).
 *
 * Revision 1.14  2005/10/24 09:29:40  pdoubleya
 * Added some missing idents.
 *
 * Revision 1.13  2005/10/21 12:01:13  pdoubleya
 * Added cachable rect property for margin, cleanup minor in styling.
 *
 * Revision 1.12  2005/10/21 10:02:53  pdoubleya
 * Cleanup, removed unneeded vars, reorg code in CS.
 *
 * Revision 1.11  2005/10/20 20:48:04  pdoubleya
 * Updates for refactoring to style classes. CalculatedStyle now has lookup methods to cover all general cases, so propertyByName() is private, which means the backing classes for styling were able to be replaced.
 *
 * Revision 1.10  2005/10/08 17:40:18  tobega
 * Patch from Peter Brant
 *
 * Revision 1.9  2005/09/26 22:40:16  tobega
 * Applied patch from Peter Brant concerning margin collapsing
 *
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
