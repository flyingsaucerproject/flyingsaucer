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

import org.w3c.dom.Node;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.block.Relative;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.layout.content.FloatedBlockContent;
import org.xhtmlrenderer.layout.content.ReplacedContent;
import org.xhtmlrenderer.layout.content.TextContent;
import org.xhtmlrenderer.layout.inline.TextDecoration;
import org.xhtmlrenderer.layout.inline.VerticalAlign;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.util.u;

import java.awt.*;

/**
 * Description of the Class
 *
 * @author empty
 */
public class LineBreaker {

    /**
     * Standard multiline breaking. This is the most commonly called
     * method and is a good target for speedups.
     *
     * @param c          PARAM
     * @param node       PARAM
     * @param start      PARAM
     * @param text       PARAM
     * @param prev       PARAM
     * @param prev_align PARAM
     * @param avail      PARAM
     * @return Returns
     */
    /*not used, tobe 2004-12-10: public static InlineBox generateMultilineBreak(Context c, Node node, int start, String text,
                                                   InlineBox prev, InlineBox prev_align, int avail) {

        int extra = 0;
        // calc end index to most words that will fit
        int end = start;
        int dbcount = 0;
        while (true) {


            // debugging code
            dbcount++;
            //u.off();
            //u.on(); if ( dbcount > 50 ) { u.on(); }
            if (dbcount > 100) {
                //u.on();
                u.p("db 2 hit");
                u.p("text = " + text);
                u.p("end = " + end);
                throw new InfiniteLoopError("Caught a potential infinite loop in the LineBreaker");
            }


            int next_space = text.indexOf(" ", end);
            if (next_space == -1) {
                next_space = text.length();
            }

            CalculatedStyle style = c.css.getStyle(node);
            Font font = FontUtil.getFont(c, style);
            //Font font = FontUtil.getFont( c, node );

            int len2 = FontUtil.len(c, text.substring(start, next_space), font);
            // if this won't fit, then break and use the previous span
            if (len2 > avail) {
                InlineBox box = styleBox(c, node, start, end, prev, text, prev_align, font);
                return box;
            }
            // if this will fit but we are at the end then break and use current span
            if (next_space == text.length()) {
                InlineBox box = styleBox(c, node, start, next_space, prev, text, prev_align, font);
                //u.p("normal break returning curr span: " + box);
                return box;
            }
            // skip over the space
            end = next_space + 1;
        }
    }*/

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param node  PARAM
     * @param start PARAM
     * @param text  PARAM
     * @param avail PARAM
     * @param font  PARAM
     * @return Returns
     */
    /*not used: tobe 2004-12-10 public static boolean canFitOnLine(Context c, int start, String text, int avail, Font font) {
        // if the rest of text can fit on the current line
        // if length of remaining text < available width
        //u.p("avail = " + avail + " len = " + FontUtil.len(c,node,text.substring(start)));
        if (FontUtil.len(c, text.substring(start), font) < avail) {
            return true;
        } else {
            return false;
        }
    }*/

    /**
     * Description of the Method
     *
     * @param c          PARAM
     * @param node       PARAM
     * @param start      PARAM
     * @param text       PARAM
     * @param prev       PARAM
     * @param prev_align PARAM
     * @param font       PARAM
     * @return Returns
     */
    /*not used: tobe 2004-12-10 public static InlineBox generateRestOfTextNodeInlineBox(Context c, Node node, int start, String text,
                                                            InlineBox prev, InlineBox prev_align, Font font) {
        InlineBox box = styleBox(c, node, start, text.length(), prev, text, prev_align, font);
        // turn off breaking since more might fit on this line
        box.break_after = false;
        //u.p("fits on line returning : " + box);
        return box;
    }*/


    /**
     * Description of the Method
     *
     * @param c          PARAM
     * @param node       PARAM
     * @param start      PARAM
     * @param text       PARAM
     * @param prev       PARAM
     * @param prev_align PARAM
     * @param font       PARAM
     * @return Returns
     */
    /*not used: tobe 2004-12-10 public static InlineBox generateUnbreakableInlineBox(Context c, Node node, int start, String text,
                                                         InlineBox prev, InlineBox prev_align, Font font) {
        //u.p("generating unbreakable inline: start = " + start + " text = " + text);
        
        //joshy: redundant code w/ isUnbreakable. clean up
        //extract first word
        int first_word_index = text.indexOf(" ", start);
        if (first_word_index == -1) {
            first_word_index = text.length();
        }
        String first_word = text.substring(start, first_word_index);
        first_word = first_word.trim();
        
        //u.p("first word = " + first_word);
        InlineBox box = styleBox(c, node, start, first_word_index, prev, text, prev_align, font);
        // move back to the left margin since this is on it's own line
        box.x = 0;
        box.break_before = true;
        //u.p("generate unbreakable returning: " + box);
        box.break_after = false;
        return box;
    }*/

    /**
     * Description of the Method
     *
     * @param c          PARAM
     * @param node       PARAM
     * @param start      PARAM
     * @param prev       PARAM
     * @param text       PARAM
     * @param prev_align PARAM
     * @param font       PARAM
     * @return Returns
     */
    /*not used: tobe 2004-12-10 public static InlineBox generateWhitespaceInlineBox(Context c, Node node, int start,
                                                        InlineBox prev, String text, InlineBox prev_align, Font font) {
        //u.p("preformatted text");
        int cr_index = text.indexOf("\n", start + 1);
        //u.p("cr_index = " + cr_index);
        if (cr_index == -1) {
            cr_index = text.length();
        }
        InlineBox box = styleBox(c, node, start, cr_index, prev, text, prev_align, font);
        return box;
    }*/

    /**
     * Description of the Method
     *
     * @param node PARAM
     * @return Returns
     */
    /*public static InlineBox generateBreakInlineBox(Node node) {
        InlineBox box = new InlineBox();
        box.setNode(node);
        box.width = 0;
        box.height = 0;
        box.break_after = true;
        box.x = 0;
        box.y = 0;
        box.setBreak(true);
        return box;
    }*/


    // change this to use the existing block instead of a new one
    /**
     * Description of the Method
     *
     * @param c          PARAM
     * @param content    PARAM
     * @param avail      PARAM
     * @param prev_align PARAM
     * @param font       PARAM
     * @return Returns
     */
    public static InlineBox generateReplacedInlineBox(Context c, Content content, int avail, InlineBox prev_align, Font font) {
        //u.p("generating replaced Inline Box");

        // get the layout for the replaced element
        Layout layout = c.getLayout(content.getElement());
        BlockBox block = (BlockBox) layout.layout(c, content);
        //u.p("got a block box from the sub layout: " + block);
        Rectangle bounds = new Rectangle(block.x, block.y, block.width, block.height);
        //u.p("bounds = " + bounds);
        /*
         * joshy: change this to just modify the existing block instead of creating
         * a  new one
         */
        //TODO: is firstLineStyle needed? It should probably have been handled above
        CascadedStyle firstLineStyle = c.css.getPseudoElementStyle(content.getElement(), "first-line");
        // create new inline (null text is safe!)
        //TODO: refactor styleBox, it is too overloaded, we know which type we want, right?
        InlineBox box = newBox(c, content, 0, 0, null, bounds, prev_align, font, firstLineStyle);
        //joshy: activate this: box.block = block
        //u.p("created a new inline box");
        box.replaced = true;
        box.sub_block = block;
        block.setParent(box);

        // set up the extents
        box.width = bounds.width;
        box.height = bounds.height;
        box.break_after = false;

        // if it won't fit on this line, then put it on the next one
        if (box.width > avail) {
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
     * @param c     PARAM
     * @param node  PARAM
     * @param start PARAM
     * @param text  PARAM
     * @param avail PARAM
     * @param font  PARAM
     * @return The unbreakableLine value
     */
    /*not used: tobe 2004-12-10 public static boolean isUnbreakableLine(Context c, int start, String text, int avail, Font font) {
        //u.p("isUnbreakableLine( start = " + start + " text = " + text + " avail = " + avail + " font = " + font);

        // extract the first real word from the text
        int first_word_index = text.indexOf(" ", start);
        if (first_word_index == -1) {
            first_word_index = text.length();
        }
        String first_word = text.substring(start, first_word_index);
        first_word = first_word.trim();
        
        // if the first word could fit in the available space, then return true
        if (avail < FontUtil.len(c, first_word, font)) {
            return true;
        } else {
            return false;
        }
    }*/

    /**
     * Gets the whitespace attribute of the LineBreaker class
     *
     * @param c                PARAM
     * @param containing_block PARAM
     * @return The whitespace value
     */
    /*not used: tobe 2004-12-10 public static boolean isWhitespace(Context c, Element containing_block) {
        String white_space = c.css.getStyle(containing_block).getStringProperty("white-space");
        // if doing preformatted whitespace
        if (white_space != null && white_space.equals("pre")) {
            return true;
        } else {
            return false;
        }
    }*/

    /**
     * Description of the Method
     *
     * @param c              PARAM
     * @param content
     * @param start          PARAM
     * @param end            PARAM
     * @param text           PARAM
     * @param prev_align     PARAM
     * @param font           PARAM
     * @param firstLineStyle
     * @return Returns
     */
    public static InlineBox newBox(Context c, Content content, int start, int end, String text, InlineBox prev_align, Font font, CascadedStyle firstLineStyle) {
        return newBox(c, content, start, end, text, null, prev_align, font, firstLineStyle);
    }

// this function by itself takes up fully 29% of the complete program's
// rendering time.
    /**
     * Description of the Method
     *
     * @param c              PARAM
     * @param content
     * @param start          PARAM
     * @param end            PARAM
     * @param text           PARAM
     * @param bounds         PARAM
     * @param prev_align     PARAM
     * @param font           PARAM
     * @param firstLineStyle
     * @return Returns
     */
    public static InlineBox newBox(Context c, Content content, int start, int end, String text, Rectangle bounds, InlineBox prev_align, Font font, CascadedStyle firstLineStyle) {
        InlineBox box = new InlineBox();
        box.setContent(content);
        //TODO: refactor styleBox.
        return styleBox(c, content.getElement(), start, end, text, bounds, prev_align, font, box, firstLineStyle);
    }

    public static InlineBox styleBox(Context c, Node node, int start, int end, String text, Rectangle bounds, InlineBox prev_align, Font font, InlineBox box, CascadedStyle firstLineStyle) {
        //u.p("styleBox node = " + node.getNodeName() + " start = " + start + " end = " + end +
        //" prev = " + prev + " text = " + text + " bounds = " + bounds + " prev_align = " + prev_align);
        //u.p("Making box for: "  + node);
        //u.p("prev = " + prev);
        // if ( prev_align != prev ) {
        //u.p("prev = " + prev);
        //u.p("prev align inline = " + prev_align);
        // }
        Content content = box.getContent();
        CalculatedStyle style = content.getStyle();
        box.setNode(node);
        box.start_index = start;
        box.end_index = end;

        BoxLayout.getBackgroundColor(c, box);
        BoxLayout.getBorder(c, box);
        BoxLayout.getMargin(c, box);
        BoxLayout.getPadding(c, box);

        // use the prev_align to calculate the x
        if (prev_align != null && !prev_align.break_after) {
            box.x = prev_align.x + prev_align.width;
        } else {
            box.x = 0;
        }

        box.y = 0;// it's relative to the line
        try {
            if (!(content instanceof ReplacedContent)) {
                if (!(content instanceof FloatedBlockContent)) {
                    box.width = FontUtil.len(c, text.substring(start, end), font);
                } else {
                    box.width = bounds.width;
                }
            } else {
                box.width = bounds.width;
            }
        } catch (StringIndexOutOfBoundsException ex) {
            u.p("ex");
            u.p("start = " + start);
            u.p("end = " + end);
            u.p("text = " + node.getNodeValue());
            throw ex;
        }
        //u.p("box.x = " + box.x);
        if (content instanceof ReplacedContent) {
            box.height = bounds.height;
        } else if (content instanceof FloatedBlockContent) {
            box.height = bounds.height;
        } else {
            box.height = FontUtil.lineHeight(c, style);
        }

        box.break_after = true;

        box.setText(text);
        box.setMasterText(text);

        if (!(content instanceof ReplacedContent)) {
            if (!(content instanceof FloatedBlockContent)) {
                TextDecoration.setupTextDecoration(box);
                if (box.getText() == null) {
                    return box;
                }
            }
        }

        // do vertical alignment
        
/*        Element elem = null;
        if(node instanceof Element) {
            elem = (Element)node;
        } else {
            elem = (Element)node.getParentNode();
        }*/
        //CalculatedStyle style = box.getStyle(c);//c.css.getStyle(elem);
        VerticalAlign.setupVerticalAlign(c, style, box);
        box.setFont(font);//FontUtil.getFont(c,node));
        box.color = c.css.getStyle(node).getColor();
        Relative.setupRelative(box);

        
        
        // if first line then do extra setup        
        if (c.isFirstLine()) {
            //u.p("node = " + node);
            //u.p("block elem = " + getNearestBlockElement(node,c));
            // if there is a first line firstLineStyle class
            if (firstLineStyle != null) {
                //CalculatedStyle normal = c.css.getStyle(box.getRealElement());
                CalculatedStyle merged = c.css.getDerivedStyle(style, firstLineStyle);
                styleInlineBox(c, merged, box);
            }
        }
        
        

        // adjust width based on borders and padding
        box.width += box.totalHorizontalPadding();
        //box.height += box.totalVerticalPadding();
        
        return box;
    }

    /*not used anymore public static Element getNearestBlockElement(Node node, Context c) {
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            return ((Document) node).getDocumentElement();
        }
        if (LayoutUtil.isBlockNode(node, c)) {
            return (Element) node;
        } else {
            return getNearestBlockElement(node.getParentNode(), c);
        }
    }*/


    public static boolean isFirstLetter(Context c, Node node, int start) {
        //u.p("looking at node: " + node);
        //u.p("start = " + start);
        if (start > 0) {
            return false;
        }
        if (node == null) {
            return false;
        }
        //u.p("parent's first child = " + node.getParentNode().getFirstChild());
        if (node.getParentNode().getFirstChild() != node) {
            return false;
        }
        //u.p("it's the first child");
        CascadedStyle cs = c.css.getPseudoElementStyle(node, "first-letter");
        if (cs != null) {
            return true;
            //return false;
        }
        return false;
    }

    public static InlineBox generateFirstLetterInlineBox(Context c, int start, String text,
                                                         InlineBox prev_align, TextContent content, CascadedStyle firstLetterStyle, CascadedStyle firstLineStyle) {
        // u.p("gen first letter box");
        // u.p("node = " + node);
        // u.p("start = " + start);
        int end = start + 1;
        // u.p("text = " + text);
        // u.p("prev = " + prev);
        // u.p("prev align = " + prev_align);
        // u.p("avail = " + avail);
        
        CalculatedStyle style = content.getStyle();
        Font font = FontUtil.getFont(c, style);
        //TODO: refactor styleBox, far too overloaded. Temporary hack here. Does box already have content set?
        InlineBox box = newBox(c, content, start, end, text, prev_align, font, firstLineStyle);
        //not used: int len = FontUtil.len(c, text.substring(start, end), font);
        CalculatedStyle cs = null;
        if (firstLetterStyle != null) {
            cs = c.css.getDerivedStyle(style, firstLetterStyle);
        } else {
            cs = style;
        }
        // u.p("style = " + cs);
        styleInlineBox(c, cs, box);
        box.break_after = false;
        //u.p("generated a first letter inline: " + box);
        return box;
    }

    public static void styleInlineBox(Context c, CalculatedStyle style, InlineBox box) {
        box.color = style.getColor();
        TextDecoration.setupTextDecoration(style, box);
        Font font = FontUtil.getFont(c, style);
        box.setFont(font);
        box.width = FontUtil.len(c, box.getSubstring(), font);
        box.height = FontUtil.lineHeight(c, style, box);
        VerticalAlign.setupVerticalAlign(c, style, box);
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.31  2004/12/10 06:51:02  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.30  2004/12/09 00:11:52  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.29  2004/12/05 18:11:38  tobega
 * Now uses style cache for pseudo-element styles. Also started preparing to replace inline node handling with inline content handling.
 *
 * Revision 1.28  2004/12/05 14:35:39  tobega
 * Cleaned up some usages of Node (and removed unused stuff) in layout code. The goal is to pass "better" objects than Node wherever possible in an attempt to shake out the bugs in tree-traversal (probably often unnecessary tree-traversal)
 *
 * Revision 1.27  2004/12/05 00:48:58  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.26  2004/12/01 01:57:01  joshy
 * more updates for float support.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.25  2004/11/27 15:46:39  joshy
 * lots of cleanup to make the code clearer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.24  2004/11/23 02:41:59  joshy
 * fixed vertical-align support for first-letter pseudos
 * tested first-line w/ new breaking routines
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.23  2004/11/23 02:11:25  joshy
 * re-enabled text-decoration
 * moved it to it's own class
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.22  2004/11/18 02:37:26  joshy
 * moved most of default layout into layout util or box layout
 *
 * start spliting parts of box layout into the block subpackage
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.21  2004/11/15 14:33:10  joshy
 * fixed line breaking bug with certain kinds of unbreakable lines
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.20  2004/11/14 16:40:58  joshy
 * refactored layout factory
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.19  2004/11/10 14:54:43  joshy
 * code cleanup on aisle 6
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.18  2004/11/10 14:31:15  joshy
 * removed commented out lines
 *
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

