package org.xhtmlrenderer.css.style.derived;

import org.xhtmlrenderer.css.constants.CSSName;


/**
 * Represents a set of CSS properties that together define
 * some rectangular area, and per-side thickness.
 */
public abstract class RectPropertySet {
    protected String _key;
    protected float _top;
    protected float _right;
    protected float _bottom;
    protected float _left;
    protected CSSName _cssName;

    protected RectPropertySet() {
        _top = _right = _bottom = _left = 0f;
    }

    public RectPropertySet(CSSName cssName) {
        this();
        this._cssName = cssName;
    }

    public String toString() {
        return getPropertyIdentifier();
    }

    public abstract RectPropertySet copyOf();

    public float getTopWidth() {
        return _top;
    }

    public float getRightWidth() {
        return _right;
    }

    public float getBottomWidth() {
        return _bottom;
    }

    public float getLeftWidth() {
        return _left;
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
}
