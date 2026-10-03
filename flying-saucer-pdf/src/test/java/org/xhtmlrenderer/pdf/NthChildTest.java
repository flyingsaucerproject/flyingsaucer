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

public class NthChildTest {
    private static final Logger log = LoggerFactory.getLogger(NthChildTest.class);

    @Test
    public void samplePdf() throws IOException {
        byte[] bytes = generatePdf("nth-child.html");
        PDF pdf = printFile(log, bytes, "nth-child.pdf");
        assertThat(pdf).containsText("o1. ODD", "o2.", "o3. ODD", "o4.");
        assertThat(pdf).containsText("e1.", "e2. EVEN", "e3.", "e4. EVEN");
        assertThat(pdf).containsText("t1.", "t2.", "t3. THIRD", "t4.");
        assertThat(pdf).containsText("af1. ", "af2. ", "af3. ", "af4. AFTER-FOURTH", "af5. AFTER-FOURTH", "af6. AFTER-FOURTH");
        assertThat(pdf).containsText("d1. d'Artagnan", "d2.", "d3.", "d4. d'Artagnan", "d5.", "d6.", "d7. d'Artagnan");
    }

    private byte[] generatePdf(String htmlPath) {
        URL htmlUrl = requireNonNull(currentThread().getContextClassLoader().getResource(htmlPath),
                () -> "Test resource not found: " + htmlPath);
        return Html2Pdf.fromUrl(htmlUrl);
    }
}
