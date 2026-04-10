package org.xhtmlrenderer.css.parser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.extend.AttributeResolver;
import org.xhtmlrenderer.css.extend.lib.DOMTreeResolver;
import org.xhtmlrenderer.css.newmatch.Selector;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_IDENT;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_NUMBER;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PX;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_URI;
import static org.xhtmlrenderer.css.constants.CSSName.BACKGROUND_COLOR;
import static org.xhtmlrenderer.css.constants.CSSName.BACKGROUND_IMAGE;
import static org.xhtmlrenderer.css.constants.CSSName.BORDER_TOP_COLOR;
import static org.xhtmlrenderer.css.constants.CSSName.COLOR;
import static org.xhtmlrenderer.css.sheet.StylesheetInfo.Origin.AUTHOR;

class CSSParserTest {
    private final List<String> errors = new ArrayList<>();
    private final CSSParser parser = new CSSParser((uri, message) -> errors.add(message));

    @AfterEach
    void tearDown() {
        assertThat(errors).isEmpty();
    }

    @Test
    void rgba() throws IOException {
        Stylesheet stylesheet = parser.parseStylesheet(null, AUTHOR, new StringReader("""
            p {
                color: rgb(255, 165, 11);
                background-color: rgba(233, 99, 71, 0.5);
                border-top-color: rgba(-20, 255, 300, 1);
            }
            """));
        assertThat(stylesheet.getContents()).hasSize(1);
        Ruleset ruleset = (Ruleset) stylesheet.getContents().get(0);

        assertThat(ruleset.getPropertyDeclarations()).usingRecursiveComparison().isEqualTo(List.of(
            css(COLOR, new FSRGBColor(255, 165, 11)),
            css(BACKGROUND_COLOR, new FSRGBColor(233, 99, 71, 0.5f)),
            css(BORDER_TOP_COLOR, new FSRGBColor(0, 255, 255, 1.0f))
        ));
    }

    @ParameterizedTest
    @CsvSource(value = {
        " background.png                                             | /css/v1/sample.css                 | /css/v1/background.png",
        " background.png                                             | sample.css                         | background.png",
        " https://some.com/background.png                            | /css/v1/sample.css                 | https://some.com/background.png",
        " /img/background.png                                        | https://some.com/css/v1/sample.css | https://some.com/img/background.png",
        " /img/background.png                                        | /css/v1/sample.css                 | /img/background.png",
        " data:image/svg+xml;charset=utf8,%3Csvg xmlns=              | /css/v1/sample.css                 | data:image/svg+xml;charset=utf8,%3Csvg xmlns=",
        " blob:https://site.com/258186e7-a0a1-40e5-bcf8-5ac35f965454 | /css/v1/sample.css                 | blob:https://site.com/258186e7-a0a1-40e5-bcf8-5ac35f965454",
        " blob:                                                      | /css/v1/sample.css                 | blob:",
    }, delimiterString = "|")
    void imageUrl(String imageUrl, String cssUrl, String expectedFullImageUrl) throws IOException {
        String css = "div { background-image: url('%s') }".formatted(imageUrl);
        Stylesheet stylesheet = parser.parseStylesheet(cssUrl, AUTHOR, new StringReader(css));
        assertThat(stylesheet.getContents()).hasSize(1);
        assertThat(stylesheet.getContents()).hasSize(1);
        Ruleset ruleset = (Ruleset) stylesheet.getContents().get(0);

        assertThat(ruleset.getPropertyDeclarations()).usingRecursiveComparison().isEqualTo(List.of(
            new PropertyDeclaration(BACKGROUND_IMAGE, new PropertyValue(CSS_URI, expectedFullImageUrl, "url('%s')".formatted(imageUrl)), false, AUTHOR)
        ));
    }

    @Test
    void hasPseudoClassParsesAndMatches() throws Exception {
        Stylesheet stylesheet = parser.parseStylesheet(null, AUTHOR, new StringReader("""
            section:has(> p.note) { color: red; }
            article:has(+ article.highlight) { color: blue; }
            """));

        Ruleset first = (Ruleset) stylesheet.getContents().get(0);
        Ruleset second = (Ruleset) stylesheet.getContents().get(1);
        Selector firstSelector = first.getFSSelectors().get(0);
        Selector secondSelector = second.getFSSelectors().get(0);

        Document document = DocumentBuilderFactory.newDefaultInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader("""
                    <root>
                      <section id='has-child'><p class='note'>x</p></section>
                      <section id='no-child'><p>x</p></section>
                      <article id='a1'/>
                      <article id='a2' class='highlight'/>
                    </root>
                    """)));

        Element hasChild = elementById(document, "has-child");
        Element noChild = elementById(document, "no-child");
        Element a1 = elementById(document, "a1");
        Element a2 = elementById(document, "a2");

        AttributeResolver attributes = domAttributeResolver();
        DOMTreeResolver treeResolver = new DOMTreeResolver();

        assertThat(firstSelector.matches(hasChild, attributes, treeResolver)).isTrue();
        assertThat(firstSelector.matches(noChild, attributes, treeResolver)).isFalse();
        assertThat(secondSelector.matches(a1, attributes, treeResolver)).isTrue();
        assertThat(secondSelector.matches(a2, attributes, treeResolver)).isFalse();
    }

    @Test
    void parsesMultiColumnProperties() throws IOException {
        Stylesheet stylesheet = parser.parseStylesheet(null, AUTHOR, new StringReader("""
            div {
                column-count: 3;
                column-gap: 24px;
                column-width: 180px;
            }
            """));
        Ruleset ruleset = (Ruleset) stylesheet.getContents().get(0);
        List<PropertyDeclaration> declarations = ruleset.getPropertyDeclarations();

        assertThat(declarations).hasSize(3);
        assertThat(declarations.get(0).getCSSName()).isEqualTo(CSSName.COLUMN_COUNT);
        assertThat(declarations.get(0).getValue().getPrimitiveType()).isEqualTo(CSS_NUMBER);
        assertThat(declarations.get(0).getValue().getFloatValue(CSS_NUMBER)).isEqualTo(3f);

        assertThat(declarations.get(1).getCSSName()).isEqualTo(CSSName.COLUMN_GAP);
        assertThat(declarations.get(1).getValue().getPrimitiveType()).isEqualTo(CSS_PX);
        assertThat(declarations.get(1).getValue().getFloatValue(CSS_PX)).isEqualTo(24f);

        assertThat(declarations.get(2).getCSSName()).isEqualTo(CSSName.COLUMN_WIDTH);
        assertThat(declarations.get(2).getValue().getPrimitiveType()).isEqualTo(CSS_PX);
        assertThat(declarations.get(2).getValue().getFloatValue(CSS_PX)).isEqualTo(180f);
    }

    @Test
    void expandsColumnsShorthand() throws IOException {
        Stylesheet stylesheet = parser.parseStylesheet(null, AUTHOR, new StringReader("""
            div { columns: 12em 4; }
            """));
        Ruleset ruleset = (Ruleset) stylesheet.getContents().get(0);
        List<PropertyDeclaration> declarations = ruleset.getPropertyDeclarations();

        assertThat(declarations).hasSize(2);
        assertThat(declarations.get(0).getCSSName()).isEqualTo(CSSName.COLUMN_WIDTH);
        assertThat(declarations.get(1).getCSSName()).isEqualTo(CSSName.COLUMN_COUNT);
        assertThat(declarations.get(1).getValue().getPrimitiveType()).isEqualTo(CSS_NUMBER);
        assertThat(declarations.get(1).getValue().getFloatValue(CSS_NUMBER)).isEqualTo(4f);
    }

    @Test
    void expandsColumnsShorthandWithSingleValue() throws IOException {
        Stylesheet stylesheet = parser.parseStylesheet(null, AUTHOR, new StringReader("""
            div { columns: 3; }
            """));
        Ruleset ruleset = (Ruleset) stylesheet.getContents().get(0);
        List<PropertyDeclaration> declarations = ruleset.getPropertyDeclarations();

        assertThat(declarations).hasSize(2);
        assertThat(declarations.get(0).getCSSName()).isEqualTo(CSSName.COLUMN_WIDTH);
        assertThat(declarations.get(0).getValue().getPrimitiveType()).isEqualTo(CSS_IDENT);
        assertThat(declarations.get(0).getValue().getCssText()).isEqualTo("auto");
        assertThat(declarations.get(1).getCSSName()).isEqualTo(CSSName.COLUMN_COUNT);
        assertThat(declarations.get(1).getValue().getPrimitiveType()).isEqualTo(CSS_NUMBER);
        assertThat(declarations.get(1).getValue().getFloatValue(CSS_NUMBER)).isEqualTo(3f);
    }

    private static PropertyDeclaration css(CSSName property, FSRGBColor color) {
        return new PropertyDeclaration(property, new PropertyValue(color), false, AUTHOR);
    }

    private static Element elementById(Document document, String id) {
        Node node = document.getDocumentElement().getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (id.equals(element.getAttribute("id"))) {
                    return element;
                }
            }
            node = node.getNextSibling();
        }
        throw new IllegalArgumentException("Element with id '%s' not found".formatted(id));
    }

    private static AttributeResolver domAttributeResolver() {
        return new AttributeResolver() {
            @Override
            public String getAttributeValue(Node e, String attrName) {
                return e instanceof Element element && element.hasAttribute(attrName) ? element.getAttribute(attrName) : null;
            }

            @Override
            public String getAttributeValue(Node e, String namespaceURI, String attrName) {
                return getAttributeValue(e, attrName);
            }

            @Override
            public String getClass(Node e) {
                return getAttributeValue(e, "class");
            }

            @Override
            public String getID(Node e) {
                return getAttributeValue(e, "id");
            }

            @Override
            public String getNonCssStyling(Node e) {
                return null;
            }

            @Override
            public String getElementStyling(Node e) {
                return null;
            }

            @Override
            public String getLang(Node e) {
                return getAttributeValue(e, "lang");
            }

            @Override
            public boolean isLink(Node e) {
                return false;
            }

            @Override
            public boolean isVisited(Node e) {
                return false;
            }

            @Override
            public boolean isHover(Node e) {
                return false;
            }

            @Override
            public boolean isActive(Node e) {
                return false;
            }

            @Override
            public boolean isFocus(Node e) {
                return false;
            }
        };
    }
}
