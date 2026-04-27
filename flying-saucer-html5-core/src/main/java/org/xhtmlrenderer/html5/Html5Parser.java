package org.xhtmlrenderer.html5;

import org.htmlunit.cyberneko.html.dom.HTMLDocumentImpl;
import org.htmlunit.cyberneko.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Parses HTML5 content using htmlunit-neko and returns a W3C {@link Document}
 * suitable for use with the Flying Saucer rendering pipeline.
 *
 * <p>The neko parser handles tag soup, optional closing tags, and automatic
 * structure completion (implicit {@code <html>/<head>/<body>}). Element and
 * attribute names are normalised to lowercase for compatibility with Flying
 * Saucer's layout engine.
 *
 * <p>Usage:
 * <pre>{@code
 * Document doc = new Html5Parser().parse("<h1>Hello</h1>");
 * Document doc = Html5Parser.fromUrl("https://example.com/");
 * Document doc = Html5Parser.ofFragment("<p>snippet</p>");
 * }</pre>
 */
public class Html5Parser {
    private static final String NAMES_ELEMS = "http://cyberneko.org/html/properties/names/elems";
    private static final String NAMES_ATTRS = "http://cyberneko.org/html/properties/names/attrs";

    private final Charset charset;
    private final String baseUri;
    private final int urlTimeoutMs;

    public Html5Parser() {
        this(StandardCharsets.UTF_8, "", 10_000);
    }

    public Html5Parser(Charset charset, String baseUri) {
        this(charset, baseUri, 10_000);
    }

    public Html5Parser(Charset charset, String baseUri, int urlTimeoutMs) {
        this.charset = charset;
        this.baseUri = baseUri;
        this.urlTimeoutMs = urlTimeoutMs;
    }

    /** Returns a copy of this parser with the given charset. */
    public Html5Parser withCharset(Charset newCharset) {
        return new Html5Parser(newCharset, baseUri, urlTimeoutMs);
    }

    /** Returns a copy of this parser with the given base URI. */
    public Html5Parser withBaseUri(String newBaseUri) {
        return new Html5Parser(charset, newBaseUri, urlTimeoutMs);
    }

    /** Returns a copy of this parser with the given URL connection timeout (ms). */
    public Html5Parser withUrlTimeout(int timeoutMs) {
        return new Html5Parser(charset, baseUri, timeoutMs);
    }

    // ── Full-document parsing ────────────────────────────────────────────

    /** Parses an HTML5 string as a full document. */
    public Document parse(String html) {
        try {
            InputSource source = new InputSource(new StringReader(html));
            source.setSystemId(systemId());
            return doParse(source);
        } catch (IOException e) {
            throw new RuntimeException(e); // won't happen with StringReader
        }
    }

    /** Parses HTML5 from a {@link Reader}. The reader is not closed. */
    public Document parse(Reader reader) throws IOException {
        InputSource source = new InputSource(reader);
        source.setSystemId(systemId());
        return doParse(source);
    }

    /**
     * Parses an HTML5 byte stream. Charset is auto-detected from BOM or
     * {@code <meta charset>}; falls back to this parser's {@code charset} field.
     */
    public Document parse(InputStream inputStream) throws IOException {
        InputSource source = new InputSource(inputStream);
        source.setSystemId(systemId());
        source.setEncoding(charset.name());
        return doParse(source);
    }

    /**
     * Parses an HTML5 file. Charset is auto-detected from BOM or
     * {@code <meta charset>}; falls back to this parser's {@code charset} field.
     * Uses the file's path as base URI when no explicit base URI is set.
     */
    public Document parse(File file) throws IOException {
        String base = baseUri.isEmpty() ? file.toURI().toString() : baseUri;
        try (InputStream stream = new FileInputStream(file)) {
            InputSource source = new InputSource(stream);
            source.setSystemId(base);
            source.setEncoding(charset.name());
            return doParse(source);
        }
    }

    /**
     * Fetches and parses the HTML5 document at the given URL, using this parser's
     * timeout and charset settings.
     */
    public Document parse(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(urlTimeoutMs);
        connection.setReadTimeout(urlTimeoutMs);
        try (InputStream stream = connection.getInputStream()) {
            InputSource source = new InputSource(stream);
            source.setSystemId(url.toString());
            source.setEncoding(charset.name());
            return doParse(source);
        }
    }

    // ── Fragment parsing ─────────────────────────────────────────────────

    /**
     * Parses an HTML5 body fragment — content that would appear inside {@code <body>}.
     * The returned document has the standard {@code html/head/body} structure with the
     * fragment placed inside {@code <body>}.
     */
    public Document parseFragment(String bodyHtml) {
        return parse("<html><head></head><body>" + bodyHtml + "</body></html>");
    }

    // ── Static convenience factories ─────────────────────────────────────

    /** Fetches and parses the HTML5 document at the given URL string with default settings. */
    public static Document fromUrl(String url) throws IOException {
        return new Html5Parser().parse(new URL(url));
    }

    /** Parses an HTML5 body fragment with default settings. */
    public static Document ofFragment(String bodyHtml) {
        return new Html5Parser().parseFragment(bodyHtml);
    }

    // ── Internal ─────────────────────────────────────────────────────────

    private String systemId() {
        return baseUri.isEmpty() ? null : baseUri;
    }

    private static Document doParse(InputSource source) throws IOException {
        try {
            DOMParser parser = new DOMParser(HTMLDocumentImpl.class);
            parser.setProperty(NAMES_ELEMS, "lower");
            parser.setProperty(NAMES_ATTRS, "lower");
            parser.parse(source);
            return parser.getDocument();
        } catch (SAXException e) {
            throw new IOException("Failed to parse HTML", e);
        }
    }
}
