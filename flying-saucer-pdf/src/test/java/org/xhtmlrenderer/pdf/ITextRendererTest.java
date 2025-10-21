package org.xhtmlrenderer.pdf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ITextRendererTest {
    private final ITextRenderer cut = new ITextRenderer();

    @Test
    void versionIsNullByDefault() {
        assertThat(cut.getPDFVersion()).isNull();
    }

    @Test
    void getAndSetPDFVersion() {
        cut.setPDFVersion("2.0");
        assertThat(cut.getPDFVersion()).isEqualTo("2.0");
    }

    @Test
    void canSetVersionNull() {
        cut.setPDFVersion(null);
        assertThat(cut.getPDFVersion()).isEqualTo(null);
    }

    @Test
    void cannotSetIllegalVersion() {
        assertThatThrownBy(() -> cut.setPDFVersion("0.1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("""
                Invalid PDF version character: "0.1"; use one of constants in [1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 2.0].
                """.trim());
    }

}
