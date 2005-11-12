package org.xhtmlrenderer.css.style.derived;

import org.w3c.dom.css.RGBColor;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.constants.Idents;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final List COLOR_PROPERTIES;

    /**
     * Properties which only accept string assignments (e.g. URLs)
     */
    private static final List STRING_PROPERTIES;

    /**
     * Properties which only accept enumerated constants
     */
    private static final List IDENT_PROPERTIES;

    public static FSDerivedValue newDerivedValue(CalculatedStyle style,
                                                 CSSName cssName,
                                                 short cssSACUnitType,
                                                 String cssText,
                                                 String cssStringValue,
                                                 RGBColor rgbColor) {
        FSDerivedValue val = null;

        // default to copy of parent if inherited; may be overridden in some cases
        boolean declaredInherit = cssText.equals("inherit");
        if (declaredInherit) {
            if (style.getParent().isLengthValue(cssName)) {
                val = new InheritedLength((LengthValue) style.getParent().valueByName(cssName));
            } else {
                // not as bad as it looks; all IdentValues return their
                // own instance in copyOf, so they are really singletons
                val = style.getParent().copyOf(cssName);
            }
        } else {
            if (cssName == CSSName.BACKGROUND_POSITION) {
                val = new PointValue(style, cssName, cssSACUnitType, cssText, cssStringValue);
            } else if (COLOR_PROPERTIES.contains(cssName)) {
                // TODO: we should probably cache these, as colors will be reused across styles
                // and probably won't be too many of them
                val = newColor(style, cssName, cssSACUnitType, cssText, cssStringValue, rgbColor);
            } else if (STRING_PROPERTIES.contains(cssName)) {
                val = new StringValue(style, cssName, cssSACUnitType, cssText, cssStringValue);
            } else if (Idents.looksLikeALength(cssText) && !(cssName == CSSName.FONT_WEIGHT)) {
                val = new LengthValue(style, cssName, cssSACUnitType, cssText, cssStringValue);
            } else if (IDENT_PROPERTIES.contains(cssName) || IdentValue.looksLikeIdent(cssText)) {
                val = IdentValue.getByIdentString(cssText);
            } else if (Idents.looksLikeANumber(cssText)) {
                val = new NumberValue(style, cssName, cssSACUnitType, cssText, cssStringValue);
            } else {
                throw new XRRuntimeException("Can't determine the dervived value type to use for property " +
                        "named '" + cssName + "' with value " + cssText);
            }
        }

        return val;
    }

    /**
     * Returns the ColorValue, possibly from local cache by cssText value; RGB colors are not cached (guessing
     * these will be custom anyway).
     */
    private static FSDerivedValue newColor(CalculatedStyle style, CSSName cssName, short cssSACUnitType, String cssText, String cssStringValue, RGBColor rgbColor) {
        FSDerivedValue val;
        if (rgbColor == null) {
            val = (FSDerivedValue) CACHED_COLORS.get(cssText);
            if (val == null) {
                val = new ColorValue(style, cssName, cssSACUnitType, cssText, cssStringValue, rgbColor);
                CACHED_COLORS.put(cssText, val);
            }
        } else {
            val = new ColorValue(style, cssName, cssSACUnitType, cssText, cssStringValue, rgbColor);
        }
        return val;
    }

    private static final Map CACHED_COLORS;

    static {
        CACHED_COLORS = new HashMap();

        COLOR_PROPERTIES = new ArrayList();
        COLOR_PROPERTIES.add(CSSName.COLOR);
        COLOR_PROPERTIES.add(CSSName.BACKGROUND_COLOR);
        COLOR_PROPERTIES.add(CSSName.OUTLINE_COLOR);
        COLOR_PROPERTIES.add(CSSName.BORDER_COLOR_TOP);
        COLOR_PROPERTIES.add(CSSName.BORDER_COLOR_RIGHT);
        COLOR_PROPERTIES.add(CSSName.BORDER_COLOR_BOTTOM);
        COLOR_PROPERTIES.add(CSSName.BORDER_COLOR_LEFT);

        STRING_PROPERTIES = new ArrayList();
        STRING_PROPERTIES.add(CSSName.FONT_FAMILY);
        STRING_PROPERTIES.add(CSSName.BACKGROUND_IMAGE);
        STRING_PROPERTIES.add(CSSName.LIST_STYLE_IMAGE);
        STRING_PROPERTIES.add(CSSName.CONTENT);

        IDENT_PROPERTIES = new ArrayList();
        IDENT_PROPERTIES.add(CSSName.BACKGROUND_ATTACHMENT);
        IDENT_PROPERTIES.add(CSSName.BACKGROUND_REPEAT);
        IDENT_PROPERTIES.add(CSSName.BORDER_COLLAPSE);
        IDENT_PROPERTIES.add(CSSName.BORDER_STYLE_BOTTOM);
        IDENT_PROPERTIES.add(CSSName.BORDER_STYLE_LEFT);
        IDENT_PROPERTIES.add(CSSName.BORDER_STYLE_RIGHT);
        IDENT_PROPERTIES.add(CSSName.BORDER_STYLE_TOP);
        IDENT_PROPERTIES.add(CSSName.DISPLAY);
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
