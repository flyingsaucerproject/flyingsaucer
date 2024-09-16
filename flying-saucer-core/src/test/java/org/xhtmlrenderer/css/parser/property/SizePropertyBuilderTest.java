package org.xhtmlrenderer.css.parser.property;

import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.CSSParseException;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_IDENT;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_MM;
import static org.xhtmlrenderer.css.constants.CSSName.getByPropertyName;
import static org.xhtmlrenderer.css.constants.IdentValue.AUTO;

class SizePropertyBuilderTest {
    private final SizePropertyBuilder builder = new SizePropertyBuilder();
    private final PropertyValue width = new PropertyValue(CSS_MM, 0, "43mm");
    private final PropertyValue height = new PropertyValue(CSS_MM, 0, "25mm");
    private final PropertyValue landscape = new PropertyValue(CSS_IDENT, 0, "landscape");
    private final CSSName cssName = getByPropertyName("size");

    @Test
    void buildDeclarationsFromOneValue() {
        List<PropertyDeclaration> result = builder.buildDeclarations(cssName, List.of(width), 6, false);

        assertThat(result).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new PropertyDeclaration(getByPropertyName("-fs-page-orientation"), new PropertyValue(AUTO), false, 6),
                new PropertyDeclaration(getByPropertyName("-fs-page-width"), width, false, 6),
                new PropertyDeclaration(getByPropertyName("-fs-page-height"), width, false, 6)
        );
    }

    @Test
    void buildDeclarationsFromTwoValues() {
        List<PropertyDeclaration> result = builder.buildDeclarations(cssName, List.of(width, height), 6, false);

        assertThat(result).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new PropertyDeclaration(getByPropertyName("-fs-page-orientation"), new PropertyValue(AUTO), false, 6),
                new PropertyDeclaration(getByPropertyName("-fs-page-width"), width, false, 6),
                new PropertyDeclaration(getByPropertyName("-fs-page-height"), height, false, 6)
        );
    }

    @Test
    void buildDeclarationsFromThreeValues() {
        List<PropertyDeclaration> result = builder.buildDeclarations(cssName, List.of(width, height, landscape), 0, false);

        assertThat(result).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new PropertyDeclaration(getByPropertyName("-fs-page-width"), width, false, 0),
                new PropertyDeclaration(getByPropertyName("-fs-page-height"), height, false, 0),
                new PropertyDeclaration(getByPropertyName("-fs-page-orientation"), landscape, false, 0)
        );
    }

    @Test
    void declarationMustHaveAtLeastOneValue() {
        assertThatThrownBy(() -> builder.buildDeclarations(cssName, emptyList(), 6, false))
                .isInstanceOf(CSSParseException.class)
                .hasMessageStartingWith("Found 0 values for size");
    }

    @Test
    void declarationMustHaveAtMostThreeValue() {
        assertThatThrownBy(() -> builder.buildDeclarations(cssName, List.of(width, height, landscape, landscape), 6, false))
                .isInstanceOf(CSSParseException.class)
                .hasMessageStartingWith("Found 4 values for size");
    }
}