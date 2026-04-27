package org.xhtmlrenderer.html5;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class Html5RendererTest {

    @Test
    void rendersHtml5StringToImage() {
        String html = """
                <!DOCTYPE html>
                <html>
                <head><style>body { font-family: sans-serif; }</style></head>
                <body><h1>Hello HTML5</h1><p>Flying Saucer HTML5 renderer.</p></body>
                </html>
                """;

        BufferedImage image = Html5Java2DRenderer.renderToImage(html, 800, 600);

        assertThat(image.getWidth()).isEqualTo(800);
        assertThat(image.getHeight()).isEqualTo(600);
    }

    @Test
    void semanticElementsRenderAsBlock() {
        // These elements must render as block (not inline) via Html5NamespaceHandler.css.
        // If they rendered inline, all headings/paragraphs would collapse into a single
        // line and the image height would be much smaller.
        String html = """
                <!DOCTYPE html><html><body>
                <header><h1>Header</h1></header>
                <nav><a href="/">Home</a></nav>
                <main>
                  <article><section><p>Content paragraph here.</p></section></article>
                  <aside><p>Sidebar content.</p></aside>
                </main>
                <footer><p>Footer text.</p></footer>
                </body></html>
                """;

        BufferedImage image = Html5Java2DRenderer.renderToImage(html, 800);

        assertThat(image).isNotNull();
        // Multiple block elements stacked vertically: image must be taller than a single line.
        assertThat(image.getHeight()).isGreaterThan(50);
    }

    @Test
    void rendersHtml5Table() {
        String html = """
                <!DOCTYPE html><html><body>
                <table border="1">
                  <thead><tr><th>Name</th><th>Score</th></tr></thead>
                  <tbody>
                    <tr><td>Alice</td><td>100</td></tr>
                    <tr><td>Bob</td><td>85</td></tr>
                  </tbody>
                </table>
                </body></html>
                """;

        BufferedImage image = Html5Java2DRenderer.renderToImage(html, 600, 300);

        assertThat(image.getWidth()).isEqualTo(600);
    }

    @Test
    void rendersTagSoupWithoutCrashing() {
        String html = "<h1>Unclosed heading<p>No html/body tags<ul><li>Item";

        BufferedImage image = Html5Java2DRenderer.renderToImage(html, 600, 400);

        assertThat(image).isNotNull();
    }

    @Test
    void rendersHtml5MarkAndFigure() {
        String html = """
                <!DOCTYPE html><html><body>
                <p>The word <mark>highlighted</mark> stands out.</p>
                <figure><figcaption>A caption.</figcaption></figure>
                </body></html>
                """;

        BufferedImage image = Html5Java2DRenderer.renderToImage(html, 700, 400);

        assertThat(image.getWidth()).isEqualTo(700);
    }

    @Test
    void rendersFragmentViaParser() {
        Document doc = Html5Parser.ofFragment("<h2>Fragment heading</h2><p>Some text.</p>");

        BufferedImage image = new Html5Java2DRenderer(doc, 600, 300).getImage();

        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(600);
    }

    @Test
    void rendersFromInputStream() throws Exception {
        String html = "<html><body><h1>Stream</h1></body></html>";
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);

        Html5Java2DRenderer renderer = new Html5Java2DRenderer(
                new ByteArrayInputStream(bytes), StandardCharsets.UTF_8, "", 800, -1);
        BufferedImage image = renderer.getImage();

        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(800);
    }

    @Test
    void renderToImageAutoSize() {
        String html = "<html><body><p>Auto-sized content</p></body></html>";

        BufferedImage image = Html5Java2DRenderer.renderToImage(html, 600);

        assertThat(image.getWidth()).isEqualTo(600);
        assertThat(image.getHeight()).isGreaterThan(0);
    }

    @Test
    void hiddenElementsAreNotRendered() {
        // With Html5NamespaceHandler the hidden attribute suppresses display.
        // Render with vs without the hidden element; heights should not differ.
        String without = "<html><body><p>Visible</p></body></html>";
        String with    = "<html><body><p>Visible</p><div hidden><p>Secret</p></div></body></html>";

        BufferedImage imgWithout = Html5Java2DRenderer.renderToImage(without, 600);
        BufferedImage imgWith    = Html5Java2DRenderer.renderToImage(with, 600);

        assertThat(imgWith.getHeight()).isEqualTo(imgWithout.getHeight());
    }
}
