/*
 * {{{ header & license
 * CalculatedStyle.java
 * Copyright (c) 2004 Torbjörn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
/*
 * CalculatedStyle.java
 *
 * Created on den 30 augusti 2004, 01:11
 */

package net.homelinux.tobe.xhtmlrenderer;

import java.awt.Color;

import org.joshy.html.Border;
import org.joshy.html.Context;

import com.pdoubleya.xhtmlrenderer.css.value.BorderColor;

import net.homelinux.tobe.xhtmlrenderer.stylerImpl.XRPropertyImpl;

/**
 * extending XRDerivedStyle seems mostly correct, but perhaps is not entirely minimal
 *
 * @author  Torbjörn Gannholm
 */
public interface CalculatedStyle {
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
    
    /**
     * An iterator of all XRProperties in this rule.
     *
     * @return   Returns
     */
    java.util.Iterator listXRProperties();


    /**
     * Value of a single XRProperty, by name.
     *
     * @param propName  PARAM
     * @param context   PARAM
     * @return          Returns
     */
    XRPropertyImpl propertyByName( Context context, String propName );


    /**
     * Returns true if the named property was defined and has a value in this
     * rule set.
     *
     * @param propName  PARAM
     * @return          Returns
     */
    boolean hasProperty( String propName );


    /**
     * Merges two XRRules, combining all properties. This is not used for
     * cascading, rather for two rules defined separately in the same sheet with
     * the same selector. Any properties with the same name in fromRuleSet will
     * replace existing properties with that name in this XRRule.
     *
     * @param fromRuleSet  PARAM
     */
    //void mergeProperties( XRRule fromRuleSet );
}
