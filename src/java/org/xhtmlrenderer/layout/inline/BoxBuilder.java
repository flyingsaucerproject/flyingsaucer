package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.w3c.dom.*;
import java.awt.Font;

public class BoxBuilder {

    public static void prepBox(Context c, InlineBox box, InlineBox prev_align, Font font) {
        //u.p("box = " + box);
        //u.p("prev align = " + prev_align);

        Element elem = null;
        if(box.node instanceof Element) {
            elem = (Element)box.node;
        } else {
            elem = (Element)box.node.getParentNode();
        }
        CalculatedStyle style = c.css.getStyle(elem);
        box.setStyle(style);


        // prepare the font, colors, border, etc
        box.setFont(font);
        BoxLayout.getBackgroundColor(c,box);
        BoxLayout.getBorder(c,box);
        BoxLayout.getMargin(c,box);
        BoxLayout.getPadding(c,box);


        // =========== setup the color
        if ( box.node.getNodeType() == box.node.TEXT_NODE ) {
            box.color = c.css.getColor( (Element)box.node.getParentNode(), true );
        } else {
            box.color = c.css.getColor( (Element)box.node, true );
        }





        // ============ set x ===========
        // shift left if starting a new line
        if(box.break_before) {
            box.x = 0;
        }

        // use the prev_align to calculate the x if not at start of
        // new line
        if ( prev_align != null && 
            !prev_align.break_after && 
            !box.break_before
            ) {
            //u.p("prev align = " + prev_align);
            //u.p("floated = " + LayoutUtil.isFloatedBlock( prev_align.node, c ) );
            box.x = prev_align.x + prev_align.width;
        } else {
            box.x = 0;

            // trim off leading space only if at start of new line
            if(box.getSubstring().startsWith(WhitespaceStripper.SPACE)) {
                box.setSubstring(box.start_index+1,box.end_index);
            }
        }
        
        
        
        // cache the metrics
        box.text_bounds = FontUtil.getTextBounds(c,box);
        box.line_metrics = FontUtil.getLineMetrics(c,box);
        
        
        
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
        box.width = (int)box.text_bounds.getWidth();
        //box.width = FontUtil.len(c , box.node, box.getSubstring(), font);
        // u.p("width = " + box.width + " from '"+box.getSubstring() +"'");
        /*
            box.width = bounds.width;
        }
        //u.p("box.x = " + box.x);
        */
        
        
        
        
        // ============= set height
        
        /*
        if ( LayoutUtil.isReplaced(c, node ) ) {
            box.height = bounds.height;
        } else if ( LayoutUtil.isFloatedBlock( node, c ) ) {
            box.height = bounds.height;
        } else {
            */
        box.height = FontUtil.lineHeight( c, box);
        //box.height = FontUtil.lineHeight( c, box.node );
            /*
        }
        */

        
        
        //box.break_after = true;


        // =========== setup text decorations
        if(TextDecoration.isDecoratable(c,box.node)) {
            TextDecoration.setupTextDecoration( c, box.node, box );
        }
        
        
        // =========== setup vertical alignment
        VerticalAlign.setupVerticalAlign( c, style, box );
        
        // =========== setup relative
        //Relative.setupRelative( c, box );

        
        // ============= do special setup for first line
        
        // if first line then do extra setup        
        if(c.isFirstLine()) {
            // if there is a first line pseudo class
            CascadedStyle pseudo = c.css.getPseudoElementStyle(LineBreaker.getNearestBlockElement(box.node,c),"first-line");
            if(pseudo != null) {
                CalculatedStyle normal = c.css.getStyle(box.getRealElement());
                CalculatedStyle merged = new CalculatedStyle(normal,pseudo);
                LineBreaker.styleInlineBox(c,merged,box);
            }
        }
        
        
        
        // adjust width based on borders and padding
        box.width += box.totalHorizontalPadding();
    }

}
