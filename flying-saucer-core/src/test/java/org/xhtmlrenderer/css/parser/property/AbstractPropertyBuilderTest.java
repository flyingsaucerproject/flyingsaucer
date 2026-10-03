package org.xhtmlrenderer.css.parser.property;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xhtmlrenderer.css.parser.PropertyValue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_CM;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_DEG;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_EMS;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_EXS;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_IN;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_MM;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_NUMBER;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PC;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PERCENTAGE;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PT;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PX;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_STRING;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_UNKNOWN;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_URI;

class AbstractPropertyBuilderTest {
    private final AbstractPropertyBuilder builder = spy();

    @ParameterizedTest
    @ValueSource(shorts = {CSS_EMS, CSS_EXS, CSS_PX, CSS_IN, CSS_CM, CSS_MM, CSS_PT, CSS_PC})
    void lengthCssTypes(short type) {
        assertThat(builder.isLength(new PropertyValue(type, 123.45f, "?")))
            .isTrue();
    }

    @Test
    void cssNumber() {
        assertThat(builder.isLength(new PropertyValue(CSS_NUMBER, 0f, "?"))).isTrue();
        assertThat(builder.isLength(new PropertyValue(CSS_NUMBER, 0.0f, "?"))).isTrue();
        assertThat(builder.isLength(new PropertyValue(CSS_NUMBER, 123.45f, "?"))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(shorts = {CSS_UNKNOWN, CSS_PERCENTAGE, CSS_DEG, CSS_STRING, CSS_URI})
    void otherCssTypes(short type) {
        assertThat(builder.isLength(new PropertyValue(type, 123.45f, "?")))
            .isFalse();
    }
}