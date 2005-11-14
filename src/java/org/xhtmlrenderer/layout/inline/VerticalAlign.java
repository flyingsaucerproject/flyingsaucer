/*
 * VerticalAlign.java
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
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
package org.xhtmlrenderer.layout.inline;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.InlineTextBox;
import org.xhtmlrenderer.render.LineBox;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;


/**
 * Description of the Class
 *
 * @author Torbjörn Gannholm
 */
public class VerticalAlign {

    /**
     * returns how much the baseline should be raised (negative means lowered)
     *
     * @param c          PARAM
     * @param curr_line  PARAM
     * @param new_inline PARAM
     * @return The baselineOffset value
     */
    public static int getBaselineOffset(CssContext c, LineBox curr_line, InlineBox new_inline, TextRenderer tr, Graphics2D g2, BlockFormattingContext bfc) {
        CalculatedStyle style = new_inline.getStyle().getCalculatedStyle();
        int lineHeight;
        int ascent;
        int descent;
        int leading;
        //int xheight;
        int baselineOffset;
        if (new_inline instanceof InlineTextBox) {
            Font font = c.getFont(style.getFont(c));
            LineMetrics metrics = FontUtil.getLineMetrics(font, null, tr, g2);
            lineHeight = (int) style.getLineHeight(c);
            ascent = (int) metrics.getAscent();
            descent = (int) metrics.getDescent();
            leading = lineHeight - (ascent + descent);
        } else {
            lineHeight = new_inline.height;
            ascent = lineHeight;
            descent = 0;
            leading = 0;
        }
        return getBaselineOffset(style, c, tr, g2, ascent, descent, lineHeight, leading, curr_line, bfc);

    }

    private static int getBaselineOffset(CalculatedStyle style, CssContext c, TextRenderer tr, Graphics2D g2, int ascent, int descent, int lineHeight, int leading, LineBox curr_line, BlockFormattingContext bfc) {
        int baselineOffset;
        CalculatedStyle parentStyle = style.getParent();
        Font parentFont = c.getFont(parentStyle.getFont(c));
        LineMetrics parentLineMetrics = FontUtil.getLineMetrics(parentFont, null, tr, g2);
        int parentLineHeight = (int) style.getLineHeight(c);
        int parentAscent = (int) parentLineMetrics.getAscent();
        int parentDescent = (int) parentLineMetrics.getDescent();
        int parentLeading = lineHeight - (ascent + descent);

        //Assumption: our baseline is aligned with parent baseline
        //Logic: the baselineOffset is calculated as up being positive, down negative
        IdentValue vertical_align = style.getIdent(CSSName.VERTICAL_ALIGN);
        if (vertical_align == IdentValue.BASELINE) {
            baselineOffset = 0;
        } else if (vertical_align == IdentValue.SUPER) {
            // works okay i think
            //strikeThroughOffset has negative values for up.
            baselineOffset = (int) Math.round(-parentLineMetrics.getStrikethroughOffset() * 1.5);
            //XRLog.render("baseline offset for super "+baselineOffset);
        } else if (vertical_align == IdentValue.SUB) {
            // works okay i think
            //strikeThroughOffset has negative values for up.
            baselineOffset = (int) parentLineMetrics.getStrikethroughOffset();
            //XRLog.render("baseline offset for sub "+baselineOffset);
        } else if (vertical_align == IdentValue.TEXT_TOP) {
            int top = ascent + leading / 2;
            int parentTop = parentAscent + parentLeading / 2;
            baselineOffset = parentTop - top;
        } else if (vertical_align == IdentValue.TEXT_BOTTOM) {
            int bottom = descent + leading / 2;
            int parentBottom = parentDescent + parentLeading / 2;
            baselineOffset = -(parentBottom - bottom);
        } else if (vertical_align == IdentValue.MIDDLE) {
            // just like firefox! Works because the middle of the box must always be the same as the middle of the text!
            int halfxheight = -(int) (parentLineMetrics.getStrikethroughOffset());
            int boxmiddle = (int) Math.round(lineHeight / 2.0);
            int boxbase = (int) Math.round(ascent + leading / 2.0);
            int boxalign = boxbase - boxmiddle;
            baselineOffset = halfxheight - boxalign;
        } else if (vertical_align == IdentValue.TOP) {
            // like firefox, so I suppose it's correct...
            //TODO: this is basically correct for simple cases, but we really need to check the whole subtree
            baselineOffset = curr_line.getBaseline() - lineHeight;
        } else if (vertical_align == IdentValue.BOTTOM) {
            //TODO: this is basically correct for simple cases, but we really need to check the whole subtree
            baselineOffset = descent - (curr_line.height - curr_line.getBaseline());
        } else {
            baselineOffset = (int) style.getFloatPropertyProportionalHeight(
                    CSSName.VERTICAL_ALIGN, style.getLineHeight(c), c);
        }
        return baselineOffset;
    }
}

