package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

class TransformTest {
    private static final Logger log = LoggerFactory.getLogger(TransformTest.class);

    @Test
    void rotatedBoxIsRenderedViaAContentStreamTransform() throws IOException {
        byte[] bytes = Html2Pdf.fromClasspathResource("transform.html");
        PDF pdf = printFile(log, bytes, "transform.pdf");

        assertThat(pdf).containsText("Not transformed", "Rotated");

        try (PdfReader reader = new PdfReader(bytes)) {
            String content = new String(reader.getPageContent(1), StandardCharsets.ISO_8859_1);

            // The rotated box's text is drawn with a 45-degree rotation baked into its text matrix
            // (cos45 = sin45 = 0.70711), while the untransformed box uses the identity "1 0 0 1 ... Tm".
            assertThat(content).contains("1 0 0 1 42 789.11 Tm");
            assertThat(content).containsPattern("0\\.70711 -0\\.70711 0\\.70711 0\\.70711 [0-9.]+ [0-9.]+ Tm");
        }
    }

    @Test
    void positionedDescendantOfATransformedBoxInheritsTheTransform() throws IOException {
        byte[] bytes = Html2Pdf.fromClasspathResource("transform.html");
        PDF pdf = printFile(log, bytes, "transform.pdf");

        assertThat(pdf).containsText("absolute");

        try (PdfReader reader = new PdfReader(bytes)) {
            String content = new String(reader.getPageContent(1), StandardCharsets.ISO_8859_1);

            // The "position: absolute" descendant establishes its own layer; unless that layer is
            // painted as part of its transformed ancestor's stacking context, this text would be drawn
            // with the identity matrix instead of a 90-degree rotation (cos90=0, sin90=1).
            assertThat(content).containsPattern("0 -1 1 0 [0-9.]+ [0-9.]+ Tm");
        }
    }
}
