package org.xhtmlrenderer.css.value;

import org.w3c.dom.css.*;
import org.xhtmlrenderer.css.constants.Idents;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.util.XRRuntimeException;
import org.xhtmlrenderer.util.GeneralUtil;


/**
 * Implementation of a {@link org.w3c.dom.css.CSSPrimitiveValue}. The main
 * feature of this class is that on construction, values will be "normalized",
 * so that color idents (such as 'black') are converted to valid java.awt.Color
 * strings, and other idents are resolved as possible.
 *
 * @author   empty
 */
public class FSCssValue implements org.w3c.dom.css.CSSPrimitiveValue {
    /**
     *
     */
    private String propName;
    /** Description of the Field */
    private String cssText;
    /** Description of the Field */
    private short valueType;
    /** Description of the Field */
    private Counter counter;
    /** Description of the Field */
    private float floatValue;
    /** Description of the Field */
    private short primitiveType;
    /** Description of the Field */
    private Rect rectValue;
    /** Description of the Field */
    private RGBColor rgbColorValue;
    /** Description of the Field */
    private String stringValue;

    /**
     * Constructor for the FSCssValue object
     *
     * @param primitive  PARAM
     */
    public FSCssValue( String propName, org.w3c.dom.css.CSSPrimitiveValue primitive ) {
        this.propName = propName;
        this.primitiveType = primitive.getPrimitiveType();
        this.cssText = (primitiveType == CSSPrimitiveValue.CSS_STRING ?
                        primitive.getStringValue() :
                        primitive.getCssText());
        this.valueType = primitive.getCssValueType();

        // CLEAN
        /* if ( this.valueType == CSSValue.CSS_VALUE_LIST ) {
            System.out.println("### FSCssValue created as value type: " + propName);
            //new Exception().printStackTrace();
        } */


        // TODO
        // access on these values is not correctly supported in this class
        // right now. would need a switch/case on primitive type
        // as the getZZZ will fail if not the corresponding type
        // e.g. getCounterValue() fails if not actually a counter
        // (PWW 19-11-04)
        //this.floatValue = primitive.getFloatValue( primitiveType );

        // convert type as necessary
        switch ( primitiveType ) {
            case org.w3c.dom.css.CSSPrimitiveValue.CSS_RGBCOLOR:
                cssText = Idents.getColorHex( cssText );
                this.rgbColorValue = primitive.getRGBColorValue();
                break;
            case org.w3c.dom.css.CSSPrimitiveValue.CSS_IDENT:
                cssText = Idents.convertIdent(propName, cssText);
                break;
            case org.w3c.dom.css.CSSPrimitiveValue.CSS_STRING:
                stringValue = cssText;
                break;
            case org.w3c.dom.css.CSSPrimitiveValue.CSS_COUNTER:
                this.counter = primitive.getCounterValue();
                break;
            case org.w3c.dom.css.CSSPrimitiveValue.CSS_RECT:
                this.rectValue = primitive.getRectValue();
                break;
                // TODO: we can code other fixed-type conversions here, like inch-pixel (PWW 19-11-04)
            default:
            // leave as is
        }
        if ( cssText == null ) {
            System.out.println("CSSText is null for " + primitive);   
            System.out.println("   csstext " + primitive.getCssText());   
            System.out.println("   string value " + primitive.getStringValue());   
        }
    }

    /** Use a given CSSPrimitiveValue, with an overriding internal text value */
    public FSCssValue( String propName, org.w3c.dom.css.CSSPrimitiveValue primitive, String newValue ) {
        this(propName, primitive);
        this.cssText = newValue;
        this.stringValue = newValue;
    }
    /**
     * Returns the string representation of the instance, in this case, the CSS
     * text value.
     */
    public String toString() {
        return this.cssText;
    }

    /**
     * Not supported, class is immutable. Sets the string representation of the
     * current value.
     *
     * @param cssText  The new cssText value
     */
    public void setCssText( String cssText ) {
        this.cssText = cssText;
    }

    /**
     * Not supported, class is immutable. A method to set the float value with a
     * specified unit.
     *
     * @param unitType    The new floatValue value
     * @param floatValue  The new floatValue value
     */
    public void setFloatValue( short unitType, float floatValue ) {
        throw new XRRuntimeException( "FSCssValue is immutable." );
    }

    /**
     * Not supported, class is immutable. A method to set the string value with
     * the specified unit.
     *
     * @param stringType   The new stringValue value
     * @param stringValue  The new stringValue value
     */
    public void setStringValue( short stringType, String stringValue ) {
        throw new XRRuntimeException( "FSCssValue is immutable." );
    }

    /**
     * A string representation of the current value.
     *
     * @return   The cssText value
     */
    public String getCssText() {
        return cssText;
    }

    /**
     * A code defining the type of the value as defined above.
     *
     * @return   The cssValueType value
     */
    public short getCssValueType() {
        //return valueType;
        // HACK: we assume that, whatever value we are wrapping, we are, in effect, a single value
        // because shorthand-expansion creates us
        return CSSValue.CSS_PRIMITIVE_VALUE;
    }

    /**
     * Not supported. This method is used to get the Counter value.
     *
     * @return   The counterValue value
     */
    public Counter getCounterValue() {
        return counter;
        //throw new XRRuntimeException( "FSCssValue.getCounterValue() is not supported." );
    }

    /**
     * This method is used to get a float value in a specified unit.
     *
     * @param unitType  PARAM
     * @return          The floatValue value
     */
    public float getFloatValue( short unitType ) {
        return floatValue;
        //throw new XRRuntimeException( "FSCssValue.getFloatValue() is not supported." );
    }

    /**
     * The type of the value as defined by the constants specified above.
     *
     * @return   The primitiveType value
     */
    public short getPrimitiveType() {
        return primitiveType;
    }

    /**
     * Not supported. This method is used to get the Rect value.
     *
     * @return   The rectValue value
     */
    public Rect getRectValue() {
        return rectValue;
        //throw new XRRuntimeException( "FSCssValue.getRectValue() is not supported." );
    }

    /**
     * Not supported. This method is used to get the RGB color.
     *
     * @return   The rGBColorValue value
     */
    // not supported, just not coded right now. would need to keep RGBColor instance
    // in sync with changes to color from RuleNormalizer.
    public RGBColor getRGBColorValue() {
        return rgbColorValue;
        //throw new XRRuntimeException( "FSCssValue.getRGBColorValue() is not supported." );
    }

    /**
     * This method is used to get the string value.
     *
     * @return   The stringValue value
     */
    public String getStringValue() {
        // HACK
        return cssText;
        //CLEANthrow new XRRuntimeException( "FSCssValue.getStringValue() is not supported." );
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2005/01/24 14:27:52  pdoubleya
 * Added to CVS.
 *
 *
 */

