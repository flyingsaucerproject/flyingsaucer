/*
 * CalculatedStyle.java
 * Copyright (c) 2004, 2005 Patrick Wright, Torbjï¿½rn Gannholm
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

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.constants.Idents;
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
 * only one value in this class (e.g. one-one map). Any methods to retrieve
 * property values from an instance of this class require a valid {@link
 * org.xhtmlrenderer.layout.Context} be given to it, for some cases of property
 * resolution. Generally, a programmer will not use this class directly, but
 * will retrieve properties using a {@link org.xhtmlrenderer.css.StyleReference}
 * implementation.
 *
 * @author   Torbjörn Gannholm
 * @author   Patrick Wright
 */
public class CalculatedStyle {
    /** The parent-style we inherit from  */
    private CalculatedStyle _parent;

    /**
     * Array of DerivedProperties, keyed by the {@link CSSName#getAssignedID()).
     */
    private DerivedProperty[] _derivedPropertiesById;

    /** The derived border width for this RuleSet  */
    private Border _drvBorderWidth;

    /** The derived margin width for this RuleSet  */
    private Border _drvMarginWidth;

    /** The derived padding width for this RuleSet  */
    private Border _drvPaddingWidth;

    /** The derived background color value for this RuleSet  */
    private Color _drvBackgroundColor;

    /** The derived border color value for this RuleSet  */
    private BorderColor _drvBorderColor;

    /** The derived Color value for this RuleSet  */
    private Color _drvColor;


    /**
     * Default constructor; as the instance is immutable after use, don't use
     * this for class instantiation externally.
     */
    protected CalculatedStyle() {
        _derivedPropertiesById = new DerivedProperty[CSSName.countCSSNames()];
    }


    /**
     * Constructor for the CalculatedStyle object. To get a derived style, use
     * the Styler objects getDerivedStyle which will cache styles
     *
     * @param parent   PARAM
     * @param matched  PARAM
     */
    CalculatedStyle( CalculatedStyle parent, CascadedStyle matched ) {
        this();
        _parent = parent;

        derive( matched );
    }


    /**
     * Returns true if property has been defined in this style.
     *
     * @param cssName  The CSS property name to look for, e.g. "font-family".
     * @return         True if the property is defined.
     */
    public boolean hasProperty( CSSName cssName ) {
        return _derivedPropertiesById[cssName.getAssignedID()] != null;
    }


    /**
     * Returns a {@link DerivedProperty} by name. Because we are a derived
     * style, the property will already be resolved at this point. Thus, on this
     * DerivedProperty you can call {@link DerivedProperty#computedValue()} to
     * get something meaningful.
     *
     * @param cssName  The CSS property name, e.g. "font-family"
     * @return         See desc.
     */
    public DerivedProperty propertyByName( CSSName cssName ) {
        DerivedProperty prop = (DerivedProperty)_derivedPropertiesById[cssName.getAssignedID()];

        // but the property may not be defined for this Element
        if ( prop == null ) {
            // if it is inheritable (like color) and we are not root, ask our parent
            // for the value
            if ( CSSName.propertyInherits( cssName )
                    && _parent != null
                    && ( prop = _parent.propertyByName( cssName ) ) != null ) {

                // get a copy, which is always a calculated value!
                prop = prop.copyForInherit();
            } else {
                // otherwise, use the initial value (defined by the CSS2 Spec)
                String initialValue = CSSName.initialValue( cssName );
                if ( initialValue == null ) {
                    throw new XRRuntimeException( "Property '" + cssName + "' has no initial values assigned. " +
                            "Check CSSName declarations." );
                }
                initialValue = Idents.convertIdent( cssName, initialValue );
                org.xhtmlrenderer.css.impl.DefaultCSSPrimitiveValue cssval =
                        new org.xhtmlrenderer.css.impl.DefaultCSSPrimitiveValue( initialValue );

                // ASK: a default value should always be absolute?
                DerivedValue xrVal = new DerivedValue( cssName, cssval, _parent );
                prop = new DerivedProperty( cssName, xrVal );
            }
            _derivedPropertiesById[cssName.getAssignedID()] = prop;
        }
        return prop;
    }

    /** */
    public void dumpProperties() {
        StringBuffer out = new StringBuffer();
        for ( int i = 0; i < _derivedPropertiesById.length; i++ ) {
            DerivedProperty derivedProperty = _derivedPropertiesById[i];
            if ( derivedProperty == null ) {
                out.append("There is an UNEXPECTED null derived property in this CalculatedStyle.\n");
            } else {
                String s = derivedProperty.propertyName();
                out.append( "  " + s + " = " + derivedProperty.computedValue().asString() + "\n" );
            }
        }
        System.out.println( out );
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return   A string representation of the object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < _derivedPropertiesById.length; i++ ) {
            DerivedProperty derivedProperty = _derivedPropertiesById[i];
            String s = derivedProperty.propertyName();
            sb.append( s ).append( " | " );
        }
        return sb.toString();
    }


    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided border width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @param parentWidth
     * @param parentHeight
     * @return              The borderWidth value
     */
    public Border getBorderWidth( float parentWidth, float parentHeight ) {
        if ( _drvBorderWidth == null ) {
            _drvBorderWidth = deriveBorderInstance(
                    new CSSName[]{CSSName.BORDER_WIDTH_TOP, CSSName.BORDER_WIDTH_BOTTOM, CSSName.BORDER_WIDTH_LEFT, CSSName.BORDER_WIDTH_RIGHT},
                    parentHeight,
                    parentWidth );
        }
        return _drvBorderWidth;
    }


    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided margin width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @param parentWidth
     * @param parentHeight
     * @return              The marginWidth value
     */
    public Border getMarginWidth( float parentWidth, float parentHeight ) {
        if ( _drvMarginWidth == null ) {
            _drvMarginWidth = deriveBorderInstance(
                    new CSSName[]{CSSName.MARGIN_TOP, CSSName.MARGIN_BOTTOM, CSSName.MARGIN_LEFT, CSSName.MARGIN_RIGHT},
                    parentHeight,
                    parentWidth );
        }
        return _drvMarginWidth;
    }


    /**
     * Convenience property accessor; returns a Border initialized with the
     * four-sided padding width. Uses the actual value (computed actual value)
     * for this element.
     *
     * @param parentWidth
     * @param parentHeight
     * @return              The paddingWidth value
     */
    public Border getPaddingWidth( float parentWidth, float parentHeight ) {
        if ( _drvPaddingWidth == null ) {
            _drvPaddingWidth = deriveBorderInstance(
                    new CSSName[]{CSSName.PADDING_TOP, CSSName.PADDING_BOTTOM, CSSName.PADDING_LEFT, CSSName.PADDING_RIGHT},
                    parentHeight,
                    parentWidth );
        }
        return _drvPaddingWidth;
    }


    /**
     * Convenience property accessor; returns a Color initialized with the
     * background color value; Uses the actual value (computed actual value) for
     * this element.
     *
     * @return   The backgroundColor value
     */
    public Color getBackgroundColor() {
        if ( _drvBackgroundColor == null ) {
            _drvBackgroundColor = propertyByName( CSSName.BACKGROUND_COLOR ).computedValue().asColor();
            XRLog.cascade( Level.FINEST, "Background color: " + _drvBackgroundColor );
        }
        return _drvBackgroundColor;
    }


    /**
     * Convenience property accessor; returns a BorderColor initialized with the
     * four-sided border color. Uses the actual value (computed actual value)
     * for this element.
     *
     * @return   The borderColor value
     */
    public BorderColor getBorderColor() {
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
     * @return   The color value
     */
    public Color getColor() {
        if ( _drvColor == null ) {
            _drvColor = propertyByName( CSSName.COLOR ).computedValue().asColor();
            XRLog.cascade( Level.FINEST, "Color: " + _drvColor );
        }
        return _drvColor;
    }

    /**
     * @param parentWidth
     * @param parentHeight
     * @return              The "background-position" property as a Point
     */
    public Point getBackgroundPosition( float parentWidth, float parentHeight ) {
        DerivedProperty xrProp = propertyByName( CSSName.BACKGROUND_POSITION );
        DerivedValue dv = (DerivedValue)xrProp.computedValue();
        return dv.asPoint( parentWidth, parentHeight );
    }

    /**
     * @param cssName
     * @param parentWidth
     * @return
     */
    public float getFloatPropertyProportionalWidth( CSSName cssName, float parentWidth ) {
        DerivedProperty prop = propertyByName( cssName );
        DerivedValue value = prop.computedValue();
        float floatProportionalWidth = value.getFloatProportionalWidth( parentWidth );
        return floatProportionalWidth;
    }

    /**
     * @param cssName
     * @param parentHeight
     * @return
     */
    public float getFloatPropertyProportionalHeight( CSSName cssName, float parentHeight ) {
        DerivedProperty prop = propertyByName( cssName );
        DerivedValue value = prop.computedValue();
        float floatProportionalHeight = value.getFloatProportionalHeight( parentHeight );
        return floatProportionalHeight;
    }

    /**
     * @param cssName
     * @return
     */
    public String getStringProperty( CSSName cssName ) {
        return propertyByName( cssName ).computedValue().asString();
    }

    /**
     * Gets the ident attribute of the CalculatedStyle object
     *
     * @param cssName  PARAM
     * @param val      PARAM
     * @return         The ident value
     */
    public boolean isIdent( CSSName cssName, IdentValue val ) {
        return propertyByName( cssName ).isIdent( val );
    }

    /**
     * Gets the ident attribute of the CalculatedStyle object
     *
     * @param cssName  PARAM
     * @return         The ident value
     */
    public IdentValue getIdent( CSSName cssName ) {
        return propertyByName( cssName ).asIdentValue();
    }

    /**
     * @param cssName
     * @return
     */
    public boolean isIdentifier( CSSName cssName ) {
        return propertyByName( cssName ).computedValue().isIdentifier();
    }

    /**
     * Instantiates a Border instance for a four-sided property, e.g.
     * border-width, padding, margin.
     *
     * @param whichProperties  Array of CSSNames for 4 sides, in order: top,
     *      bottom, left, right
     * @param parentHeight     Container parent height
     * @param parentWidth      Container parent width
     * @return                 A Border instance representing the value for the
     *      4 sides.
     */
    private Border deriveBorderInstance( CSSName[] whichProperties, float parentHeight, float parentWidth ) {
        Border border = new Border();
        border.top = (int)getFloatPropertyProportionalHeight( whichProperties[0], parentHeight );
        border.bottom = (int)getFloatPropertyProportionalHeight( whichProperties[1], parentHeight );
        border.left = (int)getFloatPropertyProportionalWidth( whichProperties[2], parentWidth );
        border.right = (int)getFloatPropertyProportionalWidth( whichProperties[3], parentWidth );
        return border;
    }

    /**
     * <p/>
     *
     * <p/>
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
     *
     * @param matched  PARAM
     */
    private void derive( CascadedStyle matched ) {
        if ( matched == null ) {
            return;
        }//nothing to derive

        Iterator mProps = matched.getMatchedPropertyDeclarations();
        int i = 0;
        while ( mProps.hasNext() ) {
            PropertyDeclaration pd = (PropertyDeclaration)mProps.next();
            DerivedProperty prop = deriveProperty( pd.getCSSName(), pd.getValue() );
            _derivedPropertiesById[pd.getCSSName().getAssignedID()] = prop;
        }
    }

    /**
     * Description of the Method
     *
     * @param cssName  PARAM
     * @param value    PARAM
     * @return         Returns
     */
    private DerivedProperty deriveProperty( CSSName cssName, org.w3c.dom.css.CSSPrimitiveValue value ) {
        // Start assuming our computed value is the same as the specified value
        DerivedValue specified = new DerivedValue( cssName, value, _parent );
        DerivedValue computed = specified;

        if ( !specified.hasAbsoluteUnit() ) {
            // inherit the value from parent element if value is set to inherit
            if ( specified.forcedInherit() ) {
                // if we are root, have no parent, use the initial value as
                // defined by the CSS2 spec
                if ( _parent == null ) {
                    throw new XRRuntimeException(
                            "CalculatedStyle: trying to resolve an inherited property, " +
                            "but have no parent CalculatedStyle (root of document?)--" +
                            "property '" + cssName + "' may not be defined in CSS." );
                } else {
                    // pull from our parent CalculatedStyle
                    computed = _parent.propertyByName( cssName ).computedValue();
                }
            }
        }
        return new DerivedProperty( cssName, computed );
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.14  2005/02/03 23:15:50  pdoubleya
 * .
 *
 * Revision 1.13  2005/01/29 20:22:20  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.12  2005/01/25 12:46:12  pdoubleya
 * Refactored duplicate code into separate method.
 *
 * Revision 1.11  2005/01/24 22:46:43  pdoubleya
 * Added support for ident-checks using IdentValue instead of string comparisons.
 *
 * Revision 1.10  2005/01/24 19:01:05  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
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

