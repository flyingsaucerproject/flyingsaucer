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
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.Boxing;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.InlineBlockBox;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.awt.*;


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
    public static int adjustForTab(Context c, LineBox prev_line, int remaining_width) {
        if (prev_line.width == 0) {
//temporarily set width as an "easy" way of passing this as parameter
            prev_line.width = remaining_width;
        } else {
            Uu.p("warning. possible error. line already has width: " + prev_line);
        }
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        remaining_width -= bfc.getLeftFloatDistance(prev_line);
        remaining_width -= bfc.getRightFloatDistance(prev_line);
        //reset the line width to allow shrink wrap
        prev_line.width = 0;
        //Uu.p("adjusting the line by: " + remaining_width);
        return remaining_width;
    }


    // change this to use the existing block instead of a new one
    /**
     * Description of the Method
     *
     * @param c         PARAM
     * @param content
     * @param avail     PARAM
     * @param curr_line PARAM
     * @return Returns
     */
    public static InlineBox generateFloatedBlockInlineBox(Context c, Content content, int avail, LineBox curr_line) {
        //Uu.p("generate floated block inline box: avail = " + avail);
        //Uu.p("generate floated block inline box");
        Rectangle oe = c.getExtents();// copy the extents for safety
        c.setExtents(new Rectangle(oe));

        //BlockBox block = (BlockBox)layout.layout( c, (Element)node );
        InlineBlockBox inline_block = new InlineBlockBox();
        inline_block.element = content.getElement();
        Boxing.layout(c, inline_block, content);
        
        //HACK: tobe 2004-12-22 - guessing here
        // calculate the float property
        if (c.getCurrentStyle().isIdent(CSSName.FLOAT, IdentValue.NONE)) {
            throw new XRRuntimeException("Invalid call to  generateFloatedBlockInlineBox(); where float: none ");
        }

        inline_block.floated = true;

        IdentValue ident = c.getCurrentStyle().getIdent(CSSName.FLOAT);
        if (ident == IdentValue.LEFT) {
            //inline_block.x = 0;
        }

        if (ident == IdentValue.RIGHT) {
            inline_block.x = oe.width - inline_block.width;
        }
        //HACK: tobe 2004-12-22 end

        Point offset = c.getBlockFormattingContext().getOffset(inline_block);
        inline_block.y += offset.y;

        //Uu.p("got a box now = : " + inline_block);
        Rectangle bounds = new Rectangle(inline_block.x, inline_block.y,
                inline_block.width, inline_block.height);
        c.setExtents(oe);

        //InlineBox box =
        // Uu.p("before newbox block = " + inline_block);
        int x = inline_block.x;
        int y = inline_block.y;
        CalculatedStyle style = c.getCurrentStyle();

        //TODO: check if floats should be affected by vertical alignment

        inline_block.x = x;
        inline_block.y = y;
        inline_block.width = bounds.width;
        inline_block.height = bounds.height;
        inline_block.break_after = false;
        inline_block.floated = true;
        if (inline_block.width > avail) {
            inline_block.break_before = true;
        }

        return inline_block;
    }

}

