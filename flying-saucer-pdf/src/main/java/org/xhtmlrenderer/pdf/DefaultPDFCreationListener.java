package org.xhtmlrenderer.pdf;

/**
 * No-op implementation of a {@link org.xhtmlrenderer.pdf.PDFCreationListener}. Override methods as needed.
 */
public class DefaultPDFCreationListener implements PDFCreationListener {
    @Override
    public void preOpen(ITextRenderer iTextRenderer) { }

    @Override
    public void preWrite(ITextRenderer iTextRenderer, int pageCount) {}

    @Override
    public void onClose(ITextRenderer renderer) { }
}
