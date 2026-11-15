package org.xhtmlrenderer.pdf;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.xhtmlrenderer.pdf.TestUtils.pageContent;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

/**
 * A justified line spreads its content with adjustments handed to the text renderers rather than
 * baked into the box widths, so the background of an inline box has to grow by the adjustment its
 * own content received, or it stops short of the text it highlights (issue #778).
 * <p>
 * The fixture highlights the whole of a wrapping paragraph -- so every line of it but the last is
 * justified -- and a single phrase of a second paragraph, which is highlighted only as far as its
 * own content runs.
 */
class JustifiedBackgroundTest {
    private static final Logger log = LoggerFactory.getLogger(JustifiedBackgroundTest.class);

    /** The fixture's {@code width: 200px}, which the PDF device draws at 3/4 of a point per pixel. */
    private static final double CONTENT_WIDTH = 150.0;

    private static final String CONTENT = render();

    @Test
    void justifiedLinesAreHighlightedToTheContentEdge() {
        List<Highlight> highlights = highlights();

        // every line of the first paragraph but its left aligned last one is justified, and the
        // final highlight belongs to the second paragraph's partial one
        assertThat(highlights.subList(0, highlights.size() - 2))
                .isNotEmpty()
                .allSatisfy(highlight -> assertThat(highlight.right())
                        .as("justified line %s", highlight)
                        .isCloseTo(CONTENT_WIDTH, within(1.0)));
    }

    @Test
    void lastLineOfAParagraphIsLeftAlignedAndStaysShort() {
        List<Highlight> highlights = highlights();

        assertThat(highlights.subList(highlights.size() - 2, highlights.size()))
                .allSatisfy(highlight -> assertThat(highlight.width())
                        .as("left aligned line %s", highlight)
                        .isLessThan(CONTENT_WIDTH - 1));
    }

    @Test
    void partiallyHighlightedLineStopsWhereItsContentEnds() {
        Highlight partial = highlights().getLast();

        // only "alpha beta gamma" is highlighted; the rest of the line resumes in a later text run
        // at exactly the point the highlight ends
        assertThat(partial.right()).isCloseTo(lastRunStartBelow(partial.top()), within(0.01));
    }

    private static String render() {
        try {
            byte[] bytes = Html2Pdf.fromClasspathResource("justified-background.html");
            printFile(log, bytes, "justified-background.pdf");
            return pageContent(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to render justified-background.html", e);
        }
    }

    /** The rectangles filled in yellow, top down. */
    private static List<Highlight> highlights() {
        List<Highlight> result = Pattern.compile(
                        "1 1 0 rg\\s+"
                                + "(-?[0-9.]+) (-?[0-9.]+) m\\s+"
                                + "-?[0-9.]+ -?[0-9.]+ l\\s+"
                                + "(-?[0-9.]+) -?[0-9.]+ l\\s+"
                                + "-?[0-9.]+ -?[0-9.]+ l\\s+"
                                + "h\\s+f").matcher(CONTENT).results()
                .map(match -> new Highlight(
                        Double.parseDouble(match.group(1)),
                        Double.parseDouble(match.group(2)),
                        Double.parseDouble(match.group(3))))
                .sorted(Comparator.comparingDouble(Highlight::top).reversed())
                .toList();

        assertThat(result)
                .as("expected the fixture's highlighted lines in:%n%s", CONTENT)
                .hasSizeGreaterThan(2);
        return result;
    }

    /** The x the last text run on the line whose highlight starts at {@code top} starts at. */
    private static double lastRunStartBelow(double top) {
        return Pattern.compile("1 0 0 1 (-?[0-9.]+) (-?[0-9.]+) Tm").matcher(CONTENT).results()
                .filter(match -> {
                    double baseline = Double.parseDouble(match.group(2));
                    return baseline < top && baseline > top - 12;
                })
                .mapToDouble(match -> Double.parseDouble(match.group(1)))
                .max()
                .orElseThrow(() -> new AssertionError("no text run below " + top));
    }

    /**
     * A background rectangle in PDF user space, where y grows upwards so a larger {@code top}
     * is further up the page.
     */
    private record Highlight(double left, double top, double right) {
        double width() {
            return right - left;
        }
    }
}
