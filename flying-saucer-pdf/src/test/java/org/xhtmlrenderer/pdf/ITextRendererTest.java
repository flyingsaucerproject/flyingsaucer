package org.xhtmlrenderer.pdf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ITextRendererTest {

    @Test
    void getAndSetPDFVersion() {

        var cut = new ITextRenderer();

        assertThat(cut.getPDFVersion()).isNull();

        cut.setPDFVersion("2.0");
        assertThat(cut.getPDFVersion()).isEqualTo("2.0");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> cut.setPDFVersion("0.1"));

        assertThat(thrown.getMessage()).isEqualTo("Invalid PDF version character: \"0.1\"; use one of constants in [1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 2.0].");

        cut.setPDFVersion(null);
        assertThat(cut.getPDFVersion()).isEqualTo(null);
    }
}