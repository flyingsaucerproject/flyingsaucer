package org.xhtmlrenderer.html5;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class Html5ParserTest {

    private final Html5Parser parser = new Html5Parser();

    @Test
    void parsesBasicHtml5() {
        Document doc = parser.parse("<html><body><h1>Hello</h1></body></html>");

        assertThat(doc).isNotNull();
        assertThat(doc.getElementsByTagName("h1").item(0).getTextContent()).isEqualTo("Hello");
    }

    @Test
    void repairsTagSoup() {
        Document doc = parser.parse("<h1>Title<p>No closing tags");

        assertThat(doc.getElementsByTagName("h1").getLength()).isEqualTo(1);
        assertThat(doc.getElementsByTagName("p").getLength()).isEqualTo(1);
    }

    @Test
    void injectsImplicitHtmlStructure() {
        Document doc = parser.parse("<p>Just a paragraph");

        assertThat(doc.getElementsByTagName("html").getLength()).isEqualTo(1);
        assertThat(doc.getElementsByTagName("head").getLength()).isEqualTo(1);
        assertThat(doc.getElementsByTagName("body").getLength()).isEqualTo(1);
    }

    @Test
    void parsesHtml5SemanticElements() {
        String html = """
                <!DOCTYPE html>
                <html><body>
                <header><h1>Site</h1></header>
                <nav><a href="/">Home</a></nav>
                <main><article><section><p>Content</p></section></article></main>
                <aside>Sidebar</aside>
                <footer>Footer</footer>
                </body></html>
                """;

        Document doc = parser.parse(html);

        for (String tag : new String[]{"header", "nav", "main", "article", "section", "aside", "footer"}) {
            assertThat(doc.getElementsByTagName(tag).getLength())
                    .as("Expected <%s> element", tag)
                    .isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void parsesHtml5WithInlineStyles() {
        String html = """
                <!DOCTYPE html><html>
                <head><style>h1 { color: red; }</style></head>
                <body><h1>Styled</h1></body></html>
                """;

        Document doc = parser.parse(html);

        assertThat(doc.getElementsByTagName("style").getLength()).isEqualTo(1);
        assertThat(doc.getElementsByTagName("h1").item(0).getTextContent()).isEqualTo("Styled");
    }

    @Test
    void parsesHtml5Table() {
        String html = "<table><tr><th>Name</th><th>Value</th></tr><tr><td>Foo</td><td>42</td></tr></table>";

        Document doc = parser.parse(html);

        assertThat(doc.getElementsByTagName("tr").getLength()).isEqualTo(2);
        assertThat(doc.getElementsByTagName("td").getLength()).isEqualTo(2);
    }

    @Test
    void parsesInputStream() throws Exception {
        byte[] bytes = "<p>Stream content</p>".getBytes(StandardCharsets.UTF_8);
        Document doc = parser.parse(new ByteArrayInputStream(bytes));

        assertThat(doc.getElementsByTagName("p").item(0).getTextContent()).isEqualTo("Stream content");
    }

    @Test
    void parseFragmentWrapsInBodyStructure() {
        Document doc = parser.parseFragment("<p>Fragment</p><span>More</span>");

        assertThat(doc.getElementsByTagName("html").getLength()).isEqualTo(1);
        assertThat(doc.getElementsByTagName("body").getLength()).isEqualTo(1);
        assertThat(doc.getElementsByTagName("p").getLength()).isEqualTo(1);
    }

    @Test
    void withCharsetReturnsCopy() {
        Html5Parser utf16Parser = parser.withCharset(StandardCharsets.UTF_16);
        assertThat(utf16Parser).isNotSameAs(parser);
    }

    @Test
    void withBaseUriReturnsCopy() {
        Html5Parser based = parser.withBaseUri("https://example.com/");
        assertThat(based).isNotSameAs(parser);

        Document doc = based.parse("<a href='page.html'>link</a>");
        assertThat(doc).isNotNull();
    }

    @Test
    void hiddenAttributePreservedInDom() {
        Document doc = parser.parse("<p hidden>Secret</p>");

        Element p = (Element) doc.getElementsByTagName("p").item(0);
        assertThat(p.hasAttribute("hidden")).isTrue();
    }

    @Test
    void parsesMarkAndTimeElements() {
        Document doc = parser.parse("<p>See <mark>highlighted</mark> text on <time datetime='2025-01-01'>New Year</time></p>");

        assertThat(doc.getElementsByTagName("mark").item(0).getTextContent()).isEqualTo("highlighted");
        assertThat(((Element) doc.getElementsByTagName("time").item(0)).getAttribute("datetime")).isEqualTo("2025-01-01");
    }

    @Test
    void parsesDetailsAndSummary() {
        Document doc = parser.parse("<details><summary>Title</summary><p>Body</p></details>");

        assertThat(doc.getElementsByTagName("details").getLength()).isEqualTo(1);
        assertThat(doc.getElementsByTagName("summary").item(0).getTextContent()).isEqualTo("Title");
    }

    @Test
    void staticParseFragmentConvenience() {
        Document doc = Html5Parser.ofFragment("<li>Item</li>");

        NodeList items = doc.getElementsByTagName("li");
        assertThat(items.getLength()).isEqualTo(1);
        assertThat(items.item(0).getTextContent()).isEqualTo("Item");
    }
}
