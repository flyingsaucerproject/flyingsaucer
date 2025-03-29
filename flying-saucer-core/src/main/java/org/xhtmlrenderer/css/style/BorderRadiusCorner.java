package org.xhtmlrenderer.css.style;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.style.derived.LengthValue;
import org.xhtmlrenderer.css.style.derived.ListValue;

public class BorderRadiusCorner {

    public static final BorderRadiusCorner UNDEFINED = new BorderRadiusCorner(0, 0);

    private record Length(float value, boolean percent) {}

    private Length _left;
    private Length _right;

    // TODO: FIXME the way values are passed from the CSS to the border corners really sucks, improve it

    public BorderRadiusCorner(float left, float right) {
        this._left = new Length(left, false);
        this._right = new Length(right, false);
    }

    public BorderRadiusCorner(CSSName fromVal, CalculatedStyle style, CssContext ctx) {
        FSDerivedValue value = style.valueByName(fromVal);
        if (value instanceof ListValue lValues) {
            PropertyValue first = (PropertyValue) lValues.getValues().get(0);
            PropertyValue second = lValues.getValues().size() > 1 ? (PropertyValue) lValues.getValues().get(1) : first;

            if (fromVal.equals(CSSName.BORDER_TOP_LEFT_RADIUS) || fromVal.equals(CSSName.BORDER_BOTTOM_RIGHT_RADIUS)) {
                setRight(fromVal, style, first, ctx);
                setLeft(fromVal, style, second, ctx);
            } else {
                setLeft(fromVal, style, first, ctx);
                setRight(fromVal, style, second, ctx);
            }
        } else if (value instanceof LengthValue lv) {

            if (lv.getStringValue().contains("%")) {
                _left = _right = new Length(value.asFloat() / 100.0f, true);
            } else {
                _left = _right = new Length((int) lv.getFloatProportionalTo(fromVal, 0, ctx), false);
            }
        }
    }

    private void setLeft(CSSName fromVal, CalculatedStyle style, PropertyValue value, CssContext ctx) {
        if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
            _left = new Length(value.getFloatValue() / 100.0f, true);
        } else {
            _left = new Length((int) LengthValue.calcFloatProportionalValue(
                style,
                fromVal,
                value.getCssText(),
                value.getFloatValue(),
                value.getPrimitiveType(),
                0,
                ctx), false);
        }
    }

    private void setRight(CSSName fromVal, CalculatedStyle style, PropertyValue value, CssContext ctx) {
        if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
            _right = new Length(value.getFloatValue() / 100.0f, true);
        } else {
            _right = new Length((int) LengthValue.calcFloatProportionalValue(
                style,
                fromVal,
                value.getCssText(),
                value.getFloatValue(),
                value.getPrimitiveType(),
                0,
                ctx), false);
        }
    }

    public boolean hasRadius() {
        return _left.value() > 0 || _right.value() > 0;
    }

    public float getMaxLeft(float max) {
        if (_left.percent())
            return max * _left.value();
        return Math.min(_left.value(), max);
    }

    public float getMaxRight(float max) {
        if (_right.percent())
            return max * _right.value();
        return Math.min(_right.value(), max);
    }


    public float left() {
        return _left.value();
    }

    public float right() {
        return _right.value();
    }
}
