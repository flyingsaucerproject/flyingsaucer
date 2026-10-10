/*
 * Breaker.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm,
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
package org.xhtmlrenderer.layout.breaker;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.LineBreakContext;
import org.xhtmlrenderer.layout.TextUtil;
import org.xhtmlrenderer.layout.WhitespaceStripper;
import org.xhtmlrenderer.render.FSFont;

import java.text.BreakIterator;

/**
 * A utility class that scans the text of a single inline box, looking for the
 * next break point.
 * @author Torbjoern Gannholm
 */
public class Breaker {

    private static final String DEFAULT_LANGUAGE = System.getProperty("org.xhtmlrenderer.layout.breaker.default-language", "en");

    public static void breakFirstLetter(LayoutContext c, LineBreakContext context,
            int avail, CalculatedStyle style) {
        FSFont font = style.getFSFont(c);
        context.setEnd(getFirstLetterEnd(context.getMaster(), context.getStart()));
        context.setWidth(TextUtil.textWidth(c, style, font, context.getCalculatedSubstring()));

        if (context.getWidth() > avail) {
            context.setNeedsNewLine(true);
            context.setUnbreakable(true);
        }
    }

    private static int getFirstLetterEnd(String text, int start) {
        boolean letterFound = false;
        int end = text.length();
        char currentChar;
        for ( int i = start; i < end; i++ ) {
            currentChar = text.charAt(i);
            if (!TextUtil.isFirstLetterSeparatorChar(currentChar)) {
                if (letterFound) {
                    return i;
                } else {
                    letterFound = true;
                }
            }
        }
        return end;
    }

    public static void breakText(LayoutContext c,
            LineBreakContext context, int avail, int fullLineWidth, CalculatedStyle style) {
        FSFont font = style.getFSFont(c);
        IdentValue whitespace = style.getWhitespace();

        // ====== handle nowrap
        if (whitespace == IdentValue.NOWRAP) {
            context.setEnd(context.getLast());
            context.setWidth(TextUtil.textWidth(c, style, font, context.getCalculatedSubstring()));
            return;
        }

        //check if we should break on the next newline
        if (whitespace == IdentValue.PRE ||
                whitespace == IdentValue.PRE_WRAP ||
                whitespace == IdentValue.PRE_LINE) {
            int n = context.getStartSubstring().indexOf(WhitespaceStripper.EOL);
            if (n > -1) {
                context.setEnd(context.getStart() + n + 1);
                context.setWidth(TextUtil.textWidth(c, style, font, context.getStartSubstring().substring(0, n)));
                context.setNeedsNewLine(true);
                context.setEndsOnNL(true);
            } else if (whitespace == IdentValue.PRE) {
                context.setEnd(context.getLast());
                context.setWidth(TextUtil.textWidth(c, style, font, context.getCalculatedSubstring()));
            } else {
                // The last line: its box asks for its width measured whole (InlineBox.calcMaxWidthFromLineLength), which
                // may be less than its parts added up, so it is measured whole here too before it is broken (#742)
                int width = TextUtil.textWidth(c, style, font, context.getStartSubstring());
                if (width <= avail) {
                    context.setEnd(context.getLast());
                    context.setWidth(width);
                    return;
                }
            }
        }

        //check if we may wrap
        if (whitespace == IdentValue.PRE ||
            context.isNeedsNewLine() && context.getWidth() <= avail) {
            return;
        }

        context.setEndsOnNL(false);

        boolean tryToBreakAnywhere = style.getWordBreak() == IdentValue.BREAK_ALL;

        doBreakText(c, context, avail, style, tryToBreakAnywhere, fullLineWidth);
    }

    public static BreakPointsProvider getBreakPointsProvider(String text, LayoutContext c, Element element, CalculatedStyle style) {
        return c.getSharedContext().getLineBreakingStrategy().getBreakPointsProvider(text, getLanguage(c, element), style);
    }

    public static BreakPointsProvider getBreakPointsProvider(String text, LayoutContext c, Text textNode, CalculatedStyle style) {
        return c.getSharedContext().getLineBreakingStrategy().getBreakPointsProvider(text, getLanguage(c, textNode), style);
    }

    @CheckReturnValue
    private static String getLanguage(LayoutContext c, @Nullable Element element) {
        String language = element == null ? null : c.getNamespaceHandler().getLang(element);
        if (language == null || language.isEmpty()) {
            language = DEFAULT_LANGUAGE;
        }
        return language;
    }

    @CheckReturnValue
    private static String getLanguage(LayoutContext c, @Nullable Text textNode) {
        if (textNode != null) {
            Node parentNode = textNode.getParentNode();
            if (parentNode instanceof Element element) {
                return getLanguage(c, element);
            }
        }
        return DEFAULT_LANGUAGE;
    }

    /**
     * Ends the line at the last break point whose text fits {@code avail}, and gives it the width that decided it fits.
     * <p>
     * The width is the sum of the widths of the text between the break points, plus the hyphen of the break point the
     * line ends at. A text measured whole may be wider than its parts added up (rounding, kerning), and the parts are
     * what the width of a table cell is made of: recording the whole would let the line exceed the width it was laid
     * out in, and a {@code <br>} after it then make an empty line (#742).
     * <p>
     * A line which fits whole gets no hyphen, so the hyphen of a break point at the end of the text does not count. Its
     * width adds the text after the last break point, as a {@link LineBreakingStrategy} need not end with a break point
     * at the end of the text, and where that text does not fit, the line wraps at the last break point.
     */
    private static void doBreakText(LayoutContext c,
            LineBreakContext context, int avail, CalculatedStyle style,
            boolean tryToBreakAnywhere, int fullLineWidth) {
        FSFont f = style.getFSFont(c);
        String currentString = context.getStartSubstring();
        BreakPointsProvider iterator = getBreakPointsProvider(currentString, c, context.getTextNode(), style);
        if (tryToBreakAnywhere) {
            iterator = new BreakAnywhereLineBreakStrategy(currentString);
        }
        BreakPoint bp = iterator.next();
        BreakPoint lastBreakPoint = null;
        int right = -1;
        int rightWidth = 0;
        int previousWidth = 0;
        int previousPosition = 0;
        while (bp != null && bp.position() != BreakIterator.DONE) {
            String part = currentString.substring(previousPosition, bp.position());
            int partWidth = TextUtil.textWidth(c, style, f, part);
            // A hyphen is printed only where the line wraps, so neither the text after this break point nor a line ending
            // with the text is wider by it
            boolean printsHyphen = !bp.hyphen().isEmpty() && bp.position() < currentString.length();
            int widthWithHyphen = previousWidth + (printsHyphen ? TextUtil.textWidth(c, style, f, part + bp.hyphen()) : partWidth);
            previousWidth += partWidth;
            previousPosition = bp.position();
            if (widthWithHyphen > avail) break;
            right = previousPosition;
            rightWidth = widthWithHyphen;
            lastBreakPoint = bp;
            bp = iterator.next();
        }

        // Every break point fits: so does the line, if the text after the last one does
        boolean fits = false;
        int fitsWidth = 0;
        if (bp != null && bp.position() == BreakIterator.DONE) {
            String tail = currentString.substring(previousPosition);
            fitsWidth = previousWidth + (tail.isEmpty() ? 0 : TextUtil.textWidth(c, style, f, tail));
            fits = fitsWidth <= avail;
        }

        // add hyphen if needed
        if (bp != null && !fits
                && right >= 0 // some break point found
                && !lastBreakPoint.hyphen().isEmpty()) {
            context.setMaster(new StringBuilder(context.getMaster()).insert(context.getStart() + right, lastBreakPoint.hyphen()).toString());
            right += lastBreakPoint.hyphen().length();
        }

        if (fits) {
            context.setWidth(fitsWidth);
            context.setEnd(context.getMaster().length());
            //It fits!
            return;
        }

        context.setNeedsNewLine(true);
        if (right <= 0 && style.getWordWrap() == IdentValue.BREAK_WORD) {
            if (!tryToBreakAnywhere) {
                doBreakText(c, context, avail, style, true, fullLineWidth);
                return;
            }

            if (avail < fullLineWidth) {
                // Float reduced avail — word may fit on next full line
                // → unbreakable: InlineBoxing's getNextLineBoxDelta moves past float
                context.setEnd(context.getStart() + currentString.length());
                context.setUnbreakable(true);
                context.setWidth(TextUtil.textWidth(c, style, f, context.getCalculatedSubstring()));
            } else {
                // avail IS the full line width → container genuinely too narrow
                // → force a break after one code point (browser behaviour),
                // keeping surrogate pairs intact
                int oneCodePoint = currentString.offsetByCodePoints(0, 1);
                context.setEnd(context.getStart() + oneCodePoint);
                context.setWidth(TextUtil.textWidth(c, style, f, currentString.substring(0, oneCodePoint)));
            }
            return;
        }

        if (right > 0) { // found a place to wrap
            context.setEnd(context.getStart() + right);
            context.setWidth(rightWidth);
            return;
        }

        // unbreakable string
        context.setEnd(context.getStart() + currentString.length());
        context.setUnbreakable(true);
        context.setWidth(TextUtil.textWidth(c, style, f, context.getCalculatedSubstring()));
    }

}


