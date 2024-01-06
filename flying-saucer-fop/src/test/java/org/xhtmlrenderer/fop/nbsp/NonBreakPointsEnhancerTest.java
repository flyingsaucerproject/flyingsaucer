/*
 * Copyright (C) 2017 Lukas Zaruba, lukas.zaruba@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.fop.nbsp;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public class NonBreakPointsEnhancerTest {

    @Test
    public void nullInput() {
        assertThat(new NonBreakPointsEnhancer().enhance(null, "cs")).isNull();
    }

    @Test
    public void nullLang() {
        assertThat(new NonBreakPointsEnhancer().enhance("some input", null)).isEqualTo("some input");
    }

    @Test
    public void emptyLang() {
        assertThat(new NonBreakPointsEnhancer().enhance("some input", "")).isEqualTo("some input");
    }

    @Test
    public void emptyInput() {
        assertThat(new NonBreakPointsEnhancer().enhance("", "en")).isEqualTo("");
    }

    @Test
    public void noDefinition() {
        NonBreakPointsLoader nullLoader = lang -> emptyList();
        assertThat(new NonBreakPointsEnhancer(nullLoader).enhance("some text with spaces", "cs")).isEqualTo("some text with spaces");
    }

    @Test
    public void loaderConfiguration() {
        final String[] c = {null};
        NonBreakPointsLoader capturingLoader = lang -> {
            c[0] = lang;
            return emptyList();
        };
        new NonBreakPointsEnhancer(capturingLoader).enhance("some text with spaces", "cs");
        assertThat(c[0]).isEqualTo("cs");
    }

    @Test
    public void emptyRules() {
        testRulesInternal("some text", "some text");
    }

    @Test
    public void rules1() {
        testRulesInternal("prselo a potom kousek", "prselo a\u00A0potom kousek", "([\\s]+a)( )([^\\s]+)");
    }

    @Test
    public void rules2() {
        testRulesInternal("prselo a potom se to cele stalo a sli jsme domu", "prselo a\u00A0potom se to cele stalo a\u00A0sli jsme domu", "([\\s]+a)( )([^\\s]+)");
    }

    @Test
    public void rules3() {
        testRulesInternal("prselo a potom jsme sli domu s kamarady a povidali si", "prselo a\u00A0potom jsme sli domu s\u00A0kamarady a\u00A0povidali si", "([\\s]+a)( )([^\\s]+)", "([\\s]+s)( )([^\\s]+)");
    }

    @Test
    public void invalidRuleGroups() {
        assertThatThrownBy(() -> testRulesInternal("a potom", "a\u00A0potom", "a( )[^\\s]{1,}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expression must contain exactly 3 groups! a( )[^\\s]{1,}");
    }

    @Test
    public void czechRules() {
        assertCzech("byli jsme u babicky", "byli jsme u\u00A0babicky");
        assertCzech("byli jsme k babicky", "byli jsme k\u00A0babicky");
        assertCzech("byli jsme s babicky", "byli jsme s\u00A0babicky");
        assertCzech("byli jsme v babicky", "byli jsme v\u00A0babicky");
        assertCzech("byli jsme z babicky", "byli jsme z\u00A0babicky");
        assertCzech("byli jsme o babicky", "byli jsme o\u00A0babicky");
        assertCzech("byli jsme u babicky", "byli jsme u\u00A0babicky");
        assertCzech("byli jsme a babicky", "byli jsme a\u00A0babicky");
        assertCzech("byli jsme i babicky", "byli jsme i\u00A0babicky");
        assertCzech("50 %", "50\u00A0%");
        assertCzech("§ 50", "§\u00A050");
        assertCzech("# 50", "#\u00A050");
        assertCzech("* 50", "*\u00A050");
        assertCzech("† 50", "†\u00A050");
        assertCzech("50 000", "50\u00A0000");
        assertCzech("50 000 000", "50\u00A0000\u00A0000");
        assertCzech("+420 800 123 987", "+420\u00A0800\u00A0123\u00A0987");
        assertCzech(" 21. 3. 2017", " 21.\u00A03. 2017");
    }

    private void assertCzech(String text, String expected) {
        assertThat(new NonBreakPointsEnhancer().enhance(text, "cs")).isEqualTo(expected);
    }

    private void testRulesInternal(String text, String expected, final String ... rules) {
        NonBreakPointsLoader dummyLoader = lang -> Arrays.asList(rules);
        assertThat(new NonBreakPointsEnhancer(dummyLoader).enhance(text, "en")).isEqualTo(expected);
    }

}
