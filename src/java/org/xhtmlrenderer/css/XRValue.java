/*
 * {{{ header & license
 * XRValue.java
 * Copyright (c) 2004 Patrick Wright
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
 * }}}
 */
package org.xhtmlrenderer.css;

import java.awt.Color;
import org.w3c.dom.css.CSSValue;
import org.xhtmlrenderer.layout.Context;


/**
 * A CSSValue as parsed from a stylesheet.
 *
 * @author   Patrick Wright
 */
// HACK: need to sort out conceptual confusion betw. CSSValue and CSSPrimitive value. Many of these methods
// are single-value specific but for just a couple of cases it is nice to store the value list as we read it...
// for now, am punting.
public interface XRValue extends CSSValue {
    /** Constant for CSS2 value of "important" */
    String IMPORTANT = "important";

    /** Constant for CSS2 value of "inherit" */
    String INHERIT = "inherit";


    /**
     * True if this value specifically marked as inherited.
     *
     * @return   Returns
     */
    boolean forcedInherit();


    /**
     * True if the value declaration marked as important.
     *
     * @return   The important value
     */
    boolean isImportant();


    /**
     * The value as a CSSValue; changes to the CSSValue are not tracked. Any
     * changes to the properties should be made through the XRProperty and
     * XRValue classes.
     *
     * @return   Returns
     */
    CSSValue cssValue();


    /**
     * The value as a float; if conversion fails, returns .MIN_VALUE
     *
     * @return   Returns
     */
    float asFloat();


    /**
     * The value as specified in the CSS
     *
     * @return   Returns
     */
    String asString();


    /**
     * The value as specified in the CSS
     *
     * @return   Returns
     */
    String[] asStringArray();


    /**
     * Gets the primitiveType attribute of the XRValue object
     *
     * @return   The primitiveType value
     */
    boolean isPrimitiveType();


    /**
     * Gets the valueList attribute of the XRValue object
     *
     * @return   The valueList value
     */
    boolean isValueList();


    /**
     * HACK: this only works if the value is actually a primitve
     *
     * @return   The rGBColorValue value
     */
    Color asColor();


    /**
     * Returns true if this is a relative unit (e.g. percentage) whose value has
     * been computed as an absolute computed value.
     *
     * @return   The relativeUnitComputed value
     */
    boolean requiresComputation();


    /**
     * Computes a relative unit (e.g. percentage) as an absolute value, using
     * the property's XRElement context.
     *
     * @param context       PARAM
     * @param ownerElement  PARAM
     * @param propName      PARAM
     */
    // TODO: clean all PARAM
    void computeRelativeUnit( Context context, XRElement ownerElement, String propName );

    /**
     * Deep copy operation. However, any contained SAC instances are not
     * deep-copied.
     *
     * @return   Returns
     */
    XRValue copyOf();
}// end interface

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2004/10/23 13:03:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

