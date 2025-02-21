package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import com.lowagie.text.DocumentException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.resource.XMLResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;

class ITextRendererTest {
    private Logger log = LoggerFactory.getLogger(ITextRendererTest.class);

    @Test
    public void simplePdf_fitToScale() throws DocumentException, IOException {
        String htmlContent =
                "<!DOCTYPE html><html><body><h1>My First Heading</h1><p>My first paragraph.</p></body></html>";

        File file = new File("target/simple_fitToScale.pdf");
        try (FileOutputStream o = new FileOutputStream(file)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setScaleToFit(true);
            Document source = XMLResource.load(new StringReader(htmlContent)).getDocument();
            renderer.createPDF(source, o);
        }
        log.info("Generated PDF: {}", file.getAbsolutePath());

        PDF pdf = new PDF(file);
        assertThat(pdf).containsText("My First Heading", "My first paragraph");
    }
}