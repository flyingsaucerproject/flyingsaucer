package org.xhtmlrenderer.pdf;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class SvgTest {
    private static final Logger log = LoggerFactory.getLogger(SvgTest.class);

    @Test
    void pageWithSVG() throws IOException, ParserConfigurationException, SAXException {
        String fileName = "page-with-svg.html";
        URL template = requireNonNull(getClass().getClassLoader().getResource(fileName));
        try (InputStream htmlStream = template.openStream()) {
            String htmlContent = new String(htmlStream.readAllBytes(), UTF_8);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(htmlContent)));

            ITextRenderer renderer = new ITextRenderer();

            ITextUserAgent userAgent = new ITextUserAgent(renderer.getOutputDevice(), Math.round(renderer.getOutputDevice().getDotsPerPoint()));
            renderer.getSharedContext().setUserAgentCallback(userAgent);

            renderer.setDocument(document, template.toString());

            File result = new File("target", fileName + ".pdf");
            try (FileOutputStream outputStream = new FileOutputStream(result)) {
                renderer.layout();
                renderer.createPDF(outputStream);
            }
            log.info("PDF with SVG: {}", result.getAbsolutePath());
        }
    }
}
