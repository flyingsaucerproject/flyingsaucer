/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
 * }}}
 */
package org.xhtmlrenderer.css.parser;

import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.util.ArrayUtil;

public class PropertyValue implements CSSPrimitiveValue {
    public static final short VALUE_TYPE_NUMBER = 1;
    public static final short VALUE_TYPE_LENGTH = 2;
    public static final short VALUE_TYPE_COLOR = 3;
    public static final short VALUE_TYPE_IDENT = 4;
    public static final short VALUE_TYPE_STRING = 5;
    public static final short VALUE_TYPE_LIST = 6;
    public static final short VALUE_TYPE_FUNCTION = 7;
    
    private short _type;
    private short _cssValueType;
    
    private String _stringValue;
    private float _floatValue;
    private String[] _stringArrayValue;
    
    private String _cssText;
    
    private FSColor _FSColor;
    
    private IdentValue _identValue;
    
    private short _propertyValueType;
    
    private Token _operator;
    
    private List _values;
    private FSFunction _function;

    public PropertyValue(short type, float floatValue, String cssText) {
        _type = type;
        _floatValue = floatValue;
        _cssValueType = CSSValue.CSS_PRIMITIVE_VALUE;
        _cssText = cssText;
        
        if (type == CSSPrimitiveValue.CSS_NUMBER && floatValue != 0.0f) {
            _propertyValueType = VALUE_TYPE_NUMBER;
        } else {
            _propertyValueType = VALUE_TYPE_LENGTH;
        }
    }
    
    public PropertyValue(FSColor color) {
        _type = CSSPrimitiveValue.CSS_RGBCOLOR;
        _cssValueType = CSSValue.CSS_PRIMITIVE_VALUE;
        _cssText = color.toString();
        _FSColor = color;
        
        _propertyValueType = VALUE_TYPE_COLOR;
    }
    
    public PropertyValue(short type, String stringValue, String cssText) {
        _type = type;
        _stringValue = stringValue;
        // Must be a case-insensitive compare since ident values aren't normalized
        // for font and font-family
        _cssValueType = _stringValue.equalsIgnoreCase("inherit") ? CSSValue.CSS_INHERIT : CSSValue.CSS_PRIMITIVE_VALUE;
        _cssText = cssText;
        
        if (type == CSSPrimitiveValue.CSS_IDENT) {
            _propertyValueType = VALUE_TYPE_IDENT;
        } else {
            _propertyValueType = VALUE_TYPE_STRING;
        }
    }
    
    public PropertyValue(IdentValue ident) {
        _type = CSSPrimitiveValue.CSS_IDENT;
        _stringValue = ident.toString();
        _cssValueType = _stringValue.equals("inherit") ? CSSValue.CSS_INHERIT : CSSValue.CSS_PRIMITIVE_VALUE;
        _cssText = ident.toString();
        
        _propertyValueType = VALUE_TYPE_IDENT;
        _identValue = ident;
    }
    
    public PropertyValue(List values) {
        _type = CSSPrimitiveValue.CSS_UNKNOWN; // HACK
        _cssValueType = CSSValue.CSS_CUSTOM;
        _cssText = values.toString(); // HACK
        
        _values = values;
        _propertyValueType = VALUE_TYPE_LIST;
    }
    
    public PropertyValue(FSFunction function) {
        _type = CSSPrimitiveValue.CSS_UNKNOWN;
        _cssValueType = CSSValue.CSS_CUSTOM;
        _cssText = function.toString();
        
        _function = function;
        _propertyValueType = VALUE_TYPE_FUNCTION;
    }

    public Counter getCounterValue() throws DOMException {
        throw new UnsupportedOperationException();
    }

    public float getFloatValue(short unitType) throws DOMException {
        return _floatValue;
    }
    
    public float getFloatValue() {
        return _floatValue;
    }

    public short getPrimitiveType() {
        return _type;
    }

    public RGBColor getRGBColorValue() throws DOMException {
        throw new UnsupportedOperationException();
    }

    public Rect getRectValue() throws DOMException {
        throw new UnsupportedOperationException();
    }

    public String getStringValue() throws DOMException {
        return _stringValue;
    }

    public void setFloatValue(short unitType, float floatValue) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public void setStringValue(short stringType, String stringValue) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public String getCssText() {
        return _cssText;
    }

    public short getCssValueType() {
        return _cssValueType;
    }

    public void setCssText(String cssText) throws DOMException {
        throw new UnsupportedOperationException();
    }
    
    public FSColor getFSColor() {
        return _FSColor;
    }

    public IdentValue getIdentValue() {
        return _identValue;
    }

    public void setIdentValue(IdentValue identValue) {
        _identValue = identValue;
    }
    
    public short getPropertyValueType() {
        return _propertyValueType;
    }

    public Token getOperator() {
        return _operator;
    }

    public void setOperator(Token operator) {
        _operator = operator;
    }

    public String[] getStringArrayValue() {
        return ArrayUtil.cloneOrEmpty(_stringArrayValue);
    }

    public void setStringArrayValue(String[] stringArrayValue) {
        _stringArrayValue = ArrayUtil.cloneOrEmpty(stringArrayValue);
    }
    
    public String toString() {
        return _cssText;
    }
    
    public List getValues() {
        return new ArrayList(_values);
    }
    
    public FSFunction getFunction() {
        return _function;
    }
    
    public String getFingerprint() {
        if (getPropertyValueType() == VALUE_TYPE_IDENT) {
            if (_identValue == null) {
                _identValue = IdentValue.getByIdentString(getStringValue());
            }
            return "I" + _identValue.FS_ID;
        } else {
            return getCssText();
        }
    }
}
