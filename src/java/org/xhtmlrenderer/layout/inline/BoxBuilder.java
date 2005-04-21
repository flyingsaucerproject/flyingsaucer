/*
 * BoxBuilder.java
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

import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.InlineTextBox;


/**
 * Description of the Class
 *
 * @author Torbjörn Gannholm
 */
public class BoxBuilder {

    /**
     * Description of the Method
     *
     * @param c          PARAM
     * @param box        PARAM
     * @param prev_align PARAM
     */
    public static void prepBox(Context c, InlineTextBox box, InlineBox prev_align) {
        //Uu.p("box = " + box);
        //Uu.p("prev align = " + prev_align);

        // use the prev_align to calculate the Xx if not at start of
        // new line
        if (prev_align != null &&
                !prev_align.break_after &&
                !box.break_before
        ) {
            //Uu.p("prev align = " + prev_align);
            //Uu.p("floated = " + LayoutUtil.isFloatedBlock( prev_align.node, c ) );
            box.x = prev_align.x + prev_align.width;
        } else {
            box.x = 0;

        }

        // =========== set y ===========
        // y is  relative to the line, so it's always 0
        box.y = 0;

        box.width = (int) FontUtil.getTextBounds(c, box).getWidth();
        box.height = (int) FontUtil.getLineMetrics(c, box).getHeight();

        // =========== setup vertical alignment
        //now done later VerticalAlign.setupVerticalAlign(c, style, box);

        // adjust width based on borders and padding
        //can't do it here: box.width += box.totalHorizontalPadding( c.getCurrentStyle() );
    }

}

