package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.render.LineBox;

public class TextAlign {

    public static void adjustTextAlignment(Context c, CalculatedStyle style, LineBox line_to_save, int width, int x, boolean last) {
        String text_align = style.getStringProperty(CSSName.TEXT_ALIGN);
        if (text_align == null) {
            return;
        }
        if (text_align.equals("right")) {
            line_to_save.x = x + width - line_to_save.width;
        }
        if (text_align.equals("center")) {
            line_to_save.x = x + (width - line_to_save.width) / 2;
        }
        if (TextAlignJustify.isJustified(style)) {
            if (!last) {
                TextAlignJustify.justifyLine(c, line_to_save, width);
            }
        }
    }

}

