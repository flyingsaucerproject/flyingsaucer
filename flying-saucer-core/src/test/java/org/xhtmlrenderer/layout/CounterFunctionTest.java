package org.xhtmlrenderer.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.util.ConstantConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xhtmlrenderer.css.constants.IdentValue.DECIMAL_LEADING_ZERO;
import static org.xhtmlrenderer.css.constants.IdentValue.LOWER_ROMAN;
import static org.xhtmlrenderer.css.constants.IdentValue.UPPER_ROMAN;
import static org.xhtmlrenderer.layout.CounterFunction.createCounterText;

class CounterFunctionTest {
    @ParameterizedTest
    @CsvSource({"LOWER_ALPHA", "LOWER_LATIN"})
    void lowerLatin(@ConvertWith(IdentValueConverter.class) IdentValue style) {
        assertThat(createCounterText(style, 0)).isEqualTo("");
        assertThat(createCounterText(style, 1)).isEqualTo("a");
        assertThat(createCounterText(style, 2)).isEqualTo("b");
        assertThat(createCounterText(style, 5)).isEqualTo("e");
        assertThat(createCounterText(style, 25)).isEqualTo("y");
        assertThat(createCounterText(style, 26)).isEqualTo("z");
        assertThat(createCounterText(style, 27)).isEqualTo("aa");
        assertThat(createCounterText(style, 28)).isEqualTo("ab");
        assertThat(createCounterText(style, 26*26 - 1)).isEqualTo("yy");
    }

    @ParameterizedTest
    @CsvSource({"UPPER_ALPHA", "UPPER_LATIN"})
    void upperLatin(@ConvertWith(IdentValueConverter.class) IdentValue style) {
        assertThat(createCounterText(style, 1)).isEqualTo("A");
        assertThat(createCounterText(style, 7)).isEqualTo("G");
        assertThat(createCounterText(style, 26)).isEqualTo("Z");
        assertThat(createCounterText(style, 27)).isEqualTo("AA");
        assertThat(createCounterText(style, 28)).isEqualTo("AB");
        assertThat(createCounterText(style, 27 * 2)).isEqualTo("BB");
        assertThat(createCounterText(style, 27 * 3)).isEqualTo("CC");
        assertThat(createCounterText(style, 26 * 27)).isEqualTo("ZZ");
    }

    @Test
    void lowerRoman() {
        assertThat(createCounterText(LOWER_ROMAN, 0)).isEqualTo("");
        assertThat(createCounterText(LOWER_ROMAN, 1)).isEqualTo("i");
        assertThat(createCounterText(LOWER_ROMAN, 2)).isEqualTo("ii");
        assertThat(createCounterText(LOWER_ROMAN, 5)).isEqualTo("v");
        assertThat(createCounterText(LOWER_ROMAN, 10)).isEqualTo("x");
        assertThat(createCounterText(LOWER_ROMAN, 49)).isEqualTo("xlix");
        assertThat(createCounterText(LOWER_ROMAN, 50)).isEqualTo("l");
        assertThat(createCounterText(LOWER_ROMAN, 51)).isEqualTo("li");
        assertThat(createCounterText(LOWER_ROMAN, 99)).isEqualTo("xcix");
        assertThat(createCounterText(LOWER_ROMAN, 100)).isEqualTo("c");
        assertThat(createCounterText(LOWER_ROMAN, 104)).isEqualTo("civ");
        assertThat(createCounterText(LOWER_ROMAN, 998)).as("CM = 900, XC = 90, VIII = 8").isEqualTo("cmxcviii");
        assertThat(createCounterText(LOWER_ROMAN, 1003)).isEqualTo("miii");
    }

    @Test
    void upperRoman() {
        assertThat(createCounterText(UPPER_ROMAN, 1)).isEqualTo("I");
        assertThat(createCounterText(UPPER_ROMAN, 4)).isEqualTo("IV");
        assertThat(createCounterText(UPPER_ROMAN, 6)).isEqualTo("VI");
        assertThat(createCounterText(UPPER_ROMAN, 47)).isEqualTo("XLVII");
        assertThat(createCounterText(UPPER_ROMAN, 48)).as("XL = 40, VIII = 8").isEqualTo("XLVIII");
        assertThat(createCounterText(UPPER_ROMAN, 49)).as("XL = 40, IX = 9").isEqualTo("XLIX");
        assertThat(createCounterText(UPPER_ROMAN, 50)).isEqualTo("L");
        assertThat(createCounterText(UPPER_ROMAN, 99)).as("XC = 90, IX = 9").isEqualTo("XCIX");
        assertThat(createCounterText(UPPER_ROMAN, 101)).isEqualTo("CI");
        assertThat(createCounterText(UPPER_ROMAN, 500)).isEqualTo("D");
        assertThat(createCounterText(UPPER_ROMAN, 1002)).isEqualTo("MII");
    }

    @Test
    void decimalLeadingZero() {
        assertThat(createCounterText(DECIMAL_LEADING_ZERO, 1)).isEqualTo("01");
        assertThat(createCounterText(DECIMAL_LEADING_ZERO, 2)).isEqualTo("02");
        assertThat(createCounterText(DECIMAL_LEADING_ZERO, 9)).isEqualTo("09");
        assertThat(createCounterText(DECIMAL_LEADING_ZERO, 10)).isEqualTo("10");
        assertThat(createCounterText(DECIMAL_LEADING_ZERO, 11)).isEqualTo("11");
        assertThat(createCounterText(DECIMAL_LEADING_ZERO, 99)).isEqualTo("99");
        assertThat(createCounterText(DECIMAL_LEADING_ZERO, 100)).isEqualTo("100");
        assertThat(createCounterText(DECIMAL_LEADING_ZERO, 1002)).isEqualTo("1002");
    }

    @ParameterizedTest
    @CsvSource({"DECIMAL", "DISC", "CIRCLE", "SQUARE"})
    void defaultStyleIsDisk(@ConvertWith(IdentValueConverter.class) IdentValue style) {
        assertThat(createCounterText(style, 1)).isEqualTo("1");
        assertThat(createCounterText(style, 7)).isEqualTo("7");
        assertThat(createCounterText(style, 8888)).isEqualTo("8888");
    }

    private static class IdentValueConverter extends ConstantConverter<IdentValue> {
    }
}