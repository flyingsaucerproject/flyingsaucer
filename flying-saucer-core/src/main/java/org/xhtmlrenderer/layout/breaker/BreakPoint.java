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
import java.util.Objects;

import static java.util.Objects.requireNonNullElse;

/**
 * @author Lukas Zaruba, lukas.zaruba@gmail.com
 */
public class BreakPoint implements Comparable<BreakPoint> {

    private final int position;
    private final String hyphen;

    public BreakPoint(int position) {
        this(position, null);
    }

    public BreakPoint(int position, @Nullable String hyphen) {
        this.position = position;
        this.hyphen = requireNonNullElse(hyphen, "");
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "BreakPoint [position=" + position + "]";
    }

    @Override
    public int compareTo(BreakPoint o) {
        return Comparator.<BreakPoint>
            comparingInt(x -> x.position)
            .thenComparing(x -> x.hyphen)
            .compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BreakPoint other &&
            Objects.equals(this.position, other.position) &&
            Objects.equals(this.hyphen, other.hyphen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, hyphen);
    }

    public String getHyphen() {
        return hyphen;
    }

    public static BreakPoint getDonePoint() {
        return new BreakPoint(BreakIterator.DONE);
    }

}
