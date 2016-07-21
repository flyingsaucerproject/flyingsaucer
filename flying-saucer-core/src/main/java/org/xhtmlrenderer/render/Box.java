/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 * Copyright (c) 2005, 2006 Wisconsin Court System
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
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.PaintingInfo;
import org.xhtmlrenderer.layout.Styleable;
import org.xhtmlrenderer.util.XRLog;

public abstract class Box implements Styleable {
    protected static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private Element _element;

    private int _x;
    private int _y;

    private int _absY;
    private int _absX;

    /**
     * Box width.
     */
    private int _contentWidth;
    private int _rightMBP = 0;
    private int _leftMBP = 0;

    /**
     * Box height.
     */
    private int _height;

    private Layer _layer = null;
    private Layer _containingLayer;

    private Box _parent;

    private List _boxes;

    /**
     * Keeps track of the start of childrens containing block.
     */
    private int _tx;
    private int _ty;

    private CalculatedStyle _style;
    private Box _containingBlock;

    private Dimension _relativeOffset;

    private PaintingInfo _paintingInfo;

    private RectPropertySet _workingMargin;

    private int _index;

    private String _pseudoElementOrClass;

    private boolean _anonymous;

    protected Box() {
    }

    public abstract String dump(LayoutContext c, String indent, int which);

    protected void dumpBoxes(
            LayoutContext c, String indent, List boxes,
            int which, StringBuffer result) {
        for (Iterator i = boxes.iterator(); i.hasNext(); ) {
            Box b = (Box)i.next();
            result.append(b.dump(c, indent + "  ", which));
            if (i.hasNext()) {
                result.append('\n');
            }
        }
    }

    public int getWidth() {
        return getContentWidth() + getLeftMBP() + getRightMBP();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Box: ");
        sb.append(" (" + getAbsX() + "," + getAbsY() + ")->(" + getWidth() + " x " + getHeight() + ")");
        return sb.toString();
    }

    public void addChildForLayout(LayoutContext c, Box child) {
        addChild(child);

        child.initContainingLayer(c);
    }

    public void addChild(Box child) {
        if (_boxes == null) {
            _boxes = new ArrayList();
        }
        if (child == null) {
            throw new NullPointerException("trying to add null child");
        }
        child.setParent(this);
        child.setIndex(_boxes.size());
        _boxes.add(child);
    }

    public void addAllChildren(List children) {
        for (Iterator i = children.iterator(); i.hasNext(); ) {
            Box box = (Box)i.next();
            addChild(box);
        }
    }

    public void removeAllChildren() {
        if (_boxes != null) {
            _boxes.clear();
        }
    }

    public void removeChild(Box target) {
        if (_boxes != null) {
            boolean found = false;
            for (Iterator i = getChildIterator(); i.hasNext(); ) {
                Box child = (Box)i.next();
                if (child.equals(target)) {
                    i.remove();
                    found = true;
                } else if (found) {
                    child.setIndex(child.getIndex()-1);
                }
            }
        }
    }

    public Box getPreviousSibling() {
        Box parent = getParent();
        return parent == null ? null : parent.getPrevious(this);
    }

    public Box getNextSibling() {
        Box parent = getParent();
        return parent == null ? null : parent.getNext(this);
    }

    protected Box getPrevious(Box child) {
        return child.getIndex() == 0 ? null : getChild(child.getIndex()-1);
    }

    protected Box getNext(Box child) {
        return child.getIndex() == getChildCount() - 1 ? null : getChild(child.getIndex()+1);
    }

    public void removeChild(int i) {
        if (_boxes != null) {
            removeChild(getChild(i));
        }
    }

    public void setParent(Box box) {
        _parent = box;
    }

    public Box getParent() {
        return _parent;
    }

    public Box getDocumentParent() {
        return getParent();
    }

    public int getChildCount() {
        return _boxes == null ? 0 : _boxes.size();
    }

    public Box getChild(int i) {
        if (_boxes == null) {
            throw new IndexOutOfBoundsException();
        } else {
            return (Box) _boxes.get(i);
        }
    }

    public Iterator getChildIterator() {
        return _boxes == null ? Collections.EMPTY_LIST.iterator() : _boxes.iterator();
    }

    public List getChildren() {
        return _boxes == null ? Collections.EMPTY_LIST : _boxes;
    }

    public static final int NOTHING = 0;
    public static final int FLUX = 1;
    public static final int CHILDREN_FLUX = 2;
    public static final int DONE = 3;

    private int _state = NOTHING;

    public static final int DUMP_RENDER = 2;

    public static final int DUMP_LAYOUT = 1;

    public synchronized int getState() {
        return _state;
    }

    public synchronized void setState(int state) {
        _state = state;
    }

    public static String stateToString(int state) {
        switch (state) {
            case NOTHING:
                return "NOTHING";
            case FLUX:
                return "FLUX";
            case CHILDREN_FLUX:
                return "CHILDREN_FLUX";
            case DONE:
                return "DONE";
            default:
                return "unknown";
        }
    }

    public final CalculatedStyle getStyle() {
        return _style;
    }

    public void setStyle(CalculatedStyle style) {
        _style = style;
    }

    public Box getContainingBlock() {
        return _containingBlock == null ? getParent() : _containingBlock;
    }

    public void setContainingBlock(Box containingBlock) {
        _containingBlock = containingBlock;
    }

    public Rectangle getMarginEdge(int left, int top, CssContext cssCtx, int tx, int ty) {
        // Note that negative margins can mean this rectangle is inside the border
        // edge, but that's the way it's supposed to work...
        Rectangle result = new Rectangle(left, top, getWidth(), getHeight());
        result.translate(tx, ty);
        return result;
    }

    public Rectangle getMarginEdge(CssContext cssCtx, int tx, int ty) {
        return getMarginEdge(getX(), getY(), cssCtx, tx, ty);
    }

    public Rectangle getPaintingBorderEdge(CssContext cssCtx) {
        return getBorderEdge(getAbsX(), getAbsY(), cssCtx);
    }

    public Rectangle getPaintingPaddingEdge(CssContext cssCtx) {
        return getPaddingEdge(getAbsX(), getAbsY(), cssCtx);
    }

    public Rectangle getPaintingClipEdge(CssContext cssCtx) {
        return getPaintingBorderEdge(cssCtx);
    }

    public Rectangle getChildrenClipEdge(RenderingContext c) {
        return getPaintingPaddingEdge(c);
    }

    /**
     * <B>NOTE</B>: This method does not consider any children of this box
     */
    public boolean intersects(CssContext cssCtx, Shape clip) {
        return clip == null || clip.intersects(getPaintingClipEdge(cssCtx));
    }

    public Rectangle getBorderEdge(int left, int top, CssContext cssCtx) {
        RectPropertySet margin = getMargin(cssCtx);
        Rectangle result = new Rectangle(left + (int) margin.left(),
                top + (int) margin.top(),
                getWidth() - (int) margin.left() - (int) margin.right(),
                getHeight() - (int) margin.top() - (int) margin.bottom());
        return result;
    }

    public Rectangle getPaddingEdge(int left, int top, CssContext cssCtx) {
        RectPropertySet margin = getMargin(cssCtx);
        RectPropertySet border = getBorder(cssCtx);
        Rectangle result = new Rectangle(left + (int) margin.left() + (int) border.left(),
                top + (int) margin.top() + (int) border.top(),
                getWidth() - (int) margin.width() - (int) border.width(),
                getHeight() - (int) margin.height() - (int) border.height());
        return result;
    }

    protected int getPaddingWidth(CssContext cssCtx) {
        RectPropertySet padding = getPadding(cssCtx);
        return (int)padding.left() + getContentWidth() + (int)padding.right();
    }

    public Rectangle getContentAreaEdge(int left, int top, CssContext cssCtx) {
        RectPropertySet margin = getMargin(cssCtx);
        RectPropertySet border = getBorder(cssCtx);
        RectPropertySet padding = getPadding(cssCtx);

        Rectangle result = new Rectangle(
                left + (int)margin.left() + (int)border.left() + (int)padding.left(),
                top + (int)margin.top() + (int)border.top() + (int)padding.top(),
                getWidth() - (int)margin.width() - (int)border.width() - (int)padding.width(),
                getHeight() - (int) margin.height() - (int) border.height() - (int) padding.height());
        return result;
    }

    public Layer getLayer() {
        return _layer;
    }

    public void setLayer(Layer layer) {
        _layer = layer;
    }

    public Dimension positionRelative(CssContext cssCtx) {
        int initialX = getX();
        int initialY = getY();

        CalculatedStyle style = getStyle();
        if (! style.isIdent(CSSName.LEFT, IdentValue.AUTO)) {
            setX(getX() + (int)style.getFloatPropertyProportionalWidth(
                    CSSName.LEFT, getContainingBlock().getContentWidth(), cssCtx));
        } else if (! style.isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
            setX(getX() - (int)style.getFloatPropertyProportionalWidth(
                    CSSName.RIGHT, getContainingBlock().getContentWidth(), cssCtx));
        }

        int cbContentHeight = 0;
        if (! getContainingBlock().getStyle().isAutoHeight()) {
            CalculatedStyle cbStyle = getContainingBlock().getStyle();
            cbContentHeight = (int)cbStyle.getFloatPropertyProportionalHeight(
                    CSSName.HEIGHT, 0, cssCtx);
        } else if (isInlineBlock()) {
            // FIXME Should be content height, not overall height
            cbContentHeight = getContainingBlock().getHeight();
        }

        if (!style.isIdent(CSSName.TOP, IdentValue.AUTO)) {
            setY(getY() + ((int)style.getFloatPropertyProportionalHeight(
                    CSSName.TOP, cbContentHeight, cssCtx)));
        } else if (!style.isIdent(CSSName.BOTTOM, IdentValue.AUTO)) {
            setY(getY() - ((int)style.getFloatPropertyProportionalHeight(
                    CSSName.BOTTOM, cbContentHeight, cssCtx)));
        }

        setRelativeOffset(new Dimension(getX() - initialX, getY() - initialY));
        return getRelativeOffset();
    }

    protected boolean isInlineBlock()
    {
        return false;
    }

    public void setAbsY(int absY) {
        _absY = absY;
    }

    public int getAbsY() {
        return _absY;
    }

    public void setAbsX(int absX) {
        _absX = absX;
    }

    public int getAbsX() {
        return _absX;
    }

    public boolean isStyled() {
        return _style != null;
    }

    public int getBorderSides() {
        return BorderPainter.ALL;
    }

    public void paintBorder(RenderingContext c) {
        c.getOutputDevice().paintBorder(c, this);
    }

    private boolean isPaintsRootElementBackground() {
        return (isRoot() && getStyle().isHasBackground()) ||
                (isBody() && ! getParent().getStyle().isHasBackground());
    }

    public void paintBackground(RenderingContext c) {
        if (! isPaintsRootElementBackground()) {
            c.getOutputDevice().paintBackground(c, this);
        }
    }

    public void paintRootElementBackground(RenderingContext c) {
        PaintingInfo pI = getPaintingInfo();
        if (pI != null) {
            if (getStyle().isHasBackground()) {
                paintRootElementBackground(c, pI);
            } else if (getChildCount() > 0) {
                Box body = getChild(0);
                body.paintRootElementBackground(c, pI);
            }
        }
    }

    private void paintRootElementBackground(RenderingContext c, PaintingInfo pI) {
        Dimension marginCorner = pI.getOuterMarginCorner();
        Rectangle canvasBounds = new Rectangle(0, 0, marginCorner.width, marginCorner.height);
        canvasBounds.add(c.getViewportRectangle());
        c.getOutputDevice().paintBackground(c, getStyle(), canvasBounds, canvasBounds, BorderPropertySet.EMPTY_BORDER);
    }

    public Layer getContainingLayer() {
        return _containingLayer;
    }

    public void setContainingLayer(Layer containingLayer) {
        _containingLayer = containingLayer;
    }

    public void initContainingLayer(LayoutContext c) {
        if (getLayer() != null) {
            setContainingLayer(getLayer());
        } else if (getContainingLayer() == null) {
            if (getParent() == null || getParent().getContainingLayer() == null) {
                throw new RuntimeException("internal error");
            }
            setContainingLayer(getParent().getContainingLayer());

            // FIXME Will be glacially slow for large inline relative layers.  Could
            // be much more efficient.  We're just looking for block boxes which are
            // directly wrapped by an inline relative layer (i.e. block boxes sandwiched
            // between anonymous block boxes)
            if (c.getLayer().isInline()) {
                List content =
                    ((InlineLayoutBox)c.getLayer().getMaster()).getElementWithContent();
                if (content.contains(this)) {
                    setContainingLayer(c.getLayer());
                }
            }
        }
    }

    public void connectChildrenToCurrentLayer(LayoutContext c) {

        for (int i = 0; i < getChildCount(); i++) {
            Box box = getChild(i);
            box.setContainingLayer(c.getLayer());
            box.connectChildrenToCurrentLayer(c);
        }
    }

    public List getElementBoxes(Element elem) {
        List result = new ArrayList();
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            if (child.getElement() == elem) {
                result.add(child);
            }
            result.addAll(child.getElementBoxes(elem));
        }
        return result;
    }

    public void reset(LayoutContext c) {
        resetChildren(c);
        if (_layer != null) {
            _layer.detach();
            _layer = null;
        }

        setContainingLayer(null);
        setLayer(null);
        setPaintingInfo(null);
        setContentWidth(0);

        _workingMargin = null;

        String anchorName = c.getNamespaceHandler().getAnchorName(getElement());
        if (anchorName != null) {
            c.removeBoxId(anchorName);
        }

        Element e = getElement();
        if (e != null) {
            String id = c.getNamespaceHandler().getID(e);
            if (id != null) {
                c.removeBoxId(id);
            }
        }
    }

    public void detach(LayoutContext c) {
        reset(c);

        if (getParent() != null) {
            getParent().removeChild(this);
            setParent(null);
        }
    }

    public void resetChildren(LayoutContext c, int start, int end) {
        for (int i = start; i <= end; i++) {
            Box box = getChild(i);
            box.reset(c);
        }
    }

    protected void resetChildren(LayoutContext c) {
        int remaining = getChildCount();
        for (int i = 0; i < remaining; i++) {
            Box box = getChild(i);
            box.reset(c);
        }
    }

    public abstract void calcCanvasLocation();

    public void calcChildLocations() {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            child.calcCanvasLocation();
            child.calcChildLocations();
        }
    }

    public int forcePageBreakBefore(LayoutContext c, IdentValue pageBreakValue, boolean pendingPageName) {
        PageBox page = c.getRootLayer().getFirstPage(c, this);
        if (page == null) {
            XRLog.layout(Level.WARNING, "Box has no page");
            return 0;
        } else {
            int pageBreakCount = 1;
            if (page.getTop() == getAbsY()) {
                pageBreakCount--;
                if (pendingPageName && page == c.getRootLayer().getLastPage()) {
                    c.getRootLayer().removeLastPage();
                    c.setPageName(c.getPendingPageName());
                    c.getRootLayer().addPage(c);
                }
            }
            if ((page.isLeftPage() && pageBreakValue == IdentValue.LEFT) ||
                    (page.isRightPage() && pageBreakValue == IdentValue.RIGHT)) {
                pageBreakCount++;
            }

            if (pageBreakCount == 0) {
                return 0;
            }

            if (pageBreakCount == 1 && pendingPageName) {
                c.setPageName(c.getPendingPageName());
            }

            int delta = page.getBottom() + c.getExtraSpaceTop() - getAbsY();
            if (page == c.getRootLayer().getLastPage()) {
                c.getRootLayer().addPage(c);
            }

            if (pageBreakCount == 2) {
                page = (PageBox)c.getRootLayer().getPages().get(page.getPageNo()+1);
                delta += page.getContentHeight(c);

                if (pageBreakCount == 2 && pendingPageName) {
                    c.setPageName(c.getPendingPageName());
                }

                if (page == c.getRootLayer().getLastPage()) {
                    c.getRootLayer().addPage(c);
                }
            }

            setY(getY() + delta);

            return delta;
        }
    }

    public void forcePageBreakAfter(LayoutContext c, IdentValue pageBreakValue) {
        boolean needSecondPageBreak = false;
        PageBox page = c.getRootLayer().getLastPage(c, this);

        if (page != null) {
            if ((page.isLeftPage() && pageBreakValue == IdentValue.LEFT) ||
                    (page.isRightPage() && pageBreakValue == IdentValue.RIGHT)) {
                needSecondPageBreak = true;
            }

            int delta = page.getBottom() + c.getExtraSpaceTop() - (getAbsY() +
                    getMarginBorderPadding(c, CalculatedStyle.TOP) + getHeight());

            if (page == c.getRootLayer().getLastPage()) {
                c.getRootLayer().addPage(c);
            }

            if (needSecondPageBreak) {
                page = (PageBox)c.getRootLayer().getPages().get(page.getPageNo()+1);
                delta += page.getContentHeight(c);

                if (page == c.getRootLayer().getLastPage()) {
                    c.getRootLayer().addPage(c);
                }
            }

            setHeight(getHeight() + delta);
        }
    }

    public boolean crossesPageBreak(LayoutContext c) {
        if (! c.isPageBreaksAllowed()) {
            return false;
        }

        PageBox pageBox = c.getRootLayer().getFirstPage(c, this);
        if (pageBox == null) {
            return false;
        } else {
            return getAbsY() + getHeight() >= pageBox.getBottom() - c.getExtraSpaceBottom();
        }
    }

    public Dimension getRelativeOffset() {
        return _relativeOffset;
    }

    public void setRelativeOffset(Dimension relativeOffset) {
        _relativeOffset = relativeOffset;
    }

    public Box find(CssContext cssCtx, int absX, int absY, boolean findAnonymous) {
        PaintingInfo pI = getPaintingInfo();
        if (pI != null && ! pI.getAggregateBounds().contains(absX, absY)) {
            return null;
        }

        Box result = null;
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            result = child.find(cssCtx, absX, absY, findAnonymous);
            if (result != null) {
                return result;
            }
        }

        Rectangle edge = getContentAreaEdge(getAbsX(), getAbsY(), cssCtx);
        return edge.contains(absX, absY) && getStyle().isVisible() ? this : null;
    }

    public boolean isRoot() {
        return getElement() != null && ! isAnonymous() && getElement().getParentNode().getNodeType() == Node.DOCUMENT_NODE;
    }

    public boolean isBody() {
        return getParent() != null && getParent().isRoot();
    }

    public Element getElement() {
        return _element;
    }

    public void setElement(Element element) {
        _element = element;
    }

    public void setMarginTop(CssContext cssContext, int marginTop) {
        ensureWorkingMargin(cssContext);
        _workingMargin.setTop(marginTop);
    }

    public void setMarginBottom(CssContext cssContext, int marginBottom) {
        ensureWorkingMargin(cssContext);
        _workingMargin.setBottom(marginBottom);
    }

    public void setMarginLeft(CssContext cssContext, int marginLeft) {
        ensureWorkingMargin(cssContext);
        _workingMargin.setLeft(marginLeft);
    }

    public void setMarginRight(CssContext cssContext, int marginRight) {
        ensureWorkingMargin(cssContext);
        _workingMargin.setRight(marginRight);
    }

    private void ensureWorkingMargin(CssContext cssContext) {
        if (_workingMargin == null) {
            _workingMargin = getStyleMargin(cssContext).copyOf();
        }
    }

    public RectPropertySet getMargin(CssContext cssContext) {
        return _workingMargin != null ? _workingMargin : getStyleMargin(cssContext);
    }

    protected RectPropertySet getStyleMargin(CssContext cssContext) {
        return getStyle().getMarginRect(getContainingBlockWidth(), cssContext);
    }

    protected RectPropertySet getStyleMargin(CssContext cssContext, boolean useCache) {
        return getStyle().getMarginRect(getContainingBlockWidth(), cssContext, useCache);
    }

    public RectPropertySet getPadding(CssContext cssCtx) {
        return getStyle().getPaddingRect(getContainingBlockWidth(), cssCtx);
    }

    public BorderPropertySet getBorder(CssContext cssCtx) {
        return getStyle().getBorder(cssCtx);
    }

    protected int getContainingBlockWidth() {
        return getContainingBlock().getContentWidth();
    }

    protected void resetTopMargin(CssContext cssContext) {
        if (_workingMargin != null) {
            RectPropertySet styleMargin = getStyleMargin(cssContext);

            _workingMargin.setTop(styleMargin.top());
        }
    }

    public void clearSelection(List modified) {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            child.clearSelection(modified);
        }
    }

    public void selectAll() {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            child.selectAll();
        }
    }

    public PaintingInfo calcPaintingInfo(CssContext c, boolean useCache) {
        PaintingInfo cached = getPaintingInfo();
        if (cached != null && useCache) {
            return cached;
        }

        final PaintingInfo result = new PaintingInfo();

        Rectangle bounds = getMarginEdge(getAbsX(), getAbsY(), c, 0, 0);
        result.setOuterMarginCorner(
            new Dimension(bounds.x + bounds.width, bounds.y + bounds.height));

        result.setAggregateBounds(getPaintingClipEdge(c));

        if (!getStyle().isOverflowApplies() || getStyle().isOverflowVisible()) {
            calcChildPaintingInfo(c, result, useCache);
        }

        setPaintingInfo(result);

        return result;
    }

    protected void calcChildPaintingInfo(
            CssContext c, PaintingInfo result, boolean useCache) {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            PaintingInfo info = child.calcPaintingInfo(c, useCache);
            moveIfGreater(result.getOuterMarginCorner(), info.getOuterMarginCorner());
            result.getAggregateBounds().add(info.getAggregateBounds());
        }
    }

    public int getMarginBorderPadding(CssContext cssCtx, int which) {
        BorderPropertySet border = getBorder(cssCtx);
        RectPropertySet margin = getMargin(cssCtx);
        RectPropertySet padding = getPadding(cssCtx);

        switch (which) {
            case CalculatedStyle.LEFT:
                return (int)(margin.left() + border.left() + padding.left());
            case CalculatedStyle.RIGHT:
                return (int)(margin.right() + border.right() + padding.right());
            case CalculatedStyle.TOP:
                return (int)(margin.top() + border.top() + padding.top());
            case CalculatedStyle.BOTTOM:
                return (int)(margin.bottom() + border.bottom() + padding.bottom());
            default:
                throw new IllegalArgumentException();
        }
    }

    protected void moveIfGreater(Dimension result, Dimension test) {
        if (test.width > result.width) {
            result.width = test.width;
        }
        if (test.height > result.height) {
            result.height = test.height;
        }
    }

    public void restyle(LayoutContext c) {
        Element e = getElement();
        CalculatedStyle style = null;

        String pe = getPseudoElementOrClass();
        if (pe != null) {
            if (e != null) {
                style = c.getSharedContext().getStyle(e, true);
                style = style.deriveStyle(c.getCss().getPseudoElementStyle(e, pe));
            } else {
                BlockBox container = (BlockBox)getParent().getParent();
                e = container.getElement();
                style = c.getSharedContext().getStyle(e, true);
                style = style.deriveStyle(c.getCss().getPseudoElementStyle(e, pe));
                style = style.createAnonymousStyle(IdentValue.INLINE);
            }
        } else {
            if (e != null) {
                style = c.getSharedContext().getStyle(e, true);
                if (isAnonymous()) {
                    style = style.createAnonymousStyle(getStyle().getIdent(CSSName.DISPLAY));
                }
            } else {
                Box parent = getParent();
                if (parent != null) {
                    e = parent.getElement();
                    if (e != null) {
                        style = c.getSharedContext().getStyle(e, true);
                        style = style.createAnonymousStyle(IdentValue.INLINE);
                    }
                }
            }
        }

        if (style != null) {
            setStyle(style);
        }

        restyleChildren(c);
    }

    protected void restyleChildren(LayoutContext c) {
        for (int i = 0; i < getChildCount(); i++) {
            Box b = getChild(i);
            b.restyle(c);
        }
    }

    public Box getRestyleTarget() {
        return this;
    }

    protected int getIndex() {
        return _index;
    }

    protected void setIndex(int index) {
        _index = index;
    }

    public String getPseudoElementOrClass() {
        return _pseudoElementOrClass;
    }

    public void setPseudoElementOrClass(String pseudoElementOrClass) {
        _pseudoElementOrClass = pseudoElementOrClass;
    }

    public void setX(int x) {
        _x = x;
    }

    public int getX() {
        return _x;
    }

    public void setY(int y) {
        _y = y;
    }

    public int getY() {
        return _y;
    }

    public void setTy(int ty) {
        _ty = ty;
    }

    public int getTy() {
        return _ty;
    }

    public void setTx(int tx) {
        _tx = tx;
    }

    public int getTx() {
        return _tx;
    }

    public void setRightMBP(int rightMBP) {
        _rightMBP = rightMBP;
    }

    public int getRightMBP() {
        return _rightMBP;
    }

    public void setLeftMBP(int leftMBP) {
        _leftMBP = leftMBP;
    }

    public int getLeftMBP() {
        return _leftMBP;
    }

    public void setHeight(int height) {
        _height = height;
    }

    public int getHeight() {
        return _height;
    }

    public void setContentWidth(int contentWidth) {
        _contentWidth = contentWidth < 0 ? 0 : contentWidth;
    }

    public int getContentWidth() {
        return _contentWidth;
    }

    public PaintingInfo getPaintingInfo() {
        return _paintingInfo;
    }

    private void setPaintingInfo(PaintingInfo paintingInfo) {
        _paintingInfo = paintingInfo;
    }

    public boolean isAnonymous() {
        return _anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        _anonymous = anonymous;
    }

    public BoxDimensions getBoxDimensions() {
        BoxDimensions result = new BoxDimensions();

        result.setLeftMBP(getLeftMBP());
        result.setRightMBP(getRightMBP());
        result.setContentWidth(getContentWidth());
        result.setHeight(getHeight());

        return result;
    }

    public void setBoxDimensions(BoxDimensions dimensions) {
        setLeftMBP(dimensions.getLeftMBP());
        setRightMBP(dimensions.getRightMBP());
        setContentWidth(dimensions.getContentWidth());
        setHeight(dimensions.getHeight());
    }

    public void collectText(RenderingContext c, StringBuffer buffer) throws IOException {
        for (Iterator i = getChildIterator(); i.hasNext(); ) {
            Box b = (Box)i.next();
            b.collectText(c, buffer);
        }
    }

    public void exportText(RenderingContext c, Writer writer) throws IOException {
        if (c.isPrint() && isRoot()) {
            c.setPage(0, (PageBox)c.getRootLayer().getPages().get(0));
            c.getPage().exportLeadingText(c, writer);
        }
        for (Iterator i = getChildIterator(); i.hasNext(); ) {
            Box b = (Box)i.next();
            b.exportText(c, writer);
        }
        if (c.isPrint() && isRoot()) {
            exportPageBoxText(c, writer);
        }
    }

    private void exportPageBoxText(RenderingContext c, Writer writer) throws IOException {
        c.getPage().exportTrailingText(c, writer);
        if (c.getPage() != c.getRootLayer().getLastPage()) {
            List pages = c.getRootLayer().getPages();
            do {
                PageBox next = (PageBox)pages.get(c.getPageNo()+1);
                c.setPage(next.getPageNo(), next);
                next.exportLeadingText(c, writer);
                next.exportTrailingText(c, writer);
            } while (c.getPage() != c.getRootLayer().getLastPage());
        }
    }

    protected void exportPageBoxText(RenderingContext c, Writer writer, int yPos) throws IOException {
        c.getPage().exportTrailingText(c, writer);
        List pages = c.getRootLayer().getPages();
        PageBox next = (PageBox)pages.get(c.getPageNo()+1);
        c.setPage(next.getPageNo(), next);
        while (next.getBottom() < yPos) {
            next.exportLeadingText(c, writer);
            next.exportTrailingText(c, writer);
            next = (PageBox)pages.get(c.getPageNo()+1);
            c.setPage(next.getPageNo(), next);
        }
        next.exportLeadingText(c, writer);
    }

    public boolean isInDocumentFlow() {
        Box flowRoot = this;
        while (true) {
            Box parent = flowRoot.getParent();
            if (parent == null) {
                break;
            } else {
                flowRoot = parent;
            }
        }

        return flowRoot.isRoot();
    }

    public void analyzePageBreaks(LayoutContext c, ContentLimitContainer container) {
        container.updateTop(c, getAbsY());
        for (Iterator i = getChildIterator(); i.hasNext(); ) {
            Box b = (Box)i.next();
            b.analyzePageBreaks(c, container);
        }
        container.updateBottom(c, getAbsY() + getHeight());
    }

    public FSColor getEffBackgroundColor(RenderingContext c) {
        FSColor result = null;
        Box current = this;
        while (current != null) {
            result = current.getStyle().getBackgroundColor();
            if (result != null) {
                return result;
            }

            current = current.getContainingBlock();
        }

        PageBox page = c.getPage();
        result = page.getStyle().getBackgroundColor();
        if (result == null) {
            return new FSRGBColor(255, 255, 255);
        } else {
            return result;
        }
    }

    protected boolean isMarginAreaRoot() {
        return false;
    }

    public boolean isContainedInMarginBox() {
        Box current = this;
        while (true) {
            Box parent = current.getParent();
            if (parent == null) {
                break;
            } else {
                current = parent;
            }
        }

        return current.isMarginAreaRoot();
    }

    public int getEffectiveWidth() {
        return getWidth();
    }

    protected boolean isInitialContainingBlock() {
        return false;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.147  2010/01/11 01:27:41  peterbrant
 * Correct background-position calculation.  Detective work and initial patch from Stefano Bagnara.
 *
 * 14.2 says backgrounds are painted over the border box.  However, background-position is calculated relative to the padding box (spec 14.2.1).  We were calculating it relative to the border box.
 *
 * Revision 1.146  2008/11/07 18:34:33  peterbrant
 * Add API to retrieve PDF page and coordinates for boxes with an ID attribute
 *
 * Revision 1.145  2008/07/27 00:21:47  peterbrant
 * Implement CMYK color support for PDF output, starting with patch from Mykola Gurov / Banish java.awt.Color from FS core layout classes
 *
 * Revision 1.144  2008/04/30 23:02:42  peterbrant
 * Fix for bug #239 (detective work by Ludovic Durand-Texte)
 *
 * Revision 1.143  2008/01/26 15:34:52  peterbrant
 * Make getNextSibling() and getPreviousSibling() work with InlineLayoutBox (or as well as it can in that context)
 *
 * Revision 1.142  2007/08/23 20:52:31  peterbrant
 * Begin work on AcroForm support
 * Revision 1.141 2007/08/19 22:22:50 peterbrant Merge
 * R8pbrant changes to HEAD
 *
 * Revision 1.140.2.10 2007/08/18 00:52:44 peterbrant When paginating a table,
 * clip cells that span multiple pages to the effective content area visible on
 * the current page
 *
 * Revision 1.140.2.9 2007/08/15 21:29:31 peterbrant Initial draft of support
 * for running headers and footers on tables
 *
 * Revision 1.140.2.8 2007/08/14 16:10:30 peterbrant Remove obsolete code
 *
 * Revision 1.140.2.7 2007/08/13 22:57:03 peterbrant Make sure trailing pages
 * with only a header and footer are exported when exporting to text
 *
 * Revision 1.140.2.6 2007/08/13 22:41:13 peterbrant First pass at exporting the
 * render tree as text
 *
 * Revision 1.140.2.5 2007/08/09 20:18:16 peterbrant Bug fixes to improved
 * pagination support
 *
 * Revision 1.140.2.4 2007/08/08 18:28:25 peterbrant Further progress on CSS3
 * paged media
 *
 * Revision 1.140.2.3 2007/08/07 17:06:32 peterbrant Implement named pages /
 * Implement page-break-before/after: left/right / Experiment with efficient
 * selection
 *
 * Revision 1.140.2.2 2007/07/30 00:43:15 peterbrant Start implementing text
 * selection and copying
 *
 * Revision 1.140.2.1 2007/07/09 22:18:03 peterbrant Begin work on running
 * headers and footers and named pages
 *
 * Revision 1.140 2007/06/11 22:30:15 peterbrant Minor cleanup to LayoutContext /
 * Start adding infrastructure to support better table pagination
 *
 * Revision 1.139 2007/04/25 18:09:41 peterbrant Always reset block box margin
 * if it is the first thing on a page
 *
 * Revision 1.138 2007/04/16 01:10:05 peterbrant Vertical margin and padding
 * with percentage values may be incorrect if box participated in a
 * shrink-to-fit calculation. Fix margin calculation.
 *
 * Revision 1.137 2007/04/15 00:34:40 peterbrant Allow inline-block /
 * inline-table content to be relatively positioned
 *
 * Revision 1.136 2007/03/17 22:55:51 peterbrant Remove distinction between box
 * IDs and named anchors
 *
 * Revision 1.135 2007/03/08 01:46:34 peterbrant Fix calculation of
 * viewport/page rectangle when calculating fixed background positions
 *
 * Revision 1.134 2007/02/24 01:57:30 peterbrant toString() changes
 *
 * Revision 1.133 2007/02/24 01:36:57 peterbrant Fix potential NPE if layout
 * fails
 *
 * Revision 1.132 2007/02/24 00:46:38 peterbrant Paint root element background
 * over entire canvas (or it's first child if the root element doesn't define a
 * background)
 *
 * Revision 1.131 2007/02/23 15:50:37 peterbrant Fix incorrect absolute box
 * positioning with print medium
 *
 * Revision 1.130 2007/02/22 18:21:19 peterbrant Add support for overflow:
 * visible/hidden
 *
 * Revision 1.129 2007/02/22 16:10:54 peterbrant Remove unused API
 *
 * Revision 1.128 2007/02/22 15:52:46 peterbrant Restyle generated content
 * correctly (although the CSS matcher needs more work before restyle with
 * generated content and dynamic pseudo classes will work)
 *
 * Revision 1.127 2007/02/22 15:30:42 peterbrant Internal links should be able
 * to target block boxes too (plus other minor cleanup)
 *
 * Revision 1.126 2007/02/21 23:49:41 peterbrant Can't calculate clearance until
 * margins have been collapsed / Clearance must be calculated relative to the
 * box's border edge, not margin edge
 *
 * Revision 1.125 2007/02/21 23:11:03 peterbrant Correct margin edge calculation
 * (as it turns out the straightforward approach is also the correct one)
 *
 * Revision 1.124 2007/02/21 19:15:05 peterbrant right and bottom need to push
 * the opposite direction as left and top with position: relative
 *
 * Revision 1.123 2007/02/19 23:42:54 peterbrant Fix resetChildren() typo
 *
 * Revision 1.122 2007/02/19 14:53:36 peterbrant Integrate new CSS parser
 *
 * Revision 1.121 2007/02/11 23:10:59 peterbrant Make sure bounds information is
 * calculated for fixed layers
 *
 * Revision 1.120 2007/02/07 16:33:22 peterbrant Initial commit of rewritten
 * table support and associated refactorings
 *
 * Revision 1.119 2006/10/04 23:52:57 peterbrant Implement support for margin:
 * auto (centering blocks in their containing block)
 *
 * Revision 1.118 2006/10/04 19:49:08 peterbrant Improve calculation of
 * available width when calculating shrink-to-fit width
 *
 * Revision 1.117 2006/09/08 15:41:58 peterbrant Calculate containing block
 * width accurately when collapsing margins / Provide collapsed bottom margin to
 * floats / Revive :first-line and :first-letter / Minor simplication in
 * InlineBoxing (get rid of now-superfluous InlineBoxInfo)
 *
 * Revision 1.116 2006/09/05 23:03:44 peterbrant Initial draft of shrink-to-fit
 * support
 *
 * Revision 1.115 2006/09/01 23:49:38 peterbrant Implement basic margin
 * collapsing / Various refactorings in preparation for shrink-to-fit / Add hack
 * to treat auto margins as zero
 *
 * Revision 1.114 2006/08/30 18:25:41 peterbrant Further refactoring / Bug fix
 * for problem reported by Mike Curtis
 *
 * Revision 1.113 2006/08/29 17:29:13 peterbrant Make Style object a thing of
 * the past
 *
 * Revision 1.112 2006/08/27 00:36:44 peterbrant Initial commit of (initial) R7
 * work
 *
 * Revision 1.111 2006/03/01 00:45:02 peterbrant Provide LayoutContext when
 * calling detach() and friends
 *
 * Revision 1.110 2006/02/22 02:20:19 peterbrant Links and hover work again
 *
 * Revision 1.109 2006/02/21 20:43:45 peterbrant right was actually using left,
 * bottom was actually using top (relative positioning)
 *
 * Revision 1.108 2006/02/20 23:29:20 peterbrant Fix positioning of replaced
 * elements with margins, borders, padding
 *
 * Revision 1.107 2006/02/07 00:02:52 peterbrant If "keep together" cannot be
 * satisified, drop rule vs. pushing to next page / Fix bug with incorrect
 * positioning of content following relative block layers
 *
 * Revision 1.106 2006/02/01 01:30:14 peterbrant Initial commit of PDF work
 *
 * Revision 1.105 2006/01/27 01:15:38 peterbrant Start on better support for
 * different output devices
 *
 * Revision 1.104 2006/01/11 22:09:27 peterbrant toString() uses absolute
 * coordinates now
 *
 * Revision 1.103 2006/01/10 19:56:00 peterbrant Fix inappropriate box resizing
 * when width: auto
 *
 * Revision 1.102 2006/01/09 23:25:22 peterbrant Correct (?) position of debug
 * outline
 *
 * Revision 1.101 2006/01/04 19:50:14 peterbrant More pagination bug fixes /
 * Implement simple pagination for tables
 *
 * Revision 1.100 2006/01/03 23:55:57 peterbrant Add support for proper page
 * breaking of floats / More bug fixes to pagination support
 *
 * Revision 1.99 2006/01/03 02:12:20 peterbrant Various pagination fixes / Fix
 * fixed positioning
 *
 * Revision 1.98 2006/01/02 20:59:09 peterbrant Implement
 * page-break-before/after: avoid
 *
 * Revision 1.97 2006/01/01 03:14:25 peterbrant Implement page-break-inside:
 * avoid
 *
 * Revision 1.96 2006/01/01 02:38:19 peterbrant Merge more pagination work /
 * Various minor cleanups
 *
 * Revision 1.95 2005/12/30 01:32:39 peterbrant First merge of parts of
 * pagination work
 *
 * Revision 1.94 2005/12/28 00:50:52 peterbrant Continue ripping out first try
 * at pagination / Minor method name refactoring
 *
 * Revision 1.93 2005/12/21 02:36:29 peterbrant - Calculate absolute positions
 * incrementally (prep work for pagination) - Light cleanup - Fix bug where
 * floats nested in floats could cause the outer float to be positioned in the
 * wrong place
 *
 * Revision 1.92 2005/12/17 02:24:14 peterbrant Remove last pieces of old (now
 * non-working) clip region checking / Push down handful of fields from Box to
 * BlockBox
 *
 * Revision 1.91 2005/12/15 20:04:47 peterbrant Implement visibility: hidden
 *
 * Revision 1.90 2005/12/14 22:06:47 peterbrant Fix NPE
 *
 * Revision 1.89 2005/12/13 02:41:33 peterbrant Initial implementation of
 * vertical-align: top/bottom (not done yet) / Minor cleanup and optimization
 *
 * Revision 1.88 2005/12/11 02:51:18 peterbrant Minor tweak (misread spec)
 *
 * Revision 1.87 2005/12/10 03:11:43 peterbrant Use margin edge not content edge
 *
 * Revision 1.86 2005/12/09 21:41:19 peterbrant Finish support for relative
 * inline layers
 *
 * Revision 1.85 2005/12/09 01:24:56 peterbrant Initial commit of relative
 * inline layers
 *
 * Revision 1.84 2005/12/08 02:21:26 peterbrant Fix positioning bug when CB of
 * absolute block is a relative block
 *
 * Revision 1.83 2005/12/07 03:14:20 peterbrant Fixes to final float position
 * when float BFC is not contained in the layer being positioned / Implement
 * 10.6.7 of the spec
 *
 * Revision 1.82 2005/12/07 00:33:12 peterbrant :first-letter and :first-line
 * works again
 *
 * Revision 1.81 2005/12/05 00:13:53 peterbrant Improve list-item support
 * (marker positioning is now correct) / Start support for relative inline
 * layers
 *
 * Revision 1.80 2005/11/29 02:37:24 peterbrant Make clear work again / Rip out
 * old pagination code
 *
 * Revision 1.79 2005/11/25 16:57:20 peterbrant Initial commit of inline content
 * refactoring
 *
 * Revision 1.78 2005/11/13 01:14:16 tobega Take into account the height of a
 * first-letter. Also attempt to line-break better with inline padding.
 *
 * Revision 1.77 2005/11/10 18:27:28 peterbrant Position absolute box correctly
 * when top: auto and bottom: auto.
 *
 * Revision 1.76 2005/11/10 01:55:16 peterbrant Further progress on layer work
 *
 * Revision 1.75 2005/11/09 18:41:28 peterbrant Fixes to vertical margin
 * collapsing in the presence of floats / Paint floats as layers
 *
 * Revision 1.74 2005/11/08 20:03:57 peterbrant Further progress on painting
 * order / improved positioning implementation
 *
 * Revision 1.73 2005/11/05 23:19:07 peterbrant Always add fixed layers to root
 * layer / If element has fixed background just note this on the root layer
 * instead of property in Box
 *
 * Revision 1.72 2005/11/05 18:45:06 peterbrant General cleanup / Remove
 * obsolete code
 *
 * Revision 1.71 2005/11/05 03:30:01 peterbrant Start work on painting order and
 * improved positioning implementation
 *
 * Revision 1.70 2005/11/03 17:58:41 peterbrant Float rewrite (still stomping
 * bugs, but demos work)
 *
 * Revision 1.69 2005/11/02 18:15:29 peterbrant First merge of Tobe's and my
 * stacking context work / Rework float code (not done yet)
 *
 * Revision 1.68 2005/10/30 22:06:15 peterbrant Only create child List if
 * necessary
 *
 * Revision 1.67 2005/10/29 22:31:01 tobega House-cleaning
 *
 * Revision 1.66 2005/10/27 00:09:02 tobega Sorted out Context into
 * RenderingContext and LayoutContext
 *
 * Revision 1.65 2005/10/18 20:57:05 tobega Patch from Peter Brant
 *
 * Revision 1.64 2005/10/15 23:39:18 tobega patch from Peter Brant
 *
 * Revision 1.63 2005/10/12 21:17:13 tobega patch from Peter Brant
 *
 * Revision 1.62 2005/10/08 17:40:21 tobega Patch from Peter Brant
 *
 * Revision 1.61 2005/10/06 03:20:22 tobega Prettier incremental rendering. Ran
 * into more trouble than expected and some creepy crawlies and a few pages
 * don't look right (forms.xhtml, splash.xhtml)
 *
 * Revision 1.60 2005/10/02 21:30:00 tobega Fixed a lot of concurrency (and
 * other) issues from incremental rendering. Also some house-cleaning.
 *
 * Revision 1.59 2005/09/30 04:58:05 joshy fixed garbage when showing a document
 * with a fixed positioned block
 *
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.58 2005/09/29 21:34:04 joshy minor updates to a lot of files.
 * pulling in more incremental rendering code. fixed another resize bug Issue
 * number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.57 2005/09/26 22:40:21 tobega Applied patch from Peter Brant
 * concerning margin collapsing
 *
 * Revision 1.56 2005/07/04 00:12:12 tobega text-align now works for table-cells
 * too (is done in render, not in layout)
 *
 * Revision 1.55 2005/06/16 07:24:51 tobega Fixed background image bug. Caching
 * images in browser. Enhanced LinkListener. Some house-cleaning, playing with
 * Idea's code inspection utility.
 *
 * Revision 1.54 2005/06/16 04:31:30 joshy added clear support to the box Issue
 * number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.53 2005/05/13 15:23:55 tobega Done refactoring box borders, margin
 * and padding. Hover is working again.
 *
 * Revision 1.52 2005/05/13 11:49:59 tobega Started to fix up borders on
 * inlines. Got caught up in refactoring. Boxes shouldn't cache borders and
 * stuff unless necessary. Started to remove unnecessary references. Hover is
 * not working completely well now, might get better when I'm done.
 *
 * Revision 1.51 2005/05/08 14:36:58 tobega Refactored away the need for having
 * a context in a CalculatedStyle
 *
 * Revision 1.50 2005/04/22 17:19:19 joshy resovled conflicts in Box Issue
 * number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.49 2005/04/21 18:16:08 tobega Improved handling of inline padding.
 * Also fixed first-line handling according to spec.
 *
 * Revision 1.48 2005/04/19 17:51:18 joshy fixed absolute positioning bug
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.47 2005/04/19 13:59:48 pdoubleya Added defaults for margin,
 * padding, border.
 *
 * Revision 1.46 2005/02/03 23:16:16 pdoubleya .
 *
 * Revision 1.45 2005/01/31 22:51:35 pdoubleya Added caching for
 * padding/margin/border calcs, plus alternate calls to get their totals (with
 * and without style available). Reformatted.
 *
 * Revision 1.44 2005/01/29 20:24:23 pdoubleya Clean/reformat code. Removed
 * commented blocks, checked copyright.
 *
 * Revision 1.43 2005/01/24 22:46:42 pdoubleya Added support for ident-checks
 * using IdentValue instead of string comparisons.
 *
 * Revision 1.42 2005/01/24 19:01:03 pdoubleya Mass checkin. Changed to use
 * references to CSSName, which now has a Singleton instance for each property,
 * everywhere property names were being used before. Removed commented code.
 * Cascaded and Calculated style now store properties in arrays rather than
 * maps, for optimization.
 *
 * Revision 1.41 2005/01/24 14:36:35 pdoubleya Mass commit, includes: updated
 * for changes to property declaration instantiation, and new use of
 * DerivedValue. Removed any references to older XR... classes (e.g.
 * XRProperty). Cleaned imports.
 *
 * Revision 1.40 2005/01/16 18:50:05 tobega Re-introduced caching of styles,
 * which make hamlet and alice scroll nicely again. Background painting still
 * slow though.
 *
 * Revision 1.39 2005/01/09 15:22:50 tobega Prepared improved handling of
 * margins, borders and padding.
 *
 * Revision 1.38 2005/01/07 00:29:29 tobega Removed Content reference from Box
 * (mainly to reduce memory footprint). In the process stumbled over and cleaned
 * up some messy stuff.
 *
 * Revision 1.37 2005/01/05 23:15:09 tobega Got rid of some redundant code for
 * hover-styling
 *
 * Revision 1.36 2005/01/05 01:10:15 tobega Went wild with code analysis tool.
 * removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.35 2005/01/02 01:00:09 tobega Started sketching in code for
 * handling replaced elements in the NamespaceHandler
 *
 * Revision 1.34 2004/12/29 12:57:27 tobega Trying to handle BFC:s right
 *
 * Revision 1.33 2004/12/28 02:15:19 tobega More cleaning.
 *
 * Revision 1.32 2004/12/28 01:48:24 tobega More cleaning. Magically, the
 * financial report demo is starting to look reasonable, without any effort
 * being put on it.
 *
 * Revision 1.31 2004/12/27 09:40:48 tobega Moved more styling to render stage.
 * Now inlines have backgrounds and borders again.
 *
 * Revision 1.30 2004/12/27 07:43:32 tobega Cleaned out border from box, it can
 * be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.29 2004/12/16 15:53:10 joshy fixes for absolute layout
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.28 2004/12/13 15:15:57 joshy fixed bug where inlines would pick up
 * parent styles when they aren't supposed to fixed extra Xx's in printed text
 * added conf boolean to turn on box outlines
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.27 2004/12/12 23:19:26 tobega Tried to get hover working.
 * Something happens, but not all that's supposed to happen.
 *
 * Revision 1.26 2004/12/12 03:33:00 tobega Renamed x and u to avoid confusing
 * IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.25 2004/12/11 23:36:49 tobega Progressing on cleaning up layout
 * and boxes. Still broken, won't even compile at the moment. Working hard to
 * fix it, though.
 *
 * Revision 1.24 2004/12/11 18:18:11 tobega Still broken, won't even compile at
 * the moment. Working hard to fix it, though. Replace the StyleReference
 * interface with our only concrete implementation, it was a bother changing in
 * two places all the time.
 *
 * Revision 1.23 2004/12/10 06:51:04 tobega Shamefully, I must now check in
 * painfully broken code. Good news is that Layout is much nicer, and we also
 * handle :before and :after, and do :first-line better than before. Table stuff
 * must be brought into line, but most needed is to fix Render. IMO Render
 * should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.22 2004/12/09 21:18:53 tobega precaution: code still works
 *
 * Revision 1.21 2004/12/09 18:00:05 joshy fixed hover bugs fixed li's not being
 * blocks bug
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.20 2004/12/05 05:22:36 joshy fixed NPEs in selection listener
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.19 2004/12/05 05:18:02 joshy made bullets be anti-aliased fixed
 * bug in link listener that caused NPEs
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.18 2004/12/05 00:48:59 tobega Cleaned up so that now all
 * property-lookups use the CalculatedStyle. Also added support for relative
 * values of top, left, width, etc.
 *
 * Revision 1.17 2004/12/01 01:57:02 joshy more updates for float support.
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.16 2004/11/18 16:45:13 joshy improved the float code a bit. now
 * floats are automatically forced to be blocks
 *
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.15 2004/11/17 00:44:54 joshy fixed bug in the history manager
 * added cursor support to the link listener
 *
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.14 2004/11/15 15:20:39 joshy fixes for absolute layout
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.13 2004/11/12 20:25:18 joshy added hover support to the browser
 * created hover demo fixed bug with inline borders
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.12 2004/11/12 17:05:25 joshy support for fixed positioning Issue
 * number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.11 2004/11/09 02:04:23 joshy support for text-align: justify
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.10 2004/11/08 15:10:10 joshy added support for styling
 * :first-letter inline boxes updated the absolute positioning tests
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.9 2004/11/07 16:23:18 joshy added support for lighten and darken
 * to bordercolor added support for different colored sides
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.8 2004/11/06 22:49:52 joshy cleaned up alice initial support for
 * inline borders and backgrounds moved all of inlinepainter back into
 * inlinerenderer, where it belongs.
 *
 *
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.7 2004/11/03 23:54:34 joshy added hamlet and tables to the browser
 * more support for absolute layout added absolute layout unit tests removed
 * more dead code and moved code into layout factory
 *
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.6 2004/11/03 15:17:05 joshy added intial support for absolute
 * positioning
 *
 * Issue number: Obtained from: Submitted by: Reviewed by:
 *
 * Revision 1.5 2004/10/23 13:50:26 pdoubleya Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io,
 * java.util, etc). Added CVS log comments at bottom.
 *
 *
 */

