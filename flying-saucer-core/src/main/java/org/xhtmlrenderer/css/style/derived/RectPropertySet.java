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
    }

    public static RectPropertySet newInstance(
            CalculatedStyle style,
            CSSName shortHandProperty,
            CSSName.CSSSideProperties sideProperties,
            float cbWidth,
            CssContext ctx
    ) {
        // HACK isLengthValue is part of margin auto hack
        RectPropertySet rect =
                new RectPropertySet(
                        shortHandProperty,
                        ! style.isLengthOrNumber(sideProperties.top) ? 0 : style.getFloatPropertyProportionalHeight(sideProperties.top, cbWidth, ctx),
                        ! style.isLengthOrNumber(sideProperties.right) ? 0 : style.getFloatPropertyProportionalWidth(sideProperties.right, cbWidth, ctx),
                        ! style.isLengthOrNumber(sideProperties.bottom) ? 0 : style.getFloatPropertyProportionalHeight(sideProperties.bottom, cbWidth, ctx),
                        ! style.isLengthOrNumber(sideProperties.left) ? 0 : style.getFloatPropertyProportionalWidth(sideProperties.left, cbWidth, ctx)
                );
        return rect;
    }

    public String toString() {
        return "RectPropertySet[top=" + _top + ",right=" + _right + ",bottom=" + _bottom + ",left=" + _left + "]";
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

    public RectPropertySet copyOf() {
        RectPropertySet newRect = new RectPropertySet();
        newRect._top = _top;
        newRect._right = _right;
        newRect._bottom = _bottom;
        newRect._left = _left;
        return newRect;
    }
    
    public boolean isAllZeros() {
        return _top == 0.0f && _right == 0.0f && _bottom == 0.0f && _left == 0.0f;
    }
    
    public boolean hasNegativeValues() {
        return _top < 0 || _right < 0 || _bottom < 0 || _left < 0;
    }
    
    public void resetNegativeValues() {
        if (top() < 0) {
            setTop(0);
        }
        if (right() < 0) {
            setRight(0);
        }
        if (bottom() < 0) {
            setBottom(0);
        }
        if (left() < 0) {
            setLeft(0);
        }
    }
}
