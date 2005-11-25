/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjoern Gannholm, Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.layout;

import java.awt.Font;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.layout.content.AbsolutelyPositionedContent;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.layout.content.FirstLetterStyle;
import org.xhtmlrenderer.layout.content.FirstLineStyle;
import org.xhtmlrenderer.layout.content.FloatedBlockContent;
import org.xhtmlrenderer.layout.content.InlineBlockContent;
import org.xhtmlrenderer.layout.content.StylePop;
import org.xhtmlrenderer.layout.content.StylePush;
import org.xhtmlrenderer.layout.content.TextContent;
import org.xhtmlrenderer.layout.content.WhitespaceStripper;
import org.xhtmlrenderer.render.AnonymousBlockBox;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FloatDistances;
import org.xhtmlrenderer.render.FloatedBlockBox;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.render.Style;

// XXX make sure unused first line styles are popped (first letters too?)
public class InlineBoxing {
    
    private static int pushPseudoClasses(LayoutContext c, List contentList) {
        int result = 0;
        
        Object first = contentList.size() > 0 ? contentList.get(0) : null;
        Object second = contentList.size() > 1 ? contentList.get(0) : null;
        
        if (first instanceof FirstLineStyle) {
            c.getFirstLinesTracker().addStyle(((FirstLineStyle)first).getStyle());
            result++;
        } else if (first instanceof FirstLetterStyle) {
            c.getFirstLettersTracker().addStyle(((FirstLetterStyle)first).getStyle());
            result++;
        } else if (second instanceof FirstLetterStyle) {
            c.getFirstLettersTracker().addStyle(((FirstLetterStyle)second).getStyle());
            result++;
        }
        
        return result;
    }

    public static void layoutContent(LayoutContext c, Box box, List contentList) {
        int maxAvailableWidth = c.getExtents().width;
        int remainingWidth = maxAvailableWidth;
        
        int minimumLineHeight = (int)c.getCurrentStyle().getLineHeight(c); 
        
        LineBox currentLine = newLine(c, null, box);
        LineBox previousLine = null;
        
        InlineBox currentIB = null;
        InlineBox previousIB = null;
        
        List elementStack = new ArrayList();
        if (box instanceof AnonymousBlockBox) {
            List pending = ((BlockBox)box.getParent()).getPendingInlineElements();
            if (pending != null) {
                currentIB = addNestedInlineBoxes(currentLine, pending, maxAvailableWidth);
                elementStack = pending;
            }
        }
        
        CalculatedStyle parentStyle = c.getCurrentStyle();
        int indent = (int)parentStyle.getFloatPropertyProportionalWidth(CSSName.TEXT_INDENT, maxAvailableWidth, c);
        remainingWidth -= indent;
        currentLine.x = indent;
        
        if (! box.getStyle().isCleared()) {
            remainingWidth -= c.getBlockFormattingContext().getFloatDistance(
                    c, currentLine, remainingWidth);
        }
        
        List pendingFloats = new ArrayList();
        int pendingLeftMBP = 0;
        int pendingRightMBP = 0;
        
        int start = pushPseudoClasses(c, contentList);
        
        if (c.getFirstLinesTracker().hasStyles()) {
            c.getFirstLinesTracker().pushStyles(c);
        }
        
        for (int i = start; i < contentList.size(); i++) {
            Object o = contentList.get(i);
            
            if (o instanceof StylePush) {
                StylePush sp = (StylePush) o;
                CascadedStyle cascaded = sp.getStyle(c);
                
                c.pushStyle(cascaded);
                
                CalculatedStyle style = c.getCurrentStyle();
                
                previousIB = currentIB;
                currentIB = new InlineBox(sp.getElement(), style, maxAvailableWidth);
                currentIB.calculateHeight(c);
                
                elementStack.add(new InlineBoxInfo(cascaded, currentIB));
                
                if (previousIB == null) {
                    currentLine.addChild(currentIB);
                } else {
                    previousIB.addInlineChild(currentIB);
                }
                
                //To break the line well, assume we don't just want to paint padding on next line
                pendingLeftMBP += style.getLeftMarginBorderPadding(c, maxAvailableWidth);
                pendingRightMBP += style.getRightMarginBorderPadding(c, maxAvailableWidth);
                continue;
            }
            
            if (o instanceof StylePop) {
                CalculatedStyle style = c.getCurrentStyle();
                int rightMBP = style.getRightMarginBorderPadding(c, maxAvailableWidth);
                
                pendingRightMBP -= rightMBP;
                remainingWidth -= rightMBP;
                
                elementStack.remove(elementStack.size()-1);
                
                currentIB.setEndsHere(true);
                
                previousIB = currentIB;
                currentIB = currentIB.getParent() instanceof LineBox ? 
                        null : (InlineBox)currentIB.getParent();
                
                c.popStyle();
                continue;
            }
            
            Content content = (Content) o;
            
            if (mustBeTakenOutOfFlow(content)) {
                processOutOfFlowContent(c, content, currentLine, remainingWidth, pendingFloats);
            } else if (isInlineBlock(content)) {
                Box inlineBlock = Boxing.layout(c, content);
                
                if (inlineBlock.getWidth() > remainingWidth && currentLine.isContainsContent()) {
                    saveLine(currentLine, previousLine, c, box, minimumLineHeight, 
                            maxAvailableWidth, elementStack, pendingFloats);
                    inlineBlock = Boxing.layout(c, content);
                    previousLine = currentLine;
                    currentLine = newLine(c, previousLine, box);
                    currentIB = addNestedInlineBoxes(currentLine, elementStack, maxAvailableWidth);
                    previousIB = currentIB.getParent() instanceof LineBox ? 
                            null : (InlineBox)currentIB.getParent();
                    remainingWidth = maxAvailableWidth;
                    if (!box.getStyle().isCleared()) {
                        remainingWidth -= c.getBlockFormattingContext().getFloatDistance(
                                c, currentLine, remainingWidth);
                    }
                } 
                
                if (currentIB == null) {
                    currentLine.addChild(inlineBlock);
                } else {
                    currentIB.addInlineChild(inlineBlock);
                }
                
                currentLine.setContainsContent(true);
                
                remainingWidth -= inlineBlock.getWidth();
            } else {
                TextContent text = (TextContent)content;
                LineBreakContext lbContext = new LineBreakContext();
                lbContext.setMaster(TextUtil.transformText(text.getText(), c.getCurrentStyle()));
                do {
                    lbContext.reset();
                    
                    int fit = 0;
                    if (lbContext.getStart() == 0) {
                        fit += pendingLeftMBP;
                    }
                    
                    if (hasTrimmableLeadingSpace(currentLine, c.getCurrentStyle(), 
                            lbContext)) {
                        lbContext.setStart(lbContext.getStart() + 1);
                    }
                    
                    InlineText inlineText = layoutText(c, remainingWidth - fit, lbContext);
                    
                    if (! lbContext.isUnbreakable() || 
                            (lbContext.isUnbreakable() && currentLine.isContainsContent())) {
                        currentIB.addInlineChild(inlineText);
                        currentLine.setContainsContent(true);
                        lbContext.setStart(lbContext.getEnd());
                        remainingWidth -= inlineText.getWidth();
                    }
                    
                    if (lbContext.isNeedsNewLine()) {
                        saveLine(currentLine, previousLine, c, box, minimumLineHeight, 
                                maxAvailableWidth, elementStack, pendingFloats);
                        previousLine = currentLine;
                        currentLine = newLine(c, previousLine, box);
                        currentIB = addNestedInlineBoxes(currentLine, elementStack, maxAvailableWidth);
                        previousIB = currentIB.getParent() instanceof LineBox ? 
                                null : (InlineBox)currentIB.getParent();
                        remainingWidth = maxAvailableWidth;
                        if (!box.getStyle().isCleared()) {
                            remainingWidth -= c.getBlockFormattingContext().getFloatDistance(
                                    c, currentLine, remainingWidth);
                        }                        
                    }
                } while (! lbContext.isFinished());
            }
        }
        
        saveLine(currentLine, previousLine, c, box, minimumLineHeight, 
                maxAvailableWidth, elementStack, pendingFloats);
        
        if (box instanceof AnonymousBlockBox) {
            ((BlockBox)box.getParent()).setPendingInlineElements(
                    elementStack.size() == 0 ? null : elementStack);
        }
        
        // XXX what does this do?
        if (!c.shrinkWrap()) box.contentWidth = maxAvailableWidth;
        
        box.setHeight(currentLine.y + currentLine.getHeight());
    }
    
    private static int positionHorizontally(LayoutContext c, Box current, int start) {
        int x = start;
        
        InlineBox currentIB = null;
        
        if (current instanceof InlineBox) {
            currentIB = (InlineBox)currentIB;
            x += currentIB.getLeftMarginBorderPadding(c);
        }
        
        for (int i = 0; i < current.getChildCount(); i++) {
            Box b = current.getChild(i);
            if (b instanceof InlineBox) {
                InlineBox iB = (InlineBox)current.getChild(i);
                iB.x = x;
                x += positionHorizontally(c, iB, x);
            } else {
                b.x = x;
                x += b.getWidth();
            }
        }
        
        if (currentIB != null) {
            x += currentIB.getRightMarginPaddingBorder(c);
            currentIB.setInlineWidth(x - start);
        }
        
        return x - start;
    }
    
    private static int positionHorizontally(LayoutContext c, InlineBox current, int start) {
        int x = start;
        
        x += current.getLeftMarginBorderPadding(c);
        
        for (int i = 0; i < current.getInlineChildCount(); i++) {
            Object child = current.getInlineChild(i);
            if (child instanceof InlineBox) {
                InlineBox iB = (InlineBox)child;
                iB.x = x;
                x += positionHorizontally(c, iB, x);
            } else if (child instanceof InlineText) {
                InlineText iT = (InlineText)child;
                iT.setX(x);
                x += iT.getWidth();
            } else if (child instanceof Box) {
                Box b = (Box)child;
                b.x = x;
                x += b.getWidth();
            }
        }
        
        x += current.getRightMarginPaddingBorder(c);
        
        current.setInlineWidth(x - start);
        
        return x - start;
    }
    
    private static void positionVertically(LayoutContext c, Box container, LineBox current) {
        if (current.getChildCount() == 0) {
            current.height = 0;
        } else {
            VerticalAlignContext vaContext = new VerticalAlignContext();
            vaContext.pushMeasurements(getInitialMeasurements(c, container));
            
            for (int i = 0; i < current.getChildCount(); i++) {
                Object child = (Box)current.getChild(i);
                if (child instanceof InlineBox) {
                    InlineBox iB = (InlineBox)child;
                    positionInlineVertically(c, vaContext, iB);
                } else if (child instanceof Box) {
                    positionInlineBlockVertically(c, vaContext, (Box)child);
                }
            }
            
            current.setHeight(vaContext.getLineBoxHeight());
            
            if (vaContext.getInlineTop() < 0) {
                moveLineContents(current, -vaContext.getInlineTop());
            }
        }
    }

    private static void positionInlineVertically(LayoutContext c, VerticalAlignContext vaContext, InlineBox iB) {
        InlineBoxMeasurements iBMeasurements = calculateInlineMeasurements(c, iB, vaContext);
        vaContext.pushMeasurements(iBMeasurements);
        positionInlineChildren(c, iB, vaContext);
        vaContext.popMeasurements();
    }
    
    private static void positionInlineBlockVertically(LayoutContext c, 
            VerticalAlignContext vaContext, Box inlineBlock) {
        alignInlineContent(c, inlineBlock, inlineBlock.getHeight(), 0, vaContext);
        
        vaContext.updateInlineTop(inlineBlock.y);
        vaContext.updateInlineBottom(inlineBlock.y + inlineBlock.getHeight());
    }
    
    private static void moveLineContents(LineBox current, int ty) {
        for (int i = 0; i < current.getChildCount(); i++) {
            Box child = (Box)current.getChild(i);
            child.y += ty;
            if (child instanceof InlineBox) {
                moveInlineContents((InlineBox)child, ty);
            }
        }
    }
    
    private static void moveInlineContents(InlineBox box, int ty) {
        for (int i = 0; i < box.getInlineChildCount(); i++) {
            Object obj = (Object)box.getInlineChild(i);
            if (obj instanceof Box) {
                ((Box)obj).y += ty;
                
                if (obj instanceof InlineBox) {
                    moveInlineContents((InlineBox)obj, ty);
                }
            }
        }
    }

    private static InlineBoxMeasurements calculateInlineMeasurements(LayoutContext c, InlineBox iB, 
            VerticalAlignContext vaContext) {
        LineMetrics lm = iB.getStyle().getLineMetrics(c);
        
        CalculatedStyle style = iB.getStyle().getCalculatedStyle();
        float lineHeight = style.getLineHeight(c);
        FontSpecification fontSpec = style.getFont(c);
        
        int halfLeading = Math.round((lineHeight - fontSpec.size) / 2);
        
        iB.setBaseline((int)lm.getAscent());
        
        alignInlineContent(c, iB, lm.getAscent(), lm.getDescent(), vaContext);
        
        InlineBoxMeasurements result = new InlineBoxMeasurements();
        result.setBaseline(iB.y + iB.getBaseline());
        result.setInlineTop(iB.y - halfLeading);
        result.setInlineBottom(Math.round(result.getInlineTop() + lineHeight));
        result.setTextTop(iB.y);
        result.setTextBottom((int)(result.getBaseline() + lm.getDescent()));
        
        result.setContainsContent(iB.containsContent());
        
        return result;
    }
    
    // XXX vertical-align: top/bottom are unimplemented, vertical-align: super/middle/sub could be improved
    private static void alignInlineContent(LayoutContext c, Box box, 
            float ascent, float descent, VerticalAlignContext vaContext) {
        InlineBoxMeasurements measurements = vaContext.getParentMeasurements();
        
        CalculatedStyle style = box.getStyle().getCalculatedStyle();
        
        if (style.isLengthValue(CSSName.VERTICAL_ALIGN)) {
            box.y = (int)(measurements.getBaseline() - ascent -
                style.getFloatPropertyProportionalTo(
                        CSSName.VERTICAL_ALIGN, style.getLineHeight(c), c)); 
        } else {
            IdentValue vAlign = style.getIdent(CSSName.VERTICAL_ALIGN);
            
            if (vAlign == IdentValue.BASELINE) {
                box.y = (int)(measurements.getBaseline() - ascent);
            } else if (vAlign == IdentValue.TEXT_TOP) {
                box.y = measurements.getTextTop();
            } else if (vAlign == IdentValue.TEXT_BOTTOM) {
                box.y = (int)(measurements.getTextBottom() - descent - ascent);
            } else if (vAlign == IdentValue.MIDDLE) {
                box.y = (int)((measurements.getTextTop() - measurements.getBaseline()) / 2 
                        - ascent / 2);
            } else if (vAlign == IdentValue.SUPER) {
                box.y = (int)((measurements.getTextTop() - measurements.getBaseline()) / 2 
                        - ascent);
            } else if (vAlign == IdentValue.SUB) {
                box.y = (int)(measurements.getBaseline() + ascent / 2);
            } else {
                // TODO implement vertical-align: top/bottom, for now just treat as baseline
                box.y = (int)(measurements.getBaseline() - ascent);
            }
        }
    }
    
    private static InlineBoxMeasurements getInitialMeasurements(LayoutContext c, Box container) {
        Style style = container.getStyle();
        LineMetrics strutLM = style.getLineMetrics(c);
        float lineHeight = style.getCalculatedStyle().getLineHeight(c);
        FontSpecification fontSpec = style.getCalculatedStyle().getFont(c);
        
        float halfLeading = (lineHeight - fontSpec.size) / 2;
        
        InlineBoxMeasurements measurements = new InlineBoxMeasurements();
        measurements.setBaseline((int)(halfLeading + strutLM.getAscent()));
        measurements.setTextTop((int)halfLeading);
        measurements.setTextBottom((int)(measurements.getBaseline() + strutLM.getDescent()));
        measurements.setInlineTop((int)halfLeading);
        measurements.setInlineBottom((int)(halfLeading + lineHeight));
        
        return measurements;
    }
    
    private static void positionInlineChildren(LayoutContext c, InlineBox current, 
            VerticalAlignContext vaContext) {
        for (int i = 0; i < current.getInlineChildCount(); i++) {
            Object child = current.getInlineChild(i);
            if (child instanceof InlineBox) {
                InlineBox iB = (InlineBox)child;
                positionInlineVertically(c, vaContext, iB);
            } else if (child instanceof Box) {
                positionInlineBlockVertically(c, vaContext, (Box)child);
            }
        }
    }
    
    
    private static void saveLine(final LineBox current, LineBox previous, 
            final LayoutContext c, Box block, int minHeight, 
            final int maxAvailableWidth, List elementStack, List pendingFloats) {
        current.prunePendingInlineBoxes();
        
        int totalLineWidth = positionHorizontally(c, current, 0);
        current.contentWidth = totalLineWidth;
        
        positionVertically(c, block, current);
        
        if (! c.shrinkWrap()) {
        	current.setFloatDistances(new FloatDistances() {
        		public int getLeftFloatDistance() {
        			return c.getBlockFormattingContext().getLeftFloatDistance(
        					c, current, maxAvailableWidth);
        		}
        		
        		public int getRightFloatDistance() {
        			return c.getBlockFormattingContext().getRightFloatDistance(
        					c, current, maxAvailableWidth);
        		}
        	});
        	current.align();
        	current.setFloatDistances(null);
        } else {
        	// FIXME Not right, but can't calculated float distance yet
        	// because we don't know how wide the line is.  Should save
        	// BFC and BFC offset and use that to calculate float distance
        	// correctly when we're ready to align the line.
        	FloatDistances distances = new FloatDistances();
        	distances.setLeftFloatDistance(0);
        	distances.setRightFloatDistance(0);
        	current.setFloatDistances(distances);
        }
        
        /*
        if (c.hasFirstLineStyles()) {
//          first pop element styles pushed on first line
            for (int i = 0; i < pushedOnFirstLine.size(); i++) c.popStyle();
//          Now save the first-line style
            current.firstLinePseudo = new InlineElement(null, "first-line", null);
            Style fls = new Style(c.getCurrentStyle(), bounds.width);
            current.firstLinePseudo.setStartStyle(fls);
            current.firstLinePseudo.setEndStyle(fls);
//          then pop first-line styles
            for (int i = 0; i < c.getFirstLineStyles().size(); i++) c.popStyle();
//          reinstate element styles
            for (Iterator i = pushedOnFirstLine.iterator(); i.hasNext();) {
                c.pushStyle((CascadedStyle) i.next());
            }
            c.clearFirstLineStyles();
        }
        */
        
        if (c.shrinkWrap()) {
            block.adjustWidthForChild(current.contentWidth);
        }
        
        current.y = previous == null ? 0 : previous.y + previous.height;
        
        if (current.height != 0 && current.height < minHeight) {//would like to discard it otherwise, but that could lose inline elements
            current.height = minHeight;
        }
        block.addChild(current);
        
        if (pendingFloats.size() > 0) {
            c.setFloatingY(c.getFloatingY() + current.height);
            c.getBlockFormattingContext().floatPending(c, pendingFloats);
        }
        
        // new float code
        if (!block.getStyle().isClearLeft()) {
            current.x += c.getBlockFormattingContext().getLeftFloatDistance(
                    c, current, maxAvailableWidth);
        }
    }
    
    private static InlineText layoutText(LayoutContext c, int remainingWidth, 
            LineBreakContext lbContext) {
        // TODO handle first-letter
        
        InlineText result = null;

        result = new InlineText();
        result.setMasterText(lbContext.getMaster());
        
        Font font = c.getFont(c.getCurrentStyle().getFont(c));
        
        Breaker.breakText(c, lbContext, remainingWidth, 
                c.getCurrentStyle().getWhitespace(), font);
        
        result.setSubstring(lbContext.getStart(), lbContext.getEnd());
        result.setWidth(lbContext.getWidth());
        
        return result;
    }

    private static boolean mustBeTakenOutOfFlow(Content content) {
        return content instanceof FloatedBlockContent || 
            content instanceof AbsolutelyPositionedContent;
    }
    
    private static boolean isInlineBlock(Content content) {
        return content instanceof InlineBlockContent;
    }
    
    private static int processOutOfFlowContent(
            LayoutContext c, Content content, LineBox current, int available, List pendingFloats) {
        int result = 0;
        c.pushStyle(content.getStyle());
        if (content instanceof AbsolutelyPositionedContent) {
            LayoutUtil.generateAbsolute(c, content, current);
        } else if (content instanceof FloatedBlockContent) {
            FloatedBlockBox floater = LayoutUtil.generateFloated(
                    c, content, available, current, pendingFloats);
            if (!floater.isPending()) {
                result = floater.getWidth();
            }
        }
        c.popStyle();
        
        return result;
    }
    
    private static boolean hasTrimmableLeadingSpace(LineBox line, CalculatedStyle style, 
            LineBreakContext lbContext) {
        if (! line.isContainsContent() && lbContext.getStartSubstring().startsWith(WhitespaceStripper.SPACE)) {
            IdentValue whitespace = style.getWhitespace();
            if (whitespace == IdentValue.NORMAL || whitespace == IdentValue.NOWRAP) {
                return true;
            }
        }
        return false;
    }
    
    private static LineBox newLine(LayoutContext c, LineBox previousLine, Box box) {
        LineBox result = new LineBox();
        result.createDefaultStyle(c);
        result.setParent(box);
        
        if (previousLine != null) {
            result.y = previousLine.y + previousLine.getHeight();
        }
        
        return result;
    }
    
    private static InlineBox addNestedInlineBoxes(LineBox line, List elementStack, int cbWidth) {
        InlineBox currentIB = null;
        InlineBox previousIB = null;
        
        boolean first = true;
        for (Iterator i = elementStack.iterator(); i.hasNext(); ) {
            InlineBoxInfo info = (InlineBoxInfo)i.next();
            currentIB = info.getInlineBox().copyOf();
            if (first) {
                line.addChild(currentIB);
                first = false;
            } else {
                previousIB.addInlineChild(currentIB);
            }
            previousIB = currentIB;
        }
        return currentIB;
    }
    
    private static class InlineBoxInfo {
        private CascadedStyle cascadedStyle;
        private InlineBox inlineBox;
        
        public InlineBoxInfo(
                CascadedStyle cascadedStyle, InlineBox inlineBox) {
            this.cascadedStyle = cascadedStyle;
            this.inlineBox = inlineBox;
        }
        
        public InlineBoxInfo() {
        }

        public CascadedStyle getCascadedStyle() {
            return cascadedStyle;
        }

        public void setCascadedStyle(CascadedStyle cascadedStyle) {
            this.cascadedStyle = cascadedStyle;
        }

        public InlineBox getInlineBox() {
            return inlineBox;
        }

        public void setInlineBox(InlineBox inlineBox) {
            this.inlineBox = inlineBox;
        }
    }
}

