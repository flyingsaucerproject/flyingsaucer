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


    /**
     * Sets up all required code for a floated block
     *
     * @param c               PARAM
     * @param inline          PARAM
     * @param line            PARAM
     * @param full_width      PARAM
     * @param enclosing_block PARAM
     */
    /*not used public static void handleFloated(Context c, InlineBox inline, LineBox line,
                                     int full_width, Element enclosing_block) {

        BlockFormattingContext bfc = c.getBlockFormattingContext();
        //Uu.p("testing inline box: " + inline);
        // joshy: ??? i don't know what this is for. nesting?
        if (inline.getNode() == enclosing_block) {
            return;
        }
        // joshy: ??? i don't know what this is for. nesting?
        // we must make sure not to grab the float from the containing
        // block incase it is floated.
        if (inline.getNode().getNodeType() == inline.getNode().TEXT_NODE) {
            //Uu.p("is text node");
            if (inline.getNode().getParentNode() == enclosing_block) {
                //Uu.p("parent match: " + inline);
                return;
            }
        }

        
        // calculate the float property
        String float_val = c.css.getStyle(inline.getNode()).getStringProperty(CSSName.FLOAT);
        if (float_val == null) {
            float_val = "none";
        }
        if (float_val.equals("none")) {
            return;
        }
        
        // Uu.p("got a floated inline: " + inline);
        // Uu.p("sub = " + inline.sub_block);
        // Uu.p("adjusting it's position");
        
        // mark as floated
        inline.floated = true;

        if (float_val.equals("left")) {
            // move the inline to the left
            //inline.Xx = 0 - inline.width;
            // the inline's own width is already included in the left float distance
            // so you must subtract off the width twice
            inline.Xx = bfc.getLeftFloatDistance(line) - inline.width - inline.width;
            // add the float to the containing block
            //bfc.addLeftFloat(inline);
        }


        if (float_val.equals("right")) {
            // move the inline to the right
            // don't subtract off the inline's own width, because it's
            // already included in the right float distance
            // Uu.p("inline.Xx = " + inline.Xx);
            inline.Xx = full_width - bfc.getRightFloatDistance(line);
            // Uu.p("inline.Xx = " + inline.Xx);
            inline.Xx = bfc.getRightAddPoint(inline.sub_block).Xx;
            // Uu.p("inline.Xx = " + inline.Xx);
            //inline.sub_block.Xx = 0;
            //bfc.addRightFloat(inline);
        }
        
        // shrink the line width to account for the possible floats
        //Uu.p("accounting for the left float");
        line.width -= bfc.getLeftFloatDistance(line);
        //Uu.p("accounting for right float");
        line.width -= bfc.getRightFloatDistance(line);
        //line.width = line.width - inline.width;
    }*/

    
    // change this to use the existing block instead of a new one
    /**
     * Description of the Method
     *
     * @param c          PARAM
     * @param content
     * @param avail      PARAM
     * @param prev_align PARAM
     * @param font       PARAM
     * @return Returns
     */
    public static InlineBox generateFloatedBlockInlineBox(Context c, Content content, int avail, InlineBox prev_align, Font font) {
        // Uu.p("generate floated block inline box: avail = " + avail);
        /*
          joshy: change this to just modify the existing block instead of creating
          a  new one. is that possible?
        */
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
        // use the prev_align to calculate the Xx
        /*if (prev_align != null && !prev_align.break_after) {
            inline_block.x = prev_align.x + prev_align.width;
        } else {
            inline_block.x = 0;
        }*/

        //inline_block.y = 0;// it's relative to the line
        //inline_block.break_after = true;

        // do vertical alignment
        VerticalAlign.setupVerticalAlign(c, style, inline_block);
        // adjust width based on borders and padding
        //inline_block.width += inline_block.totalHorizontalPadding(c.getCurrentStyle());
        //box.height += box.totalVerticalPadding();

        inline_block.x = x;
        inline_block.y = y;
        // Uu.p("after newbox = " + inline_block);
        //box.sub_block = block;
        //block.setParent( box );
        inline_block.width = bounds.width;
        inline_block.height = bounds.height;
        inline_block.break_after = false;
        inline_block.floated = true;
        // Uu.p("width = " + inline_block.width);
        // Uu.p("avail = " + avail);
        if (inline_block.width > avail) {
            inline_block.break_before = true;
            //inline_block.Xx = 0;
        }
        
        //inline_block.floated = true;
        //inline_block.Xx = bfc.getLeftFloatDistance(line) - inline.width - inline.width;
        //inline_block.Xx = 0;
        //inline_block.y = 0;

        // Uu.p("final inline block = " + inline_block);
        return inline_block;
    }

}
