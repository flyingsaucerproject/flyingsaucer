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
    public PdfStructureElement preOpenTag(PdfStructureElement struc, PdfContentByte cb);

    /**
     * Called immediately before close a tag
     *
     */
    public void preCloseTag();
    
    /**
     * Method that return true if new page created
     * @return
     */
    public boolean newPageCreated();
    
    /**
     * Sets if new page created
     * @param newPageCreated
     */
    public void setNewPageCreated(boolean newPageCreated);
    
    /**
     * 
     * Gets current open tags
     * @return
     */
    public int getCurrentOpenTags();
    
    public void closeOpenTags();
}
