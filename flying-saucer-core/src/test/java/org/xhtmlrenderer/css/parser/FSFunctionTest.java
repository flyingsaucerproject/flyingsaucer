package org.xhtmlrenderer.css.parser;

import org.junit.jupiter.api.Test;
import org.w3c.dom.css.CSSPrimitiveValue;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class FSFunctionTest {
    private final PropertyValue parameter = new PropertyValue(CSSPrimitiveValue.CSS_IDENT, 0.0f, "header");
    private final FSFunction function = new FSFunction("element", List.of(parameter));

    @Test
    void is() {
        assertThat(function.is("element")).isTrue();
        assertThat(function.is("counter")).isFalse();
    }

    @Test
    void getName() {
        assertThat(function.getName()).isEqualTo("element");
    }

    @Test
    void getParameters() {
        assertThat(function.getParameters()).containsExactly(parameter);
        assertThat(new FSFunction("element", emptyList()).getParameters()).isEmpty();
    }

    @Test
    void stringRepresentation() {
        assertThat(function).hasToString("element(header,)");
    }
}
