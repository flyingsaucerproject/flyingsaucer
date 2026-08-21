package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.within;
import static org.xhtmlrenderer.pdf.TestUtils.pageContent;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

/**
 * The {@code letter-spacing} property should both widen the layout (line breaking)
 * and space out the glyphs actually painted to the page.
 *
 * See <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/356">issue #356</a>.
 */
class LetterSpacingTest {
    private static final Logger log = LoggerFactory.getLogger(LetterSpacingTest.class);

    private static final byte[] SPACED = render("letter-spacing.html");
    private static final byte[] WRAPPED = render("letter-spacing-wrap.html");

    @Test
    void positiveLetterSpacingSpacesOutGlyphs() throws IOException {
        // 0.25em of extra spacing is 250/1000 of the em square, drawn as a
        // TJ kerning array: [(W) -250 (i) -250 (d) -250 (e)] TJ
        assertThat(kernBetween(pageContent(SPACED), 'W', 'i')).isCloseTo(-250.0f, within(0.5f));
    }

    @Test
    void negativeLetterSpacingTightensGlyphs() throws IOException {
        // -1px at font-size 12px is -83.3/1000 of the em square; positive
        // TJ adjustments move the following glyph closer
        assertThat(kernBetween(pageContent(SPACED), 'T', 'i')).isCloseTo(250.0f / 3, within(0.5f));
    }

    @Test
    void normalLetterSpacingDrawsPlainStrings() throws IOException {
        assertThat(new PDF(SPACED)).containsText("Hello");
        assertThat(pageContent(SPACED)).contains("(Hello)Tj");
    }

    @Test
    void letterSpacingIsIncludedInLineBreaking() throws IOException {
        String content = pageContent(WRAPPED);

        // the plain paragraph keeps "aaa aaa" on one line, drawn as a single string
        assertThat(content).contains("(aaa aaa)Tj");

        // the letter-spaced paragraph no longer fits 100px and wraps into two
        // kerned runs ("aaa" / "aaa"), so the page has three distinct baselines
        assertThat(content.split("]TJ", -1)).hasSize(3);
        assertThat(baselines(content)).hasSize(3);
    }

    private static byte[] render(String resource) {
        try {
            byte[] bytes = Html2Pdf.fromClasspathResource(resource);
            printFile(log, bytes, resource.replace(".html", ".pdf"));
            return bytes;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to render " + resource, e);
        }
    }

    private static float kernBetween(String content, char first, char second) {
        Matcher matcher = Pattern.compile("\\(%s\\)\\s*(-?[0-9.]+)\\s*\\(%s\\)".formatted(first, second)).matcher(content);
        assertThat(matcher.find())
                .as("expected a TJ kerning adjustment between (%s) and (%s) in:%n%s", first, second, content)
                .isTrue();
        return Float.parseFloat(matcher.group(1));
    }

    private static Set<String> baselines(String content) {
        return Pattern.compile("1 0 0 1 [0-9.]+ ([0-9.]+) Tm").matcher(content).results()
                .map(match -> match.group(1))
                .collect(toSet());
    }
}
