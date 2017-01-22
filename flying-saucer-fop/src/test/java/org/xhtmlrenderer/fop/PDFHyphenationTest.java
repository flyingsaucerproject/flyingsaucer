/*
 * Copyright (C) 2017 Lukas Zaruba, lukas.zaruba@gmail.com
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
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
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
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
