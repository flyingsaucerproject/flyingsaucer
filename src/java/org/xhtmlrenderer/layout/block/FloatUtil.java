package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.css.constants.CSSName;

public class FloatUtil {

    public static void setupFloat(Context c, Box box) {
        if (LayoutUtil.isFloated(c, box.node)) {
            // u.p("==== setup float ====");
            String float_val = c.css.getStringProperty(box.node, CSSName.FLOAT, false);
            if (float_val == null) {
                float_val = "none";
            }
            if (float_val.equals("none")) {
                return;
            }
            box.floated = true;
            if (float_val.equals("left")) {
                positionBoxLeft(c,box);
                c.getBlockFormattingContext().addLeftFloat(box);
            }
            if (float_val.equals("right")) {
                positionBoxRight(c,box);
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
        if(floater == null) {
            // u.p("no floater blocked. returning");
            box.x = 0;
            return;
        }
        
        
        box.x = floater.x + floater.width;
        
        if(box.x + box.width > c.getExtents().width &&
            box.width <= c.getExtents().width) {
            // u.p("not enough room!!!");
            // move the box to be below the last float and
            // try it again
            box.y = floater.y + floater.height;
            // u.p("trying again with box: " + box);
            positionBoxLeft(c,box);
            // u.p("final box = " + box);
        }
    }

    private static void positionBoxRight(Context c, Box box) {
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        Box floater = bfc.getRightFloatX(box);
        if(floater == null) {
            // u.p("floaters are null");
            // u.p("extents = " + c.getExtents().width);
            box.x = c.getExtents().width - box.width;
            return;
        }
        
        box.x = floater.x - box.width;
        
        if(box.x < 0 &&
            box.width <= c.getExtents().width) {
            // u.p("not enough room!!!");
            // move the box to be below the last float and
            // try it again
            box.y = floater.y + floater.height;
            positionBoxRight(c,box);
        }
        // u.p("final box = " + box);
    }

}
