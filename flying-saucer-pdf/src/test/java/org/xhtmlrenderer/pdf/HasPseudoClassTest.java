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

public class HasPseudoClassTest {
    private static final Logger log = LoggerFactory.getLogger(HasPseudoClassTest.class);

    @Test
    void hasPseudoClassAffectsRenderedContent() throws IOException {
        byte[] bytes = generatePdf("has-pseudo-class.html");
        PDF pdf = printFile(log, bytes, "has-pseudo-class.pdf");
        assertThat(pdf).containsText("CARD-1x [HAS]", "CARD-2 [PLAIN]");
    }

    private byte[] generatePdf(String htmlPath) {
        URL htmlUrl = requireNonNull(currentThread().getContextClassLoader().getResource(htmlPath),
                () -> "Test resource not found: " + htmlPath);
        return Html2Pdf.fromUrl(htmlUrl);
    }
}
