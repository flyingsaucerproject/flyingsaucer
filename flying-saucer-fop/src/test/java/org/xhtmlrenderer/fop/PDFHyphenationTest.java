/*
 * Copyright (C) 2017 MEDIA SOLUTIONS
 * All Rights Reserved.  No use, copying or distribution of this
 * work may be made except in accordance with a valid license
 * agreement from MEDIA SOLUTIONS. This notice must be
 * included on all copies, modifications and derivatives of this
 * work.
 *
 * MEDIA SOLUTIONS MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * MEDIA SOLUTIONS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */
package org.xhtmlrenderer.fop;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;

import com.itextpdf.text.DocumentException;

/**
 * @author Lukas Zaruba, lukas.zaruba@media-sol.com, MEDIA SOLUTIONS
 */
public class PDFHyphenationTest {
	
	private static final String XML = 
			"<html>\n" + 
			"	<head>\n" + 
			"		<meta http-equiv=\"Content-Language\" content=\"cs\"/>" +
			"		<style media=\"print\" type=\"text/css\">\n" + 
			"			body {\n" + 
			"				background: gray;\n" + 
			"				margin:0;\n" + 
			"				word-wrap: break-word; \n" + 
			"				text-align: justify;\n" + 
			"				font-size: 7.5pt;\n" + 
			"				line-height: 1;\n" + 
			"               hyphens: auto\n" +		
			"			}\n" + 
			"\n" + 
			"			@page {\n" + 
			"				size: 43mm 25mm;\n" + 
			"				margin-top:0cm; \n" + 
			"			    margin-left:0cm; \n" + 
			"			    margin-right:0cm; \n" + 
			"			    margin-bottom:0cm; \n" + 
			"			}\n" + 
			"		</style>\n" + 
			"	</head>\n" + 
			"	<body>\n" + 
			"		Velice dlouhy text, ktery bude mit problemy se zalamovanim, pokud nebude perfektne nastaveno." +  
			"	</body>\n" + 
			"</html>";
	
	@Test
	public void testGenerator() throws Exception {
		Path temp = Files.createTempFile("pdfTest", ".pdf");
		OutputStream os = Files.newOutputStream(temp);
		generatePDF(XML, os);
		System.out.println(temp);
	}
	
	private void generatePDF(String xml, OutputStream os) throws DocumentException, IOException {
		ITextRenderer renderer = new ITextRenderer();
		ITextUserAgent ua = new ITextUserAgent(renderer.getOutputDevice());
		ua.setSharedContext(renderer.getSharedContext());
		renderer.getSharedContext().setUserAgentCallback(ua);
		renderer.getSharedContext().setLineBreakingStrategy(new FOPLineBreakingStrategy());
		
		renderer.setDocumentFromString(xml);
		renderer.layout();
		renderer.createPDF(os);
	}

}
