package org.xhtmlrenderer.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void documentFromString_rejectsBillionLaughsEntityExpansion() {
        // Each &lolN; expands to 10 copies of the previous one, so &lol9; alone would expand to
        // 10^9 "lol"s if fully resolved. FEATURE_SECURE_PROCESSING caps entity expansion, so
        // this must be rejected instead of exhausting memory/CPU.
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE lolz [
                 <!ENTITY lol "lol">
                 <!ENTITY lol2 "&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;">
                 <!ENTITY lol3 "&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;">
                 <!ENTITY lol4 "&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;">
                 <!ENTITY lol5 "&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;">
                 <!ENTITY lol6 "&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;">
                 <!ENTITY lol7 "&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;">
                 <!ENTITY lol8 "&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;">
                 <!ENTITY lol9 "&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;">
                ]>
                <lolz>&lol9;</lolz>
                """;

        assertThatThrownBy(() -> XMLUtil.documentFromString(xml))
                .isInstanceOf(SAXException.class)
                .hasMessageContaining("entity expansions");
    }

    @Test
    void newSecureDocumentBuilderFactory_deniesExternalDtdAccessAtTheJaxpLevel() throws Exception {
        // Defense-in-depth: even without FSEntityResolver attached, the factory itself must
        // refuse to fetch an external DTD.
        DocumentBuilder builder = XMLUtil.newSecureDocumentBuilderFactory().newDocumentBuilder();

        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE root SYSTEM "http://127.0.0.1:1/unreachable.dtd">
                <root/>
                """;

        assertThatThrownBy(() -> builder.parse(new InputSource(new StringReader(xml))))
                .isInstanceOf(SAXException.class)
                .hasMessageContaining("accessExternalDTD");
    }
}
