package org.xhtmlrenderer.css.sheet.factory;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;
import org.xhtmlrenderer.css.value.FSCssValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Jul 27, 2006
 * Time: 5:06:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class FontSizeHackHelper {
    private static final Map FONT_SIZE_ABS_CONVERT_MAP = new HashMap();

    static {
        // TODO:  there is a special cap on sizes too low; setting xx-small here to 50% of base, needs work
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("xx-small", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(.75f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("x-small", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(0.8f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("small", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(.83f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("medium", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(1f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("large", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(1.17f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("x-large", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(1.5f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("xx-large", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(2f)));

        // HACK: absolutely no way to handle larger and smaller in this workaround; leave as a relative
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("larger", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(0.8f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("smaller", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(1.2f)));
    }

    /*package*/
    static FSCssValue fontSizeAbsoluteHack(CSSPrimitiveValue primValue) {
        String val = primValue.getCssText();

        FSCssValue fv = null;
        // TODO: in principle we should be able to check getPrimitiveType(), then only look in our map if it's an
        // SAC_IDENT, but in testing the values returned did not match those declared in the interface, which indicates
        // some version of SAC may be interposed (e.g. by the JVM). Hence we check the map every time, sorry!

        short primitiveType = primValue.getPrimitiveType();
        if ( primitiveType == CSSPrimitiveValue.CSS_IDENT ) {
            fv = (FSCssValue) FONT_SIZE_ABS_CONVERT_MAP.get(val);
        }

        if (fv == null) {
            fv = new FSCssValue(primValue);
        }
        return fv;
    }

    static class FontSizeHackPrimitiveValue implements CSSPrimitiveValue {
        private float floatValue;
        private String asString;

        public FontSizeHackPrimitiveValue(float val) {
            this.floatValue = val;
            this.asString = String.valueOf(floatValue) + "em";
        }

        public short getPrimitiveType() {
            return CSS_EMS;
        }

        public float getFloatValue(short unitType) throws DOMException {
            return floatValue;
        }

        public void setFloatValue(short unitType, float floatValue) throws DOMException {
            //noimpl
        }

        public String getStringValue() throws DOMException {
            return asString;  //noimpl
        }

        public void setStringValue(short stringType, String stringValue) throws DOMException {
            //noimpl
        }

        public Counter getCounterValue() throws DOMException {
            return null;  // noimpl
        }

        public RGBColor getRGBColorValue() throws DOMException {
            return null;  // noimpl
        }

        public Rect getRectValue() throws DOMException {
            return null;  // noimpl
        }

        public short getCssValueType() {
            return CSS_PRIMITIVE_VALUE;
        }

        public String getCssText() {
            return asString;
        }

        public void setCssText(String cssText) throws DOMException {
            // noimpl
        }
    }
}
