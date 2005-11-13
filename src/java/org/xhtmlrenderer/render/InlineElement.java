/*
 * Copyright (c) 2005 Torbjörn Gannholm
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
 *
 */
package org.xhtmlrenderer.render;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.block.Relative;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.font.LineMetrics;
import java.util.LinkedList;

/**
 * User: tobe
 * Date: 2005-nov-02
 * Time: 23:08:21
 */
public class InlineElement /*implements ElementBox*/ {
    private InlineElement parent;
    private Style startStyle;
    private Style endStyle;
    private Element element;
    private String pseudoElement;

    public InlineElement(Element element, String pseudoElement, InlineElement parent) {
        this.element = element;
        this.pseudoElement = pseudoElement;
        this.parent = parent;
    }

    public void setStartStyle(Style style) {
        startStyle = style;
    }

    public void setEndStyle(Style style) {
        endStyle = style;
    }

    public InlineElement getParent() {
        return parent;
    }

    public void restyleStart(LayoutContext c, int pushstyles, LinkedList pushedStyles) {
        pushstyles--;
        if (pushstyles > 0) parent.restyleStart(c, pushstyles, pushedStyles);
        CascadedStyle cascaded;
        if (pseudoElement == null) {
            cascaded = c.getCss().getCascadedStyle(element, true);
        } else {
            cascaded = c.getCss().getPseudoElementStyle(element, pseudoElement);
        }
        if (pushedStyles != null) pushedStyles.addLast(cascaded);
        c.pushStyle(cascaded);
        startStyle.setCalculatedStyle(c.getCurrentStyle());
    }

    public void restyleEnd(LayoutContext c, int popstyles) {
        endStyle.setCalculatedStyle(c.getCurrentStyle());
        c.popStyle();
        popstyles--;
        if (popstyles > 0) parent.restyleEnd(c, popstyles);
    }

    /**
     * borders are painted in sections, in horizontal order.
     * Keep track of how much is painted for horizontal pattern phase
     */
    private int xOffset = 0;

    public void paintBackground(RenderingContext c,
                                LineBox line,
                                InlineBox inline,
                                int sides) {
        if (inline.contentWidth == 0) return;
        paint(c, line, inline, line.x + inline.x, inline.contentWidth, sides, false);
        untranslateRelative(c, line.isFirstLine);
    }

    /**
     * Will translate relative recursively, so call untranslateRelative afterwards
     */
    private void paint(RenderingContext c,
                       LineBox line,
                       InlineBox inline,
                       int start,
                       int width,
                       int sides, boolean doTextDecorations) {
        Style style;
        if (line.isFirstLine || endStyle == null) {//endStyle can be null if layout is not complete
            style = startStyle;
        } else {
            style = endStyle;
        }
        CalculatedStyle cs = style.getCalculatedStyle();
        //recurse for TOP and BOTTOM only!
        if (parent != null) {
            //these will all do the relevant translates
            parent.paint(c, line, inline, start, width, BorderPainter.BOTTOM + BorderPainter.TOP, doTextDecorations);
        }
        Relative.translateRelative(c, cs);
        Color background_color = cs.getBackgroundColor();
        int parent_width = line.getParent().getWidth();
        RectPropertySet margin = cs.getMarginRect(parent_width, parent_width, c);
        BorderPropertySet border = cs.getBorder(c);
        RectPropertySet padding = cs.getPaddingRect(parent_width, parent_width, c);
        LineMetrics lm = FontUtil.getLineMetrics(inline.getStyle().getFont(c), inline, c.getTextRenderer(), c.getGraphics());
        int ty = line.getBaseline() - inline.y - inline.height - (int) margin.top() - (int) border.top() - (int) padding.top() + line.y;
        ty += (int) lm.getDescent();
        c.translate(0, ty);
        // CLEAN: cast to int
        Rectangle bounds = new Rectangle(start,
                inline.y + (int) margin.top(),
                width,
                inline.height + (int) border.top() + (int) padding.top() + (int) padding.bottom() + (int) border.bottom());
        //first the background
        if (background_color != null) {
            // skip transparent background
            if (!background_color.equals(BackgroundPainter.transparent)) {
                //TODO. make conf controlled Uu.p("filling a background");
                c.getGraphics().setColor(background_color);
                c.getGraphics().fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }

        //then the border
        BorderPainter.paint(bounds, sides, cs, c.getGraphics(), c, xOffset);
        c.translate(0, -ty);
        xOffset += inline.contentWidth;
        //and text decorations
        if (doTextDecorations) paintTextDecoration(style, c, line, start, width);
    }

    public void untranslateRelative(RenderingContext c, boolean isFirstLine) {
        Relative.untranslateRelative(c, isFirstLine || endStyle == null ? startStyle.getCalculatedStyle() : endStyle.getCalculatedStyle());
        if (parent != null) parent.untranslateRelative(c, isFirstLine);
    }

    public void translateRelative(RenderingContext c, boolean isFirstLine) {
        if (parent != null) parent.translateRelative(c, isFirstLine);
        Relative.translateRelative(c, isFirstLine ? startStyle.getCalculatedStyle() : endStyle.getCalculatedStyle());
    }

    public int handleStart(RenderingContext c, LineBox line, InlineBox ib, int padX, int pushstyles) {
        pushstyles--;
        if (pushstyles > 0) padX = parent.handleStart(c, line, ib, padX, pushstyles);
        int parent_width = line.getParent().getWidth();
        //Now we know that an inline element started here, handle borders and such
        CalculatedStyle style = startStyle.getCalculatedStyle();
        BorderPropertySet border = style.getBorder(c);
        //note: percentages here refer to width of containing block
        RectPropertySet margin = style.getMarginRect(parent_width, parent_width, c);
        RectPropertySet padding = style.getPaddingRect(parent_width, parent_width, c);
        //left margin
        // CLEAN: cast to int
        if (parent != null && margin.left() != 0) {
            parent.paint(c,
                    line,
                    ib,
                    line.x + padX + ib.x,
                    (int) margin.left(),
                    BorderPainter.TOP + BorderPainter.BOTTOM, true);
            parent.untranslateRelative(c, line.isFirstLine);
        }
        padX += margin.left();
        //left padding for this inline element
        paintLeftPadding(c, line, ib, padX, border, padding);
        // CLEAN: cast to int
        padX += (int) border.left() + (int) padding.left();
        return padX;
    }

    private void paintLeftPadding(RenderingContext c,
                                  LineBox line,
                                  InlineBox inline,
                                  int padX,
                                  BorderPropertySet border,
                                  RectPropertySet padding) {

        float width = border.left() + padding.left();
        if (width == 0) return;
        paint(c, line, inline,
                line.x + inline.x + padX,
                (int) width, BorderPainter.LEFT + BorderPainter.TOP + BorderPainter.BOTTOM, true);
        untranslateRelative(c, line.isFirstLine);
    }

    public int handleEnd(RenderingContext c, LineBox line, InlineBox ib, int padX, int popstyles) {
        CalculatedStyle style = endStyle.getCalculatedStyle();
        //right padding for this inline element
        int parent_width = line.getParent().getWidth();
        BorderPropertySet border = style.getBorder(c);
        //note: percentages here refer to width of containing block
        RectPropertySet margin = style.getMarginRect(parent_width, parent_width, c);
        RectPropertySet padding = style.getPaddingRect(parent_width, parent_width, c);
        paintRightPadding(c, line, ib, padX, border, padding);

        // CLEAN: cast to int
        padX += (int) padding.right() + (int) border.right();
        if (parent != null && margin.right() != 0) {
            parent.paint(c, line, ib, line.x + ib.x + padX, (int) margin.right(), BorderPainter.TOP + BorderPainter.BOTTOM, true);
            parent.untranslateRelative(c, line.isFirstLine);
        }
        padX += margin.right();
        popstyles--;
        if (popstyles > 0) padX = parent.handleEnd(c, line, ib, padX, popstyles);
        return padX;
    }

    private void paintRightPadding(RenderingContext c,
                                   LineBox line,
                                   InlineBox inline,
                                   int padX,
                                   BorderPropertySet border,
                                   RectPropertySet padding) {
        float width = padding.right() + border.right();
        if (width == 0) return;
        paint(c, line, inline, line.x + padX + inline.x, (int) width, BorderPainter.RIGHT + BorderPainter.TOP + BorderPainter.BOTTOM, true);
        untranslateRelative(c, line.isFirstLine);
    }

    /**
     * Does relative translates recursively. Call untranslateRelative after.
     *
     * @param c
     * @param line
     * @param inline
     */
    public void paintTextDecoration(RenderingContext c,
                                    LineBox line,
                                    InlineBox inline) {
        if (parent != null) {
            //these will all do the relevant translates
            parent.paintTextDecoration(c, line, inline);
        }
        Style style;
        if (line.isFirstLine || endStyle == null) {//endStyle can be null if layout is not complete
            style = startStyle;
        } else {
            style = endStyle;
        }
        CalculatedStyle cs = style.getCalculatedStyle();
        Relative.translateRelative(c, cs);
        paintTextDecoration(style, c, line, line.x + inline.x, inline.contentWidth);
    }

    public static void paintTextDecoration(Style style, RenderingContext c, LineBox line, int x, int width) {
        CalculatedStyle cs = style.getCalculatedStyle();
        //text decoration?
        IdentValue decoration = cs.getIdent(CSSName.TEXT_DECORATION);
        if (decoration != IdentValue.NONE) {
            // CLEAN: cast to int
            //decorations.addLast(new TextDecoration(decoration, ib.x + (int) margin.left() + (int) border.left() + (int) padding.left(), c.getCurrentStyle().getColor(), FontUtil.getLineMetrics(c, null, c.getTextRenderer(), c.getGraphics())));
            //}
            //TODO: handle LineMetrics better
            LineMetrics lm = FontUtil.getLineMetrics(style.getFont(c), null, c.getTextRenderer(), c.getGraphics());
            Graphics g = c.getGraphics();
            Color oldcolor = c.getGraphics().getColor();
            c.getGraphics().setColor(cs.getColor());
            int baseline = line.getBaseline() + line.y;

            float up = 0;
            float thick = 0;
            if (decoration == IdentValue.UNDERLINE) {
                up = lm.getUnderlineOffset();
                thick = lm.getUnderlineThickness();
                // correct the vertical pos of the underline by 2px, as without that
                // the underline sticks right against the text.
                up -= 2;
            } else if (decoration == IdentValue.LINE_THROUGH) {
                up = -lm.getStrikethroughOffset();
                thick = lm.getStrikethroughThickness();
                up += thick;
            } else if (decoration == IdentValue.OVERLINE) {
                up = lm.getAscent();
                thick = lm.getUnderlineThickness();
            }
            g.fillRect(x, baseline - (int) up, width, (int) thick);

            c.getGraphics().setColor(oldcolor);
        }
    }
}

/*
 * $Id$
 *
 */
