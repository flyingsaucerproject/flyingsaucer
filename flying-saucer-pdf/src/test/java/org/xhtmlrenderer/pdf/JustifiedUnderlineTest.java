package org.xhtmlrenderer.pdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.xhtmlrenderer.pdf.TestUtils.pageContent;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

/**
 * A justified line spreads its content with per-character adjustments that are handed to the
 * text renderers rather than baked into the box widths, so a text decoration has to grow by the
 * adjustment its own content received, or it stops short of the text it underlines. It must not
 * grow by the adjustment counted for the line's final character either: that one trails the last
 * glyph, which nothing is drawn after, so the decoration of the box holding that character would
 * otherwise overshoot its text by exactly one adjustment.
 *
 * One fixture underlines the whole of a wrapping paragraph -- so every line of it but the last is
 * justified -- and a single phrase of a second paragraph, which is underlined only as far as its
 * own content runs. The others wrap a number with {@code word-wrap: break-word}, so their lines
 * have no space at all to spread: one character's share of the extra space is the whole of it,
 * and the overshoot is far larger than a pixel of tolerance can hide. One of them holds the number
 * in padded boxes, so a line ends in padding rather than at the content edge; another is too
 * narrow for more than a single digit a line, which leaves no gap to spread anything over. The
 * last two end a line with an inline-block, which must be moved by the gaps between the
 * characters before it, and not by an adjustment after the last of them -- even when that last
 * character is a space.
 *
 */
class JustifiedUnderlineTest {
    private static final Logger log = LoggerFactory.getLogger(JustifiedUnderlineTest.class);

    /** The first fixture's {@code width: 200px}, which the PDF device draws at 3/4 of a point per pixel. */
    private static final double CONTENT_WIDTH = 150.0;

    /** The second fixture's {@code width: 100px}, which the PDF device draws at 3/4 of a point per pixel. */
    private static final double NARROW_CONTENT_WIDTH = 75.0;

    /** The single character fixture's {@code width: 30px}, in points. */
    private static final double NARROW_SINGLE_CHARACTER_WIDTH = 22.5;

    /** The padded fixture's {@code padding-right: 30px}, in points. */
    private static final double PADDING = 22.5;

    private static final byte[] PDF_BYTES = render("justified-underline.html");
    private static final byte[] NO_SPACE_PDF_BYTES = render("justified-underline-no-space.html");
    private static final byte[] PADDING_PDF_BYTES = render("justified-underline-padding.html");
    private static final byte[] SINGLE_CHARACTER_PDF_BYTES = render("justified-underline-single-character.html");

    @Test
    void justifiedLinesAreUnderlinedToTheContentEdge() throws IOException {
        List<Underline> underlines = underlines(pageContent(PDF_BYTES));

        // every line of the first paragraph but its left aligned last one is justified, and the
        // final underline belongs to the second paragraph's partial decoration
        assertThat(underlines.subList(0, underlines.size() - 2))
                .isNotEmpty()
                .allSatisfy(underline -> assertThat(underline.right())
                        .as("justified line %s", underline)
                        .isCloseTo(CONTENT_WIDTH, within(1.0)));
    }

    @Test
    void lastLineOfAParagraphIsLeftAlignedAndStaysShort() throws IOException {
        List<Underline> underlines = underlines(pageContent(PDF_BYTES));

        // the last line of a justified paragraph is left aligned, so its underline must not
        // stretch, and neither may the second paragraph's partial one
        assertThat(underlines.subList(underlines.size() - 2, underlines.size()))
                .allSatisfy(underline -> assertThat(underline.width())
                        .as("left aligned line %s", underline)
                        .isLessThan(CONTENT_WIDTH - 1));
    }

    @Test
    void partiallyUnderlinedLineStopsWhereItsContentEnds() throws IOException {
        String content = pageContent(PDF_BYTES);
        Underline partial = underlines(content).getLast();

        // only "alpha beta gamma" is underlined; the rest of the line resumes in a later text run
        // at exactly the point the underline ends, so the decoration covers no more than its own
        // content -- including the justification space that content contains
        assertThat(partial.right())
                .isCloseTo(lastRunStartOnBaseline(content, partial.top()), within(0.01));
    }

    @Test
    void justifiedLineWithoutSpacesIsUnderlinedToTheContentEdge() throws IOException {
        List<Underline> underlines = underlines(pageContent(NO_SPACE_PDF_BYTES));

        // every line of the wrapped number but the left aligned last one is justified, and with no
        // space on any of them to spread, each character -- the line's last included -- carries the
        // whole of the extra space. The underline of the box holding that last character therefore
        // overshoots by an adjustment several pixels wide, not by a fraction of one, and the
        // tolerance here can be a hundredth of a point where the test above spends a whole pixel.
        assertThat(underlines.subList(0, underlines.size() - 1))
                .isNotEmpty()
                .allSatisfy(underline -> assertThat(underline.right())
                        .as("justified line %s", underline)
                        .isCloseTo(NARROW_CONTENT_WIDTH, within(0.01)));
    }

    @Test
    void justifiedLineEndingInPaddingIsUnderlinedToTheEndOfItsText() throws IOException {
        List<Underline> underlines = underlines(pageContent(PADDING_PDF_BYTES));

        // each justified line holds a single padded box, whose text is spread to the start of its
        // padding. The adjustment counted for the box's last digit is several pixels wide and
        // would carry the underline into that padding, where the line's content edge cannot stop it.
        assertThat(underlines.subList(0, underlines.size() - 1))
                .isNotEmpty()
                .allSatisfy(underline -> assertThat(underline.right())
                        .as("justified line %s", underline)
                        .isCloseTo(NARROW_CONTENT_WIDTH - PADDING, within(0.01)));
    }

    @Test
    void justifiedLineWithASingleCharacterIsNotStretched() throws IOException {
        List<Underline> underlines = underlines(pageContent(SINGLE_CHARACTER_PDF_BYTES));

        // a line of one digit has no gap to spread the extra space over, so it stays as wide as
        // the left aligned last line, and so does the underline of the paragraph
        double lastLineWidth = underlines.getLast().width();
        assertThat(underlines)
                .allSatisfy(underline -> assertThat(underline.width())
                        .as("single character line %s", underline)
                        .isCloseTo(lastLineWidth, within(0.01))
                        .isLessThan(NARROW_SINGLE_CHARACTER_WIDTH - 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"justified-underline-trailing-box.html", "justified-underline-trailing-box-after-space.html"})
    void justifiedLineEndingInABoxEndsAtTheContentEdge(String resource) throws IOException {
        String content = pageContent(render(resource));

        // the first line holds two digits, in one fixture followed by a no-break space, and the
        // inline-block after them. The single gap between the digits takes all the extra space:
        // the space is the line's final character, with no gap after it, just like the second
        // digit where there is none. So the box lands on the content edge rather than short of
        // it or an adjustment past it, and so does the underline beneath it.
        assertThat(boxRight(content)).isCloseTo(NARROW_CONTENT_WIDTH, within(0.01));
        assertThat(underlines(content).getFirst().right()).isCloseTo(NARROW_CONTENT_WIDTH, within(0.01));
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

    /**
     * The filled rectangles in the page content, top down. A decoration is painted as a filled
     * path ({@code m}/{@code l}/{@code h}/{@code f}); the page's clip path ends in {@code W n}
     * and so is not matched.
     */
    private static List<Underline> underlines(String content) {
        Matcher matcher = Pattern.compile(
                "(-?[0-9.]+) (-?[0-9.]+) m\\s+"
                        + "(-?[0-9.]+) (-?[0-9.]+) l\\s+"
                        + "(-?[0-9.]+) (-?[0-9.]+) l\\s+"
                        + "(-?[0-9.]+) (-?[0-9.]+) l\\s+"
                        + "(-?[0-9.]+) (-?[0-9.]+) l\\s+"
                        + "h\\s+f").matcher(content);

        List<Underline> result = matcher.results()
                .map(match -> new Underline(
                        Double.parseDouble(match.group(1)),
                        Double.parseDouble(match.group(2)),
                        Double.parseDouble(match.group(3))))
                .sorted(Comparator.comparingDouble(Underline::top).reversed())
                .toList();

        assertThat(result)
                .as("expected the fixture's underlined lines in:%n%s", content)
                .hasSizeGreaterThan(2);
        return result;
    }

    /** The right edge of the fixture's inline-block, the only path filled in red. */
    private static double boxRight(String content) {
        Matcher box = Pattern.compile("1 0 0 rg\\s+([^h]*)h\\s+f").matcher(content);
        assertThat(box.find()).as("expected a red box in:%n%s", content).isTrue();
        return Pattern.compile("(-?[0-9.]+) -?[0-9.]+ [ml]").matcher(box.group(1)).results()
                .mapToDouble(point -> Double.parseDouble(point.group(1)))
                .max()
                .orElseThrow();
    }

    /** The x the last text run on {@code baseline} starts at. */
    private static double lastRunStartOnBaseline(String content, double baseline) {
        return Pattern.compile("1 0 0 1 (-?[0-9.]+) (-?[0-9.]+) Tm").matcher(content).results()
                .filter(match -> Double.parseDouble(match.group(2)) == baseline)
                .mapToDouble(match -> Double.parseDouble(match.group(1)))
                .max()
                .orElseThrow(() -> new AssertionError("no text run on baseline " + baseline));
    }

    /**
     * A decoration rectangle in PDF user space, where y grows upwards so a larger {@code top}
     * is further up the page.
     */
    private record Underline(double left, double top, double right) {
        double width() {
            return right - left;
        }
    }
}
