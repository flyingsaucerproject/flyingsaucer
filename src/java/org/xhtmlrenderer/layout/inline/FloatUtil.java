/*
 * FloatUtil.java
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.layout.inline;

import java.awt.Rectangle;
import java.util.List;

import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.Boxing;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.FloatedBlockBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRRuntimeException;


/**
 * Description of the Class
 *
 * @author Torbjörn Gannholm
 */
public class FloatUtil {

    public static int adjustForTab(LayoutContext c, LineBox prev_line, int remaining_width) {
        if (prev_line.contentWidth == 0) {
//temporarily set width as an "easy" way of passing this as parameter
            prev_line.contentWidth = remaining_width;
        } else {
            Uu.p("warning. possible error. line already has width: " + prev_line);
        }
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        remaining_width -= bfc.getLeftFloatDistance(c, prev_line);
        remaining_width -= bfc.getRightFloatDistance(c, prev_line);
        //reset the line width to allow shrink wrap
        prev_line.contentWidth = 0;
        //Uu.p("adjusting the line by: " + remaining_width);
        return remaining_width;
    }

    public static FloatedBlockBox generateFloatedBlock(
            LayoutContext c, Content content, int avail, LineBox curr_line, List pendingFloats) {
        //Uu.p("generate floated block inline box: avail = " + avail);
        //Uu.p("generate floated block inline box");
        Rectangle oe = c.getExtents();// copy the extents for safety
        c.setExtents(new Rectangle(oe));

        c.setFloatingY(curr_line.y);
        FloatedBlockBox block = new FloatedBlockBox();
        block.setContainingBlock(curr_line.getParent());
        block.element = content.getElement();
        Boxing.realLayout(c, block, content);
        
        if (! block.getStyle().isFloated()) {
            throw new XRRuntimeException("Invalid call to generateFloatedBlock(); where float: none ");
        }

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

