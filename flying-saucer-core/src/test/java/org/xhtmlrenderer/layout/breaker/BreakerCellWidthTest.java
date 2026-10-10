package org.xhtmlrenderer.layout.breaker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.LineBreakContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.InlineBox;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A text laid out in the width its {@link InlineBox} asks for fits on one line, see
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/742">issue 742</a>: a table cell is as wide as
 * the widest of its texts, so a text which does not fit that width wraps, and a {@code <br>} after it makes an empty line.
 * <p>
 * The text renderer of these tests rounds as {@code Java2DTextRenderer} does with fractional font metrics: a letter is
 * 5.2 wide and a space 2.3, and a text measures its exact width rounded. A word and its space measured together then round
 * up where each alone rounds down: "ab" is 10, " " is 2, and "ab " is 13 rather than 12.
 */
class BreakerCellWidthTest {
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
        when(c.getNamespaceHandler()).thenReturn(mock(NamespaceHandler.class));
        when(c.getFont(any())).thenReturn(font);
        when(sharedContext.getLineBreakingStrategy()).thenReturn(new DefaultLineBreakingStrategy());
        when(textRenderer.getWidth(any(), any(), anyString())).thenAnswer(invocation -> width(invocation.getArgument(2)));
        when(style.getFSFont(c)).thenReturn(font);
        when(style.getWordBreak()).thenReturn(IdentValue.NORMAL);
        when(style.getWordWrap()).thenReturn(IdentValue.NORMAL);
    }

    @Test
    void aTextFitsTheWidthItsBoxAsksFor() {
        assertFitsTheWidthItsBoxAsksFor("ab ab ab ab ab", IdentValue.NORMAL);
    }

    @Test
    void aTextOfWordsOfDifferentLengthsFitsTheWidthItsBoxAsksFor() {
        assertFitsTheWidthItsBoxAsksFor("a abc ab abcd a", IdentValue.NORMAL);
    }

    /** Measured whole, "ab ab ab ab ab" is 61, its parts added up 62. */
    @Test
    void aTextWithPreWrapFitsTheWidthItsBoxAsksFor() {
        assertFitsTheWidthItsBoxAsksFor("ab ab ab ab ab", IdentValue.PRE_WRAP);
    }

    @Test
    void aTextWithPreLineFitsTheWidthItsBoxAsksFor() {
        assertFitsTheWidthItsBoxAsksFor("ab ab ab ab ab", IdentValue.PRE_LINE);
    }

    /** The line after a new line is the last one, and the box asks for its width too. */
    @Test
    void aLastLineWithPreWrapFitsTheWidthItsBoxAsksFor() {
        assertFitsTheWidthItsBoxAsksFor("ab\nab ab ab ab ab", IdentValue.PRE_WRAP);
    }

    @Test
    void aLastLineWithPreLineFitsTheWidthItsBoxAsksFor() {
        assertFitsTheWidthItsBoxAsksFor("ab\nab ab ab ab ab", IdentValue.PRE_LINE);
    }

    @Test
    void aLineBeforeANewLineWithPreWrapFitsTheWidthItsBoxAsksFor() {
        assertFitsTheWidthItsBoxAsksFor("ab ab ab ab ab\nab", IdentValue.PRE_WRAP);
    }

    /** A text measured whole is checked only for the line it ends: one which does not fit still wraps between its words. */
    @Test
    void aTextWithPreWrapWhichDoesNotFitStillWraps() {
        when(style.getWhitespace()).thenReturn(IdentValue.PRE_WRAP);

        assertThat(lines("ab ab ab ab ab", 40)).containsExactly("ab ab ab ", "ab ab");
    }

    private void assertFitsTheWidthItsBoxAsksFor(String text, IdentValue whitespace) {
        when(style.getWhitespace()).thenReturn(whitespace);
        InlineBox box = new InlineBox(text, null);
        box.setStyle(style);
        box.setStartsHere(true);
        box.setEndsHere(true);
        box.calcMinMaxWidth(c, 1000, false);

        assertThat(lines(text, box.getMaxWidth()))
                .as("%s laid out in the %d its box asks for", text.replace("\n", "\\n"), box.getMaxWidth())
                .containsExactly(text.split("(?<=\n)"));
    }

    /** Lays the text out in lines of the given width, as {@code InlineBoxing} does, and returns their text. */
    private List<String> lines(String text, int avail) {
        LineBreakContext context = new LineBreakContext(text, null);
        List<String> lines = new ArrayList<>();
        do {
            context.reset();
            Breaker.breakText(c, context, avail, avail, style);
            assertThat(context.getWidth()).as("the width of a line").isLessThanOrEqualTo(avail);
            lines.add(text.substring(context.getStart(), context.getEnd()));
            context.setStart(context.getEnd());
        } while (!context.isFinished());
        return lines;
    }

    private static int width(String text) {
        double exact = 0;
        for (char ch : text.toCharArray()) {
            exact += ch == ' ' ? 2.3 : 5.2;
        }
        return (int) Math.round(exact);
    }
}
