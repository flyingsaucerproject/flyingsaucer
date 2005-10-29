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
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.layout.block.Relative;
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
        Graphics g = c.getGraphics();
        //adjust font for current settings
        Font oldfont = c.getGraphics().getFont();
        c.getGraphics().setFont(inline.getStyle().getFont(c));
        Color oldcolor = c.getGraphics().getColor();
        c.getGraphics().setColor(inline.getStyle().getCalculatedStyle().getColor());

        //baseline is baseline! iy -= (int) lm.getDescent();

        //draw the line
        if (text != null && text.length() > 0) {
            c.getTextRenderer().drawString(c.getGraphics(), text, ix, iy);
        }

        c.getGraphics().setColor(oldcolor);
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
        c.getGraphics().setFont(oldfont);
    }

    /**
     * @param c      PARAM
     * @param line   PARAM
     * @param inline PARAM
     */
    public static void paintBackground(RenderingContext c, LineBox line, InlineBox inline) {
        for (Iterator i = c.getInlineBorders().iterator(); i.hasNext();) {
            ((InlineBorder) i.next()).paint(c,
                    line,
                    inline.x,
                    inline.contentWidth,
                    BorderPainter.TOP + BorderPainter.BOTTOM);
        }
    }

    /**
     * @param c       PARAM
     * @param line    PARAM
     * @param inline  PARAM
     * @param margin
     * @param border
     * @param padding
     */
    public static void paintLeftPadding(RenderingContext c,
                                        LineBox line,
                                        InlineBox inline,
                                        int padX,
                                        RectPropertySet margin,
                                        BorderPropertySet border,
                                        RectPropertySet padding) {
        LineMetrics lm = FontUtil.getLineMetrics(c, inline, c.getTextRenderer(), c.getGraphics());
        for (Iterator i = c.getInlineBorders().iterator(); i.hasNext();) {
            // CLEAN: cast to int
            ((InlineBorder) i.next()).paint(c,
                    line,
                    padX + inline.x + (int) margin.left(),
                    (int) padding.left() + (int) border.left(),
                    BorderPainter.TOP + BorderPainter.BOTTOM);
        }

        Color background_color = c.getCurrentStyle().getBackgroundColor();
        InlineBorder ib = new InlineBorder(inline.y, inline.height, margin, border, padding, c.getCurrentStyle(), lm, background_color);
        // CLEAN: cast to int
        ib.paint(c, line,
                inline.x + padX + (int) margin.left(),
                (int) border.left() + (int) padding.left(), BorderPainter.LEFT + BorderPainter.TOP + BorderPainter.BOTTOM);
        c.getInlineBorders().addLast(ib);
    }

    /**
     * @param c       PARAM
     * @param line    PARAM
     * @param inline  PARAM
     * @param border
     * @param padding
     */
    public static void paintRightPadding(RenderingContext c,
                                         LineBox line,
                                         InlineBox inline,
                                         int padX,
                                         BorderPropertySet border,
                                         RectPropertySet padding) {
        InlineBorder ib = (InlineBorder) c.getInlineBorders().removeLast();
        for (Iterator i = c.getInlineBorders().iterator(); i.hasNext();) {
            ((InlineBorder) i.next()).paint(c,
                    line,
                    padX + inline.x,
                    (int) padding.right() + (int) border.right(),
                    BorderPainter.TOP + BorderPainter.BOTTOM);
        }

        ib.paint(c, line, padX + inline.x, (int) padding.right() + (int) border.right(), BorderPainter.RIGHT + BorderPainter.TOP + BorderPainter.BOTTOM);
    }

    /**
     * @param c      PARAM
     * @param line   PARAM
     * @param inline PARAM
     */
    public static void paintMargin(RenderingContext c, LineBox line, InlineBox inline, int padX, int width) {
        if (width <= 0) return;
        for (Iterator i = c.getInlineBorders().iterator(); i.hasNext();) {
            ((InlineBorder) i.next()).paint(c,
                    line,
                    inline.x + padX,
                    width,
                    BorderPainter.TOP + BorderPainter.BOTTOM);
        }
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

        if (c.hasFirstLineStyles() && pushedStyles != null) {
            for (int i = 0; i < pushedStyles.size(); i++) {
                IdentValue decoration = c.getCurrentStyle().getIdent(CSSName.TEXT_DECORATION);
                if (decoration != IdentValue.NONE) {
                    decorations.removeLast();//might have changed because of first line style
                }
                c.popStyle();
            }
            LinkedList pushedBorders = (LinkedList) c.getInlineBorders().clone();
            c.getInlineBorders().clear();//No other inline borders could be present, except those created on first line
            for (Iterator i = c.getFirstLineStyles().iterator(); i.hasNext(); i.next()) {
                c.getInlineBorders().add(pushedBorders.removeFirst());//paint the first-line pseudo-element border
            }

            padX = handleInlineElementEnd(c, decorations, lastInline, padX, line, null);//get rid of firstLineStyle
            
            //should really be reverse order here, but since it isn't used...
//            for (Iterator i = c.getFirstLineStyles().iterator(); i.hasNext(); i.next()) {
//                padX = handleInlineElementEnd(c, decorations, lastInline, padX, line, null);//get rid of firstLineStyle
//            }
            //reinstitute the rest
//            c.getInlineBorders().addAll(pushedBorders);
//            for (Iterator i = pushedStyles.iterator(); i.hasNext();) {
//                c.pushStyle((CascadedStyle) i.next());
//                IdentValue decoration = c.getCurrentStyle().getIdent(CSSName.TEXT_DECORATION);
//                if (decoration != IdentValue.NONE) {
//                    decorations.addLast(new TextDecoration(decoration, 0, c.getCurrentStyle().getColor(), FontUtil.getLineMetrics(c, null)));
//                }
//            }
//            c.clearFirstLineStyles();
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
        padX = handleInlineElementStart(c, line, ib, padX, decorations);

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

        /* Come back to this */
//        if (ib.popstyles != 0) {
//            for (int i = 0; i < ib.popstyles; i++) {
//                padX = handleInlineElementEnd(c, decorations, ib, padX, line, pushedStyles);
//            }
//        }

        return padX;
    }

    private static int handleInlineElementEnd(RenderingContext c, LinkedList decorations, InlineBox ib, int padX, LineBox line, LinkedList pushedStyles) {
        //end text decoration?
        IdentValue decoration = ib.getStyle().getCalculatedStyle().getIdent(CSSName.TEXT_DECORATION);
        if (decoration != IdentValue.NONE) {
            TextDecoration td = (TextDecoration) decorations.getLast();
            td.setEnd(ib.x + padX);
            td.paint(c, line);
            decorations.removeLast();
        }
        //right padding for this inline element
        CalculatedStyle style = ib.getStyle().getCalculatedStyle();
        int parent_width = line.getParent().getWidth();
        BorderPropertySet border = style.getBorder(c);
        //note: percentages here refer to width of containing block
        RectPropertySet margin = style.getMarginRect(parent_width, parent_width, c);
        RectPropertySet padding = style.getPaddingRect(parent_width, parent_width, c);
        paintRightPadding(c, line, ib, padX, border, padding);

        // CLEAN: cast to int
        padX += (int) padding.right() + (int) border.right();
        Relative.untranslateRelative(c, ib.getStyle().getCalculatedStyle(), true);
        c.popStyle();
        if (pushedStyles != null) pushedStyles.removeLast();
        // TODO What does this do?
        style = c.getCurrentStyle();
        //border = style.getBorder(c.getCtx());
        //note: percentages here refer to width of containing block
        margin = style.getMarginRect(parent_width, parent_width, c);
        //padding = style.getPaddingRect(parent_width, parent_width, c.getCtx());
        // CLEAN: cast to int
        paintMargin(c, line, ib, padX, (int) margin.right());
        padX += margin.right();
        return padX;
    }

    private static int handleInlineElementStart(RenderingContext c, LineBox line, InlineBox ib, int padX, LinkedList decorations) {
        // NOT RIGHT (ib.getStyle()...), refers to previous inline? Comment out for now
        /*
        CalculatedStyle style = ib.getStyle().getCalculatedStyle();
        int parent_width = line.getParent().getWidth();
        //Border border = style.getBorder(c.getCtx());
        //note: percentages here refer to width of containing block
        Border margin = style.getMarginRect(parent_width, parent_width, c.getCtx());
        //Border padding = style.getPaddingRect(parent_width, parent_width, c.getCtx());
        paintMargin(c, line, ib, padX, margin.left);
        */

        int parent_width = line.getParent().getWidth();
        //Now we know that an inline element started here, handle borders and such
        Relative.translateRelative(c, ib.getStyle().getCalculatedStyle(), true);
        CalculatedStyle style = ib.getStyle().getCalculatedStyle();
        BorderPropertySet border = style.getBorder(c);
        //note: percentages here refer to width of containing block
        RectPropertySet margin = style.getMarginRect(parent_width, parent_width, c);
        RectPropertySet padding = style.getPaddingRect(parent_width, parent_width, c);
        //left padding for this inline element
        //paintLeftPadding takes the margin into account
        paintLeftPadding(c, line, ib, padX, margin, border, padding);
        // CLEAN: cast to int
        padX += (int) margin.left() + (int) border.left() + (int) padding.left();
        //text decoration?
        IdentValue decoration = c.getCurrentStyle().getIdent(CSSName.TEXT_DECORATION);
        if (decoration != IdentValue.NONE) {
            // CLEAN: cast to int
            decorations.addLast(new TextDecoration(decoration, ib.x + (int) margin.left() + (int) border.left() + (int) padding.left(), c.getCurrentStyle().getColor(), FontUtil.getLineMetrics(c, null, c.getTextRenderer(), c.getGraphics())));
        }
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


