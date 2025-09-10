/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
 * }}}
 */
package org.xhtmlrenderer.css.parser;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.css.newmatch.Selector;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.Stylesheet;

import java.io.IOException;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xhtmlrenderer.css.sheet.StylesheetInfo.Origin.USER_AGENT;

public class ParserTest {
    private static final Logger log = LoggerFactory.getLogger(ParserTest.class);
    private final String test = String.format("div { background-image: url('something') }%n");
    private final CSSErrorHandler errorHandler = (uri, message) -> log.error(message);

    @Test
    public void cssParsingPerformance() throws IOException {
        int count = 10_000;
        String longTest = test.repeat(count);
        assertThat(longTest.length()).as("Long enough input").isEqualTo(test.length() * count);

        long total = 0;
        for (int i = 0; i < 40; i++) {
            long start = System.currentTimeMillis();
            CSSParser p = new CSSParser(errorHandler);
            Stylesheet stylesheet = p.parseStylesheet(null, USER_AGENT, new StringReader(longTest));
            long end = System.currentTimeMillis();
            log.trace(" Parsing #{} took {} ms", i, end - start);
            total += (end-start);

            assertThat(stylesheet.getContents()).hasSize(count);
        }
        log.info("Average {} ms", total / 40);

        total = 0;
        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            CSSParser p = new CSSParser(errorHandler);
            Stylesheet stylesheet = p.parseStylesheet(null, USER_AGENT, new StringReader(longTest));
            long end = System.currentTimeMillis();
            log.trace("Parsing #{} took {} ms", i, end - start);
            total += (end-start);
            assertThat(stylesheet.getContents()).hasSize(count);
        }
        log.info("Average {} ms", total / 10);

        CSSParser p = new CSSParser(errorHandler);

        total = 0;
        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 10000; j++) {
                Stylesheet stylesheet = p.parseStylesheet(null, USER_AGENT, new StringReader(test));
                assertThat(stylesheet.getURI()).isNull();
                assertThat(stylesheet.getOrigin()).isEqualTo(USER_AGENT);
                assertThat(stylesheet.getContents()).hasSize(1);
            }
            long end = System.currentTimeMillis();
            log.trace("Parsing #{} took {} ms", i, end - start);
            total += (end-start);
        }
        log.info("Average {} ms", total / 10);
    }

    @Test
    public void parseCss() throws IOException {
        CSSParser p = new CSSParser(errorHandler);

        Stylesheet stylesheet = p.parseStylesheet(null, USER_AGENT, new StringReader(test));
        assertThat(stylesheet.getContents()).hasSize(1);
        Ruleset ruleset = (Ruleset) stylesheet.getContents().get(0);
        org.assertj.core.api.Assertions.assertThat(ruleset.getFSSelectors()).hasSize(1);
        assertThat(ruleset.getFSSelectors().get(0)).isInstanceOf(Selector.class);
        org.assertj.core.api.Assertions.assertThat(ruleset.getPropertyDeclarations()).hasSize(1);
        PropertyDeclaration propertyDeclaration = ruleset.getPropertyDeclarations().get(0);
        assertThat(propertyDeclaration.getPropertyName()).isEqualTo("background-image");
        assertThat(propertyDeclaration.getCSSName()).hasToString("background-image");
        assertThat(propertyDeclaration.getValue().getCssText()).isEqualTo("url('something')");
    }
}
