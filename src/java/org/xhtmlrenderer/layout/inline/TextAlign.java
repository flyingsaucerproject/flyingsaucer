package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.layout.*;
import org.w3c.dom.*;
import java.util.*;
public class TextAlign {
 
    public static void adjustTextAlignment(Context c, LineBox line_to_save, Element containing_block, int width, int x, boolean last) {
        String text_align = c.css.getStringProperty( containing_block, "text-align", true );
        if(text_align == null) {
            return;
        }
        if ( text_align.equals( "right" ) ) {
            line_to_save.x = x + width - line_to_save.width;
        }
        if ( text_align.equals( "center" ) ) {
            line_to_save.x = x + ( width - line_to_save.width ) / 2;
        }
        if(TextAlignJustify.isJustified(c,containing_block)) {
            if(!last) {            
                TextAlignJustify.justifyLine(c,line_to_save,containing_block,width);
            }
        }
    }
    
}

