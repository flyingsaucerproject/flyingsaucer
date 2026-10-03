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

public class ListStylesTest {
    private static final Logger log = LoggerFactory.getLogger(ListStylesTest.class);

    @Test
    public void upperRoman() throws IOException {
        byte[] bytes = generatePdf("list-styles.upper-roman.html");
        PDF pdf = printFile(log, bytes, "list-styles.upper-roman.pdf");
        assertThat(pdf).containsText(
            "I.", "II.", "III.", "IV.", "V.", "VI.", "VII.", "VIII.", "IX.", "X.",
            "i1", "i2", "i3", "i4", "i5", "i6", "i7", "i8", "i9", "i10"
        );
    }

    @Test
    public void upperLatin() throws IOException {
        byte[] bytes = generatePdf("list-styles.upper-latin.html");
        PDF pdf = printFile(log, bytes, "list-styles.upper-latin.pdf");
        assertThat(pdf).containsText(
            "A.", "B.", "C.", "D.", "E.", "F.", "G.", "H.", "I.", "J.",
            "i1", "i2", "i3", "i4", "i5", "i6", "i7", "i8", "i9", "i10"
        );
    }

    private byte[] generatePdf(String htmlPath) {
        URL htmlUrl = requireNonNull(currentThread().getContextClassLoader().getResource(htmlPath),
                () -> "Test resource not found: " + htmlPath);
        return Html2Pdf.fromUrl(htmlUrl);
    }
}
