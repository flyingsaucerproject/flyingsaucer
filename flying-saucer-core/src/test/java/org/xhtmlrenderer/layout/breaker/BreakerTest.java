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

    private static int width(String text) {
        return Math.max(0, 6 * text.codePointCount(0, text.length()) - 1);
    }

    private static BreakPointsProvider breakPoints(BreakPoint... points) {
        Iterator<BreakPoint> iterator = List.of(points).iterator();
        return () -> iterator.hasNext() ? iterator.next() : BreakPoint.DONE;
    }
}
