package org.xhtmlrenderer.chrome;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

import static java.util.Objects.requireNonNull;

public final class Html2Pdf {
    private static final ChromiumPdfRenderer SHARED = new ChromiumPdfRenderer();

    private Html2Pdf() {
    }

    public static byte[] fromUrl(URL html) {
        try {
            return SHARED.renderToPdf(html);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to render PDF from " + html, e);
        }
    }

    public static byte[] fromFile(File html) {
        try {
            return SHARED.renderToPdf(html);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to render PDF from " + html, e);
        }
    }

    public static byte[] fromHtml(String html) {
        try {
            return SHARED.renderFromHtml(html);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to render PDF from inline HTML", e);
        }
    }

    public static byte[] fromClasspathResource(String resourcePath) {
        URL url = requireNonNull(Thread.currentThread().getContextClassLoader().getResource(resourcePath),
                () -> "Resource not found in classpath: " + resourcePath);
        return fromUrl(url);
    }
}
