package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.BoxLayout;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.render.InlineBox;

import java.awt.Font;

public class BoxBuilder {

    public static void prepBox(Context c, InlineBox box, InlineBox prev_align, Font font) {
        //Uu.p("box = " + box);
        //Uu.p("prev align = " + prev_align);

        CalculatedStyle style = c.getCurrentStyle();
        box.setStyle(style);


        // prepare the font, colors, border, etc
        box.setFont(font);
        BoxLayout.getBackgroundColor(c, box);
        //BoxLayout.getBorder(c, box);
        //Uu.p("set border on inline box: " + box);
        BoxLayout.getMargin(c, box);
        BoxLayout.getPadding(c, box);


        // =========== setup the color
        //box.color = style.getColor();





        // ============ set Xx ===========
        // shift left if starting a new line
        if (box.break_before) {
            box.x = 0;
        }

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

            // trim off leading space only if at start of new line
            //this should be done in the line-box rather - tobe 2004-12-14
            /*if (box.getSubstring().startsWith(WhitespaceStripper.SPACE)) {
                String whitespace = c.getCurrentStyle().getStringProperty(CSSName.WHITE_SPACE);
                if(whitespace.equals("normal") ||
                        whitespace.equals("nowrap") ||
                        whitespace.equals("pre-line"))
                box.setSubstring(box.start_index + 1, box.end_index);
            }*/
        }
        
        
        
        // cache the metrics
        box.text_bounds = FontUtil.getTextBounds(c, box);
        box.line_metrics = FontUtil.getLineMetrics(c, box);
        
        
        
        // =========== set y ===========
        // y is  relative to the line, so it's always 0
        box.y = 0;
        
        
        
        
        // =========== set width ==========
        
        /*
        if ( !LayoutUtil.isReplaced(c, node ) ) {
            if ( !LayoutUtil.isFloatedBlock( node, c ) ) {
                box.width = FontUtil.len( c, node, text.substring( start, end ), font );
            } else {
                box.width = bounds.width;
            }
        } else {
                */
        box.width = (int) box.text_bounds.getWidth();
        //box.width = FontUtil.len(c , box.node, box.getSubstring(), font);
        // Uu.p("width = " + box.width + " from '"+box.getSubstring() +"'");
        /*
            box.width = bounds.width;
        }
        //Uu.p("box.Xx = " + box.Xx);
        */
        
        
        
        
        // ============= set height
        
        /*
        if ( LayoutUtil.isReplaced(c, node ) ) {
            box.height = bounds.height;
        } else if ( LayoutUtil.isFloatedBlock( node, c ) ) {
            box.height = bounds.height;
        } else {
            */
        box.height = FontUtil.lineHeight(c, box);
        //box.height = FontUtil.lineHeight( c, box.node );
        /*
    }
    */

        
        
        //box.break_after = true;


        // =========== setup text decorations
        if (TextDecoration.isDecoratable(box)) {
            TextDecoration.setupTextDecoration(style, box);
        }
        
        
        // =========== setup vertical alignment
        VerticalAlign.setupVerticalAlign(c, style, box);
        
        // =========== setup relative
        //Relative.setupRelative( c, box );

        
        // ============= do special setup for first line
        
        // if first line then do extra setup        
        /* should already be handled if (c.isFirstLine()) {
            // if there is a first line firstLineStyle class
            if (firstLineStyle != null) {
                CalculatedStyle normal = c.css.getStyle(box.getRealElement());
                CalculatedStyle merged = c.css.getDerivedStyle(normal, firstLineStyle);
                LineBreaker.styleInlineBox(c, merged, box);
            }
        } */
        
        
        
        // adjust width based on borders and padding
        box.width += box.totalHorizontalPadding(c.getCurrentStyle());
    }

}
