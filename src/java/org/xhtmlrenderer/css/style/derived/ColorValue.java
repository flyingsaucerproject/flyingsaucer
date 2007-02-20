package org.xhtmlrenderer.css.style.derived;

import java.awt.Color;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.RGBColor;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.style.DerivedValue;
import org.xhtmlrenderer.css.util.ConversionUtil;
import org.xhtmlrenderer.util.XRRuntimeException;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Oct 17, 2005
 * Time: 2:09:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColorValue extends DerivedValue {
    public static final Color COLOR_TRANSPARENT = new Color(0, 0, 0, 0);
    private static final String TRANSPARENT = "transparent";

    private Color _derivedColor;

    public ColorValue(
            CSSName name,
            short cssSACUnitType,
            String cssText,
            String cssStringValue,
            RGBColor rgbColor
    ) {
        super(name, cssSACUnitType, cssText, cssStringValue);
        _derivedColor = deriveColor(name, rgbColor);
    }
    
    public ColorValue(CSSName name, PropertyValue value) {
        super(name, value.getPrimitiveType(), value.getCssText(), value.getCssText());
        
        short type = value.getPrimitiveType();
        if (type == CSSPrimitiveValue.CSS_IDENT) {
            _derivedColor = COLOR_TRANSPARENT;  // Only possibility
        } else {
            _derivedColor = value.getFSRGBColor().toAWTColor();
        }
    }

    /**
     * Returns the value as a Color, if it is a color.
     *
     * @return The rGBColorValue value
     */
    public Color asColor() {
        return _derivedColor;
    }

    private Color deriveColor(CSSName cssName, RGBColor rgbColor) {
        Color color = null;
        String str = getStringValue();
        try {
            if (TRANSPARENT.equals(str)) {
                color = COLOR_TRANSPARENT;
            } else if (getCssSacUnitType() == CSSPrimitiveValue.CSS_RGBCOLOR && rgbColor != null) {
                color = ConversionUtil.rgbToColor(rgbColor);
            } else {
                color = Color.decode(str);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new XRRuntimeException("Could not return '" + cssName + "' in a DerivedValue as a Color (value '" + str + "'). ", ex);
        }
        return color;
    }
}
