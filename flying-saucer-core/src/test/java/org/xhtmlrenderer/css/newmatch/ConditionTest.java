package org.xhtmlrenderer.css.newmatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.xhtmlrenderer.css.newmatch.Condition.NthChildCondition;
import org.xhtmlrenderer.util.IntListConverter;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xhtmlrenderer.css.newmatch.Condition.createNthChildCondition;

class ConditionTest {
    @Test
    void nthChildParsing() {
        assertThat(createNthChildCondition("33")).usingRecursiveComparison().isEqualTo(new NthChildCondition(0, 33));
        assertThat(createNthChildCondition("even")).usingRecursiveComparison().isEqualTo(new NthChildCondition(2, 0));
        assertThat(createNthChildCondition("odd")).usingRecursiveComparison().isEqualTo(new NthChildCondition(2, 1));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         11   | 11
         2    | 2
         odd  | 1, 3, 5, 7, 9, 11
         even | 0, 2, 4, 6, 8, 10
         n+4  | 4, 5, 6, 7, 8, 9, 10, 11
         4n+1 | 1, 5, 9
        """
        // TODO -n+4 | 1, 2, 3, 4   // FIXME the negative N is implemented incorrectly
    )
    void nthChildMatching(String input, @ConvertWith(IntListConverter.class) List<Integer> expectedMatchingIndices) {
        NthChildCondition condition = (NthChildCondition) createNthChildCondition(input);
        List<Integer> actual = IntStream.range(0, 12)
            .filter(index -> condition.matches(index))
            .boxed().toList();

        assertThat(actual).isEqualTo(expectedMatchingIndices);
    }
}
