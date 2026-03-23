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
        Set<String> tags = render(html).getUnsupportedTags();
        assertThat(tags).containsExactlyInAnyOrder(tag, "summary", "video");
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
        Set<String> tags = render(html).getUnsupportedTags();
        assertThat(tags).isEmpty();
    }

    @Test
    void warnsOnFlexboxDisplayValue() throws Exception {
        String html = """
            <html>
                <head><style>.box { display: flex; }</style></head>
                <body><div class="box">Hello</div></body>
            </html>
            """;
        Set<String> unsupportedCssFeatures = render(html).getCss().getUnsupportedCssFeatures();

        assertThat(unsupportedCssFeatures).containsExactly("display: flex");
    }

    @Test
    void warnsOnGridDisplayValue() throws Exception {
        String html = """
            <html>
                <head><style>.box { display: grid; }</style></head>
                <body><div class="box">Hello</div></body>
            </html>
            """;
        Set<String> unsupportedCssFeatures = render(html).getCss().getUnsupportedCssFeatures();

        assertThat(unsupportedCssFeatures).containsExactly("display: grid");
    }

    @ParameterizedTest
    @ValueSource(strings = {"transition", "animation", "transform", "box-shadow", "filter"})
    void warnsOnCss3PropertyNames(String property) throws Exception {
        String html = """
            <html>
                <head><style>.box { %s: none; } body {resize: both; display: flex}</style></head>
                <body><div class="box">Hello</div></body>
            </html>
            """.formatted(property);
        Set<String> unsupportedCssFeatures = render(html).getCss().getUnsupportedCssFeatures();

        assertThat(unsupportedCssFeatures).containsExactlyInAnyOrder(property, "resize", "display: flex");
    }

    @Test
    void noWarningForCss2Properties() throws Exception {
        String html = """
            <html>
                <head><style>.box { color: red; font-size: 14px; margin: 10px; }</style></head>
                <body><div class="box">Hello</div></body>
            </html>
            """;
        Set<String> unsupportedCssFeatures = render(html).getCss().getUnsupportedCssFeatures();

        assertThat(unsupportedCssFeatures).isEmpty();
    }

    private SharedContext render(String html) throws ParserConfigurationException, IOException, SAXException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(html.getBytes(UTF_8))) {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            Java2DRenderer renderer = new Java2DRenderer(document, 200);
            assertThat(renderer.getImage()).isNotNull();
            return renderer.getSharedContext();
        }
    }
}
