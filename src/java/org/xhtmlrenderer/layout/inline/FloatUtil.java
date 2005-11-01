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

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.Boxing;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.awt.Point;
import java.awt.Rectangle;


/**
 * Description of the Class
 *
 * @author Torbjörn Gannholm
 */
public class FloatUtil {
    /**
     * the new way of doing floats
     *
     * @param c               PARAM
     * @param prev_line       PARAM
     * @param remaining_width PARAM
     * @return Returns
     */
    public static int adjustForTab(LayoutContext c, LineBox prev_line, int remaining_width) {
        if (prev_line.contentWidth == 0) {
//temporarily set width as an "easy" way of passing this as parameter
            prev_line.contentWidth = remaining_width;
        } else {
            Uu.p("warning. possible error. line already has width: " + prev_line);
        }
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        remaining_width -= bfc.getLeftFloatDistance(prev_line);
        remaining_width -= bfc.getRightFloatDistance(prev_line);
        //reset the line width to allow shrink wrap
        prev_line.contentWidth = 0;
        //Uu.p("adjusting the line by: " + remaining_width);
        return remaining_width;
    }


    // change this to use the existing block instead of a new one
    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content
     * @return Returns
     */
    public static BlockBox generateFloatedBlockInlineBox(LayoutContext c, Content content) {
        //Uu.p("generate floated block inline box: avail = " + avail);
        //Uu.p("generate floated block inline box");
        Rectangle oe = c.getExtents();// copy the extents for safety
        c.setExtents(new Rectangle(oe));

        //BlockBox block = (BlockBox)layout.layout( c, (Element)node );
        BlockBox floated_block = new BlockBox();
        floated_block.element = content.getElement();
        Boxing.layout(c, floated_block, content);
        
        //HACK: tobe 2004-12-22 - guessing here
        // calculate the float property
        if (c.getCurrentStyle().isIdent(CSSName.FLOAT, IdentValue.NONE)) {
            throw new XRRuntimeException("Invalid call to  generateFloatedBlockInlineBox(); where float: none ");
        }

        IdentValue ident = c.getCurrentStyle().getIdent(CSSName.FLOAT);
        if (ident == IdentValue.LEFT) {
            //floated_block.x = 0;
        }

        if (ident == IdentValue.RIGHT) {
            floated_block.x = oe.width - floated_block.getWidth();
        }
        //HACK: tobe 2004-12-22 end

        Point offset = c.getBlockFormattingContext().getOffset(floated_block);
        floated_block.y += offset.y;

        c.setExtents(oe);

        //TODO: check if floats should be affected by vertical alignment

        /*floated_block.break_after = false;
        if (floated_block.getWidth() > avail) {
            floated_block.break_before = true;
        }*/

        return floated_block;
    }

}

