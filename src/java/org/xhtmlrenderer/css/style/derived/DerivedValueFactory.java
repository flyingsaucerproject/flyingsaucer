package org.xhtmlrenderer.css.style.derived;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.constants.Idents;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.FSDerivedValue;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Oct 17, 2005
 * Time: 2:21:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class DerivedValueFactory {
    /**
     * Properties which only accept color assignments; transparent is included.
     */
    private static final Set COLOR_PROPERTIES;

    /**
     * Properties which only accept string assignments (e.g. URLs)
     */
    private static final Set STRING_PROPERTIES;

    /**
     * Properties which only accept enumerated constants
     */
    private static final Set IDENT_PROPERTIES;
    
    public static FSDerivedValue newDerivedValue(
            CalculatedStyle style, CSSName cssName, PropertyValue value) {
        if (value.getCssValueType() == CSSValue.CSS_INHERIT) {
            return style.getParent().valueByName(cssName);
        }
        switch (value.getPropertyValueType()) {
            case PropertyValue.VALUE_TYPE_LENGTH:
                return new LengthValue(style, cssName, value);
            case PropertyValue.VALUE_TYPE_IDENT:
                IdentValue ident = value.getIdentValue();
                if (ident == null) {
                    ident = IdentValue.getByIdentString(value.getStringValue());
                }
                return ident;
            case PropertyValue.VALUE_TYPE_STRING:
                return new StringValue(cssName, value);
            case PropertyValue.VALUE_TYPE_NUMBER:
                return new NumberValue(cssName, value);
            case PropertyValue.VALUE_TYPE_COLOR:
                FSDerivedValue color = (FSDerivedValue)CACHED_COLORS.get(value.getCssText());
                if (color == null) {
                    color = new ColorValue(cssName, value);
                    CACHED_COLORS.put(value.getCssText(), color);
                }
                return color;
            case PropertyValue.VALUE_TYPE_LIST:
                // background-position is the only one that uses VALUE_TYPE_LIST
                // (and can appear here)
                return new PointValue(style, cssName, value);
            default:
                throw new IllegalArgumentException();
        }
    }
    
    public static FSDerivedValue newDerivedValue(CalculatedStyle style,
                                                 CSSName cssName,
                                                 short cssSACUnitType,
                                                 String cssText,
                                                 String cssStringValue,
                                                 RGBColor rgbColor) {
        FSDerivedValue val = null;

        // default to copy of parent if inherited
        boolean declaredInherit = cssText.equals("inherit");
        if (declaredInherit) {
            val = style.getParent().valueByName(cssName);
        } else {
            if (cssName == CSSName.BACKGROUND_POSITION) {
                val = new PointValue(style, cssName, cssSACUnitType, cssText, cssStringValue);
            } else if (COLOR_PROPERTIES.contains(cssName)) {
                val = newColor(cssName, cssSACUnitType, cssText, cssStringValue, rgbColor);
            } else if (STRING_PROPERTIES.contains(cssName)) {
                val = new StringValue(cssName, cssSACUnitType, cssText, cssStringValue);
            } else if (!(cssName == CSSName.FONT_WEIGHT) && Idents.looksLikeALength(cssText)) {
                val = new LengthValue(style, cssName, cssSACUnitType, cssText, cssStringValue);
            } else if (!(cssName == CSSName.FONT_WEIGHT) && Idents.looksLikeANumber(cssText)) {
                val = new NumberValue(cssName, cssSACUnitType, cssText, cssStringValue);
            } else if (IDENT_PROPERTIES.contains(cssName) || IdentValue.looksLikeIdent(cssText)) {
                val = IdentValue.getByIdentString(cssText);
            } else {
                // use regular RuntimeException; XRRuntimeException auto-logs, which we don't need
                // in this case.
                throw new RuntimeException("Can't determine the dervived value type to use for property " +
                        "named '" + cssName + "' with value " + cssText);
            }
        }

        return val;
    }

    /**
     * Returns the ColorValue, possibly from local cache by cssText value; RGB colors are not cached (guessing
     * these will be custom anyway).
     */
    private static FSDerivedValue newColor(CSSName cssName, short cssSACUnitType, String cssText, String cssStringValue, RGBColor rgbColor) {
        FSDerivedValue val;
        if (rgbColor == null) {
            val = (FSDerivedValue) CACHED_COLORS.get(cssText);
            if (val == null) {
                val = new ColorValue(cssName, cssSACUnitType, cssText, cssStringValue, rgbColor);
                CACHED_COLORS.put(cssText, val);
            }
        } else {
            val = new ColorValue(cssName, cssSACUnitType, cssText, cssStringValue, rgbColor);
        }
        return val;
    }

    private static final Map CACHED_COLORS;

    static {
        CACHED_COLORS = new HashMap();

        COLOR_PROPERTIES = new HashSet();
        COLOR_PROPERTIES.add(CSSName.COLOR);
        COLOR_PROPERTIES.add(CSSName.BACKGROUND_COLOR);
        COLOR_PROPERTIES.add(CSSName.OUTLINE_COLOR);
        COLOR_PROPERTIES.add(CSSName.BORDER_TOP_COLOR);
        COLOR_PROPERTIES.add(CSSName.BORDER_RIGHT_COLOR);
        COLOR_PROPERTIES.add(CSSName.BORDER_BOTTOM_COLOR);
        COLOR_PROPERTIES.add(CSSName.BORDER_LEFT_COLOR);

        STRING_PROPERTIES = new HashSet();
        STRING_PROPERTIES.add(CSSName.FONT_FAMILY);
        STRING_PROPERTIES.add(CSSName.BACKGROUND_IMAGE);
        STRING_PROPERTIES.add(CSSName.LIST_STYLE_IMAGE);
        STRING_PROPERTIES.add(CSSName.CONTENT);
        STRING_PROPERTIES.add(CSSName.QUOTES);
        STRING_PROPERTIES.add(CSSName.FS_MOVE_TO_FLOW);
        STRING_PROPERTIES.add(CSSName.FS_FLOW_TOP);
        STRING_PROPERTIES.add(CSSName.FS_FLOW_RIGHT);
        STRING_PROPERTIES.add(CSSName.FS_FLOW_BOTTOM);
        STRING_PROPERTIES.add(CSSName.FS_FLOW_LEFT);

        IDENT_PROPERTIES = new HashSet();
        IDENT_PROPERTIES.add(CSSName.BACKGROUND_ATTACHMENT);
        IDENT_PROPERTIES.add(CSSName.BACKGROUND_REPEAT);
        IDENT_PROPERTIES.add(CSSName.BORDER_COLLAPSE);
        IDENT_PROPERTIES.add(CSSName.BORDER_BOTTOM_STYLE);
        IDENT_PROPERTIES.add(CSSName.BORDER_LEFT_STYLE);
        IDENT_PROPERTIES.add(CSSName.BORDER_RIGHT_STYLE);
        IDENT_PROPERTIES.add(CSSName.BORDER_TOP_STYLE);
        IDENT_PROPERTIES.add(CSSName.DISPLAY);
        IDENT_PROPERTIES.add(CSSName.EMPTY_CELLS);        
        IDENT_PROPERTIES.add(CSSName.FLOAT);
        IDENT_PROPERTIES.add(CSSName.FONT_STYLE);
        IDENT_PROPERTIES.add(CSSName.FONT_VARIANT);
        IDENT_PROPERTIES.add(CSSName.FONT_WEIGHT);
        IDENT_PROPERTIES.add(CSSName.LIST_STYLE_TYPE);
        IDENT_PROPERTIES.add(CSSName.POSITION);
        IDENT_PROPERTIES.add(CSSName.TEXT_DECORATION);
        IDENT_PROPERTIES.add(CSSName.TEXT_TRANSFORM);
        IDENT_PROPERTIES.add(CSSName.VERTICAL_ALIGN);
        IDENT_PROPERTIES.add(CSSName.WHITE_SPACE);
    }
}
