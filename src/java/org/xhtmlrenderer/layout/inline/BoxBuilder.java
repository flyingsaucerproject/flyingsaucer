package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.InlineTextBox;

import java.awt.*;

public class BoxBuilder {

    public static void prepBox(Context c, InlineTextBox box, InlineBox prev_align, Font font) {
        //Uu.p("box = " + box);
        //Uu.p("prev align = " + prev_align);

        CalculatedStyle style = c.getCurrentStyle();

        // use the prev_align to calculate the Xx if not at start of
        // new line
        if (prev_align != null &&
                !prev_align.break_after &&
                !box.break_before
        ) {
            //Uu.p("prev align = " + prev_align);
            //Uu.p("floated = " + LayoutUtil.isFloatedBlock( prev_align.node, c ) );
            box.x = prev_align.x + prev_align.width;
        } else {
            box.x = 0;

        }
        
        
        
        // cache the metrics
        box.text_bounds = FontUtil.getTextBounds(c, box);
        box.line_metrics = FontUtil.getLineMetrics(c, box);
        
        
        
        // =========== set y ===========
        // y is  relative to the line, so it's always 0
        box.y = 0;


        box.width = (int) box.text_bounds.getWidth();
        box.height = FontUtil.lineHeight(c, box);

        // =========== setup text decorations
        /*leave it to rendering if (TextDecoration.isDecoratable(box)) {
            TextDecoration.setupTextDecoration(c, box);
        }*/
        
        
        // =========== setup vertical alignment
        VerticalAlign.setupVerticalAlign(c, style, box);
        
        // adjust width based on borders and padding
        box.width += box.totalHorizontalPadding(c.getCurrentStyle());
    }

}
