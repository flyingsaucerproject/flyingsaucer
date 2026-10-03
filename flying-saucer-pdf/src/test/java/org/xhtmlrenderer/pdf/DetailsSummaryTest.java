package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

/**
 * See <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/500">issue 500</a>
 */
class DetailsSummaryTest {
    private static final Logger log = LoggerFactory.getLogger(DetailsSummaryTest.class);

    @Test
    void closedDetailsShowsOnlySummary() throws IOException {
        byte[] bytes = Html2Pdf.fromClasspathResource("details-summary.html");
        PDF pdf = printFile(log, bytes, "details-summary.pdf");

        assertThat(pdf).containsText("Closed summary", "Open summary", "Open content");
        assertThat(pdf).doesNotContainText("Closed content");
    }
}
