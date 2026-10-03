package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.openpdf.text.DocumentException;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.resource.XMLResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.newOutputStream;
import static java.util.Objects.requireNonNull;

public class PDFRenderTest {
    private static final Logger log = LoggerFactory.getLogger(PDFRenderTest.class);

    @Test
    public void convertSimpleHtmlToPdf() throws IOException, DocumentException {
        URL source = requireNonNull(Thread.currentThread().getContextClassLoader().getResource("hello.html"));
        File output = File.createTempFile("flying-saucer-" + getClass().getSimpleName(), ".hello.pdf");
        PDF pdf = generatePDF(source, output);
        assertThat(pdf).containsExactText("Hello, world");
    }

    @Test
    public void convertComplexHtmlToPdf() throws IOException, DocumentException {
        URL source = requireNonNull(Thread.currentThread().getContextClassLoader().getResource("hamlet.xhtml"));
        File output = File.createTempFile("flying-saucer-" + getClass().getSimpleName(), ".hamlet.pdf");
        PDF pdf = generatePDF(source, output);
        assertThat(pdf).containsText(
                "Previous Page", "Next Page",
                "Hamlet",
                "by William Shakespeare",
                "Dramatis Personae",
                "ACT I",
                "Scene 5",
                "ACT V",
                "Dramatis Personae",
                "Claudius, King of Denmark",
                "THE END");
    }

    private static PDF generatePDF(URL source, File output) throws IOException, DocumentException {
        Document doc = XMLResource.load(source).getDocument();

        try (OutputStream os = newOutputStream(output.toPath())) {
            ITextRenderer renderer = new ITextRenderer();
            ResourceLoaderUserAgent callback = new ResourceLoaderUserAgent(renderer.getOutputDevice(), renderer.getSharedContext().getDotsPerPixel());
            renderer.getSharedContext().setUserAgentCallback(callback);
            renderer.createPDF(doc, os);
        }
        log.info("Rendered {}{}  to PDF: {}", source, lineSeparator(), output.toURI());
        return new PDF(output);
    }

    private static class ResourceLoaderUserAgent extends ITextUserAgent {
        private ResourceLoaderUserAgent(ITextOutputDevice outputDevice, int dotsPerPixel) {
            super(outputDevice, dotsPerPixel);
        }

        @Override
        @Nullable
        protected InputStream resolveAndOpenStream(@Nullable String uri) {
            InputStream is = super.resolveAndOpenStream(uri);
            log.info("IN resolveAndOpenStream() {}", uri);
            return is;
        }
    }
}
