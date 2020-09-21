package org.xhtmlrenderer.pdf;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.event.DocTagListenerAccessible;
import org.xhtmlrenderer.render.RenderingContext;

import com.itextpdf.text.pdf.PdfStructureElement;
import com.itextpdf.text.pdf.PdfStructureTreeRoot;

public class ITextOutputDeviceAccessibleBean {
	private boolean endMarkedSecuence;
	private boolean parentEndMarkedSecuence;
	private boolean paintText;
    // PDF/UA: The structure tree root corresponds to the highest hierarchy level in a tagged PDF
    private PdfStructureTreeRoot root;
    // PDF/UA: File structure logical principal node
    private PdfStructureElement tagDocument;
    // PDF/UA Current LI previously tagged. 
    private Node liTagged;
    // PDF/UA Current UL/OL/DL previously tagged.
    private Node ulTagged;
    // PDF/UA parent block struc element
    private PdfStructureElement currentBlockStrucElement;
    // PDF/UA parent block xml element
    private Element currentBlockElement;
    //PDF/UA Rendering Context
    private RenderingContext renderingContext;
    //PDF/UA DocListener implementation to close/open tag when a new document is created
    private DocTagListenerAccessible listener;
    
	public boolean isEndMarkedSecuence() {
		return endMarkedSecuence;
	}
	public void setEndMarkedSecuence(boolean endMarkedSecuence) {
		this.endMarkedSecuence = endMarkedSecuence;
	}
	public boolean isParentEndMarkedSecuence() {
		return parentEndMarkedSecuence;
	}
	public void setParentEndMarkedSecuence(boolean parentEndMarkedSecuence) {
		this.parentEndMarkedSecuence = parentEndMarkedSecuence;
	}
	public boolean isPaintText() {
		return paintText;
	}
	public void setPaintText(boolean paintText) {
		this.paintText = paintText;
	}
	public PdfStructureTreeRoot getRoot() {
		return root;
	}
	public void setRoot(PdfStructureTreeRoot root) {
		this.root = root;
	}
	public PdfStructureElement getTagDocument() {
		return tagDocument;
	}
	public void setTagDocument(PdfStructureElement tagDocument) {
		this.tagDocument = tagDocument;
	}
	public PdfStructureElement getCurrentBlockStrucElement() {
		return currentBlockStrucElement;
	}
	public void setCurrentBlockStrucElement(PdfStructureElement currentBlockStrucElement) {
		this.currentBlockStrucElement = currentBlockStrucElement;
	}
	public Element getCurrentBlockElement() {
		return currentBlockElement;
	}
	public void setCurrentBlockElement(Element currentBlockElement) {
		this.currentBlockElement = currentBlockElement;
	}
	public RenderingContext getRenderingContext() {
		return renderingContext;
	}
	public void setRenderingContext(RenderingContext renderingContext) {
		this.renderingContext = renderingContext;
	}
	public DocTagListenerAccessible getListener() {
		return listener;
	}
	public void setListener(DocTagListenerAccessible listener) {
		this.listener = listener;
	}
	public Node getLiTagged() {
		return liTagged;
	}
	public void setLiTagged(Node liTagged) {
		this.liTagged = liTagged;
	}
	public Node getUlTagged() {
		return ulTagged;
	}
	public void setUlTagged(Node ulTagged) {
		this.ulTagged = ulTagged;
	}

}
