package org.xhtmlrenderer.css.value;

import org.w3c.dom.css.*;
import org.xhtmlrenderer.util.XRRuntimeException;


/**
 * Implementation of a {@link org.w3c.dom.css.CSSPrimitiveValue}. The main
 * feature of this class is that on construction, values will be "normalized",
 * so that color idents (such as 'black') are converted to valid java.awt.Color
 * strings, and other idents are resolved as possible.
 *
 * @author empty
 */
public class FSCssValue implements org.w3c.dom.css.CSSPrimitiveValue {
    /** */
    //not used private String propName;
    /** Description of the Field */
    //not used private CSSName cssName;
    /**
     * Description of the Field
     */
    private String _cssText;
    /**
     * Description of the Field
     */
    private Counter counter;
    /**
     * Description of the Field
     */
    private float floatValue;
    /**
     * Description of the Field
     */
    private short primitiveType;
    /**
     * Description of the Field
     */
    private Rect rectValue;
    /**
     * Description of the Field
     */
    private RGBColor rgbColorValue;

    /**
     * Constructor for the FSCssValue object
     *
     * @param primitive PARAM
     */
    public FSCssValue(org.w3c.dom.css.CSSPrimitiveValue primitive) {
        //not used this.cssName = cssName;
        //not used this.propName = cssName.toString();
        this.primitiveType = primitive.getPrimitiveType();
        this._cssText = (primitiveType == CSSPrimitiveValue.CSS_STRING ?
                primitive.getStringValue() :
                primitive.getCssText());

        // TODO
        // access on these values is not correctly supported in this class
        // right now. would need a switch/case on primitive type
        // as the getZZZ will fail if not the corresponding type
        // e.g. getCounterValue() fails if not actually a counter
        // (PWW 19-11-04)
        //this.floatValue = primitive.getFloatValue( primitiveType );

        // convert type as necessary
        switch (primitiveType) {
            case org.w3c.dom.css.CSSPrimitiveValue.CSS_RGBCOLOR:
                this.rgbColorValue = primitive.getRGBColorValue();
                break;
            case org.w3c.dom.css.CSSPrimitiveValue.CSS_IDENT:
                break;
            case org.w3c.dom.css.CSSPrimitiveValue.CSS_STRING:
                // ASK: do we need this? not clear when a CSS_STRING is meaningful (PWW 24-01-05)
                break;
            case org.w3c.dom.css.CSSPrimitiveValue.CSS_COUNTER:
                this.counter = primitive.getCounterValue();
                break;
            case org.w3c.dom.css.CSSPrimitiveValue.CSS_RECT:
                this.rectValue = primitive.getRectValue();
                break;
            case org.w3c.dom.css.CSSPrimitiveValue.CSS_URI:
                this._cssText = primitive.getStringValue();
                break;
            case CSSPrimitiveValue.CSS_IN:
                // fall-thru
            case CSSPrimitiveValue.CSS_CM:
                // fall-thru
            case CSSPrimitiveValue.CSS_EMS:
                // fall-thru
            case CSSPrimitiveValue.CSS_EXS:
                // fall-thru
            case CSSPrimitiveValue.CSS_MM:
                // fall-thru
            case CSSPrimitiveValue.CSS_NUMBER:
                // fall-thru
            case CSSPrimitiveValue.CSS_PC:
                // fall-thru
            case CSSPrimitiveValue.CSS_PERCENTAGE:
                // fall-thru
            case CSSPrimitiveValue.CSS_PT:
                // fall-thru
            case CSSPrimitiveValue.CSS_PX:
                this.floatValue = primitive.getFloatValue(primitiveType);
                break;
            default:
                // leave as is
        }
        if (_cssText == null) {
            throw new XRRuntimeException("CSSText is null for " + primitive + "   csstext " + primitive.getCssText() + "   string value " + primitive.getStringValue());
        }
    }

    /**
     * Use a given CSSPrimitiveValue, with an overriding internal text value
     *
     * @param primitive PARAM
     * @param newValue  PARAM
     */
    public FSCssValue(org.w3c.dom.css.CSSPrimitiveValue primitive, String newValue) {
        this(primitive);
        this._cssText = newValue;
    }

    FSCssValue(short primitiveType, String value) {
        this.primitiveType = primitiveType;
        this._cssText = value;
    }

    public static FSCssValue getNewIdentValue(String identValue) {
        return new FSCssValue(CSSPrimitiveValue.CSS_IDENT, identValue);
    }

    /**
     * Returns the string representation of the instance, in this case, the CSS
     * text value.
     *
     * @return A string representation of the object.
     */
    public String toString() {
        return getCssText();
    }

    /**
     * Not supported, class is immutable. Sets the string representation of the
     * current value.
     *
     * @param cssText The new cssText value
     */
    public void setCssText(String cssText) {
        this._cssText = cssText;
    }

    /**
     * Not supported, class is immutable. A method to set the float value with a
     * specified unit.
     *
     * @param unitType   The new floatValue value
     * @param floatValue The new floatValue value
     */
    public void setFloatValue(short unitType, float floatValue) {
        throw new XRRuntimeException("FSCssValue is immutable.");
    }

    /**
     * Not supported, class is immutable. A method to set the string value with
     * the specified unit.
     *
     * @param stringType  The new stringValue value
     * @param stringValue The new stringValue value
     */
    public void setStringValue(short stringType, String stringValue) {
        throw new XRRuntimeException("FSCssValue is immutable.");
    }

    /**
     * Gets the propName attribute of the FSCssValue object
     *
     * @return   The propName value
     */
    /*public String getPropName() {
        return propName;
    } tobe deleted: not used*/

    /**
     * Gets the cssName attribute of the FSCssValue object
     *
     * @return   The cssName value
     */
    /*public CSSName getCssName() {
        return cssName;
    } tobe deleted: not used */

    /**
     * A string representation of the current value.
     *
     * @return The _cssText value
     */
    public String getCssText() {
        return this._cssText;
    }

    /**
     * A code defining the type of the value as defined above.
     *
     * @return The cssValueType value
     */
    public short getCssValueType() {
        // HACK: we assume that, whatever value we are wrapping, we are, in effect, a single value
        // because shorthand-expansion creates us
        return CSSValue.CSS_PRIMITIVE_VALUE;
    }

    /**
     * Not supported. This method is used to get the Counter value.
     *
     * @return The counterValue value
     */
    public Counter getCounterValue() {
        return counter;
    }

    /**
     * This method is used to get a float value in a specified unit.
     *
     * @param unitType PARAM
     * @return The floatValue value
     */
    public float getFloatValue(short unitType) {
        return floatValue;
    }

    /**
     * The type of the value as defined by the constants specified above.
     *
     * @return The primitiveType value
     */
    public short getPrimitiveType() {
        return primitiveType;
    }

    /**
     * Not supported. This method is used to get the Rect value.
     *
     * @return The rectValue value
     */
    public Rect getRectValue() {
        return rectValue;
    }

    /**
     * Not supported. This method is used to get the RGB color.
     *
     * @return The rGBColorValue value
     */
    public RGBColor getRGBColorValue() {
        return rgbColorValue;
    }

    /**
     * This method is used to get the string value.
     *
     * @return The stringValue value
     */
    public String getStringValue() {
        return this._cssText;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.7  2005/12/28 00:50:53  peterbrant
 * Continue ripping out first try at pagination / Minor method name refactoring
 *
 * Revision 1.6  2005/05/08 13:02:38  tobega
 * Fixed a bug whereby styles could get lost for inline elements, notably if root element was inline. Did a few other things which probably has no importance at this moment, e.g. refactored out some unused stuff.
 *
 * Revision 1.5  2005/02/02 12:13:23  pdoubleya
 * For URIs, return string value.
 *
 * Revision 1.4  2005/01/29 16:18:13  pdoubleya
 * Fixed error: wasn't storing RGB color value passed in.
 *
 * Revision 1.3  2005/01/29 16:04:15  pdoubleya
 * No longer look up identifier when instantiating; value remains as specified in CSS.
 *
 * Revision 1.2  2005/01/24 19:01:07  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.1  2005/01/24 14:27:52  pdoubleya
 * Added to CVS.
 *
 *
 */

