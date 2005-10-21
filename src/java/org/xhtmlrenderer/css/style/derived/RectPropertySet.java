package org.xhtmlrenderer.css.style.derived;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;


/**
 * Represents a set of CSS properties that together define
 * some rectangular area, and per-side thickness.
 */
public class RectPropertySet {
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
            float parentHeight,
            float parentWidth,
            CssContext ctx
    ) {
        RectPropertySet rect =
                new RectPropertySet(
                        shortHandProperty,
                        style.getFloatPropertyProportionalHeight(sideProperties[0], parentHeight, ctx),
                        style.getFloatPropertyProportionalWidth(sideProperties[1], parentWidth, ctx),
                        style.getFloatPropertyProportionalHeight(sideProperties[2], parentHeight, ctx),
                        style.getFloatPropertyProportionalWidth(sideProperties[3], parentWidth, ctx)
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
        for (int i = 0; i < sideProperties.length && isAbs; i++) {
            isAbs = style.hasAbsoluteUnit(sideProperties[i]);

        }
        if (isAbs) {
            key = new StringBuffer()
                    .append("side-rect : ")
                    .append(style.asFloat(sideProperties[0]) + "px ")
                    .append(style.asFloat(sideProperties[1]) + "px ")
                    .append(style.asFloat(sideProperties[2]) + "px ")
                    .append(style.asFloat(sideProperties[3]) + "px")
                    .append(";")
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
