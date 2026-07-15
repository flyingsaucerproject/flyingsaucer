package org.xhtmlrenderer.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class XMLUtilTest {
    @Test
    void documentFromString_doesNotResolveExternalEntities(@TempDir Path tempDir) throws Exception {
        Path secretFile = tempDir.resolve("secret.txt");
        Files.writeString(secretFile, "top-secret-content");

        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE root [
                  <!ENTITY xxe SYSTEM "%s">
                ]>
                <root>&xxe; Hello</root>
                """.formatted(secretFile.toUri());

        Document document = XMLUtil.documentFromString(xml);

        assertThat(document.getDocumentElement().getTextContent()).doesNotContain("top-secret-content");
        assertThat(document.getDocumentElement().getTextContent()).isEqualTo(" Hello");
    }

    @Test
    void documentFromString_parsesRegularXml() throws Exception {
        Document document = XMLUtil.documentFromString("<root><child>hello</child></root>");

        assertThat(document.getDocumentElement().getTextContent()).isEqualTo("hello");
    }

    @Test
    void documentFromString_stillResolvesXhtmlNamedEntitiesFromLocalDtd() throws Exception {
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <html><body><p>a&nbsp;b&quot;c</p></body></html>
                """;

        Document document = XMLUtil.documentFromString(xml);

        assertThat(document.getDocumentElement().getTextContent()).isEqualTo("a b\"c");
    }
}
