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
        breakText(c, context, avail, fullLineWidth, style, 0);
    }

    /**
     * Breaks the text as {@link #breakText(LayoutContext, LineBreakContext, int, int, CalculatedStyle)} does, where the
     * text after it continues with no break point for {@code gluedWidthAfter} (the text of the next inline box, see
     * {@code org.xhtmlrenderer.layout.InlineBoundaryBreaks}): the line ends with the text only if that fits as well.
     */
    public static void breakText(LayoutContext c,
            LineBreakContext context, int avail, int fullLineWidth, CalculatedStyle style, int gluedWidthAfter) {
        FSFont font = style.getFSFont(c);
        IdentValue whitespace = style.getWhitespace();

        // ====== handle nowrap
        if (whitespace == IdentValue.NOWRAP) {
            context.setEnd(context.getLast());
            context.setWidth(TextUtil.textWidth(c, style, font, context.getCalculatedSubstring()));
            checkGluedWidthFits(context, avail, gluedWidthAfter);
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
                checkGluedWidthFits(context, avail, gluedWidthAfter);
            } else {
                // The last line: its box asks for its width measured whole (InlineBox.calcMaxWidthFromLineLength), which
                // may be less than its parts added up, so it is measured whole here too before it is broken (#742)
                int width = TextUtil.textWidth(c, style, font, context.getStartSubstring());
                if (width + gluedWidthAfter <= avail) {
                    context.setEnd(context.getLast());
                    context.setWidth(width);
                    return;
                }
            }
        }

        // check if we may wrap: a forced line break with no text before it (e.g. <br>) always ends the line,
        // even if the line already overflows (#491)
        if (whitespace == IdentValue.PRE ||
            context.isNeedsNewLine() && (context.getWidth() <= avail || context.getEnd() == context.getStart() + 1)) {
            return;
        }

        context.setEndsOnNL(false);

        boolean tryToBreakAnywhere = style.getWordBreak() == IdentValue.BREAK_ALL;

        doBreakText(c, context, avail, style, tryToBreakAnywhere, fullLineWidth, gluedWidthAfter);
    }

    /**
     * A text which may not break ends where the text glued to it starts: if the two do not fit, the line does not take
     * the text unless it is empty (see {@link org.xhtmlrenderer.layout.InlineBoxing}), and the text starts the next one.
     */
    private static void checkGluedWidthFits(LineBreakContext context, int avail, int gluedWidthAfter) {
        if (gluedWidthAfter > 0 && context.getWidth() + gluedWidthAfter > avail) {
            context.setNeedsNewLine(true);
            context.setUnbreakable(true);
        }
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
     * <p>
     * A line broken between any two characters ({@code word-break: break-all}, or a word {@code word-wrap: break-word}
     * breaks) is measured whole instead: its characters measured one by one, each rounded on its own, can add up to a
     * width far from the one the line is rendered with.
     * <p>
     * The end of the text is a break point only if the text glued to it ({@code gluedWidthAfter}) fits too.
     */
    private static void doBreakText(LayoutContext c,
            LineBreakContext context, int avail, CalculatedStyle style,
            boolean tryToBreakAnywhere, int fullLineWidth, int gluedWidthAfter) {
        FSFont f = style.getFSFont(c);
        String currentString = context.getStartSubstring();
        if (tryToBreakAnywhere) {
            breakAnywhere(c, context, avail, style, f, currentString, fullLineWidth, gluedWidthAfter);
            return;
        }
        BreakPointsProvider iterator = getBreakPointsProvider(currentString, c, context.getTextNode(), style);
        BreakPoint bp = iterator.next();
        BreakPoint lastBreakPoint = null;
        int right = -1;
        int rightWidth = 0;
        int previousWidth = 0;
        int previousPosition = 0;
        while (bp != null && bp.position() != BreakIterator.DONE) {
            String part = currentString.substring(previousPosition, bp.position());
            int partWidth = TextUtil.textWidth(c, style, f, part);
            // A hyphen is printed only where the line wraps, so neither the text after this break point nor a line
            // ending with the text is wider by it
            boolean printsHyphen = !bp.hyphen().isEmpty() && bp.position() < currentString.length();
            int widthWithHyphen = previousWidth + (printsHyphen ? TextUtil.textWidth(c, style, f, part + bp.hyphen()) : partWidth);
            previousWidth += partWidth;
            previousPosition = bp.position();
            int glued = previousPosition == currentString.length() ? gluedWidthAfter : 0;
            if (widthWithHyphen + glued > avail) break;
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
            fits = fitsWidth + gluedWidthAfter <= avail;
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

        // No break point fits: the loop stopped at the first one, which ends the first word
        BreakPoint firstWordEnd = right < 0 && bp != null && bp.position() != BreakIterator.DONE ? bp : null;
        wrap(c, context, avail, style, f, currentString, false, fullLineWidth, right, rightWidth, gluedWidthAfter, firstWordEnd);
    }

    /**
     * Ends a line which may break between any two characters ({@code word-break: break-all}, or a word that
     * {@code word-wrap: break-word} breaks) after the most code points that fit, measuring the line whole, as it is
     * rendered: its characters measured one by one, each rounded on its own, can add up to a width far from that.
     * <p>
     * The width grows with the prefix, so the prefix is found with a few measurements rather than one per character:
     * prefixes of 1, 2, 4, 8 code points and so on until one does not fit, then a binary search between the last two.
     * That is about two measurements per doubling of the line, each of at most twice its length, and the remaining text
     * is measured whole only when it fits.
     * <p>
     * A text which fits whole, but not with the text glued to it ({@code gluedWidthAfter}), is not broken: on a line
     * with something before it, it moves with that text to the next line.
     */
    private static void breakAnywhere(LayoutContext c, LineBreakContext context, int avail, CalculatedStyle style, FSFont f,
            String currentString, int fullLineWidth, int gluedWidthAfter) {
        int codePoints = currentString.codePointCount(0, currentString.length());
        int fitting = 0;
        int fittingWidth = 0;
        int notFitting = codePoints + 1;
        int probe = 1;
        while (probe <= codePoints) {
            int width = prefixWidth(c, style, f, currentString, probe);
            if (width > avail) {
                notFitting = probe;
                break;
            }
            fitting = probe;
            fittingWidth = width;
            if (probe == codePoints) {
                break;
            }
            probe = Math.min(probe * 2, codePoints);
        }
        // Only when a prefix did not fit: when the whole text fits, the two are one apart already
        while (notFitting - fitting > 1) {
            int middle = (fitting + notFitting) >>> 1;
            int width = prefixWidth(c, style, f, currentString, middle);
            if (width <= avail) {
                fitting = middle;
                fittingWidth = width;
            } else {
                notFitting = middle;
            }
        }
        boolean gluedFits = fittingWidth + gluedWidthAfter <= avail || avail >= fullLineWidth;
        if (fitting == codePoints && gluedFits) {
            context.setWidth(codePoints == 0 ? TextUtil.textWidth(c, style, f, currentString) : fittingWidth);
            context.setEnd(context.getMaster().length());
            return;
        }
        int right = fitting == 0 || fitting == codePoints ? -1 : currentString.offsetByCodePoints(0, fitting);
        BreakPoint firstCodePoint = new BreakPoint(currentString.offsetByCodePoints(0, 1));
        wrap(c, context, avail, style, f, currentString, true, fullLineWidth, right, fittingWidth, gluedWidthAfter, firstCodePoint);
    }

    private static int prefixWidth(LayoutContext c, CalculatedStyle style, FSFont f, String text, int codePoints) {
        return TextUtil.textWidth(c, style, f, text.substring(0, text.offsetByCodePoints(0, codePoints)));
    }

    /**
     * Ends a line that does not fit whole: at {@code right}, the last break point that fits, or as the style asks.
     * <p>
     * Where no break point fits, the line holds the text up to {@code firstWordEnd}, the first break point, even if it
     * does not fit: the text after it goes to the next line. With no break point, the line holds all the text.
     */
    private static void wrap(LayoutContext c, LineBreakContext context, int avail, CalculatedStyle style, FSFont f,
            String currentString, boolean tryToBreakAnywhere, int fullLineWidth, int right, int rightWidth,
            int gluedWidthAfter, @Nullable BreakPoint firstWordEnd) {
        context.setNeedsNewLine(true);
        if (right <= 0 && style.getWordWrap() == IdentValue.BREAK_WORD) {
            if (!tryToBreakAnywhere) {
                doBreakText(c, context, avail, style, true, fullLineWidth, gluedWidthAfter);
                context.setBrokenInsideWord(!context.isUnbreakable() && !context.isFinished());
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
        if (firstWordEnd != null && firstWordEnd.position() > 0 && firstWordEnd.position() < currentString.length()) {
            int end = firstWordEnd.position();
            if (!firstWordEnd.hyphen().isEmpty()) {
                context.setMaster(new StringBuilder(context.getMaster()).insert(context.getStart() + end, firstWordEnd.hyphen()).toString());
                end += firstWordEnd.hyphen().length();
            }
            context.setEnd(context.getStart() + end);
        } else {
            context.setEnd(context.getStart() + currentString.length());
        }
        context.setUnbreakable(true);
        context.setWidth(TextUtil.textWidth(c, style, f, context.getCalculatedSubstring()));
    }

}


