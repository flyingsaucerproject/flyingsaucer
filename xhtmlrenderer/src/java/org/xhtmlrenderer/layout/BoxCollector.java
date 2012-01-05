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

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.List;

import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.newtable.TableBox;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.render.RenderingContext;

/**
 * A class to collect boxes which intersect a given clip region.  If available,
 * aggregate bounds information will be used.  Block and inline content are
 * added to separate lists as they are painted in separate render phases.
 */
public class BoxCollector {
    public void collect(
            CssContext c, Shape clip, Layer layer, 
            List blockContent, List inlineContent, BoxRangeLists rangeLists) {
        if (layer.isInline()) {
            collectInlineLayer(c, clip, layer, blockContent, inlineContent, rangeLists);
        } else {
            collect(c, clip, layer, layer.getMaster(), blockContent, inlineContent, rangeLists);
        }
    }
    
    public boolean intersectsAny(
            CssContext c, Shape clip, Box master) {
        return intersectsAny(c, clip, master, master);
    }
    
    private void collectInlineLayer(
            CssContext c, Shape clip, Layer layer, 
            List blockContent, List inlineContent, BoxRangeLists rangeLists) {
        InlineLayoutBox iB = (InlineLayoutBox)layer.getMaster();
        List content = iB.getElementWithContent();
        
        for (int i = 0; i < content.size(); i++) {
            Box b = (Box)content.get(i);
            
            if (b.intersects(c, clip)) {
                if (b instanceof InlineLayoutBox) {
                    inlineContent.add(b);
                } else { 
                    BlockBox bb = (BlockBox)b;
                    if (bb.isInline()) {
                        if (intersectsAny(c, clip, b)) {
                            inlineContent.add(b);
                        }
                    } else {
                        collect(c, clip, layer, bb, blockContent, inlineContent, rangeLists);
                    }
                }
            }
        }
    }
    
    private boolean intersectsAggregateBounds(Shape clip, Box box) {
        if (clip == null) {
            return true;
        }
        PaintingInfo info = box.getPaintingInfo();
        if (info == null) {
            return false;
        }
        Rectangle bounds = info.getAggregateBounds();
        return clip.intersects(bounds);
    }
    
    public void collect(
            CssContext c, Shape clip, Layer layer, Box container, 
            List blockContent, List inlineContent, BoxRangeLists rangeLists) {
        if (layer != container.getContainingLayer()) {
            return;
        }
        
        boolean isBlock = container instanceof BlockBox;
        
        int blockStart = 0;
        int inlineStart = 0;
        int blockRangeStart = 0;
        int inlineRangeStart = 0;
        if (isBlock) {
            blockStart = blockContent.size();
            inlineStart = inlineContent.size();
            
            blockRangeStart = rangeLists.getBlock().size();
            inlineRangeStart = rangeLists.getInline().size();
        }
        
        if (container instanceof LineBox) {
            if (intersectsAggregateBounds(clip, container) ||
                    (container.getPaintingInfo() == null && container.intersects(c, clip))) {
                inlineContent.add(container);
                ((LineBox)container).addAllChildren(inlineContent, layer);
            }
        } else {
            boolean intersectsAggregateBounds = intersectsAggregateBounds(clip, container);
            if (container.getLayer() == null || !(container instanceof BlockBox)) {
                if (intersectsAggregateBounds || 
                        (container.getPaintingInfo() == null && container.intersects(c, clip))) {
                    blockContent.add(container);
                    if (container.getStyle().isTable() && c instanceof RenderingContext) {  // HACK
                        TableBox table = (TableBox)container;
                        if (table.hasContentLimitContainer()) {
                            table.updateHeaderFooterPosition((RenderingContext)c);
                        }
                    }
                }
            }

            if (container.getPaintingInfo() == null || intersectsAggregateBounds) {
                if (container.getLayer() == null || container == layer.getMaster()) {
                    for (int i = 0; i < container.getChildCount(); i++) {
                        Box child = container.getChild(i);
                        collect(c, clip, layer, child, blockContent, inlineContent, rangeLists);
                    }
                }
            }
        }
        
        saveRangeData(
                c, container, blockContent, inlineContent, 
                rangeLists, isBlock, blockStart, inlineStart,
                blockRangeStart, inlineRangeStart);
    }

    private void saveRangeData(
            CssContext c, Box container, List blockContent, List inlineContent,
            BoxRangeLists rangeLists, boolean isBlock, int blockStart, int inlineStart,
            int blockRangeStart, int inlineRangeStart) {
        if (isBlock && c instanceof RenderingContext) {
            BlockBox blockBox = (BlockBox)container;
            if (blockBox.isNeedsClipOnPaint((RenderingContext)c)) {
                int blockEnd = blockContent.size();
                if (blockStart != blockEnd) {
                    BoxRange range = new BoxRange(blockStart, blockEnd);
                    rangeLists.getBlock().add(blockRangeStart, new BoxRangeData(blockBox, range));
                }
                
                int inlineEnd = inlineContent.size();
                if (inlineStart != inlineEnd) {
                    BoxRange range = new BoxRange(inlineStart, inlineEnd);
                    rangeLists.getInline().add(inlineRangeStart, new BoxRangeData(blockBox, range));
                }
            }
        }
    }
    
    private boolean intersectsAny(
            CssContext c, Shape clip, 
            Box master, Box container) {
        if (container instanceof LineBox) {
            if (container.intersects(c, clip)) {
                return true;
            }
        } else {
            if (container.getLayer() == null || !(container instanceof BlockBox)) {
                if (container.intersects(c, clip)) {
                    return true;
                }
            }

            if (container.getLayer() == null || container == master) {
                for (int i = 0; i < container.getChildCount(); i++) {
                    Box child = container.getChild(i);
                    boolean possibleResult = intersectsAny(c, clip, master, child);
                    if (possibleResult) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }    
}
