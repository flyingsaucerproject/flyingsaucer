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

import static java.util.Locale.ROOT;

public class CounterFunction implements CssFunction {
    private static final String GREEK_UPPER_LETTERS="ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ";
    private static final String GREEK_LOWER_LETTERS="αβγδεζηθικλμνξοπρστυφχψω";

    private final IdentValue _listStyleType;
    private final int _counterValue;

    CounterFunction(int counterValue, IdentValue listStyleType) {
        _counterValue = counterValue;
        _listStyleType = listStyleType;
    }

    @CheckReturnValue
    @Override
    public String evaluate() {
        return createCounterText(_listStyleType, _counterValue);
    }

    public static String createCounterText(IdentValue listStyle, int listCounter) {
        if (listStyle == IdentValue.LOWER_LATIN || listStyle == IdentValue.LOWER_ALPHA) {
            return toLatin(listCounter - 1).toLowerCase(ROOT);
        } else if (listStyle == IdentValue.UPPER_LATIN || listStyle == IdentValue.UPPER_ALPHA) {
            return toLatin(listCounter - 1).toUpperCase(ROOT);
        } else if (listStyle == IdentValue.LOWER_ROMAN) {
            return toRoman(listCounter).toLowerCase(ROOT);
        } else if (listStyle == IdentValue.LOWER_GREEK) {
            return toGreekLower(listCounter - 1);
        } else if (listStyle == IdentValue.UPPER_GREEK) {
            return toGreekUpper(listCounter - 1);
        } else if (listStyle == IdentValue.UPPER_ROMAN) {
            return toRoman(listCounter).toUpperCase(ROOT);
        } else if (listStyle == IdentValue.DECIMAL_LEADING_ZERO) {
            return (listCounter >= 10 ? "" : "0") + listCounter;
        } else {
            return Integer.toString(listCounter);
        }
    }

    private static String toLatin(int zeroBasedIndex) {
        return zeroBasedIndex < 0 ? "" : toLatin(zeroBasedIndex / 26 - 1) + (char) ('A' + zeroBasedIndex % 26);
    }

    private static String toGreekUpper(int zeroBasedIndex) {
        return zeroBasedIndex < 0 ? "" : toGreekUpper(zeroBasedIndex / 24 - 1) + GREEK_UPPER_LETTERS.charAt(zeroBasedIndex % 24);
    }

    private static String toGreekLower(int zeroBasedIndex) {
        return zeroBasedIndex < 0 ? "" : toGreekLower(zeroBasedIndex / 24 - 1) + GREEK_LOWER_LETTERS.charAt(zeroBasedIndex % 24);
    }

    private static String toRoman(int val) {
        int[] ints = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] nums = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ints.length; i++) {
            int count = val / ints[i];
            sb.append(nums[i].repeat(Math.max(0, count)));
            val -= ints[i] * count;
        }
        return sb.toString();
    }
}
