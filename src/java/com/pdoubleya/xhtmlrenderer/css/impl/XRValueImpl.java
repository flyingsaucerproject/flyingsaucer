/*
 * {{{ header & license
 * XRValueImpl.java
 * Copyright (c) 2004 Patrick Wright
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
package com.pdoubleya.xhtmlrenderer.css.impl;

import java.awt.Color;
import java.util.logging.*;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.Rect;

import com.pdoubleya.xhtmlrenderer.css.XRElement;
import com.pdoubleya.xhtmlrenderer.css.XRValue;
import com.pdoubleya.xhtmlrenderer.css.constants.CSSName;
import com.pdoubleya.xhtmlrenderer.css.constants.ValueConstants;
import com.pdoubleya.xhtmlrenderer.css.util.ConversionUtil;
import com.pdoubleya.xhtmlrenderer.util.LoggerUtil;

import org.joshy.html.Context;
import org.joshy.html.util.FontUtil;


/**
 * A primitive value assigned to an XRProperty. XRValue allows for easy type
 * conversions, relative value derivation, etc. The class is intended to "wrap"
 * a CSSValue from a SAC CSS parser. Note that not all type conversions make
 * sense, and that some won't make sense until relative values are resolved. You
 * should check with the cssSACPrimitiveValueType() to see if the value
 * conversion you are requesting is rational. 
 *
 * @author    Patrick Wright
 *
 */
public class XRValueImpl implements XRValue {
    /** Logger instance used for debug messages from this class. */
    private final static Logger sDbgLogger = LoggerUtil.getDebugLogger( XRValueImpl.class );

    // ASK: need to clarify if this class is for both List and Primitives, or just primitives...

    /** The DOM CSSValue we are given from the Parse */
    private CSSValue _domCSSValue;
    
    /** HACK: if the DOM value was relative, and we convert to absolute, the new type of our value, after conversion; we have to store this separately for now because CSSValue has no API for changing type at runtime. */
    private short _newPrimitiveValueType;

    /** The value as text */
    private String _domValueTextClean;

    /** The priority, either "" or "important" */
    private String _domPriority;

    /** CLEANUP: is this needed (PWW 13/08/04) */
    private float _asFloat;

    /** CLEANUP: is this needed (PWW 13/08/04) */
    private boolean _requiresComputation;

    /** String array, if there is one to split from value */
    private String[] _stringAsArray;

    /**
     * Constructor for the XRValueImpl object
     *
     * @param domCSSValue  PARAM
     * @param domPriority  PARAM
     */
    public XRValueImpl( CSSValue domCSSValue, String domPriority ) {
        sDbgLogger.setLevel(Level.OFF);
        _domCSSValue = domCSSValue;
        _domValueTextClean = getCssTextClean();
        _domPriority = domPriority;
        _newPrimitiveValueType = -1;
        _requiresComputation = ! ValueConstants.isAbsoluteUnit(domCSSValue);
        
        if ( ValueConstants.isNumber(cssSACPrimitiveValueType()) ) {
            if ( shouldConvertToPixels() ) {
                _asFloat = convertValueToPixels();
            } else {
                _asFloat = new Float( _domValueTextClean ).floatValue();
            }
        }
    }


    /**
     * Deep copy operation. However, any contained SAC instances are not
     * deep-copied.
     *
     * @return   Returns
     */
    public XRValue copyOf() {
        XRValueImpl nv = new XRValueImpl( _domCSSValue, _domPriority );
        //nv._newPrimitiveValueType = this._newPrimitiveValueType;
        return nv;
    }


    /**
     * The value as a float; returns Float.MIN_VALUE (as float) if there is an
     * error.
     *
     * @return   Returns
     */
    public float asFloat() {
        float f = new Float( Float.MIN_VALUE ).floatValue();
        try {
            f = _asFloat;
        } catch ( Exception ex ) {
            System.err.println( "Value '" + _domValueTextClean + "' is not a valid float." );
        }
        return f;
    }


    /**
     * value as a string...same as getStringValue() but kept for parallel with
     * other as <type>... methods
     *
     * @return   Returns
     */
    public String asString() {
        return getStringValue();
    }


    /**
     * Returns the value as assigned, split into a string array on comma.
     *
     * @return   Returns
     */
    public String[] asStringArray() {
        if ( _stringAsArray == null ) {
            if ( getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE ) {
                String str = getStringValue();
                _stringAsArray = ( str == null ? new String[0] : str.split( "," ));
            } else if ( getCssValueType() == CSSValue.CSS_VALUE_LIST ) {
                CSSValueList list = (CSSValueList)_domCSSValue;
                int len=list.getLength();
                _stringAsArray = new String[len];
                for ( int i=0; i < len; i++ ) {
                    _stringAsArray[i] = ((CSSValue)list.item(i)).getCssText();
                }
            }
        }
        return _stringAsArray;
    }


    /**
     * The value as a CSSValue; changes to the CSSValue are not tracked. Any
     * changes to the properties should be made through the XRProperty and
     * XRValue classes.
     *
     * @return   Returns
     */
    public CSSValue cssValue() {
        return _domCSSValue;
    }


    /**
     * See interface.
     *
     * @return   See desc.
     */
    public boolean forcedInherit() {
        return _domCSSValue.getCssText().indexOf( INHERIT ) >= 0;
    }


    /**
     * A text representation of the value, for dumping
     *
     * @return   Returns
     */
    public String toString() {
        return getCssText() + " (" + ValueConstants.cssType(_domCSSValue.getCssValueType(), cssSACPrimitiveValueType()) + "--" + ValueConstants.getCssValueTypeDesc(cssValue()) + ")\n" +
                "   " + ( isImportant() ? "" : "not " ) + "important" + "\n" +
                "   " + ( forcedInherit() ? "" : "not " ) + "inherited";
    }


    /**
     * Computes a relative unit (e.g. percentage) as an absolute value, using
     * the property's XRElement context.
     *
     * @param ownerElement  The XRElement that has a property with this value
     *      instance
     * @param propName      The name of the property to which this value is
     *      assigned; given because some relative values differ for font-size,
     *      etc.
     * @param context       PARAM
     */
    public void computeRelativeUnit( Context context, XRElement ownerElement, String propName ) {
        if ( ValueConstants.isAbsoluteUnit(cssValue()) ) {
            sDbgLogger.info( "Was asked to convert a relative value, but value is absolute. Call isAbsolute() first." );
            return;
        }

        float relVal = new Float( _domValueTextClean ).floatValue();
        float absVal = 0F;
        String newTypeSuffix = "px";

        switch ( cssSACPrimitiveValueType() ) {
            case CSSPrimitiveValue.CSS_EMS:
                // EM is equal to font-size of element on which it is used
                // The exception is when �em� occurs in the value of
                // the �font-size� property itself, in which case it refers
                // to the font size of the parent element (spec: 4.3.2)
                absVal = relVal * deriveFontSize( context, ownerElement, propName );
                _newPrimitiveValueType = CSSPrimitiveValue.CSS_PX;

                break;
            case CSSPrimitiveValue.CSS_EXS:
                // HACK: just to convert the value to something meaningful, using the height of the 'x' character
                // on the default system font.
                // To convert EMS to pixels, we need the height of the lowercase 'x' character in the current
                // element...
                int xHeight = FontUtil.fontXHeightForElement( context, (Element)ownerElement.domNode() );

                absVal = relVal * xHeight;
                _newPrimitiveValueType = CSSPrimitiveValue.CSS_PX;
                break;
            case CSSPrimitiveValue.CSS_PX:
                // nothing to do
                absVal = relVal;
                break;
            case CSSPrimitiveValue.CSS_PERCENTAGE:
                // percentage depends on the property this value belongs to
                float base = 1.0F;
                if ( propName.equals( CSSName.BOTTOM ) ) {
                    // TODO: need height of containing block
                    System.err.println( "Value not available: property '" + CSSName.BOTTOM + "' as % requires height of containing block." );
                } else if ( propName.equals( CSSName.TOP ) ) {
                    // TODO: need height of containing block
                    System.err.println( "Value not available: property '" + CSSName.TOP + "' as % requires height of containing block." );
                } else if ( propName.equals( CSSName.LEFT ) ) {
                    // TODO: need height of containing block
                    System.err.println( "Value not available: property '" + CSSName.LEFT + "' as % requires width of containing block." );
                } else if ( propName.equals( CSSName.RIGHT ) ) {
                    // TODO: need height of containing block
                    System.err.println( "Value not available: property '" + CSSName.RIGHT + "' as % requires width of containing block." );
                } else if ( propName.equals( CSSName.HEIGHT ) ) {
                    // TODO: need height of containing block
                    System.err.println( "Value not available: property '" + CSSName.HEIGHT + "' as % requires height of containing block." );
                } else if ( propName.equals( CSSName.MAX_HEIGHT ) ) {
                    // TODO: need height of containing block
                    System.err.println( "Value not available: property '" + CSSName.MAX_HEIGHT + "' as % requires height of containing block." );
                } else if ( propName.equals( CSSName.MIN_HEIGHT ) ) {
                    // TODO: need height of containing block
                    System.err.println( "Value not available: property '" + CSSName.MIN_HEIGHT + "' as % requires height of containing block." );
                } else if ( propName.equals( CSSName.MAX_WIDTH ) ) {
                    // TODO: need height of containing block
                    System.err.println( "Value not available: property '" + CSSName.MAX_WIDTH + "' as % requires width of containing block." );
                } else if ( propName.equals( CSSName.MIN_WIDTH ) ) {
                    // TODO: need height of containing block
                    System.err.println( "Value not available: property '" + CSSName.MIN_WIDTH + "' as % requires width of containing block." );
                } else if ( propName.equals( CSSName.TEXT_INDENT ) ) {
                    // TODO: need height of containing block
                    System.err.println( "Value not available: property '" + CSSName.TEXT_INDENT + "' as % requires width of containing block." );
                } else if ( propName.equals( CSSName.VERTICAL_ALIGN ) ) {
                    base = ownerElement.derivedStyle().propertyByName( context, CSSName.LINE_HEIGHT ).actualValue().asFloat();

                } else if ( propName.equals( CSSName.FONT_SIZE ) ) {
                    // same as with EM
                    base = deriveFontSize( context, ownerElement, propName );
                    _newPrimitiveValueType = CSSPrimitiveValue.CSS_PT;
                    newTypeSuffix = "pt";
                    _newPrimitiveValueType = CSSPrimitiveValue.CSS_PX;
                }
                absVal = ( relVal / 100 ) * base;
                // CLEAN System.out.println("New calculated abs val: " + absVal);
                break;
            default:
                // nothing to do, we only convert those listed above
                System.err.println( "Asked to convert value from relative to absolute, don't recognize the datatype " + toString() );
        }
        assert( new Float( absVal ).intValue() > 0 );
        sDbgLogger.finer( "Converted '" + propName + "' relative value of " + relVal + " (" + _domCSSValue.getCssText() + ") to absolute value of " + absVal );
        
        // round down
        double d = Math.floor((double)absVal);
        _asFloat = new Float(d).floatValue();
        
        // note--this is an important step, because if the value is ever
        // inherited, the child will inherit a copy, which will at some
        // point parse the text looking for the type code--so need the suffix
        setCssText("" + _asFloat + newTypeSuffix);
        _requiresComputation = false;
    }


    /**
     * HACK: this only works if the value is actually a primitve
     *
     * @return   The rGBColorValue value
     */
    public Color asColor() {
        assert( getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE );
        String str = getCssText();
        if ( "transparent".equals(str)) 
            return new Color(0,0,0,0);
        else
            return ConversionUtil.rgbToColor( ( (CSSPrimitiveValue)_domCSSValue ).getRGBColorValue() );
    }


    /**
     * Returns true if this is a relative unit (e.g. percentage) whose value has
     * been computed as an absolute computed value, or if by chance this is an
     * absolute unit.
     *
     * @return   The relativeUnitComputed value
     */
    public boolean requiresComputation() {
        return _requiresComputation;
    }


    /**
     * See interface.
     *
     * @param index  The new stringValue value
     * @param s      The new stringValue value
     */
    public void setStringValue( short index, String s ) {
        assert( getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE );
        ( (CSSPrimitiveValue)_domCSSValue ).setStringValue( index, s );
    }


    /**
     * See interface.
     *
     * @param unitType  The new floatValue value
     * @param val       The new floatValue value
     */
    public void setFloatValue( short unitType, float val ) {
        assert( getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE );
        ( (CSSPrimitiveValue)_domCSSValue ).setFloatValue( unitType, val );
    }


    /**
     * Sets the cssText attribute of the XRValueImpl object
     *
     * @param str               The new cssText value
     * @exception DOMException  Throws
     */
    public void setCssText( String str )
        throws DOMException {
        _domCSSValue.setCssText( str );
    }


    /**
     * See interface.
     *
     * @return   Returns
     */
    public short getPrimitiveType() {
        assert( getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE );
        return ( (CSSPrimitiveValue)_domCSSValue ).getPrimitiveType();
    }


    /**
     * See interface.
     *
     * @return   Returns
     */
    public String getStringValue() {
        assert( getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE );
        return ( (CSSPrimitiveValue)_domCSSValue ).getStringValue();
    }


    /**
     * See interface.
     *
     * @param unitType  PARAM
     * @return          Returns
     */
    public float getFloatValue( short unitType ) {
        assert( getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE );
        return ( (CSSPrimitiveValue)_domCSSValue ).getFloatValue( unitType );
    }


    /**
     * See interface.
     *
     * @return   Returns
     */
    public Counter getCounterValue() {
        assert( getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE );
        return ( (CSSPrimitiveValue)_domCSSValue ).getCounterValue();
    }


    /**
     * See interface.
     *
     * @return   Returns
     */
    public Rect getRectValue() {
        assert( getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE );
        return ( (CSSPrimitiveValue)_domCSSValue ).getRectValue();
    }


    /**
     * Gets the primitiveType attribute of the XRValueImpl object
     *
     * @return   The primitiveType value
     */
    public boolean isPrimitiveType() {
        return getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE;
    }


    /**
     * Gets the valueList attribute of the XRValueImpl object
     *
     * @return   The valueList value
     */
    public boolean isValueList() {
        return getCssValueType() == CSSValue.CSS_VALUE_LIST;
    }


    /**
     * Gets the cssText attribute of the XRValueImpl object
     *
     * @return   The cssText value
     */
    public String getCssText() {
        return _domCSSValue.getCssText();
    }


    /**
     * Gets the cssValueType attribute of the XRValueImpl object
     *
     * @return   The cssValueType value
     */
    public short getCssValueType() {
        return _domCSSValue.getCssValueType();
    }


    /**
     * Gets the important attribute of the XRValueImpl object
     *
     * @return   The important value
     */
    public boolean isImportant() {
        return _domPriority != null && _domPriority.equals( IMPORTANT );
    }


    // the CSSValue type if we are wrapping a CSSValue, type
    // CSSValue.CSS_UNKNOWN if we are not wrapping a primitive; best to
    // check if we are wrapping a primitive first
    /**
     * See interface.
     *
     * @return   Returns
     */
    private short cssSACPrimitiveValueType() {
        assert( getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE );
        
        if ( _newPrimitiveValueType >= 0 ) 
            return _newPrimitiveValueType;
        else 
            return ( (CSSPrimitiveValue)_domCSSValue ).getPrimitiveType();
    }


    /**
     * See interface.
     *
     * @param context       PARAM
     * @param ownerElement  PARAM
     * @param propName      PARAM
     * @return              Returns
     */
    private float deriveFontSize( Context context, XRElement ownerElement, String propName ) {
        float fontSize = 0F;
        XRElement parentElem = ownerElement.parentXRElement();
        if ( propName.equals( CSSName.FONT_SIZE ) && parentElem != null ) {
            fontSize = parentElem.derivedStyle().propertyByName( context, CSSName.FONT_SIZE ).actualValue().asFloat();
        } else {
            fontSize = ownerElement.derivedStyle().propertyByName( context, CSSName.FONT_SIZE ).actualValue().asFloat();
        }
        return fontSize;
    }


    /**
     * See interface.
     *
     * @return   Returns
     */
    private float convertValueToPixels() {
        assert( shouldConvertToPixels() );

        float pixelVal = new Float( Float.MIN_VALUE ).floatValue();

        float startVal = new Float( _domValueTextClean ).floatValue();

        final float MM_PER_PX = 0.28F;
        final int MM_PER_CM = 10;
        final float CM_PER_IN = 2.54F;
        final float PT_PER_IN = 72;
        final float PC_PER_PT = 12;

        float cm;

        float mm;

        float in;

        float pt;

        float pc = 0.0F;

        switch ( cssSACPrimitiveValueType() ) {
            case CSSPrimitiveValue.CSS_EMS:
                // TODO
                pixelVal = startVal;
                break;
            case CSSPrimitiveValue.CSS_EXS:
                // TODO
                pixelVal = startVal;
                break;
            case CSSPrimitiveValue.CSS_PX:
                // nothing to do
                pixelVal = startVal;
                break;
            case CSSPrimitiveValue.CSS_PERCENTAGE:
                // TODO
                break;
            // length
            case CSSPrimitiveValue.CSS_IN:
                cm = startVal * CM_PER_IN;
                mm = cm * MM_PER_CM;
                pixelVal = mm / MM_PER_PX;
                break;
            case CSSPrimitiveValue.CSS_CM:
                cm = startVal;
                mm = cm * MM_PER_CM;
                pixelVal = mm / MM_PER_PX;
                break;
            case CSSPrimitiveValue.CSS_MM:
                mm = startVal;
                pixelVal = mm / MM_PER_PX;
                break;
            case CSSPrimitiveValue.CSS_PT:
                pt = startVal;
                in = pt * PT_PER_IN;
                cm = in * CM_PER_IN;
                mm = cm * MM_PER_CM;
                pixelVal = mm / MM_PER_PX;
                break;
            case CSSPrimitiveValue.CSS_PC:
                pc = startVal;
                pt = pc * PC_PER_PT;
                in = pt * PT_PER_IN;
                cm = in * CM_PER_IN;
                mm = cm * MM_PER_CM;
                pixelVal = mm / MM_PER_PX;
                break;
        }
        return pixelVal;
    }


    /**
     * Gets the length attribute of the XRValueImpl object
     *
     * @return   The length value
     */
    private boolean shouldConvertToPixels() {
        return ValueConstants.isNumber(cssSACPrimitiveValueType()) && cssSACPrimitiveValueType() != CSSPrimitiveValue.CSS_PT;
    }


    /**
     * Gets the cssText attribute of the XRValueImpl object
     *
     * @return   The cssText value
     */
    private String getCssTextClean() {
        String text = getCssText().trim();
        // TODO: use regex to pull out all possible endings
        if ( text.endsWith( "px" ) || text.endsWith( "pt" ) || text.endsWith( "em" ) ) {
            text = text.substring( 0, text.length() - 2 ).trim();
        } else if ( text.endsWith( "%" ) ) {
            text = text.substring( 0, text.length() - 1 ).trim();
        }
        return text;
    }
}// end class

