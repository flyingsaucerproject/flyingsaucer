/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjšrn Gannholm
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

import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.Configuration;

import java.awt.*;
import java.util.LinkedList;

/**
 * Description of the Class
 *
 * @author Joshua Marinacci
 * @author Torbjörn Gannholm
 */
public class BlockRendering {

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param box     PARAM
     * @param restyle
     */
    public static void paintBlockContext(Context c, Box box, boolean restyle) {
        //if (box.getPersistentBFC() != null) c.pushBFC(box.getPersistentBFC());
        c.translate(box.x, box.y);
        c.getGraphics().translate(box.x, box.y);
        if (box.getPersistentBFC() != null) {
            c.pushBFC(new BlockFormattingContext(box.getPersistentBFC()));
        }
        //XRLog.render(Level.WARNING, "using default renderer paintChildren for " + box.getClass().getPropertyName());
        //TODO: work out how images and other replaced content really should be handled
        if (box.component != null) {
            //HACK: the positions during layout are still not perfect, reset here - tobe 2005-01-07
            //TODO: fix the translates during layout to handle this directly instead
            Point origin = c.getOriginOffset();
            box.component.setLocation((int) origin.getX(), (int) origin.getY());
            if (!c.isInteractive()) {
                box.component.paint(c.getGraphics());
            }
        } else {
            for (int i = 0; i < box.getChildCount(); i++) {
                LinkedList inlineBorders = null;
                Box child = (Box) box.getChild(i);
                if (!(child instanceof AnonymousBlockBox)) {
                    //save inline borders and reconstitute them
                    inlineBorders = (LinkedList) c.getInlineBorders().clone();
                    c.getInlineBorders().clear();
                }
                paintChild(c, child, restyle);
                if (!(child instanceof AnonymousBlockBox)) {
                    c.getInlineBorders().addAll(inlineBorders);
                }
            }
        }
        if (box.getPersistentBFC() != null) {
            c.popBFC();
        }
        c.translate(-box.x, -box.y);
        c.getGraphics().translate(-box.x, -box.y);
        //if (box.getPersistentBFC() != null) c.popBFC();
    }

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param box     PARAM
     * @param restyle PARAM
     */
    public static void paintChild(Context c, Box box, boolean restyle) {
        if (box.isChildrenExceedBounds()) {
            //Uu.p("box children exceed: " + box);
            //BoxRendering.paint(c, box, false, restyle);
            //return;
        }

        if (Configuration.isTrue("xr.renderer.viewport-repaint", false)) {
            if (c.getGraphics().getClip() != null) {
                Shape oldclip = (Shape) c.getGraphics().getClip();

                Rectangle box_rect = new Rectangle(box.x, box.y, box.width, box.height);
                Rectangle old_clip = c.getGraphics().getClipBounds();
                // Uu.p("old clip = " + old_clip);
                // Uu.p("box rect = " + box_rect);
                box_rect = box_rect.union(old_clip);
                // Uu.p("union = " + box_rect);
                // c.getGraphics().setClip(box_rect);
                //TODO: handle floated content. HACK: descend into anonymous boxes, won't work for deeper nesting
                // Uu.p("test box in state: " + box.stateToString(box.getState()));
                
                // partially built boxes should be painted fully
                // boxes which intersect the cliprect should be painted fully
                // we should skip the rest
                if (box.getState() == 2) {
                    //Uu.p("calling paint for partial");
                    BoxRendering.paint(c, box, false, restyle);
                } else {
                    if (oldclip.intersects(box_rect) || (box instanceof AnonymousBlockBox)) {
                        BoxRendering.paint(c, box, false, restyle);
                    } else {
                        //Uu.p("no intersection. skipping");
                    }
                }
                c.getGraphics().setClip(old_clip);
                return;
            }
        }

        BoxRendering.paint(c, box, false, restyle);
    }

}

