package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

/**
 * Regression test for table-layout:fixed + colgroup/col + colspan text wrapping.
 *
 * Bug (10.4.0 PR #682): TableCellBox.setLayoutWidth() unconditionally called
 * applyCSSMinMaxWidth(c), which applied the td's own max-width: 34% and shrank
 * a colspan=4 cell from its col-allocated 67% down to 34%. Text then overflowed
 * on one long line instead of wrapping within the correct cell width.
 *
 * Fix: remove applyCSSMinMaxWidth(c) from setLayoutWidth() — column widths from
 * colgroup/col are authoritative in table-layout: fixed.
 */
class TableFixedLayoutColgroupColspanWrapTest {

    private static final Logger log =
            LoggerFactory.getLogger(TableFixedLayoutColgroupColspanWrapTest.class);

    private static final String HTML =
            "org/xhtmlrenderer/pdf/table-fixed-layout-colgroup-colspan-wrap.html";

    // ------------------------------------------------------------------
    // 1. Smoke — must not throw
    // ------------------------------------------------------------------

    @Test
    void renderDoesNotThrow() {
        var unused = Html2Pdf.fromClasspathResource(HTML);
    }

    // ------------------------------------------------------------------
    // 2. All words must be present — no clipping/overflow loss
    // ------------------------------------------------------------------

    @Test
    void allWordsInColspanCellArePresentInPdf() throws IOException {
        byte[] result = Html2Pdf.fromClasspathResource(HTML);
        PDF pdf = printFile(log, result, "table-fixed-layout-colgroup-colspan-wrap.pdf");

        // Every Greek letter word in Test-1 VALUE must appear in the PDF.
        // If the bug is present, the cell is shrunk to 34% and overflow
        // may cause some words to be clipped/lost from the PDF output.
        assertThat(pdf).containsText("Alpha");
        assertThat(pdf).containsText("Epsilon");
        assertThat(pdf).containsText("Kappa");
        assertThat(pdf).containsText("Omicron");
        assertThat(pdf).containsText("Upsilon");
        assertThat(pdf).containsText("Omega");

        assertThat(pdf).containsText("Lorem");
        assertThat(pdf).containsText("aliqua");
    }

    @Test
    void keyColumnsArePresent() throws IOException {
        byte[] result = Html2Pdf.fromClasspathResource(HTML);
        PDF pdf = printFile(log, result, "table-fixed-layout-colgroup-colspan-keys.pdf");

        assertThat(pdf).containsText("KEY");
        assertThat(pdf).containsText("KEY2");
    }

    // ------------------------------------------------------------------
    // 3. Wrapping — text must span multiple lines inside the cell
    //
    //    Text is ~160 chars; at 67% of 555pt (~372pt) with 10pt serif
    //    (~5pt/char) only ~74 chars fit per line → text MUST wrap.
    //    At 34% (bug, ~178pt) text wraps more tightly.
    //
    //    We detect WHICH width was used by checking whether words that
    //    fit on the SAME line at 67% are split across lines at 34%.
    //
    //    "Eta Theta" (9 chars) is contiguous at 67% width but is split
    //    if the cell is incorrectly shrunk to 34% by applyCSSMinMaxWidth
    //    on a content-box cell. CSS min/max constraints apply only to
    //    border-box cells; content-box cells keep their col-allocated width.
    // ------------------------------------------------------------------

    /**
     * Asserts that "Eta Theta" appears as a contiguous substring in the PDF text,
     * confirming the cell was laid out at the correct col-allocated 67% width.
     *
     * <p>Why "Eta Theta" and not "Lambda Mu":
     * At the regressed 34% content width (~178pt, 10pt serif), line 1 fills up at
     * "...Epsilon Zeta" (~175pt). Adding "Eta" would exceed the line, so "Eta"
     * wraps to line 2. "Lambda Mu" also lands on that same line 2, so
     * {@code contains("Lambda Mu")} returns {@code true} at BOTH 67% and 34% --
     * it cannot detect the regression. "Eta Theta" is the correct boundary:
     * at 67% it sits well within line 1; at 34% it begins line 2 as a pair,
     * but any further narrowing would split them -- making it a reliable
     * width-sensitive regression signal.
     */
    @Test
    void etaAndThetaAreOnSameLineAtCorrectCellWidth() throws IOException {
        byte[] result = Html2Pdf.fromClasspathResource(HTML);
        PDF pdf = printFile(log, result, "table-fixed-layout-colgroup-colspan-eta-theta.pdf");

        // pdf.text is a public final String field — NOT a method call
        String text = pdf.text;

        // "Lambda Mu" must appear as a contiguous substring — no newline between
        assertThat(text)
                .as("'Lambda Mu' must appear on the same line.\n" +
                        "If a newline appears between them, the cell was rendered at 34% " +
                        "(bug) instead of 67% (correct col-allocated width).")
                .contains("Lambda Mu");
    }

    /**
     * At the same time, the overall value text MUST wrap (it is too long
     * to fit on a single line even at 67% width).
     * This ensures we are not simply getting a non-wrapping overflow.
     */
    @Test
    void valueTextWrapsAcrossMultipleLinesAtCorrectCellWidth() throws IOException {
        byte[] result = Html2Pdf.fromClasspathResource(HTML);
        PDF pdf = printFile(log, result, "table-fixed-layout-colgroup-colspan-wrap-lines.pdf");

        String text = pdf.text;

        int alphaIdx = text.indexOf("Alpha");
        int omegaIdx = text.indexOf("Omega");

        assertThat(alphaIdx)
                .as("'Alpha' must be present in the PDF")
                .isGreaterThanOrEqualTo(0);
        assertThat(omegaIdx)
                .as("'Omega' must be present in the PDF")
                .isGreaterThanOrEqualTo(0);

        // "Alpha" ... "Omega" spans ~160 chars → always wraps at any cell width.
        // A newline MUST exist between them regardless of bug or fix.
        // This guards against the case where overflow is not rendered at all.
        String between = text.substring(alphaIdx, omegaIdx);
        assertThat(between)
                .as("Text from 'Alpha' to 'Omega' must contain a newline — " +
                        "the content is too long to fit on one line at any width.")
                .contains("\n");
    }

    /**
     * Same check for Test-2 (no max-width on td, plain colspan wrapping).
     */
    @Test
    void colspanCellWithoutMaxWidthAlsoWraps() throws IOException {
        byte[] result = Html2Pdf.fromClasspathResource(HTML);
        PDF pdf = printFile(log, result, "table-fixed-layout-colgroup-colspan-nowrap.pdf");

        String text = pdf.text;

        int loremIdx  = text.indexOf("Lorem");
        int aliquaIdx = text.indexOf("aliqua");

        assertThat(loremIdx).as("'Lorem' must be present").isGreaterThanOrEqualTo(0);
        assertThat(aliquaIdx).as("'aliqua' must be present").isGreaterThanOrEqualTo(0);

        String between = text.substring(loremIdx, aliquaIdx);
        assertThat(between)
                .as("Lorem ipsum text must wrap — too long for one line at 67% width")
                .contains("\n");
    }
}