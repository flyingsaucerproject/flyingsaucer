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

import org.apache.fop.hyphenation.Hyphenation;
import org.apache.fop.hyphenation.HyphenationTree;
import org.apache.fop.hyphenation.Hyphenator;
import org.jspecify.annotations.Nullable;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.fop.nbsp.NonBreakPointsEnhancer;
import org.xhtmlrenderer.layout.breaker.BreakPoint;
import org.xhtmlrenderer.layout.breaker.BreakPointsProvider;
import org.xhtmlrenderer.layout.breaker.LineBreakingStrategy;
import org.xhtmlrenderer.layout.breaker.ListBreakPointsProvider;
import org.xhtmlrenderer.layout.breaker.UrlAwareLineBreakIterator;

import java.text.BreakIterator;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Word wrapping capability
 *
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public class FOPLineBreakingStrategy implements LineBreakingStrategy {

    private static final int SOFT_HYPHEN = '\u00AD';

    private Set<BreakPoint> getPoints(String text, String lang, CalculatedStyle style) {
        text = new NonBreakPointsEnhancer().enhance(text, lang);
        BreakIterator breakIt = new UrlAwareLineBreakIterator(text);
        Set<BreakPoint> points = new TreeSet<>();
        int p;
        while ((p = breakIt.next()) != BreakIterator.DONE) {
            points.add(new BreakPoint(p));
        }
        if (style.getHyphens() == IdentValue.NONE) {
            return points;
        }
        if (style.getHyphens() == IdentValue.MANUAL) {
            int index = text.indexOf(SOFT_HYPHEN);
            while (index >= 0) {
                BreakPoint point = new BreakPoint(index, "-");
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
                BreakPoint point = new BreakPoint(position, "-");
                points.add(point);
            }
        }
        return points;
    }

    @Override
    public BreakPointsProvider getBreakPointsProvider(String text, String lang, CalculatedStyle style) {
        return new ListBreakPointsProvider(List.of(getPoints(text, lang, style).toArray(new BreakPoint[0])));
    }

    /**
     * The break points of {@code text} from {@code start} on, found in a window of the text after {@code start} which
     * grows (512 characters, then twice as many each time) only as far as the break points are asked for. A text is
     * broken into lines one at a time, each from the start of the rest of the text, and the break points of a line are
     * asked for only a little past its end: finding them in all the rest of the text, hyphenated, each time would take
     * time quadratic in its length (#757).
     */
    @Override
    public BreakPointsProvider getBreakPointsProvider(String text, int start, String lang, CalculatedStyle style) {
        return new WindowedBreakPointsProvider(text, start, lang, style);
    }

    /**
     * Gives the break points found in a window of the text only where they are as in the whole text: before the last
     * whitespace of the window, and in its first half. Where a break point is depends on the text around it only (the
     * word it is in, the words next to it for the rules of {@link NonBreakPointsEnhancer}), so the text after the
     * window would not change these.
     */
    private final class WindowedBreakPointsProvider implements BreakPointsProvider {
        private static final int FIRST_WINDOW = 512;

        private final String text;
        private final int start;
        private final String lang;
        private final CalculatedStyle style;
        private int windowEnd;
        private Iterator<BreakPoint> points = Collections.emptyIterator();
        @Nullable
        private BreakPoint last;

        private WindowedBreakPointsProvider(String text, int start, String lang, CalculatedStyle style) {
            this.text = text;
            this.start = start;
            this.lang = lang;
            this.style = style;
            this.windowEnd = start;
        }

        @Override
        public BreakPoint next() {
            while (!points.hasNext()) {
                if (windowEnd == text.length()) {
                    return BreakPoint.DONE;
                }
                widen();
            }
            last = points.next();
            return last;
        }

        private void widen() {
            windowEnd = (int) Math.min(text.length(), start + Math.max(FIRST_WINDOW, 2L * (windowEnd - start)));
            String window = text.substring(start, windowEnd);
            int safeLength = windowEnd == text.length() ? Integer.MAX_VALUE : safeLength(window);
            BreakPoint given = last;
            points = getPoints(window, lang, style).stream()
                    .filter(point -> point.position() < safeLength)
                    .filter(point -> given == null || point.compareTo(given) > 0)
                    .iterator();
        }

        private static int safeLength(String window) {
            int lastWhitespace = window.length() - 1;
            while (lastWhitespace > 0 && !Character.isWhitespace(window.charAt(lastWhitespace))) {
                lastWhitespace--;
            }
            return Math.min(Math.max(lastWhitespace, 0), window.length() / 2);
        }
    }
}
