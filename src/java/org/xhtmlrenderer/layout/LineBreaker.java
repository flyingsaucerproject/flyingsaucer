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
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.layout.content.FloatedBlockContent;
import org.xhtmlrenderer.layout.content.InlineBlockContent;
import org.xhtmlrenderer.layout.content.TextContent;
import org.xhtmlrenderer.layout.inline.TextDecoration;
import org.xhtmlrenderer.layout.inline.VerticalAlign;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.util.Uu;

import java.awt.Font;
import java.awt.Rectangle;

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
            //Uu.off();
            //Uu.on(); if ( dbcount > 50 ) { Uu.on(); }
            if (dbcount > 100) {
                //Uu.on();
                Uu.p("db 2 hit");
                Uu.p("text = " + text);
                Uu.p("end = " + end);
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
                //Uu.p("normal break returning curr span: " + box);
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
        //Uu.p("avail = " + avail + " len = " + FontUtil.len(c,node,text.substring(start)));
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
        //Uu.p("fits on line returning : " + box);
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
        //Uu.p("generating unbreakable inline: start = " + start + " text = " + text);
        
        //joshy: redundant code w/ isUnbreakable. clean up
        //extract first word
        int first_word_index = text.indexOf(" ", start);
        if (first_word_index == -1) {
            first_word_index = text.length();
        }
        String first_word = text.substring(start, first_word_index);
        first_word = first_word.trim();
        
        //Uu.p("first word = " + first_word);
        InlineBox box = styleBox(c, node, start, first_word_index, prev, text, prev_align, font);
        // move back to the left margin since this is on it's own line
        box.Xx = 0;
        box.break_before = true;
        //Uu.p("generate unbreakable returning: " + box);
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
        //Uu.p("preformatted text");
        int cr_index = text.indexOf("\n", start + 1);
        //Uu.p("cr_index = " + cr_index);
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
        box.Xx = 0;
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
        //Uu.p("generating replaced Inline Box");

        // get the layout for the replaced element
        Layout layout = c.getLayout(content.getElement());
        BlockBox block = (BlockBox) layout.layout(c, content);
        //Uu.p("got a block box from the sub layout: " + block);
        Rectangle bounds = new Rectangle(block.x, block.y, block.width, block.height);
        //Uu.p("bounds = " + bounds);
        /*
         * joshy: change this to just modify the existing block instead of creating
         * a  new one
         */
        // create new inline (null text is safe!)
        //TODO: refactor styleBox, it is too overloaded, we know which type we want, right?
        InlineBox box = newBox(c, content, 0, 0, bounds, prev_align, font);
        //joshy: activate this: box.block = block
        //Uu.p("created a new inline box");
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
        //Uu.p("last replaced = " + box);
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
        //Uu.p("isUnbreakableLine( start = " + start + " text = " + text + " avail = " + avail + " font = " + font);

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
     * @param c          PARAM
     * @param content
     * @param start      PARAM
     * @param end        PARAM
     * @param prev_align PARAM
     * @param font       PARAM
     * @return Returns
     */
    /* not used public static InlineBox newBox(Context c, Content content, int start, int end, InlineBox prev_align, Font font) {
        return newBox(c, content, start, end, null, prev_align, font);
    }*/

// this function by itself takes up fully 29% of the complete program's
// rendering time.
    /**
     * Description of the Method
     *
     * @param c          PARAM
     * @param content
     * @param start      PARAM
     * @param end        PARAM
     * @param bounds     PARAM
     * @param prev_align PARAM
     * @param font       PARAM
     * @return Returns
     */
    public static InlineBox newBox(Context c, Content content, int start, int end, Rectangle bounds, InlineBox prev_align, Font font) {
        InlineBox box = new InlineBox();
        box.content = content;
        return styleBox(c, content.getElement(), start, end, bounds, prev_align, font, box);
    }

    //TODO: refactor styleBox.
    public static InlineBox styleBox(Context c, Node node, int start, int end, Rectangle bounds, InlineBox prev_align, Font font, InlineBox box) {
        //Uu.p("styleBox node = " + node.getNodeName() + " start = " + start + " end = " + end +
        //" prev = " + prev + " text = " + text + " bounds = " + bounds + " prev_align = " + prev_align);
        //Uu.p("Making box for: "  + node);
        //Uu.p("prev = " + prev);
        // if ( prev_align != prev ) {
        //Uu.p("prev = " + prev);
        //Uu.p("prev align inline = " + prev_align);
        // }
        Content content = box.content;
        CalculatedStyle style = c.getCurrentStyle();
        //box.start_index = start;
        //box.end_index = end;
        if (content instanceof TextContent) {
            box.setSubstring(start, end);
        }
        BoxLayout.getBackgroundColor(c, box);
        //BoxLayout.getBorder(c, box);
        BoxLayout.getMargin(c, box);
        BoxLayout.getPadding(c, box);

        // use the prev_align to calculate the Xx
        if (prev_align != null && !prev_align.break_after) {
            box.x = prev_align.x + prev_align.width;
        } else {
            box.x = 0;
        }

        box.y = 0;// it's relative to the line
        try {
            if (!(content instanceof InlineBlockContent)) {
                if (!(content instanceof FloatedBlockContent)) {
                    box.width = FontUtil.len(c, box.getSubstring(), font);
                } else {
                    box.width = bounds.width;
                }
            } else {
                box.width = bounds.width;
            }
        } catch (StringIndexOutOfBoundsException ex) {
            Uu.p("ex");
            Uu.p("start = " + start);
            Uu.p("end = " + end);
            Uu.p("text = " + node.getNodeValue());
            throw ex;
        }
        //Uu.p("box.Xx = " + box.Xx);
        if (content instanceof InlineBlockContent) {
            box.height = bounds.height;
        } else if (content instanceof FloatedBlockContent) {
            box.height = bounds.height;
        } else {
            box.height = FontUtil.lineHeight(c, style);
        }

        box.break_after = true;

        //already set: box.setMasterText(text);

        if (!(content instanceof InlineBlockContent)) {
            if (!(content instanceof FloatedBlockContent)) {
                TextDecoration.setupTextDecoration(style, box);
                //TODO: don't understand the reasoning behind this:
                //was: if (box.getText() == null) {
                if (box.getMasterText() == null) {
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
        //box.color = style.getColor();
        //Relative.setupRelative(box, c);

        
        
        // if first line then do extra setup
        //this should be taken care of already - tobe 2004-12-11
        /*if (c.isFirstLine()) {
            //Uu.p("node = " + node);
            //Uu.p("block elem = " + getNearestBlockElement(node,c));
            // if there is a first line firstLineStyle class
            if (firstLineStyle != null) {
                //CalculatedStyle normal = c.css.getStyle(box.getRealElement());
                CalculatedStyle merged = c.css.getDerivedStyle(style, firstLineStyle);
                styleInlineBox(c, merged, box);
            }
        }*/
        
        

        // adjust width based on borders and padding
        box.width += box.totalHorizontalPadding(c.getCurrentStyle());
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


    /*not used public static InlineBox generateFirstLetterInlineBox(Context c, int start,
                                                         InlineBox prev_align, TextContent content, CascadedStyle firstLetterStyle) {
        // Uu.p("gen first letter box");
        // Uu.p("node = " + node);
        // Uu.p("start = " + start);
        int end = start + 1;
        // Uu.p("text = " + text);
        // Uu.p("prev = " + prev);
        // Uu.p("prev align = " + prev_align);
        // Uu.p("avail = " + avail);
        c.pushStyle(firstLetterStyle);

        CalculatedStyle style = c.getCurrentStyle();
        Font font = FontUtil.getFont(c, style);
        InlineBox box = newBox(c, content, start, end, prev_align, font);
        // Uu.p("style = " + cs);
        styleInlineBox(c, style, box);
        box.break_after = false;
        //Uu.p("generated a first letter inline: " + box);
        c.popStyle();
        return box;
    } */

    /* not used public static void styleInlineBox(Context c, CalculatedStyle style, InlineBox box) {
        box.color = style.getColor();
        TextDecoration.setupTextDecoration(style, box);
        Font font = FontUtil.getFont(c, style);
        box.setFont(font);
        box.width = FontUtil.len(c, box.getSubstring(), font);
        box.height = FontUtil.lineHeight(c, style, box);
        VerticalAlign.setupVerticalAlign(c, style, box);
    } */
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.42  2004/12/29 10:39:33  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.41  2004/12/28 02:15:18  tobega
 * More cleaning.
 *
 * Revision 1.40  2004/12/27 09:40:47  tobega
 * Moved more styling to render stage. Now inlines have backgrounds and borders again.
 *
 * Revision 1.39  2004/12/27 07:43:31  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.38  2004/12/15 00:53:40  tobega
 * Started playing a bit with inline box, provoked a few nasties, probably created some, seems to work now
 *
 * Revision 1.37  2004/12/12 21:02:37  tobega
 * Images working again
 *
 * Revision 1.36  2004/12/12 04:18:57  tobega
 * Now the core compiles at least. Now we must make it work right. Table layout is one point that really needs to be looked over
 *
 * Revision 1.35  2004/12/12 03:32:59  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.34  2004/12/12 03:18:34  tobega
 * Making progress
 *
 * Revision 1.33  2004/12/11 23:36:49  tobega
 * Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.32  2004/12/11 21:14:48  tobega
 * Prepared for handling run-in content (OK, I know, a side-track). Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
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

