/*
 * CalculatedStyle.java
 * Copyright (c) 2004 Patrick Wright, Torbjï¿½rn Gannholm
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

import java.awt.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.css.CSSPrimitiveValue;

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.RuleNormalizer;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.value.BorderColor;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;


/**
 * A set of properties that apply to a single Element, derived from all matched
 * properties following the rules for CSS cascade, inheritance, importance,
 * specificity and sequence. A derived style is just like a style but
 * (presumably) has additional information that allows relative properties to be
 * assigned values, e.g. font attributes. Property values are fully resolved
 * when this style is created. A property retrieved by name should always have
 * only one value in this class (e.g. one-one map). Any methods to retrieve property values
 * from an instance of this class require a valid {@link
 * org.xhtmlrenderer.layout.Context} be given to it, for some cases of property
 * resolution. Generally, a programmer will not use this class directly, but
 * will retrieve properties using a {@link org.xhtmlrenderer.css.StyleReference}
 * implementation.
 *
 * @author   Torbjörn Gannholm
 * @author   Patrick Wright
 */
public class CalculatedStyle {
    /**
     * The parent-style we inherit from
     */
    private CalculatedStyle _parent;

    /**
     * The main Map of XRProperties keyed by property name, after
     * cascade/inherit takes place. This is the map we look up properties with.
     * Do NOT call clear() (haha).
     */
    private Map _derivedPropertiesByName;

    /**
     * The derived border width for this RuleSet
     */
    private Border _drvBorderWidth;

    /**
     * The derived margin width for this RuleSet
     */
    private Border _drvMarginWidth;

    /**
     * The derived padding width for this RuleSet
     */
    private Border _drvPaddingWidth;

    /**
     * The derived background color value for this RuleSet
     */
    private Color _drvBackgroundColor;

    /**
     * The derived border color value for this RuleSet
     */
    private BorderColor _drvBorderColor;

    /**
     * The derived Color value for this RuleSet
     */
    private Color _drvColor;


    /**
     * Constructor for the CalculatedStyle object.
     * To get a derived style, use the Styler objects getDerivedStyle which will cache styles
     *
     * @param parent  PARAM
     * @param matched PARAM
     */
    CalculatedStyle(CalculatedStyle parent, CascadedStyle matched) {
        this();
        _parent = parent;

        derive( matched );
    }


    /**
     * Default constructor; as the instance is immutable after use, don't use
     * this for class instantiation externally.
     */
    protected CalculatedStyle() {
        _derivedPropertiesByName = new TreeMap();
    }


    /**
     * Returns true if property has been defined in this style.
     *
     * @param propName  The CSS property name to look for, e.g. "font-family".
     * @return          True if the property is defined.
     */
    public boolean hasProperty(String propName) {
        return _derivedPropertiesByName.get(propName) != null;
    }


    /**
     * Returns a {@link DerivedProperty} by name. Because we are a derived
     * style, the property will already be resolved at this point. Thus, on this
     * DerivedProperty you can call {@link DerivedProperty#computedValue()} to get
     * something meaningful.
     *
     * @param propName  The CSS property name, e.g. "font-family"
     * @return          See desc.
     */
    public DerivedProperty propertyByName(String propName) {
        DerivedProperty prop = (DerivedProperty) _derivedPropertiesByName.get(propName);

        // but the property may not be defined for this Element
        if (prop == null) {
            // if it is inheritable (like color) and we are not root, ask our parent
            // for the value
            if ( CSSName.propertyInherits( propName )
                 && _parent != null 
                 && ( prop = _parent.propertyByName( propName ) ) != null ) {

                // get a copy, which is always a calculated value!
                prop = prop.copyForInherit();
            } else {
                // otherwise, use the initial value (defined by the CSS2 Spec)
                String initialValue = CSSName.initialValue(propName);
                if (initialValue == null) {
                    throw new XRRuntimeException("Property '" + propName + "' has no initial values assigned. " +
                            "Check CSSName declarations.");
                }
                initialValue = RuleNormalizer.convertIdent( propName, initialValue );
                org.xhtmlrenderer.css.impl.DefaultCSSPrimitiveValue cssval =
                        new org.xhtmlrenderer.css.impl.DefaultCSSPrimitiveValue( initialValue );
                
                // ASK: a default value should always be absolute?
                DerivedValue xrVal = new DerivedValue(propName, cssval, _parent);
                prop = new DerivedProperty(propName, xrVal);
            }
            _derivedPropertiesByName.put(propName, prop);
        }
        return prop;
    }

    // CLEAN
    public void dumpProperties() {
        StringBuffer out = new StringBuffer();
        Iterator iter = _derivedPropertiesByName.keySet().iterator();
        while (iter.hasNext()) {
            String s =  (String)iter.next();
            out.append("  " + s + " = " + ((DerivedProperty)_derivedPropertiesByName.get(s)).computedValue().asString() + "\n");
        }
        System.out.println(out);
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {
        return _derivedPropertiesByName.keySet().toString();
    }

    /** */
     // CLEAN: Returns all the property names *currently* defined in this style.
     // Since all properties will return a value on request (even if initial
     // value), this is redundant--the same as CSSName list of all properties--
     // we don't track the actual properties assigned by a sheet, so this is irrelevant
     // (PWW 15-11-04)
    /**
     * Implemented for the DOMInspector of HTMLTest. Might be useful for other
     * things too
     *
     * @return The availablePropertyNames value
     */
    public java.util.Set getAvailablePropertyNames() {
        return _derivedPropertiesByName.keySet();
    }


    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided border width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @return The borderWidth value
     * @param parentWidth
     * @param parentHeight
     */
    // TODO: need container size for proportional values here (PWW 21-01-2005)
    public Border getBorderWidth(float parentWidth, float parentHeight) {
        if (_drvBorderWidth == null) {
            Border border = new Border();
            /*border.top = (int) propertyByName(CSSName.BORDER_WIDTH_TOP).computedValue().asFloat();
            border.bottom = (int) propertyByName(CSSName.BORDER_WIDTH_BOTTOM).computedValue().asFloat();
            border.left = (int) propertyByName(CSSName.BORDER_WIDTH_LEFT).computedValue().asFloat();
            border.right = (int) propertyByName(CSSName.BORDER_WIDTH_RIGHT).computedValue().asFloat();*/

            border.top = (int) getFloatPropertyProportionalHeight(CSSName.BORDER_WIDTH_TOP, parentHeight);
            border.bottom = (int) getFloatPropertyProportionalHeight(CSSName.BORDER_WIDTH_BOTTOM, parentHeight);
            border.left = (int) getFloatPropertyProportionalWidth(CSSName.BORDER_WIDTH_LEFT, parentWidth);
            border.right = (int) getFloatPropertyProportionalWidth(CSSName.BORDER_WIDTH_RIGHT, parentWidth);
            _drvBorderWidth = border;
        }
        return _drvBorderWidth;
    }


    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided margin width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @return The marginWidth value
     * @param parentWidth
     * @param parentHeight
     */
    // TODO: need container size for proportional values here (PWW 21-01-2005)
    public Border getMarginWidth(float parentWidth, float parentHeight) {
        if (_drvMarginWidth == null) {
            Border border = new Border();
            /*border.top = (int) propertyByName(CSSName.MARGIN_TOP).computedValue().asFloat();
            border.bottom = (int) propertyByName(CSSName.MARGIN_BOTTOM).computedValue().asFloat();
            border.left = (int) propertyByName(CSSName.MARGIN_LEFT).computedValue().asFloat();
            border.right = (int) propertyByName(CSSName.MARGIN_RIGHT).computedValue().asFloat();*/

            border.top = (int) getFloatPropertyProportionalHeight(CSSName.MARGIN_TOP, parentHeight);
            border.bottom = (int) getFloatPropertyProportionalHeight(CSSName.MARGIN_BOTTOM, parentHeight);
            border.left = (int) getFloatPropertyProportionalWidth(CSSName.MARGIN_LEFT, parentWidth);
            border.right = (int) getFloatPropertyProportionalWidth(CSSName.MARGIN_RIGHT, parentWidth);

            _drvMarginWidth = border;
        }
        return _drvMarginWidth;
    }


    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided padding width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @return The paddingWidth value
     * @param parentWidth
     * @param parentHeight
     */
    // TODO: need container size for proportional values here (PWW 21-01-2005)
    public Border getPaddingWidth(float parentWidth, float parentHeight) {
        if (_drvPaddingWidth == null) {
            Border border = new Border();
            /*border.top = (int) propertyByName(CSSName.PADDING_TOP).computedValue().asFloat();
            border.bottom = (int) propertyByName(CSSName.PADDING_BOTTOM).computedValue().asFloat();
            border.left = (int) propertyByName(CSSName.PADDING_LEFT).computedValue().asFloat();
            border.right = (int) propertyByName(CSSName.PADDING_RIGHT).computedValue().asFloat();*/

            border.top = (int) getFloatPropertyProportionalHeight(CSSName.PADDING_TOP, parentHeight);
            border.bottom = (int) getFloatPropertyProportionalHeight(CSSName.PADDING_BOTTOM, parentHeight);
            border.left = (int) getFloatPropertyProportionalWidth(CSSName.PADDING_LEFT, parentWidth);
            border.right = (int) getFloatPropertyProportionalWidth(CSSName.PADDING_RIGHT, parentWidth);
            _drvPaddingWidth = border;
        }
        return _drvPaddingWidth;
    }


    /**
     * Convenience property accessor; returns a Color initialized with the
     * background color value; Uses the actual value (computed actual value) for
     * this element.
     *
     * @return The backgroundColor value
     */
    public Color getBackgroundColor() {
        if (_drvBackgroundColor == null) {
            _drvBackgroundColor = propertyByName(CSSName.BACKGROUND_COLOR).computedValue().asColor();
            XRLog.cascade(Level.FINEST, "Background color: " + _drvBackgroundColor);
        }
        return _drvBackgroundColor;
    }


    /**
     * Convenience property accessor; returns a BorderColor initialized with the
     * four-sided border color. Uses the actual value (computed actual value)
     * for this element.
     *
     * @return The borderColor value
     */
    // TODO: need container size for proportional values here (PWW 21-01-2005)
    public BorderColor getBorderColor() {
        if (_drvBorderColor == null) {
            BorderColor bcolor = new BorderColor();
            bcolor.topColor = propertyByName(CSSName.BORDER_COLOR_TOP).computedValue().asColor();
            bcolor.rightColor = propertyByName(CSSName.BORDER_COLOR_RIGHT).computedValue().asColor();
            bcolor.bottomColor = propertyByName(CSSName.BORDER_COLOR_BOTTOM).computedValue().asColor();
            bcolor.leftColor = propertyByName(CSSName.BORDER_COLOR_LEFT).computedValue().asColor();
            _drvBorderColor = bcolor;
        }
        return _drvBorderColor;
    }


    /**
     * Convenience property accessor; returns a Color initialized with the
     * foreground color Uses the actual value (computed actual value) for this
     * element.
     *
     * @return The color value
     */
    public Color getColor() {
        if (_drvColor == null) {
            _drvColor = propertyByName(CSSName.COLOR).computedValue().asColor();
            XRLog.cascade(Level.FINEST, "Color: " + _drvColor);
        }
        return _drvColor;
    }

    /**
     * @return The "background-position" property as a Point
     * @param parentWidth
     * @param parentHeight
     */
    public Point getBackgroundPosition(float parentWidth, float parentHeight) {
        DerivedProperty xrProp = propertyByName(CSSName.BACKGROUND_POSITION);
        DerivedValue dv = (DerivedValue) xrProp.computedValue();
        return dv.asPoint(parentWidth, parentHeight);
    }

    /**
     *
     * @param name
     * @param parentWidth
     * @return
     */
    public float getFloatPropertyProportionalWidth(String name, float parentWidth) {
        DerivedProperty prop = propertyByName(name);
        DerivedValue value = prop.computedValue();
        float floatProportionalWidth = value.getFloatProportionalWidth(parentWidth);
        return floatProportionalWidth;
    }

    /**
     *
     * @param name
     * @param parentHeight
     * @return
     */
    public float getFloatPropertyProportionalHeight(String name, float parentHeight) {
        DerivedProperty prop = propertyByName(name);
        DerivedValue value = prop.computedValue();
        float floatProportionalHeight = value.getFloatProportionalHeight(parentHeight);
        return floatProportionalHeight;
    }

    /**
     *
     * @param name
     * @return
     */
    public String getStringProperty(String name) {
        return propertyByName(name).computedValue().asString();
    }

    /**
     *
     * @param name
     * @return
     */
    public boolean isIdentifier(String name) {
        return propertyByName(name).computedValue().isIdentifier();
    }

    /**
     * <p/>
     * <p/>
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
     *
     * @param matched  PARAM
     */
    private void derive(CascadedStyle matched) {
        if (matched == null) {
            return;
        }//nothing to derive

        Iterator mProps = matched.getMatchedPropertyDeclarations();
        while ( mProps.hasNext() ) {
            PropertyDeclaration pd = (PropertyDeclaration)mProps.next();
            DerivedProperty prop = deriveProperty( pd.getName(), pd.getValue() );
            _derivedPropertiesByName.put( prop.propertyName(), prop );
        }
    }

    /**
     * Description of the Method
     *
     * @param propName  PARAM
     * @param value PARAM
     * @return Returns
     */
    private DerivedProperty deriveProperty( String propName, org.w3c.dom.css.CSSPrimitiveValue value ) {
        // Start assuming our computed value is the same as the specified value
        DerivedValue specified = new DerivedValue(propName, value, _parent );
        DerivedValue computed = specified;

        // If the value is not an absolute unit (like pixel), try to resolve it.
        // CLEAN
        /*if ( ((CSSPrimitiveValue)specified.cssValue()).getPrimitiveType() == CSSPrimitiveValue.CSS_UNKNOWN ) {
            System.out.println("   $$$ " + propName + " is of UNKNOWN type.");
        }*/
        /*if ( specified.isPrimitiveType() &&
                ( !propName.equals(CSSName.BACKGROUND_POSITION) &&
                !ValueConstants.isAbsoluteUnit( (CSSPrimitiveValue)specified.cssValue() ))) {*/
        if ( !specified.hasAbsoluteUnit()) {
            // inherit the value from parent element if value is set to inherit
            if (specified.forcedInherit()) {
                // if we are root, have no parent, use the initial value as
                // defined by the CSS2 spec
                if ( _parent == null ) {
                    throw new XRRuntimeException(
                            "CalculatedStyle: trying to resolve an inherited property, " +
                            "but have no parent CalculatedStyle (root of document?)--" +
                            "property '" + propName + "' may not be defined in CSS." );
                } else {
                    // pull from our parent CalculatedStyle
                    computed = _parent.propertyByName( propName ).computedValue();
                }
            }

            // if value is relative value (e.g. percentage), resolve it
            /*
            CLEAN
            if (computed.requiresComputation()) {
                computed.computeRelativeUnit(_parent, propName);
            }*/
        }
        return new DerivedProperty(propName, computed);
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.9  2005/01/24 14:36:31  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.8  2004/12/05 18:11:36  tobega
 * Now uses style cache for pseudo-element styles. Also started preparing to replace inline node handling with inline content handling.
 *
 * Revision 1.7  2004/12/05 00:48:54  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.6  2004/11/15 12:42:23  pdoubleya
 * Across this checkin (all may not apply to this particular file)
 * Changed default/package-access members to private.
 * Changed to use XRRuntimeException where appropriate.
 * Began move from System.err.println to std logging.
 * Standard code reformat.
 * Removed some unnecessary SAC member variables that were only used in initialization.
 * CVS log section.
 *
 *
 */

