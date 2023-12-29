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

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public class NonBreakPointsLoaderImplTest {

    @Test
    public void nullLang() {
        assertThatThrownBy(() -> new NonBreakPointsLoaderImpl().loadNBSP(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Lang must be filled to search for file!");
    }

    @Test
    public void emptyLang() {
        assertThatThrownBy(() -> new NonBreakPointsLoaderImpl().loadNBSP(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Lang must be filled to search for file!");
    }

    @Test
    public void loadExactMatch() {
        List<String> lines = new NonBreakPointsLoaderImpl().loadNBSP("de");
        assertEquals(1, lines.size());
        assertEquals("deRuleščřžýá", lines.get(0)); // tests also UTF-8 chars
    }

    @Test
    public void loadNonExactMatch() {
        List<String> lines = new NonBreakPointsLoaderImpl().loadNBSP("de_DE");
        assertEquals(1, lines.size());
        assertEquals("deRuleščřžýá", lines.get(0)); // tests also UTF-8 chars
    }

    @Test
    public void nonExisting() {
        assertThat(new NonBreakPointsLoaderImpl().loadNBSP("es")).isEmpty();
    }

    @Test
    public void loadExactMatch2() {
        List<String> lines = new NonBreakPointsLoaderImpl().loadNBSP("en_GB");
        assertEquals(2, lines.size());
        assertEquals("enGBRule1", lines.get(0));
        assertEquals("enGBRule2", lines.get(1));
    }

}
