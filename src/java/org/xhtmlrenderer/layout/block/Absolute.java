/*
 * {{{ header & license
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
 * }}}
 */
package org.xhtmlrenderer.layout.block;

import java.awt.Rectangle;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Boxing;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.LineBox;

/**
 * @author Torbjörn Gannholm
 */
public class Absolute {
    public static void generateAbsoluteBox(LayoutContext c, Content content, LineBox currentLine) {
        Rectangle oe = c.getExtents();// copy the extents for safety
        c.setExtents(new Rectangle(oe));
        Box box = Boxing.layout(c, content);
        box.setContainingBlock(c.getLayer().getMaster());
        box.setStaticParent(currentLine);
        
        // HACK
        if (box.getStyle().isFixed()) {
            currentLine.setFixedDescendant(true);
        }
        
        c.setExtents(oe);
    }

    public static void setupAbsolute(Box box, LayoutContext c) {
        //Uu.p("setting up an abs for box: " +box);
        CalculatedStyle style = c.getCurrentStyle();
        if (style.isIdent(CSSName.POSITION, IdentValue.ABSOLUTE)) {
            //Uu.p("is absolute pos");
            if (!style.isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
                box.right = (int) style.getFloatPropertyProportionalWidth(CSSName.RIGHT, c.getBlockFormattingContext().getWidth(), c);
                box.right_set = true;
            }
            if (!style.isIdent(CSSName.LEFT, IdentValue.AUTO)) {
                box.left = (int) style.getFloatPropertyProportionalWidth(CSSName.LEFT, c.getBlockFormattingContext().getWidth(), c);
                box.left_set = true;
            }

            if (!style.isIdent(CSSName.BOTTOM, IdentValue.AUTO)) {
                box.top = (int) style.getFloatPropertyProportionalHeight(CSSName.BOTTOM, c.getBlockFormattingContext().getHeight(), c);
                box.bottom_set = true;
            }
            if (!style.isIdent(CSSName.TOP, IdentValue.AUTO)) {
                box.top = (int) style.getFloatPropertyProportionalHeight(CSSName.TOP, c.getBlockFormattingContext().getHeight(), c);
                box.top_set = true;
                //Uu.p("set top to: " + box.top + " " + box.top_set);
            }
            
            // if right and left are set calculate width
            if (box.right_set && box.left_set) {
                //TODO: do this right
                box.contentWidth = box.contentWidth - box.right - box.left;
            }
        }
    }
}

