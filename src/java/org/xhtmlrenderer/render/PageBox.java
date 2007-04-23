/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.render;

import java.awt.Rectangle;
import java.util.Locale;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.LengthValue;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.Layer;

public class PageBox {
    private CalculatedStyle _style;
    
    private int _top;
    private int _bottom;
    
    private int _paintingTop;
    private int _paintingBottom;
    
    private int _pageNo;
    
    private int _outerPageWidth;
    
    private PageDimensions _pageDimensions;
    
    public int getWidth(CssContext cssCtx) {
        resolvePageDimensions(cssCtx);
        
        return _pageDimensions.getWidth();
    }

    public int getHeight(CssContext cssCtx) {
        resolvePageDimensions(cssCtx);
        
        return _pageDimensions.getHeight();
    }
    
    private void resolvePageDimensions(CssContext cssCtx) {
        if (_pageDimensions == null) {
            CalculatedStyle style = getStyle();
            
            int width;
            int height;
            
            if (style.isLength(CSSName.FS_PAGE_WIDTH)) {
                width = (int)style.getFloatPropertyProportionalTo(
                        CSSName.FS_PAGE_WIDTH, 0, cssCtx);
            } else {
                width = resolveAutoPageWidth(cssCtx);
            }
            
            if (style.isLength(CSSName.FS_PAGE_HEIGHT)) {
                height = (int)style.getFloatPropertyProportionalTo(
                        CSSName.FS_PAGE_HEIGHT, 0, cssCtx);
            } else {
                height = resolveAutoPageHeight(cssCtx);
            }
            
            if (style.isIdent(CSSName.FS_PAGE_ORIENTATION, IdentValue.LANDSCAPE)) {
                int temp;
                
                temp = width;
                width = height;
                height = temp;
            }
            
            PageDimensions dim = new PageDimensions();
            dim.setWidth(width);
            dim.setHeight(height);
            
            _pageDimensions = dim;
        }
    }
    
    private boolean isUseLetterSize() {
        Locale l = Locale.getDefault();
        String county = l.getCountry();
        
        // Per http://en.wikipedia.org/wiki/Paper_size, letter paper is
        // a de facto standard in Canada (although the government uses
        // its own standard) and Mexico (even though it is officially an ISO
        // country)
        return county.equals("US") || county.equals("CA") || county.equals("MX"); 
    }
    
    private int resolveAutoPageWidth(CssContext cssCtx) {
        if (isUseLetterSize()) {
            return (int)LengthValue.calcFloatProportionalValue(
                    getStyle(),
                    CSSName.FS_PAGE_WIDTH,
                    "8.5in",
                    8.5f,
                    CSSPrimitiveValue.CSS_IN,
                    0,
                    cssCtx);
        } else {
            return (int)LengthValue.calcFloatProportionalValue(
                    getStyle(),
                    CSSName.FS_PAGE_WIDTH,
                    "210mm",
                    210f,
                    CSSPrimitiveValue.CSS_MM,
                    0,
                    cssCtx);            
        }
    }
    
    private int resolveAutoPageHeight(CssContext cssCtx) {
        if (isUseLetterSize()) {
            return (int)LengthValue.calcFloatProportionalValue(
                    getStyle(),
                    CSSName.FS_PAGE_HEIGHT,
                    "11in",
                    11f,
                    CSSPrimitiveValue.CSS_IN,
                    0,
                    cssCtx);
        } else {
            return (int)LengthValue.calcFloatProportionalValue(
                    getStyle(),
                    CSSName.FS_PAGE_HEIGHT,
                    "297mm",
                    297f,
                    CSSPrimitiveValue.CSS_MM,
                    0,
                    cssCtx);            
        }
    }    

    public int getContentHeight(CssContext cssCtx) {
        return getHeight(cssCtx) 
            - getMarginBorderPadding(cssCtx, CalculatedStyle.TOP)
            - getMarginBorderPadding(cssCtx, CalculatedStyle.BOTTOM);
    }
    
    public int getContentWidth(CssContext cssCtx) {
        return getWidth(cssCtx) 
            - getMarginBorderPadding(cssCtx, CalculatedStyle.LEFT)
            - getMarginBorderPadding(cssCtx, CalculatedStyle.RIGHT);
    }
    
    public CalculatedStyle getStyle() {
        return _style;
    }

    public void setStyle(CalculatedStyle style) {
        _style = style;
    }

    public int getBottom() {
        return _bottom;
    }

    public int getTop() {
        return _top;
    }
    
    public void setTopAndBottom(CssContext cssCtx, int top) {
        _top = top;
        _bottom = top + getContentHeight(cssCtx);
    }

    public int getPaintingBottom() {
        return _paintingBottom;
    }

    public void setPaintingBottom(int paintingBottom) {
        _paintingBottom = paintingBottom;
    }

    public int getPaintingTop() {
        return _paintingTop;
    }

    public void setPaintingTop(int paintingTop) {
        _paintingTop = paintingTop;
    }
    
    public Rectangle getOverallPaintingBounds(CssContext cssCtx, int additionalClearance) {
        return new Rectangle(
                additionalClearance, getPaintingTop(),
                getWidth(cssCtx), getPaintingBottom()-getPaintingTop());
    }
    
    public Rectangle getPagedViewClippingBounds(CssContext cssCtx, int additionalClearance) {
        Rectangle result = new Rectangle(
                additionalClearance + 
                    getMarginBorderPadding(cssCtx, CalculatedStyle.LEFT),
                getPaintingTop() + 
                    getMarginBorderPadding(cssCtx, CalculatedStyle.TOP),
                getContentWidth(cssCtx),
                getContentHeight(cssCtx));

        return result;
    }
    
    public Rectangle getPrintingClippingBounds(CssContext cssCtx) {
        Rectangle result = new Rectangle(
                getMarginBorderPadding(cssCtx, CalculatedStyle.LEFT),
                getMarginBorderPadding(cssCtx, CalculatedStyle.TOP),
                getContentWidth(cssCtx),
                getContentHeight(cssCtx));
        
        result.height -= 1;

        return result;
    }
    
    public RectPropertySet getMargin(CssContext cssCtx) {
        return getStyle().getMarginRect(_outerPageWidth, cssCtx);
    }
    
    public Rectangle getFlowBounds(CssContext cssCtx, String name) {
        CalculatedStyle style = getStyle();
        String flow = null; 
        if (! style.getStringProperty(CSSName.FS_FLOW_TOP).equals("none")) {
            flow = style.getStringProperty(CSSName.FS_FLOW_TOP);
            if (name.equals(flow)) {
                return new Rectangle(
                        0, 0,
                        getWidth(cssCtx) - (int)getMargin(cssCtx).width(),
                        (int)getMargin(cssCtx).top());
            }
        } 
        if (! style.getStringProperty(CSSName.FS_FLOW_RIGHT).equals("none")) {
            flow = style.getStringProperty(CSSName.FS_FLOW_RIGHT);
            if (name.equals(flow)) {
                return new Rectangle(
                        0, 0,
                        (int)getMargin(cssCtx).right(),
                        getHeight(cssCtx) - (int)getMargin(cssCtx).height());
            }
        } 
        if (! style.getStringProperty(CSSName.FS_FLOW_BOTTOM).equals("none")) {
            flow = style.getStringProperty(CSSName.FS_FLOW_BOTTOM);
            if (name.equals(flow)) {
                return new Rectangle(
                        0, 0,
                        getWidth(cssCtx) - (int)getMargin(cssCtx).width(),
                        (int)getMargin(cssCtx).bottom());
            }
        } 
        if (! style.getStringProperty(CSSName.FS_FLOW_LEFT).equals("none")) {
            flow = style.getStringProperty(CSSName.FS_FLOW_LEFT);
            if (name.equals(flow)) {
                return new Rectangle(
                        0, 0,
                        (int)getMargin(cssCtx).left(),
                        getHeight(cssCtx) - (int)getMargin(cssCtx).height());
            }
        }
        
        return null;
    }
    
    public void paintAlternateFlows(RenderingContext c, Layer root, 
            short mode) {
        paintAlternateFlows(c, root, mode, 0);
    }
    
    public void paintAlternateFlows(RenderingContext c, Layer root, 
            short mode, int additionalClearance) {
        paintTopFlow(c, root, mode, additionalClearance);
        paintBottomFlow(c, root, mode, additionalClearance);
        paintLeftFlow(c, root, mode, additionalClearance);
        paintRightFlow(c, root, mode, additionalClearance);
    }
    
    private void paintTopFlow(RenderingContext c, Layer root, 
            short mode, int additionalClearance) {
        CalculatedStyle style = getStyle();
        String flowName = style.getStringProperty(CSSName.FS_FLOW_TOP);
        if (! flowName.equals("none")) {
            int left = additionalClearance + (int)getMargin(c).left();
            int top;
            if (mode == Layer.PAGED_MODE_SCREEN) {
                top = getPaintingTop();
            } else if (mode == Layer.PAGED_MODE_PRINT) {
                top = 0;
            } else {
                throw new IllegalArgumentException("Illegal mode");
            }
            
            paintFlow(c, root, flowName, left, top);
        }
    }
    
    private void paintBottomFlow(RenderingContext c, Layer root, 
            short mode, int additionalClearance) {
        CalculatedStyle style = getStyle();
        String flowName = style.getStringProperty(CSSName.FS_FLOW_BOTTOM);
        if (! flowName.equals("none")) {
            int left = additionalClearance + (int)getMargin(c).left();
            int top;
            
            if (mode == Layer.PAGED_MODE_SCREEN) {
                top = getPaintingBottom() - (int)getMargin(c).bottom();
            } else if (mode == Layer.PAGED_MODE_PRINT) {
                top = getHeight(c) - (int)getMargin(c).bottom();
            } else {
                throw new IllegalArgumentException("Illegal mode");
            }
            
            paintFlow(c, root, flowName, left, top);
        }
    }
    
    private void paintLeftFlow(RenderingContext c, Layer root, 
            short mode, int additionalClearance) {
        CalculatedStyle style = getStyle();
        String flowName = style.getStringProperty(CSSName.FS_FLOW_LEFT);
        if (! flowName.equals("none")) {
            int left = additionalClearance;
            int top;
            
            if (mode == Layer.PAGED_MODE_SCREEN) {
                top = getPaintingTop() + (int)getMargin(c).top();
            } else if (mode == Layer.PAGED_MODE_PRINT) {
                top = (int)getMargin(c).top();
            } else {
                throw new IllegalArgumentException("Illegal mode");
            }
            
            paintFlow(c, root, flowName, left, top);
        }
    }
    
    private void paintRightFlow(RenderingContext c, Layer root, 
            short mode, int additionalClearance) {
        CalculatedStyle style = getStyle();
        String flowName = style.getStringProperty(CSSName.FS_FLOW_RIGHT);
        if (! flowName.equals("none")) {
            int left = additionalClearance + getWidth(c) 
                - (int)getMargin(c).right();
            int top;
            
            if (mode == Layer.PAGED_MODE_SCREEN) {
                top = getPaintingTop() + (int)getMargin(c).top();
            } else if (mode == Layer.PAGED_MODE_PRINT) {
                top = (int)getMargin(c).top();
            } else {
                throw new IllegalArgumentException("Illegal mode");
            }
            
            paintFlow(c, root, flowName, left, top);
        }
    }
    
    private void paintFlow(RenderingContext c, Layer root,
            String flowName, int left, int top) {
        Layer flow = root.getAlternateFlow(flowName);
        if (flow != null) {
            c.getOutputDevice().translate(left, top);
            flow.paint(c, 0, 0, true);
            c.getOutputDevice().translate(-left, -top);
        }
    }
    
    private Rectangle getBorderEdge(int left, int top, CssContext cssCtx) {
        RectPropertySet margin = getMargin(cssCtx);
        Rectangle result = new Rectangle(left + (int) margin.left(),
                top + (int) margin.top(),
                getWidth(cssCtx) - (int) margin.left() - (int) margin.right(),
                getHeight(cssCtx) - (int) margin.top() - (int) margin.bottom());
        return result;
    }
    
    public void paintBorder(RenderingContext c, int additionalClearance, short mode) {
        int top = 0;
        if (mode == Layer.PAGED_MODE_SCREEN) {
            top = getPaintingTop();
        }
        c.getOutputDevice().paintBorder(c, 
                getStyle(),
                getBorderEdge(additionalClearance, top, c),
                BorderPainter.ALL);
    }

    public int getPageNo() {
        return _pageNo;
    }

    public void setPageNo(int pageNo) {
        _pageNo = pageNo;
    }

    public int getOuterPageWidth() {
        return _outerPageWidth;
    }

    public void setOuterPageWidth(int containingBlockWidth) {
        _outerPageWidth = containingBlockWidth;
    }
    
    public int getMarginBorderPadding(CssContext cssCtx, int which) {
        return getStyle().getMarginBorderPadding(
                cssCtx, (int)getOuterPageWidth(), which);
    }
    
    private static final class PageDimensions {
        private int _width;
        private int _height;

        public int getHeight() {
            return _height;
        }

        public void setHeight(int height) {
            _height = height;
        }

        public int getWidth() {
            return _width;
        }

        public void setWidth(int width) {
            _width = width;
        }
    }
}
