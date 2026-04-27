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
}