package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.Boxing;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.InlineBlockBox;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.LineBox;

import java.awt.*;

public class FloatUtil {
    /* the new way of doing floats */
    public static int adjustForTab(Context c, LineBox prev_line, int remaining_width) {
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        remaining_width -= bfc.getLeftFloatDistance(prev_line);
        remaining_width -= bfc.getRightFloatDistance(prev_line);
        // Uu.p("adjusting the line by: " + remaining_width);
        return remaining_width;
    }


    // change this to use the existing block instead of a new one
    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content
     * @param avail   PARAM
     * @return Returns
     */
    public static InlineBox generateFloatedBlockInlineBox(Context c, Content content, int avail, LineBox curr_line) {
        // Uu.p("generate floated block inline box: avail = " + avail);
        //Uu.p("generate floated block inline box");
        Rectangle oe = c.getExtents(); // copy the extents for safety
        c.setExtents(new Rectangle(oe));
        

        //BlockBox block = (BlockBox)layout.layout( c, (Element)node );
        InlineBlockBox inline_block = new InlineBlockBox();
        inline_block.content = content;
        Boxing.layout(c, inline_block, content);

        //HACK: tobe 2004-12-22 - guessing here
        // calculate the float property
        String float_val = c.getCurrentStyle().getStringProperty(CSSName.FLOAT);
        if (float_val == null) {
            float_val = "none";
        }
        if (float_val.equals("none")) {
            throw new RuntimeException("Bad call of this method");
        }

        inline_block.floated = true;

        if (float_val.equals("left")) {
            inline_block.x = 0;
        }


        if (float_val.equals("right")) {
            inline_block.x = oe.width - inline_block.width;
        }
        //HACK: tobe 2004-12-22 end

        inline_block.y = curr_line.y;

        //Uu.p("got a block box from the sub layout: " + block);
        Rectangle bounds = new Rectangle(inline_block.x, inline_block.y,
                inline_block.width, inline_block.height);
        c.setExtents(oe);
        
        //InlineBox box = 
        // Uu.p("before newbox block = " + inline_block);
        int x = inline_block.x;
        int y = inline_block.y;
        //inline_block.width = bounds.width;
        //inline_block.height = bounds.height;
        CalculatedStyle style = c.getCurrentStyle();

        // do vertical alignment
        VerticalAlign.setupVerticalAlign(c, style, inline_block);

        inline_block.x = x;
        inline_block.y = y;
        inline_block.width = bounds.width;
        inline_block.height = bounds.height;
        inline_block.break_after = false;
        inline_block.floated = true;
        if (inline_block.width > avail) {
            inline_block.break_before = true;
        }

        return inline_block;
    }

}
