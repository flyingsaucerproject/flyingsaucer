package org.xhtmlrenderer.css.parser.property;

import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.CSSParseException;
import org.xhtmlrenderer.css.parser.FSFunction;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.Origin;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_DEG;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_NUMBER;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PX;
import static org.xhtmlrenderer.css.constants.CSSName.cssProperty;
import static org.xhtmlrenderer.css.constants.IdentValue.NONE;
import static org.xhtmlrenderer.css.parser.PropertyValue.Type.VALUE_TYPE_FUNCTION;
import static org.xhtmlrenderer.css.parser.PropertyValue.Type.VALUE_TYPE_LIST;

class TransformPropertyBuilderTest {
    private final TransformPropertyBuilder builder = new TransformPropertyBuilder();
    private final CSSName cssName = cssProperty("transform");

    private static PropertyValue function(String name, PropertyValue... args) {
        return new PropertyValue(new FSFunction(name, List.of(args)));
    }

    private static PropertyValue px(float value) {
        return new PropertyValue(CSS_PX, value, value + "px");
    }

    private static PropertyValue deg(float value) {
        return new PropertyValue(CSS_DEG, value, value + "deg");
    }

    private static PropertyValue number(float value) {
        return new PropertyValue(CSS_NUMBER, value, String.valueOf(value));
    }

    @Test
    void none() {
        PropertyValue none = new PropertyValue(NONE);

        List<PropertyDeclaration> result = builder.buildDeclarations(cssName, List.of(none), Origin.AUTHOR, false, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValue()).isSameAs(none);
    }

    @Test
    void singleFunction() {
        PropertyValue rotate = function("rotate", deg(45));

        List<PropertyDeclaration> result = builder.buildDeclarations(cssName, List.of(rotate), Origin.AUTHOR, false, true);

        assertThat(result).hasSize(1);
        PropertyValue value = (PropertyValue) result.get(0).getValue();
        assertThat(value.getPropertyValueType()).isEqualTo(VALUE_TYPE_LIST);

        List<PropertyValue> functions = value.getValues();
        assertThat(functions).hasSize(1);
        assertThat(functions.get(0).getPropertyValueType()).isEqualTo(VALUE_TYPE_FUNCTION);
        assertThat(functions.get(0).getFunction().getName()).isEqualTo("rotate");
    }

    @Test
    void multipleFunctionsKeepOrder() {
        PropertyValue translate = function("translate", px(10), px(20));
        PropertyValue rotate = function("rotate", deg(45));

        List<PropertyDeclaration> result = builder.buildDeclarations(
                cssName, List.of(translate, rotate), Origin.AUTHOR, false, true);

        List<PropertyValue> functions = ((PropertyValue) result.get(0).getValue()).getValues();
        assertThat(functions.stream().map(v -> v.getFunction().getName())).containsExactly("translate", "rotate");
    }

    @Test
    void rejectsUnknownFunction() {
        PropertyValue unknown = function("rotate3d", deg(45));

        assertThatThrownBy(() -> builder.buildDeclarations(cssName, List.of(unknown), Origin.AUTHOR, false, true))
                .isInstanceOf(CSSParseException.class)
                .hasMessageContaining("rotate3d");
    }

    @Test
    void rejectsWrongArgumentCountForMatrix() {
        PropertyValue matrix = function("matrix", number(1), number(0), number(0), number(1), number(0));

        assertThatThrownBy(() -> builder.buildDeclarations(cssName, List.of(matrix), Origin.AUTHOR, false, true))
                .isInstanceOf(CSSParseException.class)
                .hasMessageStartingWith("matrix() requires between 6 and 6 argument(s)");
    }

    @Test
    void rejectsNonAngleArgumentForRotate() {
        PropertyValue rotate = function("rotate", px(45));

        assertThatThrownBy(() -> builder.buildDeclarations(cssName, List.of(rotate), Origin.AUTHOR, false, true))
                .isInstanceOf(CSSParseException.class)
                .hasMessageContaining("rotate()");
    }

    @Test
    void scaleAcceptsOneOrTwoNumbers() {
        PropertyValue scale = function("scale", number(2));

        List<PropertyDeclaration> result = builder.buildDeclarations(cssName, List.of(scale), Origin.AUTHOR, false, true);

        assertThat(result).hasSize(1);
    }
}
