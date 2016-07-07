package org.xhtmlrenderer.pdf;

import java.awt.Rectangle;

import org.apache.commons.lang.WordUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.pdf.ITextFontResolver.FontDescription;
import org.xhtmlrenderer.pdf.util.DomUtilsAccessible;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfStructureElement;
import com.itextpdf.text.pdf.PdfStructureTreeRoot;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Contains accessibility PDF/A generation methods.
 * Rewriten class delegates on this class 
 *
 */
public class ITextOutputDeviceAccessible {
	
	private static final String DEFAUL_IMG_ALT = "image"; 
		
	static PdfStructureTreeRoot getRoot(PdfWriter writer){
		return writer.getStructureTreeRoot();
	}

	static PdfStructureElement createTagDocument(PdfStructureTreeRoot root){
		return new PdfStructureElement(root, PdfName.DOCUMENT);
	}
	
	static void addTaggedImage(BlockBox box, PdfStructureElement tagDocument, PdfContentByte currentPage, Image image, double[] mx ) throws DocumentException{
		PdfStructureElement imageTag = new PdfStructureElement(tagDocument, PdfName.FIGURE);
		String altText = DEFAUL_IMG_ALT;
		if(box != null && box.getElement() != null && box.getElement().getAttribute("alt") != null){
			altText = box.getElement().getAttribute("alt");
		}
    	imageTag.put(new PdfName("Alt"), new PdfString(altText));
    	currentPage.beginMarkedContentSequence(imageTag);
        currentPage.addImage(image, (float) mx[0], (float) mx[1], (float) mx[2], (float) mx[3], (float) mx[4], (float) mx[5], true);
        currentPage.endMarkedContentSequence();
	}
	
	static void beginMarkedContentSequenceDrawingString(BlockBox parentBlockBox, String s, FontDescription fontDesc, float fontSize, PdfStructureElement tagDocument, PdfContentByte cb, PdfStructureTreeRoot root) {		        
        PdfStructureElement struc;
        String htmlNodeName = DomUtilsAccessible.getNodeName(parentBlockBox);
        //White spaces, commas and dots
        if(s.trim().length() == 0 || s.trim().equalsIgnoreCase(",") || s.trim().equalsIgnoreCase(".")){
        	struc = new PdfStructureElement(tagDocument, PdfName.SPAN);
        //So far icons fonts
        }else if(s.length() <= 2){        	
        	struc = new PdfStructureElement(tagDocument, PdfName.ARTIFACT);
        // others HTML elements treated as Span on PDF
        }else if (htmlNodeName.equalsIgnoreCase("HTML") || htmlNodeName.equalsIgnoreCase("BODY")){
          	struc = new PdfStructureElement(tagDocument, PdfName.SPAN);
        }else{
        	try{ 
        		String pdfName = htmlNodeName.toUpperCase();
        		if(pdfName.length() > 2){
        			pdfName = WordUtils.capitalizeFully(pdfName);
        		}
        		PdfName tag = new PdfName(pdfName);
        		struc = new PdfStructureElement(tagDocument, tag);
        	}catch(Exception e){
        		System.out.println("Crating a new element in the dictionary");
        		//Creating new element in the dictiorary
        		root.mapRole(new PdfName(htmlNodeName.toUpperCase()), PdfName.P);
        		struc = new PdfStructureElement(tagDocument, new PdfName(htmlNodeName.toUpperCase()));
        	}
        }
        cb.beginMarkedContentSequence(struc);
    }
	

	
	static void endMarkedContentSequenceDrawingString(PdfContentByte cb) {
        cb.endMarkedContentSequence();
    }
	
	static void paintReplacedElement(ITextOutputDevice outputDevice, RenderingContext c, BlockBox box) {
    	Rectangle contentBounds = box.getContentAreaEdge(box.getAbsX(), box.getAbsY(), c);
        ReplacedElement element = box.getReplacedElement();
        outputDevice.drawImage(box, ((ITextImageElement)element).getImage(), contentBounds.x, contentBounds.y);
    }
	
}
