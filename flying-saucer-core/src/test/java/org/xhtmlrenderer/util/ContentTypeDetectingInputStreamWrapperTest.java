package org.xhtmlrenderer.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.xhtmlrenderer.util.ContentTypeDetectingInputStreamWrapper.detectContentType;

class ContentTypeDetectingInputStreamWrapperTest {
    @Test
    void isPdf() throws IOException {
        try (var stream = detectContentType(new ByteArrayInputStream("%PDF1234567890".getBytes(UTF_8)))) {
            assertThat(stream.isPdf()).isTrue();
            assertThat(stream.isSvg()).isFalse();
        }
    }

    @Test
    void isNotPdf() throws IOException {
        try (var stream = detectContentType(new ByteArrayInputStream("Hello, world".getBytes(UTF_8)))) {
            assertThat(stream.isPdf()).isFalse();
            assertThat(stream.isSvg()).isFalse();
        }
    }

    @Test
    void isSvg() throws IOException {
        try (var stream = detectContentType(new ByteArrayInputStream("""
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <svg><metadata id="metadata367"><rdf:RDF>
            """.getBytes(UTF_8)))) {
            assertThat(stream.isSvg()).isTrue();
            assertThat(stream.isPdf()).isFalse();
        }
    }

    @Test
    void isSvg_withoutXmlHeader() throws IOException {
        try (var stream = detectContentType(new ByteArrayInputStream("""
            <svg><metadata id="metadata367"><rdf:RDF>
            """.getBytes(UTF_8)))) {
            assertThat(stream.isSvg()).isTrue();
            assertThat(stream.isPdf()).isFalse();
        }
    }

    /**
     * Real-world regression: SVGs saved by text editors that auto-prepend a
     * UTF-8 BOM (Notepad on Windows, some Vim configs) start with the bytes
     * EF BB BF before the {@code <?xml} or {@code <svg} header. Without
     * BOM-skipping, {@link #firstBytes} captures EF BB BF 3C and the
     * byte-exact magic comparison against {@code "<?xm"} / {@code "<svg"}
     * fails — so a valid SVG fails type detection and downstream
     * SvgImage / Batik loading is skipped.
     */
    @Test
    void isSvg_withUtf8Bom() throws IOException {
        byte[] bom = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        byte[] body = "<svg><metadata/></svg>".getBytes(UTF_8);
        byte[] streamBytes = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, streamBytes, 0, bom.length);
        System.arraycopy(body, 0, streamBytes, bom.length, body.length);

        try (var stream = detectContentType(new ByteArrayInputStream(streamBytes))) {
            assertThat(stream.isSvg()).isTrue();
            assertThat(stream.isPdf()).isFalse();
        }
    }

    @Test
    void isSvg_withUtf8Bom_andXmlHeader() throws IOException {
        byte[] bom = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        byte[] body = "<?xml version=\"1.0\"?><svg/>".getBytes(UTF_8);
        byte[] streamBytes = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, streamBytes, 0, bom.length);
        System.arraycopy(body, 0, streamBytes, bom.length, body.length);

        try (var stream = detectContentType(new ByteArrayInputStream(streamBytes))) {
            assertThat(stream.isSvg()).isTrue();
            assertThat(stream.isPdf()).isFalse();
        }
    }
}