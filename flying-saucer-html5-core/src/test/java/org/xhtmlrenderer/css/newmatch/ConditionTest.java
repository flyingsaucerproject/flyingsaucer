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
        assertThat(createNthChildCondition("22n+33")).usingRecursiveComparison().isEqualTo(new NthChildCondition(22, 33));
        assertThat(createNthChildCondition("-5n")).usingRecursiveComparison().isEqualTo(new NthChildCondition(-5, 0));
        assertThat(createNthChildCondition("-5n+7")).usingRecursiveComparison().isEqualTo(new NthChildCondition(-5, 7));
        assertThat(createNthChildCondition("-5n-2")).usingRecursiveComparison().isEqualTo(new NthChildCondition(-5, -2));
        assertThat(createNthChildCondition("+5n-1")).usingRecursiveComparison().isEqualTo(new NthChildCondition(5, -1));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         1    | 1                         | the 1st element
         2    | 2                         | the 2nd element
         10   | 10                        | the 10th element
         11   | 11                        | the 11th element
         odd  | 1, 3, 5, 7, 9, 11         | odd elements (2*n+1)
         even | 2, 4, 6, 8, 10            | even elements (2*n)
         5n   | 5, 10                     | power of 5
         n+4  | 4, 5, 6, 7, 8, 9, 10, 11  | the fourth and all following elements
         4n+1 | 1, 5, 9                   | 4*0+1, 4*1+1, 4*2+1
         n+3 | 3, 4, 5, 6, 7, 8, 9, 10, 11                   | TODO
         -n+3 | 1, 2, 3                   | the first three list items
         -n+4 | 1, 2, 3, 4                | the first four list items
         -3n+2| 2                         | only 2-3*0
         -3n+7| 1, 4, 7                   | 7-3*2, 7-3*1, 7-3*0
        """
    )
    void nthChildMatching(String input, @ConvertWith(IntListConverter.class) List<Integer> expectedMatchingIndices, String description) {
        NthChildCondition condition = (NthChildCondition) createNthChildCondition(input);
        List<Integer> actual = IntStream.range(1, 12)
            .filter(index -> condition.matches(index))
            .boxed().toList();

        assertThat(actual).as(description).isEqualTo(expectedMatchingIndices);
    }
}
