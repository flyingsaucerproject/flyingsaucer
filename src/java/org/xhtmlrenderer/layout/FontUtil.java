/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.layout;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.css.style.*;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.u;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class FontUtil {

    /** Description of the Field */
    static boolean quick = false;

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param node  PARAM
     * @param str   PARAM
     * @param font  PARAM
     * @return      Returns
     */
    public static int len( Context c, Node node, String str, Font font ) {
        return c.getGraphics().getFontMetrics( font ).stringWidth( str );
    }
    public static int len( Context c, String str, Font font ) {
        return c.getGraphics().getFontMetrics( font ).stringWidth( str );
    }

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param node  PARAM
     * @return      Returns
     */
    public static int lineHeight( Context c, Node node ) {
        int val = c.getGraphics().getFontMetrics( getFont( c, node ) ).getHeight();
        Element elem = null;
        if(node instanceof Element) {
            elem = (Element)node;
        } else {
            elem = (Element)node.getParentNode();
        }
        if(c.css.hasProperty(elem,"line-height",true)) {
            //u.p("elem = " + node);
            val = (int)c.css.getFloatProperty(elem, "line-height", val, true);
            //u.p("val = " + val);
        }
        return val;
    }
    
    public static int lineHeight(Context c, CalculatedStyle style, InlineBox box) {
        if(style.hasProperty("line-height")) {
            return (int)style.propertyByName("line-height").computedValue().asFloat();
        } else {
            return c.getGraphics().getFontMetrics( box.getFont() ).getHeight();
        }
    }

    /**
     * PWW ADDED 14/08/04
     *
     * @param context  PARAM
     * @param elem     PARAM
     * @return         Returns
     */
    public static int fontXHeightForElement( Context context, Element elem ) {
        return lineHeight( context, elem );
    }

    /**
     * Description of the Method
     *
     * @param font  PARAM
     * @param g     PARAM
     */
    public static void dumpFontMetrics( Font font, Graphics g ) {
        FontMetrics fm = g.getFontMetrics( font );
        XRLog.layout( "Font: " + font.toString() );
        XRLog.layout( "FontMetrics: " + fm.toString() );
        XRLog.layout( "Ascent: " + fm.getAscent() );
        XRLog.layout( "Descent: " + fm.getDescent() );
        XRLog.layout( "Height: " + fm.getHeight() );
        XRLog.layout( "Leading: " + fm.getLeading() );
        XRLog.layout( "Max Advance: " + fm.getMaxAdvance() );
        XRLog.layout( "Max Ascent: " + fm.getMaxAscent() );
        XRLog.layout( "Max Char Bounds: " + fm.getMaxCharBounds( g ) );
        XRLog.layout( "Max Descent: " + fm.getMaxDescent() );
        XRLog.layout( "hasUniformLineMetrics: " + fm.hasUniformLineMetrics() );
    }


    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param node  PARAM
     * @param box   PARAM
     */
    public static void setupTextDecoration( Context c, Node node, InlineBox box ) {
        Element el = null;
        if ( node instanceof Element ) {
            el = (Element)node;
        } else {
            el = (Element)node.getParentNode();
        }
        String text_decoration = c.css.getStringProperty( el, "text-decoration" );
        if ( text_decoration != null && text_decoration.equals( "underline" ) ) {
            box.underline = true;
        }
        if ( text_decoration != null && text_decoration.equals( "line-through" ) ) {
            box.strikethrough = true;
        }
        if ( text_decoration != null && text_decoration.equals( "overline" ) ) {
            box.overline = true;
        }
    }
    public static void setupTextDecoration( CalculatedStyle style, Node node, InlineBox box ) {
        Element el = null;
        if ( node instanceof Element ) {
            el = (Element)node;
        } else {
            el = (Element)node.getParentNode();
        }
        if(style.hasProperty("text-decoration")) {
            DerivedProperty text_decoration = style.propertyByName("text-decoration");
            DerivedValue dv = text_decoration.computedValue();
            String td = dv.asString();
            if ( td != null && td.equals( "underline" ) ) {
                box.underline = true;
            }
            if ( td != null && td.equals( "line-through" ) ) {
                box.strikethrough = true;
            }
            if ( td != null && td.equals( "overline" ) ) {
                box.overline = true;
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param node  PARAM
     * @param box   PARAM
     */
    public static void setupVerticalAlign( Context c, Node node, InlineBox box ) {
        //u.p("setup vertical align: node = " + node + " box = " + box);
        // get the parent node for styling
        Node parent = node.getParentNode();
        //u.p("parent = " + parent);
        Element elem = null;
        if ( node.getNodeType() == node.TEXT_NODE ) {
            parent = parent.getParentNode();
            elem = (Element)node.getParentNode();
        } else {
            elem = (Element)node;
        }
        //u.p("parent = " + parent + " elem = " + elem);
        //int parent_height = FontUtil.lineHeight(c,parent);
        Font parent_font = FontUtil.getFont( c, parent );
        LineMetrics parent_metrics = null;
        if ( !InlineLayout.isReplaced( node ) ) {
            if ( !InlineLayout.isFloatedBlock( node, c ) ) {
                parent_metrics = parent_font.getLineMetrics( box.getText(), ( (Graphics2D)c.getGraphics() ).getFontRenderContext() );
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
        if ( !InlineLayout.isReplaced( node ) ) {
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
            box.y -= fm.getDescent();
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

    /**
     * Gets the font attribute of the FontUtil class
     *
     * @param c  PARAM
     * @param e  PARAM
     * @return   The font value
     */
    public static Font getFont( Context c, Node e ) {
        //u.p("testing node: " + e);
        //Font f = c.getGraphics().getFont();

        // if plain text then get the styling from the parent node
        if ( e.getNodeType() == e.TEXT_NODE ) {
            //u.p("it's a node");
            Element el = (Element)e.getParentNode();
            return getElementFont( c, el );
        }

        if ( e.getNodeType() == e.ELEMENT_NODE ) {
            Element el = (Element)e;
            return getElementFont( c, el );
        }

        u.p( "big error in getFont(). Got a node that is neither txt nor element" );
        return null;
    }
    public static Font getFont( Context c, CalculatedStyle style, Node e ) {
        // if plain text then get the styling from the parent node
        if ( e.getNodeType() == e.TEXT_NODE ) {
            //u.p("it's a node");
            Element el = (Element)e.getParentNode();
            return getElementFont( c, style, el );
        }

        if ( e.getNodeType() == e.ELEMENT_NODE ) {
            Element el = (Element)e;
            return getElementFont( c, style, el );
        }

        u.p( "big error in getFont(). Got a node that is neither txt nor element" );
        return null;
    }

    /**
     * Gets the elementFont attribute of the FontUtil class
     *
     * @param c   PARAM
     * @param el  PARAM
     * @return    The elementFont value
     */
    public static Font getElementFont( Context c, Element el ) {
        // TODO: need to discuss what sort of caching should be going on here, because relative
        // datatypes like EM and EX depend in part on the 'current' font for an element (PWW 14/08/04)
        //u.p("testing node: " + e);
        Font f = c.getGraphics().getFont();
        if ( quick ) {
            //f = f.deriveFont((float)((int)(Math.random()*10)));
            return f;
        }

        if ( el.getParentNode().getNodeType() == el.DOCUMENT_NODE ) {
            //u.p("ended up at the top somehow!: ");
            return c.getGraphics().getFont().deriveFont( (float)10 );
        }

        // calculate the font size
        // look up the parent and use it's font size to scale against
        // joshy: this will fail if the parent also has a relative size
        //  need to fix this by passing down the enclosing block's font size
        //  in the context
        Element par = (Element)el.getParentNode();
        float parent_size = c.css.getFloatProperty( par, "font-size", true );
        float size = c.css.getFloatProperty( el, "font-size", parent_size, true );

        String weight = c.css.getStringProperty( el, "font-weight" );
        String[] families = c.css.getStringArrayProperty( el, "font-family" );

        String style = c.css.getStringProperty( el, "font-style" );
        f = c.getFontResolver().resolveFont( c, families, size, weight, style );

        // calculate the font color
        c.getGraphics().setColor( c.css.getColor( el ) );
        return f;
    }

    public static Font getElementFont( Context c, CalculatedStyle style, Element el ) {
        // TODO: need to discuss what sort of caching should be going on here, because relative
        // datatypes like EM and EX depend in part on the 'current' font for an element (PWW 14/08/04)
        //u.p("testing node: " + e);
        Font f = c.getGraphics().getFont();
        if ( quick ) {
            return f;
        }

        if ( el.getParentNode().getNodeType() == el.DOCUMENT_NODE ) {
            //u.p("ended up at the top somehow!: ");
            return c.getGraphics().getFont().deriveFont( (float)10 );
        }

        // calculate the font size
        // look up the parent and use it's font size to scale against
        // joshy: this will fail if the parent also has a relative size
        //  need to fix this by passing down the enclosing block's font size
        //  in the context
        Element par = (Element)el.getParentNode();
        //float parent_size = c.css.getFloatProperty( par, "font-size", true );
        //float size = c.css.getFloatProperty( el, "font-size", parent_size, true );
        float size = style.propertyByName("font-size").computedValue().asFloat();

        //String weight = c.css.getStringProperty( el, "font-weight" );
        String weight = style.propertyByName("font-weight").computedValue().asString();
        String[] families = c.css.getStringArrayProperty( el, "font-family" );
        
        //String fstyle = c.css.getStringProperty( el, "font-style" );
        String fstyle = style.propertyByName("font-style").computedValue().asString();
        f = c.getFontResolver().resolveFont( c, families, size, weight, fstyle );

        // calculate the font color
        //joshy: this shouldn't matter. c.getGraphics().setColor( c.css.getColor( el ) );
        return f;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.7  2004/11/08 15:10:10  joshy
 * added support for styling :first-letter inline boxes
 * updated the absolute positioning tests
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/06 01:50:40  joshy
 * support for line-height
 * cleaned up the alice demo
 * added unit tests for font family selection and line-height
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/04 15:35:45  joshy
 * initial float support
 * includes right and left float
 * cannot have more than one float per line per side
 * floats do not extend beyond enclosing block
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/23 13:46:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

