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

import org.jspecify.annotations.Nullable;

import java.text.BreakIterator;
import java.util.Comparator;

import static java.util.Objects.requireNonNullElse;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public record BreakPoint(int position, String hyphen) implements Comparable<BreakPoint> {
    public static final BreakPoint DONE = new BreakPoint(BreakIterator.DONE);

    public BreakPoint(int position) {
        this(position, null);
    }

    public BreakPoint(int position, @Nullable String hyphen) {
        this.position = position;
        this.hyphen = requireNonNullElse(hyphen, "");
    }

    @Override
    public int compareTo(BreakPoint o) {
        return Comparator.<BreakPoint>
            comparingInt(x -> x.position)
            .thenComparing(x -> x.hyphen)
            .compare(this, o);
    }
}
