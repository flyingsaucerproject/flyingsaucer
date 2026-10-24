package org.xhtmlrenderer.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/439">#439</a>
 * A header and footer that repeat on every page and never overlap the content:
 * put them into the page margins with {@code position: running()}.
 */
public class RunningHeaderFooterTest {
    private static final float PX = 72 / 96f;
    private static final float PAGE_WIDTH = 612;
    private static final float PAGE_HEIGHT = 792;
    private static final float MARGIN = 90 * PX;
    private static final int HEADER_COLOR = 0xd9c597;
    private static final String TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(150);

    private static final String HTML = """
        <html><head><style>
          @page {
            size: letter;
            margin: 90px 0;
            @top-center { content: element(header); }
            @bottom-center { content: element(footer); }
          }
          body { margin: 0; font-family: Arial, sans-serif; font-size: 12px; }
          header, footer { display: block; height: 30px; padding: 30px 15px;
                           background-color: #d9c597; text-align: center; font-size: 22px; color: #fff; }
          header { position: running(header); }
          footer { position: running(footer); }
          section { display: block; padding: 0 40px; page-break-after: always; }
          section:last-child { page-break-after: auto; }
        </style></head><body>
          <header>Header</header>
          <footer>Footer</footer>
          <section><p>%s</p></section>
          <section><p>Second section</p></section>
        </body></html>
        """.formatted(TEXT);

    @Test
    public void headerAndFooterAreRepeatedOnEveryPageAndDoNotOverlapContent() throws IOException {
        try (PDDocument document = Loader.loadPDF(renderPdf())) {
            assertThat(document.getNumberOfPages()).isEqualTo(3);
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                List<TextPosition> chars = textOnPage(document, page);
                assertThat(chars).as("header on page %s", page)
                    .anySatisfy(c -> assertThat(c.getYDirAdj()).isLessThan(MARGIN));
                assertThat(chars).as("footer on page %s", page)
                    .anySatisfy(c -> assertThat(c.getYDirAdj()).isGreaterThan(PAGE_HEIGHT - MARGIN));
                assertThat(chars).as("content on page %s", page)
                    .anySatisfy(c -> assertThat(c.getYDirAdj()).isBetween(MARGIN, PAGE_HEIGHT - MARGIN));
                assertThat(chars).as("no text crosses the page margins on page %s", page)
                    .noneMatch(c -> crosses(c, MARGIN) || crosses(c, PAGE_HEIGHT - MARGIN));
            }
        }
    }

    @Test
    public void headerBackgroundSpansWholePageWidth() throws IOException {
        try (PDDocument document = Loader.loadPDF(renderPdf())) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = renderer.renderImage(page);
                assertThat(image.getRGB(1, 10) & 0xffffff).as("left edge of header on page %s", page + 1).isEqualTo(HEADER_COLOR);
                assertThat(image.getRGB((int) PAGE_WIDTH - 2, 10) & 0xffffff).as("right edge of header on page %s", page + 1).isEqualTo(HEADER_COLOR);
                assertThat(image.getRGB(1, (int) PAGE_HEIGHT - 10) & 0xffffff).as("left edge of footer on page %s", page + 1).isEqualTo(HEADER_COLOR);
            }
        }
    }

    private static boolean crosses(TextPosition c, float y) {
        return c.getYDirAdj() - c.getHeightDir() < y && c.getYDirAdj() > y;
    }

    private static byte[] renderPdf() {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(HTML);
        renderer.layout();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderer.createPDF(out);
        return out.toByteArray();
    }

    private static List<TextPosition> textOnPage(PDDocument document, int page) throws IOException {
        List<TextPosition> result = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> positions) {
                result.addAll(positions);
            }
        };
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        stripper.getText(document);
        return result;
    }
}
