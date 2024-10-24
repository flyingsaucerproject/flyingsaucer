package org.xhtmlrenderer.css.style;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.style.derived.LengthValue;
import org.xhtmlrenderer.css.style.derived.ListValue;

public class BorderRadiusCorner {

    public static final BorderRadiusCorner UNDEFINED = new BorderRadiusCorner();

    private boolean _leftPercent = false;
    private boolean _rightPercent = false;
    private float _left;
    private float _right;

    // TODO: FIXME the way values are passed from the CSS to the border corners really sucks, improve it

    public BorderRadiusCorner() {
    }
    public BorderRadiusCorner(float left, float right) {
        this._left = left;
        this._right = right;
    }
    public BorderRadiusCorner(CSSName fromVal, CalculatedStyle style, CssContext ctx) {
        FSDerivedValue value = style.valueByName(fromVal);
        if (value instanceof ListValue lValues) {
            PropertyValue first = (PropertyValue)lValues.getValues().get(0);
            PropertyValue second = lValues.getValues().size() > 1 ?
                    (PropertyValue) lValues.getValues().get(1) : first;

            if (fromVal.equals(CSSName.BORDER_TOP_LEFT_RADIUS) || fromVal.equals(CSSName.BORDER_BOTTOM_RIGHT_RADIUS)) {
                setRight(fromVal, style, first, ctx);
                setLeft(fromVal, style, second, ctx);
                //_left = style.getFloatPropertyProportionalHeight(fromVal, 0, ctx);
                //_right = style.getFloatPropertyProportionalWidth(fromVal, 0, ctx);
            } else {
                setLeft(fromVal, style, first, ctx);
                setRight(fromVal, style, second, ctx);
                //_right = style.getFloatPropertyProportionalHeight(fromVal, 0, ctx);
                //_left = style.getFloatPropertyProportionalWidth(fromVal, 0, ctx);
            }
        } else if (value instanceof LengthValue lv){

            if(lv.getStringValue().contains("%")) {
                _leftPercent = _rightPercent = true;
                _left = _right = value.asFloat() / 100.0f;
            } else {
                _left = _right = (int) lv.getFloatProportionalTo(fromVal, 0, ctx);
            }
            //first = second = (LengthValue)value;
        }
            //System.out.println(fromVal + " " + _left + " " + _right);
            //this._right = this._left = ((PropertyValue)((ListValue)fromVal).getValues().get(0)).getFloatValue();
        /*} else if(fromVal instanceof LengthValue) {
            this._left = fromVal.asFloat();
            this._right = fromVal.asFloat();
        }*/

    }

    private void setLeft(CSSName fromVal, CalculatedStyle style, PropertyValue value, CssContext ctx) {
        if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
            _leftPercent = true;
            _left = value.getFloatValue() / 100.0f;
        } else {
            _left = (int)LengthValue.calcFloatProportionalValue(
                    style,
                    fromVal,
                    value.getCssText(),
                    value.getFloatValue(),
                    value.getPrimitiveType(),
                    0,
                    ctx);
        }
    }
    private void setRight(CSSName fromVal, CalculatedStyle style, PropertyValue value, CssContext ctx) {
        if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
            _rightPercent = true;
            _right = value.getFloatValue() / 100.0f;
        } else {
            _right = (int)LengthValue.calcFloatProportionalValue(
                    style,
                    fromVal,
                    value.getCssText(),
                    value.getFloatValue(),
                    value.getPrimitiveType(),
                    0,
                    ctx);
        }
    }

    public boolean hasRadius() {
        return _left > 0 || _right > 0;
    }

    public float getMaxLeft(float max) {
        if (_leftPercent)
            return max * _left;
        return Math.min(_left, max);
    }

    public float getMaxRight(float max) {
        if (_rightPercent)
            return max * _right;
        return Math.min(_right, max);
    }


    public float left() {
        return _left;
    }
    public float right() {
        return _right;
    }

}
