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
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-okt-23
 * Time: 22:53:47
 * To change this template use File | Settings | File Templates.
 */
public class FlattenedStackingContext extends StackingContext {

    VerticalExtentList blockVerticalExtentList = new VerticalExtentList();
    VerticalExtentList inlineVerticalExtentList = new VerticalExtentList();

    public void addBlock(Renderable b) {
        blockVerticalExtentList.addChild(b);
    }

    public void addLine(Renderable b) {
        inlineVerticalExtentList.addChild(b);
    }

    //HACK: Context should not be used here
    public void render(RenderingContext c, Graphics2D g2, double top, double bottom) {
        Comparator indexer = new Comparator() {

            public int compare(Object o, Object o1) {
                Renderable r = (Renderable) o;
                Renderable r1 = (Renderable) o1;
                if (r.getIndex() < r1.getIndex())
                    return -1;
                else if (r.getIndex() > r1.getIndex())
                    return 1;
                else
                    return 0;
            }
        };
        //render in-flow blocks
        TreeSet blocks = new TreeSet(indexer);
        blockVerticalExtentList.getIntersectingChildSet(blocks, top, bottom);
        for (Iterator i = blocks.iterator(); i.hasNext();) {
            ((Renderable) i.next()).render(c, g2);
        }
        //render in-flow inlines
        TreeSet inlines = new TreeSet(indexer);
        inlineVerticalExtentList.getIntersectingChildSet(inlines, top, bottom);
        for (Iterator i = inlines.iterator(); i.hasNext();) {
            ((Renderable) i.next()).render(c, g2);
        }
    }

    public void addAbsolute(Renderable absolute) {

    }

    public void addFloat(Renderable floater) {

    }
}
