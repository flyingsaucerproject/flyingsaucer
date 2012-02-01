/*
 * CalculatedStyle.java
 * Copyright (c) 2004, 2005 Patrick Wright, Torbj�rn Gannholm
 * Copyright (c) 2006 Wisconsin Court System
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
 *
 */
package org.xhtmlrenderer.css.style;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.parser.FSFunction;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.parser.property.PrimitivePropertyBuilders;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.DerivedValueFactory;
import org.xhtmlrenderer.css.style.derived.FunctionValue;
import org.xhtmlrenderer.css.style.derived.LengthValue;
import org.xhtmlrenderer.css.style.derived.ListValue;
import org.xhtmlrenderer.css.style.derived.NumberValue;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.FSFontMetrics;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;


/**
 * A set of properties that apply to a single Element, derived from all matched
 * properties following the rules for CSS cascade, inheritance, importance,
 * specificity and sequence. A derived style is just like a style but
 * (presumably) has additional information that allows relative properties to be
 * assigned values, e.g. font attributes. Property values are fully resolved
 * when this style is created. A property retrieved by name should always have
 * only one value in this class (e.g. one-one map). Any methods to retrieve
 * property values from an instance of this class require a valid {@link
 * org.xhtmlrenderer.layout.Context} be given to it, for some cases of property
 * resolution. Generally, a programmer will not use this class directly, but
 * will retrieve properties using a {@link org.xhtmlrenderer.context.StyleReference}
 * implementation.
 *
 * @author Torbj�rn Gannholm
 * @author Patrick Wright
 */
public class CalculatedStyle {
    /**
     * The parent-style we inherit from
     */
    private CalculatedStyle _parent;

    private BorderPropertySet _border;
    private RectPropertySet _margin;
    private RectPropertySet _padding;

    private float _lineHeight;
    private boolean _lineHeightResolved;

    private FSFont _FSFont;
    private FSFontMetrics _FSFontMetrics;

    private boolean _marginsAllowed = true;
    private boolean _paddingAllowed = true;
    private boolean _bordersAllowed = true;

    private BackgroundSize _backgroundSize;

    /**
     * Cache child styles of this style that have the same cascaded properties
     */
    private final java.util.HashMap _childCache = new java.util.HashMap();
    /*private java.util.HashMap _childCache = new java.util.LinkedHashMap(5, 0.75f, true) {
        private static final int MAX_ENTRIES = 10;

        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_ENTRIES;
        }
    };*/

    /**
     * Our main array of property values defined in this style, keyed
     * by the CSSName assigned ID.
     */
    private final FSDerivedValue[] _derivedValuesById;

    /**
     * The derived Font for this style
     */
    private FontSpecification _font;


    /**
     * Default constructor; as the instance is immutable after use, don't use
     * this for class instantiation externally.
     */
    protected CalculatedStyle() {
        _derivedValuesById = new FSDerivedValue[CSSName.countCSSPrimitiveNames()];
    }


    /**
     * Constructor for the CalculatedStyle object. To get a derived style, use
     * the Styler objects getDerivedStyle which will cache styles
     *
     * @param parent  PARAM
     * @param matched PARAM
     */
    private CalculatedStyle(CalculatedStyle parent, CascadedStyle matched) {
        this();
        _parent = parent;

        derive(matched);

        checkPaddingAllowed();
        checkMarginsAllowed();
        checkBordersAllowed();
    }

    private void checkPaddingAllowed() {
        IdentValue v = getIdent(CSSName.DISPLAY);
        if (v == IdentValue.TABLE_HEADER_GROUP || v == IdentValue.TABLE_ROW_GROUP ||
                v == IdentValue.TABLE_FOOTER_GROUP || v == IdentValue.TABLE_ROW) {
            _paddingAllowed = false;
        } else if ((v == IdentValue.TABLE || v == IdentValue.INLINE_TABLE) && isCollapseBorders()) {
            _paddingAllowed = false;
        }
    }

    private void checkMarginsAllowed() {
        IdentValue v = getIdent(CSSName.DISPLAY);
        if (v == IdentValue.TABLE_HEADER_GROUP || v == IdentValue.TABLE_ROW_GROUP ||
                v == IdentValue.TABLE_FOOTER_GROUP || v == IdentValue.TABLE_ROW ||
                v == IdentValue.TABLE_CELL) {
            _marginsAllowed = false;
        }
    }

    private void checkBordersAllowed() {
        IdentValue v = getIdent(CSSName.DISPLAY);
        if (v == IdentValue.TABLE_HEADER_GROUP || v == IdentValue.TABLE_ROW_GROUP ||
                v == IdentValue.TABLE_FOOTER_GROUP || v == IdentValue.TABLE_ROW) {
            _bordersAllowed = false;
        }
    }

    /**
     * derives a child style from this style.
     * <p/>
     * depends on the ability to return the identical CascadedStyle each time a child style is needed
     *
     * @param matched the CascadedStyle to apply
     * @return The derived child style
     */
    public synchronized CalculatedStyle deriveStyle(CascadedStyle matched) {
        String fingerprint = matched.getFingerprint();
        CalculatedStyle cs = (CalculatedStyle) _childCache.get(fingerprint);

        if (cs == null) {
            cs = new CalculatedStyle(this, matched);
            _childCache.put(fingerprint, cs);
        }
        return cs;
    }

    public int countAssigned() {
        int c = 0;
        for (int i = 0; i < _derivedValuesById.length; i++) {
            if (_derivedValuesById[i] != null) c++;
        }
        return c;
    }

    /**
     * Returns the parent style.
     *
     * @return Returns the parent style
     */
    public CalculatedStyle getParent() {
        return _parent;
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return The borderWidth value
     */
    public String toString() {
        return genStyleKey();
    }

    public FSColor asColor(CSSName cssName) {
        FSDerivedValue prop = valueByName(cssName);
        if (prop == IdentValue.TRANSPARENT) {
            return FSRGBColor.TRANSPARENT;
        } else {
            return prop.asColor();
        }
    }

    public float asFloat(CSSName cssName) {
        return valueByName(cssName).asFloat();
    }

    public String asString(CSSName cssName) {
        return valueByName(cssName).asString();
    }

    public String[] asStringArray(CSSName cssName) {
        return valueByName(cssName).asStringArray();
    }

    public void setDefaultValue(CSSName cssName, FSDerivedValue fsDerivedValue) {
        if (_derivedValuesById[cssName.FS_ID] == null) {
            _derivedValuesById[cssName.FS_ID] = fsDerivedValue;
        }
    }

    // TODO: doc
    public boolean hasAbsoluteUnit(CSSName cssName) {
        boolean isAbs = false;
        try {
            isAbs = valueByName(cssName).hasAbsoluteUnit();
        } catch (Exception e) {
            XRLog.layout(Level.WARNING, "Property " + cssName + " has an assignment we don't understand, " +
                    "and can't tell if it's an absolute unit or not. Assuming it is not. Exception was: " +
                    e.getMessage());
            isAbs = false;
        }
        return isAbs;
    }

    /**
     * Gets the ident attribute of the CalculatedStyle object
     *
     * @param cssName PARAM
     * @param val     PARAM
     * @return The ident value
     */
    public boolean isIdent(CSSName cssName, IdentValue val) {
        return valueByName(cssName) == val;
    }

    /**
     * Gets the ident attribute of the CalculatedStyle object
     *
     * @param cssName PARAM
     * @return The ident value
     */
    public IdentValue getIdent(CSSName cssName) {
        return valueByName(cssName).asIdentValue();
    }

    /**
     * Convenience property accessor; returns a Color initialized with the
     * foreground color Uses the actual value (computed actual value) for this
     * element.
     *
     * @return The color value
     */
    public FSColor getColor() {
        return asColor(CSSName.COLOR);
    }

    /**
     * Convenience property accessor; returns a Color initialized with the
     * background color value; Uses the actual value (computed actual value) for
     * this element.
     *
     * @return The backgroundColor value
     */
    public FSColor getBackgroundColor() {
        FSDerivedValue prop = valueByName(CSSName.BACKGROUND_COLOR);
        if (prop == IdentValue.TRANSPARENT) {
            return null;
        } else {
            return asColor(CSSName.BACKGROUND_COLOR);
        }
    }

    public BackgroundSize getBackgroundSize() {
        if (_backgroundSize == null) {
            _backgroundSize = createBackgroundSize();
        }

        return _backgroundSize;
    }

    private BackgroundSize createBackgroundSize() {
        FSDerivedValue value = valueByName(CSSName.BACKGROUND_SIZE);
        if (value instanceof IdentValue) {
            IdentValue ident = (IdentValue)value;
            if (ident == IdentValue.COVER) {
                return new BackgroundSize(false, true, false);
            } else if (ident == IdentValue.CONTAIN) {
                return new BackgroundSize(true, false, false);
            }
        } else {
            ListValue valueList = (ListValue)value;
            List values = valueList.getValues();
            boolean firstAuto = ((PropertyValue)values.get(0)).getIdentValue() == IdentValue.AUTO;
            boolean secondAuto = ((PropertyValue)values.get(1)).getIdentValue() == IdentValue.AUTO;

            if (firstAuto && secondAuto) {
                return new BackgroundSize(false, false, true);
            } else {
                return new BackgroundSize((PropertyValue)values.get(0), (PropertyValue)values.get(1));
            }
        }

        throw new RuntimeException("internal error");
    }

    public BackgroundPosition getBackgroundPosition() {
        ListValue result = (ListValue) valueByName(CSSName.BACKGROUND_POSITION);
        List values = result.getValues();

        return new BackgroundPosition(
                (PropertyValue) values.get(0), (PropertyValue) values.get(1));
    }

    public List getCounterReset() {
        FSDerivedValue value = valueByName(CSSName.COUNTER_RESET);

        if (value == IdentValue.NONE) {
            return null;
        } else {
            return ((ListValue) value).getValues();
        }
    }

    public List getCounterIncrement() {
        FSDerivedValue value = valueByName(CSSName.COUNTER_INCREMENT);

        if (value == IdentValue.NONE) {
            return null;
        } else {
            return ((ListValue) value).getValues();
        }
    }

    public BorderPropertySet getBorder(CssContext ctx) {
        if (! _bordersAllowed) {
            return BorderPropertySet.EMPTY_BORDER;
        } else {
            BorderPropertySet b = getBorderProperty(this, ctx);
            return b;
        }
    }

    public FontSpecification getFont(CssContext ctx) {
        if (_font == null) {
            _font = new FontSpecification();

            _font.families = valueByName(CSSName.FONT_FAMILY).asStringArray();

            FSDerivedValue fontSize = valueByName(CSSName.FONT_SIZE);
            if (fontSize instanceof IdentValue) {
                PropertyValue replacement;
                IdentValue resolved = resolveAbsoluteFontSize();
                if (resolved != null) {
                    replacement = FontSizeHelper.resolveAbsoluteFontSize(resolved, _font.families);
                } else {
                    replacement = FontSizeHelper.getDefaultRelativeFontSize((IdentValue) fontSize);
                }
                _font.size = LengthValue.calcFloatProportionalValue(
                        this, CSSName.FONT_SIZE, replacement.getCssText(),
                        replacement.getFloatValue(), replacement.getPrimitiveType(), 0, ctx);
            } else {
                _font.size = getFloatPropertyProportionalTo(CSSName.FONT_SIZE, 0, ctx);
            }

            _font.fontWeight = getIdent(CSSName.FONT_WEIGHT);

            _font.fontStyle = getIdent(CSSName.FONT_STYLE);
            _font.variant = getIdent(CSSName.FONT_VARIANT);
        }
        return _font;
    }

    public FontSpecification getFontSpecification() {
	return _font;
    }

    private IdentValue resolveAbsoluteFontSize() {
        FSDerivedValue fontSize = valueByName(CSSName.FONT_SIZE);
        if (! (fontSize instanceof IdentValue)) {
            return null;
        }
        IdentValue fontSizeIdent = (IdentValue) fontSize;
        if (PrimitivePropertyBuilders.ABSOLUTE_FONT_SIZES.get(fontSizeIdent.FS_ID)) {
            return fontSizeIdent;
        }

        IdentValue parent = getParent().resolveAbsoluteFontSize();
        if (parent != null) {
            if (fontSizeIdent == IdentValue.SMALLER) {
                return FontSizeHelper.getNextSmaller(parent);
            } else if (fontSize == IdentValue.LARGER) {
                return FontSizeHelper.getNextLarger(parent);
            }
        }

        return null;
    }

    public float getFloatPropertyProportionalTo(CSSName cssName, float baseValue, CssContext ctx) {
        return valueByName(cssName).getFloatProportionalTo(cssName, baseValue, ctx);
    }

    /**
     * @param cssName
     * @param parentWidth
     * @param ctx
     * @return TODO
     */
    public float getFloatPropertyProportionalWidth(CSSName cssName, float parentWidth, CssContext ctx) {
        return valueByName(cssName).getFloatProportionalTo(cssName, parentWidth, ctx);
    }

    /**
     * @param cssName
     * @param parentHeight
     * @param ctx
     * @return TODO
     */
    public float getFloatPropertyProportionalHeight(CSSName cssName, float parentHeight, CssContext ctx) {
        return valueByName(cssName).getFloatProportionalTo(cssName, parentHeight, ctx);
    }

    public float getLineHeight(CssContext ctx) {
        if (! _lineHeightResolved) {
            if (isIdent(CSSName.LINE_HEIGHT, IdentValue.NORMAL)) {
                float lineHeight1 = getFont(ctx).size * 1.1f;
                // Make sure rasterized characters will (probably) fit inside
                // the line box
                FSFontMetrics metrics = getFSFontMetrics(ctx);
                float lineHeight2 = (float)Math.ceil(metrics.getDescent() + metrics.getAscent());
                _lineHeight = Math.max(lineHeight1, lineHeight2);
            } else if (isLength(CSSName.LINE_HEIGHT)) {
                //could be more elegant, I suppose
                _lineHeight = getFloatPropertyProportionalHeight(CSSName.LINE_HEIGHT, 0, ctx);
            } else {
                //must be a number
                _lineHeight = getFont(ctx).size * valueByName(CSSName.LINE_HEIGHT).asFloat();
            }
            _lineHeightResolved = true;
        }
        return _lineHeight;
    }

    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided margin width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @param cbWidth
     * @param ctx
     * @return The marginWidth value
     */
    public RectPropertySet getMarginRect(float cbWidth, CssContext ctx) {
        return getMarginRect(cbWidth, ctx, true);
    }

    public RectPropertySet getMarginRect(float cbWidth, CssContext ctx, boolean useCache) {
        if (! _marginsAllowed) {
            return RectPropertySet.ALL_ZEROS;
        } else {
            return getMarginProperty(
                    this, CSSName.MARGIN_SHORTHAND, CSSName.MARGIN_SIDE_PROPERTIES, cbWidth, ctx, useCache);
        }
    }

    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided padding width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @param cbWidth
     * @param ctx
     * @return The paddingWidth value
     */
    public RectPropertySet getPaddingRect(float cbWidth, CssContext ctx, boolean useCache) {
        if (! _paddingAllowed) {
            return RectPropertySet.ALL_ZEROS;
        } else {
            return getPaddingProperty(this, CSSName.PADDING_SHORTHAND, CSSName.PADDING_SIDE_PROPERTIES, cbWidth, ctx, useCache);
        }
    }

    public RectPropertySet getPaddingRect(float cbWidth, CssContext ctx) {
        return getPaddingRect(cbWidth, ctx, true);
    }

    /**
     * @param cssName
     * @return TODO
     */
    public String getStringProperty(CSSName cssName) {
        return valueByName(cssName).asString();
    }

    /**
     * TODO: doc
     */
    public boolean isLength(CSSName cssName) {
        FSDerivedValue val = valueByName(cssName);
        return val instanceof LengthValue;
    }

    public boolean isLengthOrNumber(CSSName cssName) {
        FSDerivedValue val = valueByName(cssName);
        return val instanceof NumberValue || val instanceof LengthValue;
    }

    /**
     * Returns a {@link FSDerivedValue} by name. Because we are a derived
     * style, the property will already be resolved at this point.
     *
     * @param cssName The CSS property name, e.g. "font-family"
     * @return See desc.
     */
    public FSDerivedValue valueByName(CSSName cssName) {
        FSDerivedValue val = _derivedValuesById[cssName.FS_ID];

        boolean needInitialValue = val == IdentValue.FS_INITIAL_VALUE;

        // but the property may not be defined for this Element
        if (val == null || needInitialValue) {
            // if it is inheritable (like color) and we are not root, ask our parent
            // for the value
            if (! needInitialValue && CSSName.propertyInherits(cssName)
                    && _parent != null
                    //
                    && (val = _parent.valueByName(cssName)) != null) {
                // Do nothing, val is already set
            } else {
                // otherwise, use the initial value (defined by the CSS2 Spec)
                String initialValue = CSSName.initialValue(cssName);
                if (initialValue == null) {
                    throw new XRRuntimeException("Property '" + cssName + "' has no initial values assigned. " +
                            "Check CSSName declarations.");
                }
                if (initialValue.charAt(0) == '=') {
                    CSSName ref = CSSName.getByPropertyName(initialValue.substring(1));
                    val = valueByName(ref);
                } else {
                    val = CSSName.initialDerivedValue(cssName);
                }
            }
            _derivedValuesById[cssName.FS_ID] = val;
        }
        return val;
    }

    /**
     * <p/>
     * <p/>
     * <p/>
     * <p/>
     * Implements cascade/inherit/important logic. This should result in the
     * element for this style having a value for *each and every* (visual)
     * property in the CSS2 spec. The implementation is based on the notion that
     * the matched styles are given to us in a perfectly sorted order, such that
     * properties appearing later in the rule-set always override properties
     * appearing earlier. It also assumes that all properties in the CSS2 spec
     * are defined somewhere across all the matched styles; for example, that
     * the full-property set is given in the user-agent CSS that is always
     * loaded with styles. The current implementation makes no attempt to check
     * either of these assumptions. When this method exits, the derived property
     * list for this class will be populated with the properties defined for
     * this element, properly cascaded.</p>
     *
     * @param matched PARAM
     */
    private void derive(CascadedStyle matched) {
        if (matched == null) {
            return;
        }//nothing to derive

        Iterator mProps = matched.getCascadedPropertyDeclarations();
        while (mProps.hasNext()) {
            PropertyDeclaration pd = (PropertyDeclaration) mProps.next();
            FSDerivedValue val = deriveValue(pd.getCSSName(), pd.getValue());
            _derivedValuesById[pd.getCSSName().FS_ID] = val;
        }
    }

    private FSDerivedValue deriveValue(CSSName cssName, org.w3c.dom.css.CSSPrimitiveValue value) {
        return DerivedValueFactory.newDerivedValue(this, cssName, (PropertyValue) value);
    }

    private String genStyleKey() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < _derivedValuesById.length; i++) {
            CSSName name = CSSName.getByID(i);
            FSDerivedValue val = _derivedValuesById[i];
            if (val != null) {
                sb.append(name.toString());
            } else {
                sb.append("(no prop assigned in this pos)");
            }
            sb.append("|\n");
        }
        return sb.toString();

    }

    public RectPropertySet getCachedPadding() {
        if (_padding == null) {
            throw new XRRuntimeException("No padding property cached yet; should have called getPropertyRect() at least once before.");
        } else {
            return _padding;
        }
    }

    public RectPropertySet getCachedMargin() {
        if (_margin == null) {
            throw new XRRuntimeException("No margin property cached yet; should have called getMarginRect() at least once before.");
        } else {
            return _margin;
        }
    }

    private static RectPropertySet getPaddingProperty(CalculatedStyle style,
                                                      CSSName shorthandProp,
                                                      CSSName.CSSSideProperties sides,
                                                      float cbWidth,
                                                      CssContext ctx,
                                                      boolean useCache) {
        if (! useCache) {
            return newRectInstance(style, shorthandProp, sides, cbWidth, ctx);
        } else {
            if (style._padding == null) {
                RectPropertySet result = newRectInstance(style, shorthandProp, sides, cbWidth, ctx);
                boolean allZeros = result.isAllZeros();

                if (allZeros) {
                    result = RectPropertySet.ALL_ZEROS;
                }

                style._padding = result;

                if (! allZeros && style._padding.hasNegativeValues()) {
                    style._padding.resetNegativeValues();
                }
            }

            return style._padding;
        }
    }

    private static RectPropertySet getMarginProperty(CalculatedStyle style,
                                                     CSSName shorthandProp,
                                                     CSSName.CSSSideProperties sides,
                                                     float cbWidth,
                                                     CssContext ctx,
                                                     boolean useCache) {
        if (! useCache) {
            return newRectInstance(style, shorthandProp, sides, cbWidth, ctx);
        } else {
            if (style._margin == null) {
                RectPropertySet result = newRectInstance(style, shorthandProp, sides, cbWidth, ctx);
                if (result.isAllZeros()) {
                    result = RectPropertySet.ALL_ZEROS;
                }
                style._margin = result;
            }

            return style._margin;
        }
    }

    private static RectPropertySet newRectInstance(CalculatedStyle style,
                                                   CSSName shorthand,
                                                   CSSName.CSSSideProperties sides,
                                                   float cbWidth,
                                                   CssContext ctx) {
        RectPropertySet rect;
        rect = RectPropertySet.newInstance(style,
                shorthand,
                sides,
                cbWidth,
                ctx);
        return rect;
    }

    private static BorderPropertySet getBorderProperty(CalculatedStyle style,
                                                       CssContext ctx) {
        if (style._border == null) {
            BorderPropertySet result = BorderPropertySet.newInstance(style, ctx);

            boolean allZeros = result.isAllZeros();
            if (allZeros && ! result.hasHidden()) {
                result = BorderPropertySet.EMPTY_BORDER;
            }

            style._border = result;

            if (! allZeros && style._border.hasNegativeValues()) {
                style._border.resetNegativeValues();
            }
        }
        return style._border;
    }

    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    public static final int TOP = 3;
    public static final int BOTTOM = 4;

    public int getMarginBorderPadding(
            CssContext cssCtx, int cbWidth, int which) {
        BorderPropertySet border = getBorder(cssCtx);
        RectPropertySet margin = getMarginRect(cbWidth, cssCtx);
        RectPropertySet padding = getPaddingRect(cbWidth, cssCtx);

        switch (which) {
            case LEFT:
                return (int) (margin.left() + border.left() + padding.left());
            case RIGHT:
                return (int) (margin.right() + border.right() + padding.right());
            case TOP:
                return (int) (margin.top() + border.top() + padding.top());
            case BOTTOM:
                return (int) (margin.bottom() + border.bottom() + padding.bottom());
            default:
                throw new IllegalArgumentException();
        }
    }

    public IdentValue getWhitespace() {
        return getIdent(CSSName.WHITE_SPACE);
    }

    public FSFont getFSFont(CssContext cssContext) {
        if (_FSFont == null) {
            _FSFont = cssContext.getFont(getFont(cssContext));
        }
        return _FSFont;
    }

    public FSFontMetrics getFSFontMetrics(CssContext c) {
        if (_FSFontMetrics == null) {
            _FSFontMetrics = c.getFSFontMetrics(getFSFont(c));
        }
        return _FSFontMetrics;
    }

    public IdentValue getWordWrap() {
        return getIdent(CSSName.WORD_WRAP);
    }

    public boolean isClearLeft() {
        IdentValue clear = getIdent(CSSName.CLEAR);
        return clear == IdentValue.LEFT || clear == IdentValue.BOTH;
    }

    public boolean isClearRight() {
        IdentValue clear = getIdent(CSSName.CLEAR);
        return clear == IdentValue.RIGHT || clear == IdentValue.BOTH;
    }

    public boolean isCleared() {
        return ! isIdent(CSSName.CLEAR, IdentValue.NONE);
    }

    public IdentValue getBackgroundRepeat() {
        return getIdent(CSSName.BACKGROUND_REPEAT);
    }

    public IdentValue getBackgroundAttachment() {
        return getIdent(CSSName.BACKGROUND_ATTACHMENT);
    }

    public boolean isFixedBackground() {
        return getIdent(CSSName.BACKGROUND_ATTACHMENT) == IdentValue.FIXED;
    }

    public boolean isInline() {
        return isIdent(CSSName.DISPLAY, IdentValue.INLINE) &&
                ! (isFloated() || isAbsolute() || isFixed() || isRunning());
    }

    public boolean isInlineBlock() {
        return isIdent(CSSName.DISPLAY, IdentValue.INLINE_BLOCK);
    }

    public boolean isTable() {
        return isIdent(CSSName.DISPLAY, IdentValue.TABLE);
    }

    public boolean isInlineTable() {
        return isIdent(CSSName.DISPLAY, IdentValue.INLINE_TABLE);
    }

    public boolean isTableCell() {
        return isIdent(CSSName.DISPLAY, IdentValue.TABLE_CELL);
    }

    public boolean isTableSection() {
        IdentValue display = getIdent(CSSName.DISPLAY);

        return display == IdentValue.TABLE_ROW_GROUP ||
                display == IdentValue.TABLE_HEADER_GROUP ||
                display == IdentValue.TABLE_FOOTER_GROUP;
    }

    public boolean isTableCaption() {
        return isIdent(CSSName.DISPLAY, IdentValue.TABLE_CAPTION);
    }

    public boolean isTableHeader() {
        return isIdent(CSSName.DISPLAY, IdentValue.TABLE_HEADER_GROUP);
    }

    public boolean isTableFooter() {
        return isIdent(CSSName.DISPLAY, IdentValue.TABLE_FOOTER_GROUP);
    }

    public boolean isTableRow() {
        return isIdent(CSSName.DISPLAY, IdentValue.TABLE_ROW);
    }

    public boolean isDisplayNone() {
        return isIdent(CSSName.DISPLAY, IdentValue.NONE);
    }

    public boolean isSpecifiedAsBlock() {
        return isIdent(CSSName.DISPLAY, IdentValue.BLOCK);
    }

    public boolean isBlockEquivalent() {
        if (isFloated() || isAbsolute() || isFixed()) {
            return true;
        } else {
            IdentValue display = getIdent(CSSName.DISPLAY);
            if (display == IdentValue.INLINE) {
                return false;
            } else {
                return display == IdentValue.BLOCK || display == IdentValue.LIST_ITEM ||
                        display == IdentValue.RUN_IN || display == IdentValue.INLINE_BLOCK ||
                        display == IdentValue.TABLE || display == IdentValue.INLINE_TABLE;
            }
        }
    }

    public boolean isLayedOutInInlineContext() {
        if (isFloated() || isAbsolute() || isFixed() || isRunning()) {
            return true;
        } else {
            IdentValue display = getIdent(CSSName.DISPLAY);
            return display == IdentValue.INLINE || display == IdentValue.INLINE_BLOCK ||
                    display == IdentValue.INLINE_TABLE;
        }
    }

    public boolean isNeedAutoMarginResolution() {
        return ! (isAbsolute() || isFixed() || isFloated() || isInlineBlock());
    }

    public boolean isAbsolute() {
        return isIdent(CSSName.POSITION, IdentValue.ABSOLUTE);
    }

    public boolean isFixed() {
        return isIdent(CSSName.POSITION, IdentValue.FIXED);
    }

    public boolean isFloated() {
        IdentValue floatVal = getIdent(CSSName.FLOAT);
        return floatVal == IdentValue.LEFT || floatVal == IdentValue.RIGHT;
    }

    public boolean isFloatedLeft() {
        return isIdent(CSSName.FLOAT, IdentValue.LEFT);
    }

    public boolean isFloatedRight() {
        return isIdent(CSSName.FLOAT, IdentValue.RIGHT);
    }

    public boolean isRelative() {
        return isIdent(CSSName.POSITION, IdentValue.RELATIVE);
    }

    public boolean isPostionedOrFloated() {
        return isAbsolute() || isFixed() || isFloated() || isRelative();
    }

    public boolean isPositioned() {
        return isAbsolute() || isFixed() || isRelative();
    }

    public boolean isAutoWidth() {
        return isIdent(CSSName.WIDTH, IdentValue.AUTO);
    }

    public boolean isAbsoluteWidth() {
        return valueByName(CSSName.WIDTH).hasAbsoluteUnit();
    }

    public boolean isAutoHeight() {
        return isIdent(CSSName.HEIGHT, IdentValue.AUTO);
    }

    public boolean isAutoLeftMargin() {
        return isIdent(CSSName.MARGIN_LEFT, IdentValue.AUTO);
    }

    public boolean isAutoRightMargin() {
        return isIdent(CSSName.MARGIN_RIGHT, IdentValue.AUTO);
    }

    public boolean isAutoZIndex() {
        return isIdent(CSSName.Z_INDEX, IdentValue.AUTO);
    }

    public boolean establishesBFC() {
        FSDerivedValue value = valueByName(CSSName.POSITION);

        if (value instanceof FunctionValue) {  // running(header)
            return false;
        } else {
            IdentValue display = getIdent(CSSName.DISPLAY);
            IdentValue position = (IdentValue)value;

            return isFloated() ||
                    position == IdentValue.ABSOLUTE || position == IdentValue.FIXED ||
                    display == IdentValue.INLINE_BLOCK || display == IdentValue.TABLE_CELL ||
                    ! isIdent(CSSName.OVERFLOW, IdentValue.VISIBLE);
        }
    }

    public boolean requiresLayer() {
        FSDerivedValue value = valueByName(CSSName.POSITION);

        if (value instanceof FunctionValue) {  // running(header)
            return false;
        } else {
            IdentValue position = getIdent(CSSName.POSITION);

            if (position == IdentValue.ABSOLUTE ||
                    position == IdentValue.RELATIVE || position == IdentValue.FIXED) {
                return true;
            }

            IdentValue overflow = getIdent(CSSName.OVERFLOW);
            if ((overflow == IdentValue.SCROLL || overflow == IdentValue.AUTO) &&
                    isOverflowApplies()) {
                return true;
            }

            return false;
        }
    }

    public boolean isRunning() {
        FSDerivedValue value = valueByName(CSSName.POSITION);
        return value instanceof FunctionValue;
    }

    public String getRunningName() {
        FunctionValue value = (FunctionValue)valueByName(CSSName.POSITION);
        FSFunction function = value.getFunction();

        PropertyValue param = (PropertyValue)function.getParameters().get(0);

        return param.getStringValue();
    }

    public boolean isOverflowApplies() {
        IdentValue display = getIdent(CSSName.DISPLAY);
        return display == IdentValue.BLOCK || display == IdentValue.LIST_ITEM ||
                display == IdentValue.TABLE || display == IdentValue.INLINE_BLOCK ||
                display == IdentValue.TABLE_CELL;
    }

    public boolean isOverflowVisible() {
        return valueByName(CSSName.OVERFLOW) == IdentValue.VISIBLE;
    }

    public boolean isHorizontalBackgroundRepeat() {
        IdentValue value = getIdent(CSSName.BACKGROUND_REPEAT);
        return value == IdentValue.REPEAT_X || value == IdentValue.REPEAT;
    }

    public boolean isVerticalBackgroundRepeat() {
        IdentValue value = getIdent(CSSName.BACKGROUND_REPEAT);
        return value == IdentValue.REPEAT_Y || value == IdentValue.REPEAT;
    }

    public boolean isTopAuto() {
        return isIdent(CSSName.TOP, IdentValue.AUTO);

    }

    public boolean isBottomAuto() {
        return isIdent(CSSName.BOTTOM, IdentValue.AUTO);
    }

    public boolean isListItem() {
        return isIdent(CSSName.DISPLAY, IdentValue.LIST_ITEM);
    }

    public boolean isVisible() {
        return isIdent(CSSName.VISIBILITY, IdentValue.VISIBLE);
    }

    public boolean isForcePageBreakBefore() {
        IdentValue val = getIdent(CSSName.PAGE_BREAK_BEFORE);
        return val == IdentValue.ALWAYS || val == IdentValue.LEFT
                || val == IdentValue.RIGHT;
    }

    public boolean isForcePageBreakAfter() {
        IdentValue val = getIdent(CSSName.PAGE_BREAK_AFTER);
        return val == IdentValue.ALWAYS || val == IdentValue.LEFT
                || val == IdentValue.RIGHT;
    }

    public boolean isAvoidPageBreakInside() {
        return isIdent(CSSName.PAGE_BREAK_INSIDE, IdentValue.AVOID);
    }

    public CalculatedStyle createAnonymousStyle(IdentValue display) {
        return deriveStyle(CascadedStyle.createAnonymousStyle(display));
    }

    public boolean mayHaveFirstLine() {
        IdentValue display = getIdent(CSSName.DISPLAY);
        return display == IdentValue.BLOCK ||
                display == IdentValue.LIST_ITEM ||
                display == IdentValue.RUN_IN ||
                display == IdentValue.TABLE ||
                display == IdentValue.TABLE_CELL ||
                display == IdentValue.TABLE_CAPTION ||
                display == IdentValue.INLINE_BLOCK;
    }

    public boolean mayHaveFirstLetter() {
        IdentValue display = getIdent(CSSName.DISPLAY);
        return display == IdentValue.BLOCK ||
                display == IdentValue.LIST_ITEM ||
                display == IdentValue.TABLE_CELL ||
                display == IdentValue.TABLE_CAPTION ||
                display == IdentValue.INLINE_BLOCK;
    }

    public boolean isNonFlowContent() {
        return isFloated() || isAbsolute() || isFixed() || isRunning();
    }

    public boolean isMayCollapseMarginsWithChildren() {
        return isIdent(CSSName.OVERFLOW, IdentValue.VISIBLE) &&
                ! (isFloated() || isAbsolute() || isFixed() || isInlineBlock());
    }

    public boolean isAbsFixedOrInlineBlockEquiv() {
        return isAbsolute() || isFixed() || isInlineBlock() || isInlineTable();
    }

    public boolean isMaxWidthNone() {
        return isIdent(CSSName.MAX_WIDTH, IdentValue.NONE);
    }

    public boolean isMaxHeightNone() {
        return isIdent(CSSName.MAX_HEIGHT, IdentValue.NONE);
    }

    public int getMinWidth(CssContext c, int cbWidth) {
        return (int) getFloatPropertyProportionalTo(CSSName.MIN_WIDTH, cbWidth, c);
    }

    public int getMaxWidth(CssContext c, int cbWidth) {
        return (int) getFloatPropertyProportionalTo(CSSName.MAX_WIDTH, cbWidth, c);
    }

    public int getMinHeight(CssContext c, int cbHeight) {
        return (int) getFloatPropertyProportionalTo(CSSName.MIN_HEIGHT, cbHeight, c);
    }

    public int getMaxHeight(CssContext c, int cbHeight) {
        return (int) getFloatPropertyProportionalTo(CSSName.MAX_HEIGHT, cbHeight, c);
    }

    public boolean isCollapseBorders() {
        return isIdent(CSSName.BORDER_COLLAPSE, IdentValue.COLLAPSE) && ! isPaginateTable();
    }

    public int getBorderHSpacing(CssContext c) {
        return isCollapseBorders() ? 0 : (int) getFloatPropertyProportionalTo(CSSName.FS_BORDER_SPACING_HORIZONTAL, 0, c);
    }

    public int getBorderVSpacing(CssContext c) {
        return isCollapseBorders() ? 0 : (int) getFloatPropertyProportionalTo(CSSName.FS_BORDER_SPACING_VERTICAL, 0, c);
    }

    public int getRowSpan() {
        int result = (int) asFloat(CSSName.FS_ROWSPAN);
        return result > 0 ? result : 1;
    }

    public int getColSpan() {
        int result = (int) asFloat(CSSName.FS_COLSPAN);
        return result > 0 ? result : 1;
    }

    public Length asLength(CssContext c, CSSName cssName) {
        Length result = new Length();

        FSDerivedValue value = valueByName(cssName);
        if (value instanceof LengthValue || value instanceof NumberValue) {
            if (value.hasAbsoluteUnit()) {
                result.setValue((int) value.getFloatProportionalTo(cssName, 0, c));
                result.setType(Length.FIXED);
            } else {
                result.setValue((int) value.asFloat());
                result.setType(Length.PERCENT);
            }
        }

        return result;
    }

    public boolean isShowEmptyCells() {
        return isCollapseBorders() || isIdent(CSSName.EMPTY_CELLS, IdentValue.SHOW);
    }

    public boolean isHasBackground() {
        return ! (isIdent(CSSName.BACKGROUND_COLOR, IdentValue.TRANSPARENT) &&
                isIdent(CSSName.BACKGROUND_IMAGE, IdentValue.NONE));
    }

    public List getTextDecorations() {
        FSDerivedValue value = valueByName(CSSName.TEXT_DECORATION);
        if (value == IdentValue.NONE) {
            return null;
        } else {
            List idents = ((ListValue) value).getValues();
            List result = new ArrayList(idents.size());
            for (Iterator i = idents.iterator(); i.hasNext();) {
                result.add(DerivedValueFactory.newDerivedValue(
                        this, CSSName.TEXT_DECORATION, (PropertyValue) i.next()));
            }
            return result;
        }
    }

    public Cursor getCursor() {
        FSDerivedValue value = valueByName(CSSName.CURSOR);

        if (value == IdentValue.AUTO || value == IdentValue.DEFAULT) {
            return Cursor.getDefaultCursor();
        } else if (value == IdentValue.CROSSHAIR) {
            return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        } else if (value == IdentValue.POINTER) {
            return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        } else if (value == IdentValue.MOVE) {
            return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        } else if (value == IdentValue.E_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
        } else if (value == IdentValue.NE_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
        } else if (value == IdentValue.NW_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
        } else if (value == IdentValue.N_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
        } else if (value == IdentValue.SE_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
        } else if (value == IdentValue.SW_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
        } else if (value == IdentValue.S_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
        } else if (value == IdentValue.W_RESIZE) {
            return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
        } else if (value == IdentValue.TEXT) {
            return Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
        } else if (value == IdentValue.WAIT) {
            return Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        } else if (value == IdentValue.HELP) {
            // We don't have a cursor for this by default, maybe we need
            // a custom one for this (but I don't like it).
            return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        } else if (value == IdentValue.PROGRESS) {
            // We don't have a cursor for this by default, maybe we need
            // a custom one for this (but I don't like it).
            return Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        }

        return null;
    }

    public boolean isPaginateTable() {
        return isIdent(CSSName.FS_TABLE_PAGINATE, IdentValue.PAGINATE);
    }

    public boolean isTextJustify() {
        return isIdent(CSSName.TEXT_ALIGN, IdentValue.JUSTIFY) &&
                ! (isIdent(CSSName.WHITE_SPACE, IdentValue.PRE) ||
                        isIdent(CSSName.WHITE_SPACE, IdentValue.PRE_LINE));
    }

    public boolean isListMarkerInside() {
        return isIdent(CSSName.LIST_STYLE_POSITION, IdentValue.INSIDE);
    }

    public boolean isKeepWithInline() {
        return isIdent(CSSName.FS_KEEP_WITH_INLINE, IdentValue.KEEP);
    }

}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.110  2010/01/12 14:33:27  peterbrant
 * Ignore auto margins when calculating table min/max width.  Also, when deciding whether or not to proceed with the auto margin calculation for a table,  make sure we compare consistently with how the table min width is actually set.
 *
 * Revision 1.109  2009/11/08 23:52:48  peterbrant
 * Treat percentage widths as auto when calculating min/max widths
 *
 * Revision 1.108  2009/05/09 14:17:41  pdoubleya
 * FindBugs: static field should not be mutable; use inner class to declare CSS 4-side properties
 *
 * Revision 1.107  2009/04/25 10:48:42  pdoubleya
 * Small opt, don't pull Ident unless needed, patch from Peter Fassev issue #263
 *
 * Revision 1.106  2008/12/14 19:27:16  peterbrant
 * Always treat running elements as blocks
 *
 * Revision 1.105  2008/12/14 13:53:32  peterbrant
 * Implement -fs-keep-with-inline: keep property that instructs FS to try to avoid breaking a box so that only borders and padding appear on a page
 *
 * Revision 1.104  2008/09/06 18:21:50  peterbrant
 * Need to account for list-marker-position: inside when calculating inline min/max widths
 *
 * Revision 1.103  2008/08/01 22:23:54  peterbrant
 * Fix various bugs related to collapsed table borders (one manifestation found by Olly Headey)
 *
 * Revision 1.102  2008/07/27 00:21:48  peterbrant
 * Implement CMYK color support for PDF output, starting with patch from Mykola Gurov / Banish java.awt.Color from FS core layout classes
 *
 * Revision 1.101  2008/07/14 11:12:37  peterbrant
 * Fix two bugs when -fs-table-paginate is paginate.  Block boxes in cells in a <thead> that were also early on the page could be positioned incorrectly.  Line boxes contained within inline-block or inline-table content in a paginated table were generally placed incorrectly.
 *
 * Revision 1.100  2007/08/29 22:18:19  peterbrant
 * Experiment with text justification
 *
 * Revision 1.99  2007/08/27 19:44:06  peterbrant
 * Rename -fs-table-pagination to -fs-table-paginate
 *
 * Revision 1.98  2007/08/19 22:22:53  peterbrant
 * Merge R8pbrant changes to HEAD
 *
 * Revision 1.97.2.4  2007/08/17 23:53:32  peterbrant
 * Get rid of layer hack for overflow: hidden
 *
 * Revision 1.97.2.3  2007/08/16 22:38:48  peterbrant
 * Further progress on table pagination
 *
 * Revision 1.97.2.2  2007/07/11 22:48:29  peterbrant
 * Further progress on running headers and footers
 *
 * Revision 1.97.2.1  2007/07/09 22:18:04  peterbrant
 * Begin work on running headers and footers and named pages
 *
 * Revision 1.97  2007/06/26 18:24:52  peterbrant
 * Improve calculation of line-height: normal / If leading is positive, recalculate to center glyph area in inline box
 *
 * Revision 1.96  2007/06/19 22:31:46  peterbrant
 * Only cache all zeros margin, border, padding.  See discussion on bug #147 for more info.
 *
 * Revision 1.95  2007/06/14 22:39:31  tobega
 * Handling counters in LayoutContext instead. We still need to get the value from the counter function and change list-item counting.
 *
 * Revision 1.94  2007/06/13 14:16:00  peterbrant
 * Comment out body of resolveCounters() for now
 *
 * Revision 1.93  2007/06/12 22:59:23  tobega
 * Handling counters is done. Now we just need to get the values appropriately.
 *
 * Revision 1.92  2007/06/05 19:29:54  peterbrant
 * More progress on counter support
 *
 * Revision 1.91  2007/05/24 19:56:52  peterbrant
 * Add support for cursor property (predefined cursors only)
 *
 * Patch from Sean Bright
 *
 * Revision 1.90  2007/04/16 01:10:06  peterbrant
 * Vertical margin and padding with percentage values may be incorrect if box participated in a shrink-to-fit calculation.  Fix margin calculation.
 *
 * Revision 1.89  2007/04/14 20:09:39  peterbrant
 * Add method to clear cached rects
 *
 * Revision 1.88  2007/03/01 20:17:10  peterbrant
 * Tables with collapsed borders don't have padding
 *
 * Revision 1.87  2007/02/28 18:16:32  peterbrant
 * Support multiple values for text-decoration (per spec)
 *
 * Revision 1.86  2007/02/26 16:25:51  peterbrant
 * Method name change to avoid confusion with visibility: hidden / Don't create empty inline boxes after all (inline layout will ignore them anyway) / Robustness improvements to generated content (treat display: table/table row groups/table-row as regular block boxes)
 *
 * Revision 1.85  2007/02/24 00:46:38  peterbrant
 * Paint root element background over entire canvas (or it's first child if the root element doesn't define a background)
 *
 * Revision 1.84  2007/02/23 21:04:26  peterbrant
 * Implement complete support for background-position and background-attachment
 *
 * Revision 1.83  2007/02/23 16:54:38  peterbrant
 * Allow special ident -fs-intial-value to reset a property value to its initial value (used by border related shorthand properties as 'color' won't be known at property construction time)
 *
 * Revision 1.82  2007/02/22 18:21:20  peterbrant
 * Add support for overflow: visible/hidden
 *
 * Revision 1.81  2007/02/21 01:19:12  peterbrant
 * Need to take unit into account when creating Length objects with non-pixel units
 *
 * Revision 1.80  2007/02/20 20:05:40  peterbrant
 * Complete support for absolute and relative font sizes
 *
 * Revision 1.79  2007/02/20 16:11:11  peterbrant
 * Comment out references to CSSName.OVERFLOW
 *
 * Revision 1.78  2007/02/20 01:17:11  peterbrant
 * Start CSS parser cleanup
 *
 * Revision 1.77  2007/02/20 00:01:12  peterbrant
 * asColor() fix
 *
 * Revision 1.76  2007/02/19 23:18:43  peterbrant
 * Further work on new CSS parser / Misc. bug fixes
 *
 * Revision 1.75  2007/02/19 14:53:43  peterbrant
 * Integrate new CSS parser
 *
 * Revision 1.74  2007/02/07 16:33:28  peterbrant
 * Initial commit of rewritten table support and associated refactorings
 *
 * Revision 1.73  2007/01/16 16:11:38  peterbrant
 * Don't copy derived values as they propagate down the style tree (don't need to anymore
 * now that we don't cache length values in LengthValue and PointValue)
 *
 * Revision 1.72  2006/10/04 23:52:57  peterbrant
 * Implement support for margin: auto (centering blocks in their containing block)
 *
 * Revision 1.71  2006/09/06 22:21:43  peterbrant
 * Fixes to shrink-to-fit implementation / Implement min/max-width (non-replaced content) only
 *
 * Revision 1.70  2006/09/01 23:49:40  peterbrant
 * Implement basic margin collapsing / Various refactorings in preparation for shrink-to-fit / Add hack to treat auto margins as zero
 *
 * Revision 1.69  2006/08/29 17:29:14  peterbrant
 * Make Style object a thing of the past
 *
 * Revision 1.68  2006/08/27 00:36:16  peterbrant
 * Initial commit of (initial) R7 work
 *
 * Revision 1.67  2006/05/15 05:46:51  pdoubleya
 * Return value from abs value check never assigned!
 *
 * Revision 1.66  2006/05/08 21:24:24  pdoubleya
 * Log, don't throw exception, if we check for an absolute unit but it doesn't make sense to do so (IdentValue.hasAbsoluteUnit()).
 *
 * Revision 1.65  2006/05/08 20:56:09  pdoubleya
 * Clean exception handling for case where assigned property value is not understood as a valid value; use initial value instead.
 *
 * Revision 1.64  2006/02/21 19:30:34  peterbrant
 * Reset negative values for padding/border-width to 0
 *
 * Revision 1.63  2006/02/01 01:30:16  peterbrant
 * Initial commit of PDF work
 *
 * Revision 1.62  2006/01/27 01:15:44  peterbrant
 * Start on better support for different output devices
 *
 * Revision 1.61  2006/01/03 17:04:54  peterbrant
 * Many pagination bug fixes / Add ability to position absolute boxes in margin area
 *
 * Revision 1.60  2006/01/03 02:11:15  peterbrant
 * Expose asString() for all
 *
 * Revision 1.59  2006/01/01 02:38:22  peterbrant
 * Merge more pagination work / Various minor cleanups
 *
 * Revision 1.58  2005/12/13 02:41:36  peterbrant
 * Initial implementation of vertical-align: top/bottom (not done yet) / Minor cleanup and optimization
 *
 * Revision 1.57  2005/12/08 02:16:11  peterbrant
 * Thread safety fix
 *
 * Revision 1.56  2005/12/05 00:09:04  peterbrant
 * Couple of optimizations which improve layout speed by about 10%
 *
 * Revision 1.55  2005/11/25 16:57:26  peterbrant
 * Initial commit of inline content refactoring
 *
 * Revision 1.54  2005/11/10 22:15:41  peterbrant
 * Fix (hopefully) exception on identifiers which are converted by the CSS layer (e.g. thick becomes 3px)
 *
 * Revision 1.53  2005/11/08 22:53:44  tobega
 * added getLineHeight method to CalculatedStyle and hacked in some list-item support
 *
 * Revision 1.52  2005/10/31 22:43:15  tobega
 * Some memory optimization of the Matcher. Probably cleaner code, too.
 *
 * Revision 1.51  2005/10/31 19:02:12  pdoubleya
 * support for inherited padding and margins.
 *
 * Revision 1.50  2005/10/31 18:01:44  pdoubleya
 * InheritedLength is created per-length, to accomodate calls that need to defer to a specific parent.
 *
 * Revision 1.49  2005/10/31 12:38:14  pdoubleya
 * Additional inheritance fixes.
 *
 * Revision 1.48  2005/10/31 10:16:08  pdoubleya
 * Preliminary support for inherited lengths.
 *
 * Revision 1.47  2005/10/25 15:38:28  pdoubleya
 * Moved guessType() to ValueConstants, applied fix to method suggested by Chris Oliver, to avoid exception-based catch.
 *
 * Revision 1.46  2005/10/25 00:38:47  tobega
 * Reduced memory footprint of Matcher and stopped trying to cache the possibly uncache-able CascadedStyles, the fingerprint works just as well or better as a key in CalculatedStyle!
 *
 * Revision 1.45  2005/10/24 15:37:35  pdoubleya
 * Caching border, margin and property instances directly.
 *
 * Revision 1.44  2005/10/24 10:19:40  pdoubleya
 * CSSName FS_ID is now public and final, allowing direct access to the id, bypassing getAssignedID(); micro-optimization :); getAssignedID() and setAssignedID() have been removed. IdentValue string property is also final (as should have been).
 *
 * Revision 1.43  2005/10/22 22:58:15  peterbrant
 * Box level restyle works again (really this time!)
 *
 * Revision 1.42  2005/10/21 23:51:48  peterbrant
 * Rollback ill-advised change in revision 1.40
 *
 * Revision 1.41  2005/10/21 23:11:26  pdoubleya
 * Store key for margin, border and padding in each style instance, was re-creating on each call.
 *
 * Revision 1.40  2005/10/21 23:04:02  peterbrant
 * Make box level restyle work again
 *
 * Revision 1.39  2005/10/21 18:49:46  pdoubleya
 * Fixed border painting bug.
 *
 * Revision 1.38  2005/10/21 18:14:59  pdoubleya
 * set  initial capacity for cached rects.
 *
 * Revision 1.37  2005/10/21 18:10:50  pdoubleya
 * Support for cachable borders. Still buggy on some pages, but getting there.
 *
 * Revision 1.36  2005/10/21 13:02:20  pdoubleya
 * Changed to cache padding in RectPropertySet.
 *
 * Revision 1.35  2005/10/21 12:20:04  pdoubleya
 * Added array for margin side props.
 *
 * Revision 1.34  2005/10/21 12:16:18  pdoubleya
 * Removed use of MarginPropertySet; using RectPS  now.
 *
 * Revision 1.33  2005/10/21 12:01:13  pdoubleya
 * Added cachable rect property for margin, cleanup minor in styling.
 *
 * Revision 1.32  2005/10/21 10:02:54  pdoubleya
 * Cleanup, removed unneeded vars, reorg code in CS.
 *
 * Revision 1.31  2005/10/20 20:48:01  pdoubleya
 * Updates for refactoring to style classes. CalculatedStyle now has lookup methods to cover all general cases, so propertyByName() is private, which means the backing classes for styling were able to be replaced.
 *
 * Revision 1.30  2005/10/03 23:44:43  tobega
 * thread-safer css code and improved style caching
 *
 * Revision 1.29  2005/09/11 20:43:15  tobega
 * Fixed table-css interaction bug, colspan now works again
 *
 * Revision 1.28  2005/07/20 22:47:33  joshy
 * fix for 94, percentage for top absolute position
 *
 * Revision 1.27  2005/06/22 23:48:41  tobega
 * Refactored the css package to allow a clean separation from the core.
 *
 * Revision 1.26  2005/06/21 08:23:13  pdoubleya
 * Added specific list and count of primitive, non shorthand properties, and CalculatedStyle now sizes array to this size.
 *
 * Revision 1.25  2005/06/16 07:24:46  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.24  2005/06/03 23:06:21  tobega
 * Now uses value of "color" as initial value for "border-color" and rgb-triples are supported
 *
 * Revision 1.23  2005/06/01 00:47:02  tobega
 * Partly confused hack trying to get width and height working properly for replaced elements.
 *
 * Revision 1.22  2005/05/29 16:38:58  tobega
 * Handling of ex values should now be working well. Handling of em values improved. Is it correct?
 * Also started defining dividing responsibilities between Context and RenderingContext.
 *
 * Revision 1.21  2005/05/13 11:49:57  tobega
 * Started to fix up borders on inlines. Got caught up in refactoring.
 * Boxes shouldn't cache borders and stuff unless necessary. Started to remove unnecessary references.
 * Hover is not working completely well now, might get better when I'm done.
 *
 * Revision 1.20  2005/05/09 20:35:38  tobega
 * Caching fonts in CalculatedStyle
 *
 * Revision 1.19  2005/05/08 15:37:28  tobega
 * Fixed up style caching so it really works (internalize CascadedStyles and let each CalculatedStyle keep track of its derived children)
 *
 * Revision 1.18  2005/05/08 14:51:21  tobega
 * Removed the need for the Styler
 *
 * Revision 1.17  2005/05/08 14:36:54  tobega
 * Refactored away the need for having a context in a CalculatedStyle
 *
 * Revision 1.16  2005/04/07 16:33:34  pdoubleya
 * Fix border width if set to "none" in CSS (Kevin).
 *
 * Revision 1.15  2005/03/24 23:16:33  pdoubleya
 * Added use of SharedContext (Kevin).
 *
 * Revision 1.14  2005/02/03 23:15:50  pdoubleya
 * .
 *
 * Revision 1.13  2005/01/29 20:22:20  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.12  2005/01/25 12:46:12  pdoubleya
 * Refactored duplicate code into separate method.
 *
 * Revision 1.11  2005/01/24 22:46:43  pdoubleya
 * Added support for ident-checks using IdentValue instead of string comparisons.
 *
 * Revision 1.10  2005/01/24 19:01:05  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.9  2005/01/24 14:36:31  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.8  2004/12/05 18:11:36  tobega
 * Now uses style cache for pseudo-element styles. Also started preparing to replace inline node handling with inline content handling.
 *
 * Revision 1.7  2004/12/05 00:48:54  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.6  2004/11/15 12:42:23  pdoubleya
 * Across this checkin (all may not apply to this particular file)
 * Changed default/package-access members to private.
 * Changed to use XRRuntimeException where appropriate.
 * Began move from System.err.println to std logging.
 * Standard code reformat.
 * Removed some unnecessary SAC member variables that were only used in initialization.
 * CVS log section.
 *
 *
 */

