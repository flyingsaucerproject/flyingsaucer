package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import com.lowagie.text.DocumentException;
import org.apache.pdfbox.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;

public class SimpleHtmlTest {
    private static final Logger log = LoggerFactory.getLogger(SimpleHtmlTest.class);

    @Test
    public void simplePdf() throws DocumentException, IOException {
        ITextRenderer renderer = new ITextRenderer();

        String htmlContent = "<!DOCTYPE html><html><body><h1>My First Heading</h1><p>My first paragraph.</p></body></html>";

        renderer.setDocumentFromString(htmlContent);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        renderer.layout();
        renderer.createPDF(outputStream);
        renderer.finishPDF();

        File file = new File("target/simple.pdf");
        try (FileOutputStream o = new FileOutputStream(file)) {
            IOUtils.copy(new ByteArrayInputStream(outputStream.toByteArray()), o);
        }
        log.info("Generated PDF: {}", file.getAbsolutePath());

        PDF pdf = new PDF(file);
        assertThat(pdf).containsText("My First Heading", "My first paragraph");
    }
}
