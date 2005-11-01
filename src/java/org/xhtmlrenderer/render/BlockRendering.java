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

import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.XRLog;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.LinkedList;
import java.util.logging.Level;

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
     * @param c   PARAM
     * @param box PARAM
     */
    public static void paintBlockContext(RenderingContext c, Box box) {
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
            //Point origin = c.getOriginOffset();
            //box.component.setLocation((int) origin.getX(), (int) origin.getY());
            if (!c.isInteractive()) {
                box.component.paint(c.getGraphics());
            }
        } else {
            int start = 0;
            int end = box.getChildCount() - 1;
            if (ALTERNATE_CLIP_DAMAGE) {
                if (box.getChildCount() > 10) {
                    int tstart = getFirstNonSkippedChild(c, box);
                    XRLog.render(Level.FINE, "first non skip = " + tstart);
                    if (tstart != -1) {
                        start = tstart;
                    }
                    int tend = getLastNonSkippedChild(c, box);
                    XRLog.render(Level.FINE, "last non skip = " + tend);
                    if (tend != -1) {
                        end = tend;
                    }
                }
            }
            for (int i = start; i <= end; i++) {
                LinkedList inlineBorders = null;
                Box child = (Box) box.getChild(i);
                if (!(child instanceof AnonymousBlockBox)) {
                    //save inline borders and reconstitute them
                    inlineBorders = (LinkedList) c.getInlineBorders().clone();
                    c.getInlineBorders().clear();
                }
                paintChild(c, child);
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

    public static final boolean ALTERNATE_CLIP_DAMAGE = false;

    public static void paintChild(RenderingContext c, Box box) {
        if (!canBeSkipped(c, box)) {
            BoxRendering.paint(c, box);
        }
    }

    public static boolean canBeSkipped(RenderingContext c, Box box) {
        if (box.getStyle() == null) {
            return true;
        }

        Shape clip = c.getGraphics().getClip();
        if (!box.isChildrenExceedBounds() &&
                Configuration.isTrue("xr.renderer.viewport-repaint", false) &&
                box.getState() != Box.CHILDREN_FLUX && clip != null &&
                !(box instanceof AnonymousBlockBox)) {

            RectPropertySet margin = box.getStyle().getMarginWidth(c);
            Rectangle bounds = new Rectangle(box.x + (int) margin.left(),
                    box.y + (int) margin.top(),
                    box.getWidth() - (int) margin.left() - (int) margin.right(),
                    box.height - (int) margin.top() - (int) margin.bottom());

            return !clip.intersects(bounds);
        }

        return false;
    }

    public static int getFirstNonSkippedChild(RenderingContext c, Box box) {
        Rectangle clip = c.getGraphics().getClipBounds();
        return doesIntersect(clip, box, 0, box.getChildCount());
    }

    public static int getLastNonSkippedChild(RenderingContext c, Box box) {
        Rectangle clip = c.getGraphics().getClipBounds();
        return doesIntersect2(clip, box, 0, box.getChildCount());
    }

    public static int doesIntersect(Rectangle clip, Box box, int start, int end) {
        // Uu.p("doesIntersect: " + clip + " " + start + " " + end);
        // end case
        if (start > end) {
            // Uu.p("start is greater than end. returning");
            return -1;
        }

        int mid = (end - start) / 2 + start;
        // Uu.p("mid = " + mid);
        if (mid >= box.getChildCount()) {
            return -1;
        }
        Box child = box.getChild(mid);
        // if clip intersects
        // Uu.p("testing: " + child);
        if (clip.intersects(new Rectangle(child.x, child.y, child.getWidth(), child.getHeight()))) {
            // Uu.p("it intersects. checking above:");
            if (mid > 0) {
                Box child2 = box.getChild(mid - 1);
                // Uu.p("second test: " + child2);
                if (!clip.intersects(new Rectangle(child2.x, child2.y, child2.getWidth(), child2.getHeight()))) {
                    // Uu.p("before doesn't intersect so this is the first");
                    // Uu.p("returning: " + mid);
                    return mid;
                } else {
                    // Uu.p("before intersects. Not first");
                }
            } else {
                // Uu.p("This is the first child so it must be correct");
                return mid;
            }
        }

        // if clip is above box
        if (clip.y <= child.y) {
            // Uu.p("clip is above");
            return doesIntersect(clip, box, start, mid - 1);
        } else {
            // if clip is below box
            // Uu.p("clip is below");
            return doesIntersect(clip, box, mid + 1, end);
        }
    }

    public static int doesIntersect2(Rectangle clip, Box box, int start, int end) {
        // Uu.p("doesIntersect2: " + clip + " " + start + " " + end);
        // end case
        if (start > end) {
            // Uu.p("start is greater than end. returning");
            return -1;
        }

        int mid = (end - start) / 2 + start;
        // Uu.p("mid = " + mid);
        if (mid >= box.getChildCount()) {
            return -1;
        }
        Box child = box.getChild(mid);
        // if clip intersects
        // Uu.p("testing: " + child);
        if (clip.intersects(new Rectangle(child.x, child.y, child.getWidth(), child.getHeight()))) {
            // Uu.p("it intersects. checking below:");
            if (mid + 1 < box.getChildCount()) {
                Box child2 = box.getChild(mid + 1);
                // Uu.p("second test: " + child2);
                if (!clip.intersects(new Rectangle(child2.x, child2.y, child2.getWidth(), child2.getHeight()))) {
                    // Uu.p("after doesn't intersect so this is the last");
                    // Uu.p("returning: " + mid);
                    return mid;
                } else {
                    // Uu.p("after intersects. Not last");
                }
            } else {
                // Uu.p("This is the last child so it must be correct");
                return mid;
            }
        }

        // if clip is above box
        if (clip.y + clip.height <= child.y) {
            // Uu.p("clip is above");
            return doesIntersect2(clip, box, start, mid - 1);
        } else {
            // if clip is below box
            // Uu.p("clip is below");
            return doesIntersect2(clip, box, mid + 1, end);
        }
    }

}

