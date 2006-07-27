package org.xhtmlrenderer.css.sheet.factory;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;
import org.w3c.dom.DOMException;
import org.w3c.css.sac.LexicalUnit;
import org.xhtmlrenderer.css.value.FSCssValue;

import java.util.Map;
import java.util.HashMap;

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
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("xx-small", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(50.0f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("x-small", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(66.0f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("small", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(66.0f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("medium", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(100f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("large", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(133f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("x-large", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(166f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("xx-large", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(200f)));

        // HACK: absolutely no way to handle larger and smaller in this workaround; leave as a relative
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("larger", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(80.0f)));
        FontSizeHackHelper.FONT_SIZE_ABS_CONVERT_MAP.put("smaller", new FSCssValue(new FontSizeHackHelper.FontSizeHackPrimitiveValue(120.0f)));
    }

    /*package*/ static FSCssValue fontSizeAbsoluteHack(CSSPrimitiveValue primValue) {
        String val = primValue.getCssText();
        FSCssValue fv = (FSCssValue) FONT_SIZE_ABS_CONVERT_MAP.get(val);
        if ( fv == null ) {
            return new FSCssValue(primValue);
        } else {
            return fv;
        }
    }

    static class FontSizeHackPrimitiveValue implements CSSPrimitiveValue {
        private float pctValFloat;
        private String asString;

        // HACK: absolute font sizes like "medium" are based on the user's preferred size
        private static final int BASE_SIZE = 16;

        public FontSizeHackPrimitiveValue(float pctVal) {
            this.pctValFloat = Math.round(( pctVal / 100 ) * BASE_SIZE);
            this.asString = "" + pctValFloat;
        }

        public short getPrimitiveType() {
            return LexicalUnit.SAC_POINT;
        }

        public float getFloatValue(short unitType) throws DOMException {
            return pctValFloat;
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
