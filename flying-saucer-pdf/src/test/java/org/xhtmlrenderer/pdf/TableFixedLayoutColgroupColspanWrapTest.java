package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

/**
 * Regression tests for table-layout:fixed + colgroup/col + colspan text wrapping.
 *
 * <p>Original bug (PR #682): {@code TableCellBox.setLayoutWidth()} unconditionally
 * called {@code applyCSSMinMaxWidth(c)}, which honoured the {@code <td>}'s own
 * {@code max-width: 34%} and shrank a {@code colspan=4} cell from its col-allocated
 * 67&nbsp;% down to 34&nbsp;%.  Text then overflowed instead of wrapping within the
 * correct cell width.
 *
 * <p>The guard introduced by the follow-up PR used {@code isBorderBox()}, which
 * causes two distinct regressions that this test class covers:
 * <ol>
 *   <li><em>Content-box regression</em> — in {@code table-layout: auto} tables,
 *       {@code max-width} is silently ignored for content-box cells because
 *       {@code isBorderBox()} returns {@code false} and the call is skipped.</li>
 *   <li><em>Border-box regression</em> — in {@code table-layout: fixed} tables,
 *       {@code max-width} still overrides the col-allocated width for border-box
 *       cells because {@code isBorderBox()} returns {@code true} and the call is
 *       still made.</li>
 * </ol>
 *
 * <p>The correct guard is {@code isFixedWidthAdvisoryOnly()} (true only for
 * {@code table-layout: auto}), which fixes both cases regardless of box-sizing.
 */
class TableFixedLayoutColgroupColspanWrapTest {

    private static final Logger log =
        LoggerFactory.getLogger(TableFixedLayoutColgroupColspanWrapTest.class);

    /** Three-test HTML: content-box (Test-1), no-max-width (Test-2), border-box (Test-3). */
    private static final String FIXED_HTML =
        "org/xhtmlrenderer/pdf/table-fixed-layout-colgroup-colspan-wrap.html";

    /** Single-cell auto-layout table with max-width on a content-box td. */
    private static final String AUTO_HTML =
        "org/xhtmlrenderer/pdf/table-auto-layout-content-box-max-width.html";

    // ------------------------------------------------------------------
    // 1. Smoke — must not throw
    // ------------------------------------------------------------------

    @Test
    void renderDoesNotThrow() {
        var unused = Html2Pdf.fromClasspathResource(FIXED_HTML);
    }

    // ------------------------------------------------------------------
    // 2. All words present — no clipping / overflow loss
    // ------------------------------------------------------------------

    @Test
    void allWordsInColspanCellArePresentInPdf() throws IOException {
        PDF pdf = printFile(log, Html2Pdf.fromClasspathResource(FIXED_HTML),
            "table-fixed-layout-colgroup-colspan-wrap.pdf");

        // Every Greek-letter name in the Test-1 VALUE cell must survive in the PDF.
        // If the bug is active the cell is shrunk to 34 % and overflow may clip words.
        assertThat(pdf).containsExactText("Alpha");
        assertThat(pdf).containsExactText("Epsilon");
        assertThat(pdf).containsExactText("Kappa");
        assertThat(pdf).containsExactText("Omicron");
        assertThat(pdf).containsExactText("Upsilon");
        assertThat(pdf).containsExactText("Omega");

        assertThat(pdf).containsExactText("Lorem");
        assertThat(pdf).containsExactText("aliqua");
    }

    @Test
    void keyColumnsArePresent() throws IOException {
        PDF pdf = printFile(log, Html2Pdf.fromClasspathResource(FIXED_HTML),
            "table-fixed-layout-colgroup-colspan-keys.pdf");

        assertThat(pdf).containsExactText("KEY");
        assertThat(pdf).containsExactText("KEY2");
        assertThat(pdf).containsExactText("KEY3");
    }

    // ------------------------------------------------------------------
    // 3. Width detection — col-allocated 67 % vs buggy max-width 34 %
    // ------------------------------------------------------------------

    /**
     * Confirms that the Test-1 colspan cell uses the col-allocated 67&nbsp;%
     * width, not the {@code max-width: 34%} declared on the content-box
     * {@code <td>}.
     *
     * <p>The assertion is structural: "Eta" and "Theta" are consecutive words
     * in the source text.  At the correct 67&nbsp;% cell width the first text line
     * is wide enough to accommodate both words together.  If {@code max-width: 34%}
     * were incorrectly applied the narrower cell would wrap earlier, placing "Eta"
     * at the start of a new line and separating the two words.
     *
     * <p>No character-width arithmetic is involved.  The check iterates the
     * extracted PDF lines directly, so it remains stable across font-metric and
     * OpenPDF version changes.
     */
    @Test
    void etaAndThetaAreOnSameLineAtCorrectCellWidth() throws IOException {
        PDF pdf = printFile(log, Html2Pdf.fromClasspathResource(FIXED_HTML),
            "table-fixed-layout-colgroup-colspan-eta-theta.pdf");

        assertThat(pdf.text.lines().toList())
            .as("'Eta' and 'Theta' must appear on the same extracted-text line "
                + "(Test-1, content-box cell). "
                + "If they are split across lines the cell was rendered at the "
                + "incorrect max-width (34 %) instead of the col-allocated width "
                + "(67 %).")
            .anyMatch(line -> line.contains("Eta") && line.contains("Theta"));
    }

    /**
     * Guards against silent overflow: the Greek-alphabet text is long enough that
     * it must wrap at any realistic cell width, so it must occupy more than one
     * line.  This ensures the previous test is not vacuously satisfied by a
     * non-wrapping overflow.
     */
    @Test
    void valueTextWrapsAcrossMultipleLinesAtCorrectCellWidth() throws IOException {
        PDF pdf = printFile(log, Html2Pdf.fromClasspathResource(FIXED_HTML),
            "table-fixed-layout-colgroup-colspan-wrap-lines.pdf");

        // 24 Greek-letter words cannot fit on a single line at any realistic width.
        // Assert structurally that "Alpha" (first) and "Omega" (last) land on
        // different extracted-text lines.
        assertThat(pdf).containsExactText("Alpha");
        assertThat(pdf).containsExactText("Omega");
        assertThat(pdf.text.lines().toList())
            .as("'Alpha' and 'Omega' must be on different lines — the Greek-alphabet "
                + "content is too long to fit on a single line at any cell width.")
            .noneMatch(line -> line.contains("Alpha") && line.contains("Omega"));
    }

    /**
     * Same wrapping check for Test-2 (no {@code max-width} on td, plain colspan).
     */
    @Test
    void colspanCellWithoutMaxWidthAlsoWraps() throws IOException {
        PDF pdf = printFile(log, Html2Pdf.fromClasspathResource(FIXED_HTML),
            "table-fixed-layout-colgroup-colspan-nowrap.pdf");

        assertThat(pdf).containsExactText("Lorem");
        assertThat(pdf).containsExactText("aliqua");
        assertThat(pdf.text.lines().toList())
            .as("'Lorem' and 'aliqua' must be on different lines — the Lorem-ipsum "
                + "content is too long to fit on one line at 67 % width.")
            .noneMatch(line -> line.contains("Lorem") && line.contains("aliqua"));
    }

    // ------------------------------------------------------------------
    // 4. Regression: border-box cell in table-layout:fixed
    //
    //    The isBorderBox() guard in the PR would call applyCSSMinMaxWidth for
    //    any border-box cell, including those in table-layout:fixed tables.
    //    The col-allocated width must still win for border-box cells in fixed
    //    layout — exactly the same requirement as for content-box cells.
    // ------------------------------------------------------------------

    /**
     * A border-box colspan {@code <td>} in a {@code table-layout: fixed} table
     * must use the col-allocated width (67&nbsp;%), not the {@code max-width: 34%}
     * declared on the element.
     *
     * <p>Test-3 in the HTML fixture has {@code box-sizing: border-box} on the
     * same {@code colspan=4} cell.  Under the buggy {@code isBorderBox()} guard
     * {@code applyCSSMinMaxWidth} is called and shrinks the cell to the incorrect
     * 34&nbsp;%.  Under the correct {@code isFixedWidthAdvisoryOnly()} guard the
     * call is skipped for {@code table-layout: fixed} regardless of box-sizing,
     * so "Eta" and "Theta" appear on the same line.
     */
    @Test
    void borderBoxFixedLayoutColWidthTakesPrecedenceOverMaxWidth() throws IOException {
        PDF pdf = printFile(log, Html2Pdf.fromClasspathResource(FIXED_HTML),
            "table-fixed-layout-colgroup-colspan-borderbox.pdf");

        assertThat(pdf).containsExactText("KEY3");
        assertThat(pdf.text.lines().toList())
            .as("'Eta' and 'Theta' must appear on the same extracted-text line "
                + "(Test-3, border-box cell). "
                + "If they are split, applyCSSMinMaxWidth was invoked for a "
                + "border-box cell in table-layout:fixed (isBorderBox() guard "
                + "regression), shrinking it to max-width:34% instead of "
                + "honouring the col-allocated 67%.")
            .anyMatch(line -> line.contains("Eta") && line.contains("Theta"));
    }

    // ------------------------------------------------------------------
    // 5. Regression: content-box cell in table-layout:auto
    //
    //    The isBorderBox() guard skips applyCSSMinMaxWidth for content-box cells
    //    everywhere, including table-layout:auto tables where max-width must be
    //    honoured.  This worked correctly before PR #682.
    // ------------------------------------------------------------------

    /**
     * A content-box {@code <td>} in a {@code table-layout: auto} table must have
     * its {@code max-width} honoured by {@code applyCSSMinMaxWidth}.
     *
     * <p>Under the buggy {@code isBorderBox()} guard the call is skipped for
     * content-box cells (the default), so {@code max-width: 80pt} is silently
     * ignored and the cell expands to its natural content width, fitting all 24
     * words on a single wide line.  Under the correct
     * {@code isFixedWidthAdvisoryOnly()} guard the call IS made (auto-layout →
     * widths are advisory), constraining the cell to 80&nbsp;pt and forcing the
     * words to wrap across many lines — so "Alpha" and "Omega" land on different
     * lines.
     */
    @Test
    void contentBoxAutoLayoutMaxWidthIsRespected() throws IOException {
        PDF pdf = printFile(log, Html2Pdf.fromClasspathResource(AUTO_HTML),
            "table-auto-layout-content-box-max-width.pdf");

        // At max-width: 80pt the 24 Greek words cannot fit on one line.
        // "Alpha" (first word) and "Omega" (last word) must be on different lines.
        assertThat(pdf).containsExactText("Alpha");
        assertThat(pdf).containsExactText("Omega");
        assertThat(pdf.text.lines().toList())
            .as("'Alpha' and 'Omega' must be on different lines. "
                + "If they are on the same line, max-width was silently ignored "
                + "for a content-box cell in table-layout:auto (isBorderBox() "
                + "guard regression — the call to applyCSSMinMaxWidth was skipped).")
            .noneMatch(line -> line.contains("Alpha") && line.contains("Omega"));
    }
}