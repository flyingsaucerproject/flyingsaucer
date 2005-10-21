package org.xhtmlrenderer.css.style.derived;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Oct 21, 2005
 * Time: 12:12:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class MarginPropertySet extends RectPropertySet {
    private MarginPropertySet() {
        super();
    }

    public MarginPropertySet(
            CalculatedStyle style,
            CSSName cssName,
            float top,
            float right,
            float bottom,
            float left
    ) {
        super(cssName);
        this._top = top;
        this._right = right;
        this._bottom = bottom;
        this._left = left;
        this.buildKey();
    }

    public static MarginPropertySet newInstance(
            CalculatedStyle style,
            float parentHeight,
            float parentWidth,
            CssContext ctx
    ) {
        MarginPropertySet margin =
                new MarginPropertySet(
                        style,
                        CSSName.MARGIN_SHORTHAND,
                        style.getFloatPropertyProportionalHeight(CSSName.MARGIN_TOP, parentHeight, ctx),
                        style.getFloatPropertyProportionalWidth(CSSName.MARGIN_RIGHT, parentWidth, ctx),
                        style.getFloatPropertyProportionalHeight(CSSName.MARGIN_BOTTOM, parentHeight, ctx),
                        style.getFloatPropertyProportionalWidth(CSSName.MARGIN_LEFT, parentWidth, ctx)
                );
        return margin;
    }

    public static String deriveKey(CalculatedStyle style) {
        String key = null;
        if (style.hasAbsoluteUnit(CSSName.MARGIN_TOP) &&
                style.hasAbsoluteUnit(CSSName.MARGIN_RIGHT) &&
                style.hasAbsoluteUnit(CSSName.MARGIN_BOTTOM) &&
                style.hasAbsoluteUnit(CSSName.MARGIN_LEFT)
                ) {
            key = new StringBuffer()
                    .append(CSSName.MARGIN_TOP.toString() + ": ")
                    .append(style.asFloat(CSSName.MARGIN_TOP) + "px ")
                    .append(style.asFloat(CSSName.MARGIN_RIGHT) + "px ")
                    .append(style.asFloat(CSSName.MARGIN_BOTTOM) + "px ")
                    .append(style.asFloat(CSSName.MARGIN_LEFT) + "px")
                    .append(";")
                    .toString();
        }
        return key;
    }

    public RectPropertySet copyOf() {
        RectPropertySet newRect = new MarginPropertySet();
        newRect._top = _top;
        newRect._right = _right;
        newRect._bottom = _bottom;
        newRect._left = _left;
        return newRect;
    }

    protected void buildKey() {
        this._key = new StringBuffer()
                .append(_cssName.toString() + ": ")
                .append(_top + "px ")
                .append(_right + "px ")
                .append(_bottom + "px ")
                .append(_left + "px ")
                .append(";")
                .toString();
    }
}
