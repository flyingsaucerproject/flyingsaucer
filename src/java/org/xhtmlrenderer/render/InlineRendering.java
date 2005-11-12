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
package org.xhtmlrenderer.render;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.inline.TextAlignJustify;
import org.xhtmlrenderer.layout.inline.VerticalAlign;
import org.xhtmlrenderer.util.GraphicsUtil;

import java.awt.*;
import java.awt.font.LineMetrics;

/**
 * @author Joshua Marinacci
 * @author Torbjörn Gannholm
 */
public class InlineRendering {

    //TODO: fix this to take relative into account
    public static void paintSelection(RenderingContext c, InlineBox inline, int lx, int ly, LineMetrics lm) {
        if (c.inSelection(inline)) {
            int dw = inline.getWidth() - 2;
            int xoff = 0;
            if (c.getSelectionEnd() == inline) {
                dw = c.getSelectionEndX();
            }
            if (c.getSelectionStart() == inline) {
                xoff = c.getSelectionStartX();
            }
            c.getGraphics().setColor(new Color(200, 200, 255));
            ((Graphics2D) c.getGraphics()).setPaint(new GradientPaint(0, 0, new Color(235, 235, 255),
                    0, inline.height / 2, new Color(190, 190, 235),
                    true));
            int top = ly + inline.y - (int) Math.ceil(lm.getAscent());
            int height = (int) Math.ceil(lm.getAscent() + lm.getDescent());
            c.getGraphics().fillRect(lx + inline.x + xoff,
                    top,
                    dw - xoff,
                    height);
        }
    }

    public static void paintText(RenderingContext c, int ix, int iy, InlineTextBox inline, LineMetrics lm) {
        String text = inline.getSubstring();
        Graphics2D g = (Graphics2D) c.getGraphics();
        //adjust font for current settings
        Font oldfont = g.getFont();
        g.setFont(inline.getStyle().getFont(c));
        Color oldcolor = g.getColor();
        g.setColor(inline.getStyle().getCalculatedStyle().getColor());

        //baseline is baseline! iy -= (int) lm.getDescent();
        //draw the line
        if (text != null && text.length() > 0) {
            c.getTextRenderer().drawString(c.getGraphics(), text, ix, iy);
        }

        g.setColor(oldcolor);
        if (c.debugDrawFontMetrics()) {
            g.setColor(Color.red);
            g.drawLine(ix, iy, ix + inline.getWidth(), iy);
            iy += (int) Math.ceil(lm.getDescent());
            g.drawLine(ix, iy, ix + inline.getWidth(), iy);
            iy -= (int) Math.ceil(lm.getDescent());
            iy -= (int) Math.ceil(lm.getAscent());
            g.drawLine(ix, iy, ix + inline.getWidth(), iy);
        }

        // restore the old font
        g.setFont(oldfont);
    }

    public static void paintBackground(RenderingContext c, LineBox line, InlineBox inline) {
        if (line.firstLinePseudo != null) {
            line.firstLinePseudo.paintBackground(c,
                    line,
                    inline,
                    BorderPainter.TOP + BorderPainter.BOTTOM);
        }
        if (inline.getInlineElement() != null) {
            inline.getInlineElement().paintBackground(c,
                    line,
                    inline,
                    BorderPainter.TOP + BorderPainter.BOTTOM);
        }
    }

    /**
     * Paint all of the inlines in this box. It recurses through each line, and
     * then each inline in each line, and paints them individually.
     */
    static void paintInlineContext(RenderingContext c, Box box) {
        c.translate(box.x, box.y);

        for (int i = 0; i < box.getChildCount(); i++) {
            // get the line box
            paintLine(c, (LineBox) box.getChild(i));
        }

        // translate back to parent coords
        c.translate(-box.x, -box.y);
    }

    /**
     * paint all of the inlines on the specified line
     */
    public static void paintLine(RenderingContext c, LineBox line) {
        //Uu.p("painting line: " + line);
        // get Xx and y
        if (!line.textAligned) {
            line.x += getTextAlign(c, line);
            line.textAligned = true;
        }
        int lx = line.x;
        int ly = line.y + line.getBaseline();

        // for each inline box
        int padX = 0;
        for (int j = 0; j < line.getChildCount(); j++) {
            Box child = line.getChild(j);

            InlineBox box = (InlineBox) child;
            padX = 0;
            padX = paintInline(c, box, lx, ly, line, padX);
        }

        if (c.debugDrawLineBoxes()) {
            c.getGraphics().translate(lx, ly - line.getBaseline());
            GraphicsUtil.drawBox(c.getGraphics(), line, Color.blue);
            c.getGraphics().translate(-lx, -ly - +line.getBaseline());
        }
    }

    //HACK: this is just a quick hack
    //TODO: paint lines taking bidi into consideration
    private static int getTextAlign(RenderingContext c, LineBox line) {
        CalculatedStyle calculatedStyle = line.getStyle().getCalculatedStyle();
        if (calculatedStyle.isIdent(CSSName.TEXT_ALIGN, IdentValue.LEFT)) return 0;
        int leftover = line.getParent().contentWidth - line.contentWidth;
        if (calculatedStyle.isIdent(CSSName.TEXT_ALIGN, IdentValue.RIGHT)) {
            //Uu.p("leftover = " + leftover);
            return leftover;
        }
        if (calculatedStyle.isIdent(CSSName.TEXT_ALIGN, IdentValue.CENTER)) {
            return leftover / 2;
        }
        //HACK: justified text, should probably be done better
        if (calculatedStyle.isIdent(CSSName.TEXT_ALIGN, IdentValue.JUSTIFY)) {
            if (leftover > 1) TextAlignJustify.justifyLine(c, line, line.getParent().contentWidth);
        }
        return 0;
    }


    /**
     * Inlines are drawn vertically relative to the baseline of the containing
     * line box, not relative to the origin of the line. They *are* drawn
     * horizontally (Xx) relative to the origin of the containing line box
     * though
     */
    static int paintInline(RenderingContext c, InlineBox ib, int lx, int ly, LineBox line, int padX) {
        if (ib.pushstyles > 0)
            padX = ib.getInlineElement().handleStart(c, line, ib, padX, ib.pushstyles);

        if (ib instanceof InlineBlockBox) {
            c.translate(line.x,
                    line.y +
                    (line.getBaseline() -
                    VerticalAlign.getBaselineOffset(c, line, ib, c.getTextRenderer(), c.getGraphics(), c.getBlockFormattingContext()) -
                    ib.height));
            c.translate(ib.x, ib.y);
            Point before = c.getOriginOffset();
            c.translate(-before.x, -before.y);
            Layer.paintAsLayer(c, ((InlineBlockBox) ib).sub_block, before.x, before.y);
            c.translate(before.x, before.y);
            c.translate(-ib.x, -ib.y);
            c.translate(-line.x,
                    -(line.y +
                    (line.getBaseline() -
                    VerticalAlign.getBaselineOffset(c, line, ib, c.getTextRenderer(), c.getGraphics(), c.getBlockFormattingContext()) -
                    ib.height)));
            debugInlines(c, ib, lx, ly);
        } else {

            InlineTextBox inline = (InlineTextBox) ib;

            //Uu.p("inline = " + inline);
            //Uu.p("line.x = " + line.x + " lx = " + lx);
            //Uu.p("ib.x .y = " + ib.x + " " + ib.y);
            c.updateSelection(inline);

            // calculate the Xx and y relative to the baseline of the line (ly) and the
            // left edge of the line (lx)
            int iy = ly - VerticalAlign.getBaselineOffset(c, line, inline, c.getTextRenderer(), c.getGraphics(), c.getBlockFormattingContext());
            int ix = lx + inline.x;//TODO: find the right way to work this out

            // account for padding
            // Uu.p("adjusted inline by: " + inline.totalLeftPadding());
            // Uu.p("inline = " + inline);
            // Uu.p("padding = " + inline.padding);

            // JMM: new adjustments to move the text to account for horizontal insets
            //int padding_xoff = inline.totalLeftPadding(c.getCurrentStyle());
            c.translate(padX, 0);
            LineMetrics lm = FontUtil.getLineMetrics(inline.getStyle().getFont(c), inline, c.getTextRenderer(), c.getGraphics());
            paintBackground(c, line, inline);

            if (inline.getInlineElement() != null) inline.getInlineElement().translateRelative(c, line.isFirstLine);
            paintSelection(c, inline, lx, ly, lm);
            paintText(c, ix, iy, inline, lm);
            if (inline.getInlineElement() != null) inline.getInlineElement().untranslateRelative(c, line.isFirstLine);
            //handle block and first-line decorations first
            line.paintTextDecoration(c, inline);
            //now inline element decorations
            if (inline.getInlineElement() != null) {
                inline.getInlineElement().paintTextDecoration(c, line, inline);
                inline.getInlineElement().untranslateRelative(c, line.isFirstLine);
            }
            c.translate(-padX, 0);
            debugInlines(c, inline, lx, ly);
        }

        padX = ib.getWidth() - ib.rightPadding;

        if (ib.popstyles > 0)
            padX = ib.getInlineElement().handleEnd(c, line, ib, padX, ib.popstyles);
        return padX;
    }

    static void debugInlines(RenderingContext c, InlineBox inline, int lx, int ly) {
        if (c.debugDrawInlineBoxes()) {
            GraphicsUtil.draw(c.getGraphics(), new Rectangle(lx + inline.x + 1, ly + inline.y + 1 - inline.height,
                    inline.getWidth() - 2, inline.height - 2), Color.green);
        }
    }
}


