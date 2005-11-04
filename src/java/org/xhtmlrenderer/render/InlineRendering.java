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
import org.xhtmlrenderer.layout.inline.TextAlignJustify;
import org.xhtmlrenderer.layout.inline.VerticalAlign;
import org.xhtmlrenderer.util.GraphicsUtil;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;


/**
 * Description of the Class
 *
 * @author Joshua Marinacci
 * @author Torbjörn Gannholm
 */
public class InlineRendering {

    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param inline PARAM
     * @param lx     PARAM
     * @param ly     PARAM
     * @param lm
     */
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


    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param ix     PARAM
     * @param iy     PARAM
     * @param inline PARAM
     * @param lm
     */
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

    /**
     * @param c      PARAM
     * @param line   PARAM
     * @param inline PARAM
     */
    public static void paintBackground(RenderingContext c, LineBox line, InlineBox inline) {
        if (inline.getInlineElement() != null)
            inline.getInlineElement().paintBackground(c,
                    line,
                    inline,
                    BorderPainter.TOP + BorderPainter.BOTTOM);
    }

    /**
     * Paint all of the inlines in this box. It recurses through each line, and
     * then each inline in each line, and paints them individually.
     *
     * @param c   PARAM
     * @param box PARAM
     */
    static void paintInlineContext(RenderingContext c, Box box) {
        //dummy style to make sure that text nodes don't get extra padding and such
        {

            //Uu.p("painting box: " + box);
            LinkedList decorations = c.getDecorations();
            //doesn't work here because blocks may be inside inlines, losing inline styling:
            // c.pushStyle(CascadedStyle.emptyCascadedStyle);

            // translate into local coords
            // account for the origin of the containing box
            c.translate(box.x, box.y);
            c.getGraphics().translate(box.x, box.y);

            for (int i = 0; i < box.getChildCount(); i++) {
                // get the line box
                paintLine(c, (LineBox) box.getChild(i), decorations);
            }

            // translate back to parent coords
            c.getGraphics().translate(-box.x, -box.y);
            c.translate(-box.x, -box.y);
            //pop dummy style, but no, see above
            //c.popStyle();
        }
    }

    /**
     * paint all of the inlines on the specified line
     *
     * @param c           PARAM
     * @param line        PARAM
     * @param decorations
     */
    static void paintLine(RenderingContext c, LineBox line, LinkedList decorations) {
        //Uu.p("painting line: " + line);
        // get Xx and y
        if (!line.textAligned) {
            line.x += getTextAlign(c, line);
            line.textAligned = true;
        }
        int lx = line.x;
        //Uu.p("getting the text align for line: " + line);
        //Uu.p("getTextAlign = " + getTextAlign(c,line));
        //Uu.p("lx = " + lx);
        int ly = line.y + line.getBaseline();

        LinkedList pushedStyles = null;

        // for each inline box
        InlineBox lastInline = null;
        int padX = 0;
        for (int j = 0; j < line.getChildCount(); j++) {
            Box child = line.getChild(j);
            if (child.getStyle().isAbsolute() || child.getStyle().isFixed()) {
                LinkedList unpropagated = (LinkedList) decorations.clone();
                decorations.clear();
                paintAbsolute(c, child);
                decorations.addAll(unpropagated);
                continue;
            }

            InlineBox box = (InlineBox) child;
            lastInline = box;
            padX = 0;
//            if (c.hasFirstLineStyles() && pushedStyles == null) {
//                //Uu.p("doing first line styles");
//                pushedStyles = new LinkedList();
//                for (Iterator i = c.getFirstLineStyles().iterator(); i.hasNext();) {
//                    CascadedStyle firstLineStyle = (CascadedStyle) i.next();
//                    padX = handleInlineElementStart(c, firstLineStyle, line, box, padX, decorations);
//                }
//            }
            padX = paintInline(c, box, lx, ly, line, pushedStyles, decorations, padX);
        }

        //do text decorations, all are still active
        ListIterator li = decorations.listIterator(0);
        while (li.hasNext()) {
            TextDecoration decoration = (TextDecoration) li.next();
            //Uu.p("painting a decoration");
            decoration.paint(c, line);
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
     *
     * @param c           PARAM
     * @param ib          PARAM
     * @param lx          PARAM
     * @param ly          PARAM
     * @param line        PARAM
     * @param decorations
     * @param padX
     */
    static int paintInline(RenderingContext c, InlineBox ib, int lx, int ly, LineBox line, LinkedList pushedStyles, LinkedList decorations, int padX) {
        if (ib.pushstyles > 0)
            padX = ib.getInlineElement().handleStart(c, line, ib, padX, decorations, ib.pushstyles);

        if (ib.getStyle().isFloated()) {
            LinkedList unpropagated = (LinkedList) decorations.clone();
            decorations.clear();
            paintFloat(c, ib);
            decorations.addAll(unpropagated);
            debugInlines(c, ib, lx, ly);
        } // Uu.p("paintInline: " + inline);
        else if (ib instanceof InlineBlockBox) {
            //no text-decorations on inline-block
            LinkedList restarted = new LinkedList();
            for (Iterator i = decorations.iterator(); i.hasNext();) {
                TextDecoration td = (TextDecoration) i.next();
                td.setEnd(ib.x);
                //Uu.p("painting decoration");
                td.paint(c, line);
                i.remove();
                restarted.addLast(td.getRestarted(ib.x + ib.getWidth()));
            }
            decorations.clear();
            // c.pushStyle(c.getCss().getCascadedStyle(ib.element, restyle));
            //int textAlign = getTextAlign(c, line);
            //Uu.p("line.x = " + line.x + " text align = " + textAlign);
            //Uu.p("ib.x .y = " + ib.x + " " + ib.y);
            c.translate(line.x,
                    line.y +
                    (line.getBaseline() -
                    VerticalAlign.getBaselineOffset(c, line, ib, c.getTextRenderer(), c.getGraphics(), c.getBlockFormattingContext()) -
                    ib.height));
            c.getGraphics().translate(line.x,
                    line.y +
                    (line.getBaseline() -
                    VerticalAlign.getBaselineOffset(c, line, ib, c.getTextRenderer(), c.getGraphics(), c.getBlockFormattingContext()) -
                    ib.height));
            c.translate(ib.x, ib.y);
            c.getGraphics().translate(ib.x, ib.y);
            BoxRendering.paint(c, ((InlineBlockBox) ib).sub_block);
            c.getGraphics().translate(-ib.x, -ib.y);
            c.translate(-ib.x, -ib.y);
            c.getGraphics().translate(-line.x,
                    -(line.y +
                    (line.getBaseline() -
                    VerticalAlign.getBaselineOffset(c, line, ib, c.getTextRenderer(), c.getGraphics(), c.getBlockFormattingContext()) -
                    ib.height)));
            c.translate(-line.x,
                    -(line.y +
                    (line.getBaseline() -
                    VerticalAlign.getBaselineOffset(c, line, ib, c.getTextRenderer(), c.getGraphics(), c.getBlockFormattingContext()) -
                    ib.height)));
            debugInlines(c, ib, lx, ly);
            // c.popStyle();
            decorations.addAll(restarted);
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
            c.getGraphics().translate(padX, 0);
            LineMetrics lm = FontUtil.getLineMetrics(c, inline, c.getTextRenderer(), c.getGraphics());
            paintBackground(c, line, inline);

            paintSelection(c, inline, lx, ly, lm);
            paintText(c, ix, iy, inline, lm);
            c.getGraphics().translate(-padX, 0);
            c.translate(-padX, 0);
            debugInlines(c, inline, lx, ly);
        }

        padX = ib.getWidth() - ib.rightPadding;

        if (ib.popstyles > 0)
            padX = ib.getInlineElement().handleEnd(c, line, ib, padX, decorations, ib.popstyles);
        /* Come back to this */
//        if (ib.popstyles != 0) {
//            for (int i = 0; i < ib.popstyles; i++) {
//                padX = handleInlineElementEnd(c, decorations, ib, padX, line, pushedStyles);
//            }
//        }

        return padX;
    }

    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param inline PARAM
     * @param lx     PARAM
     * @param ly     PARAM
     */
    static void debugInlines(RenderingContext c, InlineBox inline, int lx, int ly) {
        if (c.debugDrawInlineBoxes()) {
            GraphicsUtil.draw(c.getGraphics(), new Rectangle(lx + inline.x + 1, ly + inline.y + 1 - inline.height,
                    inline.getWidth() - 2, inline.height - 2), Color.green);
        }
    }


    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param inline PARAM
     */
    static void paintAbsolute(RenderingContext c, Box inline) {
        BoxRendering.paint(c, inline);
    }


    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param inline PARAM
     */
    static void paintFloat(RenderingContext c, InlineBox inline) {
        Rectangle oe = c.getExtents();
        c.setExtents(new Rectangle(oe.x, 0, oe.width, oe.height));
        int xoff = 0;
        int yoff = 0;//line.y + ( line.baseline - inline.height );// + inline.y;
        c.translate(xoff, yoff);
        c.getGraphics().translate(xoff, yoff);
        BoxRendering.paint(c, inline);
        c.getGraphics().translate(-xoff, -yoff);
        c.translate(-xoff, -yoff);
        c.setExtents(oe);
    }
}


