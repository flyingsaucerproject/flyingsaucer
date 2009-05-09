/*
 * {{{ header & license
 * Copyright (c) 2004-2009 Josh Marinacci, Tobjorn Gannholm, Patrick Wright, Wisconsin Court System
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

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.constants.ValueConstants;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.util.XRRuntimeException;


public abstract class DerivedValue implements FSDerivedValue {
    private String _asString;

    private short _cssSacUnitType;

    protected DerivedValue() {}

    protected DerivedValue(
            CSSName name,
            short cssSACUnitType,
            String cssText,
            String cssStringValue) {
        this._cssSacUnitType = cssSACUnitType;

        if ( cssText == null ) {
            throw new XRRuntimeException(
                    "CSSValue for '" + name + "' is null after " +
                    "resolving CSS identifier for value '" + cssStringValue + "'");
        }
        this._asString = deriveStringValue(cssText, cssStringValue);
    }

    private String deriveStringValue(String cssText, String cssStringValue) {
            switch (_cssSacUnitType) {
                case CSSPrimitiveValue.CSS_IDENT:
                case CSSPrimitiveValue.CSS_STRING:
                case CSSPrimitiveValue.CSS_URI:
                case CSSPrimitiveValue.CSS_ATTR:
                    return ( cssStringValue == null ? cssText : cssStringValue );
                default:
                    return cssText;
            }
    }

    /** The getCssText() or getStringValue(), depending. */
    public String getStringValue() {
        return _asString;
    }

    /** If value is declared INHERIT should always be the IdentValue.INHERIT,
     * not a DerivedValue
     *
     */
    public boolean isDeclaredInherit() {
        return false;
    }

    public short getCssSacUnitType() {
        return _cssSacUnitType;
    }

    public boolean isAbsoluteUnit() {
        return ValueConstants.isAbsoluteUnit(_cssSacUnitType);
    }

    public float asFloat() {
        throw new XRRuntimeException("asFloat() needs to be overridden in subclass.");
    }

    public FSColor asColor() {
        throw new XRRuntimeException("asColor() needs to be overridden in subclass.");
    }

    public float getFloatProportionalTo(
            CSSName cssName,
            float baseValue,
            CssContext ctx
    ) {
        throw new XRRuntimeException("getFloatProportionalTo() needs to be overridden in subclass.");
    }

    public String asString() {
        return getStringValue();
    }
    public String[] asStringArray() {
        throw new XRRuntimeException("asStringArray() needs to be overridden in subclass.");
    }
    public IdentValue asIdentValue() {
        throw new XRRuntimeException("asIdentValue() needs to be overridden in subclass.");
    }
    public boolean hasAbsoluteUnit() {
        throw new XRRuntimeException("hasAbsoluteUnit() needs to be overridden in subclass.");
    }
    public boolean isIdent() {
        return false;
    }
    public boolean isDependentOnFontSize() {
        return false;
    }
}
