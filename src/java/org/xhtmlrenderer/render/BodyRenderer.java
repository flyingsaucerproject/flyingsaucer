package org.xhtmlrenderer.render;

//import java.awt.Image;

import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.GraphicsUtil;

import java.awt.Color;
import java.awt.Rectangle;

public class BodyRenderer extends InlineRenderer {

    private static final Color transparent = new Color(0, 0, 0, 0);

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paint(Context c, Box box) {
        //Uu.p("BoxRenderer.paint " + box);
        Box block = (Box) box;

        //set the current style
        c.initializeStyles(new EmptyStyle());

        // copy the bounds to we don't mess it up
        Rectangle oldBounds = new Rectangle(c.getExtents());


            paintNormal(c, block);

        // move the origin down to account for the contents plus the margin, borders, and padding
        oldBounds.y = oldBounds.y + block.height;
        c.setExtents(oldBounds);

        if (c.debugDrawBoxes()) {
            GraphicsUtil.drawBox(c.getGraphics(), block, Color.red);
        }
    }

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */

    public void paintBackground(Context c, Box box) {
        if (!Configuration.isTrue("xr.renderer.draw.backgrounds", true)) {
            return;
        }

        if (box.background_color != null) {
            if (!box.background_color.equals(transparent)) {
                c.getGraphics().setColor(box.background_color);
                c.getGraphics().fillRect(0, 0, c.canvas.getWidth(), c.canvas.getHeight());
            }
            super.paintBackground(c, box);
        }

    }

}

