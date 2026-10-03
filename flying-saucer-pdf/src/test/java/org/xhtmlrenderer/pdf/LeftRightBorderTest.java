package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

public class LeftRightBorderTest {
    private static final Logger log = LoggerFactory.getLogger(LeftRightBorderTest.class);

    @Test
    public void deepHierarchyInCss() throws IOException {
        byte[] bytes = generatePdf("left-right-border.html");
        PDF pdf = printFile(log, bytes, "left-right-border.pdf");
        assertThat(pdf).containsText(
            "Border top left radius",
            "Border top right radius",
            "Border bottom left radius",
            "Border bottom right radius"
        );
    }

    private byte[] generatePdf(String htmlPath) {
        URL htmlUrl = requireNonNull(currentThread().getContextClassLoader().getResource(htmlPath),
                () -> "Test resource not found: " + htmlPath);
        return Html2Pdf.fromUrl(htmlUrl);
    }
}
