/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
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
package org.xhtmlrenderer.css.style;

// A simplified version of KHTML's Length type.  It's very convenient to be able
// to treat length values (including auto) in a uniform matter when calculating
// table column widths.  Our own LengthValue is too heavyweight for this purpose and
// doesn't encompass variable (auto) widths.
public class Length {
    // Should use something more reasonable here (e.g. a few feet based on the current
    // DPI)
    public static final int MAX_WIDTH = Integer.MAX_VALUE / 2;
    
    public static final int VARIABLE = 1;
    public static final int FIXED = 2;
    public static final int PERCENT = 3;
    
    private int _type = VARIABLE;
    private long _value = 0;
    
    public Length() {
    }
    
    public Length(long value, int type) {
        _value = value;
        _type = type;
    }
    
    public void setValue(long value) {
        _value = value;
    }
    
    public long value() {
        return _value;
    }
    
    public void setType(int type) {
        _type = type;
    }
    
    public int type() {
        return _type;
    }
    
    public boolean isVariable() {
        return _type == VARIABLE;
    }
    
    public boolean isFixed() {
        return _type == FIXED;
    }
    
    public boolean isPercent() {
        return _type == PERCENT;
    }
    
    public long width(int maxWidth) {
        switch (_type) {
            case FIXED:
                return _value;
            case PERCENT:
                return maxWidth*_value/100;
            case VARIABLE:
                return maxWidth;
            default:
                return -1;
        }
    }
    
    public long minWidth(int maxWidth) {
        switch (_type) {
            case FIXED:
                return _value;
            case PERCENT:
                return maxWidth*_value/100;
            default:
                return 0;
        }
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("(type=");
        switch (_type) {
        case FIXED:
            result.append("fixed");
            break;
        case PERCENT:
            result.append("percent");
            break;
        case VARIABLE:
            result.append("variable");
            break;
        default:
            result.append("unknown");
        }
        result.append(", value=");
        result.append(_value);
        result.append(")");
        
        return result.toString();
    }
}
