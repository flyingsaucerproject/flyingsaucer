/*
 * {{{ header & license
 * ValueConstants.java
 * Copyright (c) 2004, 2005 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.css.constants;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.XRLog;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Utility class for working with {@code CSSValue} instances.
 */
public final class ValueConstants {
    private static final Map<Short, String> sacTypesStrings = new HashMap<>(25);

    public static String stringForSACPrimitiveType(short type) {
        return sacTypesStrings.get(type);
    }

    /**
     * Returns true if the specified type absolute (even if we have a computed
     * value for it), meaning that either the value can be used directly (e.g.
     * pixels) or there is a fixed context-independent conversion for it (e.g.
     * inches). Proportional types (e.g. %) return false.
     *
     * @param type The CSSValue type to check.
     */
    public static boolean isAbsoluteUnit(short type) {
        // TODO: check this list...

        // note, all types are included here to make sure none are missed
        return switch (type) {

            // proportional length or size
            case CSSPrimitiveValue.CSS_PERCENTAGE -> false;

            // refer to values known to the DerivedValue instance (tobe)
            case CSSPrimitiveValue.CSS_EMS,
                 CSSPrimitiveValue.CSS_EXS -> true;

            // length
            case CSSPrimitiveValue.CSS_IN,
                 CSSPrimitiveValue.CSS_CM,
                 CSSPrimitiveValue.CSS_MM,
                 CSSPrimitiveValue.CSS_PT,
                 CSSPrimitiveValue.CSS_PC,
                 CSSPrimitiveValue.CSS_PX -> true;

            // color
            case CSSPrimitiveValue.CSS_RGBCOLOR -> true;

            // ?
            case CSSPrimitiveValue.CSS_ATTR,
                 CSSPrimitiveValue.CSS_DIMENSION,
                 CSSPrimitiveValue.CSS_NUMBER,
                 CSSPrimitiveValue.CSS_RECT -> true;

            // counters
            case CSSPrimitiveValue.CSS_COUNTER -> true;

            // angles
            case CSSPrimitiveValue.CSS_DEG,
                 CSSPrimitiveValue.CSS_GRAD,
                 CSSPrimitiveValue.CSS_RAD -> true;

            // aural - freq
            case CSSPrimitiveValue.CSS_HZ,
                 CSSPrimitiveValue.CSS_KHZ -> true;

            // time
            case CSSPrimitiveValue.CSS_S,
                 CSSPrimitiveValue.CSS_MS -> true;

            // URI
            case CSSPrimitiveValue.CSS_URI,

                 CSSPrimitiveValue.CSS_IDENT,
                 CSSPrimitiveValue.CSS_STRING -> true;

            case CSSPrimitiveValue.CSS_UNKNOWN -> {
                XRLog.cascade(Level.WARNING, "Asked whether type was absolute, given CSS_UNKNOWN as the type. " +
                    "Might be one of those funny values like background-position.");
                GeneralUtil.dumpShortException(new Exception("Taking a thread dump..."));
                yield false;
            }

            default -> false;
        };
    }

    /**
     * Returns true if the SAC primitive value type is a number unit--a unit
     * that can only contain a numeric value. This is a shorthand way of saying,
     * did the user declare this as a number unit (like px)?
     */
    public static boolean isNumber(short cssPrimitiveType) {
        return switch (cssPrimitiveType) {
            // fall through on all these
            // relative length or size
            case CSSPrimitiveValue.CSS_EMS, CSSPrimitiveValue.CSS_EXS, CSSPrimitiveValue.CSS_PERCENTAGE ->
                // relatives will be treated separately from lengths;
                    false;
            // length
            case CSSPrimitiveValue.CSS_PX, CSSPrimitiveValue.CSS_IN, CSSPrimitiveValue.CSS_CM, CSSPrimitiveValue.CSS_MM, CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PC ->
                    true;
            default -> false;
        };
    }

    static {
        // HACK: this is a quick way to perform the lookup, but dumb if the short assigned are > 100; but the compiler will tell us that (PWW 21-01-05)
        sacTypesStrings.put(CSSPrimitiveValue.CSS_EMS, "em");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_EXS, "ex");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_PX, "px");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_PERCENTAGE, "%");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_IN, "in");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_CM, "cm");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_MM, "mm");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_PT, "pt");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_PC, "pc");
    }
}

