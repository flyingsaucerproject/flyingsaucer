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
package org.xhtmlrenderer.fop;

import java.text.BreakIterator;
import java.util.Arrays;
import java.util.TreeSet;

import org.apache.fop.hyphenation.Hyphenation;
import org.apache.fop.hyphenation.HyphenationTree;
import org.apache.fop.hyphenation.Hyphenator;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.fop.nbsp.NonBreakPointsEnhancer;
import org.xhtmlrenderer.layout.breaker.BreakPoint;
import org.xhtmlrenderer.layout.breaker.BreakPointsProvider;
import org.xhtmlrenderer.layout.breaker.LineBreakingStrategy;
import org.xhtmlrenderer.layout.breaker.ListBreakPointsProvider;
import org.xhtmlrenderer.layout.breaker.UrlAwareLineBreakIterator;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public class FOPLineBreakingStrategy implements LineBreakingStrategy {
	
	private static final int SOFT_HYPHEN = '\u00AD';
	
	private TreeSet<BreakPoint> getPoints(String text, String lang, CalculatedStyle style) {
		text = new NonBreakPointsEnhancer().enhance(text, lang);
		BreakIterator breakIt = new UrlAwareLineBreakIterator();
		breakIt.setText(text);
		TreeSet<BreakPoint> points = new TreeSet<BreakPoint>();
		int p = BreakIterator.DONE;
		while ((p = breakIt.next()) != BreakIterator.DONE) {
			points.add(new BreakPoint(p));
		}
		if (style.getHyphens() == IdentValue.NONE) {
			return points;
		}
		if (style.getHyphens() == IdentValue.MANUAL) {
			int index = text.indexOf(SOFT_HYPHEN);
			while (index >= 0) {
				BreakPoint point = new BreakPoint(index);
				addHyphen(point);
				points.add(point);
			    index = text.indexOf(SOFT_HYPHEN, index + 1);
			}
			return points;
		}
		if (style.getHyphens() == IdentValue.AUTO) {
			HyphenationTree tree = Hyphenator.getFopHyphenationTree(lang);
			Hyphenation s = tree.hyphenate(text, 2, 2);
			if (s == null) return points;
			for (int i = 0; i < s.getHyphenationPoints().length; i++) {
				int position = s.getHyphenationPoints()[i];
				BreakPoint point = new BreakPoint(position);
				addHyphen(point);
				points.add(point);
			}
		}
		return points;
	}

	@Override
	public BreakPointsProvider getBreakPointsProvider(String text, String lang, CalculatedStyle style) {
		return new ListBreakPointsProvider(Arrays.asList(getPoints(text, lang, style).toArray(new BreakPoint[0])));
	}
	
	private void addHyphen(BreakPoint p) {
		p.setHyphen("\u002D");
	}
	
}
