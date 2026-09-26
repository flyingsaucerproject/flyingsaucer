package org.xhtmlrenderer.pdf;

import org.junit.jupiter.api.Test;
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
 * adjustment its own content received, or it stops short of the text it underlines. It cannot
 * grow past the line's content edge either: the adjustment counted for the line's final
 * character trails the last glyph, which nothing is drawn after, so the decoration of the box
 * holding that character would otherwise overshoot by exactly one adjustment.
 *
 * One fixture underlines the whole of a wrapping paragraph -- so every line of it but the last is
 * justified -- and a single phrase of a second paragraph, which is underlined only as far as its
 * own content runs. The other wraps a number with {@code word-wrap: break-word}, so its lines
 * have no space at all to spread: one character's share of the extra space is the whole of it,
 * and the overshoot is far larger than a pixel of tolerance can hide.
 *
 */
class JustifiedUnderlineTest {
    private static final Logger log = LoggerFactory.getLogger(JustifiedUnderlineTest.class);

    /** The first fixture's {@code width: 200px}, which the PDF device draws at 3/4 of a point per pixel. */
    private static final double CONTENT_WIDTH = 150.0;

    /** The second fixture's {@code width: 100px}, which the PDF device draws at 3/4 of a point per pixel. */
    private static final double NARROW_CONTENT_WIDTH = 75.0;

    private static final byte[] PDF_BYTES = render("justified-underline.html");
    private static final byte[] NO_SPACE_PDF_BYTES = render("justified-underline-no-space.html");

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
