package org.xhtmlrenderer.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class BoxBuilderHtml5WarningTest {

    @ParameterizedTest
    @ValueSource(strings = {"header", "footer", "nav", "article", "section", "aside", "main"})
    void warnsWhenHtml5SemanticElementEncountered(String tag) throws Exception {
        String html = """
            <html>
                <body>
                    <%s>content</%s>
                    <summary>foo</summary>
                    <video/>
                </body>
            </html>
            """.formatted(tag, tag);
        Set<String> log = renderAndCaptureLog(html);
        assertThat(log).containsExactlyInAnyOrder(tag, "summary", "video");
    }

    @Test
    void noWarningForHtml4Tags() throws Exception {
        String html = """
            <html>
                <body>
                    <p>Hello</p>
                    <div>World</div>
                    <table><tr><td>cell</td></tr></table>
                </body>
            </html>
            """;
        Set<String> log = renderAndCaptureLog(html);
        assertThat(log).doesNotContain("not supported by FlyingSaucer");
    }

    private Set<String> renderAndCaptureLog(String html) throws ParserConfigurationException, IOException, SAXException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(html.getBytes(UTF_8)));
        Java2DRenderer renderer = new Java2DRenderer(document, 200);
        assertThat(renderer.getImage()).isNotNull();
        return renderer.getSharedContext().getUnsupportedTags();
    }
}
