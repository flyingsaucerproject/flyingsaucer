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

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.openpdf.text.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public class PDFHyphenationTest {
    private static final Logger log = LoggerFactory.getLogger(PDFHyphenationTest.class);

    private static final String XML =
            """
                    <html>
                    	<head>
                    		<meta http-equiv="Content-Language" content="cs"/>
                    		<style media="print" type="text/css">
                    			body {
                    				background: gray;
                    				margin:0;
                    				word-wrap: break-word;
                    				text-align: justify;
                    				font-size: 7.5pt;
                    				line-height: 1;
                                   hyphens: auto
                    			}

                    			@page {
                    				size: 43mm 25mm;
                    				margin-top:0cm;
                    			    margin-left:0cm;
                    			    margin-right:0cm;
                    			    margin-bottom:0cm;
                    			}
                    		</style>
                    	</head>
                    	<body>
                    		Velice dlouhy text, ktery bude mit problemy se zalamovanim, pokud nebude perfektne nastaveno.
                    	</body>
                    </html>""";

    @Test
    public void generator() throws Exception {
        Path temp = Files.createTempFile("pdfTest", ".pdf");
        OutputStream os = Files.newOutputStream(temp);
        generatePDF(XML, os);
        log.info("Generated file: {}", temp);

        PDF pdf = new PDF(temp.toFile());
        assertThat(pdf).containsText(
                "Velice dlouhy text",
                "pokud nebude perfektne nastaveno"
        );
    }

    private void generatePDF(String xml, OutputStream os) throws DocumentException {
        ITextRenderer renderer = new ITextRenderer();
        ITextUserAgent ua = new ITextUserAgent(renderer.getOutputDevice(), renderer.getSharedContext().getDotsPerPixel());
        renderer.getSharedContext().setUserAgentCallback(ua);
        renderer.getSharedContext().setLineBreakingStrategy(new FOPLineBreakingStrategy());

        renderer.setDocumentFromString(xml);
        renderer.layout();
        renderer.createPDF(os);
    }
}
