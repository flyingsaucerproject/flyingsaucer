package org.xhtmlrenderer.pdf.borderradius;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.pdf.Html2Pdf;

import java.net.URL;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static java.util.Objects.requireNonNull;

public class BorderRadiusNonRegressionTest {

    /**
     * This used to throw a ClassCastException (before this fix).
     */
    @Test
    public void borderRadiusWithBorderWidthZero() {
        testNoException("borderRadiusWithBorderWidthZero.html");
    }

    private void testNoException(String htmlPath) {
        URL htmlUrl = requireNonNull(getClass().getResource(htmlPath), () -> "test resource not found: " + htmlPath);
        byte[] pdf = Html2Pdf.fromUrl(htmlUrl);
        assertThat(new PDF(pdf)).containsText("Some content");
    }

}
