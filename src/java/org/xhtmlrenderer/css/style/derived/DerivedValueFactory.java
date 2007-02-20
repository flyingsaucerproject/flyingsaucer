package org.xhtmlrenderer.css.style.derived;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.css.CSSValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
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
                if (cssName == CSSName.BACKGROUND_POSITION) {
                    return new PointValue(style, cssName, value);
                } else if (cssName == CSSName.CONTENT) {
                    return IdentValue.FS_CONTENT_PLACEHOLDER; // HACK
                }
                // Fall through
            default:
                throw new IllegalArgumentException();
        }
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
