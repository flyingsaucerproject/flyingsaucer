package org.xhtmlrenderer.pdf;

import org.jspecify.annotations.Nullable;
import org.openpdf.text.DocumentException;
import org.xhtmlrenderer.html5.Html5NamespaceHandler;
import org.xhtmlrenderer.html5.Html5Parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * PDF renderer with HTML5 parsing support.
 *
 * <p>Extends {@link ITextRenderer} with entry points that accept raw HTML5 rather than
 * well-formed XHTML.  HTML5 is parsed by {@link Html5Parser} (backed by Jsoup), which
 * handles tag soup, missing closing tags, and automatic structure completion.
 *
 * <p>HTML5 semantic elements ({@code article}, {@code section}, {@code header},
 * {@code footer}, {@code nav}, {@code main}, {@code aside}, {@code figure},
 * {@code figcaption}, {@code mark}, {@code details}, {@code summary}, …) are rendered
 * correctly because {@link Html5NamespaceHandler} supplies the appropriate user-agent
 * CSS rules from {@code Html5NamespaceHandler.css}.
 *
 * <p>Usage:
 * <pre>{@code
 * // One-shot
 * byte[] pdf = Html5ITextRenderer.toPdf("<html><body><h1>Hello</h1></body></html>");
 *
 * // Builder style — configure fonts, encryption, etc. before rendering
 * Html5ITextRenderer r = Html5ITextRenderer.fromHtml5String(html, baseUrl);
 * r.getFontResolver().addFont("/fonts/custom.ttf", true);
 * r.layout();
 * r.createPDF(outputStream);
 * }</pre>
 */
public class Html5ITextRenderer extends ITextRenderer {

    // ── Instance API: set document ────────────────────────────────────────

    public void setDocumentFromHtml5String(String html) {
        setDocumentFromHtml5String(html, null);
    }

    public void setDocumentFromHtml5String(String html, @Nullable String baseUrl) {
        String base = baseUrl != null ? baseUrl : "";
        setDocument(new Html5Parser(StandardCharsets.UTF_8, base).parse(html), baseUrl, new Html5NamespaceHandler());
    }

    public void setDocumentFromHtml5Fragment(String bodyHtml) {
        setDocumentFromHtml5Fragment(bodyHtml, null);
    }

    public void setDocumentFromHtml5Fragment(String bodyHtml, @Nullable String baseUrl) {
        String base = baseUrl != null ? baseUrl : "";
        setDocument(new Html5Parser(StandardCharsets.UTF_8, base).parseFragment(bodyHtml), baseUrl, new Html5NamespaceHandler());
    }

    public void setDocumentFromHtml5Stream(InputStream html) throws IOException {
        setDocumentFromHtml5Stream(html, StandardCharsets.UTF_8, null);
    }

    public void setDocumentFromHtml5Stream(InputStream html, Charset charset, @Nullable String baseUrl) throws IOException {
        String base = baseUrl != null ? baseUrl : "";
        setDocument(new Html5Parser(charset, base).parse(html), baseUrl, new Html5NamespaceHandler());
    }

    public void setDocumentFromHtml5Reader(Reader html) throws IOException {
        setDocumentFromHtml5Reader(html, null);
    }

    public void setDocumentFromHtml5Reader(Reader html, @Nullable String baseUrl) throws IOException {
        String base = baseUrl != null ? baseUrl : "";
        setDocument(new Html5Parser(StandardCharsets.UTF_8, base).parse(html), baseUrl, new Html5NamespaceHandler());
    }

    public void setDocumentFromHtml5File(File file) throws IOException {
        setDocumentFromHtml5File(file, StandardCharsets.UTF_8);
    }

    public void setDocumentFromHtml5File(File file, Charset charset) throws IOException {
        String base = file.toURI().toString();
        setDocument(new Html5Parser(charset, base).parse(file), base, new Html5NamespaceHandler());
    }

    public void setDocumentFromHtml5Url(URL url) throws IOException {
        setDocumentFromHtml5Url(url, 10_000);
    }

    public void setDocumentFromHtml5Url(URL url, int timeoutMs) throws IOException {
        setDocument(new Html5Parser(StandardCharsets.UTF_8, url.toExternalForm(), timeoutMs).parse(url),
                url.toExternalForm(), new Html5NamespaceHandler());
    }

    // ── Static factories ──────────────────────────────────────────────────

    public static Html5ITextRenderer fromHtml5String(String html) {
        return fromHtml5String(html, null);
    }

    public static Html5ITextRenderer fromHtml5String(String html, @Nullable String baseUrl) {
        Html5ITextRenderer r = new Html5ITextRenderer();
        r.setDocumentFromHtml5String(html, baseUrl);
        return r;
    }

    public static Html5ITextRenderer fromHtml5Fragment(String bodyHtml) {
        Html5ITextRenderer r = new Html5ITextRenderer();
        r.setDocumentFromHtml5Fragment(bodyHtml, null);
        return r;
    }

    public static Html5ITextRenderer fromHtml5Stream(InputStream html, Charset charset, @Nullable String baseUrl) throws IOException {
        Html5ITextRenderer r = new Html5ITextRenderer();
        r.setDocumentFromHtml5Stream(html, charset, baseUrl);
        return r;
    }

    public static Html5ITextRenderer fromHtml5File(File file) throws IOException {
        Html5ITextRenderer r = new Html5ITextRenderer();
        r.setDocumentFromHtml5File(file);
        return r;
    }

    public static Html5ITextRenderer fromHtml5Url(URL url) throws IOException {
        Html5ITextRenderer r = new Html5ITextRenderer();
        r.setDocumentFromHtml5Url(url);
        return r;
    }

    // ── One-shot convenience ──────────────────────────────────────────────

    /** Renders an HTML5 string to a PDF byte array. */
    public static byte[] toPdf(String html) throws DocumentException {
        return toPdf(html, null);
    }

    /** Renders an HTML5 string to a PDF byte array, resolving relative URLs against {@code baseUrl}. */
    public static byte[] toPdf(String html, @Nullable String baseUrl) throws DocumentException {
        Html5ITextRenderer r = fromHtml5String(html, baseUrl);
        r.layout();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        r.createPDF(bos);
        return bos.toByteArray();
    }

    /** Renders an HTML5 string and writes the PDF to the given stream; closes the PDF document on completion. */
    public static void toPdf(String html, @Nullable String baseUrl, OutputStream out) throws DocumentException {
        Html5ITextRenderer r = fromHtml5String(html, baseUrl);
        r.layout();
        r.createPDF(out, true);
    }
}
