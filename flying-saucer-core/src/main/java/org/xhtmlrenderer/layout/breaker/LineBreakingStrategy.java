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
package org.xhtmlrenderer.layout.breaker;

import org.xhtmlrenderer.css.style.CalculatedStyle;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public interface LineBreakingStrategy {

    BreakPointsProvider getBreakPointsProvider(String text, String lang, CalculatedStyle style);

    /**
     * The break points of {@code text} from {@code start} on, at offsets from {@code start}: those of
     * {@code text.substring(start)}. A text is broken into lines one at a time, each from the start of the rest of
     * the text, so a strategy which finds the break points without copying the rest of the text saves time quadratic
     * in the length of a long text.
     */
    default BreakPointsProvider getBreakPointsProvider(String text, int start, String lang, CalculatedStyle style) {
        return getBreakPointsProvider(text.substring(start), lang, style);
    }

}
