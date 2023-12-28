package org.xhtmlrenderer.pdf;

import com.lowagie.text.DocumentException;
import junit.framework.TestCase;
import org.apache.pdfbox.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SimpleHtmlTest extends TestCase {
    private static final Logger log = LoggerFactory.getLogger(SimpleHtmlTest.class);

    public void testSimplePdf() throws DocumentException, IOException {
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
    }
}
