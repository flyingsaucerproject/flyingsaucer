package org.xhtmlrenderer.render;

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.ImageLayout;

import java.awt.*;


public class ImageRenderer extends BoxRenderer {

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paint(Context c, Box box) {
        //u.p("box = " + box);
        Box block = box;
        // set the contents size
        //Rectangle contents = layout(c,elem);
        // get the border and padding
        Border border = getBorder(c, block);
        Border padding = getPadding(c, block);
        Border margin = getMargin(c, block);
        // calculate the insets
        int top_inset = margin.top + border.top + padding.top;
        int left_inset = margin.left + border.left + padding.left;
        // shrink the bounds to be based on the contents
        c.getExtents().width = block.width;
        // do all of the painting
        paintBackground(c, block);
        //u.p("insets = " + left_inset  + " " + top_inset);
        c.getGraphics().translate(left_inset, top_inset);
        //c.getExtents().translate(left_inset,top_inset);
        paintComponent(c, block);
        c.getGraphics().translate(-left_inset, -top_inset);
        //c.getExtents().translate(-left_inset,-top_inset);
        paintBorder(c, block);
        // move the origin down now that we are done painting (should move this later)
        c.getExtents().y = c.getExtents().y + block.height;
    }


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintComponent(Context c, Box box) {
        Image img = ImageLayout.getImage(c, box.getNode());
        if (img != null) {
            c.getGraphics().drawImage(img, box.x, box.y, null);
        }
    }
}
