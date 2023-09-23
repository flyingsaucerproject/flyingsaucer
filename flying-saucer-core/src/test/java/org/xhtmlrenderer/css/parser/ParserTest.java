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

import junit.framework.TestCase;
import org.junit.Assert;
import org.xhtmlrenderer.css.newmatch.Selector;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.Stylesheet;

import java.io.IOException;
import java.io.StringReader;

public class ParserTest extends TestCase {
    private final String test = String.format("div { background-image: url('something') }%n");
    private final CSSErrorHandler errorHandler = (uri, message) -> System.out.println(message);

    public void test_cssParsingPerformance() throws IOException {
        int count = 10_000;
        StringBuilder longTest = new StringBuilder();
        for (int i = 0 ; i < count; i++) {
            longTest.append(test);
        }
        Assert.assertEquals("Long enough input", test.length() * count, longTest.length());
        
        long total = 0;
        for (int i = 0; i < 40; i++) {
            long start = System.currentTimeMillis();
            CSSParser p = new CSSParser(errorHandler);
            Stylesheet stylesheet = p.parseStylesheet(null, 0, new StringReader(longTest.toString()));
            long end = System.currentTimeMillis();
            // System.out.println("Took " + (end-start) + " ms");
            total += (end-start);

            assertEquals(count, stylesheet.getContents().size());
        }
        System.out.println("Average " + (total/10) + " ms");

        total = 0;
        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            CSSParser p = new CSSParser(errorHandler);
            Stylesheet stylesheet = p.parseStylesheet(null, 0, new StringReader(longTest.toString()));
            long end = System.currentTimeMillis();
            // System.out.println("Took " + (end-start) + " ms");
            total += (end-start);
            assertEquals(count, stylesheet.getContents().size());
        }
        System.out.println("Average " + (total/10) + " ms");

        CSSParser p = new CSSParser(errorHandler);

        total = 0;
        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 10000; j++) {
                p.parseStylesheet(null, 0, new StringReader(test));
            }
            long end = System.currentTimeMillis();
            // System.out.println("Took " + (end-start) + " ms");
            total += (end-start);
        }
        System.out.println("Average " + (total/10) + " ms");
    }

    public void test_parseCss() throws IOException {
        CSSParser p = new CSSParser(errorHandler);
        
        Stylesheet stylesheet = p.parseStylesheet(null, 0, new StringReader(test));
        assertEquals(1, stylesheet.getContents().size());
        Ruleset ruleset = (Ruleset) stylesheet.getContents().get(0);
        assertEquals(1, ruleset.getFSSelectors().size());
        assertEquals(Selector.class, ruleset.getFSSelectors().get(0).getClass());
        assertEquals(1, ruleset.getPropertyDeclarations().size());
        PropertyDeclaration propertyDeclaration = (PropertyDeclaration) ruleset.getPropertyDeclarations().get(0);
        assertEquals("background-image", propertyDeclaration.getPropertyName());
        assertEquals("background-image", propertyDeclaration.getCSSName().toString());
        assertEquals("url('something')", propertyDeclaration.getValue().getCssText());
    }
}
