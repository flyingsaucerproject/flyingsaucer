package org.xhtmlrenderer.render;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.extend.RenderingContext;

import java.awt.*;

/**
 * Encapuslates anything style related in a Box. A separate bean is used to
 * avoid cluttering up Box more than necessary.
 */
public class Style {
    private CalculatedStyle calculatedStyle;

    private float marginTopOverride;

    private boolean marginTopOverrideSet = false;

    private float marginBottomOverride;

    private boolean marginBottomOverrideSet = false;

    private float parentWidth;

    private CssContext cssContext;

    public Style(CalculatedStyle calculatedStyle, float parentWidth,
                 CssContext cssContext) {
        this.calculatedStyle = calculatedStyle;
        this.parentWidth = parentWidth;
        this.cssContext = cssContext;
    }

    public Font getFont(RenderingContext renderingContext) {
        return renderingContext.getFont(calculatedStyle.getFont(cssContext));
    }

    public boolean isClearLeft() {
        IdentValue clear = calculatedStyle.getIdent(CSSName.CLEAR);
        return clear == IdentValue.LEFT || clear == IdentValue.BOTH;
    }

    public boolean isClearRight() {
        IdentValue clear = calculatedStyle.getIdent(CSSName.CLEAR);
        return clear == IdentValue.RIGHT || clear == IdentValue.BOTH;
    }

    public boolean isCleared() {
        return !calculatedStyle.isIdent(CSSName.CLEAR, IdentValue.NONE);
    }

    public CalculatedStyle getCalculatedStyle() {
        return calculatedStyle;
    }

    public void setCalculatedStyle(CalculatedStyle calculatedStyle) {
        this.calculatedStyle = calculatedStyle;
    }

    public IdentValue getBackgroundRepeat() {
        return calculatedStyle.getIdent(CSSName.BACKGROUND_REPEAT);
    }

    public IdentValue getBackgroundAttachment() {
        return calculatedStyle.getIdent(CSSName.BACKGROUND_ATTACHMENT);
    }

    public boolean isAbsolute() {
        return calculatedStyle.isIdent(CSSName.POSITION, IdentValue.ABSOLUTE);
    }

    public boolean isFixed() {
        return calculatedStyle.isIdent(CSSName.POSITION, IdentValue.FIXED);
    }

    public boolean isFloated() {
        IdentValue floatVal = calculatedStyle.getIdent(CSSName.FLOAT);
        return floatVal == IdentValue.LEFT || floatVal == IdentValue.RIGHT;
    }

    public boolean isRelative() {
        return calculatedStyle.isIdent(CSSName.POSITION, IdentValue.RELATIVE);
    }

    public boolean isPostionedOrFloated() {
        return isAbsolute() || isFixed() || isFloated() || isRelative();
    }

    public float getMarginTopOverride() {
        return this.marginTopOverride;
    }

    public void setMarginTopOverride(float marginTopOverride) {
        this.marginTopOverride = marginTopOverride;
        this.marginTopOverrideSet = true;
    }

    public float getMarginBottomOverride() {
        return this.marginBottomOverride;
    }

    public void setMarginBottomOverride(float marginBottomOverride) {
        this.marginBottomOverride = marginBottomOverride;
        this.marginBottomOverrideSet = true;
    }

    public RectPropertySet getMarginWidth() {
        RectPropertySet rect = calculatedStyle.getMarginRect(parentWidth, parentWidth, cssContext).copyOf();

        // TODO: this is bad for cached rects...
        if (this.marginTopOverrideSet) {
            rect.setTop((int) this.marginTopOverride);
        }
        if (this.marginBottomOverrideSet) {
            rect.setBottom((int) this.marginBottomOverride);
        }
        return rect;
    }

    public boolean isAutoWidth() {
        return calculatedStyle.isIdent(CSSName.WIDTH, IdentValue.AUTO);
    }

    public boolean isAutoHeight() {
        // HACK: assume containing block height is auto, so percentages become
        // auto
        return calculatedStyle.isIdent(CSSName.HEIGHT, IdentValue.AUTO)
                || !calculatedStyle.hasAbsoluteUnit(CSSName.HEIGHT);
    }

    public boolean isOutsideNormalFlow() {
        return isFixed() || isAbsolute() || isFloated();
    }
}
