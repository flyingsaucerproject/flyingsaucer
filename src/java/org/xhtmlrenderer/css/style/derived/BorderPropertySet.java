package org.xhtmlrenderer.css.style.derived;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.newtable.CollapsedBorderValue;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Oct 21, 2005
 * Time: 3:24:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class BorderPropertySet extends RectPropertySet {
    public static final BorderPropertySet ALL_ZEROS = new BorderPropertySet(0.0f, 0.0f, 0.0f, 0.0f);
    
    private IdentValue _topStyle;
    private IdentValue _rightStyle;
    private IdentValue _bottomStyle;
    private IdentValue _leftStyle;

    private Color _topColor;
    private Color _rightColor;
    private Color _bottomColor;
    private Color _leftColor;

    public BorderPropertySet(BorderPropertySet border) {
        this(border.top(), border.right(), border.bottom(), border.left());
        this._topStyle = border.topStyle();
        this._rightStyle = border.rightStyle();
        this._bottomStyle = border.bottomStyle();
        this._leftStyle = border.leftStyle();

        this._topColor = border.topColor();
        this._rightColor = border.rightColor();
        this._bottomColor = border.bottomColor();
        this._leftColor = border.leftColor();
    }

    public BorderPropertySet(
            float top,
            float right,
            float bottom,
            float left
    ) {
        this._top = top;
        this._right = right;
        this._bottom = bottom;
        this._left = left;
    }
    
    public BorderPropertySet(
           CollapsedBorderValue top,
           CollapsedBorderValue right,
           CollapsedBorderValue bottom,
           CollapsedBorderValue left
    ) {
        this(   top.width(),
                right.width(),
                bottom.width(),
                left.width());
        
        this._topStyle = top.style();
        this._rightStyle = right.style();
        this._bottomStyle = bottom.style();
        this._leftStyle = left.style();

        this._topColor = top.color();
        this._rightColor = right.color();
        this._bottomColor = bottom.color();
        this._leftColor = left.color();        
    }

    private BorderPropertySet(
            CalculatedStyle style,
            CssContext ctx
    ) {
        _top = ( style.isIdent(CSSName.BORDER_TOP_STYLE, IdentValue.NONE) ||
                 style.isIdent(CSSName.BORDER_TOP_STYLE, IdentValue.HIDDEN)  
                ?
            0 : style.getFloatPropertyProportionalHeight(CSSName.BORDER_TOP_WIDTH, 0, ctx));
        _right = ( style.isIdent(CSSName.BORDER_RIGHT_STYLE, IdentValue.NONE) || 
                   style.isIdent(CSSName.BORDER_RIGHT_STYLE, IdentValue.HIDDEN) 
                  ?
            0 : style.getFloatPropertyProportionalHeight(CSSName.BORDER_RIGHT_WIDTH, 0, ctx));
        _bottom = ( style.isIdent(CSSName.BORDER_BOTTOM_STYLE, IdentValue.NONE) ||
                    style.isIdent(CSSName.BORDER_BOTTOM_STYLE, IdentValue.HIDDEN)
                   ?
            0 : style.getFloatPropertyProportionalHeight(CSSName.BORDER_BOTTOM_WIDTH, 0, ctx));
        _left = ( style.isIdent(CSSName.BORDER_LEFT_STYLE, IdentValue.NONE) ||
                  style.isIdent(CSSName.BORDER_LEFT_STYLE, IdentValue.HIDDEN)
                 ?
            0 : style.getFloatPropertyProportionalHeight(CSSName.BORDER_LEFT_WIDTH, 0, ctx));

        _topColor = style.asColor(CSSName.BORDER_TOP_COLOR);
        _rightColor = style.asColor(CSSName.BORDER_RIGHT_COLOR);
        _bottomColor = style.asColor(CSSName.BORDER_BOTTOM_COLOR);
        _leftColor = style.asColor(CSSName.BORDER_LEFT_COLOR);

        _topStyle = style.getIdent(CSSName.BORDER_TOP_STYLE);
        _rightStyle = style.getIdent(CSSName.BORDER_RIGHT_STYLE);
        _bottomStyle = style.getIdent(CSSName.BORDER_BOTTOM_STYLE);
        _leftStyle = style.getIdent(CSSName.BORDER_LEFT_STYLE);
    }

    /**
     * Returns the colors for brighter parts of each side for a particular decoration style
     *
     * @param style
     * @return Returns
     */
    public BorderPropertySet lighten(IdentValue style) {
        BorderPropertySet bc = new BorderPropertySet(this);
        bc._topColor = lightenColor(_topColor);
        bc._bottomColor = lightenColor(_bottomColor);
        bc._leftColor = lightenColor(_leftColor);
        bc._rightColor = lightenColor(_rightColor);

        return bc;
    }
    
    private Color lightenColor(Color color) {
        if (color == null) {
            return null;
        }
        
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float hBase = hsb[0];
        float sBase = hsb[1];
        float bBase = hsb[2];
        
        float hLighter = hBase;
        float sLighter = 0.35f*bBase*sBase;
        float bLighter = 0.6999f + 0.3f*bBase;
        
        return Color.getHSBColor(hLighter, sLighter, bLighter);
    }

    /**
     * Returns the colors for brighter parts of each side for a particular decoration style
     *
     * @param style
     * @return Returns
     */
    public BorderPropertySet darken(IdentValue style) {
        BorderPropertySet bc = new BorderPropertySet(this);
        bc._topColor = darkenColor(_topColor);
        bc._bottomColor = darkenColor(_bottomColor);
        bc._leftColor = darkenColor(_leftColor);
        bc._rightColor = darkenColor(_rightColor);
        return bc;
    }
    
    private Color darkenColor(Color color) {
        if (color == null) {
            return null;
        }
        
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float hBase = hsb[0];
        float sBase = hsb[1];
        float bBase = hsb[2];
        
        float hDarker = hBase;
        float sDarker = sBase;
        float bDarker = 0.56f*bBase;
        
        return Color.getHSBColor(hDarker, sDarker, bDarker);
    }

    public static BorderPropertySet newInstance(
            CalculatedStyle style,
            CssContext ctx
    ) {
        return new BorderPropertySet(style, ctx);
    }

    public String toString() {
        return "BorderPropertySet[top=" + _top + ",right=" + _right + ",bottom=" + _bottom + ",left=" + _left + "]";
    }

    public boolean noTop() {
        return this._topStyle == IdentValue.NONE || (int) _top == 0;
    }

    public boolean noRight() {
        return this._rightStyle == IdentValue.NONE || (int) _right == 0;
    }

    public boolean noBottom() {
        return this._bottomStyle == IdentValue.NONE || (int) _bottom == 0;
    }

    public boolean noLeft() {
        return this._leftStyle == IdentValue.NONE || (int) _left == 0;
    }

    public IdentValue topStyle() {
        return _topStyle;
    }

    public IdentValue rightStyle() {
        return _rightStyle;
    }

    public IdentValue bottomStyle() {
        return _bottomStyle;
    }

    public IdentValue leftStyle() {
        return _leftStyle;
    }

    public Color topColor() {
        return _topColor;
    }

    public Color rightColor() {
        return _rightColor;
    }

    public Color bottomColor() {
        return _bottomColor;
    }

    public Color leftColor() {
        return _leftColor;
    }
}

