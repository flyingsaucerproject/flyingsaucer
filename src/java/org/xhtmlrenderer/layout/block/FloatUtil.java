package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.render.Box;

public class FloatUtil {

    public static void preChildrenLayout(Context c, Box block) {
        //boolean set_bfc_float = false;
        if (LayoutUtil.isFloated(c, block.getNode())) {
            BlockFormattingContext bfc = new BlockFormattingContext(block);
            //set_bfc_float = true;
            bfc.setWidth(block.width);
            c.pushBFC(bfc);
        }
    }

    public static void postChildrenLayout(Context c, Box block) {
        if (LayoutUtil.isFloated(c, block.getNode())) {
            c.getBlockFormattingContext().doFinalAdjustments();
            c.popBFC();
        }
    }

    public static void setupFloat(Context c, Box box) {
        if (LayoutUtil.isFloated(c, box.getNode())) {
            // u.p("==== setup float ====");
            String float_val = c.css.getStyle(box.getNode()).getStringProperty(CSSName.FLOAT);
            if (float_val == null) {
                float_val = "none";
            }
            if (float_val.equals("none")) {
                return;
            }
            box.floated = true;
            if (float_val.equals("left")) {
                positionBoxLeft(c, box);
                c.getBlockFormattingContext().pushDownLeft(box);
                // u.p("final box = " + box);
                c.getBlockFormattingContext().addLeftFloat(box);
            }
            if (float_val.equals("right")) {
                positionBoxRight(c, box);
                c.getBlockFormattingContext().pushDownRight(box);
                // u.p("final box = " + box);
                c.getBlockFormattingContext().addRightFloat(box);
            }
            // u.p("box = " + box);
            // u.p("==== end setup ====");
        }
    }

    private static void positionBoxLeft(Context c, Box box) {
        // u.p("positionBoxLeft()");
        // u.p("calling the new float routine");
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        Box floater = bfc.getLeftFloatX(box);
        // u.p("floater = " + floater);
        // u.p("extents = " + c.getExtents());
        if (floater == null) {
            // u.p("no floater blocked. returning");
            box.x = 0;
            return;
        }


        box.x = floater.x + floater.width;

        if (box.x + box.width > c.getExtents().width &&
                box.width <= c.getExtents().width) {
            // u.p("not enough room!!!");
            // move the box to be below the last float and
            // try it again
            box.y = floater.y + floater.height;
            // u.p("trying again with box: " + box);
            positionBoxLeft(c, box);
            // u.p("final box = " + box);
        }
    }

    private static void positionBoxRight(Context c, Box box) {
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        Box floater = bfc.getRightFloatX(box);
        if (floater == null) {
            // u.p("floaters are null");
            // u.p("extents = " + c.getExtents().width);
            box.x = c.getExtents().width - box.width;
            return;
        }

        box.x = floater.x - box.width;

        if (box.x < 0 &&
                box.width <= c.getExtents().width) {
            // u.p("not enough room!!!");
            // move the box to be below the last float and
            // try it again
            box.y = floater.y + floater.height;
            positionBoxRight(c, box);
        }
        // u.p("final box = " + box);
    }

}
