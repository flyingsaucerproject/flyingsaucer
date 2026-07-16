package org.xhtmlrenderer.css.parser.property;

import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.CSSParseException;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.Origin;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_IDENT;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PERCENTAGE;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PX;
import static org.xhtmlrenderer.css.constants.CSSName.cssProperty;

class TransformOriginPropertyBuilderTest {
    private final TransformOriginPropertyBuilder builder = new TransformOriginPropertyBuilder();
    private final CSSName cssName = cssProperty("transform-origin");

    private static PropertyValue ident(String name) {
        return new PropertyValue(CSS_IDENT, name, name);
    }

    private static PropertyValue px(float value) {
        return new PropertyValue(CSS_PX, value, value + "px");
    }

    private List<PropertyValue> valuesOf(List<PropertyDeclaration> result) {
        return ((PropertyValue) result.get(0).getValue()).getValues();
    }

    @Test
    void singleLengthDefaultsVerticalToCenter() {
        List<PropertyDeclaration> result = builder.buildDeclarations(cssName, List.of(px(10)), Origin.AUTHOR, false, true);

        List<PropertyValue> values = valuesOf(result);
        assertThat(values.get(0).getFloatValue()).isEqualTo(10f);
        assertThat(values.get(1).getPrimitiveType()).isEqualTo(CSS_PERCENTAGE);
        assertThat(values.get(1).getFloatValue()).isEqualTo(50f);
    }

    @Test
    void keywordsResolveToPercentages() {
        List<PropertyDeclaration> result = builder.buildDeclarations(
                cssName, List.of(ident("right"), ident("bottom")), Origin.AUTHOR, false, true);

        List<PropertyValue> values = valuesOf(result);
        assertThat(values.get(0).getFloatValue()).isEqualTo(100f);
        assertThat(values.get(1).getFloatValue()).isEqualTo(100f);
    }

    @Test
    void leftAndTopKeywordsResolveToPercent0() {
        List<PropertyDeclaration> result = builder.buildDeclarations(
                cssName, List.of(ident("left"), ident("top")), Origin.AUTHOR, false, true);

        List<PropertyValue> values = valuesOf(result);
        assertThat(values.get(0).getFloatValue()).isEqualTo(0f);
        assertThat(values.get(1).getFloatValue()).isEqualTo(0f);
    }

    @Test
    void centerKeywordResolvesToPercent50() {
        List<PropertyDeclaration> result = builder.buildDeclarations(
                cssName, List.of(ident("center")), Origin.AUTHOR, false, true);

        List<PropertyValue> values = valuesOf(result);
        assertThat(values.get(0).getFloatValue()).isEqualTo(50f);
        assertThat(values.get(1).getFloatValue()).isEqualTo(50f);
    }

    @Test
    void lengthAndPercentageCombination() {
        List<PropertyDeclaration> result = builder.buildDeclarations(
                cssName, List.of(px(5), new PropertyValue(CSS_PERCENTAGE, 25f, "25%")), Origin.AUTHOR, false, true);

        List<PropertyValue> values = valuesOf(result);
        assertThat(values.get(0).getFloatValue()).isEqualTo(5f);
        assertThat(values.get(1).getFloatValue()).isEqualTo(25f);
    }

    @Test
    void singleVerticalKeywordImpliesCenteredHorizontalAxis() {
        List<PropertyDeclaration> result = builder.buildDeclarations(
                cssName, List.of(ident("top")), Origin.AUTHOR, false, true);

        List<PropertyValue> values = valuesOf(result);
        assertThat(values.get(0).getFloatValue()).isEqualTo(50f); // horizontal: center
        assertThat(values.get(1).getFloatValue()).isEqualTo(0f);  // vertical: top
    }

    @Test
    void keywordPairsCanAppearInEitherOrder() {
        List<PropertyDeclaration> topLeft = builder.buildDeclarations(
                cssName, List.of(ident("top"), ident("left")), Origin.AUTHOR, false, true);
        List<PropertyValue> topLeftValues = valuesOf(topLeft);
        assertThat(topLeftValues.get(0).getFloatValue()).isEqualTo(0f);  // horizontal: left
        assertThat(topLeftValues.get(1).getFloatValue()).isEqualTo(0f); // vertical: top

        // "right" can only be horizontal, so "center" is pushed onto the (otherwise unspecified) vertical axis.
        List<PropertyDeclaration> centerRight = builder.buildDeclarations(
                cssName, List.of(ident("center"), ident("right")), Origin.AUTHOR, false, true);
        List<PropertyValue> centerRightValues = valuesOf(centerRight);
        assertThat(centerRightValues.get(0).getFloatValue()).isEqualTo(100f); // horizontal: right
        assertThat(centerRightValues.get(1).getFloatValue()).isEqualTo(50f); // vertical: center
    }

    @Test
    void lengthCanBePairedWithAKeywordOnTheOtherAxis() {
        List<PropertyDeclaration> lengthThenTop = builder.buildDeclarations(
                cssName, List.of(px(10), ident("top")), Origin.AUTHOR, false, true);
        List<PropertyValue> lengthThenTopValues = valuesOf(lengthThenTop);
        assertThat(lengthThenTopValues.get(0).getFloatValue()).isEqualTo(10f);
        assertThat(lengthThenTopValues.get(1).getFloatValue()).isEqualTo(0f);

        List<PropertyDeclaration> leftThenLength = builder.buildDeclarations(
                cssName, List.of(ident("left"), px(20)), Origin.AUTHOR, false, true);
        List<PropertyValue> leftThenLengthValues = valuesOf(leftThenLength);
        assertThat(leftThenLengthValues.get(0).getFloatValue()).isEqualTo(0f);
        assertThat(leftThenLengthValues.get(1).getFloatValue()).isEqualTo(20f);
    }

    @Test
    void rejectsTwoHorizontalOnlyKeywords() {
        assertThatThrownBy(() -> builder.buildDeclarations(
                cssName, List.of(ident("left"), ident("right")), Origin.AUTHOR, false, true))
                .isInstanceOf(CSSParseException.class)
                .hasMessageContaining("Invalid combination");
    }

    @Test
    void rejectsAVerticalKeywordPairedWithALength() {
        assertThatThrownBy(() -> builder.buildDeclarations(
                cssName, List.of(ident("top"), px(10)), Origin.AUTHOR, false, true))
                .isInstanceOf(CSSParseException.class)
                .hasMessageContaining("Invalid combination");
    }

    @Test
    void rejectsMoreThanTwoValues() {
        assertThatThrownBy(() -> builder.buildDeclarations(
                cssName, List.of(px(1), px(2), px(3)), Origin.AUTHOR, false, true))
                .isInstanceOf(CSSParseException.class)
                .hasMessageStartingWith("Found 3 values for transform-origin");
    }
}
