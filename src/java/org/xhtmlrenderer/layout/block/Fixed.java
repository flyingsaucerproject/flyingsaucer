package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.render.Box;

import java.awt.Point;
import java.awt.Rectangle;

public class Fixed {
    public static void positionFixedChild(Context c, Box box) {
        if (LayoutUtil.isFixed(c.getCss().getCascadedStyle(box.element, false))) {//already restyled by ContentUtil
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
                box.top = (int) style.getFloatPropertyProportionalHeight("top", (float) (rect.getHeight()));
                box.top_set = true;
            }
            if (style.hasProperty("right")) {
                box.right = (int) style.getFloatPropertyProportionalWidth("right", (float) (rect.getWidth()));
                box.right_set = true;
            }
            if (style.hasProperty("bottom")) {
                box.bottom = (int) style.getFloatPropertyProportionalHeight("bottom", (float) (rect.getHeight()));
                box.bottom_set = true;
            }
            if (style.hasProperty("left")) {
                box.left = (int) style.getFloatPropertyProportionalWidth("left", (float) (rect.getWidth()));
                box.left_set = true;
            }

        }
    }
}
