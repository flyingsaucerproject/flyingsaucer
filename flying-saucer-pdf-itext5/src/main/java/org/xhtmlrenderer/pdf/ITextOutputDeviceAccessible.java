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
	
	static void beginMarkedContentSequenceDrawingString(Node parentNode, String s, PdfStructureElement tagDocument, PdfContentByte cb, PdfStructureTreeRoot root) {		        
        PdfStructureElement struc;
        String htmlNodeName = parentNode.getNodeName();
        //White spaces, commas and dots
        if(s.trim().length() == 0 || s.trim().equalsIgnoreCase(",") || s.trim().equalsIgnoreCase(".")){
        	struc = new PdfStructureElement(tagDocument, PdfName.SPAN);
        //So far icons fonts
        }else if(s.length() <= 2){        	
        	struc = new PdfStructureElement(tagDocument, PdfName.ART);
        // others HTML elements treated as Span on PDF
        }else if (htmlNodeName.equalsIgnoreCase("HTML") || htmlNodeName.equalsIgnoreCase("BODY")){
          	struc = new PdfStructureElement(tagDocument, PdfName.SPAN);
        }else if (htmlNodeName.equalsIgnoreCase("A")){
        	struc = new PdfStructureElement(tagDocument, PdfName.SPAN);
        }
        else{
        	struc = getStructElement(tagDocument, htmlNodeName, root);
        }
        cb.beginMarkedContentSequence(struc);  
    }
	
	public static PdfStructureElement getStructElement(PdfStructureElement parentStruct, String htmlNodeName, PdfStructureTreeRoot root){
		PdfStructureElement struc;
    	try{ 
    		String pdfName = htmlNodeName.toUpperCase();
    		if(pdfName.length() > 2){
    			pdfName = WordUtils.capitalizeFully(pdfName);
    		}
    		PdfName tag = new PdfName(pdfName);
    		struc = new PdfStructureElement(parentStruct, tag);
    	}catch(Exception e){
    		XRLog.log("ITextOutputDeviceAccessible.getStructElement", Level.INFO, "Creating a new element in the dictionary:" + htmlNodeName.toUpperCase());
    		//Mapping new structure element
    		root.mapRole(new PdfName(htmlNodeName.toUpperCase()), PdfName.P);
    		struc = new PdfStructureElement(parentStruct, new PdfName(htmlNodeName.toUpperCase()));
    	}
    	return struc;
	}
	
	static void endMarkedContentSequenceDrawingString(PdfContentByte cb) {
        cb.endMarkedContentSequence();
    }
	
}
