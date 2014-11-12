package org.xhtmlrenderer.css.style.derived;

import java.awt.Rectangle;
import java.util.List;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.style.BackgroundPosition;
import org.xhtmlrenderer.css.style.BorderRadiusCorner;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.newtable.CollapsedBorderValue;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Oct 21, 2005
 * Time: 3:24:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class BorderPropertySet extends RectPropertySet {
    public static final BorderPropertySet EMPTY_BORDER = new BorderPropertySet(0.0f, 0.0f, 0.0f, 0.0f);
    
    private IdentValue _topStyle;
    private IdentValue _rightStyle;
    private IdentValue _bottomStyle;
    private IdentValue _leftStyle;

    private FSColor _topColor;
    private FSColor _rightColor;
    private FSColor _bottomColor;
    private FSColor _leftColor;

    private BorderRadiusCorner _topLeft;
    private BorderRadiusCorner _topRight;
    private BorderRadiusCorner _bottomRight;
    private BorderRadiusCorner _bottomLeft;

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
        
        this._topLeft = border._topLeft;
        this._topRight = border._topRight;
        this._bottomLeft = border._bottomLeft;
        this._bottomRight = border._bottomRight;
    }
    public BorderPropertySet(
            float top,
            float right,
            float bottom,
            float left,
            BorderRadiusCorner topLeftCorner,
            BorderRadiusCorner topRightCorner,
            BorderRadiusCorner bottomRightCorner,
            BorderRadiusCorner bottomLeftCorner
    ) {
        this._top = top;
        this._right = right;
        this._bottom = bottom;
        this._left = left;
        
        this._topLeft = topLeftCorner;
        this._topRight = topRightCorner;
        this._bottomLeft = bottomLeftCorner;
        this._bottomRight = bottomRightCorner;
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
        
        this._topLeft = new BorderRadiusCorner();
        this._topRight = new BorderRadiusCorner();
        this._bottomLeft = new BorderRadiusCorner();
        this._bottomRight = new BorderRadiusCorner();
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
        
        this._topLeft = new BorderRadiusCorner();
        this._topRight = new BorderRadiusCorner();
        this._bottomLeft = new BorderRadiusCorner();
        this._bottomRight = new BorderRadiusCorner(); 
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
        /*
        _topLeft = new BorderRadiusCorner(style.valueByName(CSSName.BORDER_TOP_LEFT_RADIUS), ctx);
        _topRight = new BorderRadiusCorner(style.valueByName(CSSName.BORDER_TOP_RIGHT_RADIUS), ctx);
        _bottomLeft = new BorderRadiusCorner(style.valueByName(CSSName.BORDER_BOTTOM_LEFT_RADIUS), ctx);
        _bottomRight = new BorderRadiusCorner(style.valueByName(CSSName.BORDER_BOTTOM_RIGHT_RADIUS), ctx);
        */
        _topLeft = new BorderRadiusCorner(CSSName.BORDER_TOP_LEFT_RADIUS, style, ctx);
        _topRight = new BorderRadiusCorner(CSSName.BORDER_TOP_RIGHT_RADIUS, style, ctx);
        _bottomLeft = new BorderRadiusCorner(CSSName.BORDER_BOTTOM_LEFT_RADIUS, style, ctx);
        _bottomRight = new BorderRadiusCorner(CSSName.BORDER_BOTTOM_RIGHT_RADIUS, style, ctx);
    }

    /**
     * Returns the colors for brighter parts of each side for a particular decoration style
     *
     * @param style
     * @return Returns
     */
    public BorderPropertySet lighten(IdentValue style) {
        BorderPropertySet bc = new BorderPropertySet(this);
        bc._topColor = _topColor == null ? null : _topColor.lightenColor();
        bc._bottomColor = _bottomColor == null ? null : _bottomColor.lightenColor();
        bc._leftColor = _leftColor == null ? null : _leftColor.lightenColor();
        bc._rightColor = _rightColor == null ? null : _rightColor.lightenColor();
        return bc;
    }

    /**
     * Returns the colors for brighter parts of each side for a particular decoration style
     *
     * @param style
     * @return Returns
     */
    public BorderPropertySet darken(IdentValue style) {
        BorderPropertySet bc = new BorderPropertySet(this);
        bc._topColor = _topColor == null ? null : _topColor.darkenColor();
        bc._bottomColor = _bottomColor == null ? null : _bottomColor.darkenColor();
        bc._leftColor = _leftColor == null ? null : _leftColor.darkenColor();
        bc._rightColor = _rightColor == null ? null : _rightColor.darkenColor();
        return bc;
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

    public FSColor topColor() {
        return _topColor;
    }

    public FSColor rightColor() {
        return _rightColor;
    }

    public FSColor bottomColor() {
        return _bottomColor;
    }

    public FSColor leftColor() {
        return _leftColor;
    }
    
    public boolean hasHidden() {
        return _topStyle == IdentValue.HIDDEN || _rightStyle == IdentValue.HIDDEN ||
                    _bottomStyle == IdentValue.HIDDEN || _leftStyle == IdentValue.HIDDEN;
    }    
    
    public boolean hasBorderRadius() {
        return getTopLeft().hasRadius() || getTopRight().hasRadius() || getBottomLeft().hasRadius() || getBottomRight().hasRadius();
    }
    
    public BorderRadiusCorner getBottomRight() {
        return _bottomRight;
    }

    public void setBottomRight(BorderRadiusCorner bottomRight) {
        this._bottomRight = bottomRight;
    }

    public BorderRadiusCorner getBottomLeft() {
        return _bottomLeft;
    }

    public void setBottomLeft(BorderRadiusCorner bottomLeft) {
        this._bottomLeft = bottomLeft;
    }

    public BorderRadiusCorner getTopRight() {
        return _topRight;
    }

    public void setTopRight(BorderRadiusCorner topRight) {
        this._topRight = topRight;
    }

    public BorderRadiusCorner getTopLeft() {
        return _topLeft;
    }

    public void setTopLeft(BorderRadiusCorner topLeft) {
        this._topLeft = topLeft;
    }
    
    public BorderPropertySet normalizedInstance(Rectangle bounds) {
        float factor = 1;
        
        // top
        factor = Math.min(factor, bounds.width / getSideWidth(_topLeft, _topRight, bounds.width));
        // bottom
        factor = Math.min(factor, bounds.width / getSideWidth(_bottomRight, _bottomLeft, bounds.width));
        // right
        factor = Math.min(factor, bounds.height / getSideWidth(_topRight, _bottomRight, bounds.height));
        // left
        factor = Math.min(factor, bounds.height / getSideWidth(_bottomLeft, _bottomRight, bounds.height));
        
        BorderPropertySet newPropSet = new BorderPropertySet(_top, _right, _bottom, _left, 
                new BorderRadiusCorner(factor*_topLeft.getMaxLeft(bounds.height), factor*_topLeft.getMaxRight(bounds.width)), 
                new BorderRadiusCorner(factor*_topRight.getMaxLeft(bounds.width), factor*_topRight.getMaxRight(bounds.height)), 
                new BorderRadiusCorner(factor*_bottomRight.getMaxLeft(bounds.height), factor*_bottomRight.getMaxRight(bounds.width)), 
                new BorderRadiusCorner(factor*_bottomLeft.getMaxLeft(bounds.width), factor*_bottomLeft.getMaxRight(bounds.height)));
        
        newPropSet._topColor = _topColor;
        newPropSet._rightColor = _rightColor;
        newPropSet._bottomColor = _bottomColor;
        newPropSet._leftColor = _leftColor;
        
        newPropSet._topStyle = _topStyle;
        newPropSet._rightStyle = _rightStyle;
        newPropSet._bottomStyle = _bottomStyle;
        newPropSet._leftStyle = _leftStyle;
        
        return newPropSet;
    }

    /**
     * helper function for normalizeBorderRadius. Gets the max side width for each of the corners or the side width whichever is larger
     * @param left
     * @param right
     * @param sideWidth
     * @return
     */
    private float getSideWidth(BorderRadiusCorner left, BorderRadiusCorner right, float sideWidth) {
        return Math.max(sideWidth, left.getMaxRight(sideWidth) + right.getMaxLeft(sideWidth));
    }

}

