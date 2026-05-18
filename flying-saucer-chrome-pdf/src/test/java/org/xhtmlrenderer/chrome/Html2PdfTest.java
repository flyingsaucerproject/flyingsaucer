package org.xhtmlrenderer.chrome;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static org.xhtmlrenderer.chrome.TestUtils.printFile;

class Html2PdfTest {
    private static final Logger log = LoggerFactory.getLogger(Html2PdfTest.class);

    @Test
    void rendersClasspathHtmlToPdf() throws IOException {
        byte[] bytes = Html2Pdf.fromClasspathResource("org/xhtmlrenderer/chrome/sample.html");
        PDF pdf = printFile(log, bytes, "sample-chrome.pdf");

        assertThat(pdf).containsText("Hello Chromium");
        assertThat(pdf).containsText("chrome-headless-shell");
    }

    @Test
    void rendersInlineHtmlToPdf() throws IOException {
        byte[] bytes = Html2Pdf.fromHtml("<!DOCTYPE html><html><body><h1>Inline content</h1></body></html>");
        PDF pdf = printFile(log, bytes, "inline-chrome.pdf");

        assertThat(pdf).containsText("Inline content");
    }
}
