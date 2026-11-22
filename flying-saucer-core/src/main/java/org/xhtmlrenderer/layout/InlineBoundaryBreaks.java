package org.xhtmlrenderer.layout;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xhtmlrenderer.css.extend.ContentFunction;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle.Edge;
import org.xhtmlrenderer.layout.breaker.BreakPoint;
import org.xhtmlrenderer.layout.breaker.Breaker;
import org.xhtmlrenderer.render.BlockBox;
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
 * An inline-block or an inline-table ends the text before it. The text after it is glued to it only where their
 * nearest common ancestor does not let the line wrap. A box out of the flow, or an inline box with no text, is
 * skipped. A text which may break between any two letters ({@code word-break: break-all}) is glued
 * to nothing, unless {@code white-space} does not let it wrap.
 */
final class InlineBoundaryBreaks {
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

    InlineBoundaryBreaks(LayoutContext c, List<Styleable> content, int cbWidth) {
        this.c = c;
        this.content = content;
        this.cbWidth = cbWidth;
        this.leads = new Lead[content.size()];
    }

    /** No break point between the text of the box at {@code index} and the text before it. */
    @CheckReturnValue
    boolean startsGlued(int index) {
        return lead(index).glued();
    }

    /** The width of the text after the box at {@code index}, up to the next break point: the line may not end before it. */
    @CheckReturnValue
    int gluedWidthAfter(int index) {
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
        Styleable previousContent = previousContent(index);
        if (previousContent instanceof BlockBox atomic) {
            return mayBreakAfter(atomic, box) ? Lead.NOT_GLUED : lead(box, firstBreakPoint(box));
        }
        if (!(previousContent instanceof InlineBox previous) || breaksAnywhere(previous.getStyle())) {
            return Lead.NOT_GLUED;
        }

        CalculatedStyle style = box.getStyle();
        String before = text(previous);
        if (before.charAt(before.length() - 1) == WhitespaceStripper.EOLC) {
            return Lead.NOT_GLUED; // a forced line break ends the text before
        }
        String text = text(box);
        String joined = before + text;
        var iterator = Breaker.getBreakPointsProvider(joined, c, box.getElement(), style);
        BreakPoint bp = iterator.next();
        while (bp.position() != BreakIterator.DONE && bp.position() < before.length()) {
            bp = iterator.next();
        }
        if (bp.position() == before.length()) {
            if (mayBreakBetween(previous, box, before)) {
                return Lead.NOT_GLUED;
            }
            bp = iterator.next();
        }

        return lead(box, bp.position() == BreakIterator.DONE ? text.length() : bp.position() - before.length());
    }

    /** The first break point in the text of the box, after its first character. */
    private int firstBreakPoint(InlineBox box) {
        String text = text(box);
        var iterator = Breaker.getBreakPointsProvider(text, c, box.getElement(), box.getStyle());
        BreakPoint bp = iterator.next();
        while (bp.position() == 0) {
            bp = iterator.next();
        }
        return bp.position() == BreakIterator.DONE ? text.length() : bp.position();
    }

    /** The text of the box up to {@code breakPoint}, glued to the content before it. */
    private Lead lead(InlineBox box, int breakPoint) {
        CalculatedStyle style = box.getStyle();
        String text = text(box);
        int end = isUnbreakable(style) ? text.length() : breakPoint;
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

    /** The nearest content before the box at {@code index}: a box with text, an inline-block or an inline-table. */
    @Nullable
    private Styleable previousContent(int index) {
        for (int i = index - 1; i >= 0; i--) {
            Styleable child = content.get(i);
            if (!isTransparent(child)) {
                return child;
            }
        }
        return null;
    }

    /**
     * The nearest common ancestor of an inline-block or an inline-table and the text after it decides if the line may
     * break between them (<a href="https://www.w3.org/TR/css-text-3/#line-break-details">CSS Text 3, § 5.5</a>).
     */
    private boolean mayBreakAfter(BlockBox atomic, InlineBox box) {
        Element ancestor = commonAncestor(atomic.getElement(), owner(box));
        if (ancestor == null) {
            return !isUnbreakable(box.getStyle());
        }
        return !isUnbreakable(c.getSharedContext().getStyle(ancestor));
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

    /**
     * The break point between two boxes is a place to break only where the boxes may wrap
     * (<a href="https://www.w3.org/TR/css-text-3/#line-break-details">CSS Text 3, § 5.5</a>). The box which holds a
     * space that disappears at the break decides alone. After any other character, such as a hyphen, an em space or a
     * preserved space, the nearest common ancestor of the two boxes decides.
     */
    private boolean mayBreakBetween(InlineBox previous, InlineBox box, String before) {
        char last = before.charAt(before.length() - 1);
        if ((last == ' ' || last == '\t') && collapsesSpaces(previous.getStyle())) {
            return !isUnbreakable(previous.getStyle());
        }
        Element ancestor = commonAncestor(owner(previous), owner(box));
        if (ancestor == null) {
            return !isUnbreakable(previous.getStyle()) && !isUnbreakable(box.getStyle());
        }
        return !isUnbreakable(c.getSharedContext().getStyle(ancestor));
    }

    /**
     * The element whose content the box is: the parent of its text, or the element of its pseudo-element. Styles are
     * shared between elements, so the elements, not the styles, tell where a box is.
     */
    @Nullable
    private static Element owner(InlineBox box) {
        Text text = box.getTextNode();
        if (text != null) {
            return text.getParentNode() instanceof Element parent ? parent : null;
        }
        return box.getElement();
    }

    /** The nearest element which is or holds both elements, or {@code null} if none. */
    @Nullable
    private static Element commonAncestor(@Nullable Element first, @Nullable Element second) {
        for (Node a = first; a instanceof Element; a = a.getParentNode()) {
            for (Node b = second; b instanceof Element; b = b.getParentNode()) {
                if (a == b) {
                    return (Element) a;
                }
            }
        }
        return null;
    }

    private static boolean isUnbreakable(CalculatedStyle style) {
        IdentValue whitespace = style.getWhitespace();
        return whitespace == IdentValue.NOWRAP || whitespace == IdentValue.PRE;
    }

    /** The spaces of the box collapse, so a space at the end of a line disappears. */
    private static boolean collapsesSpaces(CalculatedStyle style) {
        IdentValue whitespace = style.getWhitespace();
        return whitespace == IdentValue.NORMAL || whitespace == IdentValue.NOWRAP || whitespace == IdentValue.PRE_LINE;
    }

    /** {@code word-break: break-all} adds places to break only where {@code white-space} lets the text wrap. */
    private static boolean breaksAnywhere(CalculatedStyle style) {
        return style.getWordBreak() == IdentValue.BREAK_ALL && !isUnbreakable(style);
    }

    private static String stripTrailingSpace(String text) {
        int end = text.length();
        while (end > 0 && Character.isWhitespace(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end);
    }
}
