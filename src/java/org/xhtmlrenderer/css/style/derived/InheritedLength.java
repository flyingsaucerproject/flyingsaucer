package org.xhtmlrenderer.css.style.derived;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.util.XRRuntimeException;

/**
 * A property with value inherit, where the parent's value is a Length; defers to parent's length
 * value for calculations; this is a marker class.
 */
public class InheritedLength extends LengthValue {
    private LengthValue _inheritedFrom;

    public boolean hasAbsoluteUnit() {
        return _inheritedFrom.hasAbsoluteUnit();
    }

    public InheritedLength(LengthValue inheritedFrom) {
        this._inheritedFrom = inheritedFrom;
    }

    /**
     * Returns the calculated proportional value. In this case, this is the same as the base
     * value, as the containing block for the block with this property will already have derived
     * the proper length. Thus the containing block acts as the holder for the computed value for
     * the length property, which is used here.
     *
     * @param cssName
     * @param baseValue
     * @param ctx
     * @return the possibly proportional float value for the property
     */
    public float getFloatProportionalTo(
            CSSName cssName,
            float baseValue,
            CssContext ctx
    ) {
        float f = Float.MIN_VALUE;
        if ( cssName == CSSName.PADDING_TOP ) {
            f = _inheritedFrom.getStyle().getCachedPadding().top();
        } else if ( cssName == CSSName.PADDING_RIGHT ) {
            f = _inheritedFrom.getStyle().getCachedPadding().right();
        } else if ( cssName == CSSName.PADDING_BOTTOM ) {
            f = _inheritedFrom.getStyle().getCachedPadding().bottom();
        } else if ( cssName == CSSName.PADDING_LEFT ) {
            f = _inheritedFrom.getStyle().getCachedPadding().left();
        } else if ( cssName == CSSName.MARGIN_TOP ) {
            f = _inheritedFrom.getStyle().getCachedMargin().top();
        } else if ( cssName == CSSName.MARGIN_RIGHT ) {
            f = _inheritedFrom.getStyle().getCachedMargin().right();
        } else if ( cssName == CSSName.MARGIN_BOTTOM ) {
            f = _inheritedFrom.getStyle().getCachedMargin().bottom();
        } else if ( cssName == CSSName.MARGIN_LEFT ) {
            f = _inheritedFrom.getStyle().getCachedMargin().left();
        } else {
            f = baseValue;
        }
        return f;
    }


    public FSDerivedValue copyOf(CSSName cssName) {
        throw new XRRuntimeException("InheritedLength can't be copied; meant as a marker.");
    }
}

