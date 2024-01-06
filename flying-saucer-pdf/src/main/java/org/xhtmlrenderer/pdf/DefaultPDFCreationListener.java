package org.xhtmlrenderer.pdf;

/**
 * No-op implementation of a {@link org.xhtmlrenderer.pdf.PDFCreationListener}. Override methods as needed.
 */
public class DefaultPDFCreationListener implements PDFCreationListener {
    public void preOpen(ITextRenderer iTextRenderer) { }

    public void preWrite(ITextRenderer iTextRenderer, int pageCount) {}

    public void onClose(ITextRenderer renderer) { }
}
