package org.xhtmlrenderer.pdf;

import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.extend.Size;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class SvgImageTest {
    @Test
    void simpleShapes_canBeDrawnAsVector() {
        assertThat(svg("""
            <svg xmlns="http://www.w3.org/2000/svg" width="300" height="200">
              <polygon points="100,10 40,198 190,78 10,78 160,198" style="fill:lime;stroke:purple;stroke-width:5"/>
              <circle cx="50" cy="50" r="20" fill="red" fill-opacity="0.5" stroke-opacity="0.5"/>
              <text x="5" y="35" font-size="20">Hello</text>
            </svg>
            """).isDrawableAsVector()).isTrue();
    }

    @Test
    void fullOpacity_canBeDrawnAsVector() {
        assertThat(svg("""
            <svg xmlns="http://www.w3.org/2000/svg" width="300" height="200">
              <g opacity="1" style="opacity:1"><rect width="10" height="10" style="opacity: 100%"/></g>
            </svg>
            """).isDrawableAsVector()).isTrue();
    }

    @Test
    void gradients_areRasterized() {
        assertThat(svg("""
            <svg xmlns="http://www.w3.org/2000/svg" width="300" height="200">
              <defs><linearGradient id="g"><stop offset="0" stop-color="red"/><stop offset="1" stop-color="blue"/></linearGradient></defs>
              <rect width="300" height="200" fill="url(#g)"/>
            </svg>
            """).isDrawableAsVector()).isFalse();
        assertThat(svg("""
            <svg xmlns="http://www.w3.org/2000/svg" width="300" height="200">
              <defs><radialGradient id="g"><stop offset="0" stop-color="red"/></radialGradient></defs>
            </svg>
            """).isDrawableAsVector()).isFalse();
    }

    @Test
    void patternsFiltersAndMasks_areRasterized() {
        for (String element : new String[]{"pattern", "filter", "mask"}) {
            assertThat(svg("""
                <svg xmlns="http://www.w3.org/2000/svg" width="300" height="200">
                  <defs><%s id="x"/></defs>
                </svg>
                """.formatted(element)).isDrawableAsVector()).as(element).isFalse();
        }
    }

    @Test
    void groupOpacity_isRasterized() {
        assertThat(svg("""
            <svg xmlns="http://www.w3.org/2000/svg" width="300" height="200">
              <g opacity="0.5"><rect width="10" height="10"/></g>
            </svg>
            """).isDrawableAsVector()).isFalse();
        assertThat(svg("""
            <svg xmlns="http://www.w3.org/2000/svg" width="300" height="200">
              <rect width="10" height="10" style="fill:red; opacity: 50%"/>
            </svg>
            """).isDrawableAsVector()).isFalse();
        assertThat(svg("""
            <svg xmlns="http://www.w3.org/2000/svg" width="300" height="200">
              <style>.t { opacity: .4 }</style>
              <rect class="t" width="10" height="10"/>
            </svg>
            """).isDrawableAsVector()).isFalse();
    }

    @Test
    void invalidSvg_isRasterized() {
        assertThat(svg("<svg xmlns=\"http://www.w3.org/2000/svg\"><rect>").isDrawableAsVector()).isFalse();
    }

    @Test
    void scaledImage_keepsTheDecision() {
        assertThat(svg("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"30\" height=\"20\"/>")
            .scale(60, 40).isDrawableAsVector()).isTrue();
        assertThat(svg("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"30\" height=\"20\"><filter/></svg>")
            .scale(60, 40).isDrawableAsVector()).isFalse();
    }

    private static SvgImage svg(String svg) {
        return new SvgImage(svg.getBytes(UTF_8), new Size(300, 200), "test.svg");
    }
}
