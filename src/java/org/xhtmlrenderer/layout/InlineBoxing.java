/*
* {{{ header & license
* Copyright (c) 2004 Joshua Marinacci, Torbjšrn Gannholm
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

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.block.Absolute;
import org.xhtmlrenderer.layout.content.*;
import org.xhtmlrenderer.layout.inline.*;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Description of the Class
 *
 * @author empty
 */
public class InlineBoxing {

    public static void layoutContent(Context c, Box box, List contentList) {
        // Uu.p("+ InlineLayout.layoutContent(): " + box);
        Rectangle bounds = new Rectangle();
        bounds.width = c.getExtents().width;
        Border border = LayoutUtil.getBorder(box, c.getCurrentStyle());
        Border margin = c.getCurrentStyle().getMarginWidth();
        Border padding = c.getCurrentStyle().getPaddingWidth();
        //below should maybe be done somewhere else?
        bounds.width -= margin.left + border.left + padding.left +
                padding.right + border.right + margin.right;
        validateBounds(bounds);
        bounds.x = 0;
        bounds.y = 0;
        bounds.height = 0;

        // prepare remaining width and first linebox
        int remaining_width = bounds.width;
        LineBox curr_line = newLine(box, bounds, null);
        c.setFirstLine(true);


        // account for text-indent
        CalculatedStyle parentStyle = c.getCurrentStyle();
        remaining_width = TextIndent.doTextIndent(parentStyle, remaining_width, curr_line);

        // more setup
        LineBox prev_line = new LineBox();
        prev_line.setParent(box);
        prev_line.y = bounds.y;
        prev_line.height = 0;
        InlineBox prev_inline = null;
        InlineBox prev_align_inline = null;

        // adjust the first line for float tabs
        remaining_width = FloatUtil.adjustForTab(c, prev_line, remaining_width);

        CalculatedStyle currentStyle = parentStyle;
        boolean isFirstLetter = true;

        List pendingPushStyles = null;
        // loop until no more nodes
        while (contentList.size() > 0) {
            Object o = contentList.get(0);
            contentList.remove(0);
            if (o instanceof FirstLineStyle) {//can actually only be the first object in list
                box.firstLineStyle = ((FirstLineStyle) o).getStyle();
                continue;
            }
            if (o instanceof FirstLetterStyle) {//can actually only be the first or second object in list
                box.firstLetterStyle = ((FirstLetterStyle) o).getStyle();
                continue;
            }
            if (o instanceof StylePush) {
                c.pushStyle(((StylePush) o).getStyle());
                if (pendingPushStyles == null) pendingPushStyles = new LinkedList();
                pendingPushStyles.add((StylePush) o);
                continue;
            }
            if (o instanceof StylePop) {
                c.popStyle();
                if (pendingPushStyles != null && pendingPushStyles.size() != 0) {
                    pendingPushStyles.remove(pendingPushStyles.size() - 1);//was a redundant one
                } else {
                    if (prev_inline != null) {
                        if (prev_inline.popstyles == null) prev_inline.popstyles = new LinkedList();
                        prev_inline.popstyles.add(o);
                    }
                }
                continue;
            }
            Content currentContent = (Content) o;
            if (currentContent.getStyle() != null) c.pushStyle(currentContent.getStyle());

            // loop until no more text in this node
            while (true) {

                if (currentContent instanceof AbsolutelyPositionedContent) {
                    // Uu.p("this might be a problem, but it could just be an absolute block");
                    //     result = new BoxLayout().layout(c,content);
                    Box absolute = Absolute.generateAbsoluteBox(c, currentContent);
                    curr_line.addChild(absolute);
                    break;
                }

                // debugging check
                if (bounds.width < 0) {
                    Uu.p("bounds width = " + bounds.width);
                    Uu.dump_stack();
                    System.exit(-1);
                }

                // the crash warning code
                if (bounds.width < 1) {
                    Uu.p("warning. width < 1 " + bounds.width);
                }

                // test if there is no more text in the current text node
                // if there is a prev, and if the prev was part of this current node
                if (prev_inline != null && prev_inline.content == currentContent) {
                    if (isEndOfBlock(prev_inline, currentContent)) {
                        break;
                    }
                }

                currentStyle = c.getCurrentStyle();

                // look at current inline
                // break off the longest section that will fit
                InlineBox new_inline = calculateInline(c, currentContent, remaining_width, bounds.width,
                        prev_inline, prev_align_inline, isFirstLetter, box.firstLetterStyle, box.firstLineStyle);
                // Uu.p("got back inline: " + new_inline);
                
                // if this inline needs to be on a new line
                if (prev_align_inline != null && new_inline.break_before && !new_inline.floated) {
                    // Uu.p("break before");
                    remaining_width = bounds.width;
                    saveLine(curr_line, currentStyle, prev_line, bounds.width, bounds.x, c, box, false);
                    bounds.height += curr_line.height;
                    prev_line = curr_line;
                    curr_line = newLine(box, bounds, prev_line);
                    remaining_width = FloatUtil.adjustForTab(c, curr_line, remaining_width);
                    //have to discard it and recalculate, particularly if this was the first line
                    //HACK: is my thinking straight? - tobe
                    prev_align_inline.break_after = true;
                    continue;
                }

                // save the new inline to the list
                // Uu.p("adding inline child: " + new_inline);
                //the inline might be discarded after this, if it is first whitespace on line.
                curr_line.addInlineChild(c, new_inline);

                // skipp empty inlines
                if (isNormalInline(currentContent)) {
                    if (new_inline.getSubstring().equals("")) break;
                }
                // Uu.p("current line = " + curr_line);

                isFirstLetter = false;
                new_inline.pushstyles = pendingPushStyles;
                pendingPushStyles = null;


                // calc new height of the line
                // don't count floats, absolutes, and inline-blocks
                if (isNormalInline(currentContent)) {
                    adjustLineHeight(curr_line, new_inline);
                }

                if (!(currentContent instanceof FloatedBlockContent)) {
                    // calc new width of the line
                    curr_line.width += new_inline.width;
                }
                // reduce the available width
                remaining_width = remaining_width - new_inline.width;

                // if the last inline was at the end of a line, then go to next line
                if (new_inline.break_after) {
                    // Uu.p("break after");
                    // then remaining_width = max_width
                    remaining_width = bounds.width;
                    // save the line
                    saveLine(curr_line, currentStyle, prev_line, bounds.width, bounds.x, c, box, false);
                    // increase bounds height to account for the new line
                    bounds.height += curr_line.height;
                    prev_line = curr_line;
                    curr_line = newLine(box, bounds, prev_line);
                    remaining_width = FloatUtil.adjustForTab(c, curr_line, remaining_width);
                }


                // set the inline to use for left alignment
                if (!isOutsideFlow(currentContent)) {
                    prev_align_inline = new_inline;
                    // }
                }

                prev_inline = new_inline;
            }

            if (currentContent.getStyle() != null) c.popStyle();

        }

        // save the final line
        saveLine(curr_line, currentStyle, prev_line, bounds.width, bounds.x, c, box, true);
        finishBlock(box, curr_line, bounds);
        // Uu.p("- InlineLayout.layoutContent(): " + box);
    }

    private static LineBox newLine(Box box, Rectangle bounds, LineBox prev_line) {
        LineBox curr_line = new LineBox();
        if (prev_line != null) {
            curr_line.setParent(prev_line.getParent());
        } else {
            curr_line.setParent(box);
        }
        curr_line.x = bounds.x;
        curr_line.width = 0;
        if (prev_line != null) {
            curr_line.y = prev_line.y + prev_line.height;
        }
        return curr_line;
    }


    private static void validateBounds(Rectangle bounds) {
        if (bounds.width <= 0) {
            bounds.width = 1;
            XRLog.exception("width < 1");
        }
    }


    public static void adjustLineHeight(LineBox curr_line, InlineBox new_inline) {
        // Uu.p("calcing new height of line");
        if (new_inline.height + new_inline.y > curr_line.height) {
            curr_line.height = new_inline.height + new_inline.y;
        }
        if (new_inline.baseline > curr_line.baseline) {
            curr_line.baseline = new_inline.baseline;
        }
    }


    private static boolean isEndOfBlock(InlineBox prev_inline, Content content) {
        // replaced elements aren't split, so done with this one
        if (content instanceof InlineBlockContent) {
            return true;
        }
        if (content instanceof FloatedBlockContent) {
            return true;
        }
        if (content instanceof AbsolutelyPositionedContent) {
            return true;
        }
        /*if (c.getRenderingContext().getLayoutFactory().isBreak(current_node)) {//not needed with content
            return true;
        }*/
        // if no more unused text in this node
        // Uu.p("looking for skip to next node");
        if (prev_inline.end_index >= prev_inline.getMasterText().length()) {
            return true;
        }
        return false;
    }

    public static void finishBlock(Box block, LineBox curr_line, Rectangle bounds) {
        bounds.height += curr_line.height;
        block.width = bounds.width;
        block.height = bounds.height;
        block.x = 0;
        block.y = 0;
    }


    public static boolean isNormalInline(Content currentContent) {
        if (!(currentContent instanceof InlineBlockContent)) {
            if (!(currentContent instanceof FloatedBlockContent)) {
                if (!(currentContent instanceof AbsolutelyPositionedContent)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isOutsideFlow(Content currentContent) {
        if (currentContent instanceof FloatedBlockContent) {
            return true;
        }
        if (currentContent instanceof AbsolutelyPositionedContent) {
            return true;
        }
        return false;
    }

    /**
     * Get the longest inline possible.
     *
     * @param c                PARAM
     * @param content
     * @param avail            PARAM
     * @param max_width        PARAM
     * @param prev             PARAM
     * @param prev_align       PARAM
     * @param isFirstLetter
     * @param firstLetterStyle
     * @param firstLineStyle
     * @return Returns
     */
    private static InlineBox calculateInline(Context c, Content content, int avail, int max_width,
                                             InlineBox prev, InlineBox prev_align, boolean isFirstLetter, CascadedStyle firstLetterStyle, CascadedStyle firstLineStyle) {

        if (c.isFirstLine() && firstLineStyle != null) c.pushStyle(firstLineStyle);

        CalculatedStyle style = c.getCurrentStyle();
        // get the current font. required for sizing
        Font font = FontUtil.getFont(c, style);
        InlineBox result;

        // handle each case
        if (content instanceof InlineBlockContent) {
            //Uu.p("is replaced");
            result = LineBreaker.generateReplacedInlineBox(c, content, avail, prev_align, font);
        } else if (content instanceof FloatedBlockContent) {
            //Uu.p("calcinline: is floated block");
            result = FloatUtil.generateFloatedBlockInlineBox(c, content, avail, prev_align, font);
        } else {

            //OK, now we should have only TextContent left, fail fast if not
            // Uu.p("real content = " + content);
            TextContent textContent = (TextContent) content;

            // calculate the starting index
            int start = 0;
            // if this is another box from the same node as the previous one
            if (prev != null && prev.content == textContent) {
                start = prev.end_index;
            }

            //might need to transform it
            // get the text of the node
            String text = textContent.getText();

            // transform the text if required (like converting to caps)
            // this must be done before any measuring since it might change the
            // size of the text
            //Uu.p("text from the node = \"" + text + "\"");
            text = TextUtil.transformText(text, style);
            int end = text.length();

            InlineBox inline = new InlineBox();
            inline.content = textContent;

            //Here we must set MasterText, it might have been restyled
            inline.setMasterText(text);

            // Uu.p("calculating inline: text = " + text);
            // Uu.p("avail space = " + avail + " max = " + max_width + "   start index = " + start);

            //CHECK:what's so very differnt between a first-letter box and another box? Can't we create them equal?
            if (isFirstLetter && firstLetterStyle != null) {
                //TODO: what if first letter is whitespace?
                end = start + 1;
                inline.setSubstring(start, end);
                c.pushStyle(firstLetterStyle);

                CalculatedStyle style1 = c.getCurrentStyle();
                Font font1 = FontUtil.getFont(c, style1);
                inline.whitespace = WhitespaceStripper.getWhitespace(style1);
                BoxBuilder.prepBox(c, inline, prev_align, font1);
                c.popStyle();
                result = inline;

                /*InlineBox box = LineBreaker.newBox(c, textContent, start, end, prev_align, font1);
                LineBreaker.styleInlineBox(c, style1, box);
                box.break_after = false;
                c.popStyle();
                result = box;*/
            } else {
                inline.setSubstring(start, end);
                /*InlineBox inline = new InlineBox();
                inline.content = textContent; */
                CalculatedStyle style1 = c.getCurrentStyle();
                inline.whitespace = WhitespaceStripper.getWhitespace(style1);
                Breaker.breakText(c, inline, prev_align, avail, max_width, font);
                BoxBuilder.prepBox(c, inline, prev_align, font);
                result = inline;
            }
        }
        if (c.isFirstLine() && firstLineStyle != null) c.popStyle();
        return result;
    }


    /**
     * Description of the Method
     *
     * @param line_to_save PARAM
     * @param style
     * @param prev_line    PARAM
     * @param width        PARAM
     * @param x            PARAM
     * @param c            PARAM
     * @param block        PARAM
     */
    private static void saveLine(LineBox line_to_save, CalculatedStyle style, LineBox prev_line, int width, int x,
                                 Context c, Box block, boolean last) {
        c.setFirstLine(false);
        // account for text-align
        TextAlign.adjustTextAlignment(c, style, line_to_save, width, x, last);
        // set the y
        line_to_save.y = prev_line.y + prev_line.height;
        
        // new float code
        line_to_save.x += c.getBlockFormattingContext().getLeftFloatDistance(line_to_save);

        // Uu.p("before line = " + line_to_save);
        VerticalAlign.setupVerticalAlign(line_to_save);
        // Uu.p("finished line = " + line_to_save);
        block.addChild(line_to_save);
    }

}

/*
* $Id$
*
* $Log$
* Revision 1.2  2005/01/03 00:25:33  tobega
* Managed to add some form support
*
* Revision 1.1  2005/01/02 09:32:41  tobega
* Now using mostly static methods for layout
*
* Revision 1.77  2005/01/02 01:00:09  tobega
* Started sketching in code for handling replaced elements in the NamespaceHandler
*
* Revision 1.76  2005/01/01 23:38:38  tobega
* Cleaned out old rendering code
*
* Revision 1.75  2005/01/01 22:37:43  tobega
* Started adding in the table support.
*
* Revision 1.74  2004/12/29 10:39:33  tobega
* Separated current state Context into ContextImpl and the rest into SharedContext.
*
* Revision 1.73  2004/12/28 01:48:24  tobega
* More cleaning. Magically, the financial report demo is starting to look reasonable, without any effort being put on it.
*
* Revision 1.72  2004/12/27 09:40:47  tobega
* Moved more styling to render stage. Now inlines have backgrounds and borders again.
*
* Revision 1.71  2004/12/27 07:43:31  tobega
* Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
*
* Revision 1.70  2004/12/26 10:14:45  tobega
* Starting to get some semblance of order concerning floats. Still needs more work.
*
* Revision 1.69  2004/12/24 11:59:25  tobega
* Starting to get some semblance of order concerning floats. Still needs more work.
*
* Revision 1.68  2004/12/24 08:46:49  tobega
* Starting to get some semblance of order concerning floats. Still needs more work.
*
* Revision 1.67  2004/12/21 20:20:28  tobega
* More hack to make Alice look ok at least
*
* Revision 1.66  2004/12/21 06:58:40  tobega
* Fixed bug in WhitespaceStripper. Started a hack to handle floats better, but it didn't solve everything, we need to think more about handling floats.
*
* Revision 1.65  2004/12/20 23:25:31  tobega
* Cleaned up handling of absolute boxes and went back to correct use of anonymous boxes in ContentUtil
*
* Revision 1.64  2004/12/16 17:33:15  joshy
* moved back to abs pos content
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.63  2004/12/16 17:22:25  joshy
* minor code cleanup
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.62  2004/12/16 15:53:08  joshy
* fixes for absolute layout
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.61  2004/12/15 00:53:40  tobega
* Started playing a bit with inline box, provoked a few nasties, probably created some, seems to work now
*
* Revision 1.60  2004/12/14 02:28:48  joshy
* removed some comments
* some bugs with the backgrounds still
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.59  2004/12/14 02:18:30  tobega
* house-cleaning
*
* Revision 1.58  2004/12/14 01:56:23  joshy
* fixed layout width bugs
* fixed extra border on document bug
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.57  2004/12/14 01:50:13  tobega
* Why is there always one more bug ;-) Now line-breaking should be cast-iron (I hope)
*
* Revision 1.56  2004/12/14 00:32:20  tobega
* Cleaned and fixed line breaking. Renamed BodyContent to DomToplevelNode
*
* Revision 1.55  2004/12/13 00:13:11  tobega
* Oops, happened to remove images as well as empty text, fixed.
*
* Revision 1.54  2004/12/12 23:45:47  tobega
* Discard inline boxes containing empty strings.
*
* Revision 1.53  2004/12/12 23:19:25  tobega
* Tried to get hover working. Something happens, but not all that's supposed to happen.
*
* Revision 1.52  2004/12/12 21:02:37  tobega
* Images working again
*
* Revision 1.51  2004/12/12 18:06:51  tobega
* Made simple layout (inline and box) a bit easier to understand
*
* Revision 1.50  2004/12/12 05:51:48  tobega
* Now things run. But there is a lot to do before it looks as nice as it did. At least we now have :before and :after content and handling of breaks by css.
*
* Revision 1.49  2004/12/12 03:32:58  tobega
* Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
*
* Revision 1.48  2004/12/12 03:29:41  tobega
* Oops, this is a real mess. CVS got into a twist on this one.
*
* Revision 1.47  2004/12/12 03:17:19  tobega
* Making progress
*
* Revision 1.46  2004/12/11 23:36:48  tobega
* Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
*
* Revision 1.45  2004/12/11 21:14:48  tobega
* Prepared for handling run-in content (OK, I know, a side-track). Still broken, won't even compile at the moment. Working hard to fix it, though.
*
* Revision 1.44  2004/12/11 18:18:11  tobega
* Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
*
* Revision 1.43  2004/12/10 06:51:02  tobega
* Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
*
* Revision 1.42  2004/12/09 21:18:52  tobega
* precaution: code still works
*
* Revision 1.41  2004/12/09 00:11:51  tobega
* Almost ready for Content-based inline generation.
*
* Revision 1.40  2004/12/08 00:42:34  tobega
* More cleaning of use of Node, more preparation for Content-based inline generation. Also fixed 2 irritating bugs!
*
* Revision 1.39  2004/12/06 02:55:43  tobega
* More cleaning of use of Node, more preparation for Content-based inline generation.
*
* Revision 1.38  2004/12/06 00:19:15  tobega
* Worked on handling :before and :after. Got sidetracked by BasicPanel causing layout to be done twice: solved. If solution causes problems, check BasicPanel.setSize
*
* Revision 1.37  2004/12/05 18:11:38  tobega
* Now uses style cache for pseudo-element styles. Also started preparing to replace inline node handling with inline content handling.
*
* Revision 1.36  2004/12/05 14:35:39  tobega
* Cleaned up some usages of Node (and removed unused stuff) in layout code. The goal is to pass "better" objects than Node wherever possible in an attempt to shake out the bugs in tree-traversal (probably often unnecessary tree-traversal)
*
* Revision 1.35  2004/12/05 00:48:57  tobega
* Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
*
* Revision 1.34  2004/12/01 01:57:00  joshy
* more updates for float support.
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.33  2004/11/27 15:46:38  joshy
* lots of cleanup to make the code clearer
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.32  2004/11/23 03:06:21  joshy
* fixed floating support
*
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.31  2004/11/23 02:41:59  joshy
* fixed vertical-align support for first-letter pseudos
* tested first-line w/ new breaking routines
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.30  2004/11/23 02:11:24  joshy
* re-enabled text-decoration
* moved it to it's own class
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.29  2004/11/23 01:53:29  joshy
* re-enabled vertical align
* added unit tests for various text-align and indent forms
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.28  2004/11/22 21:34:03  joshy
* created new whitespace handler.
* new whitespace routines only work if you set a special property. it's
* off by default.
*
* turned off fractional font metrics
*
* fixed some bugs in Uu and Xx
*
* - j
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.27  2004/11/18 18:49:49  joshy
* fixed the float issue.
* commented out more dead code
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.26  2004/11/18 16:45:11  joshy
* improved the float code a bit.
* now floats are automatically forced to be blocks
*
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.25  2004/11/18 02:37:26  joshy
* moved most of default layout into layout util or box layout
*
* start spliting parts of box layout into the block subpackage
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.24  2004/11/15 14:33:09  joshy
* fixed line breaking bug with certain kinds of unbreakable lines
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.23  2004/11/14 16:40:58  joshy
* refactored layout factory
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.22  2004/11/14 06:26:39  joshy
* added better detection for width problems. should avoid most
* crashes
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.21  2004/11/12 20:25:18  joshy
* added hover support to the browser
* created hover demo
* fixed bug with inline borders
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.20  2004/11/09 16:41:33  joshy
* moved text alignment code
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.19  2004/11/09 16:24:29  joshy
* moved float code into separate class
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.18  2004/11/09 16:07:57  joshy
* moved vertical align code
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.17  2004/11/09 15:53:48  joshy
* initial support for hover (currently disabled)
* moved justification code into it's own class in a new subpackage for inline
* layout (because it's so blooming complicated)
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.16  2004/11/09 02:04:23  joshy
* support for text-align: justify
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.15  2004/11/08 20:50:59  joshy
* improved float support
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.14  2004/11/08 16:56:51  joshy
* added first-line pseudo-class support
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.13  2004/11/08 15:10:10  joshy
* added support for styling :first-letter inline boxes
* updated the absolute positioning tests
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.12  2004/11/06 22:51:57  joshy
* removed dead code
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.11  2004/11/06 22:49:52  joshy
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
* Revision 1.10  2004/11/05 16:39:34  joshy
* more float support
* added border bug test
* -j
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.9  2004/11/04 15:35:45  joshy
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
* Revision 1.8  2004/11/03 23:54:33  joshy
* added hamlet and tables to the browser
* more support for absolute layout
* added absolute layout unit tests
* removed more dead code and moved code into layout factory
*
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.7  2004/11/02 20:44:56  joshy
* put in some prep work for float support
* removed some dead debugging code
* moved isBlock code to LayoutFactory
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.6  2004/10/28 13:46:32  joshy
* removed dead code
* moved code about specific elements to the layout factory (link and br)
* fixed form rendering bug
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.5  2004/10/27 13:39:56  joshy
* moved more rendering code out of the layouts
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.4  2004/10/27 02:00:19  joshy
* removed double spacing from inline layout
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
