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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Layer {
    private Layer parent;
    private boolean stackingContext;
    private List children;
    private Box master;

    private List floats;

    private boolean fixedBackground;
    
    public Layer(Box master, boolean pseudoLayer) {
        this(null, master);
        setStackingContext(false);
    }
    
    public static void paintAsLayer(RenderingContext c, Box master, int originX, int originY) {
        Layer layer = new Layer(master);
        layer.setStackingContext(false);
        layer.paint(c, originX, originY, true);
    }

    public Layer(Box master) {
        this(null, master);
        setStackingContext(true);
    }

    public Layer(Layer parent, Box master) {
        this.parent = parent;
        this.master = master;
        setStackingContext(!master.getStyle().isAutoZIndex());
        master.setLayer(this);
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

    public void addFloat(FloatedBlockBox floater) {
        if (isStackingContext()) {
            if (floats == null) {
                floats = new ArrayList();
            }
    
            floats.add(floater);
        } else {
            getParent().addFloat(floater);
        }
    }

    public void removeFloat(FloatedBlockBox floater) {
        if (isStackingContext()) {
            if (floats != null) {
                floats.remove(floater);
            }
        } else {
            getParent().removeFloat(floater);
        }
    }

    private void paintFloats(RenderingContext c) {
        if (floats != null) {
            for (int i = floats.size() - 1; i >= 0; i--) {
                FloatedBlockBox floater = (FloatedBlockBox) floats.get(i);
                paintAsLayer(c, floater, floater.getAbsX(), floater.getAbsY());
            }
        }
    }

    private void paintLayers(RenderingContext c, List layers) {
        for (int i = 0; i < layers.size(); i++) {
            Layer layer = (Layer) layers.get(i);
            layer.paint(c, getMaster().getAbsX(), getMaster().getAbsY());
        }
    }
    
    private List collectZIndexAutoLayers() {
        List result = new ArrayList();
        
        List children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            Layer child = (Layer)children.get(i);
            if (! child.isStackingContext()) {
                result.add(child);
                result.addAll(child.collectZIndexAutoLayers());
            }
        }
        
        return result;
    }
    
    private static final int POSITIVE = 1;
    private static final int ZERO = 2;
    private static final int NEGATIVE = 3;
    
    private List getSortedLayers(int which) {
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
        
        if (which == NEGATIVE || which == POSITIVE) {
            Collections.sort(result, new ZIndexComparator());
        }
        
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
        }
    }

    private void paintInlineContent(RenderingContext c, List lines) {
        for (Iterator i = lines.iterator(); i.hasNext();) {
            LineBox line = (LineBox) i.next();
            Point before = c.getOriginOffset();
            c.translate(line.getAbsX() - line.x, line.getAbsY() - line.y);
            InlineRendering.paintLine(c, line);
            Point after = c.getOriginOffset();
            c.translate(before.x - after.x, before.y - after.y);
        }
    }
    
    public void paint(RenderingContext c, int originX, int originY) {
        paint(c, originX, originY, isRootLayer());
    }

    private void paint(RenderingContext c, int originX, int originY, 
            boolean updateAbsoluteLocations) {
        if (getMaster().getStyle().isFixed()) {
            positionFixedLayer(c);
        }

        if (updateAbsoluteLocations || getMaster().getStyle().isFixed()) {
            updateAllAbsoluteLocations(originX, originY, true);
        }

        if (getMaster().isReplaced()) {
            paintReplacedElement(c, getMaster());
        } else {
            List blocks = new ArrayList();
            List lines = new ArrayList();
    
            collectBoxes(c, getMaster(), blocks, lines);
    
            // TODO root layer needs to be handled correctly (paint over entire canvas)
            paintLayerBackgroundAndBorder(c);
            
            paintLayers(c, getSortedLayers(NEGATIVE));
    
            paintBackgroundsAndBorders(c, blocks);
            paintFloats(c);
            paintInlineContent(c, lines);
            paintListStyles(c, blocks);
            paintReplacedElements(c, blocks);
    
            paintLayers(c, collectZIndexAutoLayers());
            // TODO z-index: 0 layers should be painted atomically
            paintLayers(c, getSortedLayers(ZERO));
            paintLayers(c, getSortedLayers(POSITIVE));
        }
    }

    private void paintListStyles(RenderingContext c, List blocks) {
        for (Iterator i = blocks.iterator(); i.hasNext();) {
            BlockBox box = (BlockBox) i.next();
            box.paintListStyles(c);
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
        fixed.positionPositionedBox(c);
    }

    private void paintLayerBackgroundAndBorder(RenderingContext c) {
        if (getMaster() instanceof BlockBox) {
            BlockBox box = (BlockBox) getMaster();
            box.paintBackground(c);
            box.paintBorder(c);
        }
    }
    
    private void paintReplacedElement(RenderingContext c, Box replaced) {
        replaced.component.setLocation(replaced.getAbsX(), (int)replaced.getAbsY());
        if (! c.isInteractive()) {
            replaced.component.paint(c.getGraphics());
        }
    }
    
    private boolean isRootLayer() {
        return getParent() == null && isStackingContext();
    }
    
    private void updateAllAbsoluteLocations(int originX, int originY, boolean updateSelf) {
        if (updateSelf) {
            updateAbsoluteLocations(originX, originY);
        }
        
        List children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            Layer child = (Layer)children.get(i);
            
            if (child.getMaster().getStyle().isFixed()) {
                continue;
            } else if (child.getMaster().getStyle().isAbsolute()) {
                child.updateAllAbsoluteLocations(
                        getMaster().getAbsX(), getMaster().getAbsY(), true);
            } else if (child.getMaster().getStyle().isRelative()) {
                child.updateAllAbsoluteLocations(0, 0, false);
            }
        }
    }

    // TODO Inline borders and padding can slide an inline box around at render
    // TODO block.renderIndex = c.getNewRenderIndex();
    // TODO Don't do this every time 
    private void updateAbsoluteLocations(int originX, int originY) {
        updateAbsoluteLocationsHelper(getMaster(), originX, originY);
    }

    private void updateAbsoluteLocationsHelper(Box box, int x, int y) {
        box.setAbsX(box.x + x);
        box.setAbsY(box.y + y);

        int nextX = (int) box.getAbsX() + box.tx;
        int nextY = (int) box.getAbsY() + box.ty;

        if (box.getPersistentBFC() != null) {
            box.getPersistentBFC().getFloatManager().updateAbsoluteLocations(nextX, nextY);
        }

        if (!(box instanceof LineBox)) {
            for (int i = 0; i < box.getChildCount(); i++) {
                Box child = (Box) box.getChild(i);
                updateAbsoluteLocationsHelper(child, nextX, nextY);
            }
        }
    }

    public void positionChildren(CssContext cssCtx) {
        for (Iterator i = getChildren().iterator(); i.hasNext();) {
            Layer child = (Layer) i.next();

            child.getMaster().positionPositionedBox(cssCtx);
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

    protected void collectBoxes(RenderingContext c, Box container, List blockContent, List inlineContent) {
        if (container instanceof LineBox) {
            if (container.intersects(c, c.getGraphics().getClip())) {
                inlineContent.add(container);
            }
        } else {
            if (container.getLayer() == null || !(container instanceof BlockBox)) {
                if (container.intersects(c, c.getGraphics().getClip())) {
                    blockContent.add(container);
                }
            }

            if (container.getLayer() == null || container == getMaster()) {
                for (int i = 0; i < container.getChildCount(); i++) {
                    Box child = container.getChild(i);
                    collectBoxes(c, child, blockContent, inlineContent);
                }
            }
        }
    }
}
