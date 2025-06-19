package org.xhtmlrenderer.layout;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xhtmlrenderer.css.constants.IdentValue.LOWER_LATIN;
import static org.xhtmlrenderer.css.constants.IdentValue.UPPER_ROMAN;

class CountersFunctionTest {
    @Test
    void upperRoman() {
        CountersFunction function = new CountersFunction(List.of(2, 4, 6, 8), " | ", UPPER_ROMAN);
        assertThat(function.evaluate()).isEqualTo("II | IV | VI | VIII");
    }

    @Test
    void lowerLatin() {
        CountersFunction function = new CountersFunction(List.of(3, 6, 9, 12), " / ", LOWER_LATIN);
        assertThat(function.evaluate()).isEqualTo("c / f / i / l");
    }
}