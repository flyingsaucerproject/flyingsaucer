package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.render.Box;

public class Relative {
    public static void setupRelative(Box box) {
        CalculatedStyle style = box.getContent().getStyle();
        String position = LayoutUtil.getPosition(style);
        if (position.equals("relative")) {
            if (style.hasProperty("right")) {
                box.left = -(int) style.getFloatPropertyRelative("right", 0);
            }
            if (style.hasProperty("bottom")) {
                box.top = -(int) style.getFloatPropertyRelative("bottom", 0);
            }
            if (style.hasProperty("top")) {
                box.top = (int) style.getFloatPropertyRelative("top", 0);
            }
            if (style.hasProperty("left")) {
                box.left = (int) style.getFloatPropertyRelative("left", 0);
            }
            box.relative = true;
        }
    }

}
