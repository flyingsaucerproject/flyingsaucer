package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.render.Box;

public class Relative {
    public static void setupRelative(Context c, Box box) {
        String position = LayoutUtil.getPosition(c, box);
        if (position.equals("relative")) {
            if (c.css.getStyle(box.getNode()).hasProperty("right")) {
                box.left = -(int) c.css.getStyle(box.getNode()).getFloatPropertyRelative("right", 0);
            }
            if (c.css.getStyle(box.getNode()).hasProperty("bottom")) {
                box.top = -(int) c.css.getStyle(box.getNode()).getFloatPropertyRelative("bottom", 0);
            }
            if (c.css.getStyle(box.getNode()).hasProperty("top")) {
                box.top = (int) c.css.getStyle(box.getNode()).getFloatPropertyRelative("top", 0);
            }
            if (c.css.getStyle(box.getNode()).hasProperty("left")) {
                box.left = (int) c.css.getStyle(box.getNode()).getFloatPropertyRelative("left", 0);
            }
            box.relative = true;
        }
    }

}
