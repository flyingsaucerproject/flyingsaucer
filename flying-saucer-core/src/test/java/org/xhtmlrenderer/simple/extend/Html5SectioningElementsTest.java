package org.xhtmlrenderer.simple.extend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.resource.XMLResource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests to ensure HTML5 sectioning and related elements are treated as block-level
 * (or appropriate) by default and that this behavior is implemented via normal CSS so that
 * author styles can still override it.
 */
class Html5SectioningElementsTest {

    private CalculatedStyle styleFor(String tagName) {
        String html = "<html><body><" + tagName + ">Test</" + tagName + "></body></html>";
        return styleForHtml(html, tagName);
    }

    private CalculatedStyle styleForWithCss(String tagName, String cssRule) {
        String html = "<html><head><style>" + cssRule + "</style></head><body><" + tagName + ">Test</" + tagName + "></body></html>";
        return styleForHtml(html, tagName);
    }

    private CalculatedStyle styleForHtml(String html, String tagName) {
        XMLResource resource = XMLResource.load(new java.io.StringReader(html));
        Document doc = resource.getDocument();
        Element element = (Element) doc.getElementsByTagName(tagName).item(0);

        SharedContext context = new SharedContext();
        context.setNamespaceHandler(new XhtmlCssOnlyNamespaceHandler());
        // Set up the document context, which loads the user agent stylesheet automatically
        context.getCss().setDocumentContext(context, context.getNamespaceHandler(), doc, null);

        return context.getStyle(element);
    }

    @ParameterizedTest
    @ValueSource(strings = {"section", "article", "header", "footer"})
    void html5_sectioning_elements_are_block_by_default(String tagName) {
        CalculatedStyle style = styleFor(tagName);
        assertEquals(IdentValue.BLOCK, style.getDisplay(),
                "Expected <" + tagName + "> to be block-level by default");
    }

    @ParameterizedTest
    @ValueSource(strings = {"nav", "aside", "main", "figure", "figcaption"})
    void additional_html5_structural_elements_are_block_by_default(String tagName) {
        CalculatedStyle style = styleFor(tagName);
        assertEquals(IdentValue.BLOCK, style.getDisplay(),
                "Expected <" + tagName + "> to be block-level by default");
    }

    @ParameterizedTest
    @ValueSource(strings = {"details", "summary"})
    void html5_details_and_summary_are_block_by_default(String tagName) {
        CalculatedStyle style = styleFor(tagName);
        assertEquals(IdentValue.BLOCK, style.getDisplay(),
                "Expected <" + tagName + "> to be block-level by default");
    }

    @Test
    void mark_is_inline_by_default() {
        CalculatedStyle style = styleFor("mark");
        assertEquals(IdentValue.INLINE, style.getDisplay(),
                "Expected <mark> to be inline by default");
    }

    @Test
    void time_is_inline_by_default() {
        CalculatedStyle style = styleFor("time");
        assertEquals(IdentValue.INLINE, style.getDisplay(),
                "Expected <time> to be inline by default");
    }

    @Test
    void canvas_is_inline_block_by_default() {
        CalculatedStyle style = styleFor("canvas");
        assertEquals(IdentValue.INLINE_BLOCK, style.getDisplay(),
                "Expected <canvas> to be inline-block by default");
    }

    @ParameterizedTest
    @ValueSource(strings = {"progress", "meter", "output"})
    void html5_form_widgets_are_inline_block_by_default(String tagName) {
        CalculatedStyle style = styleFor(tagName);
        assertEquals(IdentValue.INLINE_BLOCK, style.getDisplay(),
                "Expected <" + tagName + "> to be inline-block by default");
    } 

    @Test
    void unknown_elements_remain_inline_by_default() {
        CalculatedStyle style = styleFor("foo");
        assertEquals(IdentValue.INLINE, style.getDisplay(),
                "Expected unknown elements to remain inline by default");
    }

    @Test
    void span_is_inline_by_default() {
        CalculatedStyle style = styleFor("span");
        assertEquals(IdentValue.INLINE, style.getDisplay(),
                "Expected <span> to be inline by default");
    }

    @Test
    void css_can_override_section_to_inline() {
        CalculatedStyle style = styleForWithCss("section", "section { display: inline }");
        assertEquals(IdentValue.INLINE, style.getDisplay(),
                "Author CSS should be able to override section to inline");
    }

    @Test
    void css_can_override_header_to_inline() {
        CalculatedStyle style = styleForWithCss("header", "header { display: inline }");
        assertEquals(IdentValue.INLINE, style.getDisplay(),
                "Author CSS should be able to override header to inline");
    }
}
