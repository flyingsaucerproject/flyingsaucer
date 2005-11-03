/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjšrn Gannholm
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

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.block.Absolute;
import org.xhtmlrenderer.layout.content.*;
import org.xhtmlrenderer.layout.inline.Breaker;
import org.xhtmlrenderer.layout.inline.FloatUtil;
import org.xhtmlrenderer.layout.inline.VerticalAlign;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Description of the Class
 *
 * @author empty
 */
public class InlineBoxing {

    /**
     * Description of the Method
     *
     * @param c           PARAM
     * @param box         PARAM
     * @param contentList PARAM
     */
    public static void layoutContent(LayoutContext c, Box box, List contentList) {
        //Here we should always be inside something that corresponds to a block-level element
        //for formatting purposes
        Rectangle bounds = new Rectangle();
        bounds.width = c.getExtents().width;
        validateBounds(bounds);
        bounds.x = 0;
        bounds.y = 0;
        bounds.height = 0;

        //dummy style to make sure that text nodes don't get extra padding and such
        //doesn't work here because blocks may be inside inlines, losing inline styling:
        //c.pushStyle(CascadedStyle.emptyCascadedStyle);

        int blockLineHeight = FontUtil.lineHeight(box, c.getTextRenderer(), c.getGraphics(), c, c.getBlockFormattingContext());
        LineMetrics blockLineMetrics = c.getTextRenderer().getLineMetrics(c.getGraphics(),
                box.getStyle().getFont(c), "thequickbrownfoxjumpedoverthelazydogTHEQUICKBROWNFOXJUMPEDOVERTHELAZYDOG");

        // prepare remaining width and first linebox
        int remaining_width = bounds.width;
        LineBox curr_line = newLine(box, bounds, null, blockLineMetrics, c);
        List pushedOnFirstLine = null;

        // account for text-indent
        CalculatedStyle parentStyle = c.getCurrentStyle();
        float indent = parentStyle.getFloatPropertyProportionalWidth(CSSName.TEXT_INDENT, remaining_width, c);
        remaining_width = remaining_width - (int) indent;
        curr_line.x = curr_line.x + (int) indent;

        // more setup
        LineBox prev_line = new LineBox();
        c.pushStyle(CascadedStyle.emptyCascadedStyle);
        prev_line.setStyle(new Style(c.getCurrentStyle(), 0));
        c.popStyle();
        prev_line.setParent(box);
        prev_line.y = 0;
        prev_line.height = 0;
        InlineBox prev_inline = null;
        InlineBox prev_align_inline = null;

        // adjust the first line for float tabs
        // skip adjusting for tabs if this box is cleared
        if (!box.getStyle().isCleared()) {
            remaining_width = FloatUtil.adjustForTab(c, prev_line, remaining_width);
        }

        boolean isFirstLetter = true;

        int pendingPushStyles = 0;
        List pendingFloats = new ArrayList();
        int pendingLeftPadding = 0;
        int pendingRightPadding = 0;
        // loop until no more nodes
        Iterator contentIter = contentList.iterator();
        while (contentIter.hasNext() && !c.shouldStop()) {
            Object o = contentIter.next();
            if (o instanceof FirstLineStyle) {//can actually only be the first object in list
                box.firstLineStyle = ((FirstLineStyle) o).getStyle();
                c.addFirstLineStyle(box.firstLineStyle);
                continue;
            }
            if (o instanceof FirstLetterStyle) {//can actually only be the first or second object in list
                box.firstLetterStyle = ((FirstLetterStyle) o).getStyle();
                continue;
            }
            if (o instanceof StylePush) {
                CascadedStyle cascaded;
                StylePush sp = (StylePush) o;
                if (sp.getElement() == null) {
                    //anonymous inline box
                    cascaded = CascadedStyle.emptyCascadedStyle;
                } else if (sp.getPseudoElement() != null) {
                    cascaded = c.getCss().getPseudoElementStyle(sp.getElement(), sp.getPseudoElement());
                } else {
                    cascaded = c.getCss().getCascadedStyle(sp.getElement(), false);//already restyled by ContentUtil
                }
                //first push first-line styles
                if (c.hasFirstLineStyles()) {
                    if (pushedOnFirstLine == null) {
                        pushedOnFirstLine = new ArrayList();
                        for (Iterator i = c.getFirstLineStyles().iterator(); i.hasNext();) {
                            c.pushStyle((CascadedStyle) i.next());
                        }
                    }
                    pushedOnFirstLine.add(cascaded);
                }
                c.pushStyle(cascaded);
                /*if (pendingPushStyles == null) {
                    pendingPushStyles = new ArrayList();
                }
                pendingPushStyles.add((StylePush) o);*/
                pendingPushStyles++;
                //Relative.translateRelative(c, c.getCurrentStyle(), false);
                CalculatedStyle style = c.getCurrentStyle();
                int parent_width = bounds.width;
                c.setCurrentInlineElement(new InlineElement(((StylePush) o).getElement(), ((StylePush) o).getPseudoElement(), c.getCurrentInlineElement()));
                c.getCurrentInlineElement().setStartStyle(new Style(style, parent_width));
                //To break the line well, assume we don't just want to paint padding on next line
                BorderPropertySet border = style.getBorder(c);
                //note: percentages here refer to width of containing block
                RectPropertySet margin = style.getMarginRect(parent_width, parent_width, c);
                RectPropertySet padding = style.getPaddingRect(parent_width, parent_width, c);
                pendingLeftPadding += margin.left() + (int) border.left() + (int) padding.left();
                pendingRightPadding += (int) padding.right() + (int) border.right() + margin.right();
                continue;
            }
            if (o instanceof StylePop) {
                if (pendingPushStyles != 0) {
                    //if (pendingPushStyles != null && pendingPushStyles.size() != 0) {
                    //pendingPushStyles.remove(pendingPushStyles.size() - 1);//was a redundant one
                    pendingPushStyles--;
                    //TODO: is it correct that empty inline elements get ignored?
                    //correct the pending padding
                    CalculatedStyle style = c.getCurrentStyle();
                    int parent_width = bounds.width;
                    BorderPropertySet border = style.getBorder(c);
                    //note: percentages here refer to width of containing block
                    RectPropertySet margin = style.getMarginRect(parent_width, parent_width, c);
                    RectPropertySet padding = style.getPaddingRect(parent_width, parent_width, c);
                    pendingLeftPadding -= margin.left() + (int) border.left() + (int) padding.left();
                    pendingRightPadding -= (int) padding.right() + (int) border.right() + margin.right();
                } else {
                    if (prev_inline == null) {//only when there was a block child just before element end
                        prev_inline = new InlineTextBox();//hope it is decently initialised as empty
                        ((InlineTextBox) prev_inline).setMasterText("");
                        ((InlineTextBox) prev_inline).start_index = 0;
                        ((InlineTextBox) prev_inline).end_index = 0;
                        curr_line.addChild(prev_inline);
                        prev_inline.setInlineElement(c.getCurrentInlineElement());
                    }
                    prev_inline.popstyles++;
                    int parent_width = bounds.width;
                    CalculatedStyle style = c.getCurrentStyle();
                    c.getCurrentInlineElement().setEndStyle(new Style(style, parent_width));
                    c.setCurrentInlineElement(c.getCurrentInlineElement().getParent());
                    BorderPropertySet border = style.getBorder(c);
                    //note: percentages here refer to width of containing block
                    RectPropertySet margin = style.getMarginRect(parent_width, parent_width, c);
                    RectPropertySet padding = style.getPaddingRect(parent_width, parent_width, c);
                    // CLEAN: cast to int
                    int rp = (int) padding.right() + (int) border.right() + (int) margin.right();
                    //CHECK: not sure this is where the padding really goes, always
                    prev_inline.rightPadding += rp;

                    pendingRightPadding -= rp;
                    remaining_width -= rp;
                }
                if (c.hasFirstLineStyles()) {
                    pushedOnFirstLine.remove(pushedOnFirstLine.size() - 1);
                }
                //Relative.untranslateRelative(c, c.getCurrentStyle(), false);
                c.popStyle();
                continue;
            }
            Content currentContent = (Content) o;

            if (currentContent.getStyle() != null) {
                //first push first-line styles
                if (c.hasFirstLineStyles()) {
                    if (pushedOnFirstLine == null) {
                        pushedOnFirstLine = new ArrayList();
                        for (Iterator i = c.getFirstLineStyles().iterator(); i.hasNext();) {
                            c.pushStyle((CascadedStyle) i.next());
                        }
                    }
                    pushedOnFirstLine.add(currentContent.getStyle());
                }
                c.pushStyle(currentContent.getStyle());
            }

            // loop until no more text in this node
            InlineBox new_inline = null;
            int start = 0;
            InlineBlockBox pendingBlockBox = null;
            do {
                new_inline = null;
                if (currentContent instanceof AbsolutelyPositionedContent) {
                    // Uu.p("this might be a problem, but it could just be an absolute block");
                    //     result = new BoxLayout().layout(c,content);
                    BlockBox absolute = Absolute.generateAbsoluteBox(c, currentContent);
                    //curr_line.setChildrenExceedBounds(true);
                    //curr_line.addChild(absolute);
                    absolute.setParent(c.getBlockFormattingContext().getMaster());
                    c.getStackingContext().addAbsolute(absolute);
                    break;
                } else if (currentContent instanceof FloatedBlockContent) {
                    //Uu.p("calcinline: is floated block");
                    FloatedBlockBox floater = FloatUtil.generateFloatedBlock(c, currentContent, remaining_width, curr_line, pendingFloats);
                    if (! floater.isPending()) {
                        remaining_width -= floater.getWidth();
                    }

                    c.getStackingContext().addFloat(floater);

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

                // look at current inline
                // break off the longest section that will fit
                int fit = pendingRightPadding;//Note: this is not necessarily entirely correct
                if (start == 0) fit += pendingLeftPadding;
                start = trimLeadingSpace(c.getCurrentStyle(), prev_inline, currentContent, start);
                new_inline = calculateInline(c, currentContent, remaining_width - fit, bounds.width,
                        prev_align_inline, isFirstLetter, box.firstLetterStyle,
                        curr_line, start, pendingBlockBox, pendingFloats);

                pendingBlockBox = null;

                // if this inline needs to be on a new line
                if (prev_align_inline != null && new_inline.break_before) {
                    // Uu.p("break before");
                    remaining_width = bounds.width;
                    saveLine(curr_line, prev_line, bounds, c, box, blockLineHeight, pushedOnFirstLine, pendingFloats);
                    bounds.height += curr_line.height;
                    prev_line = curr_line;
                    curr_line = newLine(box, bounds, prev_line, blockLineMetrics, c);
                    // skip adjusting for tabs if this box is cleared
                    if (!box.getStyle().isCleared()) {
                        remaining_width = FloatUtil.adjustForTab(c, curr_line, remaining_width);
                    }
                    
                    //have to discard it and recalculate, particularly if this was the first line
                    prev_align_inline.break_after = true;
                    if (new_inline instanceof InlineBlockBox) pendingBlockBox = (InlineBlockBox) new_inline;
                    new_inline = null;
                    continue;
                }

                // save the new inline to the list
                // Uu.p("adding inline child: " + new_inline);
                //the inline might be set to size 0,0 after this, if it is first whitespace on line.
                // Cannot discard because it may contain style-pushes
                if (!(currentContent instanceof FloatedBlockContent)) {
                    curr_line.addInlineChild(new_inline);
                }
                
                // Uu.p("current line = " + curr_line);
                if (new_inline instanceof InlineTextBox) {
                    start = ((InlineTextBox) new_inline).end_index;
                }

                isFirstLetter = false;
                new_inline.pushstyles = pendingPushStyles;
                pendingPushStyles = 0;
                new_inline.leftPadding += pendingLeftPadding;
                pendingLeftPadding = 0;

                // calc new height of the line
                // don't count floats and absolutes
                if (!new_inline.getStyle().isFloated() && !new_inline.getStyle().isAbsolute()) {
                    adjustLineHeight(c, curr_line, new_inline);
                }

                // calc new width of the line
                curr_line.contentWidth += new_inline.getWidth();
                // reduce the available width
                remaining_width = remaining_width - new_inline.getWidth();

                // if the last inline was at the end of a line, then go to next line
                if (new_inline.break_after) {
                    // Uu.p("break after");
                    // then remaining_width = max_width
                    remaining_width = bounds.width;
                    // save the line
                    saveLine(curr_line, prev_line, bounds, c, box, blockLineHeight, pushedOnFirstLine, pendingFloats);
                    // increase bounds height to account for the new line
                    bounds.height += curr_line.height;
                    prev_line = curr_line;
                    curr_line = newLine(box, bounds, prev_line, blockLineMetrics, c);
                    if (!box.getStyle().isCleared()) {
                        remaining_width = FloatUtil.adjustForTab(c, curr_line, remaining_width);
                    }
                }

                // set the inline to use for left alignment
                if (!isOutsideFlow(currentContent)) {//is this necessary now?
                    prev_align_inline = new_inline;
                }

                prev_inline = new_inline;
            } while (!c.shouldStop() && (new_inline == null || !new_inline.isEndOfParentContent()));

            if (currentContent.getStyle() != null) {
                c.popStyle();
                if (c.hasFirstLineStyles()) {
                    pushedOnFirstLine.remove(pushedOnFirstLine.size() - 1);
                }
            }
        }

        // save the final line
        saveLine(curr_line, prev_line, bounds, c, box, blockLineHeight, pushedOnFirstLine, pendingFloats);
        bounds.height += curr_line.height;
        if (!c.shrinkWrap()) box.contentWidth = bounds.width;
        box.height = bounds.height;
        //box.x = 0;
        //box.y = 0;
        // Uu.p("- InlineLayout.layoutContent(): " + box);
        //pop the dummy style, but no, see above
        //c.popStyle();
    }


    private static int trimLeadingSpace(CalculatedStyle style, InlineBox prev_inline,
                                        Content currentContent, int start) {
        if ((prev_inline == null || (prev_inline != null && prev_inline.break_after)) && currentContent instanceof TextContent) {
            TextContent textContent = (TextContent) currentContent;
            if (textContent.getText().substring(start).startsWith(WhitespaceStripper.SPACE)) {
                IdentValue whitespace = style.getIdent(CSSName.WHITE_SPACE);
                if (whitespace == IdentValue.NORMAL || whitespace == IdentValue.NOWRAP || whitespace == IdentValue.PRE) {
                    start++;
                }
            }
        }
        return start;
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

    private static LineBox newLine(Box box, Rectangle bounds, LineBox prev_line, LineMetrics blockLineMetrics, LayoutContext c) {
        LineBox curr_line = new LineBox();
        c.pushStyle(CascadedStyle.emptyCascadedStyle);
        curr_line.setStyle(new Style(c.getCurrentStyle(), 0));
        c.popStyle();
        //if (Configuration.isTrue("xr.stackingcontext.enabled", false)) {
        curr_line.renderIndex = c.getNewRenderIndex();
        //}
        if (prev_line != null) {
            curr_line.setParent(prev_line.getParent());
        } else {
            curr_line.setParent(box);
        }
        curr_line.x = bounds.x;
        curr_line.contentWidth = 0;
        if (prev_line != null) {
            curr_line.y = prev_line.y + prev_line.height;
        }
        curr_line.blockLineMetrics = blockLineMetrics;
        return curr_line;
    }


    /**
     * Description of the Method
     *
     * @param bounds PARAM
     */
    private static void validateBounds(Rectangle bounds) {
        if (bounds.width <= 0) {
            bounds.width = 1;
            XRLog.exception("width < 1");
        }
    }


    /**
     * Description of the Method
     *
     * @param c          PARAM
     * @param curr_line  PARAM
     * @param new_inline PARAM
     */
    private static void adjustLineHeight(LayoutContext c, LineBox curr_line, InlineBox new_inline) {
        int lineHeight;
        int ascent;
        int descent;
        if (new_inline instanceof InlineTextBox && !((InlineTextBox) new_inline).getSubstring().equals("")) {
            // should be the metrics of the font, actually is the metrics of the text
            LineMetrics metrics = FontUtil.getLineMetrics(c, new_inline, c.getTextRenderer(), c.getGraphics());
            lineHeight = FontUtil.lineHeight(new_inline, c.getTextRenderer(), c.getGraphics(), c, c.getBlockFormattingContext());//assume that current context is valid for new_inline
            ascent = (int) metrics.getAscent();
            descent = (int) metrics.getDescent();
        } else {
            lineHeight = new_inline.height;//assume height for empty InlineTextBox is 0
            ascent = lineHeight;
            descent = 0;
        }

        if (lineHeight > curr_line.height) {
            curr_line.height = lineHeight;
        }

        int raised = VerticalAlign.getBaselineOffset(c, curr_line, new_inline, c.getTextRenderer(), c.getGraphics(), c.getBlockFormattingContext());

        if (ascent + raised > curr_line.ascent) {
            curr_line.ascent = ascent + raised;
        }
        if (descent - raised > curr_line.descent) {
            curr_line.descent = descent - raised;
        }

        if (curr_line.height < curr_line.ascent + curr_line.descent) {
            curr_line.height = curr_line.ascent + curr_line.descent;
        }
    }

    /**
     * Get the longest inline possible.
     */
    private static InlineBox calculateInline(LayoutContext c, Content content, int avail, int max_width,
                                             InlineBox prev_align, boolean isFirstLetter, CascadedStyle firstLetterStyle,
                                             LineBox curr_line, int start, InlineBlockBox pendingBlockBox,
                                             List pendingFloats) {
        InlineBox result = null;

        // handle each case
        if (content instanceof InlineBlockContent) {
            result = LineBreaker.generateReplacedInlineBox(c, content, avail, prev_align, curr_line, pendingBlockBox);
        } else {

            //OK, now we should have only TextContent left, fail fast if not
            // Uu.p("real content = " + content);
            TextContent textContent = (TextContent) content;

            //might need to transform it
            // get the text of the node
            String text = textContent.getText();

            // transform the text if required (like converting to caps)
            // this must be done before any measuring since it might change the
            // size of the text
            //Uu.p("text from the node = \"" + text + "\"");
            text = TextUtil.transformText(text, c.getCurrentStyle());
            int end = text.length();

            InlineTextBox inline = new InlineTextBox();
            inline.setStyle(new Style(c.getCurrentStyle(), max_width));
            inline.element = textContent.getElement();

            if (inline.element != null) {
                String id = c.getNamespaceHandler().getID(inline.element);
                if (id != null && !id.equals("")) {
                    c.addIDBox(id, inline);
                }
            }


            inline.pseudoElement = textContent.getPseudoElement();

            //Here we must set MasterText, it might have been restyled
            inline.setMasterText(text);

            // Uu.p("calculating inline: text = " + text);
            // Uu.p("avail space = " + avail + " max = " + max_width + "   setStartStyle index = " + setStartStyle);

            if (isFirstLetter && firstLetterStyle != null) {
                //TODO: what if first letter is whitespace?
                end = start + 1;
                inline.setSubstring(start, end);
                c.pushStyle(firstLetterStyle);
                inline.setStyle(new Style(c.getCurrentStyle(), max_width));
                prepareBox(prev_align, inline, c);
                BorderPropertySet border = c.getCurrentStyle().getBorder(c);
                //first letter is a pseudo-element, so has borders, etc.
                //note: percentages here refer to width of containing block
                RectPropertySet margin = c.getCurrentStyle().getMarginRect(max_width, max_width, c);
                RectPropertySet padding = c.getCurrentStyle().getPaddingRect(max_width, max_width, c);
                // CLEAN: cast to ints
                inline.rightPadding = (int) margin.right() + (int) border.right() + (int) padding.right();
                inline.leftPadding = (int) margin.left() + (int) border.left() + (int) padding.left();
                c.popStyle();
                result = inline;

            } else {
                inline.setSubstring(start, end);
                inline.whitespace = WhitespaceStripper.getWhitespace(c.getCurrentStyle());
                Breaker.breakText(c, inline, prev_align, avail, inline.getStyle().getFont(c));
                prepareBox(prev_align, inline, c);
            }
            result = inline;
        }

        return result;
    }

    private static void prepareBox(InlineBox prev_align, InlineTextBox inline, LayoutContext c) {
        if (prev_align != null &&
                !prev_align.break_after &&
                !inline.break_before
        ) {
            inline.x = prev_align.x + prev_align.getWidth();
        } else {
            inline.x = 0;

        }

        inline.y = 0;

        inline.contentWidth = (int) FontUtil.getTextBounds(c, inline, c.getTextRenderer(), c.getGraphics()).getWidth();
        inline.height = (int) FontUtil.getLineMetrics(c, inline, c.getTextRenderer(), c.getGraphics()).getHeight();
    }


    private static void saveLine(LineBox line_to_save, LineBox prev_line, Rectangle bounds,
                                 LayoutContext c, Box block, int minHeight, List pushedOnFirstLine,
                                 List pendingFloats) {
        if (c.hasFirstLineStyles()) {
            //first pop element styles pushed on first line
            for (int i = 0; i < pushedOnFirstLine.size(); i++) c.popStyle();
            //then pop first-line styles
            for (int i = 0; i < c.getFirstLineStyles().size(); i++) c.popStyle();
            //reinstate element styles
            for (Iterator i = pushedOnFirstLine.iterator(); i.hasNext();) {
                c.pushStyle((CascadedStyle) i.next());
            }
            c.clearFirstLineStyles();
        }
        if (c.shrinkWrap()) {
            //if (line_to_save.width > block.width) block.width = line_to_save.width;
            block.adjustWidthForChild(line_to_save.contentWidth);
        }
        //text-align now handled in render
        // set the y
        line_to_save.y = prev_line.y + prev_line.height;

        if (line_to_save.height != 0) {//would like to discard it otherwise, but that loses floats
            if (line_to_save.height < minHeight) {
                line_to_save.height = minHeight;
            }
        }
        block.addChild(line_to_save);

        if (pendingFloats.size() > 0) {
            c.setFloatingY(c.getFloatingY() + line_to_save.height);
            c.getBlockFormattingContext().floatPending(c, pendingFloats);
        }
        
        // new float code
        if (!block.getStyle().isClearLeft()) {
            line_to_save.x += c.getBlockFormattingContext().getLeftFloatDistance(c, line_to_save);
        }

        if (c.isPrint() && line_to_save.crossesPageBreak(c)) {
            line_to_save.moveToNextPage(c, bounds);
        }
        //if (Configuration.isTrue("xr.stackingcontext.enabled", false)) {
        //line should now be fixed
        Point origin = c.getOriginOffset();
        line_to_save.absY = origin.getY() + line_to_save.y;
        line_to_save.absX = origin.getX() + line_to_save.x;
        StackingContext s = c.getStackingContext();
        s.addLine(line_to_save);
        //}
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.58  2005/11/03 17:58:15  peterbrant
 * Float rewrite (still stomping bugs, but demos work)
 *
 * Revision 1.57  2005/11/02 23:36:45  tobega
 * InlineElement in place
 *
 * Revision 1.56  2005/11/02 19:59:59  peterbrant
 * Uncomment mistakenly commented out code
 *
 * Revision 1.55  2005/11/02 18:15:24  peterbrant
 * First merge of Tobe's and my stacking context work / Rework float code (not done yet)
 *
 * Revision 1.54  2005/11/01 23:49:23  tobega
 * Pulled floats and absolutes out of the "normal" rendering
 *
 * Revision 1.53  2005/10/30 00:02:35  peterbrant
 * - Minor cleanup to get rid of unused CssContext in Style constructor
 * - Switch to ArrayList from LinkedList in a few places (saves several MBs of memory on Hamlet)
 * - Introduce ScaledLineMetrics to work around apparent Java bug
 *
 * Revision 1.52  2005/10/29 00:58:03  tobega
 * Split out restyling from rendering and fixed up hovering
 *
 * Revision 1.51  2005/10/27 00:08:59  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.50  2005/10/23 22:16:42  tobega
 * Preparation for StackingContext rendering
 *
 * Revision 1.49  2005/10/23 18:37:43  tobega
 * possibly quicker stop when c.shouldStop()
 *
 * Revision 1.48  2005/10/21 18:10:51  pdoubleya
 * Support for cachable borders. Still buggy on some pages, but getting there.
 *
 * Revision 1.47  2005/10/21 13:17:12  pdoubleya
 * Rename some methods in RectPropertySet, cleanup.
 *
 * Revision 1.46  2005/10/21 13:02:22  pdoubleya
 * Changed to cache padding in RectPropertySet.
 *
 * Revision 1.45  2005/10/21 12:01:16  pdoubleya
 * Added cachable rect property for margin, cleanup minor in styling.
 *
 * Revision 1.44  2005/10/18 20:57:03  tobega
 * Patch from Peter Brant
 *
 * Revision 1.43  2005/10/16 23:57:15  tobega
 * Starting experiment with flat representation of render tree
 *
 * Revision 1.42  2005/10/12 21:17:12  tobega
 * patch from Peter Brant
 *
 * Revision 1.41  2005/10/08 17:40:20  tobega
 * Patch from Peter Brant
 *
 * Revision 1.40  2005/10/06 03:20:21  tobega
 * Prettier incremental rendering. Ran into more trouble than expected and some creepy crawlies and a few pages don't look right (forms.xhtml, splash.xhtml)
 *
 * Revision 1.39  2005/10/02 21:29:58  tobega
 * Fixed a lot of concurrency (and other) issues from incremental rendering. Also some house-cleaning.
 *
 * Revision 1.38  2005/09/29 21:34:03  joshy
 * minor updates to a lot of files. pulling in more incremental rendering code.
 * fixed another resize bug
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.37  2005/09/20 11:28:13  tobega
 * Knowledge of how to extract IDs belongs in the NamespaceHandler
 *
 * Revision 1.36  2005/07/18 17:53:31  joshy
 * fixed anchor jumping
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.35  2005/07/04 00:12:12  tobega
 * text-align now works for table-cells too (is done in render, not in layout)
 *
 * Revision 1.34  2005/06/22 23:48:45  tobega
 * Refactored the css package to allow a clean separation from the core.
 *
 * Revision 1.33  2005/06/16 18:34:10  joshy
 * support for clear:right
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.32  2005/06/16 07:24:50  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.31  2005/06/16 04:38:15  joshy
 * finished support for clear
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.30  2005/06/08 23:29:59  tobega
 * fixed a bug with first-line styles
 *
 * Revision 1.29  2005/06/08 19:01:56  tobega
 * Table cells get their preferred width
 *
 * Revision 1.28  2005/06/03 19:56:42  tobega
 * Now uses first-line styles from all block-level ancestors
 *
 * Revision 1.27  2005/06/01 00:47:04  tobega
 * Partly confused hack trying to get width and height working properly for replaced elements.
 *
 * Revision 1.26  2005/05/31 01:40:06  tobega
 * Replaced elements can now be display: block;
 * display: inline-block; should be working even for non-replaced elements.
 *
 * Revision 1.25  2005/05/17 06:56:24  tobega
 * Inline backgrounds now work correctly, as does mixing of inlines and blocks for style inheritance
 *
 * Revision 1.24  2005/05/16 13:48:59  tobega
 * Fixe inline border mismatch and started on styling problem in switching between blocks and inlines
 *
 * Revision 1.23  2005/05/13 15:23:54  tobega
 * Done refactoring box borders, margin and padding. Hover is working again.
 *
 * Revision 1.22  2005/05/13 11:49:58  tobega
 * Started to fix up borders on inlines. Got caught up in refactoring.
 * Boxes shouldn't cache borders and stuff unless necessary. Started to remove unnecessary references.
 * Hover is not working completely well now, might get better when I'm done.
 *
 * Revision 1.21  2005/05/09 23:47:14  tobega
 * Cleaned up some getting of LineMetrics and optimized InlineRendering
 *
 * Revision 1.20  2005/05/08 14:36:57  tobega
 * Refactored away the need for having a context in a CalculatedStyle
 *
 * Revision 1.19  2005/05/08 13:02:40  tobega
 * Fixed a bug whereby styles could get lost for inline elements, notably if root element was inline. Did a few other things which probably has no importance at this moment, e.g. refactored out some unused stuff.
 *
 * Revision 1.18  2005/04/21 22:34:56  tobega
 * Fixed an instability in rendering arbitrary xml (added default style to setStartStyle off with)
 *
 * Revision 1.17  2005/04/21 20:09:07  tobega
 * Found another bug on inline padding, almost correct now. Oh, put back real whitespace stripping.
 *
 * Revision 1.16  2005/04/21 18:16:06  tobega
 * Improved handling of inline padding. Also fixed first-line handling according to spec.
 *
 * Revision 1.15  2005/04/20 14:13:07  tobega
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2005/02/03 23:10:10  pdoubleya
 * .
 *
 * Revision 1.13  2005/01/29 20:21:06  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.12  2005/01/24 14:36:33  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.11  2005/01/16 18:50:05  tobega
 * Re-introduced caching of styles, which make hamlet and alice scroll nicely again. Background painting still slow though.
 *
 * Revision 1.10  2005/01/10 01:58:36  tobega
 * Simplified (and hopefully improved) handling of vertical-align. Added support for line-height. As always, provoked a few bugs in the process.
 *
 * Revision 1.9  2005/01/09 15:22:48  tobega
 * Prepared improved handling of margins, borders and padding.
 *
 * Revision 1.8  2005/01/09 13:32:35  tobega
 * Caching image components. Also fixed two bugs that were introduced fixing the last one. Code still too brittle...
 *
 * Revision 1.7  2005/01/09 00:29:28  tobega
 * Removed XPath usages from core classes. Also happened to find and fix a layout-bug that I introduced a while ago.
 *
 * Revision 1.6  2005/01/07 12:42:08  tobega
 * Hacked improved support for custom components (read forms). Creates trouble with the image demo. Anyway, components work and are usually in the right place.
 *
 * Revision 1.5  2005/01/07 00:29:29  tobega
 * Removed Content reference from Box (mainly to reduce memory footprint). In the process stumbled over and cleaned up some messy stuff.
 *
 * Revision 1.4  2005/01/06 09:49:38  tobega
 * More cleanup, aiming to remove Content reference in box
 *
 * Revision 1.3  2005/01/06 00:58:41  tobega
 * Cleanup of code. Aiming to get rid of references to Content in boxes
 *
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
 * Separated current state Context into LayoutContext and the rest into SharedContext.
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
 * setStartStyle spliting parts of box layout into the block subpackage
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

