/*
 *
 * CalculatedStyle.java
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
import java.awt.Color;
import java.awt.Font;
import java.util.*;
import java.util.logging.*;

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.RuleNormalizer;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.value.BorderColor;

import org.xhtmlrenderer.css.sheet.PropertyDeclaration;

import org.xhtmlrenderer.css.newmatch.CascadedStyle;

import org.xhtmlrenderer.util.XRLog;


public class CalculatedStyle {

    /** The parent-style we inherit from */
    private CalculatedStyle _parent;
    
    /** the matched properties at the base of this */
    private CascadedStyle _matched;

    /** The styles matched to our owner element. */
    private List _matchedProps;

    /** The main Map of XRProperties keyed by property name, after cascade/inherit takes place. This is the map we look up properties with. Do NOT call clear() (haha). */
    private Map _derivedPropertiesByName;

    /** The derived border width for this RuleSet */
    private Border _drvBorderWidth;

    /** The derived margin width for this RuleSet */
    private Border _drvMarginWidth;

    /** The derived padding width for this RuleSet */
    private Border _drvPaddingWidth;

    /** The derived background color value for this RuleSet */
    private Color _drvBackgroundColor;

    /** The derived border color value for this RuleSet */
    private BorderColor _drvBorderColor;

    /** The derived Color value for this RuleSet */
    private Color _drvColor;


    public CalculatedStyle(CalculatedStyle parent, CascadedStyle matched) {
        this();
        _parent = parent;
        _matched = matched;
        
        derive();
    }


    protected CalculatedStyle() {
        _derivedPropertiesByName = new TreeMap();
    }


    /**
     * Returns true if property has been defined in this style.
     *
     * @param propName  PARAM
     * @return          Returns
     */
    public boolean hasProperty( String propName ) {
        return _derivedPropertiesByName.get( propName ) != null;
    }


    /**
     * Returns a XRProperty by name. Because we are a derived style, the
     * property will already be resolved at this point--the method is
     * synchronized in order to allow this resolution to happen safely. Thus, on
     * this XRProperty you can call actualValue() to get something meaningful.
     *
     * @param propName  PARAM
     * @return          Returns
     */
    public DerivedProperty propertyByName( String propName ) {
        // HERE: when we get the property, check if it is resolved
        // if not, call resolve() and pass it our parent's reference
        // or just our XRElement
        DerivedProperty prop = (DerivedProperty)_derivedPropertiesByName.get( propName );

        // but the property may not be defined for this Element
        if ( prop == null ) {
            DerivedValue val = null;
            
            // if it is inheritable (like color) and we are not root, ask our parent
            // for the value
            if ( CSSName.propertyInherits(propName) && _parent != null && (prop = _parent.propertyByName(propName)) != null) {
            // get a copy, which is always a calculated value!
                prop = prop.copyForInherit();
            } else {
                // otherwise, use the initial value (defined by the CSS2 Spec)
                String initialValue = CSSName.initialValue(propName);
                if ( initialValue == null ) {
                    throw new RuntimeException("Property '" + propName + "' has no initial values assigned.");
                }
                initialValue = RuleNormalizer.convertIdent(propName, initialValue);
                org.xhtmlrenderer.css.impl.DefaultCSSPrimitiveValue cssval = new org.xhtmlrenderer.css.impl.DefaultCSSPrimitiveValue(initialValue);
                //a default value should always be absolute?
                DerivedValue xrVal = new DerivedValue(cssval, _parent);
                prop = new DerivedProperty(propName, xrVal);
            }
            _derivedPropertiesByName.put(propName, prop);
        }
        //prop.resolveValue( _parent );
        return prop;
    }


    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided border width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @return         The borderWidth value
     */
    public Border getBorderWidth( ) {
        if ( _drvBorderWidth == null ) {
            Border border = new Border();
            // ASK: why is Josh forcing to an int in CSSAccessor? don't we want float/pixels?
            border.top = (int)propertyByName( CSSName.BORDER_WIDTH_TOP ).computedValue().asFloat();
            border.bottom = (int)propertyByName( CSSName.BORDER_WIDTH_BOTTOM ).computedValue().asFloat();
            border.left = (int)propertyByName( CSSName.BORDER_WIDTH_LEFT ).computedValue().asFloat();
            border.right = (int)propertyByName( CSSName.BORDER_WIDTH_RIGHT ).computedValue().asFloat();
            _drvBorderWidth = border;
        }
        return _drvBorderWidth;
    }


    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided margin width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @return         The marginWidth value
     */
    public Border getMarginWidth( ) {
        if ( _drvMarginWidth == null ) {
            Border border = new Border();
            // ASK: why is Josh forcing to an int in CSSAccessor? don't we want float/pixels?
            border.top = (int)propertyByName( CSSName.MARGIN_TOP ).computedValue().asFloat();
            border.bottom = (int)propertyByName( CSSName.MARGIN_BOTTOM ).computedValue().asFloat();
            border.left = (int)propertyByName( CSSName.MARGIN_LEFT ).computedValue().asFloat();
            border.right = (int)propertyByName( CSSName.MARGIN_RIGHT ).computedValue().asFloat();
            _drvMarginWidth = border;
        }
        return _drvMarginWidth;
    }


    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided padding width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @return         The paddingWidth value
     */
    public Border getPaddingWidth( ) {
        if ( _drvPaddingWidth == null ) {
            Border border = new Border();
            // ASK: why is Josh forcing to an int in CSSAccessor? don't we want float/pixels?
            border.top = (int)propertyByName( CSSName.PADDING_TOP ).computedValue().asFloat();
            border.bottom = (int)propertyByName( CSSName.PADDING_BOTTOM ).computedValue().asFloat();
            border.left = (int)propertyByName( CSSName.PADDING_LEFT ).computedValue().asFloat();
            border.right = (int)propertyByName( CSSName.PADDING_RIGHT ).computedValue().asFloat();
            _drvPaddingWidth = border;
        }
        return _drvPaddingWidth;
    }


    /**
     * Convenience property accessor; returns a Color initialized with the
     * background color value; Uses the actual value (computed actual value) for
     * this element.
     *
     * @return         The backgroundColor value
     */
    public Color getBackgroundColor( ) {
        if ( _drvBackgroundColor == null ) {
            _drvBackgroundColor = propertyByName( CSSName.BACKGROUND_COLOR ).computedValue().asColor();
            XRLog.cascade(Level.FINEST, "Background color: " + _drvBackgroundColor );
        }
        return _drvBackgroundColor;
    }


    /**
     * Convenience property accessor; returns a BorderColor initialized with the
     * four-sided border color. Uses the actual value (computed actual value)
     * for this element.
     *
     * @return         The borderColor value
     */
    public BorderColor getBorderColor( ) {
        if ( _drvBorderColor == null ) {
            BorderColor bcolor = new BorderColor();
            bcolor.topColor = propertyByName( CSSName.BORDER_COLOR_TOP ).computedValue().asColor();
            bcolor.rightColor = propertyByName( CSSName.BORDER_COLOR_RIGHT ).computedValue().asColor();
            bcolor.bottomColor = propertyByName( CSSName.BORDER_COLOR_BOTTOM ).computedValue().asColor();
            bcolor.leftColor = propertyByName( CSSName.BORDER_COLOR_LEFT ).computedValue().asColor();
            _drvBorderColor = bcolor;
        }
        return _drvBorderColor;
    }


    /**
     * Convenience property accessor; returns a Color initialized with the
     * foreground color Uses the actual value (computed actual value) for this
     * element.
     *
     * @return         The color value
     */
    public Color getColor( ) {
        if ( _drvColor == null ) {
            _drvColor = propertyByName( CSSName.COLOR ).computedValue().asColor();
           XRLog.cascade(Level.FINEST, "Color: " + _drvColor );
        }
        return _drvColor;
    }


    /**
     * <p>
     *
     * Implements cascade/inherit/important logic. This should result in the
     * element for this style having a value for *each and every* (visual)
     * property in the CSS2 spec. The implementation is based on the notion that
     * the matched styles are given to us in a perfectly sorted order, such that
     * properties appearing later in the rule-set always override properties
     * appearing earlier. It also assumes that all properties in the CSS2 spec
     * are defined somewhere across all the matched styles; for example, that
     * the full-property set is given in the user-agent CSS that is always
     * loaded with styles. The current implementation makes no attempt to check
     * either of these assumptions. When this method exits, the derived property
     * list for this class will be populated with the properties defined for
     * this element, properly cascaded.</p>
     */
    private void derive() {
        Iterator mProps = _matched.getMatchedPropertyDeclarations();
        while ( mProps.hasNext() ) {
            PropertyDeclaration pd = (PropertyDeclaration)mProps.next();
            DerivedProperty prop = deriveProperty(pd.getName(), pd.getValue());
            _derivedPropertiesByName.put( prop.propertyName(), prop );
            //System.err.println(pd.getName()+" "+pd.getValue());
        }
    }
    
    private DerivedProperty deriveProperty(String name, org.w3c.dom.css.CSSValue value) {
        DerivedValue specified = new DerivedValue(value, _parent);
        DerivedValue computed = specified;
        //isResolvable
        if(specified.isPrimitiveType() && !ValueConstants.isAbsoluteUnit(specified.cssValue())) {
                // inherit the value from parent element if value is set to inherit
                if ( specified.forcedInherit() ) {
                    // if we are root, have no parent, use the initial value as
                    // defined by the CSS2 spec
                    if ( _parent == null ) {
                        System.err.println( "XRPropertyImpl: trying to resolve an inherited property, but have no parent CalculatedStyle (root of document?)--property '" + name + "' may not be defined in CSS." );
                        // TODO
                        
                    } else {
                        computed = _parent.propertyByName( name ).computedValue();
                    }
                }

                // if value is relative value (e.g. percentage), resolve it
                if ( computed.requiresComputation() ) {
                    computed.computeRelativeUnit( _parent, name );
                }

        }
        return new DerivedProperty(name, computed);
    }

    public String toString() {
        return _derivedPropertiesByName.keySet().toString();
    }

}

