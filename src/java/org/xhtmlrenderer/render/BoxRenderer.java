package org.xhtmlrenderer.render;

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.BoxLayout;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.content.ContentUtil;
import org.xhtmlrenderer.util.GraphicsUtil;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.Uu;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

public class BoxRenderer extends DefaultRenderer {

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
        if (!(box instanceof AnonymousBlockBox))
            c.pushStyle(block.content.getStyle());

        if (box.restyle) {
            restyle(c, box);
            box.restyle = false;
        }

        // copy the bounds to we don't mess it up
        Rectangle oldBounds = new Rectangle(c.getExtents());


        if (block.relative) {
            paintRelative(c, block);
        } else if (block.fixed) {
            paintFixed(c, block);
        } else {
            paintNormal(c, block);
        }

        //Uu.p("here it's : " + c.getListCounter());
        if (ContentUtil.isListItem(box.content.getStyle())) {
            paintListItem(c, box);
        }

        // move the origin down to account for the contents plus the margin, borders, and padding
        oldBounds.y = oldBounds.y + block.height;
        c.setExtents(oldBounds);

        //reset style
        if (!(box instanceof AnonymousBlockBox))
            c.popStyle();

        if (c.debugDrawBoxes()) {
            GraphicsUtil.drawBox(c.getGraphics(), block, Color.red);
        }
    }


    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param block PARAM
     */
    public void paintNormal(Context c, Box block) {
        paintBackground(c, block);

        c.translateInsets(block);
        paintComponent(c, block);
        paintChildren(c, block);
        c.untranslateInsets(block);

        paintBorder(c, block);
    }

    // adjustments for relative painting
    /**
     * Description of the Method
     *
     * @param ctx   PARAM
     * @param block PARAM
     */
    public void paintRelative(Context ctx, Box block) {
        ctx.translate(block.left, block.top);
        paintNormal(ctx, block);
        ctx.translate(-block.left, -block.top);
    }

    // adjustments for fixed painting
    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param block PARAM
     */
    public void paintFixed(Context c, Box block) {
        Rectangle rect = c.getFixedRectangle();
        //Uu.p("rect = " + rect);
        Graphics g = c.getGraphics();
        int xoff = -rect.x;
        int yoff = -rect.y;

        if (block.top_set) {
            yoff += block.top;
        }
        if (block.right_set) {
            xoff = -rect.x + rect.width - block.width - block.right;
        }
        if (block.left_set) {
            xoff = block.left;
        }
        if (block.bottom_set) {
            yoff = -rect.y + rect.height - block.height - block.bottom;
        }
        c.translate(xoff, yoff);
        paintNormal(c, block);
        c.translate(-xoff, -yoff);
    }


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintBackground(Context c, Box box) {
        Box block = box;
        // cache the background color
        getBackgroundColor(c, block);

        // get the css properties
        CalculatedStyle style = c.getCurrentStyle();
        String back_image = style.getStringProperty("background-image");
        block.repeat = style.getStringProperty("background-repeat");
        block.attachment = style.getStringProperty("background-attachment");
        // handle image positioning issues
        // need to update this to support vert and horz, not just vert
        if (style.hasProperty("background-position")) {
            Point pt = style.getBackgroundPosition();
            block.background_position_horizontal = (int) pt.getX();
            block.background_position_vertical = (int) pt.getY();
        }

        // load the background image
        block.background_image = null;
        if (back_image != null && !"none".equals(back_image)) {
            try {
                block.background_image = ImageUtil.loadImage(c, back_image);
            } catch (Exception ex) {
                ex.printStackTrace();
                Uu.p(ex);
            }
            /*
             * ImageIcon icon = new ImageIcon(back_image);
             * if(icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
             * block.background_image = icon.getImage();
             * }
             */
        }

        // actually paint the background
        BackgroundPainter.paint(c, block);
    }

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintChildren(Context c, Box box) {
        c.translate(box.x, box.y);
        super.paintChildren(c, box);
        c.translate(-box.x, -box.y);
    }


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintBorder(Context c, Box box) {
        Box block = box;
        // get the border parts

        // paint the border
        BorderPainter bp = new BorderPainter();

        // adjust to a fixed height, if necessary
        //if (!block.auto_height) {
        //bnds.y = block.height - block.margin.top - block.margin.bottom;
        //}

        bp.paint(c, block);
    }

    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintListItem(Context c, Box box) {
        ListItemPainter.paint(c, box);
    }

    public Border getBorder(Context c, Box box) {
        return BoxLayout.getBorder(c, box);
    }

    public Border getPadding(Context c, Box box) {
        return BoxLayout.getPadding(c, box);
    }

    public Color getBackgroundColor(Context c, Box box) {
        return BoxLayout.getBackgroundColor(c, box);
    }

    public Border getMargin(Context c, Box box) {
        return BoxLayout.getMargin(c, box);
    }


}
