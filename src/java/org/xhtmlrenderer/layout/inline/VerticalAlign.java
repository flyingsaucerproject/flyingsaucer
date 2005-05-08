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
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.InlineTextBox;
import org.xhtmlrenderer.render.LineBox;

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
     * @param c                PARAM
     * @param curr_line        PARAM
     * @param new_inline       PARAM
     * @param blockLineHeight  PARAM
     * @param blockLineMetrics PARAM
     * @return The baselineOffset value
     */
    public static int getBaselineOffset(Context c, LineBox curr_line, InlineBox new_inline, int blockLineHeight, LineMetrics blockLineMetrics) {
        int lineHeight;
        int ascent;
        int descent;
        int leading;
        //int xheight;
        int baselineOffset;
        if (new_inline instanceof InlineTextBox) {
            // should be the metrics of the font, actually is the metrics of the text
            LineMetrics metrics = FontUtil.getLineMetrics(c, new_inline);
            lineHeight = FontUtil.lineHeight(c);//assume that current context is valid for new_inline
            ascent = (int) metrics.getAscent();
            descent = (int) metrics.getDescent();
            leading = (int) metrics.getLeading();
            //xheight = (int)(-metrics.getStrikethroughOffset()*2.0);
        } else {
            lineHeight = new_inline.height;
            ascent = lineHeight;
            descent = 0;
            leading = 0;
            //xheight = lineHeight/2;
        }

        //Assumption: our baseline is aligned with parent baseline
        IdentValue vertical_align = c.getCurrentStyle().getIdent(CSSName.VERTICAL_ALIGN);
        if (vertical_align == IdentValue.BASELINE) {
            baselineOffset = 0;
        } else if (vertical_align == IdentValue.SUPER) {
            // works okay i think
            baselineOffset = (int) Math.round(-blockLineMetrics.getStrikethroughOffset() * 1.5);//up is negative in Java!
            //XRLog.render("baseline offset for super "+baselineOffset);
        } else if (vertical_align == IdentValue.SUB) {
            // works okay i think
            baselineOffset = (int) blockLineMetrics.getStrikethroughOffset();//up is negative in Java!
            //XRLog.render("baseline offset for sub "+baselineOffset);
        } else if (vertical_align == IdentValue.TEXT_TOP) {
            // the top of this text is equal to the top of the parent's text
            // so we take the parent's height above the baseline and subtract our
            // height above the baseline
            baselineOffset = (int) (blockLineMetrics.getAscent() - ascent);
            //XRLog.render("baseline offset for text-top"+baselineOffset);
        } else if (vertical_align == IdentValue.TEXT_BOTTOM) {
            baselineOffset = -(int) (blockLineMetrics.getDescent() - descent);
            //XRLog.render("baseline offset for text-bottom"+baselineOffset);
        } else if (vertical_align == IdentValue.MIDDLE) {
            // just like firefox!
            int halfxheight = -(int) (blockLineMetrics.getStrikethroughOffset());
            int boxmiddle = (int) Math.round(new_inline.height / 2.0);
            int boxbase = (int) Math.round(ascent + leading / 2.0);
            int boxalign = boxbase - boxmiddle;
            baselineOffset = halfxheight - boxalign;
        } else if (vertical_align == IdentValue.TOP) {
            // like firefox, so I suppose it's correct...
            baselineOffset = curr_line.getBaseline() - lineHeight;
        } else if (vertical_align == IdentValue.BOTTOM) {
            baselineOffset = descent - (curr_line.height - curr_line.getBaseline());
        } else {
            baselineOffset = (int) c.getCurrentStyle().getFloatPropertyProportionalHeight(CSSName.VERTICAL_ALIGN, c.getBlockFormattingContext().getHeight(), c.getCtx());
        }
        return baselineOffset;
    }
}

