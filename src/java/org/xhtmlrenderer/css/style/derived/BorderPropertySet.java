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

        this._key = border._key;
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

        this.buildKey(CSSName.BORDER_SHORTHAND);
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
        _top = ( style.isIdent(CSSName.BORDER_TOP_STYLE, IdentValue.NONE) ?
            0 : style.getFloatPropertyProportionalHeight(CSSName.BORDER_TOP_WIDTH, 0, ctx));
        _right = ( style.isIdent(CSSName.BORDER_RIGHT_STYLE, IdentValue.NONE) ?
            0 : style.getFloatPropertyProportionalHeight(CSSName.BORDER_RIGHT_WIDTH, 0, ctx));
        _bottom = ( style.isIdent(CSSName.BORDER_BOTTOM_STYLE, IdentValue.NONE) ?
            0 : style.getFloatPropertyProportionalHeight(CSSName.BORDER_BOTTOM_WIDTH, 0, ctx));
        _left = ( style.isIdent(CSSName.BORDER_LEFT_STYLE, IdentValue.NONE) ?
            0 : style.getFloatPropertyProportionalHeight(CSSName.BORDER_LEFT_WIDTH, 0, ctx));

        _topColor = style.asColor(CSSName.BORDER_TOP_COLOR);
        _rightColor = style.asColor(CSSName.BORDER_RIGHT_COLOR);
        _bottomColor = style.asColor(CSSName.BORDER_BOTTOM_COLOR);
        _leftColor = style.asColor(CSSName.BORDER_LEFT_COLOR);

        _topStyle = style.getIdent(CSSName.BORDER_TOP_STYLE);
        _rightStyle = style.getIdent(CSSName.BORDER_RIGHT_STYLE);
        _bottomStyle = style.getIdent(CSSName.BORDER_BOTTOM_STYLE);
        _leftStyle = style.getIdent(CSSName.BORDER_LEFT_STYLE);

        this._key = deriveKey(style);
    }

    /**
     * Returns the colors for brighter parts of each side for a particular decoration style
     *
     * @param style
     * @return Returns
     */
    public BorderPropertySet brighter(IdentValue style) {
        double dS = -0.1;
        double dB = 0.2;
        if (style == IdentValue.INSET || style == IdentValue.GROOVE) {
            dS = 0.05;
            dB = 0;
        } else if (style == IdentValue.RIDGE || style == IdentValue.OUTSET) {
            dS = -0.1;
            dB = 0.1;
        }
        BorderPropertySet bc = new BorderPropertySet(this);
        bc._topColor = modify(_topColor, dS, dB);
        bc._bottomColor = modify(_bottomColor, dS, dB);
        bc._leftColor = modify(_leftColor, dS, dB);
        bc._rightColor = modify(_rightColor, dS, dB);
        bc.buildKey(CSSName.BORDER_SHORTHAND);

        return bc;
    }

    /**
     * Returns the colors for brighter parts of each side for a particular decoration style
     *
     * @param style
     * @return Returns
     */
    public BorderPropertySet darker(IdentValue style) {
        double dS = 0.1;
        double dB = -0.2;
        if (style == IdentValue.GROOVE) {
            dS = 0;
        } else if (style == IdentValue.OUTSET) {
            dB = -0.3;
        }
        BorderPropertySet bc = new BorderPropertySet(this);
        bc._topColor = modify(_topColor, dS, dB);
        bc._bottomColor = modify(_bottomColor, dS, dB);
        bc._leftColor = modify(_leftColor, dS, dB);
        bc._rightColor = modify(_rightColor, dS, dB);
        bc.buildKey(CSSName.BORDER_SHORTHAND);
        return bc;
    }



    public static BorderPropertySet newInstance(
            CalculatedStyle style,
            CssContext ctx
    ) {
        return new BorderPropertySet(style, ctx);
    }

    public static String deriveKey(CalculatedStyle style) {
        String key = null;
        CSSName[] sides = CSSName.BORDER_SIDE_PROPERTIES;
        CSSName[] styles = CSSName.BORDER_STYLE_PROPERTIES;
        CSSName[] colors = CSSName.BORDER_COLOR_PROPERTIES;
        key = new StringBuffer()
                .append(style.asString(sides[0]))
                .append(style.getIdent(styles[0]))
                .append(style.asColor(colors[0]))
                .append(style.asString(sides[1]))
                .append(style.getIdent(styles[1]))
                .append(style.asColor(colors[1]))
                .append(style.asString(sides[2]))
                .append(style.getIdent(styles[2]))
                .append(style.asColor(colors[2]))
                .append(style.asString(sides[3]))
                .append(style.getIdent(styles[3]))
                .append(style.asColor(colors[3]))
                .toString();
        return key;
    }

    public String toString() {
        return getPropertyIdentifier();
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

    protected void buildKey(CSSName name) {
        this._key = new StringBuffer().toString();
    }


    /**
     * Decreasing saturation gives a "whiter" look.
     * Decreasing brightness gives a "shadowier", "blacker" look.
     * Increasing them goes toward the pure, full, color
     * 0.0 brightness gives black.
     * 0.0 saturation gives grayscale.
     * 0.0 saturation and 1.0 brightness gives white.
     *
     * @param color the base color
     * @param dS    change in saturation (result will be clipped to range 0.0-1.0)
     * @param dB    change in brightness (result will be clipped to range 0.0-1.0)
     */
    private static Color modify(Color color, double dS, double dB) {
        if (color == null) {
            return null;
        }
        
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        //
        //decreasing brightness and saturation gives a "shadowier", "blacker" look
        //0.0 brightness gives black
        //0.0 saturation gives grayscale
        float s = (float) Math.max(0.0, Math.min(1.0, hsb[1] + dS));
        float b = (float) Math.max(0.0, Math.min(1.0, hsb[2] + dB));
        int code = Color.HSBtoRGB(hsb[0], s, b);
        return new Color(code);
    }
}

