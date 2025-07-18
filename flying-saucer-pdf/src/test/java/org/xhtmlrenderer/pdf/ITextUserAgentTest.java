package org.xhtmlrenderer.pdf;

import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.extend.Size;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class ITextUserAgentTest {
    private final ITextUserAgent agent = new ITextUserAgent(new ITextOutputDevice(1.0f), 1);

    @Test
    void svg_withWidthAndHeightAttributes() throws IOException {
        assertThat(agent.getOriginalSvgSize("https://some.test.com", """
            <svg xmlns="http://www.w3.org/2000/svg" width="300" height="200">
            </svg>
            """.getBytes(UTF_8))).isEqualTo(new Size(300, 200));
    }

    @Test
    void svg_withViewBoxAttribute() throws IOException {
        assertThat(agent.getOriginalSvgSize("https://some.test.com", """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 500">
            </svg>
            """.getBytes(UTF_8))).isEqualTo(new Size(600, 500));
    }

    @Test
    void svg_withoutSpecifiedSize() throws IOException {
        assertThat(agent.getOriginalSvgSize("https://some.test.com", """
            <svg xmlns="http://www.w3.org/2000/svg">
            </svg>
            """.getBytes(UTF_8))).isEqualTo(new Size(300, 150));
    }
}