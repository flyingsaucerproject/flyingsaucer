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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineBox;

public class VerticalAlignContext {
    private List measurements = new ArrayList();
    
    private int inlineTop;
    private boolean inlineTopSet = false;
    
    private int inlineBottom;
    private boolean inlineBottomSet = false;
    
    private int paintingTop;
    private boolean paintingTopSet = false;
    
    private int paintingBottom;
    private boolean paintingBottomSet = false;
    
    private List children = new ArrayList();
    
    private VerticalAlignContext parent = null;
    
    private void moveTrackedValues(int ty) {
        if (inlineTopSet) {
            inlineTop += ty;
        }
        
        if (inlineBottomSet) {
            inlineBottom += ty;
        }
        
        if (paintingTopSet) {
            paintingTop += ty;
        }
        
        if (paintingBottomSet) {
            paintingBottom += ty;
        }
    }
    
    public int getInlineBottom() {
        return inlineBottom;
    }

    public int getInlineTop() {
        return inlineTop;
    }

    public void updateInlineTop(int inlineTop) {
        if (! inlineTopSet || inlineTop < this.inlineTop) {
            this.inlineTop = inlineTop;
            inlineTopSet = true;
        }
    }
    
    public void updatePaintingTop(int paintingTop) {
        if (! paintingTopSet || paintingTop < this.paintingTop) {
            this.paintingTop = paintingTop;
            paintingTopSet = true;
        }
    }
    
    public void updateInlineBottom(int inlineBottom) {
        if (! inlineBottomSet || inlineBottom > this.inlineBottom) {
            this.inlineBottom = inlineBottom;
            inlineBottomSet = true;
        }
    }
    
    public void updatePaintingBottom(int paintingBottom) {
        if (! paintingBottomSet || paintingBottom > this.paintingBottom) {
            this.paintingBottom = paintingBottom;
            paintingBottomSet = true;
        }
    }    
    
    public int getLineBoxHeight() {
        return this.inlineBottom - this.inlineTop;
    }
    
    public void pushMeasurements(InlineBoxMeasurements measurements) {
        this.measurements.add(measurements);
        
        if (measurements.isContainsContent()) {
            updateInlineTop(measurements.getInlineTop());
            updateInlineBottom(measurements.getInlineBottom());
            
            updatePaintingTop(measurements.getPaintingTop());
            updatePaintingBottom(measurements.getPaintingBottom());
        }
    }
    
    public InlineBoxMeasurements getParentMeasurements() {
        return (InlineBoxMeasurements)this.measurements.get(this.measurements.size()-1);
    }
    
    public void popMeasurements() {
        this.measurements.remove(this.measurements.size()-1);
    }

    public int getPaintingBottom() {
        return paintingBottom;
    }

    public int getPaintingTop() {
        return paintingTop;
    }
    
    public VerticalAlignContext createChild(Box root) {
        VerticalAlignContext result = new VerticalAlignContext();
        
        VerticalAlignContext vaRoot = getRoot();
        
        result.setParent(vaRoot);
        
        InlineBoxMeasurements initial = (InlineBoxMeasurements)vaRoot.measurements.get(0);
        result.pushMeasurements(initial);
        
        if (vaRoot.children == null) {
            vaRoot.children = new ArrayList();
        }
        
        vaRoot.children.add(new ChildContextData(root, result));
        
        return result;
    }
    
    public List getChildren() {
        return children == null ? Collections.EMPTY_LIST : children;
    }

    public VerticalAlignContext getParent() {
        return parent;
    }

    public void setParent(VerticalAlignContext parent) {
        this.parent = parent;
    }
    
    private VerticalAlignContext getRoot() {
        VerticalAlignContext result = this;
        return result.getParent() != null ? result.getParent() : this;
    }
    
    private void merge(VerticalAlignContext context) {
        updateInlineBottom(context.getInlineBottom());
        updateInlineTop(context.getInlineTop());
        
        updatePaintingBottom(context.getPaintingBottom());
        updatePaintingTop(context.getPaintingTop());
    }
    
    public void alignChildren() {
        List children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            ChildContextData data = (ChildContextData)children.get(i);
            data.align();
            merge(data.getVerticalAlignContext());
        }
    }
    
    private static final class ChildContextData {
        private Box root;
        private VerticalAlignContext verticalAlignContext;
        
        
        public ChildContextData() {
        }
        
        public ChildContextData(Box root, VerticalAlignContext vaContext) {
            this.root = root;
            this.verticalAlignContext = vaContext;
        }
        
        public Box getRoot() {
            return root;
        }
        
        public void setRoot(Box root) {
            this.root = root;
        }
        
        public VerticalAlignContext getVerticalAlignContext() {
            return verticalAlignContext;
        }
        
        public void setVerticalAlignContext(VerticalAlignContext verticalAlignContext) {
            this.verticalAlignContext = verticalAlignContext;
        }
        
        private void moveContextContents(int ty) {
            moveInlineContents(this.root, ty);
        }
        
        private void moveInlineContents(Box box, int ty) {
            if (canBeMoved(box)) { 
                box.y += ty;
                if (box instanceof InlineBox) {
                    InlineBox iB = (InlineBox)box;
                    for (int i = 0; i < iB.getInlineChildCount(); i++) {
                        Object child = iB.getInlineChild(i);
                        if (child instanceof Box) {
                            moveInlineContents((Box)child, ty);
                        }
                    }
                }
            }
        }
        
        private boolean canBeMoved(Box box) {
            IdentValue vAlign = box.getStyle().getCalculatedStyle().getIdent(
                    CSSName.VERTICAL_ALIGN);
            return box == this.root ||
                ! (vAlign == IdentValue.TOP || vAlign == IdentValue.BOTTOM);
        }
        
        public void align() {
            IdentValue vAlign = this.root.getStyle().getCalculatedStyle().getIdent(
                    CSSName.VERTICAL_ALIGN);
            int delta = 0;
            if (vAlign == IdentValue.TOP) {
                delta = this.verticalAlignContext.getRoot().getInlineTop() -
                    this.verticalAlignContext.getInlineTop();
            } else if (vAlign == IdentValue.BOTTOM) {
                delta = this.verticalAlignContext.getRoot().getInlineBottom() -
                    this.verticalAlignContext.getInlineBottom();
            } else {
                throw new RuntimeException("internal error");
            }
            
            this.verticalAlignContext.moveTrackedValues(delta);
            moveContextContents(delta);
        }
    }    
}
