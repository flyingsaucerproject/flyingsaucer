package org.xhtmlrenderer.css.constants;

import java.util.*;

import org.xhtmlrenderer.util.XRRuntimeException;


/**
 * An IdentValue represents a string that you can assign to a CSS property, where the string is one of several
 * enumerated values. For example, "whitespace" can take the values "nowrap", "pre" and "normal". There is a static
 * instance for all idents in the CSS 2 spec, which you can retrieve using the {@link #getByIdentString(String)} method.
 * The instance doesn't have any behavior: it's just a marker so that you can retrieve an ident from a DerivedValue or
 * CalculatedStyle, then compare to the instance here. For example:
 * <pre>
 * CalculatedStyle style = ...getstyle from somewhere
 * IdentValue whitespace = style.getIdent(CSSName.WHITESPACE);
 * if ( whitespace == IdentValue.NORMAL ) {
 *      // perform normal spacing
 * } else if ( whitespace == IdentValue.NOWRAP ) {
 *      // space with no wrapping
 * } else if ( whitespace == IdentValue.PRE ) {
 *      // preserve spacing
 * }
 * </pre>
 * All static instances are instantiated automatically, and are Singletons, so you can compare using a simple Object
 * comparison using <code>==</code>.
 */
public class IdentValue {
    private String ident;

    private static Map ALL_IDENT_VALUES;
    // CLEAN: referenced in WhitespaceStripper, probably a typo; pre-line does not exist
    // public static final IdentValue PRE_LINE = addValue("pre-line");
    public static final IdentValue ABSOLUTE = addValue("absolute");
    public static final IdentValue ARMENIAN = addValue("armenian");
    public static final IdentValue AUTO = addValue("auto");
    public static final IdentValue BASELINE = addValue("baseline");
    public static final IdentValue BLINK = addValue("blink");
    public static final IdentValue BLOCK = addValue("block");
    public static final IdentValue BOLD = addValue("bold");
    public static final IdentValue BOLDER = addValue("bolder");
    public static final IdentValue BOTTOM = addValue("bottom");
    public static final IdentValue CAPITALIZE = addValue("capitalize");
    public static final IdentValue CENTER = addValue("center");
    public static final IdentValue CIRCLE = addValue("circle");
    public static final IdentValue CJK_IDEOGRAPHIC = addValue("cjk-ideographic");
    public static final IdentValue COMPACT = addValue("compact");
    public static final IdentValue DASHED = addValue("dashed");
    public static final IdentValue DECIMAL = addValue("decimal");
    public static final IdentValue DECIMAL_LEADING_ZERO = addValue("decimal-leading-zero");
    public static final IdentValue DISC = addValue("disc");
    public static final IdentValue DOTTED = addValue("dotted");
    public static final IdentValue DOUBLE = addValue("double");
    public static final IdentValue FIXED = addValue("fixed");
    public static final IdentValue FONT_WEIGHT_100 = addValue("100");
    public static final IdentValue FONT_WEIGHT_200 = addValue("200");
    public static final IdentValue FONT_WEIGHT_300 = addValue("300");
    public static final IdentValue FONT_WEIGHT_400 = addValue("400");
    public static final IdentValue FONT_WEIGHT_500 = addValue("500");
    public static final IdentValue FONT_WEIGHT_600 = addValue("600");
    public static final IdentValue FONT_WEIGHT_700 = addValue("700");
    public static final IdentValue FONT_WEIGHT_800 = addValue("800");
    public static final IdentValue FONT_WEIGHT_900 = addValue("900");
    public static final IdentValue GEORGIAN = addValue("georgian");
    public static final IdentValue GROOVE = addValue("groove");
    public static final IdentValue HEBREW = addValue("hebrew");
    public static final IdentValue HIDDEN = addValue("hidden");
    public static final IdentValue HIRAGANA = addValue("hiragana");
    public static final IdentValue HIRAGANA_IROHA = addValue("hiragana-iroha");
    public static final IdentValue INLINE = addValue("inline");
    public static final IdentValue INLINE_TABLE = addValue("inline-table");
    public static final IdentValue INSET = addValue("inset");
    public static final IdentValue ITALIC = addValue("italic");
    public static final IdentValue JUSTIFY = addValue("justify");
    public static final IdentValue KATAKANA = addValue("katakana");
    public static final IdentValue KATAKANA_IROHA = addValue("katakana-iroha");
    public static final IdentValue LEFT = addValue("left");
    public static final IdentValue LIGHTER = addValue("lighter");
    public static final IdentValue LINE_THROUGH = addValue("line-through");
    public static final IdentValue LIST_ITEM = addValue("list-item");
    public static final IdentValue LOWERCASE = addValue("lowercase");
    public static final IdentValue LOWER_ALPHA = addValue("lower-alpha");
    public static final IdentValue LOWER_GREEK = addValue("lower-greek");
    public static final IdentValue LOWER_LATIN = addValue("lower-latin");
    public static final IdentValue LOWER_ROMAN = addValue("lower-roman");
    public static final IdentValue MARKER = addValue("marker");
    public static final IdentValue NONE = addValue("none");
    public static final IdentValue NORMAL = addValue("normal");
    public static final IdentValue NOWRAP = addValue("nowrap");
    public static final IdentValue NO_REPEAT = addValue("no-repeat");
    public static final IdentValue OBLIQUE = addValue("oblique");
    public static final IdentValue OUTSET = addValue("outset");
    public static final IdentValue OVERLINE = addValue("overline");
    public static final IdentValue PRE = addValue("pre");
    public static final IdentValue RELATIVE = addValue("relative");
    public static final IdentValue REPEAT = addValue("repeat");
    public static final IdentValue REPEAT_X = addValue("repeat-x");
    public static final IdentValue REPEAT_Y = addValue("repeat-y");
    public static final IdentValue RIDGE = addValue("ridge");
    public static final IdentValue RIGHT = addValue("right");
    public static final IdentValue RUN_IN = addValue("run-in");
    public static final IdentValue SCROLL = addValue("scroll");
    public static final IdentValue SMALL_CAPS = addValue("small-caps");
    public static final IdentValue SOLID = addValue("solid");
    public static final IdentValue SQUARE = addValue("square");
    public static final IdentValue STATIC = addValue("static");
    public static final IdentValue SUB = addValue("sub");
    public static final IdentValue SUPER = addValue("super");
    public static final IdentValue TABLE = addValue("table");
    public static final IdentValue TABLE_CAPTION = addValue("table-caption");
    public static final IdentValue TABLE_CELL = addValue("table-cell");
    public static final IdentValue TABLE_COLUMN = addValue("table-column");
    public static final IdentValue TABLE_COLUMN_GROUP = addValue("table-column-group");
    public static final IdentValue TABLE_FOOTER_GROUP = addValue("table-footer-group");
    public static final IdentValue TABLE_HEADER_GROUP = addValue("table-header-group");
    public static final IdentValue TABLE_ROW = addValue("table-row");
    public static final IdentValue TABLE_ROW_GROUP = addValue("table-row-group");
    public static final IdentValue TEXT_BOTTOM = addValue("text-bottom");
    public static final IdentValue TEXT_TOP = addValue("text-top");
    public static final IdentValue TOP = addValue("top");
    public static final IdentValue UNDERLINE = addValue("underline");
    public static final IdentValue UPPERCASE = addValue("uppercase");
    public static final IdentValue UPPER_ALPHA = addValue("upper-alpha");
    public static final IdentValue UPPER_LATIN = addValue("upper-latin");
    public static final IdentValue UPPER_ROMAN = addValue("upper-roman");

    private IdentValue(String ident) {
        this.ident = ident;
    }

    private static synchronized final IdentValue addValue(String ident) {
        if (ALL_IDENT_VALUES == null) {
            ALL_IDENT_VALUES = new TreeMap();
        }
        IdentValue val = new IdentValue(ident);
        ALL_IDENT_VALUES.put(ident, val);
        return val;
    }

    /**
     * Returns a string representation of the object, in this case, the ident as a string (as it appears in the CSS
     * spec).
     *
     * @return a string representation of the object.
     */
    public String toString() {
        return ident;
    }

    /**
     * Returns the Singleton IdentValue that corresponds to the given string, e.g. for "normal" will return
     * IdentValue.NORMAL. Use this when you have the string but need to look up the Singleton. If the string doesn't
     * match an ident in the CSS spec, a runtime exception is thrown.
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
}
