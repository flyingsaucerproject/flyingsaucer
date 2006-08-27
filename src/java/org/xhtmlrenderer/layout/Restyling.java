/*
 * Copyright (c) 2005 Torbj�rn Gannholm
 * Copyright (c) 2006 Wisconsin Court System
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
package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.content.ContentUtil;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.LineBox;

/**
 * User: tobe
 */
public class Restyling {
    public static void restyleAll(LayoutContext c, BlockBox box) {
        if (box.element != null) {
            CalculatedStyle parentStyle = box.getStyle().getCalculatedStyle().getParent();
            c.initializeStyles(parentStyle);
        }//else root box, already initialized
        restyle(c, box);
    }

    private static void restyle(LayoutContext c, BlockBox box) {
        CascadedStyle style = c.getCss().getCascadedStyle(box.element, true);
        if (style == null) {
            style = CascadedStyle.emptyCascadedStyle;
        }
        c.pushStyle(style);
        CalculatedStyle calculatedStyle = c.getCurrentStyle();
        box.getStyle().setCalculatedStyle(calculatedStyle);
            
        CascadedStyle firstLine = null;
        if (ContentUtil.mayHaveFirstLine(calculatedStyle)) {
            firstLine = c.getCss().getPseudoElementStyle(box.element, "first-line");
        }
        //TODO: first-letter
        //special style for first line?
        if (firstLine != null) {
            c.getFirstLinesTracker().addStyle(firstLine);
        }

        if (box.containsLineBoxes()) {
            restyleInlineContext(c, box);
        } else {
            restyleBlockContext(c, box);
        }
            
        //pop in case not used
        if (firstLine != null) {
            c.getFirstLinesTracker().removeLast();
        }
    }

    private static void restyle(LayoutContext c, LineBox line) {
        // for each inline box
        for (int j = 0; j < line.getChildCount(); j++) {
            Box child = line.getChild(j);

            if (c.getFirstLinesTracker().hasStyles() && line.isFirstLine()) {
                c.getFirstLinesTracker().pushStyles(c);
            }
            if (child instanceof InlineLayoutBox) {
                restyle(c, (InlineLayoutBox)child);
            } else {
                restyle(c, (BlockBox)child);
            }
        }
        
        for (int j = 0; j < line.getNonFlowContent().size(); j++) {
            BlockBox child = (BlockBox)line.getNonFlowContent().get(j); 
            restyle(c, child);
        }

        if (c.getFirstLinesTracker().hasStyles() && line.isFirstLine()) {
            c.getFirstLinesTracker().popStyles(c);
            c.getFirstLinesTracker().clearStyles();
        }
    }

    private static void restyle(LayoutContext c, InlineLayoutBox iB) {
        c.pushStyle(c.getCss().getCascadedStyle(iB.element, true));
        iB.getStyle().setCalculatedStyle(c.getCurrentStyle());
        
        iB.calculateTextDecoration(c);
       
        for (int i = 0; i < iB.getInlineChildCount(); i++) {
            Object child = iB.getInlineChild(i);
            if (child instanceof InlineLayoutBox) {
                restyle(c, (InlineLayoutBox)child);
            } else if (child instanceof BlockBox) {
                restyle(c, (BlockBox)child);
            }
        }
        
        c.popStyle();
    }

    private static void restyleBlockContext(LayoutContext c, BlockBox box) {
        if (! box.isReplaced()) {
            for (int i = 0; i <= box.getChildCount(); i++) {
                Box child = (Box) box.getChild(i);
                restyle(c, (BlockBox) child);
            }
        }

    }
    
    private static void restyleInlineContext(LayoutContext c, Box block) {
        for (int i = 0; i < block.getChildCount(); i++) {
            LineBox line = (LineBox) block.getChild(i);
            restyle(c, line);
        }
    }
}

/*
 * $Id$
 *
 */
