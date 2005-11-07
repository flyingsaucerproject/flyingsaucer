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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Layer {
    private Layer parent;
    private boolean stackingContext;
    private List children;
    private Box master;

    private int x = 0;
    private int y = 0;

    private List floats;

    private boolean fixedBackground;

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

    public void addChild(Layer layer) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(layer);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void translate(int x, int y) {
        this.x -= x;
        this.y -= y;
    }

    public void addFloat(FloatedBlockBox floater) {
        if (floats == null) {
            floats = new ArrayList();
        }

        floats.add(floater);
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
                floater.paint(c, (int) floater.absX, (int) floater.absY);
            }
        }
    }

    private void paintLayers(RenderingContext c, int tx, int ty) {
        if (children != null) {
            for (int i = children.size() - 1; i >= 0; i--) {
                Layer layer = (Layer) children.get(i);
                layer.paint(c, tx, ty);
            }
        }
    }

    public void paint(RenderingContext c, int tx, int ty) {
        updateAbsoluteLocations(tx, ty);

        c.translate(tx, ty);
        c.getGraphics().translate(tx, ty);
        
        // HACK
        Rectangle fixed = c.getFixedRectangle();
        fixed.translate(-1, -1);
        if (getMaster().getStyle().isFixed()) {
            repositionFixed(c, getMaster(), fixed);
            c.translate(0, -fixed.y);
            c.getGraphics().translate(0, -fixed.y);
        }

        BoxRendering.paint(c, getMaster());
        paintFloats(c);

        if (getMaster().getStyle().isFixed()) {
            c.translate(0, fixed.y);
            c.getGraphics().translate(0, fixed.y);
        }

        c.getGraphics().translate(-tx, -ty);
        c.translate(-tx, -ty);

        paintLayers(c, tx, ty);
    }

    private void repositionFixed(RenderingContext c, Box fixed, Rectangle rect) {
        fixed.x = 0;
        fixed.y = 0;
        fixed.absX = 0;
        fixed.absY = 0;

        fixed.setContainingBlock(new ViewportBox(rect));
        fixed.positionPositionedBox(c);
    }


    // TODO Inline borders and padding can slide an inline box around at render
    // TODO block.renderIndex = c.getNewRenderIndex();
    // TODO Don't do this every time 
    private void updateAbsoluteLocations(int tx, int ty) {
        updateAbsoluteLocationsHelper(getMaster(), tx, ty);
    }

    private void updateAbsoluteLocationsHelper(Box box, int x, int y) {
        box.absX = box.x + x;
        box.absY = box.y + y;

        int nextX = (int) box.absX + box.tx;
        int nextY = (int) box.absY + box.ty;

        if (box.getPersistentBFC() != null) {
            box.getPersistentBFC().getFloatManager().updateAbsoluteLocations(nextX, nextY);
        }
        
        /*for (Iterator i = box.getChildIterator(); i.hasNext(); ) {
            Box child = (Box)i.next();
            updateAbsoluteLocationsHelper(child, nextX, nextY);
        }*/
        for (int i = 0; i < box.getChildCount(); i++) {
            Box child = box.getChild(i);
            updateAbsoluteLocationsHelper(child, nextX, nextY);
        }
    }

    public void positionChildren(CssContext cssCtx) {
        if (children != null) {
            for (Iterator i = children.iterator(); i.hasNext();) {
                Layer child = (Layer) i.next();

                child.getMaster().positionPositionedBox(cssCtx);
            }
        }
    }

    private boolean containsFixedLayer() {
        if (children != null) {
            for (Iterator i = children.iterator(); i.hasNext();) {
                Layer child = (Layer) i.next();

                if (child.getMaster().getStyle().isFixed()) {
                    return true;
                }
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

    public List getChildren() {
        return children == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(children);
    }
}
