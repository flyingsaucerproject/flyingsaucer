package org.xhtmlrenderer.css.style.derived;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;


/**
 * Represents a set of CSS properties that together define
 * some rectangular area, and per-side thickness.
 */
public class RectPropertySet {
    //                                                                  HACK
    public static final RectPropertySet ALL_ZEROS = new RectPropertySet(CSSName.MARGIN_SHORTHAND, 0, 0, 0, 0);
    
    protected String _key;
    protected float _top;
    protected float _right;
    protected float _bottom;
    protected float _left;

    protected RectPropertySet() {
        _top = _right = _bottom = _left = 0f;
    }

    public RectPropertySet(
            CSSName cssName,
            float top,
            float right,
            float bottom,
            float left
    ) {
        this();
        this._top = top;
        this._right = right;
        this._bottom = bottom;
        this._left = left;
        this.buildKey(cssName);
    }

    public static RectPropertySet newInstance(
            CalculatedStyle style,
            CSSName shortHandProperty,
            CSSName[] sideProperties,
            float cbWidth,
            CssContext ctx
    ) {
        // HACK isLengthValue is part of margin auto hack
        RectPropertySet rect =
                new RectPropertySet(
                        shortHandProperty,
                        ! style.isLengthOrNumber(sideProperties[0]) ? 0 : style.getFloatPropertyProportionalHeight(sideProperties[0], cbWidth, ctx),
                        ! style.isLengthOrNumber(sideProperties[1]) ? 0 : style.getFloatPropertyProportionalWidth(sideProperties[1], cbWidth, ctx),
                        ! style.isLengthOrNumber(sideProperties[2]) ? 0 : style.getFloatPropertyProportionalHeight(sideProperties[2], cbWidth, ctx),
                        ! style.isLengthOrNumber(sideProperties[3]) ? 0 : style.getFloatPropertyProportionalWidth(sideProperties[3], cbWidth, ctx)
                );
        return rect;
    }

    public String toString() {
        return getPropertyIdentifier();
    }

    public float top() {
        return _top;
    }

    public float right() {
        return _right;
    }

    public float bottom() {
        return _bottom;
    }

    public float left() {
        return _left;
    }

    public float getLeftRightDiff() {
        return _left - _right;
    }

    public float height() {
        return _top + _bottom;
    }

    public float width() {
        return _left + _right;
    }

    public String getPropertyIdentifier() {
        return _key;
    }

    public void setTop(float _top) {
        this._top = _top;
    }

    public void setRight(float _right) {
        this._right = _right;
    }

    public void setBottom(float _bottom) {
        this._bottom = _bottom;
    }

    public void setLeft(float _left) {
        this._left = _left;
    }

    public static String deriveKey(
            CalculatedStyle style,
            CSSName[] sideProperties
    ) {
        String key = null;
        boolean isAbs = true;
        // HACK isLengthValue is part of margin auto hack
        for (int i = 0; i < sideProperties.length && isAbs; i++) {
            isAbs = style.isLengthOrNumber(sideProperties[i]) && style.hasAbsoluteUnit(sideProperties[i]);
        }
        
        if (isAbs) {
            key = new StringBuffer()
                    .append(style.asString(sideProperties[0]))
                    .append(style.asString(sideProperties[1]))
                    .append(style.asString(sideProperties[2]))
                    .append(style.asString(sideProperties[3]))
                    .toString();
        }
        return key;
    }

    public RectPropertySet copyOf() {
        RectPropertySet newRect = new RectPropertySet();
        newRect._top = _top;
        newRect._right = _right;
        newRect._bottom = _bottom;
        newRect._left = _left;
        return newRect;
    }

    protected void buildKey(CSSName name) {
        this._key = new StringBuffer()
                .append(name.toString() + ": ")
                .append(_top + "px ")
                .append(_right + "px ")
                .append(_bottom + "px ")
                .append(_left + "px ")
                .append(";")
                .toString();
    }
}
