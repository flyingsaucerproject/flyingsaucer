package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.render.LineBox;

public class TextAlign {

    public static void adjustTextAlignment(Context c, CalculatedStyle style, LineBox line_to_save, int width, int x, boolean last) {
        IdentValue textAlign = c.getCurrentStyle().getIdent(CSSName.TEXT_ALIGN);
        if ( textAlign == IdentValue.RIGHT ) {
            line_to_save.x = x + width - line_to_save.width;
        }
        if ( textAlign == IdentValue.CENTER) {
            line_to_save.x = x + (width - line_to_save.width) / 2;
        }
        if (TextAlignJustify.isJustified(style)) {
            if (!last) {
                TextAlignJustify.justifyLine(c, line_to_save, width);
            }
        }
    }

}

