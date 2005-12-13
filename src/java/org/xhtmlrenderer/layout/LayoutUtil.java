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
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FloatedBlockBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.render.MarkerData;
import org.xhtmlrenderer.util.XRRuntimeException;

public class LayoutUtil {

    public static void generateAbsolute(LayoutContext c, Content content, LineBox currentLine) {
        Rectangle oe = c.getExtents();// copy the extents for safety
        c.setExtents(new Rectangle(oe));
        
        MarkerData markerData = c.getCurrentMarkerData();
        c.setCurrentMarkerData(null);
        
        Box box = Boxing.preLayout(c, content);
        box.setContainingBlock(c.getLayer().getMaster());
        box.setStaticEquivalent(currentLine);
        Boxing.realLayout(c, box, content);
        
        c.setCurrentMarkerData(markerData);
        
        c.setExtents(oe);
    }

    public static FloatedBlockBox generateFloated(
            LayoutContext c, Content content, int avail, LineBox curr_line, List pendingFloats) {
        Rectangle oe = c.getExtents();
        c.setExtents(new Rectangle(oe));
        
        MarkerData markerData = c.getCurrentMarkerData();
        c.setCurrentMarkerData(null);
    
        c.setFloatingY(curr_line.y + ((FloatedBlockContent)content).getMarginFromPrevious());
        FloatedBlockBox block = new FloatedBlockBox();
        block.setContainingBlock(curr_line.getParent());
        block.setContainingLayer(curr_line.getContainingLayer());
        block.element = content.getElement();
        Boxing.realLayout(c, block, content);
        
        if (! block.getStyle().isFloated()) {
            throw new XRRuntimeException("Invalid call to generateFloatedBlock(); where float: none ");
        }
        
        c.setCurrentMarkerData(markerData);
    
        c.setExtents(oe);
        
        if (pendingFloats.size() > 0 || block.getWidth() > avail) {
            pendingFloats.add(block);
            c.getBlockFormattingContext().getFloatManager().removeFloat(block);
            c.getLayer().removeFloat(block);
            block.setPending(true);
        }
        
        return block;
    }
}
