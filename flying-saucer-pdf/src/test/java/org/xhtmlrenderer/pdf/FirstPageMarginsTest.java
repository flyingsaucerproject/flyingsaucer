package org.xhtmlrenderer.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/272">#272</a>
 */
public class FirstPageMarginsTest {
    private static final float MM = 72 / 25.4f;
    private static final String TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(40);

    @Test
    public void dynamicAutoWidthUsesWidthOfCurrentPage() throws IOException {
        float rightEdge = maxRightEdgeOnPage(html("body > * { -fs-dynamic-auto-width: dynamic; }"), 2);
        assertThat(rightEdge)
            .as("text on page 2 must not cross its 1.5cm right margin")
            .isLessThanOrEqualTo((210 - 15) * MM);
    }

    private static String html(String extraCss) {
        return """
            <html><head><style>
              @page :first { margin-top: 5mm; margin-left: 5mm; margin-bottom: 5mm; }
              @page { size: 210mm 297mm; margin-top: 3.3cm; margin-left: 1.5cm; margin-right: 1.5cm; margin-bottom: 1.7cm; }
              body { margin: 0; }
              .first { page-break-after: always; }
              %s
            </style></head><body>
              <p class="first">%s</p>
              <p>%s</p>
            </body></html>
            """.formatted(extraCss, TEXT, TEXT);
    }

    private static float maxRightEdgeOnPage(String html, int page) throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderer.createPDF(out);
        byte[] pdf = out.toByteArray();
        try (PDDocument document = Loader.loadPDF(pdf)) {
            float[] max = {0};
            PDFTextStripper stripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> positions) {
                    for (TextPosition p : positions) {
                        max[0] = Math.max(max[0], p.getXDirAdj() + p.getWidthDirAdj());
                    }
                }
            };
            stripper.setStartPage(page);
            stripper.setEndPage(page);
            stripper.getText(document);
            return max[0];
        }
    }
}
