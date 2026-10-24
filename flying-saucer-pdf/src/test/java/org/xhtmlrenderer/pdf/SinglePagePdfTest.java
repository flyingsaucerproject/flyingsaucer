package org.xhtmlrenderer.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/375">#375</a>
 */
public class SinglePagePdfTest {
    private static final String ITEMS = "<div>Item ........ 1.99 EUR</div>".repeat(80);

    @Test
    public void layoutFailsFastWithoutPrintMode() {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setPrint(false);
        renderer.setDocumentFromString(html(3));

        assertThatThrownBy(renderer::layout)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("requires print mode");
    }

    /**
     * To get the whole content on one page (e.g. a receipt for a thermal printer), lay it out twice:
     * first on a very tall page to measure the content height, then on a page of exactly that height.
     */
    @Test
    public void pageHeightCanBeFittedToContent() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html(1000));
        renderer.layout();
        float contentHeight = renderer.getRootBox().getHeight() / renderer.getSharedContext().getDPI();

        assertThat(contentHeight).as("content would not fit on a regular receipt page").isGreaterThan(5);

        // add back the page margins, plus a bit so that the last line doesn't touch the bottom of the page
        float pageHeight = contentHeight + 2 * 0.125f + 0.01f;
        renderer.setDocumentFromString(html(pageHeight));
        renderer.layout();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderer.createPDF(out);

        try (PDDocument pdf = Loader.loadPDF(out.toByteArray())) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(1);
            assertThat(pdf.getPage(0).getMediaBox().getHeight()).isCloseTo(pageHeight * 72, within(1f));
        }
    }

    private static String html(float pageHeightInches) {
        return """
            <html><head><style>
              @page { margin: 0.125in; size: 3in %sin; }
              body { margin: 0; padding: 0; font-size: 10pt; }
            </style></head><body>%s</body></html>
            """.formatted(pageHeightInches, ITEMS);
    }
}
