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

import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.render.RenderingContext;

public class BoxCollector {
    public void collect(
            RenderingContext c, Shape clip, Box master, 
            List blockContent, List inlineContent) {
        collect(c, clip, master, master, blockContent, inlineContent);
    }
    
    private void collect(
            RenderingContext c, Shape clip, 
            Box master, Box container, 
            List blockContent, List inlineContent) {
        if (container instanceof LineBox) {
            if (container.intersects(c, clip)) {
                inlineContent.add(container);
            }
        } else {
            if (container.getLayer() == null || !(container instanceof BlockBox)) {
                if (container.intersects(c, clip)) {
                    blockContent.add(container);
                }
            }

            if (container.getLayer() == null || container == master) {
                for (int i = 0; i < container.getChildCount(); i++) {
                    Box child = container.getChild(i);
                    collect(c, clip, master, child, blockContent, inlineContent);
                }
            }
        }
    }
}
