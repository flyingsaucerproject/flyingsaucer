package org.xhtmlrenderer.pdf;

import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfAConformanceTest {

    /**
     * Every character painted must come from the embedded "Jacquard 24" font - falling back to any
     * base-14 font (e.g. via an unstyled element) would make OpenPDF itself reject the document with
     * a {@code PdfXConformanceException}, since base-14 fonts can never be embedded.
     */
    private static final String ALL_TEXT_IN_EMBEDDED_FONT = """
        <html><head><style>
            @font-face {
                font-family: "Jacquard 24";
                src: url("classpath:fonts/Jacquard24-Regular.ttf");
                -fs-pdf-font-embed: embed;
            }
            body { font-family: "Jacquard 24"; }
        </style></head>
        <body><p>Hello PDF/A</p></body></html>
        """;

    @Test
    void generatesOutputIntentAndDocumentXmpMetadata() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setPdfAConformance(PdfAConformance.PDF_A_1B);
        renderer.setDocumentFromString(ALL_TEXT_IN_EMBEDDED_FONT);
        renderer.layout();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        renderer.createPDF(os);
        byte[] pdfBytes = os.toByteArray();

        try (RandomAccessRead buffer = new RandomAccessReadBuffer(pdfBytes);
             PDDocument document = new PDFParser(buffer).parse()) {
            assertThat(document.isEncrypted()).isFalse();

            PDDocumentCatalog catalog = document.getDocumentCatalog();
            assertThat(catalog.getOutputIntents()).isNotEmpty();

            String xmp = new String(catalog.getMetadata().exportXMPMetadata().readAllBytes(), UTF_8);
            assertThat(xmp).contains("pdfaid:part").contains("pdfaid:conformance");
        }
    }

    @Test
    void rejectsNonEmbeddedFontFace() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setPdfAConformance(PdfAConformance.PDF_A_1B);
        renderer.setDocumentFromString("""
            <html><head><style>
                @font-face {
                    font-family: "Jacquard 24";
                    src: url("classpath:fonts/Jacquard24-Regular.ttf");
                }
                .jacquard { font-family: "Jacquard 24"; }
            </style></head>
            <body><p class="jacquard">Not embedded</p></body></html>
            """);
        renderer.layout();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertThatThrownBy(() -> renderer.createPDF(os))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("PDF/A conformance requires all fonts to be embedded");
    }
}
