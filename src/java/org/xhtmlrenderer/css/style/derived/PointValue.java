package org.xhtmlrenderer.css.style.derived;

import java.awt.Point;
import java.util.List;
import java.util.regex.Matcher;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.ValueConstants;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.DerivedValue;
import org.xhtmlrenderer.util.XRRuntimeException;

/**
 * A DerivedValue representing a point in space; used for background-position.
 * Implements asPoint() to retrieve the calculated Point value for location; must
 * have determined parent width and height first (because location can be dependent
 * on those).
 */
public class PointValue extends DerivedValue {
    private float _xPos;
    private short _xType;
    private float _yPos;
    private short _yType;
    private boolean _isAbsolute;
    private Point _point;
    
    private CalculatedStyle _style;

    public PointValue (
            CalculatedStyle style,
            CSSName name,
            short cssSACUnitType,
            String cssText,
            String cssStringValue
    ) {
        super(name, cssSACUnitType, cssText, cssStringValue);
        _style = style;
        if ( name == CSSName.BACKGROUND_POSITION ) {
            pullPointValuesForBGPos(cssText);
        }
    }
    
    public PointValue(CalculatedStyle style, CSSName name, PropertyValue value) {
        super(name, value.getPrimitiveType(), value.getCssText(), value.getCssText());
        
        List values = (List)value.getValues();
        
        PropertyValue x = (PropertyValue)values.get(0);
        PropertyValue y = (PropertyValue)values.get(1);
        
        _xPos = x.getFloatValue();
        _xType = x.getPrimitiveType();
        
        _yPos = y.getFloatValue();
        _yType = y.getPrimitiveType();
    }

    /**
     * @param parentWidth
     * @param parentHeight
     * @param ctx
     */
    public Point asPoint(
            CSSName cssName,
            float parentWidth,
            float parentHeight,
            CssContext ctx
    ) {
        Point pt = null;
        if (_isAbsolute) {
            // It's an absolute value, so only calculate it once
            if (_point == null) {
                _point = new Point();
                float xF = LengthValue.calcFloatProportionalValue(getStyle(), cssName, getStringValue(), _xPos, _xType, parentWidth, ctx);
                float yF = LengthValue.calcFloatProportionalValue(getStyle(), cssName, getStringValue(), _yPos, _yType, parentHeight, ctx);
                _point.setLocation(xF, yF);
            }
            pt = _point;
        } else {
            pt = new Point();
            float xF = LengthValue.calcFloatProportionalValue(getStyle(), cssName, getStringValue(), _xPos, _xType, parentWidth, ctx);
            float yF = LengthValue.calcFloatProportionalValue(getStyle(), cssName, getStringValue(), _yPos, _yType, parentHeight, ctx);
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
     * precondition: the value has been canonicalized to a _point.
     * Handled by {@link org.xhtmlrenderer.css.sheet.factory.BackgroundPositionPropertyDeclarationFactory}
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
            throw new XRRuntimeException(msg.toString(), ex);
        }
    }
    
    private CalculatedStyle getStyle() {
        return _style;
    }
}
