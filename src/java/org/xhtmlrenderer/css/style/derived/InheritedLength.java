package org.xhtmlrenderer.css.style.derived;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.util.XRRuntimeException;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Oct 31, 2005
 * Time: 10:19:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class InheritedLength extends LengthValue {
    private static final InheritedLength singleton = new InheritedLength();

    private InheritedLength() {}

    public static InheritedLength instance() { return InheritedLength.singleton; } 

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
        return baseValue;
    }


    public FSDerivedValue copyOf(CSSName cssName) {
        throw new XRRuntimeException("InheritedLength can't be copied; meant as a marker.");
    }
}

