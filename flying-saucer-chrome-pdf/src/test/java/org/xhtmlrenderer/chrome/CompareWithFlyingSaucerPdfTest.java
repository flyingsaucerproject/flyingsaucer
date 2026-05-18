package org.xhtmlrenderer.chrome;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static java.util.Objects.requireNonNull;
import static org.xhtmlrenderer.chrome.TestUtils.printFile;

class CompareWithFlyingSaucerPdfTest {
    private static final Logger log = LoggerFactory.getLogger(CompareWithFlyingSaucerPdfTest.class);

    @Test
    void compareHtml5Tags() throws IOException {
        URL html = requireNonNull(
            getClass().getClassLoader().getResource("org/xhtmlrenderer/chrome/html5-sample.html"));

        byte[] pdf = org.xhtmlrenderer.pdf.Html2Pdf.fromUrl(html);
        byte[] chrome = Html2Pdf.fromUrl(html);

        printFile(log, pdf, "sample-pdf.pdf");
        printFile(log, chrome, "sample-chrome.pdf");
    }
}
