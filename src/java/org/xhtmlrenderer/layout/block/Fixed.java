package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.render.Box;

import java.awt.*;

public class Fixed {
    public static void positionFixedChild(Context c, Box box) {
        if (LayoutUtil.isFixed(c.getCss().getCascadedStyle(box.element))) {
            Point origin = c.getOriginOffset();
            box.x = 0;
            box.y = 0;
            box.x -= origin.x;
            box.y -= origin.y;
        }
    }

    public static void setupFixed(Context c, Box box) {
        if (c.getCurrentStyle().getStringProperty(CSSName.POSITION).equals("fixed")) {
            box.fixed = true;
            box.setChildrenExceedBounds(true);
            Rectangle rect = c.getFixedRectangle();

            CalculatedStyle style = c.getCurrentStyle();
            if (style.hasProperty("top")) {
                box.top = (int) style.getFloatPropertyRelative("top", (float) rect.getHeight());
                box.top_set = true;
            }
            if (style.hasProperty("right")) {
                box.right = (int) style.getFloatPropertyRelative("right", (float) (rect.getWidth()));
                box.right_set = true;
            }
            if (style.hasProperty("bottom")) {
                box.bottom = (int) style.getFloatPropertyRelative("bottom", (float) (rect.getHeight()));
                box.bottom_set = true;
            }
            if (style.hasProperty("left")) {
                box.left = (int) style.getFloatPropertyRelative("left", (float) (rect.getWidth()));
                box.left_set = true;
            }

        }
    }
}
