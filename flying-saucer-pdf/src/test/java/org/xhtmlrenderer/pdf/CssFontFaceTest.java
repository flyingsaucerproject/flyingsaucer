package org.xhtmlrenderer.pdf;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.BaseFont;
import org.w3c.dom.Document;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.resource.FSEntityResolver;
import org.xhtmlrenderer.resource.XMLResource;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.util.Objects.requireNonNull;
import static org.xhtmlrenderer.pdf.TestUtils.getFontNames;

public class CssFontFaceTest {

    /**
     * Verifies that font-face declarations with the -fs-pdf-font-embed property
     * actually results in the font being embedded in the pdf
     * <p>
     * The Jacquard24 font used to test is from <a href="https://fonts.google.com/specimen/Jacquard+24">Google Fonts</a>
     * and is licenced under the Open Font License
     */
    @Test
    public void autoInstallationOfCssDeclaredFonts() throws IOException {
        URL htmlUrl = requireNonNull(getClass().getResource("fonts/CssFontFace.html"), "test resource not found: fonts/CssFontFace.html");
        byte[] pdfBytes = Html2Pdf.fromUrl(htmlUrl);
        try (RandomAccessRead buffer = new RandomAccessReadBuffer(pdfBytes);
             PDDocument document = new PDFParser(buffer).parse()) {
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            for (int i = 0; i < document.getNumberOfPages(); ++i) {
                PDPage page = document.getPage(i);
                PDResources res = page.getResources();
                List<String> fontNames = getFontNames(res);
                assertThat(fontNames)
                        .as("Should contain Jacquard24 font")
                        .anyMatch(name -> name.contains("Jacquard24"));
            }
        }
    }

    /**
     * Verifies that imported font-face declarations with embedFontFaces
     * actually results in the font being embedded in the pdf
     * Also verifies that opaque urls with the font-face format property
     * are correctly supported
     */
    @Test
    public void importedFontFaceWithEmbedOverride() throws Exception {
        URL htmlUrl = requireNonNull(getClass().getResource("fonts/CssImportFontFace.html"), "test resource not found: fonts/CssImportFontFace.html");
        ITextRenderer renderer = new ITextRenderer();
        renderer.getFontResolver().addEmbedFontFace("Jacquard 24", BaseFont.IDENTITY_H);
        renderer.getSharedContext().setMedia("pdf");
        renderer.getSharedContext().setInteractive(false);
        renderer.getSharedContext().getTextRenderer().setSmoothingThreshold(0);

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        builder.setEntityResolver(FSEntityResolver.instance());

        Document doc = builder.parse(htmlUrl.toString());
        byte[] pdfBytes = renderer.createPDF(doc);
        assertEmbeddedJacquard(pdfBytes);
    }

    /**
     * Issue #695: {@code url('/abs/path.ttf')} inside an inline {@code <style>}
     * {@code @font-face} used to be rewritten to {@code inline/abs/path.ttf}. Relative
     * urls still resolve against the HTML document, so they never show that.
     * A server-relative path does, and the font must still be embedded.
     */
    @Test
    public void serverRelativeFontPathFromInlineStyleIsEmbedded() throws Exception {
        String fontUri = "/fonts/Jacquard24-Regular.ttf";
        String html = """
                <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
                <head>
                    <style>
                        @font-face {
                            font-family: "Jacquard 24";
                            src: url("%s");
                            -fs-pdf-font-embed: embed;
                        }
                        .jacquard { font-family: "Jacquard 24", sans-serif; }
                    </style>
                </head>
                <body>
                    <p class="jacquard">JACQUARD FONT</p>
                </body>
                </html>
                """.formatted(fontUri);

        ITextRenderer renderer = new ITextRenderer();
        ITextUserAgent userAgent = new ITextUserAgent(
                renderer.getOutputDevice(),
                Math.round(renderer.getOutputDevice().getDotsPerPoint())) {
            @Override
            public byte[] getBinaryResource(String uri) {
                if (uri != null && uri.endsWith(fontUri)) {
                    return super.getBinaryResource("classpath:fonts/Jacquard24-Regular.ttf");
                }
                return super.getBinaryResource(uri);
            }
        };
        renderer.getSharedContext().setUserAgentCallback(userAgent);
        renderer.getSharedContext().setMedia("pdf");
        renderer.getSharedContext().setInteractive(false);
        renderer.getSharedContext().getTextRenderer().setSmoothingThreshold(0);

        Document doc = XMLResource.load(html).getDocument();
        byte[] pdfBytes = renderer.createPDF(doc);

        String src = renderer.getSharedContext().getCss().getFontFaceRules().get(0)
                .getCalculatedStyle()
                .valueByName(CSSName.SRC)
                .asString();
        assertThat(src).isEqualTo(fontUri);
        assertEmbeddedJacquard(pdfBytes);
    }

    private static void assertEmbeddedJacquard(byte[] pdfBytes) throws IOException {
        try (RandomAccessRead buffer = new RandomAccessReadBuffer(pdfBytes);
             PDDocument document = new PDFParser(buffer).parse()) {
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            for (int i = 0; i < document.getNumberOfPages(); ++i) {
                PDPage page = document.getPage(i);
                PDResources res = page.getResources();
                List<String> fontNames = getFontNames(res);
                assertThat(fontNames)
                        .as("Should contain Jacquard24 font")
                        .anyMatch(name -> name.contains("Jacquard24"));
            }
        }
    }
}
