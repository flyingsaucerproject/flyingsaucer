/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjörn Gannholm
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
import java.util.List;

import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.layout.content.FloatedBlockContent;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.FloatedBlockBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.render.MarkerData;
import org.xhtmlrenderer.render.Style;

public class LayoutUtil {

    public static BlockBox generateAbsolute(LayoutContext c, Content content, LineBox currentLine) {
        Rectangle oe = c.getExtents();// copy the extents for safety
        c.setExtents(new Rectangle(oe));
        
        MarkerData markerData = c.getCurrentMarkerData();
        c.setCurrentMarkerData(null);
        
        BlockBox box = Boxing.constructBox(c, content);
        box.setContainingBlock(c.getLayer().getMaster());
        box.setStaticEquivalent(currentLine);
        
        // If printing, don't layout until we know where its going
        if (! c.isPrint()) {
            if (! isAlternateFlow(c, content)) {
                Boxing.layout(c, box, content);
            } else {
                box = null;
            }
        } else {
            c.pushStyle(content.getStyle());
            box.setStyle(new Style(c.getCurrentStyle(), 
                    c.getLayer().getMaster().getContentWidth()));
            c.pushLayer(box);
            c.getLayer().setRequiresLayout(true);
            c.getLayer().setLayoutData(
                    new AbsoluteContentLayoutData(content, c.getCurrentStyle()));
            c.popLayer();
            c.popStyle();
        }
        
        c.setCurrentMarkerData(markerData);
        
        c.setExtents(oe);
        
        return box;
    }
    
    private static boolean isAlternateFlow(LayoutContext c, Content content) {
        c.pushStyle(content.getStyle());
        boolean result = c.getCurrentStyle().isAlternateFlow();
        c.popStyle();
        return result;
    }

    public static FloatLayoutResult generateFloated(
            final LayoutContext c, FloatedBlockContent content, int avail, 
            LineBox curr_line, List pendingFloats) {
        FloatLayoutResult result = new FloatLayoutResult();
        
        Rectangle oe = c.getExtents();
        c.setExtents(new Rectangle(oe));
        
        MarkerData markerData = c.getCurrentMarkerData();
        c.setCurrentMarkerData(null);
    
        FloatedBlockBox block = new FloatedBlockBox();
        block.setContainingBlock(curr_line.getParent());
        block.setContainingLayer(curr_line.getContainingLayer());
        
        if (pendingFloats != null) {
            block.y = curr_line.y + content.getMarginFromPrevious();
        } else {
            block.y = curr_line.y + curr_line.height;
        }
        
        block.calcInitialCanvasLocation(c);
        
        int initialY = block.y;
        
        block.element = content.getElement();
        
        Boxing.layout(c, block, content);
        c.getBlockFormattingContext().floatBox(c, (FloatedBlockBox) block);

        if (pendingFloats != null && 
                (pendingFloats.size() > 0 || block.getWidth() > avail)) {
            block.detach();
            result.setPending(true);
            result.setPendingContent(content);
        } else {
            if (c.isPrint()) {
                positionFloatOnPage(c, content, curr_line, block, initialY != block.y);
                c.getRootLayer().ensureHasPage(c, block);
            }
            result.setBlock(block);
        }
        
        c.setCurrentMarkerData(markerData);
    
        c.setExtents(oe);
        
        return result;
    }

    private static void positionFloatOnPage(
            final LayoutContext c, FloatedBlockContent content, 
            LineBox curr_line, FloatedBlockBox block, boolean movedVertically) {
        boolean clearedPage = false;
        int clearDelta = 0;
        
        if (block.getStyle().isForcePageBreakBefore() || 
                (block.getStyle().isAvoidPageBreakInside() && 
                        block.crossesPageBreak(c))) {
            clearDelta = block.moveToNextPage(c);
            clearedPage = true;
            block.calcCanvasLocation();
            block.detach();
            Boxing.layout(c, block, content);
            c.getBlockFormattingContext().floatBox(c, (FloatedBlockBox) block);
        }
        
        if ((movedVertically || 
                    (block.getStyle().isAvoidPageBreakInside() && block.crossesPageBreak(c))) && 
                ! block.getStyle().isForcePageBreakBefore()) {
            if (clearedPage) {
                block.y -= clearDelta;
                block.calcCanvasLocation();
            }
            block.detach();
            Boxing.layout(c, block, content);
            c.getBlockFormattingContext().floatBox(c, (FloatedBlockBox) block);
        }
    }
}
