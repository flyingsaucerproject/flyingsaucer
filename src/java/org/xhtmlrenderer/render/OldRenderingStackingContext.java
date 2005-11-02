/*
 * {{{ header & license
 * Copyright (c) 2005 Torbjšrn Gannholm
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
package org.xhtmlrenderer.render;


import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-okt-23
 * Time: 23:10:19
 * To change this template use File | Settings | File Templates.
 */
public class OldRenderingStackingContext extends StackingContext {
    private Box root;
    private List absolutes = new ArrayList();
    private List floats = new ArrayList();

    public void addBlock(Renderable b) {
        if (root == null) root = (Box) b;
    }

    public void addLine(Renderable b) {
    }

    //HACK: Context should not be used here
    public void render(RenderingContext c, Graphics2D g2, double top, double bottom) {
        if (root == null) return;
        BoxRendering.paint(c, root);
        synchronized (floats) {
            for (Iterator i = floats.iterator(); i.hasNext();) {
                BlockBox box = (BlockBox) i.next();
                g2.translate(box.absX - box.x, box.absY - box.y);
                BoxRendering.paint(c, box);
                g2.translate(box.x - box.absX, box.y - box.absY);
            }
        }
        synchronized (absolutes) {
            for (Iterator i = absolutes.iterator(); i.hasNext();) {
                BlockBox box = (BlockBox) i.next();
                BoxRendering.paintAbsoluteBox(c, box);
            }
        }
    }

    public void addAbsolute(Renderable absolute) {
        //HACK: for now
        synchronized (absolutes) {
            absolutes.add(absolute);
        }
    }

    public void addFloat(Renderable floater) {
//        //HACK: for now
//        synchronized (floats) {
//            floats.add(floater);
//        }
    }
}
