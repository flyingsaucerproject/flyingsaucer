package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

public class HeaderTest {
    private static final Logger log = LoggerFactory.getLogger(HeaderTest.class);

    @Test
    void pageWithHeader() throws IOException {
        String fileName = "page-with-header.html";
        byte[] bytes = Html2Pdf.fromClasspathResource(fileName);

        PDF pdf = printFile(log, bytes, "page-with-header.pdf");
        assertThat(pdf).containsText("Header", "Body", "Footer");
    }
}
