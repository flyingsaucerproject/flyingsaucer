package org.xhtmlrenderer.pdf;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.extend.Size;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

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
    void groupOpacity_isRecognizedCaseInsensitively() {
        assertThat(svg("""
            <svg xmlns="http://www.w3.org/2000/svg" width="300" height="200">
              <rect width="10" height="10" style="OPACITY: 0.5"/>
            </svg>
            """).isDrawableAsVector()).isFalse();
        assertThat(svg("""
            <svg xmlns="http://www.w3.org/2000/svg" width="300" height="200">
              <style>.t { Opacity: .4 }</style>
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

    @Test
    void svgWithoutSizeAndViewBox_isDrawnInImageUnits() throws Exception {
        assertVectorMatchesRaster("""
            <svg xmlns="http://www.w3.org/2000/svg"><rect x="10" y="20" width="50" height="30"/></svg>
            """, 300, 200);
    }

    @Test
    void svgWithSizeButWithoutViewBox_preservesAspectRatio() throws Exception {
        assertVectorMatchesRaster("""
            <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"><circle cx="50" cy="50" r="40"/></svg>
            """, 300, 150);
    }

    @Test
    void svgWithViewBox_preservesAspectRatio() throws Exception {
        assertVectorMatchesRaster("""
            <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100">
              <circle cx="50" cy="50" r="40"/>
            </svg>
            """, 300, 150);
        assertVectorMatchesRaster("""
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" preserveAspectRatio="xMinYMax meet">
              <circle cx="50" cy="50" r="40"/>
            </svg>
            """, 300, 150);
    }

    @Test
    void svgWithViewBox_canBeStretched() throws Exception {
        assertVectorMatchesRaster("""
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" preserveAspectRatio="none">
              <circle cx="50" cy="50" r="40"/>
            </svg>
            """, 300, 150);
    }

    @Test
    void svgWithRequiredFeatures() throws Exception {
        assertVectorMatchesRaster("""
            <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"
                 requiredFeatures="http://www.w3.org/TR/SVG11/feature#Shape">
              <rect x="10" y="10" width="50" height="50"/>
            </svg>
            """, 100, 100);
    }

    @Test
    void svgWithPrintOnlyStyles_usesScreenMedia() throws Exception {
        assertVectorMatchesRaster("""
            <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100">
              <style>@media print { rect { display: none } }</style>
              <rect x="10" y="10" width="50" height="50"/>
            </svg>
            """, 100, 100);
    }

    private static void assertVectorMatchesRaster(String svg, int width, int height) throws IOException, TranscoderException {
        Rectangle raster = paintedBounds(rasterize(svg, width, height));
        Rectangle vector = paintedBounds(drawAsVector(svg, width, height));

        assertThat(raster).as("raster").isNotNull();
        assertThat(vector).as("vector").isNotNull();
        assertThat(vector.x).as("x").isCloseTo(raster.x, within(1));
        assertThat(vector.y).as("y").isCloseTo(raster.y, within(1));
        assertThat(vector.width).as("width").isCloseTo(raster.width, within(1));
        assertThat(vector.height).as("height").isCloseTo(raster.height, within(1));
    }

    private static BufferedImage rasterize(String svg, int width, int height) throws IOException, TranscoderException {
        PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        transcoder.transcode(new TranscoderInput(new ByteArrayInputStream(svg.getBytes(UTF_8))), new TranscoderOutput(png));
        return ImageIO.read(new ByteArrayInputStream(png.toByteArray()));
    }

    private static BufferedImage drawAsVector(String svg, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        try {
            new SvgImage(svg.getBytes(UTF_8), new Size(width, height), "test.svg").draw(g);
        } finally {
            g.dispose();
        }
        return result;
    }

    private static Rectangle paintedBounds(BufferedImage image) {
        Rectangle bounds = null;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) > 127) {
                    if (bounds == null) bounds = new Rectangle(x, y, 1, 1);
                    else bounds.add(new Rectangle(x, y, 1, 1));
                }
            }
        }
        return bounds;
    }

    private static SvgImage svg(String svg) {
        return new SvgImage(svg.getBytes(UTF_8), new Size(300, 200), "test.svg");
    }
}
