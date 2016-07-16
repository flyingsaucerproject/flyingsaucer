package org.xhtmlrenderer.event;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfStructureElement;

/**
 * Callback listener for PDF tag creation.
 */
public interface DocTagListenerAccessible {
    /**
     * Called immediately before open a tag
     * 
     * Return a parent structure element if orphan child found, null if not orphan child
     */
    PdfStructureElement preOpenTag(PdfStructureElement struc, PdfContentByte cb);

    /**
     * Called immediately before close a tag
     *
     */
    void preCloseTag(PdfStructureElement struc);
    
    /**
     * Method that return true if new page created
     * @return
     */
    boolean newPageCreated();
    
    /**
     * Sets if new page created
     * @param newPageCreated
     */
    void setNewPageCreated(boolean newPageCreated);
}
