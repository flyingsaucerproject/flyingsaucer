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
import static org.xhtmlrenderer.pdf.TestUtils.pageContent;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

public class CmykColorsTest {
    private static final Logger log = LoggerFactory.getLogger(CmykColorsTest.class);

    @Test
    public void rendersCmykColors() throws IOException {
        byte[] bytes = generatePdf("cmyk-colors.html");
        PDF pdf = printFile(log, bytes, "cmyk-colors.pdf");
        assertThat(pdf).containsText(
            "Cyan text",
            "White text on magenta background",
            "Yellow border",
            "Orange text on light blue background"
        );

        String content = pageContent(bytes);
        assertThat(content)
            .as("cyan text").contains("1 0 0 0 k")
            .as("white text").contains("0 0 0 0 k")
            .as("magenta background").contains("0 1 0 0 k")
            .as("black text and borders").contains("0 0 0 1 k")
            .as("yellow border").contains("0 0 1 0 k")
            .as("orange text from percentages").contains("0 0.5 1 0 k")
            .as("light blue background from percentages").contains("0.1 0 0 0.05 k")
            .as("no RGB colors").doesNotContainPattern("\\brg\\b");
    }

    private byte[] generatePdf(String htmlPath) {
        URL htmlUrl = requireNonNull(currentThread().getContextClassLoader().getResource(htmlPath),
                () -> "Test resource not found: " + htmlPath);
        return Html2Pdf.fromUrl(htmlUrl);
    }
}
