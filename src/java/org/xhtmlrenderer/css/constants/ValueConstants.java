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
import org.w3c.dom.css.CSSValue;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;


/**
 * Utility class for working with <code>CSSValue</code> instances.
 *
 * @author empty
 */
public final class ValueConstants {
    /**
     * Type descriptions--a crude approximation taken by scanning CSSValue
     * statics
     */
    private final static List TYPE_DESCRIPTIONS;
    /**
     * Description of the Field
     */
    private final static Map sacTypesStrings;

    /**
     * A text representation of the CSS type for this value.
     *
     * @param cssType            PARAM
     * @param primitiveValueType PARAM
     * @return Returns
     */
    public static String cssType(int cssType, int primitiveValueType) {
        String desc = null;
        if (cssType == CSSValue.CSS_PRIMITIVE_VALUE) {
            if (primitiveValueType >= TYPE_DESCRIPTIONS.size()) {
                desc = "{unknown: " + primitiveValueType + "}";
            } else {
                desc = (String) TYPE_DESCRIPTIONS.get(primitiveValueType);
                if (desc == null) {
                    desc = "{UNKNOWN VALUE TYPE}";
                }
            }
        } else {
            desc = "{value list}";
        }
        return desc;
    }

    /**
     * Description of the Method
     *
     * @param type PARAM
     * @return Returns
     */
    public static short sacPrimitiveTypeForString(String type) {
        if ("em".equals(type)) {
            return CSSPrimitiveValue.CSS_EMS;
        } else if ("ex".equals(type)) {
            return CSSPrimitiveValue.CSS_EXS;
        } else if ("px".equals(type)) {
            return CSSPrimitiveValue.CSS_PX;
        } else if ("%".equals(type)) {
            return CSSPrimitiveValue.CSS_PERCENTAGE;
        } else if ("in".equals(type)) {
            return CSSPrimitiveValue.CSS_IN;
        } else if ("cm".equals(type)) {
            return CSSPrimitiveValue.CSS_CM;
        } else if ("mm".equals(type)) {
            return CSSPrimitiveValue.CSS_MM;
        } else if ("pt".equals(type)) {
            return CSSPrimitiveValue.CSS_PT;
        } else if ("pc".equals(type)) {
            return CSSPrimitiveValue.CSS_PC;
        } else if (type == null) {
            //this is only valid if length is 0
            return CSSPrimitiveValue.CSS_PX;
        } else {
            throw new XRRuntimeException("Unknown type on CSS value: " + type);
        }
    }

    /**
     * Description of the Method
     *
     * @param type PARAM
     * @return Returns
     */
    public static String stringForSACPrimitiveType(short type) {
        return (String) sacTypesStrings.get(new Short(type));
    }

    /**
     * Returns true if the specified value was absolute (even if we have a
     * computed value for it), meaning that either the value can be used
     * directly (e.g. pixels) or there is a fixed context-independent conversion
     * for it (e.g. inches). Proportional types (e.g. %) return false.
     *
     * @param primitive The CSSValue instance to check.
     * @return See desc.
     */
    //TODO: method may be unnecessary (tobe)
    public static boolean isAbsoluteUnit(CSSPrimitiveValue primitive) {
        short type = 0;
        type = ((CSSPrimitiveValue) primitive).getPrimitiveType();
        return isAbsoluteUnit(type);
    }

    /**
     * Returns true if the specified type absolute (even if we have a computed
     * value for it), meaning that either the value can be used directly (e.g.
     * pixels) or there is a fixed context-independent conversion for it (e.g.
     * inches). Proportional types (e.g. %) return false.
     *
     * @param type The CSSValue type to check.
     * @return See desc.
     */
    //TODO: method may be unnecessary (tobe)
    public static boolean isAbsoluteUnit(short type) {
        // TODO: check this list...

        // note, all types are included here to make sure none are missed
        switch (type) {
            // proportional length or size
            case CSSPrimitiveValue.CSS_PERCENTAGE:
                return false;
                // refer to values known to the DerivedValue instance (tobe)
            case CSSPrimitiveValue.CSS_EMS:
            case CSSPrimitiveValue.CSS_EXS:
                // length
            case CSSPrimitiveValue.CSS_IN:
            case CSSPrimitiveValue.CSS_CM:
            case CSSPrimitiveValue.CSS_MM:
            case CSSPrimitiveValue.CSS_PT:
            case CSSPrimitiveValue.CSS_PC:
            case CSSPrimitiveValue.CSS_PX:

                // color
            case CSSPrimitiveValue.CSS_RGBCOLOR:

                // ?
            case CSSPrimitiveValue.CSS_ATTR:
            case CSSPrimitiveValue.CSS_DIMENSION:
            case CSSPrimitiveValue.CSS_NUMBER:
            case CSSPrimitiveValue.CSS_RECT:

                // counters
            case CSSPrimitiveValue.CSS_COUNTER:

                // angles
            case CSSPrimitiveValue.CSS_DEG:
            case CSSPrimitiveValue.CSS_GRAD:
            case CSSPrimitiveValue.CSS_RAD:

                // aural - freq
            case CSSPrimitiveValue.CSS_HZ:
            case CSSPrimitiveValue.CSS_KHZ:

                // time
            case CSSPrimitiveValue.CSS_S:
            case CSSPrimitiveValue.CSS_MS:

                // URI
            case CSSPrimitiveValue.CSS_URI:

            case CSSPrimitiveValue.CSS_IDENT:
            case CSSPrimitiveValue.CSS_STRING:
                return true;
            case CSSPrimitiveValue.CSS_UNKNOWN:
                XRLog.cascade(Level.WARNING, "Asked whether type was absolute, given CSS_UNKNOWN as the type. " +
                        "Might be one of those funny values like background-position.");
                GeneralUtil.dumpShortException(new Exception());
                // fall-through
            default:
                return false;
        }
    }

    /**
     * Gets the cssValueTypeDesc attribute of the {@link CSSValue} object
     *
     * @param cssValue PARAM
     * @return The cssValueTypeDesc value
     */
    public static String getCssValueTypeDesc(CSSValue cssValue) {
        switch (cssValue.getCssValueType()) {
            case CSSValue.CSS_CUSTOM:
                return "CSS_CUSTOM";
            case CSSValue.CSS_INHERIT:
                return "CSS_INHERIT";
            case CSSValue.CSS_PRIMITIVE_VALUE:
                return "CSS_PRIMITIVE_VALUE";
            case CSSValue.CSS_VALUE_LIST:
                return "CSS_VALUE_LIST";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Returns true if the SAC primitive value type is a number unit--a unit
     * that can only contain a numeric value. This is a shorthand way of saying,
     * did the user declare this as a number unit (like px)?
     *
     * @param cssPrimitiveType PARAM
     * @return See desc.
     */
    public static boolean isNumber(short cssPrimitiveType) {
        switch (cssPrimitiveType) {
            // fall thru on all these
            // relative length or size
            case CSSPrimitiveValue.CSS_EMS:
            case CSSPrimitiveValue.CSS_EXS:
            case CSSPrimitiveValue.CSS_PERCENTAGE:
                // relatives will be treated separately from lengths;
                return false;
                // length
            case CSSPrimitiveValue.CSS_PX:
            case CSSPrimitiveValue.CSS_IN:
            case CSSPrimitiveValue.CSS_CM:
            case CSSPrimitiveValue.CSS_MM:
            case CSSPrimitiveValue.CSS_PT:
            case CSSPrimitiveValue.CSS_PC:
                return true;
            default:
                return false;
        }
    }

    static {
        SortedMap map = new TreeMap();
        TYPE_DESCRIPTIONS = new ArrayList();
        try {
            Field fields[] = CSSPrimitiveValue.class.getFields();
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                int mod = f.getModifiers();
                if (Modifier.isFinal(mod) &&
                        Modifier.isStatic(mod) &&
                        Modifier.isPublic(mod)) {

                    Short val = (Short) f.get(null);
                    String name = f.getName();
                    if (name.startsWith("CSS_")) {
                        if (!name.equals("CSS_INHERIT") &&
                                !name.equals("CSS_PRIMITIVE_VALUE") &&
                                !name.equals("CSS_VALUE_LIST") &&
                                !name.equals("CSS_CUSTOM")) {

                            map.put(val, name.substring("CSS_".length()));
                        }
                    }
                }
            }
            // now sort by the key--the short constant for the public fields
            List keys = new ArrayList(map.keySet());
            Collections.sort(keys);

            // then add to our static list, in the order the keys appear. this means
            // list.get(index) will return the item at index, which should be the description
            // for that constant
            Iterator iter = keys.iterator();
            while (iter.hasNext()) {
                TYPE_DESCRIPTIONS.add(map.get(iter.next()));
            }
        } catch (Exception ex) {
            throw new XRRuntimeException("Could not build static list of CSS type descriptions.", ex);
        }

        // HACK: this is a quick way to perform the lookup, but dumb if the short assigned are > 100; but the compiler will tell us that (PWW 21-01-05)
        sacTypesStrings = new HashMap(25);
        sacTypesStrings.put(new Short(CSSPrimitiveValue.CSS_EMS), "em");
        sacTypesStrings.put(new Short(CSSPrimitiveValue.CSS_EXS), "ex");
        sacTypesStrings.put(new Short(CSSPrimitiveValue.CSS_PX), "px");
        sacTypesStrings.put(new Short(CSSPrimitiveValue.CSS_PERCENTAGE), "%");
        sacTypesStrings.put(new Short(CSSPrimitiveValue.CSS_IN), "in");
        sacTypesStrings.put(new Short(CSSPrimitiveValue.CSS_CM), "cm");
        sacTypesStrings.put(new Short(CSSPrimitiveValue.CSS_MM), "mm");
        sacTypesStrings.put(new Short(CSSPrimitiveValue.CSS_PT), "pt");
        sacTypesStrings.put(new Short(CSSPrimitiveValue.CSS_PC), "pc");
    }

    /**
     * Incomplete routine to try and determine the
     * CSSPrimitiveValue short code for a given value,
     * e.g. 14pt is CSS_PT.
     *
     * @param value PARAM
     * @return Returns
     */
    public static short guessType(String value) {
        short type = CSSPrimitiveValue.CSS_STRING;
        if (value != null && value.length() > 1) {
            if (value.endsWith("%")) {
                type = CSSPrimitiveValue.CSS_PERCENTAGE;
            } else if (value.startsWith("rgb") || value.startsWith("#")) {
                type = CSSPrimitiveValue.CSS_RGBCOLOR;
            } else {
                String hmm = value.substring(value.length() - 2);
                if ("pt".equals(hmm)) {
                    type = CSSPrimitiveValue.CSS_PT;
                } else if ("px".equals(hmm)) {
                    type = CSSPrimitiveValue.CSS_PX;
                } else if ("em".equals(hmm)) {
                    type = CSSPrimitiveValue.CSS_EMS;
                } else if ("ex".equals(hmm)) {
                    type = CSSPrimitiveValue.CSS_EXS;
                } else if ("in".equals(hmm)) {
                    type = CSSPrimitiveValue.CSS_IN;
                } else if ("cm".equals(hmm)) {
                    type = CSSPrimitiveValue.CSS_CM;
                } else if ("mm".equals(hmm)) {
                    type = CSSPrimitiveValue.CSS_MM;
                } else {
                    if (Character.isDigit(value.charAt(value.length() - 1))) {
                        try {
                            new Float(value);
                            type = CSSPrimitiveValue.CSS_NUMBER;
                        } catch (NumberFormatException ex) {
                            type = CSSPrimitiveValue.CSS_STRING;
                        }
                    } else {
                        type = CSSPrimitiveValue.CSS_STRING;
                    }
                }
            }
        }
        return type;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.10  2005/10/25 16:06:49  pdoubleya
 * For guessing type, with no type code, check last char, not first.
 *
 * Revision 1.9  2005/10/25 15:38:27  pdoubleya
 * Moved guessType() to ValueConstants, applied fix to method suggested by Chris Oliver, to avoid exception-based catch.
 *
 * Revision 1.8  2005/09/11 20:43:15  tobega
 * Fixed table-css interaction bug, colspan now works again
 *
 * Revision 1.7  2005/06/01 00:47:01  tobega
 * Partly confused hack trying to get width and height working properly for replaced elements.
 *
 * Revision 1.6  2005/01/29 20:18:40  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.5  2005/01/24 14:52:20  pdoubleya
 * Fixed accidental access modifier change to private--isAbsoluteUnit() is used in tests.
 *
 * Revision 1.4  2005/01/24 14:36:32  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.3  2004/11/16 10:38:21  pdoubleya
 * Use XRR exception, added comments.
 *
 * Revision 1.2  2004/10/23 13:09:13  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards
 * except for common packages
 * (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

