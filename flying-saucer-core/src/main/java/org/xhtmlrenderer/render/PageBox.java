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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.constants.MarginBoxName;
import org.xhtmlrenderer.css.newmatch.PageInfo;
import org.xhtmlrenderer.css.parser.FSFunction;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.LengthValue;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.newtable.TableBox;

public class PageBox {
    private static final MarginArea[] MARGIN_AREA_DEFS = new MarginArea[] {
        new TopLeftCorner(),
        new TopMarginArea(),
        new TopRightCorner(),
        
        new LeftMarginArea(),
        new RightMarginArea(),
        
        new BottomLeftCorner(),
        new BottomMarginArea(),
        new BottomRightCorner(),
    };
    
    private static final int LEADING_TRAILING_SPLIT = 5;
    
    private CalculatedStyle _style;
    
    private int _top;
    private int _bottom;
    
    private int _paintingTop;
    private int _paintingBottom;
    
    private int _pageNo;
    
    private int _outerPageWidth;
    
    private PageDimensions _pageDimensions;
    
    private PageInfo _pageInfo;
    
    private MarginAreaContainer[] _marginAreas = new MarginAreaContainer[MARGIN_AREA_DEFS.length];
    
    private Element _metadata;
    
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
        int retval = getHeight(cssCtx) - getMarginBorderPadding(cssCtx, CalculatedStyle.TOP)
                - getMarginBorderPadding(cssCtx, CalculatedStyle.BOTTOM);
        if (retval <= 0) {
            throw new IllegalArgumentException(
                    "The content height cannot be zero or less.  Check your document margin definition.");
        }
        return retval;
    }

    public int getContentWidth(CssContext cssCtx) {
        int retval = getWidth(cssCtx) - getMarginBorderPadding(cssCtx, CalculatedStyle.LEFT)
                - getMarginBorderPadding(cssCtx, CalculatedStyle.RIGHT);
        if (retval <= 0) {
            throw new IllegalArgumentException(
                    "The content width cannot be zero or less.  Check your document margin definition.");
        }
        return retval;
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
    
    public Rectangle getScreenPaintingBounds(CssContext cssCtx, int additionalClearance) {
        return new Rectangle(
                additionalClearance, getPaintingTop(),
                getWidth(cssCtx), getPaintingBottom()-getPaintingTop());
    }
    
    public Rectangle getPrintPaintingBounds(CssContext cssCtx) {
        return new Rectangle(
                0, 0,
                getWidth(cssCtx), getHeight(cssCtx));
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
    
    public Rectangle getPrintClippingBounds(CssContext cssCtx) {
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
    
    public void paintBackground(RenderingContext c, int additionalClearance, short mode) {
        Rectangle bounds;
        if (mode == Layer.PAGED_MODE_SCREEN) {
            bounds = getScreenPaintingBounds(c, additionalClearance);
        } else {
            bounds = getPrintPaintingBounds(c);
        }
        
        c.getOutputDevice().paintBackground(c, getStyle(), bounds, bounds, getStyle().getBorder(c));
    }
    
    public void paintMarginAreas(RenderingContext c, int additionalClearance, short mode) {
        for (int i = 0; i < MARGIN_AREA_DEFS.length; i++) {
            MarginAreaContainer container = _marginAreas[i];
            if (container != null) {
                TableBox table = _marginAreas[i].getTable();
                Point p = container.getArea().getPaintingPosition(
                        c, this, additionalClearance, mode);
                
                c.getOutputDevice().translate(p.x, p.y);
                table.getLayer().paint(c);
                c.getOutputDevice().translate(-p.x, -p.y);
            }
        }
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

    public PageInfo getPageInfo() {
        return _pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        _pageInfo = pageInfo;
    }
    
    public Element getMetadata() {
        return _metadata;
    }
    
    public void layout(LayoutContext c) {
        c.setPage(this);
        retrievePageMetadata(c);
        layoutMarginAreas(c);
    }
    
    // HACK Would much prefer to do this in ITextRenderer or ITextOutputDevice
    // but given the existing API, this is about the only place it can be done
    private void retrievePageMetadata(LayoutContext c) {
        List props = getPageInfo().getXMPPropertyList();
        if (props != null && props.size() > 0)
        {
            for (Iterator i = props.iterator(); i.hasNext(); ) {
                PropertyDeclaration decl = (PropertyDeclaration)i.next();
                if (decl.getCSSName() == CSSName.CONTENT) {
                    PropertyValue value = (PropertyValue)decl.getValue();
                    List values = value.getValues();
                    if (values.size() == 1) {
                        PropertyValue funcVal = (PropertyValue)values.get(0);
                        if (funcVal.getPropertyValueType() == PropertyValue.VALUE_TYPE_FUNCTION) {
                            FSFunction func = funcVal.getFunction();
                            if (BoxBuilder.isElementFunction(func)) {
                                BlockBox metadata = BoxBuilder.getRunningBlock(c, funcVal);
                                if (metadata != null) {
                                    _metadata = metadata.getElement();
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    private void layoutMarginAreas(LayoutContext c) {
        RectPropertySet margin = getMargin(c);
        for (int i = 0; i < MARGIN_AREA_DEFS.length; i++) {
            MarginArea area = MARGIN_AREA_DEFS[i];
            
            Dimension dim = area.getLayoutDimension(c, this, margin);
            TableBox table = BoxBuilder.createMarginTable(
                    c, _pageInfo, 
                    area.getMarginBoxNames(),
                    (int)dim.getHeight(),
                    area.getDirection());
            if (table != null) {
                table.setContainingBlock(new MarginBox(new Rectangle((int)dim.getWidth(), (int)dim.getHeight())));
                try {
                    c.setNoPageBreak(1);
                    
                    c.reInit(false);
                    c.pushLayer(table);
                    c.getRootLayer().addPage(c);
                    
                    table.layout(c);
                    
                    c.popLayer();
                } finally {
                    c.setNoPageBreak(0);
                }
                _marginAreas[i] = new MarginAreaContainer(area, table);
            }
        }
    }
    
    public boolean isLeftPage() {
        return _pageNo % 2 != 0;
    }
    
    public boolean isRightPage() {
        return _pageNo % 2 == 0;
    }
    
    public void exportLeadingText(RenderingContext c, Writer writer) throws IOException {
        for (int i = 0; i < LEADING_TRAILING_SPLIT; i++) {
            MarginAreaContainer container = _marginAreas[i];
            if (container != null) {
                container.getTable().exportText(c, writer);
            }
        }
    }
    
    public void exportTrailingText(RenderingContext c, Writer writer) throws IOException {
        for (int i = LEADING_TRAILING_SPLIT; i < _marginAreas.length; i++) {
            MarginAreaContainer container = _marginAreas[i];
            if (container != null) {
                container.getTable().exportText(c, writer);
            }
        }
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
    
    private static class MarginAreaContainer {
        private final MarginArea _area;
        private final TableBox _table;
        
        public MarginAreaContainer(MarginArea area, TableBox table) {
            _area = area;
            _table = table;
        }

        public MarginArea getArea() {
            return _area;
        }

        public TableBox getTable() {
            return _table;
        }
    }
    
    private static abstract class MarginArea {
        private final MarginBoxName[] _marginBoxNames;
        private TableBox _table;
        
        public abstract Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin);
        public abstract Point getPaintingPosition(
                RenderingContext c, PageBox page, int additionalClearance, short mode);
        
        public MarginArea(MarginBoxName marginBoxName) {
            _marginBoxNames = new MarginBoxName[] { marginBoxName };
        }
        
        public MarginArea(MarginBoxName[] marginBoxNames) {
            _marginBoxNames = marginBoxNames;
        }

        public TableBox getTable() {
            return _table;
        }

        public void setTable(TableBox table) {
            _table = table;
        }
        
        public MarginBoxName[] getMarginBoxNames() {
            return _marginBoxNames;
        }
        
        public int getDirection() {
            return BoxBuilder.MARGIN_BOX_HORIZONTAL;
        }
    }
    
    private static class TopLeftCorner extends MarginArea {
        public TopLeftCorner() {
            super(MarginBoxName.TOP_LEFT_CORNER);
        }

        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension((int)margin.left(), (int)margin.top());
        }

        public Point getPaintingPosition(
                RenderingContext c, PageBox page, int additionalClearance, short mode) {
            int left = additionalClearance;
            int top;
            if (mode == Layer.PAGED_MODE_SCREEN) {
                top = page.getPaintingTop();
            } else if (mode == Layer.PAGED_MODE_PRINT) {
                top = 0;
            } else {
                throw new IllegalArgumentException("Illegal mode");
            }
            
            return new Point(left, top);
        }

    }
    
    private static class TopRightCorner extends MarginArea {
        public TopRightCorner() {
            super(MarginBoxName.TOP_RIGHT_CORNER);
        }

        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension((int)margin.right(), (int)margin.top());
        }

        public Point getPaintingPosition(
                RenderingContext c, PageBox page, int additionalClearance, short mode) {
            int left = additionalClearance + page.getWidth(c) - (int)page.getMargin(c).right();
            int top;
            if (mode == Layer.PAGED_MODE_SCREEN) {
                top = page.getPaintingTop();
            } else if (mode == Layer.PAGED_MODE_PRINT) {
                top = 0;
            } else {
                throw new IllegalArgumentException("Illegal mode");
            }
            
            return new Point(left, top);
        }
    }
    
    private static class BottomRightCorner extends MarginArea {
        public BottomRightCorner() {
            super(MarginBoxName.BOTTOM_RIGHT_CORNER);
        }

        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension((int)margin.right(), (int)margin.bottom());
        }

        public Point getPaintingPosition(
                RenderingContext c, PageBox page, int additionalClearance, short mode) {
            int left = additionalClearance + page.getWidth(c) - (int)page.getMargin(c).right();
            int top;
            
            if (mode == Layer.PAGED_MODE_SCREEN) {
                top = page.getPaintingBottom() - (int)page.getMargin(c).bottom();
            } else if (mode == Layer.PAGED_MODE_PRINT) {
                top = page.getHeight(c) - (int)page.getMargin(c).bottom();
            } else {
                throw new IllegalArgumentException("Illegal mode");
            } 
            
            return new Point(left, top);
        }
    }
    
    private static class BottomLeftCorner extends MarginArea {
        public BottomLeftCorner() {
            super(MarginBoxName.BOTTOM_LEFT_CORNER);
        }

        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension((int)margin.left(), (int)margin.bottom());
        }

        public Point getPaintingPosition(
                RenderingContext c, PageBox page, int additionalClearance, short mode) {
            int left = additionalClearance;
            int top;
            
            if (mode == Layer.PAGED_MODE_SCREEN) {
                top = page.getPaintingBottom() - (int)page.getMargin(c).bottom();
            } else if (mode == Layer.PAGED_MODE_PRINT) {
                top = page.getHeight(c) - (int)page.getMargin(c).bottom();
            } else {
                throw new IllegalArgumentException("Illegal mode");
            } 
            
            return new Point(left, top);
        }
    }
    
    private static class LeftMarginArea extends MarginArea {
        public LeftMarginArea() {
            super(new MarginBoxName[] {
                    MarginBoxName.LEFT_TOP, 
                    MarginBoxName.LEFT_MIDDLE, 
                    MarginBoxName.LEFT_BOTTOM });
        }

        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension((int)margin.left(), page.getContentHeight(c));
        }

        public Point getPaintingPosition(
                RenderingContext c, PageBox page, int additionalClearance, short mode) {
            int left = additionalClearance;
            int top;
            if (mode == Layer.PAGED_MODE_SCREEN) {
                top = page.getPaintingTop() + (int)page.getMargin(c).top();
            } else if (mode == Layer.PAGED_MODE_PRINT) {
                top = (int)page.getMargin(c).top();
            } else {
                throw new IllegalArgumentException("Illegal mode");
            }
            
            return new Point(left, top);
        }
        
        public int getDirection() {
            return BoxBuilder.MARGIN_BOX_VERTICAL;
        }
    } 
    
    private static class RightMarginArea extends MarginArea {
        public RightMarginArea() {
            super(new MarginBoxName[] {
                    MarginBoxName.RIGHT_TOP, 
                    MarginBoxName.RIGHT_MIDDLE, 
                    MarginBoxName.RIGHT_BOTTOM });
        }

        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension((int)margin.left(), page.getContentHeight(c));
        }

        public Point getPaintingPosition(
                RenderingContext c, PageBox page, int additionalClearance, short mode) {
            int left = additionalClearance + page.getWidth(c) - (int)page.getMargin(c).right();
            int top;
            if (mode == Layer.PAGED_MODE_SCREEN) {
                top = page.getPaintingTop() + (int)page.getMargin(c).top();
            } else if (mode == Layer.PAGED_MODE_PRINT) {
                top = (int)page.getMargin(c).top();
            } else {
                throw new IllegalArgumentException("Illegal mode");
            }
            
            return new Point(left, top);
        }
        
        public int getDirection() {
            return BoxBuilder.MARGIN_BOX_VERTICAL;
        }        
    }
    
    private static class TopMarginArea extends MarginArea {
        public TopMarginArea() {
            super(new MarginBoxName[] { 
                    MarginBoxName.TOP_LEFT, 
                    MarginBoxName.TOP_CENTER, 
                    MarginBoxName.TOP_RIGHT });
        }

        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension(page.getContentWidth(c), (int)margin.top());
        }

        public Point getPaintingPosition(
                RenderingContext c, PageBox page, int additionalClearance, short mode) {
            int left = additionalClearance + (int)page.getMargin(c).left();
            int top;
            if (mode == Layer.PAGED_MODE_SCREEN) {
                top = page.getPaintingTop();
            } else if (mode == Layer.PAGED_MODE_PRINT) {
                top = 0;
            } else {
                throw new IllegalArgumentException("Illegal mode");
            }   
            
            return new Point(left, top);
        }
    }   
    
    private static class BottomMarginArea extends MarginArea {
        public BottomMarginArea() {
            super(new MarginBoxName[] { 
                    MarginBoxName.BOTTOM_LEFT, 
                    MarginBoxName.BOTTOM_CENTER, 
                    MarginBoxName.BOTTOM_RIGHT });
        }

        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension(page.getContentWidth(c), (int)margin.bottom());
        }

        public Point getPaintingPosition(
                RenderingContext c, PageBox page, int additionalClearance, short mode) {
            int left = additionalClearance + (int)page.getMargin(c).left();
            int top;
            
            if (mode == Layer.PAGED_MODE_SCREEN) {
                top = page.getPaintingBottom() - (int)page.getMargin(c).bottom();
            } else if (mode == Layer.PAGED_MODE_PRINT) {
                top = page.getHeight(c) - (int)page.getMargin(c).bottom();
            } else {
                throw new IllegalArgumentException("Illegal mode");
            }    
            
            return new Point(left, top);
        }
    } 
}
