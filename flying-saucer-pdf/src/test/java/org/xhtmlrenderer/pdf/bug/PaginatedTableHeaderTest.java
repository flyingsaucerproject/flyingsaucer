package org.xhtmlrenderer.pdf.bug;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

/**
 * Reproducible example for <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/612">issue 612</a>
 */
class PaginatedTableHeaderTest {
    private static final Logger log = LoggerFactory.getLogger(PaginatedTableHeaderTest.class);

    /**
     * An absolutely positioned element inside a table cell must not break the repeated header
     * of a table row which is split across pages.
     */
    @Test
    void repeatsHeaderAboveRowSplitAfterAbsolutelyPositionedElement() throws IOException {
        String page = """
            <html>
              <head>
                <style>
                  @page { size: A5; margin: 10mm; }
                  table { width: 100%%; border-collapse: collapse; -fs-table-paginate: paginate; }
                  td, th { border: 1px solid black; vertical-align: top; }
                  tr.filler td { height: 60px; }
                  .note { position: relative; background: #e6fdff; }
                  .icon { position: absolute; top: 0; left: 0; }
                </style>
              </head>
              <body>
                <table>
                  <thead><tr><th>Repeated header</th></tr></thead>
                  <tbody>
                    %s
                    <tr><td>
                      <div class="note"><span class="icon">!</span>Note</div>
                      <p>%s</p>
                    </td></tr>
                  </tbody>
                </table>
              </body>
            </html>
            """.formatted("<tr class=\"filler\"><td>filler</td></tr>".repeat(7), "Body text ".repeat(250));

        ITextRenderer renderer = new ITextRenderer();
        byte[] result = renderer.createPDF(XMLResource.load(page).getDocument());
        printFile(log, result, "issue-612-paginated-table-header.pdf");

        try (PDDocument pdf = Loader.loadPDF(result)) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(2);
            assertThat(pageText(pdf, 1)).startsWith("Repeated header");
            assertThat(pageText(pdf, 2)).startsWith("Repeated header\nBody text");
        }
    }

    private static String pageText(PDDocument pdf, int pageNo) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageNo);
        stripper.setEndPage(pageNo);
        return stripper.getText(pdf);
    }
}
