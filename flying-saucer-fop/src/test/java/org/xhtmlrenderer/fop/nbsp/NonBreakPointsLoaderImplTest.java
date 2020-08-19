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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.xhtmlrenderer.fop.nbsp.NonBreakPointsLoaderImpl;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public class NonBreakPointsLoaderImplTest {
	
	@Test (expected = IllegalArgumentException.class)
	public void nullLang() throws Exception {
		new NonBreakPointsLoaderImpl().loadNBSP(null);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void emptyLang() throws Exception {
		new NonBreakPointsLoaderImpl().loadNBSP("");
	}
	
	@Test
	public void loadExactMatch() throws Exception {
		List<String> lines = new NonBreakPointsLoaderImpl().loadNBSP("de");
		assertEquals(1, lines.size());
		assertEquals("deRuleščřžýá", lines.get(0)); // tests also UTF-8 chars
	}
	
	@Test
	public void loadNonExactMatch() throws Exception {
		List<String> lines = new NonBreakPointsLoaderImpl().loadNBSP("de_DE");
		assertEquals(1, lines.size());
		assertEquals("deRuleščřžýá", lines.get(0)); // tests also UTF-8 chars
	}
	
	@Test
	public void nonExisting() throws Exception {
		assertNull(new NonBreakPointsLoaderImpl().loadNBSP("es"));
	}
	
	@Test
	public void loadExactMatch2() throws Exception {
		List<String> lines = new NonBreakPointsLoaderImpl().loadNBSP("en_GB");
		assertEquals(2, lines.size());
		assertEquals("enGBRule1", lines.get(0));
		assertEquals("enGBRule2", lines.get(1));
	}

}
