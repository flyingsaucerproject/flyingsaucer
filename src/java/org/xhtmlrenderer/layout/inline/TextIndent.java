package org.xhtmlrenderer.layout.inline;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.render.LineBox;

public class TextIndent {
    public static int doTextIndent(Context c, Element elem, int width, LineBox first_line) {
        if (c.css.getStyle(elem).hasProperty(CSSName.TEXT_INDENT)) {
            float indent = c.css.getStyle(elem).getFloatPropertyRelative(CSSName.TEXT_INDENT, width);
            width = width - (int) indent;
            first_line.x = first_line.x + (int) indent;
        }
        return width;
    }
}
