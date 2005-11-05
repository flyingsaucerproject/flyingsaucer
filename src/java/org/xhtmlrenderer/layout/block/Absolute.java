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
}

