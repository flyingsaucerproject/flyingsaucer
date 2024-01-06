/*
 * {{{ header & license
 * Copyright (c) 2008 Patrick Wright
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
 * }}}
 */

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static java.nio.file.Files.newOutputStream;

/**
 * This sample shows how to create a single PDF document from multiple input documents.
 */
public class PDFRenderToMultiplePages {
    public static void main(String[] args) throws IOException {
        // create some simple, fake documents; nothing special about these, anything that Flying Saucer
        // can otherwise render
        final String[] inputs = {
                newPageHtml(1, "red"),
                newPageHtml(2, "blue"),
                newPageHtml(3, "green")
        };

        final File outputFile = File.createTempFile("FlyingSaucer.PDFRenderToMultiplePages", ".pdf");
        try (OutputStream os = newOutputStream(outputFile.toPath())) {

            ITextRenderer renderer = new ITextRenderer();

            // we need to create the target PDF
            // we'll create one page per input string, but we call layout for the first
            renderer.setDocumentFromString(inputs[0]);
            renderer.layout();
            renderer.createPDF(os, false);

            // each page after the first we add using layout() followed by writeNextDocument()
            for (int i = 1; i < inputs.length; i++) {
                renderer.setDocumentFromString(inputs[i]);
                renderer.layout();
                renderer.writeNextDocument();
            }

            // complete the PDF
            renderer.finishPDF();

            System.out.println("Sample file with " + inputs.length + " documents rendered as PDF to " + outputFile);
        }
    }

    private static String newPageHtml(int pageNo, String color) {
        return "<html style='color: " + color + "' >" +
                "    Page" + pageNo +
                "</html>";
    }
}
