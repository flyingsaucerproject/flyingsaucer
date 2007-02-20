/*
 * {{{ header & license
 * Copyright (c) 2005 Patrick Wright
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
package org.xhtmlrenderer.css.style.derived;

import java.awt.Point;
import java.util.List;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.ValueConstants;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.DerivedValue;

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
    
    public PointValue(CalculatedStyle style, CSSName name, PropertyValue value) {
        super(name, value.getPrimitiveType(), value.getCssText(), value.getCssText());
        
        _style = style;
        
        List values = (List)value.getValues();
        
        PropertyValue x = (PropertyValue)values.get(0);
        PropertyValue y = (PropertyValue)values.get(1);
        
        _xPos = x.getFloatValue();
        _xType = x.getPrimitiveType();
        
        _yPos = y.getFloatValue();
        _yType = y.getPrimitiveType();
        
        if (ValueConstants.isAbsoluteUnit(_xType) && ValueConstants.isAbsoluteUnit(_yType)) {
            _isAbsolute = true;
        } else {
            _isAbsolute = false;
        }
    }

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

    private CalculatedStyle getStyle() {
        return _style;
    }
}
