package org.xhtmlrenderer.event;

import java.util.LinkedList;

import com.itextpdf.text.DocListener;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfStructureElement;

/**
 * Implementation of a {@link org.xhtmlrenderer.pdf.PDFCreationListener}. Control the open tags created for accessibility
 * To avoid unbalanced errors when a new document is created (new page).
 */
public class DocTagListenerAccessibleImpl implements DocListener, DocTagListenerAccessible {
	
	private LinkedList<PdfStructureElement> currentOpenTags = new LinkedList<PdfStructureElement>();
	private LinkedList<PdfStructureElement> currentForcedClodedTags = new LinkedList<PdfStructureElement>();
	private PdfContentByte cb;
	private boolean newPageCreated;


    /**
     * {@inheritDoc}
     */
	@Override
	public PdfStructureElement preOpenTag(PdfStructureElement struc, PdfContentByte cb) {
		//First time called we add ContentByte to this instance
    	if(this.cb == null){
    		this.cb = cb;
    	}
    	PdfStructureElement parentStruc = null;
		// Check orphan LI elements produced by new document for page
		if (PdfName.LI.equals(struc.get(PdfName.S))) {
			PdfStructureElement lastOpenTag = currentForcedClodedTags.peekLast();
			if(lastOpenTag != null && PdfName.L.equals(lastOpenTag.get(PdfName.S))){
				parentStruc = lastOpenTag;
				//Removing avoiding reprocessing
				currentForcedClodedTags.removeLast();
			}
		}
		currentOpenTags.add(struc);

		System.out.println("Added struc in preOpenTag operation:" + struc.getStructureType());
		return parentStruc;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void preCloseTag() {
		if(currentOpenTags.size() > 0){
			System.out.println("Remove struc in preCoseTag operation:" + currentOpenTags.peekLast().getStructureType());
			currentOpenTags.removeLast();
		}
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean add(Element element) throws DocumentException {
		return false;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void open() {
		
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void close() {

	}

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean newPage() {
		closeOpenTags();
		return false;
	}
	
	public void closeOpenTags(){
		setNewPageCreated(true);
		currentForcedClodedTags = new LinkedList<PdfStructureElement>();
		addAll(currentOpenTags, currentForcedClodedTags);
    	for (PdfStructureElement pdfStructureElement : currentForcedClodedTags) {
			cb.endMarkedContentSequence();
			currentOpenTags.removeLast();
			System.out.println("struct closed and removed for new page creation." + pdfStructureElement.getStructureType());
		}
	}
	
	private void addAll(LinkedList<PdfStructureElement> ori, LinkedList<PdfStructureElement> des){
		for (PdfStructureElement pdfStructureElement : ori) {
			des.add(pdfStructureElement);
		}
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	public boolean setPageSize(Rectangle pageSize) {
		return false;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean setMargins(float marginLeft, float marginRight, float marginTop, float marginBottom) {
		return false;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean setMarginMirroring(boolean marginMirroring) {
		return false;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean setMarginMirroringTopBottom(boolean marginMirroringTopBottom) {
		return false;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void setPageCount(int pageN) {
		
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void resetPageCount() {
		
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean newPageCreated() {
		return this.newPageCreated;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void setNewPageCreated(boolean newPageCreated) {
		this.newPageCreated = newPageCreated;
	}
	
    /**
     * 
     * Gets current open tags
     * @return
     */
    public int getCurrentOpenTags(){
    	return this.currentOpenTags.size();
    }
}
