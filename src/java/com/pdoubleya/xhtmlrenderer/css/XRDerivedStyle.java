/*
 * {{{ header & license
 * XRDerivedStyle.java
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
package com.pdoubleya.xhtmlrenderer.css;

import java.awt.Color;

import org.joshy.html.Border;
import org.joshy.html.Context;

import com.pdoubleya.xhtmlrenderer.css.value.BorderColor;


/**
 * A set of properties that apply to a single Element, derived from all matched
 * properties following the rules for CSS cascade, inheritance, importance,
 * specificity and sequence. A derived style is just like a style but
 * (presumably) has additional information that allows relative properties to be
 * assigned values, e.g. font attributes. Property values are fully resolved
 * when this style is created. A property retrieved by name should always have
 * only one value in this class (e.g. one-one map). An <code>XRDerivedStyle</code>
 * is retrieved from an {@link XRElement} using the {@link
 * XRElement#derivedStyle()} method. Any methods to retrieve property values
 * from an instance of this class require a valid {@link org.joshy.html.Context} be given to
 * it, for some cases of property resolution. Generally, a programmer will not
 * use this class directly, but will retrieve properties using a {@link
 * org.joshy.html.css.StyleReference} implementation.
 *
 * @author   Patrick Wright
 * @see      XRElement
 * @see      org.joshy.html.Context
 * @see      org.joshy.html.css.StyleReference
 */
// ASK: marker interface?
public interface XRDerivedStyle extends XRRule {
    /**
     * Convenience property accessor; returns a {@link Border} initialized with
     * the four-sided border width. Uses the actual value (computed actual
     * value) for this element.
     *
     * @param context  A {@link org.joshy.html.Context} instance used in resolving relative
     *      property values.
     * @return         The borderWidth value
     */
    Border getBorderWidth( Context context );


    /**
     * Convenience property accessor; returns a {@link Border} initialized with
     * the four-sided margin width. Uses the actual value (computed actual
     * value) for this element.
     *
     * @param context  A {@link org.joshy.html.Context} instance used in resolving relative
     *      property values.
     * @return         The marginWidth value
     */
    Border getMarginWidth( Context context );


    /**
     * Convenience property accessor; returns a {@link Border} initialized with
     * the four-sided padding width. Uses the actual value (computed actual
     * value) for this element.
     *
     * @param context  A {@link org.joshy.html.Context} instance used in resolving relative
     *      property values.
     * @return         The paddingWidth value
     */
    Border getPaddingWidth( Context context );


    /**
     * Convenience property accessor; returns a {@link Border} initialized with
     * the background color value; Uses the actual value (computed actual value)
     * for this element.
     *
     * @param context  A {@link org.joshy.html.Context} instance used in resolving relative
     *      property values.
     * @return         The backgroundColor value
     */
    Color getBackgroundColor( Context context );


    /**
     * Convenience property accessor; returns a {@link BorderColor} initialized
     * with the four-sided border color. Uses the actual value (computed actual
     * value) for this element.
     *
     * @param context  A {@link org.joshy.html.Context} instance used in resolving relative
     *      property values.
     * @return         The borderColor value
     */
    BorderColor getBorderColor( Context context );


    /**
     * Convenience property accessor; returns a {@link Color} initialized with
     * the foreground color Uses the actual value (computed actual value) for
     * this element.
     *
     * @param context  A {@link org.joshy.html.Context} instance used in resolving relative
     *      property values.
     * @return         The color value
     */
    Color getColor( Context context );
}// end interface


