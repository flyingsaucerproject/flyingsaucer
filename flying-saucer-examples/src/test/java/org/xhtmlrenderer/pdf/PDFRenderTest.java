package org.xhtmlrenderer.pdf;

import com.lowagie.text.DocumentException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.resource.XMLResource;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import static java.nio.file.Files.newOutputStream;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

public class PDFRenderTest {
    private static final Logger log = LoggerFactory.getLogger(PDFRenderTest.class);

    @Test
    public void testConvertSimpleHtmlToPdf() throws IOException, DocumentException {
        URL source = requireNonNull(Thread.currentThread().getContextClassLoader().getResource("hello.html"));
        File output = File.createTempFile("flying-saucer-" + getClass().getSimpleName(), ".hello.pdf");
        createPDF(source, output);
        log.info("Rendered {} to PDF: {}", source, output.toURI());
        assertThat(output).exists();
    }

    @Test
    public void testConvertComplexHtmlToPdf() throws IOException, DocumentException {
        URL source = requireNonNull(Thread.currentThread().getContextClassLoader().getResource("hamlet.xhtml"));
        File output = File.createTempFile("flying-saucer-" + getClass().getSimpleName(), ".hamlet.pdf");
        createPDF(source, output);
        log.info("Rendered {} to PDF: {}", source, output.toURI());
        assertThat(output).exists();
    }

    private static void createPDF(URL url, File output) throws IOException, DocumentException {
        try (OutputStream os = newOutputStream(output.toPath())) {
            ITextRenderer renderer = new ITextRenderer();
            ResourceLoaderUserAgent callback = new ResourceLoaderUserAgent(renderer.getOutputDevice());
            callback.setSharedContext(renderer.getSharedContext());
            renderer.getSharedContext().setUserAgentCallback(callback);

            Document doc = XMLResource.load(new InputSource(url.toString())).getDocument();

            renderer.setDocument(doc, url.toString());
            renderer.layout();
            renderer.createPDF(os);
        }
    }

    private static class ResourceLoaderUserAgent extends ITextUserAgent {
        private ResourceLoaderUserAgent(ITextOutputDevice outputDevice) {
            super(outputDevice);
        }

        @Override
        protected InputStream resolveAndOpenStream(String uri) {
            InputStream is = super.resolveAndOpenStream(uri);
            log.info("IN resolveAndOpenStream() {}", uri);
            return is;
        }
    }
}
