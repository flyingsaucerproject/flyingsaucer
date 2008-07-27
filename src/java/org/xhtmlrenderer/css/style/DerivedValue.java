package org.xhtmlrenderer.css.style;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.constants.ValueConstants;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.util.XRRuntimeException;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Oct 17, 2005
 * Time: 12:53:07 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class DerivedValue implements FSDerivedValue {
    private String _asString;

    private short _cssSacUnitType;

    protected DerivedValue() {}

    protected DerivedValue(
            CSSName name,
            short cssSACUnitType,
            String cssText,
            String cssStringValue) {
        this._cssSacUnitType = cssSACUnitType;

        String orgText = cssText;
        if ( cssText == null ) {
            throw new XRRuntimeException(
                    "CSSValue for '" + name + "' is null after " +
                    "resolving CSS identifier for value '" + orgText + "'");
        }
        this._asString = deriveStringValue(cssText, cssStringValue);
    }

    private String deriveStringValue(String cssText, String cssStringValue) {
            switch (_cssSacUnitType) {
                case CSSPrimitiveValue.CSS_IDENT:
                case CSSPrimitiveValue.CSS_STRING:
                case CSSPrimitiveValue.CSS_URI:
                case CSSPrimitiveValue.CSS_ATTR:
                    return ( cssStringValue == null ? cssText : cssStringValue );
                default:
                    return cssText;
            }
    }

    /** The getCssText() or getStringValue(), depending. */
    public String getStringValue() {
        return _asString;
    }

    /** If value is declared INHERIT should always be the IdentValue.INHERIT,
     * not a DerivedValue
     *
     */
    public boolean isDeclaredInherit() {
        return false;
    }

    public short getCssSacUnitType() {
        return _cssSacUnitType;
    }

    public boolean isAbsoluteUnit() {
        return ValueConstants.isAbsoluteUnit(_cssSacUnitType);
    }

    public float asFloat() {
        throw new XRRuntimeException("asFloat() needs to be overridden in subclass.");
    }

    public FSColor asColor() {
        throw new XRRuntimeException("asColor() needs to be overridden in subclass.");
    }

    public float getFloatProportionalTo(
            CSSName cssName,
            float baseValue,
            CssContext ctx
    ) {
        throw new XRRuntimeException("getFloatProportionalTo() needs to be overridden in subclass.");
    }

    public String asString() {
        return getStringValue();
    }
    public String[] asStringArray() {
        throw new XRRuntimeException("asStringArray() needs to be overridden in subclass.");
    }
    public IdentValue asIdentValue() {
        throw new XRRuntimeException("asIdentValue() needs to be overridden in subclass.");
    }
    public boolean hasAbsoluteUnit() {
        throw new XRRuntimeException("hasAbsoluteUnit() needs to be overridden in subclass.");
    }
    public boolean isIdent() {
        return false;
    }
    public boolean isDependentOnFontSize() {
        return false;
    }
}
