package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.render.Box;

import java.awt.Point;

public class Fixed {
    public static void positionFixedChild(Context c, Box box) {
        if (LayoutUtil.isFixed(box.content.getStyle())) {
            Point origin = c.getOriginOffset();
            box.x = 0;
            box.y = 0;
            box.x -= origin.x;
            box.y -= origin.y;
        }
    }

    public static void setupFixed(Context c, Box box) {
        if (LayoutUtil.isFixed(box.content.getStyle())) {
            box.fixed = true;
            box.setChildrenExceedBounds(true);

            CalculatedStyle style = c.getCurrentStyle();
            if (style.hasProperty("top")) {
                box.top = (int) style.getFloatPropertyRelative("top", 0);
                box.top_set = true;
            }
            if (style.hasProperty("right")) {
                box.right = (int) style.getFloatPropertyRelative("right", 0);
                box.right_set = true;
            }
            if (style.hasProperty("bottom")) {
                box.bottom = (int) style.getFloatPropertyRelative("bottom", 0);
                box.bottom_set = true;
            }
            if (style.hasProperty("left")) {
                box.left = (int) style.getFloatPropertyRelative("left", 0);
                box.left_set = true;
            }

        }
    }
}
