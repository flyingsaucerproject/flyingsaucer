package org.xhtmlrenderer.fop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.breaker.BreakPoint;
import org.xhtmlrenderer.layout.breaker.BreakPointsProvider;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FOPLineBreakingStrategyTest {
    private static final String WORDS = "Nejvyšší soud rozhodl k­domu, že s ním v roce 2017 nesouhlasí. " +
            "Velmi dlouhé slovo nejneobhospodařovávatelnějšími http://example.com/a/b/c​d z okna. ";

    private final FOPLineBreakingStrategy strategy = new FOPLineBreakingStrategy();
    private final CalculatedStyle style = mock();

    enum Hyphens {
        NONE(IdentValue.NONE), MANUAL(IdentValue.MANUAL), AUTO(IdentValue.AUTO);

        private final IdentValue value;

        Hyphens(IdentValue value) {
            this.value = value;
        }
    }

    /** The break points of a text from the middle on are those of the rest of the text, though found in parts of it. */
    @ParameterizedTest
    @EnumSource(Hyphens.class)
    void breakPointsFromTheMiddleOfTheTextAreThoseOfTheRestOfIt(Hyphens hyphens) {
        when(style.getHyphens()).thenReturn(hyphens.value);
        String text = WORDS.repeat(40);

        for (int start : new int[]{0, 1, 17, 500, text.length() / 2, text.length() - 30, text.length()}) {
            assertThat(breakPoints(strategy.getBreakPointsProvider(text, start, "cs", style)))
                    .as("from %d", start)
                    .isEqualTo(breakPoints(strategy.getBreakPointsProvider(text.substring(start), "cs", style)));
        }
    }

    /**
     * A text broken into lines asks for the break points of each line from its start: they are found without
     * hyphenating all the rest of the text each time, see
     * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/757">issue 757</a>.
     */
    @Test
    @Timeout(value = 5, threadMode = SEPARATE_THREAD)
    void breakPointsOfEachLineOfLongTextTakeLinearTime() {
        when(style.getHyphens()).thenReturn(IdentValue.AUTO);
        String text = WORDS.repeat(20_000);

        for (int start = 0; start < text.length(); start += 20_000) {
            BreakPointsProvider provider = strategy.getBreakPointsProvider(text, start, "cs", style);
            for (int i = 0; i < 20; i++) {
                assertThat(provider.next().position()).isPositive();
            }
        }
    }

    private static List<BreakPoint> breakPoints(BreakPointsProvider provider) {
        List<BreakPoint> points = new ArrayList<>();
        for (BreakPoint point = provider.next(); point.position() != BreakPoint.DONE.position(); point = provider.next()) {
            points.add(point);
        }
        return points;
    }
}
