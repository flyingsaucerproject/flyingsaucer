package org.xhtmlrenderer.layout;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.xhtmlrenderer.css.extend.ContentFunction;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle.Edge;
import org.xhtmlrenderer.layout.breaker.BreakPoint;
import org.xhtmlrenderer.layout.breaker.Breaker;
import org.xhtmlrenderer.render.InlineBox;

import java.text.BreakIterator;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Finds where the text of a block runs across the boundary of two of its inline boxes with no place to break.
 * <p>
 * Line breaking ignores the boundaries of inline boxes
 * (<a href="https://www.w3.org/TR/css-text-3/#text-encoding">CSS Text 3, § 1.5</a>), so {@code aaa<span>bbb</span>}
 * breaks as the one word {@code aaabbb}, and {@code aaa&#160;<span>bbb</span>} as {@code aaa&#160;bbb}. The text of
 * a box is glued to the text before it if the line breaking strategy finds no break point between the two.
 * <p>
 * An inline-block or an inline-table ends the text on either side of it. A box out of the flow, or an inline box
 * with no text, does not. A text which may break between any two letters ({@code word-break: break-all}) is glued
 * to nothing.
 */
public final class InlineBoundaryBreaks {
    private final LayoutContext c;
    private final List<Styleable> content;
    private final int cbWidth;
    private final @Nullable Lead[] leads;

    /**
     * The text of a box up to its first break point.
     *
     * @param glued no break point between this text and the text before it
     * @param width the width of the text, with the inline margin, border and padding at its ends
     * @param whole the text is all of the text of the box: the next box may continue it
     */
    private record Lead(boolean glued, int width, boolean whole) {
        private static final Lead NOT_GLUED = new Lead(false, 0, false);
    }

    public InlineBoundaryBreaks(LayoutContext c, List<Styleable> content, int cbWidth) {
        this.c = c;
        this.content = content;
        this.cbWidth = cbWidth;
        this.leads = new Lead[content.size()];
    }

    /** No break point between the text of the box at {@code index} and the text before it. */
    @CheckReturnValue
    public boolean startsGlued(int index) {
        return lead(index).glued();
    }

    /** The width of the text after the box at {@code index}, up to the next break point: the line may not end before it. */
    @CheckReturnValue
    public int gluedWidthAfter(int index) {
        int width = 0;
        for (int i = index + 1; i < content.size(); i++) {
            Styleable child = content.get(i);
            if (isTransparent(child)) {
                continue;
            }
            Lead lead = lead(i);
            if (!lead.glued()) {
                break;
            }
            width += lead.width();
            if (!lead.whole()) {
                break;
            }
        }
        return width;
    }

    private Lead lead(int index) {
        Lead lead = leads[index];
        if (lead == null) {
            lead = calcLead(index);
            leads[index] = lead;
        }
        return lead;
    }

    private Lead calcLead(int index) {
        if (!(content.get(index) instanceof InlineBox box) || text(box).isEmpty() || breaksAnywhere(box.getStyle())) {
            return Lead.NOT_GLUED;
        }
        InlineBox previous = previousText(index);
        if (previous == null || breaksAnywhere(previous.getStyle())) {
            return Lead.NOT_GLUED;
        }

        CalculatedStyle style = box.getStyle();
        String before = text(previous);
        String text = text(box);
        String joined = before + text;
        var iterator = Breaker.getBreakPointsProvider(joined, c, box.getElement(), style);
        BreakPoint bp = iterator.next();
        while (bp.position() != BreakIterator.DONE && bp.position() < before.length()) {
            bp = iterator.next();
        }
        if (bp.position() == before.length()) {
            return Lead.NOT_GLUED;
        }

        int end = bp.position() == BreakIterator.DONE || isUnbreakable(style) ? text.length() : bp.position() - before.length();
        int newLine = text.indexOf(WhitespaceStripper.EOLC);
        if (newLine >= 0 && newLine < end) {
            end = newLine;
        }
        String lead = stripTrailingSpace(text.substring(0, end));
        if (lead.isEmpty()) {
            return Lead.NOT_GLUED;
        }

        boolean whole = end == text.length();
        int width = textWidth(style, lead);
        if (box.isStartsHere()) {
            width += style.getMarginBorderPadding(c, cbWidth, Edge.LEFT);
        }
        if (whole && box.isEndsHere()) {
            width += style.getMarginBorderPadding(c, cbWidth, Edge.RIGHT);
        }
        return new Lead(true, width, whole);
    }

    /** The nearest box before the one at {@code index} that has text, unless something else ends the text before. */
    @Nullable
    private InlineBox previousText(int index) {
        for (int i = index - 1; i >= 0; i--) {
            Styleable child = content.get(i);
            if (child instanceof InlineBox box && !text(box).isEmpty()) {
                return box;
            }
            if (!isTransparent(child)) {
                return null;
            }
        }
        return null;
    }

    /** A box which is not a place to break and has no text: out of the flow, or an inline box with no text. */
    private boolean isTransparent(Styleable child) {
        if (child instanceof InlineBox box) {
            return text(box).isEmpty();
        }
        return requireNonNull(child.getStyle()).isNonFlowContent();
    }

    private int textWidth(CalculatedStyle style, String text) {
        return text.isEmpty() ? 0 : TextUtil.textWidth(c, style, style.getFSFont(c), text);
    }

    private static String text(InlineBox box) {
        return box.getContentFunction() instanceof ContentFunction function ? function.getLayoutReplacementText() : box.getText();
    }

    private static boolean isUnbreakable(CalculatedStyle style) {
        IdentValue whitespace = style.getWhitespace();
        return whitespace == IdentValue.NOWRAP || whitespace == IdentValue.PRE;
    }

    private static boolean breaksAnywhere(CalculatedStyle style) {
        return style.getWordBreak() == IdentValue.BREAK_ALL;
    }

    private static String stripTrailingSpace(String text) {
        int end = text.length();
        while (end > 0 && Character.isWhitespace(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end);
    }
}
