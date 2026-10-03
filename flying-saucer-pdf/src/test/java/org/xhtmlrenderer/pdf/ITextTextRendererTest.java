package org.xhtmlrenderer.pdf;

import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.BaseFont;
import org.xhtmlrenderer.render.FSFontMetrics;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.openpdf.text.pdf.BaseFont.EMBEDDED;
import static org.openpdf.text.pdf.BaseFont.IDENTITY_H;

class ITextTextRendererTest {
    private final ITextTextRenderer renderer = new ITextTextRenderer();
    private final ITextFontContext context = new ITextFontContext();

    /**
     * Jacquard 24 is designed on a 1290-unit em.
     * Its tables define strikethrough at 306 units (50 thick), underline at -100 units (50 thick).
     */
    @Test
    void decorationsOfTrueTypeFont_areScaledByUnitsPerEm() throws Exception {
        String path = resourcePath("/fonts/Jacquard24-Regular.ttf");
        BaseFont baseFont = BaseFont.createFont(path, IDENTITY_H, EMBEDDED);
        FontDescription description = TrueTypeUtil.extractDescription(path, baseFont, null);

        FSFontMetrics metrics = renderer.getFSFontMetrics(context, new ITextFSFont(description, 12.9f), "");

        assertThat(metrics.getStrikethroughOffset()).isCloseTo(-3.06f, within(0.001f));
        assertThat(metrics.getStrikethroughThickness()).isCloseTo(0.5f, within(0.001f));
        assertThat(metrics.getUnderlineOffset()).isCloseTo(1.0f, within(0.001f));
        assertThat(metrics.getUnderlineThickness()).isCloseTo(0.5f, within(0.001f));
    }

    @Test
    void decorationsOfType1Font() throws Exception {
        BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
        FontDescription description = new FontDescription(baseFont);

        FSFontMetrics metrics = renderer.getFSFontMetrics(context, new ITextFSFont(description, 10f), "");

        assertThat(metrics.getStrikethroughOffset()).isCloseTo(-3.115f, within(0.001f));
        assertThat(metrics.getStrikethroughThickness()).isCloseTo(1.0f, within(0.001f));
        assertThat(metrics.getUnderlineOffset()).isCloseTo(0.5f, within(0.001f));
        assertThat(metrics.getUnderlineThickness()).isCloseTo(0.5f, within(0.001f));
    }

    private String resourcePath(String name) throws URISyntaxException {
        return Path.of(requireNonNull(getClass().getResource(name)).toURI()).toString();
    }
}
