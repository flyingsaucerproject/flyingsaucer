package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;

public class Relative {
    public static void setupRelative(Context c, Box box) {
        String position = LayoutUtil.getPosition(c, box);
        if (position.equals("relative")) {
            if (c.css.hasProperty(box.node, "right", false)) {
                box.left = -(int) c.css.getFloatProperty(box.node, "right", 0, false);
            }
            if (c.css.hasProperty(box.node, "bottom", false)) {
                box.top = -(int) c.css.getFloatProperty(box.node, "bottom", 0, false);
            }
            if (c.css.hasProperty(box.node, "top", false)) {
                box.top = (int) c.css.getFloatProperty(box.node, "top", 0, false);
            }
            if (c.css.hasProperty(box.node, "left", false)) {
                box.left = (int) c.css.getFloatProperty(box.node, "left", 0, false);
            }
            box.relative = true;
        }
    }
    
}
