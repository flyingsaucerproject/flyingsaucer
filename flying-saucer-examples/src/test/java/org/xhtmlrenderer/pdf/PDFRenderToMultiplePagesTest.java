package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.openpdf.text.DocumentException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.nio.file.Files.newOutputStream;

/**
 * This sample shows how to create a single PDF document from multiple input documents.
 */
public class PDFRenderToMultiplePagesTest {
    private static final Logger log = LoggerFactory.getLogger(PDFRenderToMultiplePagesTest.class);

    @Test
    public void generateSinglePdfFromMultipleInputDocuments() throws Exception {
        File output = File.createTempFile("flying-saucer-" + getClass().getSimpleName(), ".pdf");
        String[] inputs = createSimpleFakeDocuments();
        generatePDF(inputs, output);
        log.info("Sample file with {} documents rendered as PDF to {}", inputs.length, output.toURI());

        PDF pdf = new PDF(output);
        assertThat(pdf).containsText("Page1", "Page2", "Page3");
    }

    private static void generatePDF(String[] inputs, File output) throws IOException, DocumentException {
        try (OutputStream os = newOutputStream(output.toPath())) {
            ITextRenderer renderer = new ITextRenderer();

            // we need to create the target PDF
            // we'll create one page per input string, but we call layout for the first
            renderer.setDocumentFromString(inputs[0]);
            renderer.layout();
            renderer.createPDF(os, false);

            renderer.getWriter().setFullCompression();

            // each page after the first we add using layout() followed by writeNextDocument()
            for (int i = 1; i < inputs.length; i++) {
                renderer.setDocumentFromString(inputs[i]);
                renderer.layout();
                renderer.writeNextDocument();
            }

            // complete the PDF
            renderer.finishPDF();
        }
    }

    private static String[] createSimpleFakeDocuments() {
        return new String[]{
                newPageHtml(1, "red"),
                newPageHtml(2, "blue"),
                newPageHtml(3, "green")
        };
    }

    private static String newPageHtml(int pageNo, String color) {
        return String.format("<html style='color: %s' >    Page%s</html>", color, pageNo);
    }
}
