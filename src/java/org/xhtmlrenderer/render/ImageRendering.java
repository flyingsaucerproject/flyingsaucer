package org.xhtmlrenderer.render;

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.layout.Context;

import java.awt.*;


public class ImageRendering {

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public static void paint(Context c, ImageBox box) {
        //Uu.p("box = " + box);
        Box block = box;
        // set the contents size
        //Rectangle contents = layout(c,elem);
        // get the border and padding
        Border border = BoxRendering.getBorder(c, block);
        Border padding = BoxRendering.getPadding(c, block);
        Border margin = BoxRendering.getMargin(c, block);
        // calculate the insets
        int top_inset = margin.top + border.top + padding.top;
        int left_inset = margin.left + border.left + padding.left;
        // shrink the bounds to be based on the contents
        c.getExtents().width = block.width;
        // do all of the painting
        BoxRendering.paintBackground(c, block);
        //Uu.p("insets = " + left_inset  + " " + top_inset);
        c.getGraphics().translate(left_inset, top_inset);
        //c.getExtents().translate(left_inset,top_inset);
        paintComponent(c, box);
        c.getGraphics().translate(-left_inset, -top_inset);
        //c.getExtents().translate(-left_inset,-top_inset);
        BoxRendering.paintBorder(c, block);
        // move the origin down now that we are done painting (should move this later)
        c.getExtents().y = c.getExtents().y + block.height;
    }


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public static void paintComponent(Context c, ImageBox box) {
        Image img = box.img;
        if (img != null) {
            c.getGraphics().drawImage(img, box.x, box.y, null);
        }
    }
}
