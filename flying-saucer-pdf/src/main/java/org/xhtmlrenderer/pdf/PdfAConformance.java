package org.xhtmlrenderer.pdf;

import org.openpdf.text.pdf.PdfWriter;

/**
 * PDF/A conformance levels supported by {@link ITextRenderer#setPdfAConformance(PdfAConformance)}.
 * <p>
 * Note that PDF/A conformance also requires every font used in the document to be embedded. The
 * built-in base-14 fonts (Helvetica, Times, Courier, Symbol, ZapfDingbats) can never be embedded by
 * OpenPDF, so a document that falls back to one of them cannot be PDF/A conformant regardless of this
 * setting.
 */
public enum PdfAConformance {
    PDF_A_1B(PdfWriter.PDFA1B),
    PDF_A_3B(PdfWriter.PDFA3B);

    private final int pdfXConformance;

    PdfAConformance(int pdfXConformance) {
        this.pdfXConformance = pdfXConformance;
    }

    int pdfXConformance() {
        return pdfXConformance;
    }
}
