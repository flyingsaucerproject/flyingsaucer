package org.xhtmlrenderer.css.style.derived;

import org.xhtmlrenderer.css.style.*;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.ValueConstants;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.util.regex.Matcher;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Oct 17, 2005
 * Time: 2:09:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class PointValue extends DerivedValue {
    private float _xPos;
    private short _xType;
    private float _yPos;
    private short _yType;
    private boolean _isAbsolute;
    private Point _point;

    public PointValue (
            CalculatedStyle style,
            CSSName name,
            short cssSACUnitType,
            String cssText,
            String cssStringValue
    ) {
        super(style, name, cssSACUnitType, cssText, cssStringValue);
        if ( name == CSSName.BACKGROUND_POSITION ) {
            pullPointValuesForBGPos(cssText);
        }
    }

    public FSDerivedValue copyOf() {
        return new PointValue(getStyle(), getCssName(), getCssSacUnitType(), getStringValue(), getStringValue());
    }

    /**
     * @param parentWidth
     * @param parentHeight
     * @param ctx
     * @return
     */
    public Point asPoint(
            float parentWidth,
            float parentHeight,
            CssContext ctx
    ) {
        Point pt = null;
        if (_isAbsolute) {
            // It's an absolute value, so only calculate it once
            if (_point == null) {
                _point = new Point();
                float xF = LengthValue.calcFloatProportionalValue(getStyle(), getCssName(), getStringValue(), _xPos, _xType, parentWidth, ctx);
                float yF = LengthValue.calcFloatProportionalValue(getStyle(), getCssName(), getStringValue(), _yPos, _yType, parentHeight, ctx);
                _point.setLocation(xF, yF);
            }
            pt = _point;
        } else {
            pt = new Point();
            float xF = LengthValue.calcFloatProportionalValue(getStyle(), getCssName(), getStringValue(), _xPos, _xType, parentWidth, ctx);
            float yF = LengthValue.calcFloatProportionalValue(getStyle(), getCssName(), getStringValue(), _yPos, _yType, parentHeight, ctx);
            pt.setLocation(xF, yF);
        }
        return pt;
    }

    /**
     * This method extracts the two values from the background-position
     * assignment. It tries to resolve them if both values are absolute, but if
     * proportional, this is deferred until the Point is requested. We pull
     * immediately because it's a small String operation that would be silly to
     * reproduce on each request.
     * <p/>
     * precondition: the value has been canonicalized to a _point. Handled by {@link org.xhtmlrenderer.css.sheet.factory.BackgroundPositionPropertyDeclarationFactory}
     *
     * @param cssText The CSS text for the property
     */
    private void pullPointValuesForBGPos(String cssText) {
        String[] pos = cssText.split(" ");
        try {
            Matcher m = LengthValue.getLengthMatcher(pos[0]);
            m.matches();
            String xAsString = m.group(1);
            _xPos = new Float(xAsString).floatValue();
            _xType = ValueConstants.sacPrimitiveTypeForString(m.group(3));

            m = LengthValue.getLengthMatcher(pos[1]);
            m.matches();
            String yAsString = m.group(1);
            _yPos = new Float(yAsString).floatValue();
            _yType = ValueConstants.sacPrimitiveTypeForString(m.group(3));

            if (ValueConstants.isAbsoluteUnit(_xType) && ValueConstants.isAbsoluteUnit(_yType)) {
                _isAbsolute = true;
            } else {
                _isAbsolute = false;
            }
        } catch (Exception ex) {
            StringBuffer msg = new StringBuffer();
            msg.append("background-position: failed to convert '" + cssText + "' into a Point. ");
            msg.append("Property value (as text) was split into " + pos.length + " values for positioning. ");
            if (pos.length >= 1) {
                msg.append(" background-position x-pos is " + pos[0]);
            }
            if (pos.length == 2) {
                msg.append(" background-position y-pos is " + pos[1]);
            }
            throw new XRRuntimeException(msg.toString());
        }
    }
}
