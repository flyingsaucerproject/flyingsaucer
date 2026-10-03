package org.xhtmlrenderer.simple.extend;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StyleBuilderTest {
    private final StyleBuilder handler = new StyleBuilder();

    @Test
    void isInteger() {
        assertThat(handler.isInteger("")).isFalse();
        assertThat(handler.isInteger("_")).isFalse();
        assertThat(handler.isInteger("0")).isTrue();
        assertThat(handler.isInteger("01234")).isTrue();
        assertThat(handler.isInteger("123a")).isFalse();
        assertThat(handler.isInteger("a234b")).isFalse();
    }

    @Test
    void convertToLength() {
        assertThat(handler.convertToLength("")).isEqualTo("");
        assertThat(handler.convertToLength("big")).isEqualTo("big");
        assertThat(handler.convertToLength("#eeffgg")).isEqualTo("#eeffgg");
        assertThat(handler.convertToLength("123a")).isEqualTo("123a");
        assertThat(handler.convertToLength("0")).isEqualTo("0px");
        assertThat(handler.convertToLength("123")).isEqualTo("123px");
    }
}