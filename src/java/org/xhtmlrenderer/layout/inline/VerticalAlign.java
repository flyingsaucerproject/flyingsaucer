package org.xhtmlrenderer.layout.inline;

import java.awt.*;
import java.awt.font.*;
import org.w3c.dom.*;

import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.css.style.*;
import org.xhtmlrenderer.util.u;

public class VerticalAlign {


    public static void setupVerticalAlign( Context c, Node node, InlineBox box ) {
        //u.p("setup vertical align: node = " + node + " box = " + box);
        // get the parent node for styling
        Node parent = box.node.getParentNode();
        //u.p("parent = " + parent);
        Element elem = null;
        if ( box.node.getNodeType() == box.node.TEXT_NODE ) {
            parent = parent.getParentNode();
            elem = (Element)box.node.getParentNode();
        } else {
            elem = (Element)box.node;
        }
        //u.p("parent = " + parent + " elem = " + elem);
        //int parent_height = FontUtil.lineHeight(c,parent);
        Font parent_font = FontUtil.getFont( c, parent );
        //u.p("parent font = " + parent_font);
        LineMetrics parent_metrics = null;
        if ( !LayoutUtil.isReplaced(c, node ) ) {
            if ( !LayoutUtil.isFloatedBlock( node, c ) ) {
                parent_metrics = parent_font.getLineMetrics( box.getSubstring(), ( (Graphics2D)c.getGraphics() ).getFontRenderContext() );
            } else {
                parent_metrics = parent_font.getLineMetrics( "Test", ( (Graphics2D)c.getGraphics() ).getFontRenderContext() );
            }
        } else {
            parent_metrics = parent_font.getLineMetrics( "Test", ( (Graphics2D)c.getGraphics() ).getFontRenderContext() );
        }
        // the height of the font
        float parent_height = parent_metrics.getHeight();
        //u.p("parent strikethrough height = " + parent_metrics.getStrikethroughOffset());
        String vertical_align = c.css.getStringProperty( elem, "vertical-align" );
        // set the height of the box to the height of the font
        if ( !LayoutUtil.isReplaced(c, node ) ) {
            box.height = FontUtil.lineHeight( c, node );
            //u.p("set height of box: " + box.height + " == " + box);
        }
        //u.p("vertical align = " + vertical_align);
        if ( vertical_align == null ) {
            vertical_align = "baseline";
        }
        box.baseline = 0;
        // box.y is relative to the parent's baseline
        box.y = 0;
        // do nothing for 'baseline'
        box.vset = true;
        if ( vertical_align.equals( "baseline" ) ) {
            //u.p("doing baseline");
            Font font = FontUtil.getFont( c, node );
            //u.p("font = " + font);
            FontMetrics fm = c.getGraphics().getFontMetrics( font );
            //noop    box.y = box.y;
            box.y += fm.getDescent();
            //u.p("fm.descent = " + fm.getDescent());
        }
        // works okay i think
        if ( vertical_align.equals( "super" ) ) {
            box.y = box.y + (int)( parent_metrics.getStrikethroughOffset() * 2.0 );
        }
        // works okay, i think
        if ( vertical_align.equals( "sub" ) ) {
            box.y = box.y - (int)parent_metrics.getStrikethroughOffset();
        }
        // joshy: this is using the current baseline instead of the parent's baseline
        // must fix
        if ( vertical_align.equals( "text-top" ) ) {
            // the top of this text is equal to the top of the parent's text
            // so we take the parent's height above the baseline and subtract our
            // height above the baseline
            box.y = -( (int)parent_height - box.height );//(int) (parent_metrics.getStrikethroughOffset()*2.0);
        }
        // not implemented correctly yet
        if ( vertical_align.equals( "text-bottom" ) ) {
            box.y = 0;
        }
        // not implemented correctly yet.
        if ( vertical_align.equals( "top" ) ) {
            //u.p("before y = " + box.y);
            //u.p("baseline = " + box.baseline);
            box.y = box.y - box.baseline;//(int) (parent_metrics.getStrikethroughOffset()*2.0);
            box.top_align = true;
            //u.p("after y = " + box.y);
            box.vset = false;
        }
        if ( vertical_align.equals( "bottom" ) ) {
            //u.p("before y = " + box.y);
            //u.p("baseline = " + box.baseline);
            box.y = box.y - box.baseline;//(int) (parent_metrics.getStrikethroughOffset()*2.0);
            box.bottom_align = true;
            //u.p("after y = " + box.y);
            box.vset = false;
        }
        
        //u.p("box.y = " + box.y);
        //u.p("returning box: " + box);
    }
    
    public static void setupVerticalAlign( Context c, CalculatedStyle style, InlineBox box) {
        Node node = box.node;
        //u.p("setup vertical align: node = " + node + " box = " + box);
        // get the parent node for styling
        Node parent = box.node.getParentNode();
        //u.p("parent = " + parent);
        Element elem = null;
        if ( box.node.getNodeType() == box.node.TEXT_NODE ) {
            parent = parent.getParentNode();
            elem = (Element)box.node.getParentNode();
        } else {
            elem = (Element)box.node;
        }
        //u.p("parent = " + parent + " elem = " + elem);
        //int parent_height = FontUtil.lineHeight(c,parent);
        Font parent_font = FontUtil.getFont( c, parent );
        //u.p("parent font = " + parent_font);
        LineMetrics parent_metrics = null;
        if ( !LayoutUtil.isReplaced(c, node ) ) {
            if ( !LayoutUtil.isFloatedBlock( node, c ) ) {
                parent_metrics = parent_font.getLineMetrics( box.getSubstring(), ( (Graphics2D)c.getGraphics() ).getFontRenderContext() );
            } else {
                parent_metrics = parent_font.getLineMetrics( "Test", ( (Graphics2D)c.getGraphics() ).getFontRenderContext() );
            }
        } else {
            parent_metrics = parent_font.getLineMetrics( "Test", ( (Graphics2D)c.getGraphics() ).getFontRenderContext() );
        }
        // the height of the font
        float parent_height = parent_metrics.getHeight();
        //u.p("parent strikethrough height = " + parent_metrics.getStrikethroughOffset());
        String vertical_align = style.propertyByName("vertical-align").computedValue().asString();
        //c.css.getStringProperty( elem, "vertical-align" );

            // set the height of the box to the height of the font
        if ( !LayoutUtil.isReplaced(c, node ) ) {
            box.height = FontUtil.lineHeight( c, style, box );
            //u.p("set height of box: " + box.height + " == " + box);
        }
        //u.p("vertical align = " + vertical_align);
        if ( vertical_align == null ) {
            vertical_align = "baseline";
        }
        box.baseline = 0;
        // box.y is relative to the parent's baseline
        box.y = 0;
        
        // do nothing for 'baseline'
        box.vset = true;
        if ( vertical_align.equals( "baseline" ) ) {
            Font font = FontUtil.getFont( c, style, node );
            FontMetrics fm = c.getGraphics().getFontMetrics( font );
            box.y += fm.getDescent();
        }
        // works okay i think
        if ( vertical_align.equals( "super" ) ) {
            box.y = box.y + (int)( parent_metrics.getStrikethroughOffset() * 2.0 );
        }
        // works okay, i think
        if ( vertical_align.equals( "sub" ) ) {
            box.y = box.y - (int)parent_metrics.getStrikethroughOffset();
        }

        // joshy: this is using the current baseline instead of the parent's baseline
        // must fix
        if ( vertical_align.equals( "text-top" ) ) {
            // the top of this text is equal to the top of the parent's text
            // so we take the parent's height above the baseline and subtract our
            // height above the baseline
            box.y = -( (int)parent_height - box.height );
        }
        // not implemented correctly yet
        if ( vertical_align.equals( "text-bottom" ) ) {
            box.y = 0;
        }
        // not implemented correctly yet.
        if ( vertical_align.equals( "top" ) ) {
            box.y = box.y - box.baseline;
            box.top_align = true;
            box.vset = false;
        }
        if ( vertical_align.equals( "bottom" ) ) {
            box.y = box.y - box.baseline;
            box.bottom_align = true;
            box.vset = false;
        }
        
        //u.p("box.y = " + box.y);
        //u.p("returning box: " + box);
    }

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param node  PARAM
     * @param box   PARAM
     */
    public static void setupVerticalAlign( Context c, Node node, LineBox box ) {
        //u.p("doing line box: " + box);
        // get the parent node for styling
        Node parent = node.getParentNode();
        Element elem = null;
        if ( node.getNodeType() == node.TEXT_NODE ) {
            parent = parent.getParentNode();
            elem = (Element)node.getParentNode();
        } else {
            elem = (Element)node;
        }
        // top and bottom are max dist from baseline
        int top = 0;
        int bot = 0;
        int height = 0;
        for ( int i = 0; i < box.getChildCount(); i++ ) {
            InlineBox inline = (InlineBox)box.getChild( i );
            // skip floated inlines. they don't affect height calculations
            if ( inline.floated ) {
                continue;
            }
            if ( inline.vset ) {
                //u.p("looking at vset inline: " + inline);
                // compare the top of the box
                if ( inline.y - inline.height < top ) {
                    top = inline.y - inline.height;
                    //u.p("set top to: " + top);
                }
                // compare the bottom of the box
                if ( inline.y + 0 > bot ) {
                    bot = inline.y + 0;
                    //u.p("set bottom to: " + bot);
                }
            } else {
                // if it's not one of the baseline derived vertical aligns
                // then just compare the straight height of the inline
                if ( inline.height > height ) {
                    height = inline.height;
                    //u.p("set height to: " + height);
                }
            }
        }
        //u.p("line bot = " + bot + " top = " + top);
        if ( bot - top > height ) {
            box.height = bot - top;
            box.baseline = box.height - bot;
            //u.p("box.baseline = box height - bot = " + box.baseline);
        } else {
            box.height = height;
            box.baseline = box.height;
            //u.p("box.baseline = box height = " + box.baseline);
        }
        //u.p("line height = " + box.height);
        // loop through all inlines to set the last ones
        for ( int i = 0; i < box.getChildCount(); i++ ) {
            InlineBox inline = (InlineBox)box.getChild( i );
            if ( inline.floated ) {
                inline.y = -box.baseline + inline.height;
            } else {
                if ( !inline.vset ) {
                    inline.vset = true;
                    if ( inline.top_align ) {
                        inline.y = -box.baseline + inline.height;
                    }
                    if ( inline.bottom_align ) {
                        inline.y = 0;
                    }
                }
            }
            //u.p("final inline = " + inline);
        }
    }
}
