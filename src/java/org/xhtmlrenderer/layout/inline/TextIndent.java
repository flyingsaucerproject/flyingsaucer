package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.render.LineBox;

public class TextIndent {
    public static int doTextIndent(CalculatedStyle style, int width, LineBox first_line) {
        if (style.hasProperty(CSSName.TEXT_INDENT)) {
            float indent = style.getFloatPropertyProportionalWidth(CSSName.TEXT_INDENT, width);
            width = width - (int) indent;
            first_line.x = first_line.x + (int) indent;
        }
        return width;
    }
}
