package org.xhtmlrenderer.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code target-counter(attr(href), page)} must resolve page numbers relative to the
 * page sequence ({@code -fs-page-sequence: start}) that contains the target element.
 */
public class PageSequenceTargetCounterTest {
    private static final String STYLE = """
        <style>
          @page { size: A5; margin: 20mm; }
          body { font-family: sans-serif; font-size: 12px; }
          .sequence { -fs-page-sequence: start; page-break-before: always; }
          .chapter + .chapter { page-break-before: always; }
          a.toc::after { content: " p" target-counter(attr(href), page); }
        </style>
        """;

    @Test
    void targetInSinglePageSequence() throws IOException {
        String html = """
            <html><head>%s</head><body>
              <p><a class="toc" href="#alpha">Alpha</a></p>
              <p><a class="toc" href="#beta">Beta</a></p>
              <div class="sequence">
                <div class="chapter" id="alpha">Alpha chapter</div>
                <div class="chapter" id="beta">Beta chapter</div>
              </div>
            </body></html>
            """.formatted(STYLE);

        String toc = firstPageText(renderPdf(html));

        assertThat(toc).contains("Alpha p1", "Beta p2");
    }

    @Test
    void targetInLastOfSeveralPageSequences() throws IOException {
        String html = """
            <html><head>%s</head><body>
              <p><a class="toc" href="#alpha">Alpha</a></p>
              <p><a class="toc" href="#beta">Beta</a></p>
              <p><a class="toc" href="#gamma">Gamma</a></p>
              <p><a class="toc" href="#delta">Delta</a></p>
              <div class="sequence">
                <div class="chapter" id="alpha">Alpha chapter</div>
                <div class="chapter" id="beta">Beta chapter</div>
              </div>
              <div class="sequence">
                <div class="chapter" id="gamma">Gamma chapter</div>
                <div class="chapter" id="delta">Delta chapter</div>
              </div>
            </body></html>
            """.formatted(STYLE);

        String toc = firstPageText(renderPdf(html));

        assertThat(toc).contains("Alpha p1", "Beta p2", "Gamma p1", "Delta p2");
    }

    private static byte[] renderPdf(String html) {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderer.createPDF(out);
        return out.toByteArray();
    }

    private static String firstPageText(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            return stripper.getText(document);
        }
    }
}
