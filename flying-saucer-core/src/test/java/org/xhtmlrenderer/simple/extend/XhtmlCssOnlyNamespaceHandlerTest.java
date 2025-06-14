package org.xhtmlrenderer.simple.extend;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.xhtmlrenderer.css.sheet.StylesheetInfo.Origin.AUTHOR;
import static org.xhtmlrenderer.css.sheet.StylesheetInfo.Origin.USER_AGENT;
import static org.xhtmlrenderer.simple.extend.XhtmlCssOnlyNamespaceHandler.collapseWhiteSpace;

class XhtmlCssOnlyNamespaceHandlerTest {
    private final XhtmlCssOnlyNamespaceHandler handler = new XhtmlCssOnlyNamespaceHandler();

    @Test
    void readsAllCssStyles() throws IOException, ParserConfigurationException, SAXException {
        List<StylesheetInfo> stylesheets = handler.getStylesheets(read("/hello.css.html"));
        assertThat(stylesheets).hasSize(2);
        assertThat(stylesheets.get(0).getUri()).matches("inline:\\d+");
        assertThat(stylesheets.get(0).getMedia()).containsExactly("all");
        assertThat(stylesheets.get(0).getOrigin()).isEqualTo(AUTHOR);
        assertThat(stylesheets.get(0).getContent()).contains("body {color: black;}");
        assertThat(stylesheets.get(1).getContent()).contains("h1 {color: red;}");
    }

    @Test
    void fileWithoutCssStyles() throws IOException, ParserConfigurationException, SAXException {
        List<StylesheetInfo> stylesheets = handler.getStylesheets(read("/hello.html"));
        assertThat(stylesheets).isEmpty();
    }

    @Test
    void readsDefaultCssStylesheetFromFile() {
        StylesheetInfo css = handler.getDefaultStylesheet().get();
        assertThat(css.getUri()).endsWith("/css/XhtmlNamespaceHandler.css");
        assertThat(css.getOrigin()).isEqualTo(USER_AGENT);
        assertThat(css.getMedia()).containsExactly("all");
    }

    @Test
    void collapseWhiteSpace_samples() {
        assertThat(collapseWhiteSpace("")).isEqualTo("");
        assertThat(collapseWhiteSpace(" ")).isEqualTo(" ");
        assertThat(collapseWhiteSpace("     ")).isEqualTo(" ");
        assertThat(collapseWhiteSpace(" a  \t  b  \n  c  \r  d  \u000B  e    \f   f  ")).isEqualTo(" a b c d e f ");
        assertThat(collapseWhiteSpace("| \t  \n |  \r \u000B \u001E \f    |  \u001E   \u001F   |")).isEqualTo("| | | |");
    }

    private Document read(String name) throws IOException, SAXException, ParserConfigurationException {
        URL url = requireNonNull(getClass().getResource(name), () -> "test resource not found: " + name);
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(url.toString());
    }
}