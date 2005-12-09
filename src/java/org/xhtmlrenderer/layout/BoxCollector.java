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

import java.awt.Shape;
import java.util.List;

import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.LineBox;

public class BoxCollector {
    public void collect(
            CssContext c, Shape clip, Layer layer, 
            List blockContent, List inlineContent) {
        if (layer.isInline()) {
            collectInlineLayer(c, clip, layer, blockContent, inlineContent);
        } else {
            collect(c, clip, layer, layer.getMaster(), blockContent, inlineContent);
        }
    }
    
    public boolean intersectsAny(
            CssContext c, Shape clip, Box master) {
        return intersectsAny(c, clip, master, master);
    }
    
    private void collectInlineLayer(
            CssContext c, Shape clip, Layer layer, List blockContent, List inlineContent) {
        InlineBox iB = (InlineBox)layer.getMaster();
        List content = iB.getElementWithContent();
        
        for (int i = 0; i < content.size(); i++) {
            Box b = (Box)content.get(i);
            
            if (b.intersects(c, clip)) {
                if (b instanceof InlineBox) {
                    inlineContent.add(b);
                } else { 
                    BlockBox bb = (BlockBox)b;
                    if (bb.isInline()) {
                        if (intersectsAny(c, clip, b)) {
                            inlineContent.add(b);
                        }
                    } else {
                        collect(c, clip, layer, bb, blockContent, inlineContent);
                    }
                }
            }
        }
    }
    
    public void collect(
            CssContext c, Shape clip, 
            Layer layer, Box container, 
            List blockContent, List inlineContent) {
        if (layer != container.getContainingLayer()) {
            return;
        }
        
        if (container instanceof LineBox) {
            if (container.intersects(c, clip)) {
                inlineContent.add(container);
                ((LineBox)container).addAllChildren(inlineContent, layer);
            }
        } else {
            if (container.getLayer() == null || !(container instanceof BlockBox)) {
                if (container.intersects(c, clip)) {
                    blockContent.add(container);
                }
            }

            if (container.getLayer() == null || container == layer.getMaster()) {
                for (int i = 0; i < container.getChildCount(); i++) {
                    Box child = container.getChild(i);
                    collect(c, clip, layer, child, blockContent, inlineContent);
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
