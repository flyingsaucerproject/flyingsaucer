package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.util.*;
import org.w3c.dom.*;
import java.util.*;
import java.awt.*;
import org.xhtmlrenderer.css.constants.CSSName;

public class FloatUtil {
     /* the new way of doing floats */
     public static int adjustForTab( Context c, LineBox prev_line, int remaining_width ) {
         BlockFormattingContext bfc = c.getBlockFormattingContext();
         remaining_width -= bfc.getLeftFloatDistance(prev_line);
         remaining_width -= bfc.getRightFloatDistance(prev_line);
         return remaining_width;
     }
     
     
     
    /**
     * Sets up all required code for a floated block
     *
     * @param c                PARAM
     * @param inline           PARAM
     * @param line             PARAM
     * @param full_width       PARAM
     * @param enclosing_block  PARAM
     */
    public static void handleFloated( Context c, InlineBox inline, LineBox line,
                                      int full_width, Element enclosing_block ) {
                                          
        /* NOTE: this code may be dead now because there's no such thing as an inline floated box.
        All floats must become block boxes. JMM 11/17 */
        /* NOTE: nevermind. it's the other one. JMM 11/17 */
        
        BlockFormattingContext bfc = c.getBlockFormattingContext();
        //u.p("testing inline box: " + inline);
        // joshy: ??? i don't know what this is for. nesting?
        if ( inline.node == enclosing_block ) {
            return;
        }
        // joshy: ??? i don't know what this is for. nesting?
        // we must make sure not to grab the float from the containing
        // block incase it is floated.
        if ( inline.node.getNodeType() == inline.node.TEXT_NODE ) {
            //u.p("is text node");
            if ( inline.node.getParentNode() == enclosing_block ) {
                //u.p("parent match: " + inline);
                return;
            }
        }

        
        // calculate the float property
        String float_val = c.css.getStringProperty( inline.node, CSSName.FLOAT, false );
        if ( float_val == null ) {
            float_val = "none";
        }
        if ( float_val.equals( "none" ) ) {
            return;
        }
        
        // u.p("got a floated inline: " + inline);
        // u.p("sub = " + inline.sub_block);
        // u.p("adjusting it's position");
        
        // mark as floated
        inline.floated = true;
        
        if ( float_val.equals( "left" ) ) {
            // move the inline to the left
            //inline.x = 0 - inline.width;
            // the inline's own width is already included in the left float distance
            // so you must subtract off the width twice
            inline.x = bfc.getLeftFloatDistance(line) - inline.width - inline.width;
            // add the float to the containing block
            //bfc.addLeftFloat(inline);
        }
        
        
        
        if( float_val.equals( "right" ) ) {
            // move the inline to the right
            // don't subtract off the inline's own width, because it's
            // already included in the right float distance
            // u.p("inline.x = " + inline.x);
            inline.x = full_width - bfc.getRightFloatDistance(line);
            // u.p("inline.x = " + inline.x);
            inline.x = bfc.getRightAddPoint(inline.sub_block).x;
            // u.p("inline.x = " + inline.x);
            //inline.sub_block.x = 0;
            //bfc.addRightFloat(inline);
        }
        
        // shrink the line width to account for the possible floats
        //u.p("accounting for the left float");
        line.width -= bfc.getLeftFloatDistance(line);
        //u.p("accounting for right float");
        line.width -= bfc.getRightFloatDistance(line);
        //line.width = line.width - inline.width;
    }

    
    // change this to use the existing block instead of a new one
    /**
     * Description of the Method
     *
     * @param c           PARAM
     * @param node        PARAM
     * @param avail       PARAM
     * @param prev        PARAM
     * @param text        PARAM
     * @param prev_align  PARAM
     * @param font        PARAM
     * @return            Returns
     */
    public static InlineBox generateFloatedBlockInlineBox( Context c, Node node, int avail, InlineBox prev, String text, InlineBox prev_align, Font font ) {
        /*
          joshy: change this to just modify the existing block instead of creating
          a  new one. is that possible?
        */
        //u.p("generate floated block inline box");
        Layout layout = c.getLayout( node ); //
        Rectangle oe = c.getExtents(); // copy the extents for safety
        c.setExtents( new Rectangle( oe ) );
        BlockBox block = (BlockBox)layout.layout( c, (Element)node );
        //u.p("got a block box from the sub layout: " + block);
        Rectangle bounds = new Rectangle( block.x, block.y, block.width, block.height );
        c.setExtents( oe );
        
        InlineBox box = LineBreaker.newBox( c, node, 0, 0, prev, text, bounds, prev_align, font );
        box.sub_block = block;
        block.setParent( box );
        box.width = bounds.width;
        box.height = bounds.height;
        box.break_after = false;
        box.floated = true;
        if ( box.width > avail ) {
            box.break_before = true;
            box.x = 0;
        }
        return box;
    }

}
