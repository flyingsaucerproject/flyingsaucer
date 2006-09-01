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
package org.xhtmlrenderer.layout;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FloatedBlockBox;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.MarginBox;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.render.ViewportBox;
import org.xhtmlrenderer.table.TableBox;

public class Layer {
    public static final short PAGED_MODE_SCREEN = 1;
    public static final short PAGED_MODE_PRINT = 2;
    
    private Layer parent;
    private boolean stackingContext;
    private List children;
    private Box master;
    
    private Box end;

    private List floats;

    private boolean fixedBackground;
    
    private boolean inline;
    private boolean requiresLayout;
    
    private AbsoluteContentLayoutData layoutData;
    
    private List pages;
    
    public Layer(Box master) {
        this(null, master);
        setStackingContext(true);
    }

    public Layer(Layer parent, Box master) {
        this.parent = parent;
        this.master = master;
        setStackingContext(!master.getStyle().isAutoZIndex());
        master.setLayer(this);
        master.setContainingLayer(this);
    }

    public Layer getParent() {
        return parent;
    }

    public boolean isStackingContext() {
        return stackingContext;
    }

    public void setStackingContext(boolean stackingContext) {
        this.stackingContext = stackingContext;
    }

    public int getZIndex() {
        return (int) master.getStyle().asFloat(CSSName.Z_INDEX);
    }
    
    public boolean isAlternateFlow() {
        return master.getStyle().isAlternateFlow();
    }

    public Box getMaster() {
        return master;
    }

    public synchronized void addChild(Layer layer) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(layer);
    }

    public void addFloat(FloatedBlockBox floater, BlockFormattingContext bfc) {
        if (floats == null) {
            floats = new ArrayList();
        }

        floats.add(floater);
        
        floater.setDrawingLayer(this);
    }

    public void removeFloat(FloatedBlockBox floater) {
        if (floats != null) {
            floats.remove(floater);
        }
    }

    private void paintFloats(RenderingContext c) {
        if (floats != null) {
            for (int i = floats.size() - 1; i >= 0; i--) {
                FloatedBlockBox floater = (FloatedBlockBox) floats.get(i);
                paintAsLayer(c, floater);
            }
        }
    }

    private void paintLayers(RenderingContext c, List layers) {
        for (int i = 0; i < layers.size(); i++) {
            Layer layer = (Layer) layers.get(i);
            layer.paint(c, getMaster().getAbsX(), getMaster().getAbsY());
        }
    }
    
    private static final int POSITIVE = 1;
    private static final int ZERO = 2;
    private static final int NEGATIVE = 3;
    private static final int AUTO = 4;
    
    private List collectLayers(int which) {
        List result = new ArrayList();
        
        if (which != AUTO) {
            result.addAll(getStackingContextLayers(which));
        }
        
        List children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            Layer child = (Layer)children.get(i);
            if (isRootLayer() && child.isAlternateFlow()) {
                continue;
            }
            if (! child.isStackingContext()) {
                if (which == AUTO) {
                    result.add(child);
                } 
                result.addAll(child.collectLayers(which));
            }
        }
        
        return result;
    }
    
    private List getStackingContextLayers(int which) {
        List result = new ArrayList();
        
        List children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            Layer target = (Layer)children.get(i);
            
            if (isRootLayer() && target.isAlternateFlow()) {
                continue;
            }

            if (target.isStackingContext()) {
                if (which == NEGATIVE && target.getZIndex() < 0) {
                    result.add(target);
                } else if (which == POSITIVE && target.getZIndex() > 0) {
                    result.add(target);
                } else if (which == ZERO) {
                    result.add(target);
                }
            }
        }
        
        return result;
    }
    
    private List getSortedLayers(int which) {
        List result = collectLayers(which);
        
        Collections.sort(result, new ZIndexComparator());
        
        return result;
    }
    
    private static class ZIndexComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Layer l1 = (Layer)o1;
            Layer l2 = (Layer)o2;
            return l1.getZIndex() - l2.getZIndex();
        }
    }
    
    private void paintBackgroundsAndBorders(RenderingContext c, List blocks) {
        for (Iterator i = blocks.iterator(); i.hasNext();) {
            BlockBox box = (BlockBox) i.next();
            // HACK to make existing table layout work
            if (! (box.getStyle().isTable() && ! (box instanceof TableBox))) {
                box.paintBackground(c);
                box.paintBorder(c);
                if (c.debugDrawBoxes()) {
                    box.paintDebugOutline(c);
                }
            }
        }
    }

    private void paintInlineContent(RenderingContext c, List lines) {
        for (Iterator i = lines.iterator(); i.hasNext();) {
            InlinePaintable paintable = (InlinePaintable) i.next();
            paintable.paintInline(c);
        }
    }
    
    public Dimension getPaintingDimension(LayoutContext c) {
        return calcPaintingDimension(c).outerMarginCorner;
    }
    
    public void paint(RenderingContext c, int originX, int originY) {
        paint(c, originX, originY, false);
    }

    public void paint(RenderingContext c, int originX, int originY, 
            boolean paintAlternateFlows) {
        if (! paintAlternateFlows && isAlternateFlow()) {
            return;
        }
        
        if (getMaster().getStyle().isFixed()) {
            positionFixedLayer(c);
        }

        if (! isInline() && ((BlockBox)getMaster()).isReplaced()) {
            paintLayerBackgroundAndBorder(c);
            paintReplacedElement(c, (BlockBox)getMaster());
        } else {
            List blocks = new ArrayList();
            List lines = new ArrayList();
    
            BoxCollector collector = new BoxCollector();
            collector.collect(c, c.getOutputDevice().getClip(), this, blocks, lines);
    
            // TODO root layer needs to be handled correctly (paint over entire canvas)
            if (! isInline()) {
                paintLayerBackgroundAndBorder(c);
                if (c.debugDrawBoxes()) {
                    ((BlockBox)getMaster()).paintDebugOutline(c);
                }
            }
            
            if (isRootLayer() || isStackingContext() || isAlternateFlow()) {
                paintLayers(c, getSortedLayers(NEGATIVE));
            }
    
            paintBackgroundsAndBorders(c, blocks);
            paintFloats(c);
            paintListMarkers(c, blocks);
            paintInlineContent(c, lines);
            paintReplacedElements(c, blocks);
    
            if (isRootLayer() || isStackingContext() || isAlternateFlow()) {
                paintLayers(c, collectLayers(AUTO));
                // TODO z-index: 0 layers should be painted atomically
                paintLayers(c, getSortedLayers(ZERO));
                paintLayers(c, getSortedLayers(POSITIVE));
            }
        }
    }
    
    private List getFloats() {
        return this.floats == null ? Collections.EMPTY_LIST : this.floats;
    }
    
    public Box find(CssContext cssCtx, int absX, int absY) {
        Box result = null;
        if (isRootLayer() || isStackingContext()) {
            result = find(cssCtx, absX, absY, getSortedLayers(POSITIVE));
            if (result != null) {
                return result;
            }
            
            result = find(cssCtx, absX, absY, getSortedLayers(ZERO));
            if (result != null) {
                return result;
            } 
            result = find(cssCtx, absX, absY, collectLayers(AUTO));
            if (result != null) {
                return result;
            }
        }
        
        for (int i = 0; i < getFloats().size(); i++) {
            Box floater = (Box)getFloats().get(i);
            result = floater.find(cssCtx, absX, absY);
            if (result != null) {
                return result;
            }
        }
        
        result = getMaster().find(cssCtx, absX, absY);
        if (result != null) {
            return result;
        }
        
        if (isRootLayer() || isStackingContext()) {
            result = find(cssCtx, absX, absY, getSortedLayers(NEGATIVE));
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }
    
    private Box find(CssContext cssCtx, int absX, int absY, List layers) {
        Box result = null;
        for (int i = 0; i < layers.size(); i++) {
            Layer l = (Layer)layers.get(i);
            result = l.getMaster().find(cssCtx, absX, absY);
            if (result != null) {
                return result;
            }
        }
        return result;
    }
    
    public void paintAsLayer(RenderingContext c, BlockBox startingPoint) {
        List blocks = new ArrayList();
        List lines = new ArrayList();
    
        BoxCollector collector = new BoxCollector();
        collector.collect(c, c.getOutputDevice().getClip(), 
                this, startingPoint, blocks, lines);
    
        paintBackgroundsAndBorders(c, blocks);
        paintListMarkers(c, blocks);
        paintInlineContent(c, lines);
        paintReplacedElements(c, blocks);
    }    

    private void paintListMarkers(RenderingContext c, List blocks) {
        for (Iterator i = blocks.iterator(); i.hasNext();) {
            BlockBox box = (BlockBox) i.next();
            box.paintListMarker(c);
        }
    }
    
    private void paintReplacedElements(RenderingContext c, List blocks) {
        for (Iterator i = blocks.iterator(); i.hasNext();) {
            BlockBox box = (BlockBox) i.next();
            if (box.isReplaced()) {
                paintReplacedElement(c, box);
            }
        }
    }

    private void positionFixedLayer(RenderingContext c) {
        Rectangle rect = c.getFixedRectangle();
        rect.translate(-1, -1);

        Box fixed = getMaster();

        fixed.x = 0;
        fixed.y = 0;
        fixed.setAbsX(0);
        fixed.setAbsY(0);

        fixed.setContainingBlock(new ViewportBox(rect));
        ((BlockBox)fixed).positionAbsolute(c, BlockBox.POSITION_BOTH);
    }

    private void paintLayerBackgroundAndBorder(RenderingContext c) {
        if (getMaster() instanceof BlockBox) {
            BlockBox box = (BlockBox) getMaster();
            box.paintBackground(c);
            box.paintBorder(c);
        }
    }
    
    private void paintReplacedElement(RenderingContext c, BlockBox replaced) {
        Rectangle contentBounds = replaced.getContentAreaEdge(
                replaced.getAbsX(), replaced.getAbsY(), c); 
        // Minor hack:  It's inconvenient to adjust for margins, border, padding during
        // layout so just do it here.
        Point loc = replaced.getReplacedElement().getLocation();
        if (contentBounds.x != loc.x || contentBounds.y != loc.y) {
            replaced.getReplacedElement().setLocation(contentBounds.x, contentBounds.y);
        }
        if (! c.isInteractive()) {
            c.getOutputDevice().paintReplacedElement(c, replaced);
        }
    }
    
    public boolean isRootLayer() {
        return getParent() == null && isStackingContext();
    }
    
    private static class PaintingInfo {
        public Dimension outerMarginCorner;
        public Rectangle aggregateBounds;
    }
    
    private void moveIfGreater(Dimension result, Dimension test) {
        if (test.width > result.width) {
            result.width = test.width;
        }
        if (test.height > result.height) {
            result.height = test.height;
        }
    }
    
    private PaintingInfo calcPaintingDimension(LayoutContext c) {
        PaintingInfo result = scanLayer(c);
        
        List children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            Layer child = (Layer)children.get(i);
            
            if (child.getMaster().getStyle().isFixed()) {
                continue;
            } else if (child.getMaster().getStyle().isAbsolute()) {
                PaintingInfo info = child.scanLayer(c);
                moveIfGreater(result.outerMarginCorner, info.outerMarginCorner);
            } 
        }
        
        return result;
    }

    // TODO block.renderIndex = c.getNewRenderIndex();
    private PaintingInfo scanLayer(LayoutContext c) {
        return scanLayerHelper(c, getMaster());
    }
    
    private PaintingInfo scanLayerHelper(final LayoutContext c, final Box box) {
        final PaintingInfo result = new PaintingInfo();
        
        Rectangle bounds = box.getBounds(box.getAbsX(), box.getAbsY(), c, 0, 0);
        result.outerMarginCorner =
            new Dimension(bounds.x + bounds.width, bounds.y + bounds.height);
        
        result.aggregateBounds = box.getPaintingClipEdge(c);
        
        if (! (box instanceof InlineLayoutBox)) {
            if (box instanceof BlockBox && ((BlockBox)box).getPersistentBFC() != null) {
                ((BlockBox)box).getPersistentBFC().getFloatManager().performFloatOperation(
                        new FloatManager.FloatOperation() {
                            public void operate(Box floater) {
                                PaintingInfo info = scanLayerHelper(c, floater);
                                moveIfGreater(result.outerMarginCorner, info.outerMarginCorner);
                            }
                        });
            }
            
            for (int i = 0; i < box.getChildCount(); i++) {
                Box child = (Box) box.getChild(i);
                PaintingInfo info = scanLayerHelper(c, child);
                moveIfGreater(result.outerMarginCorner, info.outerMarginCorner);
                result.aggregateBounds.add(info.aggregateBounds);
            }
        } else {
            InlineLayoutBox iB = (InlineLayoutBox)box;
            for (int i = 0; i < iB.getInlineChildCount(); i++) {
                Object obj = iB.getInlineChild(i);
                if (obj instanceof Box) {
                    PaintingInfo info = scanLayerHelper(c, (Box)obj);
                    moveIfGreater(result.outerMarginCorner, info.outerMarginCorner);
                    result.aggregateBounds.add(info.aggregateBounds);
                } 
            }
        }
        
        box.setAggregateBounds(result.aggregateBounds);
        
        return result;
    }

    public void positionChildren(LayoutContext c) {
        for (Iterator i = getChildren().iterator(); i.hasNext();) {
            Layer child = (Layer) i.next();

            child.position(c);
        }
    }
    
    private void position(LayoutContext c) {
        if (getMaster().getStyle().isAbsolute() && ! c.isPrint()) {
            ((BlockBox)getMaster()).positionAbsolute(c, BlockBox.POSITION_BOTH);
        } else if (getMaster().getStyle().isRelative() && isInline()) {
            getMaster().positionRelative(c);
        }
    }

    private boolean containsFixedLayer() {
        for (Iterator i = getChildren().iterator(); i.hasNext();) {
            Layer child = (Layer) i.next();

            if (child.getMaster().getStyle().isFixed()) {
                return true;
            }
        }
        return false;
    }

    public boolean containsFixedContent() {
        return fixedBackground || containsFixedLayer();
    }

    public void setFixedBackground(boolean b) {
        this.fixedBackground = b;
    }

    public synchronized List getChildren() {
        return children == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(children);
    }
    
    public Layer getAlternateFlow(String name) {
        List children = getChildren();
        for (Iterator i = children.iterator(); i.hasNext(); ) {
            Layer child = (Layer)i.next();
            if (child.getMaster().getStyle().isAlternateFlow()) {
                CalculatedStyle cs = child.getMaster().getStyle();
                if (cs.getStringProperty(CSSName.FS_MOVE_TO_FLOW).equals(name)) {
                    return child;
                }
            }
        }
        return null;
    }

    private void remove(Layer layer) {
        boolean removed = false;
        
        if (children != null) {
            for (Iterator i = children.iterator(); i.hasNext(); ) {
                Layer child = (Layer)i.next();
                if (child == layer) {
                    removed = true;
                    i.remove();
                    break;
                }
            }
        }
        
        if (! removed) {
            throw new RuntimeException("Could not find layer to remove");
        }
    }
    
    public void detach() {
        if (getParent() != null) {
            getParent().remove(this);
        }
    }

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }

    public Box getEnd() {
        return end;
    }

    public void setEnd(Box end) {
        this.end = end;
    }

    public AbsoluteContentLayoutData getLayoutData() {
        return layoutData;
    }

    public void setLayoutData(AbsoluteContentLayoutData layoutData) {
        this.layoutData = layoutData;
    }

    public boolean isRequiresLayout() {
        return requiresLayout;
    }

    public void setRequiresLayout(boolean requiresLayout) {
        this.requiresLayout = requiresLayout;
    }
    
    public void finish(LayoutContext c) {
        if (c.isPrint()) {
            layoutAbsoluteChildren(c);
            if (isRootLayer()) {
                layoutAlternateFlows(c);
            }
        }
        if (! isInline()) {
            positionChildren(c);
        }
    }
    
    private void layoutAlternateFlows(LayoutContext c) {
        List children = getChildren();
        if (children.size() > 0) {
            LayoutState state = c.captureLayoutState();
            for (int i = 0; i < children.size(); i++) {
                Layer child = (Layer)children.get(i);
                if (child.isRequiresLayout() && child.isAlternateFlow()) {
                    CalculatedStyle cs = child.getMaster().getStyle();
                    MarginBox cb = createMarginBox(c, 
                            cs.getStringProperty(CSSName.FS_MOVE_TO_FLOW));
                    if (cb != null) {
                        child.getMaster().setContainingBlock(cb);
                        layoutAlternateFlowChild(c, child);
                        child.setRequiresLayout(false);
                        child.finish(c);
                    } else {
                        child.setRequiresLayout(false);
                    }
                }
            }
            c.restoreLayoutState(state);
        }
    }
    
    private MarginBox createMarginBox(CssContext cssCtx, String flowName) {
        List pages = getPages();
        Rectangle bounds =  null;
        for (Iterator i = pages.iterator(); i.hasNext(); ) {
            PageBox pageBox = (PageBox)i.next();
            bounds = pageBox.getFlowBounds(cssCtx, flowName);
            if (bounds != null) {
                break;
            }
        }
        return bounds == null ? null : new MarginBox(bounds);
    }
    
    private void layoutAbsoluteChildren(LayoutContext c) {
        List children = getChildren();
        if (children.size() > 0) {
            LayoutState state = c.captureLayoutState();
            for (int i = 0; i < children.size(); i++) {
                Layer child = (Layer)children.get(i);
                if (child.isRequiresLayout() && ! child.isAlternateFlow()) {
                    layoutAbsoluteChild(c, child);
                    if (child.getMaster().getStyle().isAvoidPageBreakInside() &&
                            child.getMaster().crossesPageBreak(c)) {
                        ((BlockBox)child.getMaster()).setNeedPageClear(true);
                        child.getMaster().reset(c);
                        layoutAbsoluteChild(c, child);
                        ((BlockBox)child.getMaster()).setNeedPageClear(false);
                        if (child.getMaster().crossesPageBreak(c)) {
                            child.getMaster().reset(c);
                            layoutAbsoluteChild(c, child);
                        }
                    }
                    child.setRequiresLayout(false);
                    child.finish(c);
                    c.getRootLayer().ensureHasPage(c, child.getMaster());
                }
            }
            c.restoreLayoutState(state);
        }
    }

    private void layoutAbsoluteChild(LayoutContext c, Layer child) {
        BlockBox master = (BlockBox)child.getMaster();
        if (child.getMaster().getStyle().isBottomAuto()) {
            // Set top, left
            master.positionAbsolute(c, BlockBox.POSITION_BOTH);
            master.positionAbsoluteOnPage(c);
            c.reInit(child.getLayoutData().getParentStyle());
            ((BlockBox)child.getMaster()).layout(c);
            // Set right
            master.positionAbsolute(c, BlockBox.POSITION_HORIZONTALLY);
        } else {
            // FIXME Not right in the face of pagination, but what
            // to do?  Not sure if just laying out and positioning
            // repeatedly will converge on the correct position,
            // so just guess for now
            c.reInit(child.getLayoutData().getParentStyle());
            ((BlockBox)child.getMaster()).layout(c);
            
            child.getMaster().reset(c);
            master.positionAbsolute(c, BlockBox.POSITION_BOTH);
            master.positionAbsoluteOnPage(c);
            
            c.reInit(child.getLayoutData().getParentStyle());
            ((BlockBox)child.getMaster()).layout(c);
        }
    }
    
    private void layoutAlternateFlowChild(LayoutContext c, Layer child) {
        BlockBox master = (BlockBox)child.getMaster();
        // Set top, left
        master.positionAbsolute(c, BlockBox.POSITION_BOTH);
        c.reInit(child.getLayoutData().getParentStyle());
        ((BlockBox)child.getMaster()).layout(c);
        // Set bottom, right
        master.positionAbsolute(c, BlockBox.POSITION_BOTH);
    }
    
    public List getPages() {
        return pages == null ? Collections.EMPTY_LIST : pages;
    }

    public void setPages(List pages) {
        this.pages = pages;
    }
    
    public boolean isLastPage(PageBox pageBox) {
        return this.pages.get(this.pages.size()-1) == pageBox;
    }
    
    public void addPage(CssContext c) {
        String pseudoPage = null;
        if (this.pages == null) {
            this.pages = new ArrayList();
        }
        
        List pages = getPages();
        if (pages.size() == 0) {
            pseudoPage = "first";
        } else if (pages.size() % 2 == 0) {
            pseudoPage = "left";
        } else {
            pseudoPage = "right";
        }
        PageBox pageBox = createPageBox(c, pseudoPage);
        if (pages.size() == 0) {
            pageBox.setTopAndBottom(c, 0);
        } else {
            PageBox previous = (PageBox)pages.get(pages.size()-1);
            pageBox.setTopAndBottom(c, previous.getBottom());
        }
        
        pageBox.setPageNo(pages.size());
        pages.add(pageBox);
    }
    
    public static PageBox createPageBox(CssContext c, String pseudoPage) {
        PageBox result = new PageBox();
        CalculatedStyle cs = new EmptyStyle().deriveStyle(
                c.getCss().getPageStyle(pseudoPage));
        result.setStyle(cs);
        result.setOuterPageWidth(result.getWidth(c));
        
        return result;
    }
    
    public PageBox getFirstPage(CssContext c, Box box) {
        return getPage(c, box.getAbsY());
    }
    
    public PageBox getLastPage(CssContext c, Box box) {
        return getPage(c, box.getAbsY() + box.getHeight() - 1);
    }
    
    public void ensureHasPage(CssContext c, Box box) {
        getLastPage(c, box);
    }
    
    public PageBox getPage(CssContext c, int yOffset) {
        List pages = getPages();
        if (yOffset < 0) {
            return null;
        } else {
            PageBox last = (PageBox) pages.get(pages.size()-1);
            if (yOffset < last.getBottom()) {
                for (int i = pages.size()-1; i >= 0; i--) {
                    PageBox pageBox = (PageBox)pages.get(i);
                    if (yOffset >= pageBox.getTop() && yOffset < pageBox.getBottom()) {
                        return pageBox;
                    }
                }
            } else {
                addPagesUntilPosition(c, yOffset);
                return (PageBox) pages.get(pages.size()-1);
            }
        }
        
        throw new RuntimeException("internal error");
    }
    
    private void addPagesUntilPosition(CssContext c, int position) {
        List pages = getPages();
        PageBox last = (PageBox)pages.get(pages.size()-1);
        while (position >= last.getBottom()) {
            addPage(c);
            last = (PageBox)pages.get(pages.size()-1);
        }
    }
    
    public void trimEmptyPages(CssContext c, int maxYHeight) {
        // Empty pages may result when a "keep together" constraint
        // cannot be satisfied and is dropped
        List pages = getPages();
        for (int i = pages.size() - 1; i >= 0; i--) {
            PageBox page = (PageBox)pages.get(i);
            if (page.getTop() > maxYHeight) {
                pages.remove(i);
            } else {
                break;
            }
        }
    }
    
    public void assignPagePaintingPositions(CssContext cssCtx, short mode) {
        assignPagePaintingPositions(cssCtx, mode, 0);
    }
    
    public void assignPagePaintingPositions(
            CssContext cssCtx, int mode, int additionalClearance) {
        List pages = getPages();
        int paintingTop = additionalClearance;
        for (Iterator i = pages.iterator(); i.hasNext(); ) {
            PageBox page = (PageBox)i.next();
            page.setPaintingTop(paintingTop);
            if (mode == PAGED_MODE_SCREEN) {
                page.setPaintingBottom(paintingTop + page.getHeight(cssCtx));
            } else if (mode == PAGED_MODE_PRINT){
                page.setPaintingBottom(paintingTop + page.getContentHeight(cssCtx));
            } else {
                throw new IllegalArgumentException("Illegal mode");
            }
            paintingTop = page.getPaintingBottom() + additionalClearance;
        }
    }
    
    public int getMaxPageWidth(CssContext cssCtx, int additionalClearance) {
        List pages = getPages();
        int maxWidth = 0;
        for (Iterator i = pages.iterator(); i.hasNext(); ) {
            PageBox page = (PageBox)i.next();
            int pageWidth = page.getWidth(cssCtx) + additionalClearance*2;
            if (pageWidth > maxWidth) {
                maxWidth = pageWidth;
            }
        }
        
        return maxWidth;
    }
    
    public PageBox getLastPage() {
        List pages = getPages();
        return pages.size() == 0 ? null : (PageBox)pages.get(pages.size()-1);
    }
    
    public boolean crossesPageBreak(LayoutContext c, int top, int bottom) {
        if (top < 0) {
            return false;
        }
        List pages = getPages();
        for (Iterator i = pages.iterator(); i.hasNext(); ) {
            PageBox page = (PageBox)i.next();
            if (top >= page.getTop() && top < page.getBottom()) {
                return bottom >= page.getBottom();
            }
        }
        throw new RuntimeException("Could not find page");
    }
    
    public Layer findRoot() {
        if (isRootLayer()) {
            return this;
        } else {
            return getParent().findRoot();
        }
    }
}
