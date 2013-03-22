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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.PageElementPosition;
import org.xhtmlrenderer.css.newmatch.PageInfo;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.newtable.TableCellBox;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.BoxDimensions;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.render.ViewportBox;

/**
 * All positioned content as well as content with an overflow value other
 * than visible creates a layer.  Layers which define stacking contexts
 * provide the entry for rendering the box tree to an output device.  The main
 * purpose of this class is to provide an implementation of Appendix E of the
 * spec, but it also provides additional utility services including page
 * management and mapping boxes to coordinates (for e.g. links).  When 
 * rendering to a paged output device, the layer is also responsible for laying
 * out absolute content (which is layed out after its containing block has
 * completed layout).
 */
public class Layer {
    public static final short PAGED_MODE_SCREEN = 1;
    public static final short PAGED_MODE_PRINT = 2;
    
    private Layer _parent;
    private boolean _stackingContext;
    private List _children;
    private Box _master;
    
    private Box _end;

    private List _floats;

    private boolean _fixedBackground;
    
    private boolean _inline;
    private boolean _requiresLayout;
    
    private List _pages;
    private PageBox _lastRequestedPage = null;
    
    private Set _pageSequences;
    private List _sortedPageSequences;
    
    private Map _runningBlocks;
    
    private Box _selectionStart;
    private Box _selectionEnd;
    
    private int _selectionStartX;
    private int _selectionStartY;
    
    private int _selectionEndX;
    private int _selectionEndY;
    
    public Layer(Box master) {
        this(null, master);
        setStackingContext(true);
    }

    public Layer(Layer parent, Box master) {
        _parent = parent;
        _master = master;
        setStackingContext(
                master.getStyle().isPositioned() && ! master.getStyle().isAutoZIndex());
        master.setLayer(this);
        master.setContainingLayer(this);
    }

    public Layer getParent() {
        return _parent;
    }

    public boolean isStackingContext() {
        return _stackingContext;
    }

    public void setStackingContext(boolean stackingContext) {
        _stackingContext = stackingContext;
    }

    public int getZIndex() {
        return (int) _master.getStyle().asFloat(CSSName.Z_INDEX);
    }
    
    public Box getMaster() {
        return _master;
    }

    public synchronized void addChild(Layer layer) {
        if (_children == null) {
            _children = new ArrayList();
        }
        _children.add(layer);
    }

    public void addFloat(BlockBox floater, BlockFormattingContext bfc) {
        if (_floats == null) {
            _floats = new ArrayList();
        }

        _floats.add(floater);
        
        floater.getFloatedBoxData().setDrawingLayer(this);
    }

    public void removeFloat(BlockBox floater) {
        if (_floats != null) {
            _floats.remove(floater);
        }
    }

    private void paintFloats(RenderingContext c) {
        if (_floats != null) {
            for (int i = _floats.size() - 1; i >= 0; i--) {
                BlockBox floater = (BlockBox) _floats.get(i);
                paintAsLayer(c, floater);
            }
        }
    }

    private void paintLayers(RenderingContext c, List layers) {
        for (int i = 0; i < layers.size(); i++) {
            Layer layer = (Layer) layers.get(i);
            layer.paint(c);
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

            if (target.isStackingContext()) {
                int zIndex = target.getZIndex();
                if (which == NEGATIVE && zIndex < 0) {
                    result.add(target);
                } else if (which == POSITIVE && zIndex > 0) {
                    result.add(target);
                } else if (which == ZERO && zIndex == 0) {
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
    
    private void paintBackgroundsAndBorders(
            RenderingContext c, List blocks, 
            Map collapsedTableBorders, BoxRangeLists rangeLists) {
        BoxRangeHelper helper = new BoxRangeHelper(c.getOutputDevice(), rangeLists.getBlock());
        
        for (int i = 0; i < blocks.size(); i++) {
            helper.popClipRegions(c, i);
            
            BlockBox box = (BlockBox)blocks.get(i);
            box.paintBackground(c);
            box.paintBorder(c);
            if (c.debugDrawBoxes()) {
                box.paintDebugOutline(c);
            }
            
            if (collapsedTableBorders != null && box instanceof TableCellBox) {
                TableCellBox cell = (TableCellBox)box;
                if (cell.hasCollapsedPaintingBorder()) {
                    List borders = (List)collapsedTableBorders.get(cell);
                    if (borders != null) {
                        paintCollapsedTableBorders(c, borders);
                    }
                }
            }

            helper.pushClipRegion(c, i);
        }
        
        helper.popClipRegions(c, blocks.size());
    }

    private void paintInlineContent(RenderingContext c, List lines, BoxRangeLists rangeLists) {
        BoxRangeHelper helper = new BoxRangeHelper(
                c.getOutputDevice(), rangeLists.getInline());
        
        for (int i = 0; i < lines.size(); i++) {
            helper.popClipRegions(c, i);
            helper.pushClipRegion(c, i);
            
            InlinePaintable paintable = (InlinePaintable)lines.get(i);
            paintable.paintInline(c);
        }
        
        helper.popClipRegions(c, lines.size());
    }
    
    private void paintSelection(RenderingContext c, List lines) {
        if (c.getOutputDevice().isSupportsSelection()) {
            for (Iterator i = lines.iterator(); i.hasNext();) {
                InlinePaintable paintable = (InlinePaintable) i.next();
                if (paintable instanceof InlineLayoutBox) {
                    ((InlineLayoutBox)paintable).paintSelection(c);
                }
            }
        }
    }
    
    public Dimension getPaintingDimension(LayoutContext c) {
        return calcPaintingDimension(c).getOuterMarginCorner();
    }
    
    public void paint(RenderingContext c) {
        if (getMaster().getStyle().isFixed()) {
            positionFixedLayer(c);
        }
        
        if (isRootLayer()) {
            getMaster().paintRootElementBackground(c);
        }
        
        if (! isInline() && ((BlockBox)getMaster()).isReplaced()) {
            paintLayerBackgroundAndBorder(c);
            paintReplacedElement(c, (BlockBox)getMaster());
        } else {
            BoxRangeLists rangeLists = new BoxRangeLists();
            
            List blocks = new ArrayList();
            List lines = new ArrayList();
    
            BoxCollector collector = new BoxCollector();
            collector.collect(c, c.getOutputDevice().getClip(), this, blocks, lines, rangeLists);
    
            if (! isInline()) {
                paintLayerBackgroundAndBorder(c);
                if (c.debugDrawBoxes()) {
                    ((BlockBox)getMaster()).paintDebugOutline(c);
                }
            }
            
            if (isRootLayer() || isStackingContext()) {
                paintLayers(c, getSortedLayers(NEGATIVE));
            }
            
            Map collapsedTableBorders = collectCollapsedTableBorders(c, blocks);
    
            paintBackgroundsAndBorders(c, blocks, collapsedTableBorders, rangeLists);
            paintFloats(c);
            paintListMarkers(c, blocks, rangeLists);
            paintInlineContent(c, lines, rangeLists);
            paintReplacedElements(c, blocks, rangeLists);
            paintSelection(c, lines); // XXX do only when there is a selection
    
            if (isRootLayer() || isStackingContext()) {
                paintLayers(c, collectLayers(AUTO));
                // TODO z-index: 0 layers should be painted atomically
                paintLayers(c, getSortedLayers(ZERO));
                paintLayers(c, getSortedLayers(POSITIVE));
            }
        }
    }
    
    private List getFloats() {
        return _floats == null ? Collections.EMPTY_LIST : _floats;
    }
    
    public Box find(CssContext cssCtx, int absX, int absY, boolean findAnonymous) {
        Box result = null;
        if (isRootLayer() || isStackingContext()) {
            result = find(cssCtx, absX, absY, getSortedLayers(POSITIVE), findAnonymous);
            if (result != null) {
                return result;
            }
            
            result = find(cssCtx, absX, absY, getSortedLayers(ZERO), findAnonymous);
            if (result != null) {
                return result;
            }
 
            result = find(cssCtx, absX, absY, collectLayers(AUTO), findAnonymous);
            if (result != null) {
                return result;
            }
        }
        
        for (int i = 0; i < getFloats().size(); i++) {
            Box floater = (Box)getFloats().get(i);
            result = floater.find(cssCtx, absX, absY, findAnonymous);
            if (result != null) {
                return result;
            }
        }
        
        result = getMaster().find(cssCtx, absX, absY, findAnonymous);
        if (result != null) {
            return result;
        }
        
        if (isRootLayer() || isStackingContext()) {
            result = find(cssCtx, absX, absY, getSortedLayers(NEGATIVE), findAnonymous);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }
    
    private Box find(CssContext cssCtx, int absX, int absY, List layers, boolean findAnonymous) {
        Box result = null;
        // Work backwards since layers are painted forwards and we're looking
        // for the top-most box
        for (int i = layers.size()-1; i >= 0; i--) {
            Layer l = (Layer)layers.get(i);
            result = l.find(cssCtx, absX, absY, findAnonymous);
            if (result != null) {
                return result;
            }
        }
        return result;
    }
    
    // Bit of a kludge here.  We need to paint collapsed table borders according
    // to priority so (for example) wider borders float to the top and aren't
    // overpainted by thinner borders.  This method scans the block boxes
    // we're about to draw and returns a map with the last cell in a given table
    // we'll paint as a key and a sorted list of borders as values.  These are
    // then painted after we've drawn the background for this cell.
    private Map collectCollapsedTableBorders(RenderingContext c, List blocks) {
        Map cellBordersByTable = new HashMap();
        Map triggerCellsByTable = new HashMap();
        
        Set all = new HashSet();
        for (Iterator i = blocks.iterator(); i.hasNext(); ) {
            Box b = (Box)i.next();
            if (b instanceof TableCellBox) {
                TableCellBox cell = (TableCellBox)b;
                if (cell.hasCollapsedPaintingBorder()) {
                    List borders = (List)cellBordersByTable.get(cell.getTable());
                    if (borders == null) {
                        borders = new ArrayList();
                        cellBordersByTable.put(cell.getTable(), borders);
                    }
                    triggerCellsByTable.put(cell.getTable(), cell);
                    cell.addCollapsedBorders(all, borders);
                }
            }
        }
        
        if (triggerCellsByTable.size() == 0) {
            return null;
        } else {
            Map result = new HashMap();
            
            for (Iterator i = triggerCellsByTable.values().iterator(); i.hasNext(); ) {
                TableCellBox cell = (TableCellBox)i.next();
                List borders = (List)cellBordersByTable.get(cell.getTable());
                Collections.sort(borders);
                result.put(cell, borders);
            }
            
            return result;
        }
    }
    
    private void paintCollapsedTableBorders(RenderingContext c, List borders) {
        for (Iterator i = borders.iterator(); i.hasNext(); ) {
            CollapsedBorderSide border = (CollapsedBorderSide)i.next();
            border.getCell().paintCollapsedBorder(c, border.getSide());
        }
    }
    
    public void paintAsLayer(RenderingContext c, BlockBox startingPoint) {
        BoxRangeLists rangeLists = new BoxRangeLists();
        
        List blocks = new ArrayList();
        List lines = new ArrayList();
    
        BoxCollector collector = new BoxCollector();
        collector.collect(c, c.getOutputDevice().getClip(), 
                this, startingPoint, blocks, lines, rangeLists);
    
        Map collapsedTableBorders = collectCollapsedTableBorders(c, blocks);
        
        paintBackgroundsAndBorders(c, blocks, collapsedTableBorders, rangeLists);
        paintListMarkers(c, blocks, rangeLists);
        paintInlineContent(c, lines, rangeLists);
        paintSelection(c, lines); // XXX only do when there is a selection
        paintReplacedElements(c, blocks, rangeLists);
    }    

    private void paintListMarkers(RenderingContext c, List blocks, BoxRangeLists rangeLists) {
        BoxRangeHelper helper = new BoxRangeHelper(c.getOutputDevice(), rangeLists.getBlock());
        
        for (int i = 0; i < blocks.size(); i++) {
            helper.popClipRegions(c, i);
            
            BlockBox box = (BlockBox)blocks.get(i);
            box.paintListMarker(c);
            
            helper.pushClipRegion(c, i);
        }
        
        helper.popClipRegions(c, blocks.size());        
    }
    
    private void paintReplacedElements(RenderingContext c, List blocks, BoxRangeLists rangeLists) {
        BoxRangeHelper helper = new BoxRangeHelper(c.getOutputDevice(), rangeLists.getBlock());
        
        for (int i = 0; i < blocks.size(); i++) {
            helper.popClipRegions(c, i);
            
            BlockBox box = (BlockBox)blocks.get(i);
            if (box.isReplaced()) {
                paintReplacedElement(c, box);
            }
            
            helper.pushClipRegion(c, i);
        }
        
        helper.popClipRegions(c, blocks.size());
    }

    private void positionFixedLayer(RenderingContext c) {
        Rectangle rect = c.getFixedRectangle();

        Box fixed = getMaster();

        fixed.setX(0);
        fixed.setY(0);
        fixed.setAbsX(0);
        fixed.setAbsY(0);

        fixed.setContainingBlock(new ViewportBox(rect));
        ((BlockBox)fixed).positionAbsolute(c, BlockBox.POSITION_BOTH);
        
        fixed.calcPaintingInfo(c, false);
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
        if (! c.isInteractive() || replaced.getReplacedElement().isRequiresInteractivePaint()) {
            c.getOutputDevice().paintReplacedElement(c, replaced);
        }
    }
    
    public boolean isRootLayer() {
        return getParent() == null && isStackingContext();
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
        getMaster().calcPaintingInfo(c, true);
        PaintingInfo result = (PaintingInfo)getMaster().getPaintingInfo().copyOf();
        
        List children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            Layer child = (Layer)children.get(i);
            
            if (child.getMaster().getStyle().isFixed()) {
                continue;
            } else if (child.getMaster().getStyle().isAbsolute()) {
                PaintingInfo info = child.calcPaintingDimension(c);
                moveIfGreater(result.getOuterMarginCorner(), info.getOuterMarginCorner());
            } 
        }
        
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
        } else if (getMaster().getStyle().isRelative() && 
                (isInline() || ((BlockBox)getMaster()).isInline())) {
            getMaster().positionRelative(c);
            if (! isInline()) {
                getMaster().calcCanvasLocation();
                getMaster().calcChildLocations();
            }
             
        }
    }

    private boolean containsFixedLayer() {
        for (Iterator i = getChildren().iterator(); i.hasNext();) {
            Layer child = (Layer) i.next();

            if (child.getMaster().getStyle().isFixed() || child.containsFixedLayer()) {
                return true;
            }
        }
        return false;
    }

    public boolean containsFixedContent() {
        return _fixedBackground || containsFixedLayer();
    }

    public void setFixedBackground(boolean b) {
        _fixedBackground = b;
    }

    public synchronized List getChildren() {
        return _children == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(_children);
    }
    
    private void remove(Layer layer) {
        boolean removed = false;

        // access to _children is synchronized
        synchronized (this) {
            if (_children != null) {
                for (Iterator i = _children.iterator(); i.hasNext(); ) {
                    Layer child = (Layer)i.next();
                    if (child == layer) {
                        removed = true;
                        i.remove();
                        break;
                    }
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
        return _inline;
    }

    public void setInline(boolean inline) {
        _inline = inline;
    }

    public Box getEnd() {
        return _end;
    }

    public void setEnd(Box end) {
        _end = end;
    }

    public boolean isRequiresLayout() {
        return _requiresLayout;
    }

    public void setRequiresLayout(boolean requiresLayout) {
        _requiresLayout = requiresLayout;
    }
    
    public void finish(LayoutContext c) {
        if (c.isPrint()) {
            layoutAbsoluteChildren(c);
        }
        if (! isInline()) {
            positionChildren(c);
        }
    }
    
    private void layoutAbsoluteChildren(LayoutContext c) {
        List children = getChildren();
        if (children.size() > 0) {
            LayoutState state = c.captureLayoutState();
            for (int i = 0; i < children.size(); i++) {
                Layer child = (Layer)children.get(i);
                if (child.isRequiresLayout()) {
                    layoutAbsoluteChild(c, child);
                    if (child.getMaster().getStyle().isAvoidPageBreakInside() &&
                            child.getMaster().crossesPageBreak(c)) {
                        child.getMaster().reset(c);
                        ((BlockBox)child.getMaster()).setNeedPageClear(true);
                        layoutAbsoluteChild(c, child);
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
            c.reInit(true);
            ((BlockBox)child.getMaster()).layout(c);
            // Set right
            master.positionAbsolute(c, BlockBox.POSITION_HORIZONTALLY);
        } else {
            // FIXME Not right in the face of pagination, but what
            // to do?  Not sure if just laying out and positioning
            // repeatedly will converge on the correct position,
            // so just guess for now
            c.reInit(true);
            master.layout(c);
            
            BoxDimensions before = master.getBoxDimensions();
            master.reset(c);
            BoxDimensions after = master.getBoxDimensions();
            master.setBoxDimensions(before);
            master.positionAbsolute(c, BlockBox.POSITION_BOTH);
            master.positionAbsoluteOnPage(c);
            master.setBoxDimensions(after);
            
            c.reInit(true);
            ((BlockBox)child.getMaster()).layout(c);
        }
    }
    
    public List getPages() {
        return _pages == null ? Collections.EMPTY_LIST : _pages;
    }

    public void setPages(List pages) {
        _pages = pages;
    }
    
    public boolean isLastPage(PageBox pageBox) {
        return _pages.get(_pages.size()-1) == pageBox;
    }
    
    public void addPage(CssContext c) {
        String pseudoPage = null;
        if (_pages == null) {
            _pages = new ArrayList();
        }
        
        List pages = getPages();
        if (pages.size() == 0) {
            pseudoPage = "first";
        } else if (pages.size() % 2 == 0) {
            pseudoPage = "right";
        } else {
            pseudoPage = "left";
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
    
    public void removeLastPage() {
        PageBox pageBox = (PageBox)_pages.remove(_pages.size()-1);
        if (pageBox == getLastRequestedPage()) {
            setLastRequestedPage(null);
        }
    }
    
    public static PageBox createPageBox(CssContext c, String pseudoPage) {
        PageBox result = new PageBox();
        
        String pageName = null;
        // HACK We only create pages during layout, but the OutputDevice
        // queries page positions and since pages are created lazily, changing
        // this method to use LayoutContext is tricky
        if (c instanceof LayoutContext) {
            pageName = ((LayoutContext)c).getPageName();
        }
        
        PageInfo pageInfo = c.getCss().getPageStyle(pageName, pseudoPage);
        result.setPageInfo(pageInfo);
        
        CalculatedStyle cs = new EmptyStyle().deriveStyle(pageInfo.getPageStyle());
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
            PageBox lastRequested = getLastRequestedPage();
            if (lastRequested != null) {
                if (yOffset >= lastRequested.getTop() && yOffset < lastRequested.getBottom()) {
                    return lastRequested;
                }
            }
            PageBox last = (PageBox) pages.get(pages.size()-1);
            if (yOffset < last.getBottom()) {
                // The page we're looking for is probably at the end of the
                // document so do a linear search for the first few pages
                // and then fall back to a binary search if that doesn't work 
                // out
                int count = pages.size();
                for (int i = count-1; i >= 0 && i >= count-5; i--) {
                    PageBox pageBox = (PageBox)pages.get(i);
                    if (yOffset >= pageBox.getTop() && yOffset < pageBox.getBottom()) {
                        setLastRequestedPage(pageBox);
                        return pageBox;
                    }
                }
                
                int low = 0;
                int high = count-6;
                
                while (low <= high) {
                    int mid = (low + high) >> 1;
                    PageBox pageBox = (PageBox)pages.get(mid);
                    
                    if (yOffset >= pageBox.getTop() && yOffset < pageBox.getBottom()) {
                        setLastRequestedPage(pageBox);
                        return pageBox;
                    }
                    
                    if (pageBox.getTop() < yOffset) {
                        low = mid + 1;
                    } else {
                        high = mid - 1;
                    }
                }
            } else {
                addPagesUntilPosition(c, yOffset);
                PageBox result = (PageBox) pages.get(pages.size()-1);
                setLastRequestedPage(result);
                return result;
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
        for (int i = pages.size() - 1; i > 0; i--) {
            PageBox page = (PageBox)pages.get(i);
            if (page.getTop() >= maxYHeight) {
                if (page == getLastRequestedPage()) {
                    setLastRequestedPage(null);
                }
                pages.remove(i);
            } else {
                break;
            }
        }
    }
    
    public void trimPageCount(int newPageCount) {
        while (_pages.size() > newPageCount) {
            PageBox pageBox = (PageBox)_pages.remove(_pages.size()-1);
            if (pageBox == getLastRequestedPage()) {
                setLastRequestedPage(null);
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
        PageBox page = getPage(c, top);
        return bottom >= page.getBottom() - c.getExtraSpaceBottom();
    }
    
    public Layer findRoot() {
        if (isRootLayer()) {
            return this;
        } else {
            return getParent().findRoot();
        }
    }
    
    public void addRunningBlock(BlockBox block) {
        if (_runningBlocks == null) {
            _runningBlocks = new HashMap();
        }
        
        String identifier = block.getStyle().getRunningName();
        
        List blocks = (List)_runningBlocks.get(identifier);
        if (blocks == null) {
            blocks = new ArrayList();
            _runningBlocks.put(identifier, blocks);
        }
        
        blocks.add(block);
        
        Collections.sort(blocks, new Comparator() {
            public int compare(Object o1, Object o2) {
                BlockBox b1 = (BlockBox)o1;
                BlockBox b2 = (BlockBox)o2;
                
                return b1.getAbsY() - b2.getAbsY();
            }
        });
    }
    
    public void removeRunningBlock(BlockBox block) {
        if (_runningBlocks == null) {
            return;
        }
        
        String identifier = block.getStyle().getRunningName();
        
        List blocks = (List)_runningBlocks.get(identifier);
        if (blocks == null) {
            return;
        }
        
        blocks.remove(block);
    }
    
    public BlockBox getRunningBlock(String identifer, PageBox page, PageElementPosition which) {
        if (_runningBlocks == null) {
            return null;
        }
        
        List blocks = (List)_runningBlocks.get(identifer);
        if (blocks == null) {
            return null;
        }
        
        if (which == PageElementPosition.START) {
            BlockBox prev = null;
            for (Iterator i = blocks.iterator(); i.hasNext(); ) {
                BlockBox b = (BlockBox)i.next();
                if (b.getStaticEquivalent().getAbsY() >= page.getTop()) {
                    break;
                }
                prev = b;
            }
            return prev;
        } else if (which == PageElementPosition.FIRST) {
            for (Iterator i = blocks.iterator(); i.hasNext(); ) {
                BlockBox b = (BlockBox)i.next();
                int absY = b.getStaticEquivalent().getAbsY();
                if (absY >= page.getTop() && absY < page.getBottom()) {
                    return b;
                }
            }
            return getRunningBlock(identifer, page, PageElementPosition.START);
        } else if (which == PageElementPosition.LAST) {
            BlockBox prev = null;
            for (Iterator i = blocks.iterator(); i.hasNext(); ) {
                BlockBox b = (BlockBox)i.next();
                if (b.getStaticEquivalent().getAbsY() > page.getBottom()) {
                    break;
                }
                prev = b;
            }
            return prev;
        } else if (which == PageElementPosition.LAST_EXCEPT) {
            BlockBox prev = null;
            for (Iterator i = blocks.iterator(); i.hasNext(); ) {
                BlockBox b = (BlockBox)i.next();
                int absY = b.getStaticEquivalent().getAbsY();
                if (absY >= page.getTop() && absY < page.getBottom()) {
                    return null;
                }
                if (absY > page.getBottom()) {
                    break;
                }
                prev = b;
            }
            return prev;
        }
        
        throw new RuntimeException("bug: internal error");
    }
    
    public void layoutPages(LayoutContext c) {
        c.setRootDocumentLayer(c.getRootLayer());
        for (Iterator i = _pages.iterator(); i.hasNext(); ) {
            PageBox pageBox = (PageBox)i.next();
            pageBox.layout(c);
        }
    }
    
    public void addPageSequence(BlockBox start) {
        if (_pageSequences == null) {
            _pageSequences = new HashSet();
        }
        
        _pageSequences.add(start);
    }
    
    private List getSortedPageSequences() {
        if (_pageSequences == null) {
            return null;
        }
        
        if (_sortedPageSequences == null) {
            List result = new ArrayList(_pageSequences);
            
            Collections.sort(result, new Comparator() {
                public int compare(Object o1, Object o2) {
                    BlockBox b1 = (BlockBox)o1;
                    BlockBox b2 = (BlockBox)o2;
                    
                    return b1.getAbsY() - b2.getAbsY();
                }
            });
            
            _sortedPageSequences  = result;
        }
        
        return _sortedPageSequences;
    }
    
    public int getRelativePageNo(RenderingContext c) {
        List sequences = getSortedPageSequences();
        int initial = 0;
        if (c.getInitialPageNo() > 0) {
            initial = c.getInitialPageNo() - 1;
        }
        if (sequences == null) {
            return initial + c.getPageNo();
        } else {
            int sequenceStartIndex = getPageSequenceStart(c, sequences, c.getPage());
            if (sequenceStartIndex == -1) {
                return initial + c.getPageNo();
            } else {
                BlockBox block = (BlockBox)sequences.get(sequenceStartIndex);
                return c.getPageNo() - getFirstPage(c, block).getPageNo();
            }
        }
    }
    
    public int getRelativePageCount(RenderingContext c) {
        List sequences = getSortedPageSequences();
        int initial = 0;
        if (c.getInitialPageNo() > 0) {
            initial = c.getInitialPageNo() - 1;
        }
        if (sequences == null) {
            return initial + c.getPageCount();
        } else {
            int firstPage;
            int lastPage;
            
            int sequenceStartIndex = getPageSequenceStart(c, sequences, c.getPage());
            
            if (sequenceStartIndex == -1) {
                firstPage = 0;
            } else {
                BlockBox block = (BlockBox)sequences.get(sequenceStartIndex);
                firstPage = getFirstPage(c, block).getPageNo();
            }
            
            if (sequenceStartIndex < sequences.size() - 1) {
                BlockBox block = (BlockBox)sequences.get(sequenceStartIndex+1);
                lastPage = getFirstPage(c, block).getPageNo();
            } else {
                lastPage = c.getPageCount();
            }
            
            int sequenceLength = lastPage - firstPage;
            if (sequenceStartIndex == -1) {
                sequenceLength += initial;
            }
            
            return sequenceLength;
        }
    }    
    
    private int getPageSequenceStart(RenderingContext c, List sequences, PageBox page) {
        for (int i = sequences.size() - 1; i >= 0; i--) {
            BlockBox start = (BlockBox)sequences.get(i);
            if (start.getAbsY() < page.getBottom() - 1) {
                return i;
            }
        }
        
        return -1;
    }    

    public Box getSelectionEnd() {
        return _selectionEnd;
    }

    public void setSelectionEnd(Box selectionEnd) {
        _selectionEnd = selectionEnd;
    }

    public Box getSelectionStart() {
        return _selectionStart;
    }

    public void setSelectionStart(Box selectionStart) {
        _selectionStart = selectionStart;
    }

    public int getSelectionEndX() {
        return _selectionEndX;
    }

    public void setSelectionEndX(int selectionEndX) {
        _selectionEndX = selectionEndX;
    }

    public int getSelectionEndY() {
        return _selectionEndY;
    }

    public void setSelectionEndY(int selectionEndY) {
        _selectionEndY = selectionEndY;
    }

    public int getSelectionStartX() {
        return _selectionStartX;
    }

    public void setSelectionStartX(int selectionStartX) {
        _selectionStartX = selectionStartX;
    }

    public int getSelectionStartY() {
        return _selectionStartY;
    }

    public void setSelectionStartY(int selectionStartY) {
        _selectionStartY = selectionStartY;
    }

    private PageBox getLastRequestedPage() {
        return _lastRequestedPage;
    }

    private void setLastRequestedPage(PageBox lastRequestedPage) {
        _lastRequestedPage = lastRequestedPage;
    }
}
