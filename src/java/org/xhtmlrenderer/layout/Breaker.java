/*
 * Breaker.java
 * Copyright (c) 2004, 2005 Torbjörn Gannholm, 
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
 *
 */
package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.layout.FontUtil;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.content.WhitespaceStripper;

import java.awt.Font;

/**
 * @author Torbjörn Gannholm
 */
public class Breaker {

    public static void breakFirstLetter(LayoutContext c, LineBreakContext context,
            int avail, Font font) {
        context.setEnd(getFirstLetterEnd(context.getMaster(), context.getStart()));
        context.setWidth(FontUtil.len(context.getCalculatedSubstring(), font,
                c.getTextRenderer(), c.getGraphics()));
        
        if (context.getWidth() > avail) {
            context.setNeedsNewLine(true);
            context.setUnbreakable(true);
        }
    }
    
    private static int getFirstLetterEnd(String text, int start) {
        int i = start;
        while (i < text.length()) {
            char c = text.charAt(i);
            int type = Character.getType(c);
            if (type == Character.START_PUNCTUATION || 
                    type == Character.END_PUNCTUATION ||
                    type == Character.INITIAL_QUOTE_PUNCTUATION ||
                    type == Character.FINAL_QUOTE_PUNCTUATION ||
                    type == Character.OTHER_PUNCTUATION) {
                i++;
            } else {
                break;
            }
        }
        if (i < text.length()) {
            i++;
        }
        return i;
    }    
    
    public static void breakText(LayoutContext c, 
            LineBreakContext context, int avail, IdentValue whitespace, Font font) {
        // ====== handle nowrap
        if (whitespace == IdentValue.NOWRAP) {
        	context.setEnd(context.getLast());
        	context.setWidth(FontUtil.len(context.getCalculatedSubstring(), font,
        			c.getTextRenderer(), c.getGraphics()));
            return;
        }

        //check if we should break on the next newline
        if (whitespace == IdentValue.PRE ||
                whitespace == IdentValue.PRE_WRAP ||
                whitespace == IdentValue.PRE_LINE) {
            int n = context.getStartSubstring().indexOf(WhitespaceStripper.EOL);
            if (n > -1) {
                context.setEnd(context.getStart() + n + 1);
                context.setWidth(FontUtil.len(context.getCalculatedSubstring(), font, 
                        c.getTextRenderer(), c.getGraphics()));                
                context.setNeedsNewLine(true);
            } else if (whitespace == IdentValue.PRE) {
            	context.setEnd(context.getLast());
                context.setWidth(FontUtil.len(context.getCalculatedSubstring(), font, 
                        c.getTextRenderer(), c.getGraphics()));   
            }
        }

        //check if we may wrap
        if (whitespace == IdentValue.PRE) {
            return;
        }

        String currentString = context.getStartSubstring();
        int left = 0;
        int right = currentString.indexOf(WhitespaceStripper.SPACE, left + 1);
        int lastWrap = 0;
        int graphicsLength = 0;
        int lastGraphicsLength = 0;

        while (right > 0 && graphicsLength <= avail) {
            lastGraphicsLength = graphicsLength;
            graphicsLength += FontUtil.len(currentString.substring(left, right), 
                    font, c.getTextRenderer(), c.getGraphics());
            lastWrap = left;
            left = right;
            right = currentString.indexOf(WhitespaceStripper.SPACE, left + 1);
        }

        if (graphicsLength <= avail) {
            //try for the last bit too!
            lastWrap = left;
            lastGraphicsLength = graphicsLength;
            graphicsLength += FontUtil.len(currentString.substring(left), font, 
                    c.getTextRenderer(), c.getGraphics());
        }

        if (graphicsLength <= avail) {
            context.setWidth(graphicsLength);
            context.setEnd(context.getMaster().length());
            //It fit!
            return;
        }
        
        context.setNeedsNewLine(true);

        if (lastWrap != 0) {//found a place to wrap
            context.setEnd(context.getStart() + lastWrap);
            context.setWidth(lastGraphicsLength);
        } else {//unbreakable string
            if (left == 0) {
                left = currentString.length();
            }
            
            context.setEnd(context.getStart() + left);
            context.setUnbreakable(true);
            
            if (left == 0) {
                context.setWidth(FontUtil.len(context.getCalculatedSubstring(), font, 
                        c.getTextRenderer(), c.getGraphics()));
            } else {
                context.setWidth(graphicsLength);
            }
        }
        return;
    }

}

