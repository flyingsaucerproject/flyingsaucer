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

import java.awt.font.LineMetrics;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.InlineTextBox;
import org.xhtmlrenderer.render.LineBox;


/**
 * Description of the Class
 *
 * @author   Torbjörn Gannholm
 */
public class VerticalAlign {

    /**
     * returns how much the baseline should be raised (negative means lowered)
     *
     * @param c                 PARAM
     * @param curr_line         PARAM
     * @param new_inline        PARAM
     * @param blockLineHeight   PARAM
     * @param blockLineMetrics  PARAM
     * @return                  The baselineOffset value
     */
    public static int getBaselineOffset( Context c, LineBox curr_line, InlineBox new_inline, int blockLineHeight, LineMetrics blockLineMetrics ) {
        int lineHeight;
        int ascent;
        int descent;
        int baselineOffset;
        if ( new_inline instanceof InlineTextBox ) {
            // should be the metrics of the font, actually is the metrics of the text
            LineMetrics metrics = FontUtil.getLineMetrics( c, new_inline );
            lineHeight = FontUtil.lineHeight( c );//assume that current context is valid for new_inline
            ascent = (int)metrics.getAscent();
            descent = (int)metrics.getDescent();
        } else {
            lineHeight = new_inline.height;
            ascent = lineHeight;
            descent = 0;
        }

        IdentValue vertical_align = c.getCurrentStyle().getIdent( CSSName.VERTICAL_ALIGN );
        if ( vertical_align == IdentValue.BASELINE ) {
            baselineOffset = 0;
        } else if ( vertical_align == IdentValue.SUPER ) {
            // works okay i think
            baselineOffset = (int)( -blockLineMetrics.getStrikethroughOffset() * 2.0 );//up is negative in Java!
            //XRLog.render("baseline offset for super "+baselineOffset);
        } else if ( vertical_align == IdentValue.SUB ) {
            // works okay i think
            baselineOffset = (int)blockLineMetrics.getStrikethroughOffset();//up is negative in Java!
            //XRLog.render("baseline offset for sub "+baselineOffset);
        } else if ( vertical_align == IdentValue.TEXT_TOP ) {
            // the top of this text is equal to the top of the parent's text
            // so we take the parent's height above the baseline and subtract our
            // height above the baseline
            baselineOffset = (int)( blockLineMetrics.getAscent() - ascent );
            //XRLog.render("baseline offset for text-top"+baselineOffset);
        } else if ( vertical_align == IdentValue.TEXT_BOTTOM ) {
            baselineOffset = -(int)( blockLineMetrics.getDescent() - descent );
            //XRLog.render("baseline offset for text-bottom"+baselineOffset);
        } else if ( vertical_align == IdentValue.TOP ) {
            //oops, this will be difficult because we need to keep track of the element sub-tree!
            //HACK: for now, just align the top of this box with the top of the line
            baselineOffset = curr_line.getBaseline() - ascent;
        } else if ( vertical_align == IdentValue.BOTTOM ) {
            //oops, this will be difficult because we need to keep track of the element sub-tree!
            //HACK: for now, just align the top of this box with the top of the line
            baselineOffset = descent - ( curr_line.height - curr_line.getBaseline() );
        } else {
            baselineOffset = (int)c.getCurrentStyle().getFloatPropertyProportionalHeight( CSSName.VERTICAL_ALIGN, c.getBlockFormattingContext().getHeight() );
        }
        return baselineOffset;
    }
}

