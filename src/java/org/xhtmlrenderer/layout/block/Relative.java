package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;

public class Relative {
    /*public static void setupRelative(Box box, Context c) {
        CalculatedStyle style = c.getCurrentStyle();
        String position = style.getStringProperty(CSSName.POSITION);
        if (position.equals("relative")) {
            if (style.hasProperty("right")) {
                box.left = -(int) style.getFloatPropertyProportionalWidth("right", 0);
            }
            if (style.hasProperty("bottom")) {
                box.top = -(int) style.getFloatPropertyProportionalWidth("bottom", 0);
            }
            if (style.hasProperty("top")) {
                box.top = (int) style.getFloatPropertyProportionalWidth("top", 0);
            }
            if (style.hasProperty("left")) {
                box.left = (int) style.getFloatPropertyProportionalWidth("left", 0);
            }
            box.relative = true;
        }
    }*/

    public static void translateRelative(Context c) {
        CalculatedStyle style = c.getCurrentStyle();
        String position = style.getStringProperty(CSSName.POSITION);
        int top = 0;
        int left = 0;
        if (position.equals("relative")) {
            if (style.hasProperty(CSSName.RIGHT)) {
                left = -(int) style.getFloatPropertyProportionalWidth(CSSName.RIGHT, c.getBlockFormattingContext().getWidth());
            }
            if (style.hasProperty(CSSName.BOTTOM)) {
                top = -(int) style.getFloatPropertyProportionalHeight(CSSName.BOTTOM, c.getBlockFormattingContext().getHeight());
            }
            if (style.hasProperty(CSSName.TOP)) {
                top = (int) style.getFloatPropertyProportionalHeight(CSSName.TOP,  c.getBlockFormattingContext().getHeight());
            }
            if (style.hasProperty(CSSName.LEFT)) {
                left = (int) style.getFloatPropertyProportionalWidth(CSSName.LEFT,  c.getBlockFormattingContext().getWidth());
            }
            c.translate(left, top);
        }
    }

    public static void untranslateRelative(Context c) {
        CalculatedStyle style = c.getCurrentStyle();
        String position = style.getStringProperty(CSSName.POSITION);
        int top = 0;
        int left = 0;
        if (position.equals("relative")) {
            if (style.hasProperty(CSSName.RIGHT)) {
                left = -(int) style.getFloatPropertyProportionalWidth(CSSName.RIGHT, c.getBlockFormattingContext().getWidth());
            }
            if (style.hasProperty(CSSName.BOTTOM)) {
                top = -(int) style.getFloatPropertyProportionalHeight(CSSName.BOTTOM, c.getBlockFormattingContext().getHeight());
            }
            if (style.hasProperty(CSSName.TOP)) {
                top = (int) style.getFloatPropertyProportionalHeight(CSSName.TOP,  c.getBlockFormattingContext().getHeight());
            }
            if (style.hasProperty(CSSName.LEFT)) {
                left = (int) style.getFloatPropertyProportionalWidth(CSSName.LEFT,  c.getBlockFormattingContext().getWidth());
            }
            c.translate(-left, -top);
        }
    }

    public static boolean isRelative(Context c) {
        CalculatedStyle style = c.getCurrentStyle();
        String position = style.getStringProperty(CSSName.POSITION);
        if (position.equals("relative")) {
            return true;
        }
        return false;
    }

}
