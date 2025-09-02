package org.xhtmlrenderer.css.parser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.Stylesheet;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
            css(BORDER_TOP_COLOR, new FSRGBColor(0, 255, 255, 1f))
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

    private static PropertyDeclaration css(CSSName property, FSRGBColor color) {
        return new PropertyDeclaration(property, new PropertyValue(color), false, AUTHOR);
    }
}
