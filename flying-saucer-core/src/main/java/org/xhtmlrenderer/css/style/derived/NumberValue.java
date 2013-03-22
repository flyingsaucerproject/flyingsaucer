/*
 * Copyright (c) 2005 Torbjoern Gannholm
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.css.style.derived;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.DerivedValue;

public class NumberValue extends DerivedValue {
    private float _floatValue;

    public NumberValue(CSSName cssName, PropertyValue value) {
        super(cssName, value.getPrimitiveType(), value.getCssText(), value.getCssText());
        _floatValue = value.getFloatValue();
    }

    public float asFloat() {
        return _floatValue;
    }
    
    public float getFloatProportionalTo(CSSName cssName, float baseValue, CssContext ctx) {
        return _floatValue;
    }
    
    public boolean hasAbsoluteUnit() {
        return true;
    }
}

/*
 * $Id$
 */
