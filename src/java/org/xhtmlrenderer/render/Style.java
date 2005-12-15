package org.xhtmlrenderer.render;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.LayoutContext;

import java.awt.font.LineMetrics;

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

    private float containingBlockWidth;
    
    public Style(CalculatedStyle calculatedStyle, float containingBlockWidth) {
        this.calculatedStyle = calculatedStyle;
        this.containingBlockWidth = containingBlockWidth;
    }
    
    public LineMetrics getLineMetrics(LayoutContext c) {
        return calculatedStyle.getLineMetrics(c);
    }
    
    public int getContainingBlockWidth() {
        return (int)containingBlockWidth;
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

    public IdentValue getBackgroundRepeat() {
        return calculatedStyle.getIdent(CSSName.BACKGROUND_REPEAT);
    }

    public IdentValue getBackgroundAttachment() {
        return calculatedStyle.getIdent(CSSName.BACKGROUND_ATTACHMENT);
    }
    
    public boolean isFixedBackground() {
        return calculatedStyle.getIdent(CSSName.BACKGROUND_ATTACHMENT) == IdentValue.FIXED;
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
    
    public boolean isFloatedLeft() {
        return calculatedStyle.isIdent(CSSName.FLOAT, IdentValue.LEFT);
    }
    
    public boolean isFloatedRight() {
        return calculatedStyle.isIdent(CSSName.FLOAT, IdentValue.RIGHT);
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

    public RectPropertySet getMarginWidth(CssContext cssContext) {
        RectPropertySet rect = 
            calculatedStyle.getMarginRect(containingBlockWidth, containingBlockWidth, cssContext);

        if (marginTopOverrideSet || marginBottomOverrideSet) {
            rect = rect.copyOf();
            if (marginTopOverrideSet) {
                rect.setTop((int)marginTopOverride);
            }
            if (marginBottomOverrideSet) {
                rect.setBottom((int)marginBottomOverride);
            }
        }

        return rect;
    }
    
    public RectPropertySet getPaddingWidth(CssContext cssCtx) {
        return calculatedStyle.getPaddingRect(containingBlockWidth, containingBlockWidth, cssCtx);
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
    
    public boolean isAutoZIndex() {
        return calculatedStyle.isIdent(CSSName.Z_INDEX, IdentValue.AUTO);
    }

    public void setCalculatedStyle(CalculatedStyle calculatedStyle) {
        this.calculatedStyle = calculatedStyle;
    }
    
    public boolean establishesBFC() {
        IdentValue display = calculatedStyle.getIdent(CSSName.DISPLAY);
        IdentValue position = calculatedStyle.getIdent(CSSName.POSITION);
        
        return isFloated() || 
            position == IdentValue.ABSOLUTE || position == IdentValue.FIXED ||
            display == IdentValue.INLINE_BLOCK || display == IdentValue.TABLE_CELL ||
            ! calculatedStyle.isIdent(CSSName.OVERFLOW, IdentValue.VISIBLE);
    }
    
    public boolean requiresLayer() {
        IdentValue position = calculatedStyle.getIdent(CSSName.POSITION);
        
        return position == IdentValue.ABSOLUTE || position == IdentValue.RELATIVE ||
            position == IdentValue.FIXED;
    }
    
    public boolean isHorizontalBackgroundRepeat() {
        IdentValue value = calculatedStyle.getIdent(CSSName.BACKGROUND_REPEAT);
        return value == IdentValue.REPEAT_X || value == IdentValue.REPEAT;
    }
    
    public boolean isVerticalBackgroundRepeat() {
        IdentValue value = calculatedStyle.getIdent(CSSName.BACKGROUND_REPEAT);
        return value == IdentValue.REPEAT_Y || value == IdentValue.REPEAT;
    }
    
    public boolean isTopAuto() {
        return calculatedStyle.isIdent(CSSName.TOP, IdentValue.AUTO);
        
    }
    
    public boolean isBottomAuto() {
        return calculatedStyle.isIdent(CSSName.BOTTOM, IdentValue.AUTO);   
    }
    
    public int getLeftMarginBorderPadding(CssContext cssCtx) {
        return getCalculatedStyle().getLeftMarginBorderPadding(cssCtx, (int)containingBlockWidth);
    }
    
    public int getRightMarginBorderPadding(CssContext cssCtx) {
        return getCalculatedStyle().getRightMarginBorderPadding(cssCtx, (int)containingBlockWidth);
    }  
    
    public boolean isListItem() {
        return getCalculatedStyle().isIdent(CSSName.DISPLAY, IdentValue.LIST_ITEM);
    } 
    
    public boolean isHidden() {
        return getCalculatedStyle().isIdent(CSSName.VISIBILITY, IdentValue.HIDDEN);
    }
}
