package org.xhtmlrenderer.layout.block;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;

public class Relative {
    public static void translateRelative(Context c) {
        int top = 0;
        int left = 0;
        int topLeft[] = null;
        topLeft = extractLeftTopRelative(c);
        if ( topLeft != null ) {
            top = topLeft[0];
            left = topLeft[1];
            c.translate(left, top);
        }
    }

    public static void untranslateRelative(Context c) {
        int top = 0;
        int left = 0;
        int topLeft[] = null;
        topLeft = extractLeftTopRelative(c);
        if ( topLeft != null ) {
            top = topLeft[0];
            left = topLeft[1];
            c.translate(-left, -top);
        }
    }

    private static int[] extractLeftTopRelative(Context c) {
        CalculatedStyle style = c.getCurrentStyle();
        int top = 0;
        int left = 0;
        int topLeft[] = null;
        if ( style.isIdent(CSSName.POSITION, IdentValue.RELATIVE)) {
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
            topLeft = new int[]{top, left};
        }
        return topLeft;
    }

    public static boolean isRelative(Context c) {
        return c.getCurrentStyle().isIdent(CSSName.POSITION, IdentValue.RELATIVE);
    }
}
