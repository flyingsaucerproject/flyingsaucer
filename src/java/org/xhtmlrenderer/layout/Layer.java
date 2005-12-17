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

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.render.*;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Layer {
    private Layer parent;
    private boolean stackingContext;
    private List children;
    private Box master;
    
    private Box end;

    private List floats;
    private Map moveWithLayerFloats;

    private boolean fixedBackground;
    
    private boolean positionsFinalized;
    
    private boolean inline;
    
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
        return (int) master.getStyle().getCalculatedStyle().asFloat(CSSName.Z_INDEX);
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
        
        maybeAddMoveWithLayerFloat(floater, bfc);
    }

    public void removeFloat(FloatedBlockBox floater) {
        if (floats != null) {
            floats.remove(floater);
        }
        
        removeMoveWithLayerFloat(floater);
    }
    
    private void maybeAddMoveWithLayerFloat(FloatedBlockBox floater, 
            BlockFormattingContext bfc) {
        if (getMaster().getStyle().isRelative() && 
                ! bfc.getFloatManager().getMaster().containedIn(this)) {
            if (moveWithLayerFloats == null) {
                moveWithLayerFloats = new HashMap();
            }
            moveWithLayerFloats.put(floater, Boolean.TRUE);
        }
    }
    
    private void removeMoveWithLayerFloat(FloatedBlockBox floater) {
        if (moveWithLayerFloats != null) {
            moveWithLayerFloats.remove(floater);
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
            box.paintBackground(c);
            box.paintBorder(c);
            if (c.debugDrawBoxes()) {
                box.paintDebugOutline(c);
            }
        }
    }

    private void paintInlineContent(RenderingContext c, List lines) {
        for (Iterator i = lines.iterator(); i.hasNext();) {
            InlinePaintable paintable = (InlinePaintable) i.next();
            paintable.paintInline(c);
        }
    }
    
    public Point getMaxOffset() {
        return updateAllAbsoluteLocations(0, 0, true);
    }

    public void paint(RenderingContext c, int originX, int originY) {
        if (getMaster().getStyle().isFixed()) {
            positionFixedLayer(c);
        }

        if ((isRootLayer() && ! isPositionsFinalized()) || 
                getMaster().getStyle().isFixed()) {
            updateAllAbsoluteLocations(originX, originY, true);
        }

        if (! isInline() && ((BlockBox)getMaster()).isReplaced()) {
            paintReplacedElement(c, (BlockBox)getMaster());
        } else {
            List blocks = new ArrayList();
            List lines = new ArrayList();
    
            BoxCollector collector = new BoxCollector();
            collector.collect(c, c.getGraphics().getClip(), this, blocks, lines);
    
            // TODO root layer needs to be handled correctly (paint over entire canvas)
            if (! isInline()) {
                paintLayerBackgroundAndBorder(c);
                if (c.debugDrawBoxes()) {
                    ((BlockBox)getMaster()).paintDebugOutline(c);
                }
            }
            
            if (isRootLayer() || isStackingContext()) {
                paintLayers(c, getSortedLayers(NEGATIVE));
            }
    
            paintBackgroundsAndBorders(c, blocks);
            paintFloats(c);
            paintListMarkers(c, blocks);
            paintInlineContent(c, lines);
            paintReplacedElements(c, blocks);
    
            if (isRootLayer() || isStackingContext()) {
                paintLayers(c, collectLayers(AUTO));
                // TODO z-index: 0 layers should be painted atomically
                paintLayers(c, getSortedLayers(ZERO));
                paintLayers(c, getSortedLayers(POSITIVE));
            }
        }
    }
    
    public void paintAsLayer(RenderingContext c, BlockBox startingPoint) {
        if (startingPoint.isReplaced()) {
            paintReplacedElement(c, startingPoint);
        } else {
            List blocks = new ArrayList();
            List lines = new ArrayList();
    
            BoxCollector collector = new BoxCollector();
            collector.collect(c, c.getGraphics().getClip(), 
                    this, startingPoint, blocks, lines);
    
            paintBackgroundsAndBorders(c, blocks);
            paintListMarkers(c, blocks);
            paintInlineContent(c, lines);
            paintReplacedElements(c, blocks);
        }
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
        fixed.y = -rect.y;
        fixed.setAbsX(0);
        fixed.setAbsY(0);

        fixed.setContainingBlock(new ViewportBox(rect));
        fixed.positionPositioned(c);
    }

    private void paintLayerBackgroundAndBorder(RenderingContext c) {
        if (getMaster() instanceof BlockBox) {
            BlockBox box = (BlockBox) getMaster();
            box.paintBackground(c);
            box.paintBorder(c);
        }
    }
    
    private void paintReplacedElement(RenderingContext c, BlockBox replaced) {
        if (! c.isInteractive()) {
            replaced.component.paint(c.getGraphics());
        }
    }
    
    public boolean isRootLayer() {
        return getParent() == null && isStackingContext();
    }
    
    private void moveIfGreater(Point result, Point test) {
        if (test.x > result.x) {
            result.x = test.x;
        }
        if (test.y > result.y) {
            result.y = test.y;
        }
    }
    
    private Point updateAllAbsoluteLocations(int originX, int originY, boolean updateSelf) {
        Point result = new Point(originX, originY);
        
        if (updateSelf) {
            Point pt = updateAbsoluteLocations(originX, originY);
            moveIfGreater(result, pt);
        }
        
        List children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            Layer child = (Layer)children.get(i);
            
            if (child.getMaster().getStyle().isFixed()) {
                continue;
            } else if (child.getMaster().getStyle().isAbsolute()) {
                Point pt = child.updateAllAbsoluteLocations(
                        getMaster().getAbsX(), getMaster().getAbsY(), true);
                moveIfGreater(result, pt);
            } else if (child.getMaster().getStyle().isRelative()) {
                Point pt = child.updateAllAbsoluteLocations(0, 0, false);
                moveIfGreater(result, pt);
            }
        }
        
        return result;
    }

    // TODO block.renderIndex = c.getNewRenderIndex();
    private Point updateAbsoluteLocations(int originX, int originY) {
        return updateAbsoluteLocationsHelper(getMaster(), originX, originY);
    }
    
    private Point updateAbsoluteLocationsHelper(Box box, int x, int y) {
        return updateAbsoluteLocationsHelper(box, x, y, true);
    }

    private Point updateAbsoluteLocationsHelper(Box box, int x, int y, boolean updateSelf) {
        LineBox lineBox = null;
        
        if (box instanceof InlineBox) {
            lineBox = ((InlineBox)box).getLineBox();
        }
        
        if (updateSelf) {
            if (lineBox == null) {
                box.setAbsX(box.x + x);
                box.setAbsY(box.y + y);
            } else {
                box.setAbsX(box.x + lineBox.getAbsX());
                box.setAbsY(box.y + lineBox.getAbsY());
            }
        }
        
        final Point result = new Point(box.getAbsX() + box.getWidth(), box.getAbsY() + box.getHeight());
        
        if (box.getStyle().isAbsolute() && box.getStyle().isTopAuto() &&
                box.getStyle().isBottomAuto()) {
            ((BlockBox)box).alignToStaticEquivalent();
        }
        
        if (box instanceof BlockBox && ((BlockBox)box).isReplaced()) {
            positionReplacedElement((BlockBox)box);
        }

        if (box instanceof BlockBox && ((BlockBox)box).getPersistentBFC() != null) {
            ((BlockBox)box).getPersistentBFC().getFloatManager().updateAbsoluteLocations(
                    new FloatManager.FloatUpdater() {
                        public void update(Box floater) {
                            Point offset = updateAbsoluteLocationsHelper(floater, 0, 0, false);
                            moveIfGreater(result, offset);
                        }
                    });
        }

        if (! (box instanceof InlineBox)) {
            int nextX = box.getAbsX() + box.tx;
            int nextY = box.getAbsY() + box.ty;
            for (int i = 0; i < box.getChildCount(); i++) {
                Box child = (Box) box.getChild(i);
                Point offset = updateAbsoluteLocationsHelper(child, nextX, nextY);
                moveIfGreater(result, offset);
            }
        } else {
            InlineBox iB = (InlineBox)box;
            for (int i = 0; i < iB.getInlineChildCount(); i++) {
                Object obj = iB.getInlineChild(i);
                if (obj instanceof Box) {
                    Point offset = updateAbsoluteLocationsHelper(
                            (Box)obj, lineBox.getAbsX(), lineBox.getAbsY());
                    moveIfGreater(result, offset);
                } 
            }
        }
        
        return result;
    }
    
    private void positionReplacedElement(BlockBox box) {
        Point location = box.component.getLocation();
        if (location.x != box.getAbsX() || location.y != box.getAbsY()) {
            box.component.setLocation(box.getAbsX(), (int)box.getAbsY());    
        }
    }    

    public void positionChildren(CssContext cssCtx) {
        for (Iterator i = getChildren().iterator(); i.hasNext();) {
            Layer child = (Layer) i.next();

            child.finalizePosition(cssCtx);
        }
    }
    
    private void finalizePosition(CssContext cssCtx) {
        Dimension delta = getMaster().positionPositioned(cssCtx);
        
        if (getMaster().getStyle().isRelative()) {
            moveFloats(delta);
        }
    }
    
    private void moveFloats(Dimension distance) {
        if (moveWithLayerFloats != null) {
            for (Iterator i = moveWithLayerFloats.keySet().iterator(); i.hasNext(); ) {
                FloatedBlockBox floater = (FloatedBlockBox)i.next();
                floater.x += distance.width;
                floater.y += distance.height;
            }
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

    public boolean isPositionsFinalized() {
        return positionsFinalized;
    }

    public void setPositionsFinalized(boolean positionsFinalized) {
        this.positionsFinalized = positionsFinalized;
    }
    
    public void remove(Layer layer) {
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
}
