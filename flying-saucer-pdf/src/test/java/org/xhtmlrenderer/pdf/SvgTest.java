package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

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

            PDF pdf = new PDF(result);
            assertThat(pdf.text).contains("SVG Star:");
        }
    }

    @Test
    void svgWithoutGradients_isDrawnAsVectorGraphics() throws IOException {
        byte[] pdf = renderHtml("<img src=\"classpath:/star.svg\" width=\"100px\"/>");

        assertThat(xObjects(pdf)).containsExactly("form[]");
        assertThat(pdf).hasSizeLessThan(5_000);
    }

    @Test
    void svgWithGradients_isRasterized() throws IOException {
        byte[] pdf = renderHtml("<img src=\"classpath:/go-home.svg\" width=\"48px\"/>");

        assertThat(xObjects(pdf)).isNotEmpty().containsOnly("image");
    }

    private static byte[] renderHtml(String body) {
        ITextRenderer renderer = ITextRenderer.fromString("<html><head><title>svg</title></head><body>" + body + "</body></html>");
        renderer.layout();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderer.createPDF(out);
        return out.toByteArray();
    }

    private static List<String> xObjects(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return xObjects(document.getPage(0).getResources());
        }
    }

    /**
     * @return description of XObjects: "image" for raster images, "form[...]" for vector graphics (with nested XObjects)
     */
    private static List<String> xObjects(@Nullable PDResources resources) throws IOException {
        if (resources == null) return List.of();
        List<String> result = new ArrayList<>();
        for (COSName name : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(name);
            if (xObject instanceof PDImageXObject) {
                result.add("image");
            } else if (xObject instanceof PDFormXObject form) {
                result.add("form" + xObjects(form.getResources()));
            } else {
                result.add(xObject.getClass().getSimpleName());
            }
        }
        return result;
    }
}
