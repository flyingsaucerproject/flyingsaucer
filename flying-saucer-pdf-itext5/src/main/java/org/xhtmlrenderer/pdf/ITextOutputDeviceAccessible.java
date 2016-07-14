package org.xhtmlrenderer.pdf;

import java.util.logging.Level;

import org.apache.commons.lang.WordUtils;
import org.w3c.dom.Node;
import org.xhtmlrenderer.util.XRLog;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfStructureElement;
import com.itextpdf.text.pdf.PdfStructureTreeRoot;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Contains accessibility PDF/UA generation methods.
 * Rewriten class delegates on this class 
 *
 */
public class ITextOutputDeviceAccessible {
		
	static PdfStructureTreeRoot getRoot(PdfWriter writer){
		return writer.getStructureTreeRoot();
	}

	static PdfStructureElement createTagDocument(PdfStructureTreeRoot root){
		return new PdfStructureElement(root, PdfName.DOCUMENT);
	}
	
	static void beginMarkedContentSequenceDrawingString(Node parentNode, String text, PdfStructureElement tagDocument, PdfContentByte cb, PdfStructureTreeRoot root) {		        
        String htmlNodeName = parentNode.getNodeName();
        PdfStructureElement struc = getStrucElementByHtmlElement(tagDocument, htmlNodeName, text);
        if(struc == null){
        	struc = getStructElement(tagDocument, htmlNodeName, root, text);
        }
        cb.beginMarkedContentSequence(struc);  
    }
	
	public static PdfStructureElement getStructElement(PdfStructureElement parentStruct, String htmlNodeName, PdfStructureTreeRoot root, String text){
		PdfStructureElement struc = getStrucElementByHtmlElement(parentStruct, htmlNodeName, text);
    	try{ 
    		if(struc == null){
	    		String pdfName = htmlNodeName.toUpperCase();
	    		if(pdfName.length() > 2){
	    			pdfName = WordUtils.capitalizeFully(pdfName);
	    		}
	    		PdfName tag = new PdfName(pdfName);
	    		struc = new PdfStructureElement(parentStruct, tag);
    		}
    	}catch(Exception e){
    		XRLog.log("ITextOutputDeviceAccessible.getStructElement", Level.INFO, "Creating a new element in the dictionary:" + htmlNodeName.toUpperCase());
    		//Mapping new structure element
    		root.mapRole(new PdfName(htmlNodeName.toUpperCase()), PdfName.P);
    		struc = new PdfStructureElement(parentStruct, new PdfName(htmlNodeName.toUpperCase()));
    	}
    	return struc;
	}
	
	private static PdfStructureElement getStrucElementByHtmlElement(PdfStructureElement parentStruct, String htmlNodeName, String text){
		PdfStructureElement struc = null;
		//White spaces, commas and dots
        if(text != null && (text.trim().length() == 0 || text.trim().equalsIgnoreCase(",") || text.trim().equalsIgnoreCase("."))){
        	struc = new PdfStructureElement(parentStruct, PdfName.SPAN);
        //So far icons fonts
        }else if(text != null && text.length() <= 2){        	
        	struc = new PdfStructureElement(parentStruct, PdfName.ARTIFACT);
        // others HTML elements treated as Span on PDF
        }else if (htmlNodeName.equalsIgnoreCase("HTML") || htmlNodeName.equalsIgnoreCase("BODY")){
          	struc = new PdfStructureElement(parentStruct, PdfName.SPAN);
        }else if (htmlNodeName.equalsIgnoreCase("A") || htmlNodeName.equalsIgnoreCase("LI")){
        	struc = new PdfStructureElement(parentStruct, PdfName.SPAN);
        }else if (htmlNodeName.equalsIgnoreCase("TD")){
        	struc = new PdfStructureElement(parentStruct, PdfName.P);
        }
        return struc;
	}
	
	static void endMarkedContentSequenceDrawingString(PdfContentByte cb) {
        cb.endMarkedContentSequence();
    }
	
}
