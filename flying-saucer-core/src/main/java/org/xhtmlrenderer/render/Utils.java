package org.xhtmlrenderer.render;

import org.xhtmlrenderer.css.style.CalculatedStyle;

class Utils {
    static void appendPositioningInfo(CalculatedStyle style, StringBuilder result) {
        if (style.isRelative()) {
            result.append("(relative) ");
        }
        if (style.isFixed()) {
            result.append("(fixed) ");
        }
        if (style.isAbsolute()) {
            result.append("(absolute) ");
        }
        if (style.isFloated()) {
            result.append("(floated) ");
        }
    }
}
