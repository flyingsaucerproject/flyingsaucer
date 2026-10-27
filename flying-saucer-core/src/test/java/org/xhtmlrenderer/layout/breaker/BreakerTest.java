package org.xhtmlrenderer.layout.breaker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.LineBreakContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.FSFont;

import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

/**
 * The width {@link Breaker} gives a line is the width that decided the line fits, see
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/742">issue 742</a>.
 * <p>
 * The text renderer of these tests loses a pixel on every text it measures, as rounding can: a text measured whole is
 * wider than its parts added up.
 */
class BreakerTest {
    private final SharedContext sharedContext = mock();
    private final FontContext fontContext = mock();
    private final LayoutContext c = mock();
    private final CalculatedStyle style = mock();
    private final FSFont font = mock();
    @SuppressWarnings("rawtypes")
    private final TextRenderer textRenderer = mock();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(c.getSharedContext()).thenReturn(sharedContext);
        when(c.getFontContext()).thenReturn(fontContext);
        when(c.getTextRenderer()).thenReturn(textRenderer);
        when(sharedContext.getLineBreakingStrategy()).thenReturn(new DefaultLineBreakingStrategy());
        when(textRenderer.getWidth(any(), any(), anyString())).thenAnswer(invocation -> width(invocation.getArgument(2)));
        when(style.getFSFont(c)).thenReturn(font);
        when(style.getWhitespace()).thenReturn(IdentValue.NORMAL);
        when(style.getWordBreak()).thenReturn(IdentValue.NORMAL);
        when(style.getWordWrap()).thenReturn(IdentValue.NORMAL);
    }

    /**
     * A line which fits by the widths of its words keeps that width, not the larger one of the line measured whole.
     */
    @Test
    void aLineWhichFitsKeepsTheWidthItFitsBy() {
        // "A " + "line " + "of " + "a " + "cell." measure 11 + 29 + 17 + 11 + 29 = 97, the line whole 101
        LineBreakContext context = new LineBreakContext("A line of a cell.", null);

        Breaker.breakText(c, context, 97, 97, style);

        assertThat(context.getEnd()).isEqualTo(17);
        assertThat(context.getWidth()).isEqualTo(97);
        assertThat(context.isNeedsNewLine()).isFalse();
    }

    /**
     * A line which wraps keeps the width its words fit by, not the larger one of its text measured whole.
     */
    @Test
    void aLineWhichWrapsKeepsTheWidthItFitsBy() {
        // "A line of a " fits 68 by its words, 71 measured whole; "cell." does not fit after it
        LineBreakContext context = new LineBreakContext("A line of a cell.", null);

        Breaker.breakText(c, context, 68, 68, style);

        assertThat(context.getEnd()).isEqualTo(12);
        assertThat(context.getWidth()).isEqualTo(68);
        assertThat(context.isNeedsNewLine()).isTrue();
    }

    /**
     * A hyphen counts where the line ends at it, and nowhere before.
     */
    @Test
    void aHyphenCountsOnlyWhereTheLineEnds() {
        when(sharedContext.getLineBreakingStrategy()).thenReturn((text, lang, s) -> breakPoints(
                new BreakPoint(3, "-"), new BreakPoint(6, "-"), new BreakPoint(9)));

        LineBreakContext fits = new LineBreakContext("abcdefghi", null);
        Breaker.breakText(c, fits, 100, 100, style);
        // "abc" + "def" + "ghi": no hyphen is printed when the line does not end at one
        assertThat(fits.getWidth()).isEqualTo(17 + 17 + 17);

        LineBreakContext wraps = new LineBreakContext("abcdefghi", null);
        Breaker.breakText(c, wraps, 45, 45, style);
        // "abc" + "def-" fits 40, "ghi" would take it to 51: the line ends at the hyphen after "def"
        assertThat(wraps.getMaster()).isEqualTo("abcdef-ghi");
        assertThat(wraps.getEnd()).isEqualTo(7);
        assertThat(wraps.getWidth()).isEqualTo(17 + 23);
    }

    /**
     * A line which fits whole is as wide as all of its text, also where the break points do not reach its end.
     */
    @Test
    void aLineWhichFitsCountsTheTextAfterItsLastBreakPoint() {
        when(sharedContext.getLineBreakingStrategy()).thenReturn((text, lang, s) -> breakPoints(new BreakPoint(4)));

        LineBreakContext context = new LineBreakContext("abcdefghi", null);
        Breaker.breakText(c, context, 100, 100, style);

        // "abcd" + "efghi", the text after the last break point included
        assertThat(context.getEnd()).isEqualTo(9);
        assertThat(context.getWidth()).isEqualTo(23 + 29);
        assertThat(context.isNeedsNewLine()).isFalse();
    }

    /**
     * A line without any break point which fits is as wide as its text.
     */
    @Test
    void aLineWithoutBreakPointsWhichFitsIsAsWideAsItsText() {
        when(sharedContext.getLineBreakingStrategy()).thenReturn((text, lang, s) -> breakPoints());

        LineBreakContext context = new LineBreakContext("abcdefghi", null);
        Breaker.breakText(c, context, 100, 100, style);

        assertThat(context.getEnd()).isEqualTo(9);
        assertThat(context.getWidth()).isEqualTo(53);
    }

    /**
     * A line which fits whole gets no hyphen, so the hyphen of its last break point does not count.
     */
    @Test
    void aLineWhichFitsDoesNotCountTheHyphenOfItsLastBreakPoint() {
        when(sharedContext.getLineBreakingStrategy()).thenReturn((text, lang, s) -> breakPoints(
                new BreakPoint(3, "-"), new BreakPoint(9, "-")));

        LineBreakContext context = new LineBreakContext("abcdefghi", null);
        Breaker.breakText(c, context, 100, 100, style);

        assertThat(context.getMaster()).isEqualTo("abcdefghi");
        assertThat(context.getWidth()).isEqualTo(17 + 35);
    }

    /**
     * The hyphen of a break point at the end of the text is never printed, so it does not decide whether the line fits.
     */
    @Test
    void aHyphenAtTheEndOfTheTextDoesNotDecideWhetherTheLineFits() {
        when(sharedContext.getLineBreakingStrategy()).thenReturn((text, lang, s) -> breakPoints(
                new BreakPoint(3, "-"), new BreakPoint(9, "-")));

        LineBreakContext context = new LineBreakContext("abcdefghi", null);
        // "abc" + "defghi" is 52, with the hyphen after it 58
        Breaker.breakText(c, context, 52, 52, style);

        assertThat(context.getMaster()).isEqualTo("abcdefghi");
        assertThat(context.getEnd()).isEqualTo(9);
        assertThat(context.getWidth()).isEqualTo(52);
        assertThat(context.isNeedsNewLine()).isFalse();
    }

    /**
     * The text after the last break point decides whether the line fits: where it does not, the line wraps at that break point.
     */
    @Test
    void aLineWrapsWhereTheTextAfterItsLastBreakPointDoesNotFit() {
        when(sharedContext.getLineBreakingStrategy()).thenReturn((text, lang, s) -> breakPoints(new BreakPoint(4)));

        LineBreakContext context = new LineBreakContext("abcdefghi", null);
        // "abcd" is 23 and fits, "efghi" takes it to 52
        Breaker.breakText(c, context, 30, 30, style);

        assertThat(context.getEnd()).isEqualTo(4);
        assertThat(context.getWidth()).isEqualTo(23);
        assertThat(context.isNeedsNewLine()).isTrue();
    }

    /**
     * A text without any break point which does not fit is unbreakable, as a long word is.
     */
    @Test
    void aTextWithoutBreakPointsWhichDoesNotFitIsUnbreakable() {
        when(sharedContext.getLineBreakingStrategy()).thenReturn((text, lang, s) -> breakPoints());

        LineBreakContext context = new LineBreakContext("abcdefghi", null);
        Breaker.breakText(c, context, 30, 30, style);

        assertThat(context.getEnd()).isEqualTo(9);
        assertThat(context.getWidth()).isEqualTo(53);
        assertThat(context.isNeedsNewLine()).isTrue();
        assertThat(context.isUnbreakable()).isTrue();
    }

    /**
     * A first word which does not fit is unbreakable, and the words after it go to the next line.
     */
    @Test
    void aFirstWordWhichDoesNotFitEndsTheLine() {
        LineBreakContext context = new LineBreakContext("abcdefg hi", null);
        Breaker.breakText(c, context, 20, 20, style);

        assertThat(context.getEnd()).isEqualTo(8);
        assertThat(context.getWidth()).isEqualTo(47);
        assertThat(context.isNeedsNewLine()).isTrue();
        assertThat(context.isUnbreakable()).isTrue();
    }

    /**
     * A first syllable which does not fit ends the line with its hyphen.
     */
    @Test
    void aFirstSyllableWhichDoesNotFitEndsWithItsHyphen() {
        when(sharedContext.getLineBreakingStrategy()).thenReturn((text, lang, s) -> breakPoints(
                new BreakPoint(6, "-"), new BreakPoint(9)));

        LineBreakContext context = new LineBreakContext("abcdefghi", null);
        Breaker.breakText(c, context, 20, 20, style);

        assertThat(context.getMaster()).isEqualTo("abcdef-ghi");
        assertThat(context.getEnd()).isEqualTo(7);
        assertThat(context.getWidth()).isEqualTo(41);
        assertThat(context.isUnbreakable()).isTrue();
    }

    /**
     * A line broken between any two characters is as wide as its text rendered whole: its characters measured one by
     * one are what a break may fall between, not what the line is drawn as.
     */
    @Test
    void aLineBrokenAnywhereWhichFitsKeepsTheWidthOfItsText() {
        when(style.getWordBreak()).thenReturn(IdentValue.BREAK_ALL);

        LineBreakContext context = new LineBreakContext("abcdefghij", null);
        Breaker.breakText(c, context, 100, 100, style);

        // Ten characters measure 5 each, 50 added up, and 59 rendered whole
        assertThat(context.getEnd()).isEqualTo(10);
        assertThat(context.getWidth()).isEqualTo(59);
        assertThat(context.isNeedsNewLine()).isFalse();
    }

    /**
     * A line broken between any two characters ends where its text rendered whole still fits.
     */
    @Test
    void aLineBrokenAnywhereEndsWhereItsTextStillFits() {
        when(style.getWordBreak()).thenReturn(IdentValue.BREAK_ALL);

        LineBreakContext context = new LineBreakContext("abcdefghij", null);
        // "abcde" renders 29 and fits; six characters would add up to 30, but render 35
        Breaker.breakText(c, context, 30, 30, style);

        assertThat(context.getEnd()).isEqualTo(5);
        assertThat(context.getWidth()).isEqualTo(29);
        assertThat(context.isNeedsNewLine()).isTrue();
    }

    /**
     * A line broken between any two characters holds one character where not even one fits, and the text after it goes
     * to the next line.
     */
    @Test
    void aLineBrokenAnywhereHoldsOneCharacterWhereNoneFits() {
        when(style.getWordBreak()).thenReturn(IdentValue.BREAK_ALL);

        LineBreakContext context = new LineBreakContext("abc", null);
        Breaker.breakText(c, context, 3, 3, style);

        assertThat(context.getEnd()).isEqualTo(1);
        assertThat(context.getWidth()).isEqualTo(5);
        assertThat(context.isNeedsNewLine()).isTrue();
    }

    /**
     * A word which does not fit a line on its own is broken between its characters with {@code word-wrap: break-word},
     * and that line too is as wide as its text rendered whole.
     */
    @Test
    void aWordBrokenByBreakWordEndsWhereItsTextStillFits() {
        when(style.getWordWrap()).thenReturn(IdentValue.BREAK_WORD);

        LineBreakContext context = new LineBreakContext("abcdefghij", null);
        Breaker.breakText(c, context, 30, 30, style);

        assertThat(context.getEnd()).isEqualTo(5);
        assertThat(context.getWidth()).isEqualTo(29);
        assertThat(context.isNeedsNewLine()).isTrue();
    }

    /**
     * A line broken between any two characters measures a few prefixes of the text, not the whole remaining text and
     * every prefix of the line: a long text without break points, such as base64 in a table cell, would otherwise cost
     * time quadratic in its length.
     */
    @Test
    void aLineBrokenAnywhereMeasuresFewPrefixes() {
        when(style.getWordBreak()).thenReturn(IdentValue.BREAK_ALL);
        String text = "x".repeat(10_000);

        LineBreakContext context = new LineBreakContext(text, null);
        // 50 characters render 299 and fit, 51 render 305
        Breaker.breakText(c, context, 300, 300, style);

        assertThat(context.getEnd()).isEqualTo(50);
        assertThat(context.getWidth()).isEqualTo(299);
        List<String> measured = mockingDetails(textRenderer).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("getWidth"))
                .map(invocation -> (String) invocation.getArgument(2))
                .toList();
        assertThat(measured).hasSizeLessThan(20);
        assertThat(measured.stream().mapToInt(String::length).sum()).isLessThan(1_000);
    }

    /** An empty text broken anywhere fits, with the width of nothing. */
    @Test
    void anEmptyTextBrokenAnywhereFits() {
        when(style.getWordBreak()).thenReturn(IdentValue.BREAK_ALL);

        LineBreakContext context = new LineBreakContext("", null);
        Breaker.breakText(c, context, 30, 30, style);

        assertThat(context.getEnd()).isZero();
        assertThat(context.getWidth()).isZero();
        assertThat(context.isNeedsNewLine()).isFalse();
    }

    /**
     * A {@code <br>} (a newline with no text before it) ends a line which already overflows, as with
     * {@code white-space: nowrap}, instead of moving to the next line and making it empty, see
     * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/491">issue 491</a>.
     */
    @Test
    void aNewlineEndsALineWhichAlreadyOverflows() {
        when(style.getWhitespace()).thenReturn(IdentValue.PRE_LINE);

        LineBreakContext context = new LineBreakContext("\n", null);
        Breaker.breakText(c, context, -50, 100, style);

        assertThat(context.getEnd()).isEqualTo(1);
        assertThat(context.getWidth()).isZero();
        assertThat(context.isNeedsNewLine()).isTrue();
        assertThat(context.isEndsOnNL()).isTrue();
    }

    private static int width(String text) {
        return Math.max(0, 6 * text.codePointCount(0, text.length()) - 1);
    }

    private static BreakPointsProvider breakPoints(BreakPoint... points) {
        Iterator<BreakPoint> iterator = List.of(points).iterator();
        return () -> iterator.hasNext() ? iterator.next() : BreakPoint.DONE;
    }
}
