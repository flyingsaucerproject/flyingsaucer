/*
 * {{{ header & license
 * XRDerivedStyleImpl.java
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
package org.xhtmlrenderer.css.impl;

import org.xhtmlrenderer.css.constants.ValueConstants;
import java.awt.Color;
import java.awt.Font;
import java.util.*;
import java.util.logging.*;

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.css.RuleNormalizer;

import org.xhtmlrenderer.css.XRDerivedStyle;
import org.xhtmlrenderer.css.XRElement;
import org.xhtmlrenderer.css.XRProperty;
import org.xhtmlrenderer.css.XRRule;
import org.xhtmlrenderer.css.XRStyleRule;
import org.xhtmlrenderer.css.XRValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.value.BorderColor;
import org.xhtmlrenderer.util.LoggerUtil;


/**
 * Default implementation of XRDerivedStyle.
 *
 * @author    Patrick Wright
 *
 */
public class XRDerivedStyleImpl implements XRDerivedStyle {

    /** Logger instance used for debug messages in this class. */
    private final static Logger sDbgLogger = LoggerUtil.getDebugLogger( XRDerivedStyleImpl.class );

    /** The XRElement we are a derived style for. */
    private XRElement _xrElement;

    /** The styles matched to our owner element. */
    private List _matchedStyles;

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


    /**
     * Constructor for the XRDerivedStyleImpl object
     *
     * @param forElement  PARAM
     * @param iter        PARAM
     */
    public XRDerivedStyleImpl( XRElement forElement, Iterator iter ) {
        this();
        sDbgLogger.setLevel(Level.OFF);

        _xrElement = forElement;

        while ( iter.hasNext() ) {
            _matchedStyles.add( iter.next() );
        }
        derive();
    }


    /**
     * Constructor for the XRDerivedStyleImpl object
     *
     * @param forElement  PARAM
     * @param rule        PARAM
     */
    public XRDerivedStyleImpl( XRElement forElement, XRStyleRule rule ) {
        _xrElement = forElement;

        _matchedStyles.add( rule );

        List props = pullFromRule( rule );
        Iterator iter = props.iterator();
        while ( iter.hasNext() ) {
            XRProperty property = (XRProperty)iter.next();
            _derivedPropertiesByName.put( property.propertyName(), property );
        }
    }


    /** Constructor for the XRDerivedStyleImpl object */
    private XRDerivedStyleImpl() {
        _matchedStyles = new ArrayList();
        _derivedPropertiesByName = new TreeMap();
    }


    /** Prints deriver prop information to stdout. */
    public void dump() {
        sDbgLogger.info( "Derivation complete: " + _derivedPropertiesByName );
    }


    /**
     * Returns an Iterator of our derived XRProperty set.
     *
     * @return   See desc.
     */
    public Iterator listXRProperties() {
        return _derivedPropertiesByName.values().iterator();
    }


    /**
     * Merges two XRRules, combining all properties. This is not used for
     * cascading, rather for two rules defined separately in the same sheet with
     * the same selector. Any properties with the same name in fromRuleSet will
     * replace existing properties with that name in this XRRule.
     *
     * @param fromRule  PARAM
     */
    public void mergeProperties( XRRule fromRule ) {
        Iterator iter = fromRule.listXRProperties();
        while ( iter.hasNext() ) {
            XRProperty prop = (XRProperty)iter.next();
            _derivedPropertiesByName.put( prop.propertyName(), prop );
        }
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
     * @param context   PARAM
     * @return          Returns
     */
    public XRProperty propertyByName( Context context, String propName ) {
        // HERE: when we get the property, check if it is resolved
        // if not, call resolve() and pass it our parent's reference
        // or just our XRElement
        XRProperty prop = (XRProperty)_derivedPropertiesByName.get( propName );

        // but the property may not be defined for this Element
        if ( prop == null ) {
            XRValue val = null;
            
            // if it is inheritable (like color) and we are not root, ask our parent
            // for the value
            if ( CSSName.propertyInherits(propName) && _xrElement.parentXRElement() != null ) {
                // get a copy
                prop = _xrElement.parentXRElement().derivedStyle().propertyByName(context, propName).copyForInherit();
            } else {
                // otherwise, use the initial value (defined by the CSS2 Spec)
                String initialValue = CSSName.initialValue(propName);
                if ( initialValue == null ) {
                    throw new RuntimeException("Property '" + propName + "' has no initial values assigned.");
                }
                initialValue = RuleNormalizer.convertIdent(propName, initialValue);
                DefaultCSSPrimitiveValue cssval = new DefaultCSSPrimitiveValue(initialValue);
                XRValueImpl xrVal = new XRValueImpl(cssval, "");
                prop = new XRPropertyImpl(propName, 100, xrVal);
            }
            _derivedPropertiesByName.put(propName, prop);
        }
        prop.resolveValue( context, _xrElement );
        return prop;
    }


    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided border width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @param context  PARAM
     * @return         The borderWidth value
     */
    public Border getBorderWidth( Context context ) {
        if ( _drvBorderWidth == null ) {
            Border border = new Border();
            // ASK: why is Josh forcing to an int in CSSAccessor? don't we want float/pixels?
            border.top = (int)propertyByName( context, CSSName.BORDER_WIDTH_TOP ).actualValue().asFloat();
            border.bottom = (int)propertyByName( context, CSSName.BORDER_WIDTH_BOTTOM ).actualValue().asFloat();
            border.left = (int)propertyByName( context, CSSName.BORDER_WIDTH_LEFT ).actualValue().asFloat();
            border.right = (int)propertyByName( context, CSSName.BORDER_WIDTH_RIGHT ).actualValue().asFloat();
            _drvBorderWidth = border;
        }
        return _drvBorderWidth;
    }


    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided margin width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @param context  PARAM
     * @return         The marginWidth value
     */
    public Border getMarginWidth( Context context ) {
        if ( _drvMarginWidth == null ) {
            Border border = new Border();
            // ASK: why is Josh forcing to an int in CSSAccessor? don't we want float/pixels?
            border.top = (int)propertyByName( context, CSSName.MARGIN_TOP ).actualValue().asFloat();
            border.bottom = (int)propertyByName( context, CSSName.MARGIN_BOTTOM ).actualValue().asFloat();
            border.left = (int)propertyByName( context, CSSName.MARGIN_LEFT ).actualValue().asFloat();
            border.right = (int)propertyByName( context, CSSName.MARGIN_RIGHT ).actualValue().asFloat();
            _drvMarginWidth = border;
        }
        return _drvMarginWidth;
    }


    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided padding width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @param context  PARAM
     * @return         The paddingWidth value
     */
    public Border getPaddingWidth( Context context ) {
        if ( _drvPaddingWidth == null ) {
            Border border = new Border();
            // ASK: why is Josh forcing to an int in CSSAccessor? don't we want float/pixels?
            border.top = (int)propertyByName( context, CSSName.PADDING_TOP ).actualValue().asFloat();
            border.bottom = (int)propertyByName( context, CSSName.PADDING_BOTTOM ).actualValue().asFloat();
            border.left = (int)propertyByName( context, CSSName.PADDING_LEFT ).actualValue().asFloat();
            border.right = (int)propertyByName( context, CSSName.PADDING_RIGHT ).actualValue().asFloat();
            _drvPaddingWidth = border;
        }
        return _drvPaddingWidth;
    }


    /**
     * Convenience property accessor; returns a Color initialized with the
     * background color value; Uses the actual value (computed actual value) for
     * this element.
     *
     * @param context  PARAM
     * @return         The backgroundColor value
     */
    public Color getBackgroundColor( Context context ) {
        if ( _drvBackgroundColor == null ) {
            _drvBackgroundColor = propertyByName( context, CSSName.BACKGROUND_COLOR ).actualValue().asColor();
            sDbgLogger.finest( "Background color: " + _drvBackgroundColor );
        }
        return _drvBackgroundColor;
    }


    /**
     * Convenience property accessor; returns a BorderColor initialized with the
     * four-sided border color. Uses the actual value (computed actual value)
     * for this element.
     *
     * @param context  PARAM
     * @return         The borderColor value
     */
    public BorderColor getBorderColor( Context context ) {
        if ( _drvBorderColor == null ) {
            BorderColor bcolor = new BorderColor();
            bcolor.topColor = propertyByName( context, CSSName.BORDER_COLOR_TOP ).actualValue().asColor();
            bcolor.rightColor = propertyByName( context, CSSName.BORDER_COLOR_RIGHT ).actualValue().asColor();
            bcolor.bottomColor = propertyByName( context, CSSName.BORDER_COLOR_BOTTOM ).actualValue().asColor();
            bcolor.leftColor = propertyByName( context, CSSName.BORDER_COLOR_LEFT ).actualValue().asColor();
            _drvBorderColor = bcolor;
        }
        return _drvBorderColor;
    }


    /**
     * Convenience property accessor; returns a Color initialized with the
     * foreground color Uses the actual value (computed actual value) for this
     * element.
     *
     * @param context  PARAM
     * @return         The color value
     */
    public Color getColor( Context context ) {
        if ( _drvColor == null ) {
            _drvColor = propertyByName( context, CSSName.COLOR ).actualValue().asColor();
            sDbgLogger.finest( "Color: " + _drvColor );
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
        List relativeProps = new ArrayList();
        Iterator mStyles = _matchedStyles.iterator();
        while ( mStyles.hasNext() ) {
            XRStyleRule rule = (XRStyleRule)mStyles.next();
            // IDEA: at this point could run a compareTo() check on this rule and the prev in the loop, sanity check
            Iterator props = rule.listXRProperties();
            while ( props.hasNext() ) {
                XRProperty prop = (XRProperty)props.next();
                if ( !ValueConstants.isAbsoluteUnit(prop.specifiedValue().cssValue()) ) {
                    relativeProps.add( prop );
                }
                _derivedPropertiesByName.put( prop.propertyName(), prop );
            }
        }
        // properties with relative values need to be cloned, because these can
        // vary depending on the element to which they are assigned
        Iterator iter = relativeProps.iterator();
        while ( iter.hasNext() ) {
            XRProperty prop = (XRProperty)iter.next();
            _derivedPropertiesByName.put( prop.propertyName(), prop.copyOf() );
        }
    }


    /**
     * Implements cascade/inherit/important logic. This should result in the
     * element for this style having a value for *each and every* (visual)
     * property in the CSS2 spec.
     *
     * @param context  PARAM
     */
    // This was the original version written by Patrick Wright
    // replaced in favor of sort-based version in approach suggested by Scott
    private void deriveOld( Context context ) {
        // loop all known (visual) properties, checking for a value
        // if we don't find it in our matched styles, we look to parent
        //
        // OPTM: if we can run this from root to leaf, could improve performance
        // by making resolved parent properties directly available...
        Iterator propNames = CSSName.allCSS2PropertyNames();
        while ( propNames.hasNext() ) {
            String propName = (String)propNames.next();

            // is property defined in a matched style?
            Iterator mStyles = _matchedStyles.iterator();

            // these "the" properties denote the current Property for our property name, with its seq, spec, sheet seq
            XRProperty theProp = null;
            int theSheetSeq = 0;
            int theSpec = 0;
            int theSeq = 0;

            while ( mStyles.hasNext() ) {
                XRStyleRule rule = (XRStyleRule)mStyles.next();

                XRProperty prop = rule.propertyByName( context, propName );
                if ( prop != null ) {
                    int sheetSeq = rule.getStyleSheet().sequence();
                    int spec = rule.selectorSpecificity();
                    int seq = rule.sequenceInStyleSheet();

                    // if we don't have any Property for this property name or
                    // if we have one, but the one we just found has higher
                    // precedence in stylesheet, spec, or seq, use it
                    if ( theProp == null ||
                            ( theProp != null &&
                            ( sheetSeq > theSheetSeq || ( ( spec > theSpec ) || ( spec == theSpec && seq > theSeq ) ) ) ) ) {
                        theProp = prop;
                        theSheetSeq = sheetSeq;
                        theSpec = spec;
                        theSeq = seq;
                    }
                }
            }// loop styles

            // could not find property in our styles
            // or as a property it inherits
            // or found it, but specified value "inherit"
            if ( ( theProp == null && CSSName.propertyInherits( propName ) ) ||
                    ( theProp != null && theProp.specifiedValue().forcedInherit() ) ) {

                // check our parent. this may be recursive to root in worst case
                // but should not generally be, as all our elements should have
                // default matches from default.css, which would have some initial
                // value, not value "inherit"
                XRElement parent = _xrElement.parentXRElement();
                if ( parent != null ) {
                    XRProperty prop = parent.derivedStyle().propertyByName( context, propName );
                    if ( prop != null ) {
                        if ( theProp == null ||
                                ( theProp != null &&
                                ( prop.specifiedValue().isImportant() && !theProp.specifiedValue().isImportant() ) ||
                                ( theProp.specifiedValue().forcedInherit() ) ) ) {
                            theProp = prop;
                        }
                    }
                }
            }

            _derivedPropertiesByName.put( propName, theProp );
        }
    }


    /**
     * just extracts all XRProperties from a rule into a List
     *
     * @param rule  PARAM
     * @return      Returns
     */
    private List pullFromRule( XRStyleRule rule ) {
        List der = new ArrayList();
        Iterator props = rule.listXRProperties();
        while ( props.hasNext() ) {
            der.add( props.next() );
        }
        return der;
    }
}

