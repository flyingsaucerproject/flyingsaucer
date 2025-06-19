/*
 * {{{ header & license
 * Copyright (c) 2007-2025 Wisconsin Court System
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
package org.xhtmlrenderer.layout;

import com.google.errorprone.annotations.CheckReturnValue;
import org.xhtmlrenderer.css.constants.IdentValue;

import java.util.Iterator;
import java.util.List;

import static org.xhtmlrenderer.layout.CounterFunction.createCounterText;

class CountersFunction implements CssFunction {
    private final IdentValue _listStyleType;
    private final List<Integer> _counterValues;
    private final String _separator;

    CountersFunction(List<Integer> counterValues, String separator, IdentValue listStyleType) {
        _listStyleType = listStyleType;
        _counterValues = counterValues;
        _separator = separator;
    }

    @CheckReturnValue
    @Override
    public String evaluate() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Integer> i = _counterValues.iterator(); i.hasNext();) {
            Integer value = i.next();
            sb.append(createCounterText(_listStyleType, value));
            if (i.hasNext()) sb.append(_separator);
        }
        return sb.toString();
    }
}
