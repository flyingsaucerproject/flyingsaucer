package org.xhtmlrenderer.pdf;

import java.io.IOException;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Contains accessibility PDF/UA generation methods.
 * Rewriten class delegates on this class 
 *
 */
public class ITextRendererAccessible {
	
	/** 
	 * Marks writer as tagged and adds accessibility metadata
	 * XMP metadata. Title and language are processed by Flying Saucer, HTML title and HTML lang information.
	 * @param writer
	 * @param doc
	 * @throws IOException
	 */
	static void addAccessibilityMetaData(PdfWriter writer, Document doc) throws IOException{
		addAccessibilityMetaData(writer, doc, null, null);
	}
	
	
	/** 
	 * Marks writer as tagged and adds accessibility metadata, title, language and
	 * XMP metadata
	 * @param writer
	 * @param doc
	 * @param language
	 * @param title
	 * @throws IOException
	 */
	static void addAccessibilityMetaData(PdfWriter writer, Document doc, String language, String title) throws IOException{
        //Make document tagged
        writer.setTagged();
        //Set document metadata
        writer.setViewerPreferences(PdfWriter.DisplayDocTitle);
        if(language != null){
        	doc.addLanguage(language);
        }
        if(title != null){
        	doc.addTitle(title);
        }
        writer.createXmpMetadata();
	}
	
	

}
