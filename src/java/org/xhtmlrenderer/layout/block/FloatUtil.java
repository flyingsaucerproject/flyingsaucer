package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.layout.content.ContentUtil;
import org.xhtmlrenderer.render.Box;

public class FloatUtil {

    public static void preChildrenLayout(Context c, Box block) {
        CalculatedStyle style = c.getCurrentStyle();
        //boolean set_bfc_float = false;
        if (LayoutUtil.isFloated(style)) {
            BlockFormattingContext bfc = new BlockFormattingContext(block);
            //set_bfc_float = true;
            bfc.setWidth(block.width);
            c.pushBFC(bfc);
        }
    }

    public static void postChildrenLayout(Context c, Box block) {
        CalculatedStyle style = c.getCurrentStyle();
        if (LayoutUtil.isFloated(style)) {
            c.getBlockFormattingContext().doFinalAdjustments();
            c.popBFC();
        }
    }

    public static void setupFloat(Context c, Box box) {
        CascadedStyle style = box.content.getStyle();
        if (ContentUtil.isFloated(style)) {
            // U.p("==== setup float ====");
            String float_val = style.propertyByName(CSSName.FLOAT).getValue().getCssText();
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
                // U.p("final box = " + box);
                c.getBlockFormattingContext().addLeftFloat(box);
            }
            if (float_val.equals("right")) {
                positionBoxRight(c, box);
                c.getBlockFormattingContext().pushDownRight(box);
                // U.p("final box = " + box);
                c.getBlockFormattingContext().addRightFloat(box);
            }
            // U.p("box = " + box);
            // U.p("==== end setup ====");
        }
    }

    private static void positionBoxLeft(Context c, Box box) {
        // U.p("positionBoxLeft()");
        // U.p("calling the new float routine");
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        Box floater = bfc.getLeftFloatX(box);
        // U.p("floater = " + floater);
        // U.p("extents = " + c.getExtents());
        if (floater == null) {
            // U.p("no floater blocked. returning");
            box.x = 0;
            return;
        }


        box.x = floater.x + floater.width;

        if (box.x + box.width > c.getExtents().width &&
                box.width <= c.getExtents().width) {
            // U.p("not enough room!!!");
            // move the box to be below the last float and
            // try it again
            box.y = floater.y + floater.height;
            // U.p("trying again with box: " + box);
            positionBoxLeft(c, box);
            // U.p("final box = " + box);
        }
    }

    private static void positionBoxRight(Context c, Box box) {
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        Box floater = bfc.getRightFloatX(box);
        if (floater == null) {
            // U.p("floaters are null");
            // U.p("extents = " + c.getExtents().width);
            box.x = c.getExtents().width - box.width;
            return;
        }

        box.x = floater.x - box.width;

        if (box.x < 0 &&
                box.width <= c.getExtents().width) {
            // U.p("not enough room!!!");
            // move the box to be below the last float and
            // try it again
            box.y = floater.y + floater.height;
            positionBoxRight(c, box);
        }
        // U.p("final box = " + box);
    }

}
