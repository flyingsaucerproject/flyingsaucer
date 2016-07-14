package org.xhtmlrenderer.pdf;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
    // PDF/UA List of LI previosly tagged. avoid repeat tag
    private List<Node> liTagged = new ArrayList<Node>();
    // PDF/UA List of UL/OL previosly tagged. avoid repeat tag
    private List<Node> ulTagged = new ArrayList<Node>();
    // PDF/UA parent list element
    private PdfStructureElement parentListElement;
    // PDF/UA parent block struc element
    private PdfStructureElement currentBlockStrucElement;
    // PDF/UA parent block xml element
    private Element currentBlockElement;
    //PDF/UA Rendering Context
    private RenderingContext renderingContext;
    
    
    
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
	public List<Node> getLiTagged() {
		return liTagged;
	}
	public void setLiTagged(List<Node> liTagged) {
		this.liTagged = liTagged;
	}
	public List<Node> getUlTagged() {
		return ulTagged;
	}
	public void setUlTagged(List<Node> ulTagged) {
		this.ulTagged = ulTagged;
	}
	public PdfStructureElement getParentListElement() {
		return parentListElement;
	}
	public void setParentListElement(PdfStructureElement parentListElement) {
		this.parentListElement = parentListElement;
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

}
