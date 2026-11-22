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
 * to nothing, unless {@code white-space} does not let it wrap. The text after it is glued to it only where the
 * boundary between them may not break.
 */
final class InlineBoundaryBreaks {
    private static final char OGHAM_SPACE_MARK = '\u1680';

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

    /**
     * A border or padding follows the text of the box at {@code index} before any other content: a space separator at
     * the end of the text does not hang there (CSS Text 3, § 8.2). It is the right edge of a box which ends, or the left
     * edge of an empty box after the text. Lines run left to right. A margin does not count.
     */
    @CheckReturnValue
    boolean edgeAfterText(int index) {
        for (int i = index; i < content.size(); i++) {
            if (!(content.get(i) instanceof InlineBox box) || i > index && !disappears(box)) {
                return false;
            }
            CalculatedStyle style = box.getStyle();
            if (i > index && box.isStartsHere() && hasEdge(style, Edge.LEFT) || box.isEndsHere() && hasEdge(style, Edge.RIGHT)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEdge(CalculatedStyle style, Edge edge) {
        return edge == Edge.LEFT
                ? style.getBorder(c).left() > 0 || style.getPaddingRect(cbWidth, c).left() > 0
                : style.getBorder(c).right() > 0 || style.getPaddingRect(cbWidth, c).right() > 0;
    }

    /** The box has no text, or only spaces which disappear at the end of a line. */
    private static boolean disappears(InlineBox box) {
        String text = text(box);
        return text.isEmpty() || collapsesSpaces(box.getStyle()) && text.chars().allMatch(ch -> ch == ' ');
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
            return mayBreakAfter(atomic, box) ? Lead.NOT_GLUED : lead(index, box, firstBreakPoint(box));
        }
        if (!(previousContent instanceof InlineBox previous)) {
            return Lead.NOT_GLUED;
        }

        CalculatedStyle style = box.getStyle();
        String before = text(previous);
        if (before.charAt(before.length() - 1) == WhitespaceStripper.EOLC) {
            return Lead.NOT_GLUED; // a forced line break ends the text before
        }
        if (breaksAnywhere(previous.getStyle())) {
            // The text before may break after its last letter, where white-space lets the line wrap there
            return mayBreakBetween(previous, box, before) ? Lead.NOT_GLUED : lead(index, box, firstBreakPoint(box));
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

        return lead(index, box, bp.position() == BreakIterator.DONE ? text.length() : bp.position() - before.length());
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

    /** The text of the box at {@code index} up to {@code breakPoint}, glued to the content before it. */
    private Lead lead(int index, InlineBox box, int breakPoint) {
        CalculatedStyle style = box.getStyle();
        String text = text(box);
        int end = isUnbreakable(style) ? text.length() : breakPoint;
        int newLine = text.indexOf(WhitespaceStripper.EOLC);
        if (newLine >= 0 && newLine < end) {
            end = newLine;
        }
        // A space before a break point disappears or hangs at the end of a line, but a preserved space in pre stays, and
        // a space separator before a right border or padding does not hang
        boolean edgeAfter = end == text.length() && edgeAfterText(index);
        String lead = style.getWhitespace() == IdentValue.PRE ? text.substring(0, end)
                : stripTrailingSpace(text.substring(0, end), edgeAfter, collapsesSpaces(style));
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
        if (inSamePseudoElement(previous, box)) {
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

    /**
     * Both boxes are parts of the content of one pseudo-element, such as {@code content: "a" attr(title)}. The
     * pseudo-element holds both, so its style decides, not the style of its element.
     */
    private static boolean inSamePseudoElement(InlineBox previous, InlineBox box) {
        String pseudo = box.getPseudoElementOrClass();
        return pseudo != null && pseudo.equals(previous.getPseudoElementOrClass())
                && previous.getTextNode() == null && box.getTextNode() == null
                && previous.getElement() == box.getElement();
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

    /**
     * The text without its trailing white space. Where {@code keepSeparators}, the space separators which hang stay: all
     * but U+0020, and U+1680, which is removed where the spaces collapse (CSS Text 3, § 4.1.2).
     */
    private static String stripTrailingSpace(String text, boolean keepSeparators, boolean collapses) {
        int end = text.length();
        while (end > 0 && Character.isWhitespace(text.charAt(end - 1))
                && !(keepSeparators && hangs(text.charAt(end - 1), collapses))) {
            end--;
        }
        return text.substring(0, end);
    }

    private static boolean hangs(char ch, boolean collapses) {
        return ch != ' ' && !(collapses && ch == OGHAM_SPACE_MARK) && Character.getType(ch) == Character.SPACE_SEPARATOR;
    }
}
