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

import java.text.BreakIterator;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import org.junit.Test;
import org.xhtmlrenderer.layout.breaker.BreakPoint;
import org.xhtmlrenderer.layout.breaker.UrlAwareLineBreakIterator;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public class NonBreakPointsTest {
	
	@Test
	public void testGeneral() throws Exception {
		test("text s mezerami", 5, 7, 15);
	}
	
	@Test
	public void testNBSP() throws Exception {
		test("text s\u00A0mezerami", 5, 15);
	}
	
	private void test(String text, int ... expected) {
		BreakIterator breakIt = new UrlAwareLineBreakIterator();
		breakIt.setText(text);
		TreeSet<BreakPoint> points = new TreeSet<BreakPoint>();
		int p = BreakIterator.DONE;
		while ((p = breakIt.next()) != BreakIterator.DONE) {
			points.add(new BreakPoint(p));
		}
		assertBreakPoints(points, expected);
	}
	
	private void assertBreakPoints(Collection<BreakPoint> calculated, int ... expected) {
		if (calculated.size() != expected.length) throw new AssertionError("Expected " + expected.length + 
				" break points, got " + calculated.size() + ": " + calculated);
		Iterator<BreakPoint> it = calculated.iterator();
		for (int point : expected) {
			assertEquals(point, it.next().getPosition());
		}
	}
	
	

}
