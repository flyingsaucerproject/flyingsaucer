/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci, Torbjšrn Gannholm
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
import org.xhtmlrenderer.util.Configuration;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class BlockRendering {

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public static void paintBlockContext(Context c, Box box) {
        //if (box.getBlockFormattingContext() != null) c.pushBFC(box.getBlockFormattingContext());
        c.translate(box.x, box.y);
        if (box.getBlockFormattingContext() != null) c.pushBFC(box.getBlockFormattingContext());
        //Uu.p("Layout.paintChildren(): " + box);
        //Uu.p("child count = " + box.getChildCount());
        //XRLog.render(Level.WARNING, "using default renderer paintChildren for " + box.getClass().getName());
        //TODO: work out how images and other replaced content really should be handled
        /*if (box instanceof InlineBox && box.getChildCount() == 0) {
            InlineBox inline = (InlineBox) box;
            if (inline.sub_block != null) {
                ImageBox imgbox = (ImageBox) inline.sub_block;
                ImageRendering.paintComponent(c, imgbox);
            } else {
                //do nothing??
                XRLog.render("Got childless inline box with no sub_block in block rendering context");
            }
        } else*/
        if (box.component != null) {
            //HACK: the positions during layout are still not perfect, reset here - tobe 2005-01-07
            //TODO: fix the translates during layout to handle this directly instead
            Point origin = c.getOriginOffset();
            box.component.setLocation((int) origin.getX(), (int) origin.getY());
            //box.component.paint(c.getGraphics());
        } else
            for (int i = 0; i < box.getChildCount(); i++) {
                Box child = (Box) box.getChild(i);
                paintChild(c, child);
            }
        if (box.getBlockFormattingContext() != null) c.popBFC();
        c.translate(-box.x, -box.y);
        //if (box.getBlockFormattingContext() != null) c.popBFC();
    }

    public static void paintChild(Context c, Box box) {
        if (box.isChildrenExceedBounds()) {
            BoxRendering.paint(c, box);
            return;
        }

        if (Configuration.isTrue("xr.renderer.viewport-repaint", false)) {
            if (c.getGraphics().getClip() != null) {
                Rectangle2D oldclip = (Rectangle2D) c.getGraphics().getClip();
                Rectangle2D box_rect = new Rectangle(box.x, box.y, box.width, box.height);
                //TODO: handle floated content. HACK: descend into anonymous boxes, won't work for deeper nesting
                if (oldclip.intersects(box_rect) || (box instanceof AnonymousBlockBox)) {
                    BoxRendering.paint(c, box);
                }
                return;
            }
        }


        BoxRendering.paint(c, box);
    }

}
