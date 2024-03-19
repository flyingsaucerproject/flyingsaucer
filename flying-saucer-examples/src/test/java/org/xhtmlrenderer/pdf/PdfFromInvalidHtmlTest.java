package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xhtmlrenderer.util.IOUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;

/**
 * Sample code for generating PDF from an invalid HTML source.
 * It uses JSoup library to convert invalid HTML into Document
 * and method {@link ITextRenderer#setDocument(Document)} to use the parsed Document.
 *
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/298">See issue 298</a>
 */
public class PdfFromInvalidHtmlTest {
    @Test
    public void samplePdf() throws IOException {
        byte[] pdf = generatePdf("invalid.html");
        assertThat(new PDF(pdf)).containsText("Hello, invalid html");
    }

    private org.w3c.dom.Document parseInvalidHtml(URL htmlUrl) throws IOException {
        try (InputStream in = IOUtil.openStreamAtUrl(htmlUrl.toExternalForm())) {
            org.jsoup.nodes.Document document = Jsoup.parse(in, StandardCharsets.UTF_8.name(), "/");
            document.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
            document.outputSettings().charset(StandardCharsets.UTF_8);
            return new W3CDom().fromJsoup(document);
        }
    }

    private byte[] generatePdf(String htmlPath) throws IOException {
        URL htmlUrl = requireNonNull(currentThread().getContextClassLoader().getResource(htmlPath), 
                () -> "Test resource not found: " + htmlPath);

        Document document = parseInvalidHtml(htmlUrl);

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocument(document);
        renderer.layout();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderer.createPDF(out);
        return out.toByteArray();
    }

}
