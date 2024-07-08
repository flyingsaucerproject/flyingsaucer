package org.xhtmlrenderer.pdf;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.StreamSupport;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.util.Objects.requireNonNull;

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

    private List<String> getFontNames(PDResources res) {
        return StreamSupport.stream(res.getFontNames().spliterator(), false)
                .map(font -> getFontName(res, font))
                .toList();
    }

    private String getFontName(PDResources res, COSName font) {
        try {
            return res.getFont(font).getFontDescriptor().getFontName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
