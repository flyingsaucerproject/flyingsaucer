/*
 *
 * DerivedProperty.java
 * Copyright (c) 2004 Patrick Wright, Torbjörn Gannholm
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
 * A derived property TODO
 *
 * @author    Torbjörn Gannholm
 * @author    Patrick Wright
 */
public class DerivedProperty {
    /** Property's text name, e.g. "margin-top" */
    private String _propName;

   /** Relative value as computed */
    private DerivedValue _computedValue;

    /**
     * 
     * @param propName
     * @param value
     */
    public DerivedProperty(String propName, DerivedValue value) {
        _propName = propName;
        _computedValue = value;
    }

    /**
     * Deep copy operation for the purposes of inheriting a computed value.
     * Used when a child element needs the parent element's computed value
     * for a property. The following is true of the copy:
     * <ol>
     *  <li>is resolved</li>
     *  <li>computed value is same as parent's computed</li>
     *  <li>actual value</li>
     * </ol>
     * is same as parent's actual value. Any contained SAC {@link CSSValue} instances are not
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

/*

 * $Id$

 *

 * $Log$
 * Revision 1.3  2005/01/24 14:36:31  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *

 *

*/


