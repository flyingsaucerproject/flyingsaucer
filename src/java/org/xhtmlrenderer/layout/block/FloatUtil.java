package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.css.constants.CSSName;

public class FloatUtil {

    public static void setupFloat(Context c, Box box) {
        if (LayoutUtil.isFloated(box.node, c)) {
            String float_val = c.css.getStringProperty(box.node, CSSName.FLOAT, false);
            if (float_val == null) {
                float_val = "none";
            }
            if (float_val.equals("none")) {
                return;
            }
            box.floated = true;
            if (float_val.equals("left")) {
                c.getBlockFormattingContext().addLeftFloat(box);
            }
            if (float_val.equals("right")) {
                c.getBlockFormattingContext().addRightFloat(box);
            }
        }
    }

}
