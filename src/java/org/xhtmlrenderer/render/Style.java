package org.xhtmlrenderer.render;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.value.Border;
import org.xhtmlrenderer.extend.RenderingContext;

import java.awt.Font;

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

    public Border getMarginWidth() {
        Border result = new Border(calculatedStyle.getMarginWidth(parentWidth,
                parentWidth, cssContext));
        if (this.marginTopOverrideSet) {
            result.top = (int) this.marginTopOverride;
        }
        if (this.marginBottomOverrideSet) {
            result.bottom = (int) this.marginBottomOverride;
        }
        return result;
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
