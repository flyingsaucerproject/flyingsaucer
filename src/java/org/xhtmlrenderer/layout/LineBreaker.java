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
import java.awt.Rectangle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.layout.inline.*;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.util.InfiniteLoopError;
import org.xhtmlrenderer.util.u;

/**
 * Description of the Class
 *
 * @author   empty
 */
public class LineBreaker {

    /**
     * Standard multiline breaking. This is the most commonly called
     * method and is a good target for speedups.
     *
     * @param c           PARAM
     * @param node        PARAM
     * @param start       PARAM
     * @param text        PARAM
     * @param prev        PARAM
     * @param prev_align  PARAM
     * @param avail       PARAM
     * @return            Returns
     */
    public static InlineBox generateMultilineBreak( Context c, Node node, int start, String text,
                                                    InlineBox prev, InlineBox prev_align, int avail ) {

        int extra = 0;
        // calc end index to most words that will fit
        int end = start;
        int dbcount = 0;
        while ( true ) {
            
            
            // debugging code
            dbcount++;
            //u.off();
            u.on();
            if ( dbcount > 50 ) {
                u.on();
            }
            if ( dbcount > 100 ) {
                u.on();
                u.p( "db 2 hit" );
                u.p( "text = " + text );
                u.p( "end = " + end );
                throw new InfiniteLoopError( "Caught a potential infinite loop in the LineBreaker" );
            }

            
            
            int next_space = text.indexOf( " ", end );
            if ( next_space == -1 ) {
                next_space = text.length();
            }

            Font font = FontUtil.getFont( c, node );

            int len2 = FontUtil.len( c, node, text.substring( start, next_space ), font );
            // if this won't fit, then break and use the previous span
            if ( len2 > avail ) {
                InlineBox box = newBox( c, node, start, end, prev, text, prev_align, font );
                return box;
            }
            // if this will fit but we are at the end then break and use current span
            if ( next_space == text.length() ) {
                InlineBox box = newBox( c, node, start, next_space, prev, text, prev_align, font );
                //u.p("normal break returning curr span: " + box);
                return box;
            }
            // skip over the space
            end = next_space + 1;
        }
    }

    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param node   PARAM
     * @param start  PARAM
     * @param text   PARAM
     * @param avail  PARAM
     * @param font   PARAM
     * @return       Returns
     */
    public static boolean canFitOnLine( Context c, Node node, int start, String text, int avail, Font font ) {
        // if the rest of text can fit on the current line
        // if length of remaining text < available width
        //u.p("avail = " + avail + " len = " + FontUtil.len(c,node,text.substring(start)));
        if ( FontUtil.len( c, node, text.substring( start ), font ) < avail ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Description of the Method
     *
     * @param c           PARAM
     * @param node        PARAM
     * @param start       PARAM
     * @param text        PARAM
     * @param prev        PARAM
     * @param prev_align  PARAM
     * @param font        PARAM
     * @return            Returns
     */
    public static InlineBox generateRestOfTextNodeInlineBox( Context c, Node node, int start, String text,
                                                             InlineBox prev, InlineBox prev_align, Font font ) {
        InlineBox box = newBox( c, node, start, text.length(), prev, text, prev_align, font );
        // turn off breaking since more might fit on this line
        box.break_after = false;
        //u.p("fits on line returning : " + box);
        return box;
    }


    /**
     * Description of the Method
     *
     * @param c           PARAM
     * @param node        PARAM
     * @param start       PARAM
     * @param text        PARAM
     * @param prev        PARAM
     * @param prev_align  PARAM
     * @param font        PARAM
     * @return            Returns
     */
    public static InlineBox generateUnbreakableInlineBox( Context c, Node node, int start, String text, InlineBox prev, InlineBox prev_align, Font font ) {
        int first_word_index = text.indexOf( " ", start );
        if ( first_word_index == -1 ) {
            first_word_index = text.length();
        }
        String first_word = text.substring( start, first_word_index );
        first_word = first_word.trim();
        InlineBox box = newBox( c, node, start, first_word_index, prev, text, prev_align, font );
        // move back to the left margin since this is on it's own line
        box.x = 0;
        box.break_before = true;
        //u.p("unbreakable long word returning: " + box);
        box.break_after = true;
        return box;
    }

    /**
     * Description of the Method
     *
     * @param c           PARAM
     * @param node        PARAM
     * @param start       PARAM
     * @param prev        PARAM
     * @param text        PARAM
     * @param prev_align  PARAM
     * @param font        PARAM
     * @return            Returns
     */
    public static InlineBox generateWhitespaceInlineBox( Context c, Node node, int start,
                                                         InlineBox prev, String text, InlineBox prev_align, Font font ) {
        //u.p("preformatted text");
        int cr_index = text.indexOf( "\n", start + 1 );
        //u.p("cr_index = " + cr_index);
        if ( cr_index == -1 ) {
            cr_index = text.length();
        }
        InlineBox box = newBox( c, node, start, cr_index, prev, text, prev_align, font );
        return box;
    }

    /**
     * Description of the Method
     *
     * @param node  PARAM
     * @return      Returns
     */
    public static InlineBox generateBreakInlineBox( Node node ) {
        InlineBox box = new InlineBox();
        box.node = node;
        box.width = 0;
        box.height = 0;
        box.break_after = true;
        box.x = 0;
        box.y = 0;
        box.is_break = true;
        return box;
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
    public static InlineBox generateReplacedInlineBox( Context c, Node node, int avail, InlineBox prev, String text, InlineBox prev_align, Font font ) {
        //u.p("generating replaced Inline Box");

        // get the layout for the replaced element
        Layout layout = LayoutFactory.getLayout( node );
        BlockBox block = (BlockBox)layout.layout( c, (Element)node );
        //u.p("got a block box from the sub layout: " + block);
        Rectangle bounds = new Rectangle( block.x, block.y, block.width, block.height );
        //u.p("bounds = " + bounds);
        /*
         * joshy: change this to just modify the existing block instead of creating
         * a  new one
         */
        // create new inline
        InlineBox box = newBox( c, node, 0, 0, prev, text, bounds, prev_align, font );
        //joshy: activate this: box.block = block
        //u.p("created a new inline box");
        box.replaced = true;
        box.sub_block = block;
        block.setParent( box );

        // set up the extents
        box.width = bounds.width;
        box.height = bounds.height;
        box.break_after = false;

        // if it won't fit on this line, then put it on the next one
        if ( box.width > avail ) {
            box.break_before = true;
            box.x = 0;
        }

        // return
        //u.p("last replaced = " + box);
        return box;
    }

    /**
     * Gets the unbreakableLine attribute of the LineBreaker class
     *
     * @param c      PARAM
     * @param node   PARAM
     * @param start  PARAM
     * @param text   PARAM
     * @param avail  PARAM
     * @param font   PARAM
     * @return       The unbreakableLine value
     */
    public static boolean isUnbreakableLine( Context c, Node node, int start, String text, int avail, Font font ) {
        int first_word_index = text.indexOf( " ", start );
        if ( first_word_index == -1 ) {
            first_word_index = text.length();
        }
        String first_word = text.substring( start, first_word_index );
        first_word = first_word.trim();
        if ( avail < FontUtil.len( c, node, first_word, font ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the whitespace attribute of the LineBreaker class
     *
     * @param c                 PARAM
     * @param containing_block  PARAM
     * @return                  The whitespace value
     */
    public static boolean isWhitespace( Context c, Element containing_block ) {
        String white_space = c.css.getStringProperty( containing_block, "white-space" );
        // if doing preformatted whitespace
        if ( white_space != null && white_space.equals( "pre" ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Description of the Method
     *
     * @param c           PARAM
     * @param node        PARAM
     * @param start       PARAM
     * @param end         PARAM
     * @param prev        PARAM
     * @param text        PARAM
     * @param prev_align  PARAM
     * @param font        PARAM
     * @return            Returns
     */
    public static InlineBox newBox( Context c, Node node, int start, int end, InlineBox prev, String text, InlineBox prev_align, Font font ) {
        return newBox( c, node, start, end, prev, text, null, prev_align, font );
    }

// this function by itself takes up fully 29% of the complete program's
// rendering time.
    /**
     * Description of the Method
     *
     * @param c           PARAM
     * @param node        PARAM
     * @param start       PARAM
     * @param end         PARAM
     * @param prev        PARAM
     * @param text        PARAM
     * @param bounds      PARAM
     * @param prev_align  PARAM
     * @param font        PARAM
     * @return            Returns
     */
    public static InlineBox newBox( Context c, Node node, int start, int end, InlineBox prev, String text, Rectangle bounds, InlineBox prev_align, Font font ) {
        //u.p("newBox node = " + node.getNodeName() + " start = " + start + " end = " + end +
        //" prev = " + prev + " text = " + text + " bounds = " + bounds + " prev_align = " + prev_align);
        //u.p("Making box for: "  + node);
        //u.p("prev = " + prev);
        if ( prev_align != prev ) {
            //u.p("prev = " + prev);
            //u.p("prev align inline = " + prev_align);
        }
        InlineBox box = new InlineBox();
        box.node = node;
        box.start_index = start;
        box.end_index = end;
        
        BoxLayout.getBackgroundColor(c,box);
        BoxLayout.getBorder(c,box);
        BoxLayout.getMargin(c,box);
        BoxLayout.getPadding(c,box);

        // use the prev_align to calculate the x
        if ( prev_align != null && !prev_align.break_after ) {
            box.x = prev_align.x + prev_align.width;
        } else {
            box.x = 0;
        }

        box.y = 0;// it's relative to the line
        try {
            if ( !InlineLayout.isReplaced( node ) ) {
                if ( !InlineLayout.isFloatedBlock( node, c ) ) {
                    box.width = FontUtil.len( c, node, text.substring( start, end ), font );
                } else {
                    box.width = bounds.width;
                }
            } else {
                box.width = bounds.width;
            }
        } catch ( StringIndexOutOfBoundsException ex ) {
            u.p( "ex" );
            u.p( "start = " + start );
            u.p( "end = " + end );
            u.p( "text = " + node.getNodeValue() );
            throw ex;
        }
        //u.p("box.x = " + box.x);
        if ( InlineLayout.isReplaced( node ) ) {
            box.height = bounds.height;
        } else if ( InlineLayout.isFloatedBlock( node, c ) ) {
            box.height = bounds.height;
        } else {
            box.height = FontUtil.lineHeight( c, node );
        }

        box.break_after = true;

        box.setText(text);
        
        if ( !InlineLayout.isReplaced( node ) ) {
            if ( !InlineLayout.isFloatedBlock( node, c ) ) {
                FontUtil.setupTextDecoration( c, node, box );
                if ( box.getText() == null ) {
                    return box;
                }
            }
        }

        // do vertical alignment
        VerticalAlign.setupVerticalAlign( c, node, box );
        box.setFont( font );//FontUtil.getFont(c,node));
        if ( node.getNodeType() == node.TEXT_NODE ) {
            box.color = c.css.getColor( (Element)node.getParentNode(), true );
        } else {
            box.color = c.css.getColor( (Element)node, true );
        }
        InlineLayout.setupRelative( c, box );

        
        
        // if first line then do extra setup        
        if(c.isFirstLine()) {
            //u.p("node = " + node);
            //u.p("block elem = " + getNearestBlockElement(node,c));
            // if there is a first line pseudo class
            CascadedStyle pseudo = c.css.getPseudoElementStyle(getNearestBlockElement(node,c),"first-line");
            if(pseudo != null) {
                CalculatedStyle normal = c.css.getStyle(box.getRealElement());
                CalculatedStyle merged = new CalculatedStyle(normal,pseudo);
                styleInlineBox(c,merged,box);
            }
        }
        
        //CascadedStyle hover = c.css.getPseudoClassStyle(getNearestBlockElement(node,c),"hover");
        //u.p("hover style = " + hover);
        
        

        // adjust width based on borders and padding
        box.width += box.totalHorizontalPadding();
        //box.height += box.totalVerticalPadding();
        
        return box;
    }
    
    public static Element getNearestBlockElement(Node node, Context c) {
        if(DefaultLayout.isBlockNode(node,c)) {
            return (Element)node;
        } else {
            return getNearestBlockElement(node.getParentNode(),c);
        }
    }
    

    public static boolean isFirstLetter(Context c, Node node, int start) {
        //u.p("looking at node: " + node);
        //u.p("start = " + start);
        if(start > 0) {
            return false;
        }
        if(node == null) {
            return false;
        }
        if(node.getParentNode().getFirstChild() != node) {
            return false;
        }
        //u.p("it's the first child");
        CascadedStyle cs = c.css.getPseudoElementStyle(getElement(node),"first-letter");
        if(cs != null) {
          //  return true;
          return false;
        }
        return false;
    }
    
    
    private static Element getElement(Node node) {
        Element elem = null;
        if(node instanceof Element) {
            elem = (Element)node;
        } else {
            elem = (Element)node.getParentNode();
        }
        return elem;
    }
    public static InlineBox generateFirstLetterInlineBox( Context c, Node node, int start, String text,
                                                            InlineBox prev, InlineBox prev_align, int avail ) {
        // u.p("gen first letter box");
        // u.p("node = " + node);
        // u.p("start = " + start);
        int end = start + 1;
        // u.p("text = " + text);
        // u.p("prev = " + prev);
        // u.p("prev align = " + prev_align);
        // u.p("avail = " + avail);
        
        Font font = FontUtil.getFont(c, node);
        int len = FontUtil.len( c, node, text.substring(start,end), font);
        InlineBox box = newBox( c, node, start, end, prev, text, prev_align, font );
        Element elem = getElement(node);
        CascadedStyle ps = c.css.getPseudoElementStyle(elem,"first-letter");
        CalculatedStyle parent = c.css.getStyle(elem);
        CalculatedStyle cs = null;
        if(ps != null) {
            cs = new CalculatedStyle(parent, ps);
        } else {
            cs = parent;
        }
        //u.p("style = " + cs);
        styleInlineBox(c, cs, box);
        box.break_after = false;
        return box;
    }
    
    private static void styleInlineBox(Context c, CalculatedStyle style, InlineBox box) {
        box.color = style.getColor();
        FontUtil.setupTextDecoration( style, box.node, box );
        Font font = FontUtil.getFont( c, style, box.node);
        box.setFont( font );
        box.width = FontUtil.len( c, box.getSubstring(), font );
        box.height = FontUtil.lineHeight( c, style, box );
        //FontUtil.setupVerticalAlign( c, box );
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.17  2004/11/09 16:24:30  joshy
 * moved float code into separate class
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/11/09 16:07:57  joshy
 * moved vertical align code
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2004/11/09 15:53:49  joshy
 * initial support for hover (currently disabled)
 * moved justification code into it's own class in a new subpackage for inline
 * layout (because it's so blooming complicated)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/11/09 00:41:44  joshy
 * fixed merge error
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/11/09 00:36:54  tobega
 * Fixed some NPEs
 *
 * Revision 1.12  2004/11/09 00:36:08  joshy
 * fixed more text alignment
 * added menu item to show font metrics
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.11  2004/11/08 23:53:27  joshy
 * update for first-line support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/08 23:15:56  tobega
 * Changed pseudo-element styling to just return CascadedStyle
 *
 * Revision 1.9  2004/11/08 22:08:00  joshy
 * improved inline border formatting and text drawing
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/08 20:50:59  joshy
 * improved float support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2004/11/08 16:56:52  joshy
 * added first-line pseudo-class support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/08 15:10:10  joshy
 * added support for styling :first-letter inline boxes
 * updated the absolute positioning tests
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/06 22:49:52  joshy
 * cleaned up alice
 * initial support for inline borders and backgrounds
 * moved all of inlinepainter back into inlinerenderer, where it belongs.
 *
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/11/04 15:35:45  joshy
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
 * Revision 1.3  2004/10/23 13:46:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

