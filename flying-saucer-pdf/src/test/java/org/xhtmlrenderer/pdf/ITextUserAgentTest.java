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
    void svg_withWidthAndHeightAttributes_inCentimeters() throws IOException {
        assertThat(agent.getOriginalSvgSize("https://some.test.com", """
            <svg xmlns="http://www.w3.org/2000/svg" width="4cm" height="8cm">
            </svg>
            """.getBytes(UTF_8))).isEqualTo(new Size(151, 302));
    }

    @Test
    void svg_withWidthAndHeightAttributes_inMillimeters() throws IOException {
        assertThat(agent.getOriginalSvgSize("https://some.test.com", """
            <svg xmlns="http://www.w3.org/2000/svg" width="40mm" height="80mm">
            </svg>
            """.getBytes(UTF_8))).isEqualTo(new Size(151, 302));
    }

    @Test
    void svg_withWidthAndHeightAttributes_inQuarterMillimeters() throws IOException {
        assertThat(agent.getOriginalSvgSize("https://some.test.com", """
            <svg xmlns="http://www.w3.org/2000/svg" width="4000Q" height="8000Q">
            </svg>
            """.getBytes(UTF_8))).isEqualTo(new Size(3780, 7559));
    }

    @Test
    void svg_withWidthAndHeightAttributes_inInches() throws IOException {
        assertThat(agent.getOriginalSvgSize("https://some.test.com", """
            <svg xmlns="http://www.w3.org/2000/svg" width="4in" height="8in">
            </svg>
            """.getBytes(UTF_8))).isEqualTo(new Size(4 * 96, 8 * 96));
    }

    @Test
    void svg_withWidthAndHeightAttributes_inPicas() throws IOException {
        assertThat(agent.getOriginalSvgSize("https://some.test.com", """
            <svg xmlns="http://www.w3.org/2000/svg" width="100pc" height="200pc">
            </svg>
            """.getBytes(UTF_8))).isEqualTo(new Size(1600, 3200));
    }

    @Test
    void svg_withWidthAndHeightAttributes_inPoints() throws IOException {
        assertThat(agent.getOriginalSvgSize("https://some.test.com", """
            <svg xmlns="http://www.w3.org/2000/svg" width="100pt" height="200pt">
            </svg>
            """.getBytes(UTF_8))).isEqualTo(new Size(133, 267));
    }

    @Test
    void svg_withWidthAndHeightAttributes_inPixels() throws IOException {
        assertThat(agent.getOriginalSvgSize("https://some.test.com", """
            <svg xmlns="http://www.w3.org/2000/svg" width="4px" height="8px">
            </svg>
            """.getBytes(UTF_8))).isEqualTo(new Size(4, 8));
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