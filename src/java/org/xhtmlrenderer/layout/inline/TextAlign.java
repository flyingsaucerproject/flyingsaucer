package org.xhtmlrenderer.layout.inline;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.render.LineBox;

public class TextAlign {

    public static void adjustTextAlignment(Context c, LineBox line_to_save, Element containing_block, int width, int x, boolean last) {
        String text_align = c.css.getStyle(containing_block).getStringProperty(CSSName.TEXT_ALIGN);
        if (text_align == null) {
            return;
        }
        if (text_align.equals("right")) {
            line_to_save.x = x + width - line_to_save.width;
        }
        if (text_align.equals("center")) {
            line_to_save.x = x + (width - line_to_save.width) / 2;
        }
        if (TextAlignJustify.isJustified(c, containing_block)) {
            if (!last) {
                TextAlignJustify.justifyLine(c, line_to_save, containing_block, width);
            }
        }
    }

}

