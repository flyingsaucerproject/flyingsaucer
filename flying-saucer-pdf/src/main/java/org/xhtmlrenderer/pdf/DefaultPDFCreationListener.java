package org.xhtmlrenderer.pdf;

/**
 * No-op implementation of a {@link org.xhtmlrenderer.pdf.PDFCreationListener}. Override methods as needed.
 */
public class DefaultPDFCreationListener implements PDFCreationListener {
    /**
     * {@inheritDoc}
     */
    public void preOpen(ITextRenderer iTextRenderer) { }

    /**
     * {@inheritDoc}
     */
    public void preWrite(ITextRenderer iTextRenderer, int pageCount) {}

    /**
     * {@inheritDoc}
     */
    public void onClose(ITextRenderer renderer) { }
}
