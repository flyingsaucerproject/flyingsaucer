package org.xhtmlrenderer.render;

import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.content.AnonymousBlockContent;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.XRLog;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;

public class BlockRendering extends DefaultRenderer {

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
        if (box instanceof InlineBox) {
            InlineBox inline = (InlineBox) box;
            if (inline.sub_block != null) {
                ImageBox imgbox = (ImageBox) inline.sub_block;
                ImageRendering.paintComponent(c, imgbox);
            } else {
                throw new RuntimeException("Unhandled sub_block");
            }
        } else
            for (int i = 0; i < box.getChildCount(); i++) {
                Box child = (Box) box.getChild(i);
                //Uu.p("child = " + child);
                Renderer renderer = null;
                if (child.content instanceof AnonymousBlockContent) {
                    renderer = c.getRenderingContext().getLayoutFactory().getAnonymousRenderer();
                } else {
                    if (child.content == null) {
                        XRLog.render(Level.WARNING, "null node of child: " + child + " of type " + child.getClass().getName());
                        renderer = new InlineRenderer();
                    } else {
                        /*if(isBlockLayedOut(box)) {
                            renderer = new BoxRenderer();
                        } else if(isInlineLayedOut(box)) {
                            renderer = new InlineRenderer();
                        } else*/
                        //TODO: find another way to work out the renderer
                        renderer = c.getRenderer(child.content.getElement());
                    }
                }
                paintChild(c, child, null);
            }
        if (box.getBlockFormattingContext() != null) c.popBFC();
        c.translate(-box.x, -box.y);
        //if (box.getBlockFormattingContext() != null) c.popBFC();
    }

    public static void paintChild(Context c, Box box, Renderer layout) {
        if (box.isChildrenExceedBounds()) {
            layout.paint(c, box);
            return;
        }

        if (Configuration.isTrue("xr.renderer.viewport-repaint", false)) {
            if (c.getGraphics().getClip() != null) {
                Rectangle2D oldclip = (Rectangle2D) c.getGraphics().getClip();
                Rectangle2D box_rect = new Rectangle(box.x, box.y, box.width, box.height);
                //TODO: handle floated content. HACK: descend into anonymous boxes, won't work for deeper nesting
                if (oldclip.intersects(box_rect) || (box instanceof AnonymousBlockBox)) {
                    BoxRendering.paint(c, box);
                    //layout.paint(c, box);
                }
                return;
            }
        }


        layout.paint(c, box);
    }

}
