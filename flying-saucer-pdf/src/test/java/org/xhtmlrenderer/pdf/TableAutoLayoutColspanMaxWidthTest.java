package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

/**
 * Regression tests for table-layout:auto + colspan > 1 + max-width.
 *
 * <p>Browsers do NOT enforce {@code max-width} as a hard layout width on
 * {@code colspan > 1} table cells. The allocated width is the authoritative
 * sum of the spanned column widths and must not be overridden by
 * {@code applyCSSMinMaxWidth} after allocation.
 *
 * <p>This gap was NOT covered by PR #702, which correctly fixed
 * {@code table-layout:fixed} cells via the {@code isFixedWidthAdvisoryOnly()}
 * guard but left {@code table-layout:auto} + {@code colspan > 1} cells still
 * subject to incorrect {@code max-width} enforcement.
 *
 * <p>The correct guard is:
 * <pre>
 *   if (isFixedWidthAdvisoryOnly() &amp;&amp; getStyle().getColSpan() &lt;= 1) {
 *       applyCSSMinMaxWidth(c);
 *   }
 * </pre>
 */
class TableAutoLayoutColspanMaxWidthTest {

    private static final Logger log = LoggerFactory.getLogger(TableAutoLayoutColspanMaxWidthTest.class);

    private static final String AUTO_COLSPAN_HTML =
        "org/xhtmlrenderer/pdf/table-auto-layout-colspan-max-width.html";

    @Test
    void renderDoesNotThrow() throws Exception {
        printFile(log, Html2Pdf.fromClasspathResource(AUTO_COLSPAN_HTML),
            "table-auto-layout-colspan-max-width.pdf");
    }

    /**
     * "Alpha" and "Beta" must be on the SAME line.
     *
     * <p>The {@code colspan="2"} cell's allocated width is the sum of both
     * columns (~400pt), which is far wider than the {@code max-width: 80pt}.
     * If {@code applyCSSMinMaxWidth} is incorrectly applied, the cell shrinks
     * to 80pt and "Beta" is forced onto a second line.
     * If max-width is correctly ignored (browser behavior), both words fit
     * comfortably on the first line.
     */
    @Test
    void alphaAndBetaAreOnSameLineWhenMaxWidthNotEnforced() throws Exception {
        PDF pdf = printFile(log, Html2Pdf.fromClasspathResource(AUTO_COLSPAN_HTML),
            "table-auto-layout-colspan-max-width.pdf");

        List<String> lines = pdf.text.lines().toList();

        assertThat(lines)
            .anyMatch(line -> line.contains("Alpha") && line.contains("Beta"));
    }

    /**
     * "Alpha" and "Kappa" must be on DIFFERENT lines.
     *
     * <p>Verifies the cell still wraps at the full allocated column width
     * (~400pt) rather than becoming infinitely wide. Content must wrap,
     * just not prematurely at {@code max-width: 80pt}.
     */
    @Test
    void textStillWrapsAtAllocatedColumnWidthNotAtMaxWidth() throws Exception {
        PDF pdf = printFile(log, Html2Pdf.fromClasspathResource(AUTO_COLSPAN_HTML),
            "table-auto-layout-colspan-max-width.pdf");

        List<String> lines = pdf.text.lines().toList();

        assertThat(lines)
            .noneMatch(line -> line.contains("Alpha") && line.contains("Omega"));
    }
}