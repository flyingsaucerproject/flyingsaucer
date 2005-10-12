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

    public static void paintChild(Context c, Box box, boolean restyle) {
        if (!canBeSkipped(c, box)) {
            BoxRendering.paint(c, box, false, restyle);
        }
    }

    public static boolean canBeSkipped(Context c, Box box) {
        Shape clip = c.getGraphics().getClip();
        if (!box.isChildrenExceedBounds() &&
                Configuration.isTrue("xr.renderer.viewport-repaint", false) &&
                box.getState() != Box.CHILDREN_FLUX && clip != null) {
            Rectangle bounds = new Rectangle(box.x, box.y, box.getWidth(), box.height);

            return !clip.intersects(bounds);
        }

        return false;
    }

}

