/*
 *
 * DerivedProperty.java
 * Copyright (c) 2004 Torbjörn Gannholm
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

package org.xhtmlrenderer.css.style;

import org.xhtmlrenderer.css.constants.ValueConstants;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSValue;

import org.xhtmlrenderer.css.constants.CSSName;

import org.xhtmlrenderer.css.RuleNormalizer;


/**
 * Default XRProperty implementation.
 *
 * @author    Patrick Wright
 *
 */
public class DerivedProperty {
    /** Property's text name, e.g. "margin-top" */
    private String _propName;

   /** Relative value as computed */
    private DerivedValue _computedValue;

    /**
     * Constructor for the XRPropertyImpl object
     *
     * @param style     PARAM
     * @param propName  PARAM
     * @param sequence  PARAM
     * @param value     PARAM
     */
    public DerivedProperty(String propName, DerivedValue value) {
        _propName = propName;
        _computedValue = value;
    }

    /**
     * Deep copy operation for the purposes of inheriting a computed value.
     * Used when a child element needs the parent element's computed value
     * for a property. The following is true of the copy: 1) is resolved
     * 2) computed value is same as parent's computed 3) actual value
     * is same as parent's actual value. Any contained SAC instances are not
     * deep-copied.
     *
     * @return   See desc
     */
    public DerivedProperty copyForInherit() {
        DerivedProperty newProp = new DerivedProperty( _propName, _computedValue.copyOf() );
        return newProp;
    }

    /**
     * The computed value, if the specified value is relative.
     *
     * @return   Returns
     */
    public DerivedValue computedValue() {
        //a derived property only has a calculated value!
        return _computedValue;
        //resolveValue();
        //return ( ValueConstants.isAbsoluteUnit(_specifiedValue.cssValue()) ? _specifiedValue : _computedValue );
    }

    /**
     * The plain-text property name, should be CSS2 valid.
     *
     * @return   Returns
     */
    public String propertyName() {
        return _propName;
    }


    /**
     * ...
     *
     * @return   Returns
     */
    public String toString() {
        return _propName + "=" + _computedValue;
    }

}// end class

