/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjoern Gannholm 
 * Copyright (c) 2005 Wisconsin Court System
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
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
import org.xhtmlrenderer.render.FSFontMetrics;
import org.xhtmlrenderer.render.FloatDistances;
import org.xhtmlrenderer.render.FloatedBlockBox;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.render.MarkerData;
import org.xhtmlrenderer.render.StrutMetrics;
import org.xhtmlrenderer.render.Style;
import org.xhtmlrenderer.render.TextDecoration;

public class InlineBoxing {

    public static void layoutContent(LayoutContext c, BlockBox box, List contentList) {
        int maxAvailableWidth = c.getExtents().width;
        int remainingWidth = maxAvailableWidth;

        int minimumLineHeight = (int) c.getCurrentStyle().getLineHeight(c);

        LineBox currentLine = newLine(c, null, box);
        LineBox previousLine = null;

        InlineBox currentIB = null;
        InlineBox previousIB = null;
        
        int contentStart = 0;

        List elementStack = new ArrayList();
        if (box instanceof AnonymousBlockBox) {
            List pending = ((BlockBox) box.getParent()).getPendingInlineElements();
            if (pending != null) {
                currentIB = addNestedInlineBoxes(c, currentLine, pending, 
                        maxAvailableWidth);
                elementStack = pending;
            }
        }
        
        remainingWidth -= c.getBlockFormattingContext().getFloatDistance(c, currentLine, remainingWidth);

        CalculatedStyle parentStyle = c.getCurrentStyle();
        int indent = (int) parentStyle.getFloatPropertyProportionalWidth(CSSName.TEXT_INDENT, maxAvailableWidth, c);
        remainingWidth -= indent;
        contentStart += indent;
        
        MarkerData markerData = c.getCurrentMarkerData();
        if (markerData != null && 
                box.getStyle().getCalculatedStyle().isIdent(
                        CSSName.LIST_STYLE_POSITION, IdentValue.INSIDE)) {
            remainingWidth -= markerData.getLayoutWidth();
            contentStart += markerData.getLayoutWidth();
        }
        c.setCurrentMarkerData(null);

        List pendingFloats = new ArrayList();
        int pendingLeftMBP = 0;
        int pendingRightMBP = 0;

        boolean hasFirstLinePCs = false;
        List pendingInlineLayers = new ArrayList();
        
        if (c.getFirstLinesTracker().hasStyles()) {
            c.getFirstLinesTracker().pushStyles(c);
            hasFirstLinePCs = true;
        }

        boolean needFirstLetter = c.getFirstLettersTracker().hasStyles();
        
        Iterator contentIterator = contentList.iterator();
        while (contentIterator.hasNext()) {
            Object o = contentIterator.next();
            
            if (o instanceof FirstLineStyle || o instanceof FirstLetterStyle) {
                continue;
            }

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
                    currentLine.addChild(c, currentIB);
                } else {
                    previousIB.addInlineChild(c, currentIB);
                }
                
                //To break the line well, assume we don't just want to paint padding on next line
                pendingLeftMBP += style.getMarginBorderPadding(
                        c, maxAvailableWidth, CalculatedStyle.LEFT);
                pendingRightMBP += style.getMarginBorderPadding(
                        c, maxAvailableWidth, CalculatedStyle.RIGHT);
                continue;
            }

            if (o instanceof StylePop) {
                CalculatedStyle style = c.getCurrentStyle();
                int rightMBP = style.getMarginBorderPadding(
                        c, maxAvailableWidth, CalculatedStyle.RIGHT);

                pendingRightMBP -= rightMBP;
                remainingWidth -= rightMBP;

                elementStack.remove(elementStack.size() - 1);

                currentIB.setEndsHere(true);
                
                if (currentIB.getStyle().requiresLayer()) {
                    if (currentIB.element == null || 
                            currentIB.element != c.getLayer().getMaster().element) {
                        throw new RuntimeException("internal error");
                    }
                    c.getLayer().setEnd(currentIB);
                    c.popLayer();
                    pendingInlineLayers.add(currentIB.getContainingLayer());
                }

                previousIB = currentIB;
                currentIB = currentIB.getParent() instanceof LineBox ?
                        null : (InlineBox) currentIB.getParent();

                c.popStyle();
                continue;
            }

            Content content = (Content) o;

            if (mustBeTakenOutOfFlow(content)) {
                remainingWidth -= processOutOfFlowContent(c, content, currentLine, 
                        remainingWidth, pendingFloats);
            } else if (isInlineBlock(content)) {
                Box inlineBlock = layoutInlineBlock(c, box, content);

                if (inlineBlock.getWidth() > remainingWidth && currentLine.isContainsContent()) {
                    saveLine(currentLine, previousLine, c, box, minimumLineHeight,
                            maxAvailableWidth, elementStack, pendingFloats, 
                            hasFirstLinePCs, pendingInlineLayers, markerData,
                            contentStart);
                    markerData = null;
                    contentStart = 0;
                    previousLine = currentLine;
                    currentLine = newLine(c, previousLine, box);
                    currentIB = addNestedInlineBoxes(c, currentLine, elementStack, 
                            maxAvailableWidth);
                    previousIB = currentIB == null || currentIB.getParent() instanceof LineBox ?
                            null : (InlineBox) currentIB.getParent();
                    remainingWidth = maxAvailableWidth;
                    remainingWidth -= c.getBlockFormattingContext().getFloatDistance(c, currentLine, remainingWidth);
                    
                    inlineBlock.detach();
                    inlineBlock = layoutInlineBlock(c, box, content);
                }

                if (currentIB == null) {
                    currentLine.addChild(c, inlineBlock);
                } else {
                    currentIB.addInlineChild(c, inlineBlock);
                }

                currentLine.setContainsContent(true);
                currentLine.setContainsBlockLevelContent(true);

                remainingWidth -= inlineBlock.getWidth();
                
                needFirstLetter = false;
            } else {
                TextContent text = (TextContent) content;
                LineBreakContext lbContext = new LineBreakContext();
                lbContext.setMaster(TextUtil.transformText(text.getText(), c.getCurrentStyle()));
                
                String pageCounterName = null;
                if (c.isPrint() && text.isPageCounter()) {
                    pageCounterName = text.getPageCounterName();
                    lbContext.setMaster("999");
                }
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

                    if (needFirstLetter && !lbContext.isFinished()) {
                        InlineBox firstLetter =
                            addFirstLetterBox(c, currentLine, currentIB, lbContext, 
                                    maxAvailableWidth, remainingWidth);
                        remainingWidth -= firstLetter.getInlineWidth();
                        needFirstLetter = false;
                    } else {
                        lbContext.saveEnd();
                        InlineText inlineText = layoutText(
                                c, remainingWidth - fit, lbContext, false);
                        if (!lbContext.isUnbreakable() ||
                                (lbContext.isUnbreakable() && ! currentLine.isContainsContent())) {
                            inlineText.setPageCounterName(pageCounterName);
                            currentLine.setContainsPageCounter(inlineText.isPageCounter());
                            currentIB.addInlineChild(c, inlineText);
                            currentLine.setContainsContent(true);
                            lbContext.setStart(lbContext.getEnd());
                            remainingWidth -= inlineText.getWidth();
                        } else {
                            lbContext.resetEnd();
                        }
                    }

                    if (lbContext.isNeedsNewLine()) {
                        saveLine(currentLine, previousLine, c, box, minimumLineHeight,
                                maxAvailableWidth, elementStack, pendingFloats, 
                                hasFirstLinePCs, pendingInlineLayers, markerData,
                                contentStart);
                        markerData = null;
                        contentStart = 0;
                        if (currentLine.isFirstLine() && hasFirstLinePCs) {
                            lbContext.setMaster(TextUtil.transformText(
                                    text.getText(), c.getCurrentStyle()));
                        }
                        previousLine = currentLine;
                        currentLine = newLine(c, previousLine, box);
                        currentIB = addNestedInlineBoxes(c, currentLine, elementStack, 
                                maxAvailableWidth);
                        previousIB = currentIB.getParent() instanceof LineBox ?
                                null : (InlineBox) currentIB.getParent();
                        remainingWidth = maxAvailableWidth;
                        remainingWidth -= c.getBlockFormattingContext().getFloatDistance(c, currentLine, remainingWidth);
                    }
                } while (!lbContext.isFinished());
            }
        }

        saveLine(currentLine, previousLine, c, box, minimumLineHeight,
                maxAvailableWidth, elementStack, pendingFloats, hasFirstLinePCs,
                pendingInlineLayers, markerData, contentStart);
        if (currentLine.isFirstLine() && currentLine.height == 0 && markerData != null) {
            c.setCurrentMarkerData(markerData);
        }
        markerData = null;

        if (box instanceof AnonymousBlockBox) {
            ((BlockBox) box.getParent()).setPendingInlineElements(elementStack.size() == 0 ? null : elementStack);
        }
        
        if (!c.shrinkWrap()) box.contentWidth = maxAvailableWidth;
        
        box.setHeight(currentLine.y + currentLine.getHeight());
    }
    
    private static InlineBox addFirstLetterBox(LayoutContext c, LineBox current, 
            InlineBox currentIB, LineBreakContext lbContext, int maxAvailableWidth, 
            int remainingWidth) {
        c.getFirstLettersTracker().pushStyles(c);
        
        InlineBox iB = new InlineBox(null, c.getCurrentStyle(), maxAvailableWidth);
        iB.calculateHeight(c);
        iB.setStartsHere(true);
        iB.setEndsHere(true);
        
        currentIB.addInlineChild(c, iB);
        current.setContainsContent(true);
        
        InlineText text = layoutText(c, remainingWidth, lbContext, true);
        iB.addInlineChild(c, text);
        
        lbContext.setStart(lbContext.getEnd());
        
        c.getFirstLettersTracker().popStyles(c);
        c.getFirstLettersTracker().clearStyles();
        
        return iB;
    }

    private static Box layoutInlineBlock(LayoutContext c, 
            Box containingBlock, Content content) {
        BlockBox inlineBlock = Boxing.constructBox(c, content);
        inlineBlock.setContainingBlock(containingBlock);
        inlineBlock.setContainingLayer(c.getLayer());
        Boxing.layout(c, inlineBlock, content);
        return inlineBlock;
    }

    public static int positionHorizontally(CssContext c, Box current, int start) {
        int x = start;

        InlineBox currentIB = null;

        if (current instanceof InlineBox) {
            currentIB = (InlineBox) currentIB;
            x += currentIB.getLeftMarginBorderPadding(c);
        }

        for (int i = 0; i < current.getChildCount(); i++) {
            Box b = current.getChild(i);
            if (b instanceof InlineBox) {
                InlineBox iB = (InlineBox) current.getChild(i);
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

    private static int positionHorizontally(CssContext c, InlineBox current, int start) {
        int x = start;

        x += current.getLeftMarginBorderPadding(c);

        for (int i = 0; i < current.getInlineChildCount(); i++) {
            Object child = current.getInlineChild(i);
            if (child instanceof InlineBox) {
                InlineBox iB = (InlineBox) child;
                iB.x = x;
                x += positionHorizontally(c, iB, x);
            } else if (child instanceof InlineText) {
                InlineText iT = (InlineText) child;
                iT.setX(x - start);
                x += iT.getWidth();
            } else if (child instanceof Box) {
                Box b = (Box) child;
                b.x = x;
                x += b.getWidth();
            }
        }

        x += current.getRightMarginPaddingBorder(c);

        current.setInlineWidth(x - start);

        return x - start;
    }
    
    public static StrutMetrics createDefaultStrutMetrics(LayoutContext c, Box container) {
        FSFontMetrics strutM = container.getStyle().getFSFontMetrics(c);
        InlineBoxMeasurements measurements = getInitialMeasurements(c, container, strutM);
        
        return new StrutMetrics(
                strutM.getAscent(), measurements.getBaseline(), strutM.getDescent());
    }

    private static void positionVertically(
            LayoutContext c, Box container, LineBox current, MarkerData markerData) {
        if (current.getChildCount() == 0) {
            current.height = 0;
        } else {
            FSFontMetrics strutM = container.getStyle().getFSFontMetrics(c);
            VerticalAlignContext vaContext = new VerticalAlignContext();
            InlineBoxMeasurements measurements = getInitialMeasurements(c, container, strutM);
            vaContext.pushMeasurements(measurements);
            
            TextDecoration lBDecoration = calculateTextDecoration(
                    container, measurements.getBaseline(), strutM);
            if (lBDecoration != null) {
                current.setTextDecoration(lBDecoration);
            }
            
            for (int i = 0; i < current.getChildCount(); i++) {
                Box child = current.getChild(i);
                positionInlineContentVertically(c, vaContext, child);
            }
            
            vaContext.alignChildren();

            current.setHeight(vaContext.getLineBoxHeight());
            
            int paintingTop = vaContext.getPaintingTop();
            int paintingBottom = vaContext.getPaintingBottom();

            if (vaContext.getInlineTop() < 0) {
                moveLineContents(current, -vaContext.getInlineTop());
                if (lBDecoration != null) {
                    lBDecoration.setOffset(lBDecoration.getOffset() - vaContext.getInlineTop());
                }
                paintingTop -= vaContext.getInlineTop();
                paintingBottom -= vaContext.getInlineTop();
            }
            
            if (markerData != null) {
                StrutMetrics strutMetrics = markerData.getStructMetrics();
                strutMetrics.setBaseline(measurements.getBaseline() - vaContext.getInlineTop());
                markerData.setReferenceLine(current);
                current.setMarkerData(markerData);
            }
            
            current.setPaintingTop(paintingTop);
            current.setPaintingHeight(paintingBottom - paintingTop);
        }
    }

    private static void positionInlineVertically(LayoutContext c, 
            VerticalAlignContext vaContext, InlineBox iB) {
        InlineBoxMeasurements iBMeasurements = calculateInlineMeasurements(c, iB, vaContext);
        vaContext.pushMeasurements(iBMeasurements);
        positionInlineChildrenVertically(c, iB, vaContext);
        vaContext.popMeasurements();
    }

    private static void positionInlineBlockVertically(LayoutContext c,
                                                      VerticalAlignContext vaContext, Box inlineBlock) {
        alignInlineContent(c, inlineBlock, inlineBlock.getHeight(), 0, vaContext);

        vaContext.updateInlineTop(inlineBlock.y);
        vaContext.updatePaintingTop(inlineBlock.y);
        
        vaContext.updateInlineBottom(inlineBlock.y + inlineBlock.getHeight());
        vaContext.updatePaintingBottom(inlineBlock.y + inlineBlock.getHeight());
    }

    private static void moveLineContents(LineBox current, int ty) {
        for (int i = 0; i < current.getChildCount(); i++) {
            Box child = (Box) current.getChild(i);
            child.y += ty;
            if (child instanceof InlineBox) {
                moveInlineContents((InlineBox) child, ty);
            }
        }
    }

    private static void moveInlineContents(InlineBox box, int ty) {
        for (int i = 0; i < box.getInlineChildCount(); i++) {
            Object obj = (Object) box.getInlineChild(i);
            if (obj instanceof Box) {
                ((Box) obj).y += ty;

                if (obj instanceof InlineBox) {
                    moveInlineContents((InlineBox) obj, ty);
                }
            }
        }
    }

    private static InlineBoxMeasurements calculateInlineMeasurements(LayoutContext c, InlineBox iB,
                                                                     VerticalAlignContext vaContext) {
        FSFontMetrics fm = iB.getStyle().getFSFontMetrics(c);

        CalculatedStyle style = iB.getStyle().getCalculatedStyle();
        float lineHeight = style.getLineHeight(c);

        int halfLeading = Math.round((lineHeight - 
                (fm.getAscent() + fm.getDescent())) / 2);

        iB.setBaseline(Math.round(fm.getAscent()));

        alignInlineContent(c, iB, fm.getAscent(), fm.getDescent(), vaContext);
        TextDecoration decoration = calculateTextDecoration(iB, iB.getBaseline(), fm);
        if (decoration != null) {
            iB.setTextDecoration(decoration);
        }

        InlineBoxMeasurements result = new InlineBoxMeasurements();
        result.setBaseline(iB.y + iB.getBaseline());
        result.setInlineTop(iB.y - halfLeading);
        result.setInlineBottom(Math.round(result.getInlineTop() + lineHeight));
        result.setTextTop(iB.y);
        result.setTextBottom((int) (result.getBaseline() + fm.getDescent()));
        
        RectPropertySet padding = iB.getStyle().getPaddingWidth(c);
        BorderPropertySet border = style.getBorder(c);
        
        result.setPaintingTop((int)Math.floor(iB.y - border.top() - padding.top()));
        result.setPaintingBottom((int)Math.ceil(iB.y +
                fm.getAscent() + fm.getDescent() + 
                border.bottom() + padding.bottom()));

        result.setContainsContent(iB.containsContent());

        return result;
    }
    
    private static TextDecoration calculateTextDecoration(Box box, int baseline, 
            FSFontMetrics fm) {
        CalculatedStyle style = box.getStyle().getCalculatedStyle();
        
        IdentValue val = style.getIdent(CSSName.TEXT_DECORATION);
        
        TextDecoration decoration = null;
        if (val == IdentValue.UNDERLINE) {
            decoration = new TextDecoration();
            // JDK returns zero so create additional space equal to one
            // "underlineThickness"
            if (fm.getUnderlineOffset() == 0) {
                decoration.setOffset(Math.round((baseline + fm.getUnderlineThickness())));
            } else {
                decoration.setOffset(Math.round((baseline + fm.getUnderlineOffset())));
            }
            decoration.setThickness(Math.round(fm.getUnderlineThickness()));
            
            // JDK on Linux returns some goofy values for 
            // LineMetrics.getUnderlineOffset(). Compensate by always
            // making sure underline fits inside the descender
            if (fm.getUnderlineOffset() == 0) {  // HACK, are we running under the JDK
                int maxOffset = 
                    baseline + (int)fm.getDescent() - decoration.getThickness();
                if (decoration.getOffset() > maxOffset) {
                    decoration.setOffset(maxOffset);
                }
            }
            
        } else if (val == IdentValue.LINE_THROUGH) {
            decoration = new TextDecoration();
            decoration.setOffset(Math.round(baseline + fm.getStrikethroughOffset()));
            decoration.setThickness(Math.round(fm.getStrikethroughThickness()));
        } else if (val == IdentValue.OVERLINE) {
            decoration = new TextDecoration();
            decoration.setOffset(0);
            decoration.setThickness(Math.round(fm.getUnderlineThickness()));
        }
        
        if (decoration != null) {
            if (decoration.getThickness() == 0) {
                decoration.setThickness(1);
            }
        }
        
        return decoration;
    }

    // XXX vertical-align: super/middle/sub could be improved
    private static void alignInlineContent(LayoutContext c, Box box,
                                           float ascent, float descent, VerticalAlignContext vaContext) {
        InlineBoxMeasurements measurements = vaContext.getParentMeasurements();

        CalculatedStyle style = box.getStyle().getCalculatedStyle();

        if (style.isLengthValue(CSSName.VERTICAL_ALIGN)) {
            box.y = (int) (measurements.getBaseline() - ascent -
                    style.getFloatPropertyProportionalTo(CSSName.VERTICAL_ALIGN, style.getLineHeight(c), c));
        } else {
            IdentValue vAlign = style.getIdent(CSSName.VERTICAL_ALIGN);

            if (vAlign == IdentValue.BASELINE) {
                box.y = Math.round(measurements.getBaseline() - ascent);
            } else if (vAlign == IdentValue.TEXT_TOP) {
                box.y = measurements.getTextTop();
            } else if (vAlign == IdentValue.TEXT_BOTTOM) {
                box.y = Math.round(measurements.getTextBottom() - descent - ascent);
            } else if (vAlign == IdentValue.MIDDLE) {
                box.y = Math.round((measurements.getTextTop() - measurements.getBaseline()) / 2
                        - ascent / 2);
            } else if (vAlign == IdentValue.SUPER) {
                box.y = Math.round((measurements.getTextTop() - measurements.getBaseline()) / 2
                        - ascent);
            } else if (vAlign == IdentValue.SUB) {
                box.y = Math.round(measurements.getBaseline() + ascent / 2);
            } else {
                box.y = Math.round(measurements.getBaseline() - ascent);
            }
        }
    }

    private static InlineBoxMeasurements getInitialMeasurements(
            LayoutContext c, Box container, FSFontMetrics strutM) {
        Style style = container.getStyle();
        float lineHeight = style.getCalculatedStyle().getLineHeight(c);

        int halfLeading = Math.round((lineHeight - 
                (strutM.getAscent() + strutM.getDescent())) / 2);

        InlineBoxMeasurements measurements = new InlineBoxMeasurements();
        measurements.setBaseline((int) (halfLeading + strutM.getAscent()));
        measurements.setTextTop((int) halfLeading);
        measurements.setTextBottom((int) (measurements.getBaseline() + strutM.getDescent()));
        measurements.setInlineTop((int) halfLeading);
        measurements.setInlineBottom((int) (halfLeading + lineHeight));

        return measurements;
    }

    private static void positionInlineChildrenVertically(LayoutContext c, InlineBox current,
                                               VerticalAlignContext vaContext) {
        for (int i = 0; i < current.getInlineChildCount(); i++) {
            Object child = current.getInlineChild(i);
            if (child instanceof Box) {
                positionInlineContentVertically(c, vaContext, (Box)child);
            }
        }
    }

    private static void positionInlineContentVertically(LayoutContext c, 
            VerticalAlignContext vaContext, Box child) {
        VerticalAlignContext vaTarget = vaContext;
        IdentValue vAlign = child.getStyle().getCalculatedStyle().getIdent(
                CSSName.VERTICAL_ALIGN);
        if (vAlign == IdentValue.TOP || vAlign == IdentValue.BOTTOM) {
            vaTarget = vaContext.createChild(child);
        }
        if (child instanceof InlineBox) {
            InlineBox iB = (InlineBox) child;
            positionInlineVertically(c, vaTarget, iB);
        } else if (child instanceof Box) {
            positionInlineBlockVertically(c, vaTarget, (Box) child);
        }
    }

    private static void saveLine(final LineBox current, LineBox previous,
                                 final LayoutContext c, Box block, int minHeight,
                                 final int maxAvailableWidth, List elementStack, 
                                 List pendingFloats, boolean hasFirstLinePCs,
                                 List pendingInlineLayers, MarkerData markerData,
                                 int contentStart) {
        current.setContentStart(contentStart);
        current.prunePendingInlineBoxes();

        int totalLineWidth = positionHorizontally(c, current, 0);
        current.contentWidth = totalLineWidth;

        positionVertically(c, block, current, markerData);

        current.y = previous == null ? 0 : previous.y + previous.height;
        current.calcCanvasLocation();

        if (current.height != 0 && current.height < minHeight) {//would like to discard it otherwise, but that could lose inline elements
            current.height = minHeight;
        }
        
        if (c.isPrint() && current.crossesPageBreak(c)) {
            current.moveToNextPage(c);
            current.calcCanvasLocation();
        }
        
        alignLine(c, current, maxAvailableWidth);
        
        current.calcChildLocations();
        
        block.addChild(c, current);
        
        if (pendingInlineLayers.size() > 0) {
            finishPendingInlineLayers(c, pendingInlineLayers);
            pendingInlineLayers.clear();
        }
        
        if (hasFirstLinePCs && current.isFirstLine()) {
            for (int i = 0; i < elementStack.size(); i++) {
                c.popStyle();
            }
            c.getFirstLinesTracker().popStyles(c);
            c.getFirstLinesTracker().clearStyles();
            for (Iterator i = elementStack.iterator(); i.hasNext(); ) {
                InlineBoxInfo iBInfo = (InlineBoxInfo)i.next();
                c.pushStyle(iBInfo.getCascadedStyle());
                iBInfo.setCalculatedStyle(c.getCurrentStyle());
            }
        }

        if (pendingFloats.size() > 0) {
            for (Iterator i = pendingFloats.iterator(); i.hasNext(); ) {
                FloatLayoutResult layoutResult = (FloatLayoutResult)i.next();
                FloatedBlockBox floater =
                    LayoutUtil.generateFloated(c, layoutResult.getPendingContent(), 
                        maxAvailableWidth, current, null).getBlock();
                current.addNonFlowContent(floater);
            }
            pendingFloats.clear();
        }
    }

    private static void alignLine(final LayoutContext c, final LineBox current, final int maxAvailableWidth) {
        if (!c.shrinkWrap()) {
            if (! current.isContainsPageCounter()) {
                current.setFloatDistances(new FloatDistances() {
                    public int getLeftFloatDistance() {
                        return c.getBlockFormattingContext().getLeftFloatDistance(c, current, maxAvailableWidth);
                    }
    
                    public int getRightFloatDistance() {
                        return c.getBlockFormattingContext().getRightFloatDistance(c, current, maxAvailableWidth);
                    }
                });
            } else {
                FloatDistances distances = new FloatDistances();
                distances.setLeftFloatDistance(
                        c.getBlockFormattingContext().getLeftFloatDistance(
                                c, current, maxAvailableWidth));
                distances.setRightFloatDistance(
                        c.getBlockFormattingContext().getRightFloatDistance(
                                c, current, maxAvailableWidth));
                current.setFloatDistances(distances);
            }
            current.align();
            if (! current.isContainsPageCounter()) {
                current.setFloatDistances(null);
            }
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
    }
    
    private static void finishPendingInlineLayers(LayoutContext c, List layers) {
        for (int i = 0; i < layers.size(); i++) {
            Layer l = (Layer)layers.get(i);
            l.positionChildren(c);
        }
    }
    
    private static InlineText layoutText(LayoutContext c, int remainingWidth,
                                         LineBreakContext lbContext, boolean needFirstLetter) {
        InlineText result = null;

        result = new InlineText();
        result.setMasterText(lbContext.getMaster());

        if (needFirstLetter) {
            Breaker.breakFirstLetter(c, lbContext, remainingWidth, c.getCurrentStyle());
        } else {
            Breaker.breakText(c, lbContext, remainingWidth,
                    c.getCurrentStyle().getWhitespace(), c.getCurrentStyle());
        }

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

    private static int processOutOfFlowContent(LayoutContext c, Content content, LineBox current, int available, List pendingFloats) {
        int result = 0;
        c.pushStyle(content.getStyle());
        if (content instanceof AbsolutelyPositionedContent) {
            BlockBox abs = LayoutUtil.generateAbsolute(c, content, current);
            if (abs != null) {
                current.addNonFlowContent(abs);
            }
        } else if (content instanceof FloatedBlockContent) {
            FloatLayoutResult layoutResult = LayoutUtil.generateFloated(
                    c, (FloatedBlockContent)content, available, current, pendingFloats);
            if (layoutResult.isPending()) {
                pendingFloats.add(layoutResult);
            } else {
                result = layoutResult.getBlock().getWidth();
                current.addNonFlowContent(layoutResult.getBlock());
            }
        }
        c.popStyle();

        return result;
    }

    private static boolean hasTrimmableLeadingSpace(LineBox line, CalculatedStyle style,
                                                    LineBreakContext lbContext) {
        if (!line.isContainsContent() && lbContext.getStartSubstring().startsWith(WhitespaceStripper.SPACE)) {
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
        result.initContainingLayer(c);

        if (previousLine != null) {
            result.y = previousLine.y + previousLine.getHeight();
        }
        
        result.calcCanvasLocation();

        return result;
    }

    private static InlineBox addNestedInlineBoxes(LayoutContext c, LineBox line, 
            List elementStack, int cbWidth) {
        InlineBox currentIB = null;
        InlineBox previousIB = null;

        boolean first = true;
        for (Iterator i = elementStack.iterator(); i.hasNext();) {
            InlineBoxInfo info = (InlineBoxInfo) i.next();
            currentIB = info.getInlineBox().copyOf();
            
            // :first-line transition
            if (info.getCalculatedStyle() != null) {
                currentIB.setStyle(new Style(info.getCalculatedStyle(), cbWidth));
                currentIB.calculateHeight(c);
                info.setCalculatedStyle(null);
                info.setInlineBox(currentIB);
            }
            
            if (first) {
                line.addChild(c, currentIB);
                first = false;
            } else {
                previousIB.addInlineChild(c, currentIB, false);
            }
            previousIB = currentIB;
        }
        return currentIB;
    }

    private static class InlineBoxInfo {
        private CascadedStyle cascadedStyle;
        private CalculatedStyle calculatedStyle;
        private InlineBox inlineBox;

        public InlineBoxInfo(CascadedStyle cascadedStyle, InlineBox inlineBox) {
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

        public CalculatedStyle getCalculatedStyle() {
            return calculatedStyle;
        }

        public void setCalculatedStyle(CalculatedStyle calculatedStyle) {
            this.calculatedStyle = calculatedStyle;
        }
    }
}

