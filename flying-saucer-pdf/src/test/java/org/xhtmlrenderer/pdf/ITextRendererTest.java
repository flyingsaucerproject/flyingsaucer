package org.xhtmlrenderer.pdf;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ITextRendererTest {
    private final ITextRenderer cut = new ITextRenderer();

    @Test
    void versionIsNullByDefault() {
        assertThat(cut.getPDFVersion()).isNull();
    }

    @Test
    void pdfAConformanceIsNullByDefault() {
        assertThat(cut.getPdfAConformance()).isNull();
    }

    @Test
    void getAndSetPdfAConformance() {
        cut.setPdfAConformance(PdfAConformance.PDF_A_1B);
        assertThat(cut.getPdfAConformance()).isEqualTo(PdfAConformance.PDF_A_1B);
        assertThat(cut.getPDFXConformance()).isEqualTo(PdfAConformance.PDF_A_1B.pdfXConformance());
    }

    @Test
    void canClearPdfAConformance() {
        cut.setPdfAConformance(PdfAConformance.PDF_A_1B);
        cut.setPdfAConformance(null);
        assertThat(cut.getPdfAConformance()).isNull();
    }

    @Test
    void pdfAConformanceAndEncryptionAreMutuallyExclusive() {
        cut.setPdfAConformance(PdfAConformance.PDF_A_1B);
        cut.setPDFEncryption(new PDFEncryption("user".getBytes(), "owner".getBytes()));
        cut.setDocumentFromString("<html><body>Hello</body></html>");
        cut.layout();

        assertThatThrownBy(() -> cut.createPDF(new ByteArrayOutputStream()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("PDF/A conformance and PDF encryption are mutually exclusive");
    }

    @Test
    void getAndSetPDFVersion() {
        cut.setPDFVersion("2.0");
        assertThat(cut.getPDFVersion()).isEqualTo("2.0");
    }

    @Test
    void canSetVersionNull() {
        cut.setPDFVersion(null);
        assertThat(cut.getPDFVersion()).isEqualTo(null);
    }

    @Test
    void cannotSetIllegalVersion() {
        assertThatThrownBy(() -> cut.setPDFVersion("0.1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("""
                Invalid PDF version character: "0.1"; use one of constants in [1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 2.0].
                """.trim());
    }
}
