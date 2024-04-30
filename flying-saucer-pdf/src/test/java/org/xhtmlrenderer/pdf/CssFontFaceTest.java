package org.xhtmlrenderer.pdf;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.util.Objects.requireNonNull;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

public class CssFontFaceTest {

  /**
   * Verifies that font-face declarations with the -fs-pdf-font-embed property
   * actually results in the font being embedded in the pdf
   *
   * The Jacquard24 font used to test is from <a href="https://fonts.google.com/specimen/Jacquard+24">Google Fonts</a>
   * and is licenced under the Open Font License
   *
   * @throws IOException is any of resources fails to load
   */
  @Test
  public void testAutoInstallationOfCssDeclaredFonts() throws IOException {
    URL htmlUrl = requireNonNull(getClass().getResource("fonts/CssFontFace.html"),
        "test resource not found: fonts/CssFontFace.html");
    byte[] pdfBytes = Html2Pdf.fromUrl(htmlUrl);
    try (PDDocument document = PDDocument.load(pdfBytes)) {
      boolean hasEmbeddedFont = false;
      for (int i = 0; i < document.getNumberOfPages(); ++i) {
        PDPage page = document.getPage(i);
        PDResources res = page.getResources();
        for (COSName fontName : res.getFontNames()) {
          PDFont font = res.getFont(fontName);
          if (font.getFontDescriptor().getFontName().contains("Jacquard24")) {
            hasEmbeddedFont = true;
          }
        }
      }
      assertThat(hasEmbeddedFont).isTrue();
    }
  }
}
