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
import org.xhtmlrenderer.util.XRRuntimeException;

public class LayoutUtil {

    public static BlockBox generateAbsolute(LayoutContext c, Content content, LineBox currentLine) {
        Rectangle oe = c.getExtents();// copy the extents for safety
        c.setExtents(new Rectangle(oe));
        
        MarkerData markerData = c.getCurrentMarkerData();
        c.setCurrentMarkerData(null);
        
        BlockBox box = Boxing.constructBox(c, content);
        box.setContainingBlock(c.getLayer().getMaster());
        box.setStaticEquivalent(currentLine);
        Boxing.layout(c, box, content);
        
        c.setCurrentMarkerData(markerData);
        
        c.setExtents(oe);
        
        return box;
    }

    public static FloatedBlockBox generateFloated(
            LayoutContext c, FloatedBlockContent content, int avail, 
            LineBox curr_line, List pendingFloats) {
        Rectangle oe = c.getExtents();
        c.setExtents(new Rectangle(oe));
        
        MarkerData markerData = c.getCurrentMarkerData();
        c.setCurrentMarkerData(null);
    
        FloatedBlockBox block = new FloatedBlockBox();
        block.setContainingBlock(curr_line.getParent());
        block.setContainingLayer(curr_line.getContainingLayer());
        block.y = curr_line.y + content.getMarginFromPrevious();
        block.element = content.getElement();
        Boxing.layout(c, block, content);
        
        if (! block.getStyle().isFloated()) {
            throw new XRRuntimeException("Invalid call to generateFloatedBlock(); where float: none ");
        }
        
        c.setCurrentMarkerData(markerData);
    
        c.setExtents(oe);
        
        if (pendingFloats.size() > 0 || block.getWidth() > avail) {
            if (pendingFloats.size() > 0) {
                block.y = 0;
            }
            pendingFloats.add(block);
            c.getBlockFormattingContext().getFloatManager().removeFloat(block);
            c.getLayer().removeFloat(block);
            block.setPending(true);
        }
        
        return block;
    }
}
