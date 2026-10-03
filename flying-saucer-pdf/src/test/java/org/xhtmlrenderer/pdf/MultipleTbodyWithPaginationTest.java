package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

/**
 * See <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/379">issue 379</a>
 */
class MultipleTbodyWithPaginationTest {
    private static final Logger log = LoggerFactory.getLogger(MultipleTbodyWithPaginationTest.class);

    @Test
    void rowspanInMultipleUnbreakableTbodies() throws IOException {
        byte[] bytes = Html2Pdf.fromClasspathResource("multiple-tbody-with-pagination.html");
        PDF pdf = printFile(log, bytes, "multiple-tbody-with-pagination.pdf");

        assertThat(pdf.numberOfPages).isEqualTo(2);
        assertThat(pdf.text.lines()).containsExactly(
                "Name Age", "Left 1", "Right 1.a", "Right 1.b",
                "Name Age", "Left 2", "Right 2.a", "Right 2.b");
    }

    /**
     * Without {@code page-break-inside: avoid}, the second rowspan cell ("Left 2") is split across pages.
     */
    @Test
    void rowspanCellSplitAcrossPages() throws IOException {
        byte[] bytes = Html2Pdf.fromClasspathResource("multiple-tbody-with-rowspan-split.html");
        PDF pdf = printFile(log, bytes, "multiple-tbody-with-rowspan-split.pdf");

        assertThat(pdf.numberOfPages).isEqualTo(2);
        assertThat(pdf).containsText("Left 1", "Right 1.a", "Right 1.b", "Left 2", "Right 2.a", "Right 2.b");
    }
}
