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
    }*/

    public static void translateRelative(Context c) {
        CalculatedStyle style = c.getCurrentStyle();
        String position = style.getStringProperty(CSSName.POSITION);
        int top = 0;
        int left = 0;
        if (position.equals("relative")) {
            if (style.hasProperty("right")) {
                left = -(int) style.getFloatPropertyRelative("right", 0);
            }
            if (style.hasProperty("bottom")) {
                top = -(int) style.getFloatPropertyRelative("bottom", 0);
            }
            if (style.hasProperty("top")) {
                top = (int) style.getFloatPropertyRelative("top", 0);
            }
            if (style.hasProperty("left")) {
                left = (int) style.getFloatPropertyRelative("left", 0);
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
            if (style.hasProperty("right")) {
                left = -(int) style.getFloatPropertyRelative("right", 0);
            }
            if (style.hasProperty("bottom")) {
                top = -(int) style.getFloatPropertyRelative("bottom", 0);
            }
            if (style.hasProperty("top")) {
                top = (int) style.getFloatPropertyRelative("top", 0);
            }
            if (style.hasProperty("left")) {
                left = (int) style.getFloatPropertyRelative("left", 0);
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
