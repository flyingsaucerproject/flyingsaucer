package org.xhtmlrenderer.layout;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static java.awt.Color.BLACK;
import static java.awt.Color.BLUE;
import static java.awt.Color.RED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.xhtmlrenderer.swing.Java2DRenderer.htmlAsImage;

/**
 * See <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/500">issue 500</a>
 */
class DetailsSummaryTest {
    private static final String HTML = """
            <html>
              <head><style>
                body { margin: 0; background-color: white; }
                summary { background-color: blue; color: black; height: 20px; padding-left: 4px; }
                .content { background-color: red; height: 50px; }
              </style></head>
              <body>
                <details%s>
                  <summary>Toggle</summary>
                  Some text
                  <div class="content">Hidden content</div>
                </details>
              </body>
            </html>""";

    @Test
    void closedDetailsShowsOnlySummary() throws Exception {
        BufferedImage image = htmlAsImage(HTML.formatted(""), 200);

        assertThat(image.getHeight()).isLessThan(50);
        assertThat(countPixels(image, RED.getRGB())).isZero();
    }

    @Test
    void openDetailsShowsContent() throws Exception {
        BufferedImage image = htmlAsImage(HTML.formatted(" open=\"open\""), 200);

        assertThat(countPixels(image, RED.getRGB())).isPositive();
    }

    @Test
    void summaryIsBlockSpanningWholeWidth() throws Exception {
        BufferedImage image = htmlAsImage(HTML.formatted(""), 200);

        assertThat(image.getRGB(195, 10)).isEqualTo(BLUE.getRGB());
    }

    @Test
    void summaryHasDisclosureMarker() throws Exception {
        BufferedImage closed = htmlAsImage(HTML.formatted(""), 200);
        BufferedImage open = htmlAsImage(HTML.formatted(" open=\"open\""), 200);

        BufferedImage closedMarker = closed.getSubimage(0, 0, 18, 20);
        BufferedImage openMarker = open.getSubimage(0, 0, 18, 20);
        assertThat(countPixels(closedMarker, BLACK.getRGB())).isPositive();
        assertThat(countPixels(openMarker, BLACK.getRGB())).isPositive();
        assertThat(pixels(closedMarker)).isNotEqualTo(pixels(openMarker));
    }

    private static int[] pixels(BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    private static int countPixels(BufferedImage image, int rgb) {
        int count = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) == rgb) {
                    count++;
                }
            }
        }
        return count;
    }
}
