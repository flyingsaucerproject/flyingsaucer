package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.layout.*;
import org.w3c.dom.*;
import org.xhtmlrenderer.css.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.css.constants.CSSName;

public class TextIndent {
    public static int doTextIndent( Context c, Element elem, int width, LineBox first_line ) {
        if ( c.css.hasProperty( elem, CSSName.TEXT_INDENT ) ) {
            float indent = c.css.getFloatProperty( elem, CSSName.TEXT_INDENT, width );
            width = width - (int)indent;
            first_line.x = first_line.x + (int)indent;
        }
        return width;
    }
}
