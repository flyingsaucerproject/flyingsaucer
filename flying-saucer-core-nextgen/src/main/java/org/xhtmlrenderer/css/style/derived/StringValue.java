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

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.style.DerivedValue;
import org.xhtmlrenderer.util.ArrayUtil;

public class StringValue extends DerivedValue {
    private String[] _stringAsArray;

    public StringValue(CSSName name, PropertyValue value) {
        super(name, value.getPrimitiveType(), value.getCssText(), value.getStringValue());
        if (value.getStringArrayValue() != null) {
            _stringAsArray = value.getStringArrayValue();
        }
    }

    public String[] asStringArray() {
        return ArrayUtil.cloneOrEmpty(_stringAsArray);
    }

    public String toString() {
        return getStringValue();
    }
}
