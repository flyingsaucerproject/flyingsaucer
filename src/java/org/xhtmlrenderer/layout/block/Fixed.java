package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
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
        //if (c.getCurrentStyle().getStringProperty(CSSName.POSITION).equals("fixed")) {
        if ( c.getCurrentStyle().isIdent(CSSName.POSITION, IdentValue.FIXED)) {
            box.fixed = true;
            box.setChildrenExceedBounds(true);
            Rectangle rect = c.getFixedRectangle();

            CalculatedStyle style = c.getCurrentStyle();
            if (style.hasProperty(CSSName.TOP)) {
                box.top = (int) style.getFloatPropertyProportionalHeight(CSSName.TOP, (float) (rect.getHeight()));
                box.top_set = true;
            }
            if (style.hasProperty(CSSName.RIGHT)) {
                box.right = (int) style.getFloatPropertyProportionalWidth(CSSName.RIGHT, (float) (rect.getWidth()));
                box.right_set = true;
            }
            if (style.hasProperty(CSSName.BOTTOM)) {
                box.bottom = (int) style.getFloatPropertyProportionalHeight(CSSName.BOTTOM, (float) (rect.getHeight()));
                box.bottom_set = true;
            }
            if (style.hasProperty(CSSName.LEFT)) {
                box.left = (int) style.getFloatPropertyProportionalWidth(CSSName.LEFT, (float) (rect.getWidth()));
                box.left_set = true;
            }

        }
    }
}
