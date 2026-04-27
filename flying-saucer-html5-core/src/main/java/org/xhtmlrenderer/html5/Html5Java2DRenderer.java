package org.xhtmlrenderer.html5;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.swing.Java2DRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * {@link Java2DRenderer} variant that parses HTML5 input via {@link Html5Parser} and renders
 * it with {@link Html5NamespaceHandler}, so that HTML5 semantic elements (article, section,
 * header, footer, nav, main, aside, figure, figcaption, mark, …) are displayed correctly.
 *
 * <p>All constructors accept HTML5 strings, streams, files, or URLs in addition to the
 * pre-parsed W3C {@link Document} path inherited from {@code Java2DRenderer}.
 *
 * <p>Usage:
 * <pre>{@code
 * BufferedImage img = Html5Java2DRenderer.renderToImage("<h1>Hello</h1>", 800);
 *
 * // or builder style:
 * Html5Java2DRenderer r = new Html5Java2DRenderer("<article><h1>Hi</h1></article>", 800);
 * BufferedImage img = r.getImage();
 * }</pre>
 */
public class Html5Java2DRenderer extends Java2DRenderer {

    // ── Constructors: HTML5 string ──────────────────────────────────────

    public Html5Java2DRenderer(String html, int width) {
        this(html, width, -1);
    }

    public Html5Java2DRenderer(String html, int width, int height) {
        this(html, "", width, height);
    }

    public Html5Java2DRenderer(String html, String baseUri, int width, int height) {
        super(new Html5Parser(StandardCharsets.UTF_8, baseUri).parse(html), baseUri, width, height);
    }

    // ── Constructors: InputStream ───────────────────────────────────────

    public Html5Java2DRenderer(InputStream html, int width) throws IOException {
        this(html, StandardCharsets.UTF_8, "", width, -1);
    }

    public Html5Java2DRenderer(InputStream html, Charset charset, String baseUri, int width, int height) throws IOException {
        super(new Html5Parser(charset, baseUri).parse(html), baseUri, width, height);
    }

    // ── Constructors: File ──────────────────────────────────────────────

    public Html5Java2DRenderer(File file, int width) throws IOException {
        this(file, StandardCharsets.UTF_8, width);
    }

    public Html5Java2DRenderer(File file, Charset charset, int width) throws IOException {
        super(new Html5Parser(charset, file.toURI().toString()).parse(file),
              file.toURI().toString(), width, -1);
    }

    // ── Constructors: pre-parsed Document ──────────────────────────────

    public Html5Java2DRenderer(Document doc, int width) {
        super(doc, width);
    }

    public Html5Java2DRenderer(Document doc, int width, int height) {
        super(doc, width, height);
    }

    public Html5Java2DRenderer(Document doc, @Nullable String baseUri, int width, int height) {
        super(doc, baseUri != null ? baseUri : "", width, height);
    }

    // ── Namespace handler override ──────────────────────────────────────

    @Override
    protected NamespaceHandler createNamespaceHandler() {
        return new Html5NamespaceHandler();
    }

    // ── Static convenience ──────────────────────────────────────────────

    /** Parses and renders {@code html} to a {@code width×height} image. */
    public static BufferedImage renderToImage(String html, int width, int height) {
        return new Html5Java2DRenderer(html, width, height).getImage();
    }

    /** Parses and renders {@code html} to a {@code width}-wide image; height is auto-sized. */
    public static BufferedImage renderToImage(String html, int width) {
        return new Html5Java2DRenderer(html, width).getImage();
    }
}
