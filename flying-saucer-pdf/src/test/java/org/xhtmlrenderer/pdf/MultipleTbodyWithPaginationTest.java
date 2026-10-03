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
        assertThat(pdf).containsExactText("Left 1\nRight 1.a\nRight 1.b");
        assertThat(pdf).containsExactText("Left 2\nRight 2.a\nRight 2.b");
    }
}
