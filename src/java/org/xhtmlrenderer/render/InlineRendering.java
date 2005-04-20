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

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.layout.block.Relative;
import org.xhtmlrenderer.layout.content.StylePop;
import org.xhtmlrenderer.layout.content.StylePush;
import org.xhtmlrenderer.layout.inline.VerticalAlign;
import org.xhtmlrenderer.util.GraphicsUtil;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.util.Iterator;


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
     */
    public static void paintSelection(Context c, InlineBox inline, int lx, int ly) {
        if (c.inSelection(inline)) {
            int dw = inline.width - 2;
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
            LineMetrics lm = c.getTextRenderer().getLineMetrics(c.getGraphics(), FontUtil.getFont(c), "Test");
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
     * @param lx     PARAM
     * @param ly     PARAM
     * @param ix     PARAM
     * @param iy     PARAM
     * @param inline PARAM
     */
    public static void paintText(Context c, int lx, int ly, int ix, int iy, InlineTextBox inline) {
        String text = inline.getSubstring();
        Graphics g = c.getGraphics();
        //adjust font for current settings
        Font oldfont = c.getGraphics().getFont();
        c.getGraphics().setFont(FontUtil.getFont(c));
        Color oldcolor = c.getGraphics().getColor();
        c.getGraphics().setColor(c.getCurrentStyle().getColor());
        Font cur_font = c.getGraphics().getFont();
        LineMetrics lm = c.getTextRenderer().getLineMetrics(c.getGraphics(), cur_font, text);

        //baseline is baseline! iy -= (int) lm.getDescent();

        //draw the line
        if (text != null && text.length() > 0) {
            c.getTextRenderer().drawString(c.getGraphics(), text, ix, iy);
        }

        //draw any text decoration
        int stringWidth = (int) Math.ceil(c.getTextRenderer().
                getLogicalBounds(c.getGraphics(),
                        c.getGraphics().getFont(),
                        text).getWidth());

        // override based on settings
        IdentValue decoration = c.getCurrentStyle().getIdent(CSSName.TEXT_DECORATION);

        if (decoration == IdentValue.UNDERLINE) {
            float down = lm.getUnderlineOffset();
            float thick = lm.getUnderlineThickness();
            
            // correct the vertical pos of the underline by 2px, as without that 
            // the underline sticks right against the text.
            g.fillRect(ix, iy - (int) down + 2, stringWidth, (int) thick);
        } else if (decoration == IdentValue.LINE_THROUGH) {
            float down = lm.getStrikethroughOffset();
            float thick = lm.getStrikethroughThickness();
            g.fillRect(ix, iy + (int) down, stringWidth, (int) thick);
        } else if (decoration == IdentValue.OVERLINE) {
            float down = lm.getAscent();
            float thick = lm.getUnderlineThickness();
            g.fillRect(ix, iy - (int) down, stringWidth, (int) thick);
        }

        c.getGraphics().setColor(oldcolor);
        if (c.debugDrawFontMetrics()) {
            g.setColor(Color.red);
            g.drawLine(ix, iy, ix + inline.width, iy);
            iy += (int) Math.ceil(lm.getDescent());
            g.drawLine(ix, iy, ix + inline.width, iy);
            iy -= (int) Math.ceil(lm.getDescent());
            iy -= (int) Math.ceil(lm.getAscent());
            g.drawLine(ix, iy, ix + inline.width, iy);
        }

        // restore the old font
        c.getGraphics().setFont(oldfont);
    }

    /**
     * @param c      PARAM
     * @param line   PARAM
     * @param inline PARAM
     * @deprecated this should maybe be done differently
     */
    public static void paintPadding(Context c, LineBox line, InlineBox inline) {
        //Uu.p("painting border: " + inline.border);
        // paint the background
        int padding_xoff = inline.totalLeftPadding(c.getCurrentStyle());
        int padding_yoff = inline.totalTopPadding(c.getCurrentStyle());

        int ty = line.getBaseline() - inline.y - inline.height - padding_yoff + line.y;

        LineMetrics lm = FontUtil.getLineMetrics(c, inline);
        ty += (int) lm.getDescent();
        c.translate(-padding_xoff, ty);
        int old_width = inline.width;
        int old_height = inline.height;
        inline.height += inline.totalVerticalPadding(c.getCurrentStyle());
        BoxRendering.paintBackground(c, inline);
        Border margin = c.getCurrentStyle().getMarginWidth(c.getBlockFormattingContext().getWidth(), c.getBlockFormattingContext().getHeight());

        Rectangle bounds = new Rectangle(inline.x + margin.left,
                inline.y + margin.top,
                inline.width - margin.left - margin.right,
                inline.height - margin.top - margin.bottom);
        BorderPainter.paint(c, bounds, BorderPainter.ALL);
        inline.width = old_width;
        inline.height = old_height;
        c.translate(+padding_xoff, -ty);
    }

    /**
     * Paint all of the inlines in this box. It recurses through each line, and
     * then each inline in each line, and paints them individually.
     *
     * @param c       PARAM
     * @param box     PARAM
     * @param restyle PARAM
     */
    static void paintInlineContext(Context c, Box box, boolean restyle) {
        //dummy style to make sure that text nodes don't get extra padding and such
        c.pushStyle(new CascadedStyle());
        //BlockBox block = (BlockBox)box;
        // translate into local coords
        // account for the origin of the containing box
        c.translate(box.x, box.y);
        // for each line box
        BlockBox block = null;
        if (box instanceof BlockBox) {//Why isn't it always a BlockBox? Because of e.g. Floats!
            block = (BlockBox) box;
        }

        int blockLineHeight = FontUtil.lineHeight(c);
        LineMetrics blockLineMetrics = c.getTextRenderer().getLineMetrics(c.getGraphics(),
                FontUtil.getFont(c), "thequickbrownfoxjumpedoverthelazydogTHEQUICKBROWNFOXJUMPEDOVERTHELAZYDOG");

        for (int i = 0; i < box.getChildCount(); i++) {
            if (i == 0 && block != null && block.firstLineStyle != null) {
                c.pushStyle(block.firstLineStyle);
            }
            // get the line box
            paintLine(c, (LineBox) box.getChild(i), blockLineHeight, blockLineMetrics, restyle);
            if (i == 0 && block != null && block.firstLineStyle != null) {
                c.popStyle();
            }
        }

        // translate back to parent coords
        c.translate(-box.x, -box.y);
        //pop dummy style
        c.popStyle();
    }

    /**
     * paint all of the inlines on the specified line
     *
     * @param c                PARAM
     * @param line             PARAM
     * @param blockLineHeight  PARAM
     * @param blockLineMetrics PARAM
     * @param restyle          PARAM
     */
    static void paintLine(Context c, LineBox line, int blockLineHeight, LineMetrics blockLineMetrics, boolean restyle) {
        // get Xx and y
        int lx = line.x;
        int ly = line.y + line.getBaseline();

        // for each inline box
        for (int j = 0; j < line.getChildCount(); j++) {
            Box child = line.getChild(j);
            if (child.absolute) {
                paintAbsolute(c, child, restyle);
                continue;
            }

            InlineBox box = (InlineBox) child;
            paintInline(c, box, lx, ly, line, blockLineHeight, blockLineMetrics, restyle);
        }
        if (c.debugDrawLineBoxes()) {
            GraphicsUtil.drawBox(c.getGraphics(), line, Color.blue);
        }
    }


    /**
     * Inlines are drawn vertically relative to the baseline of the containing
     * line box, not relative to the origin of the line. They *are* drawn
     * horizontally (Xx) relative to the origin of the containing line box
     * though
     *
     * @param c                PARAM
     * @param ib               PARAM
     * @param lx               PARAM
     * @param ly               PARAM
     * @param line             PARAM
     * @param blockLineHeight  PARAM
     * @param blockLineMetrics PARAM
     * @param restyle          PARAM
     */
    static void paintInline(Context c, InlineBox ib, int lx, int ly, LineBox line, int blockLineHeight, LineMetrics blockLineMetrics, boolean restyle) {
        restyle = restyle || ib.restyle;//cascade it down
        ib.restyle = false;//reset
        if (ib.pushstyles != null) {
            for (Iterator i = ib.pushstyles.iterator(); i.hasNext();) {
                StylePush sp = (StylePush) i.next();
                c.pushStyle(c.getCss().getCascadedStyle(sp.getElement(), restyle));

                //Now we know that an inline element started here, handle borders and such?
                Relative.translateRelative(c);
                //TODO: push to current border-list (and paint left edge)
                //HACK: this might do for now - tobe 2004-12-27
                paintPadding(c, line, ib);
            }
        }

        if (ib.floated) {
            paintFloat(c, ib, restyle);
            debugInlines(c, ib, lx, ly);
        } // Uu.p("paintInline: " + inline);
        else if (ib instanceof InlineBlockBox) {
            c.pushStyle(c.getCss().getCascadedStyle(ib.element, restyle));
            c.translate(line.x,
                    line.y +
                    (line.getBaseline() -
                    VerticalAlign.getBaselineOffset(c, line, ib, blockLineHeight, blockLineMetrics) -
                    ib.height));
            BoxRendering.paint(c, ib, true, restyle);
            c.translate(-line.x,
                    -(line.y +
                    (line.getBaseline() -
                    VerticalAlign.getBaselineOffset(c, line, ib, blockLineHeight, blockLineMetrics) -
                    ib.height)));
            debugInlines(c, ib, lx, ly);
            c.popStyle();
        } else {

            InlineTextBox inline = (InlineTextBox) ib;

            c.updateSelection(inline);

            // calculate the Xx and y relative to the baseline of the line (ly) and the
            // left edge of the line (lx)
            int iy = ly - VerticalAlign.getBaselineOffset(c, line, inline, blockLineHeight, blockLineMetrics);
            int ix = lx + inline.x;//TODO: find the right way to work this out

            // account for padding
            // Uu.p("adjusted inline by: " + inline.totalLeftPadding());
            // Uu.p("inline = " + inline);
            // Uu.p("padding = " + inline.padding);

            paintSelection(c, inline, lx, ly);
            paintText(c, lx, ly, ix, iy, inline);
            debugInlines(c, inline, lx, ly);
        }

        if (ib.popstyles != null) {
            for (Iterator i = ib.popstyles.iterator(); i.hasNext();) {
                StylePop sp = (StylePop) i.next();
                //TODO: paint right edge and pop current border-list
                Relative.untranslateRelative(c);
                c.popStyle();
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param c      PARAM
     * @param inline PARAM
     * @param lx     PARAM
     * @param ly     PARAM
     */
    static void debugInlines(Context c, InlineBox inline, int lx, int ly) {
        if (c.debugDrawInlineBoxes()) {
            GraphicsUtil.draw(c.getGraphics(), new Rectangle(lx + inline.x + 1, ly + inline.y + 1 - inline.height,
                    inline.width - 2, inline.height - 2), Color.green);
        }
    }


    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param inline  PARAM
     * @param restyle PARAM
     */
    static void paintAbsolute(Context c, Box inline, boolean restyle) {
        restyle = restyle || inline.restyle;
        inline.restyle = false;//reset
        // Uu.p("paint absolute: " + inline);
        BoxRendering.paint(c, inline, false, restyle);
    }


    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param inline  PARAM
     * @param restyle PARAM
     */
    static void paintFloat(Context c, InlineBox inline, boolean restyle) {
        restyle = restyle || inline.restyle;//should already have been done, but it can't hurt
        inline.restyle = false;//reset
        // Uu.p("painting a float: " + inline);
        Rectangle oe = c.getExtents();
        c.setExtents(new Rectangle(oe.x, 0, oe.width, oe.height));
        int xoff = 0;
        int yoff = 0;//line.y + ( line.baseline - inline.height );// + inline.y;
        c.translate(xoff, yoff);
        BoxRendering.paint(c, inline, false, restyle);
        c.translate(-xoff, -yoff);
        c.setExtents(oe);
    }
}


