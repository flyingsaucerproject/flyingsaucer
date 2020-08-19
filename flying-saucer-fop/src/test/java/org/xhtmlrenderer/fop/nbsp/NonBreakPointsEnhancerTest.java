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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public class NonBreakPointsEnhancerTest {
	
	@Test
	public void testNullInput() throws Exception {
		assertNull(new NonBreakPointsEnhancer().enhance(null, "cs"));
	}
	
	@Test
	public void testNullLang() throws Exception {
		assertEquals("some input", new NonBreakPointsEnhancer().enhance("some input", null));
	}
	
	@Test
	public void testEmptyLang() throws Exception {
		assertEquals("some input", new NonBreakPointsEnhancer().enhance("some input", ""));
	}
	
	@Test
	public void testEmptyInput() throws Exception {
		assertEquals("", new NonBreakPointsEnhancer().enhance("", "en"));
	}
	
	@Test
	public void noDefinition() throws Exception {
		NonBreakPointsLoader nullLoader = new NonBreakPointsLoader() {
			
			@Override
			public List<String> loadNBSP(String lang) {
				return null;
			}
		};
		assertEquals("some text with spaces", new NonBreakPointsEnhancer(nullLoader).enhance("some text with spaces", "cs"));
	}
	
	@Test
	public void testLoaderConfiguration() throws Exception {
		final String[] c = new String[] {null};
		NonBreakPointsLoader capturingLoader = new NonBreakPointsLoader() {
			
			@Override
			public List<String> loadNBSP(String lang) {
				c[0] = lang;
				return null;
			}
		};
		new NonBreakPointsEnhancer(capturingLoader).enhance("some text with spaces", "cs");
		assertEquals("cs", c[0]);
	}
	
	@Test
	public void emptyRules() throws Exception {
		testRulesInternal("some text", "some text");
	}
	
	@Test
	public void rules1() throws Exception {
		testRulesInternal("prselo a potom kousek", "prselo a\u00A0potom kousek", "([\\s]+a)( )([^\\s]+)");
	}
	
	@Test
	public void rules2() throws Exception {
		testRulesInternal("prselo a potom se to cele stalo a sli jsme domu", "prselo a\u00A0potom se to cele stalo a\u00A0sli jsme domu", "([\\s]+a)( )([^\\s]+)");
	}
	
	@Test
	public void rules3() throws Exception {
		testRulesInternal("prselo a potom jsme sli domu s kamarady a povidali si", "prselo a\u00A0potom jsme sli domu s\u00A0kamarady a\u00A0povidali si", "([\\s]+a)( )([^\\s]+)", "([\\s]+s)( )([^\\s]+)");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void invalidRuleGroups() throws Exception {
		testRulesInternal("a potom", "a\u00A0potom", "a( )[^\\s]{1,}");
	}
	
	@Test
	public void czechRules() throws Exception {
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
		assertEquals(expected, new NonBreakPointsEnhancer().enhance(text, "cs"));
	}
	
	private void testRulesInternal(String text, String expected, final String ... rules) {
		NonBreakPointsLoader dummyLoader = new NonBreakPointsLoader() {
			
			@Override
			public List<String> loadNBSP(String lang) {
				return Arrays.asList(rules);
			}
		};
		assertEquals(expected, new NonBreakPointsEnhancer(dummyLoader).enhance(text, "en"));
	}

}
