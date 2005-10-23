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

import org.xhtmlrenderer.layout.Context;

import java.awt.Graphics2D;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-okt-23
 * Time: 23:40:34
 * To change this template use File | Settings | File Templates.
 */
public class HierarchicalStackingContext extends StackingContext {
    private Box root;

    public void addBlock(Renderable b) {
        if (root == null) root = (Box) b;
    }

    public void addLine(Renderable b) {
    }

    //HACK: Context should not be used here
    public void render(Context c, Graphics2D g2, double top, double bottom) {
        if (root == null) return;
        paintBox(c, g2, (BlockBox) root);
        paintLines(c, g2, (BlockBox) root);
    }

    private void paintBox(Context c, Graphics2D g2, BlockBox box) {
        /*if (box.component != null) {
            //HACK: the positions during layout are still not perfect, reset here - tobe 2005-01-07
            //TODO: fix the translates during layout to handle this directly instead
            box.component.setLocation((int) box.absX, (int) box.absY);
            if (!c.isInteractive()) {
                box.component.paint(c.getGraphics());
            }
        } else*/ {
            int start = 0;
            int end = box.getChildCount() - 1;
            if (box.getChildCount() > 10) {
                int tstart = BlockRendering.getFirstNonSkippedChild(c, box);
                //XRLog.render(Level.FINE, "first non skip = " + tstart);
                if (tstart != -1) {
                    start = tstart;
                }
                int tend = BlockRendering.getLastNonSkippedChild(c, box);
                //XRLog.render(Level.FINE, "last non skip = " + tend);
                if (tend != -1) {
                    end = tend;
                }
            }
            for (int i = start; i <= end; i++) {
                Box child = (Box) box.getChild(i);
                if (!(child instanceof BlockBox)) continue;
                if (child instanceof AnonymousBlockBox) continue;
                BlockBox block = (BlockBox) child;
                block.render(c, g2);
            }
        }

    }

    private void paintLines(Context c, Graphics2D g2, BlockBox box) {
        int start = 0;
        int end = box.getChildCount() - 1;
        if (box.getChildCount() > 10) {
            int tstart = BlockRendering.getFirstNonSkippedChild(c, box);
            //XRLog.render(Level.FINE, "first non skip = " + tstart);
            if (tstart != -1) {
                start = tstart;
            }
            int tend = BlockRendering.getLastNonSkippedChild(c, box);
            //XRLog.render(Level.FINE, "last non skip = " + tend);
            if (tend != -1) {
                end = tend;
            }
        }
        for (int i = start; i <= end; i++) {
            LinkedList inlineBorders = null;
            Box child = (Box) box.getChild(i);
            if (child instanceof LineBox) {
                ((LineBox) child).render(c, g2);
            }
            if (!(child instanceof BlockBox)) continue;
            paintLines(c, g2, (BlockBox) child);
        }

    }
}
