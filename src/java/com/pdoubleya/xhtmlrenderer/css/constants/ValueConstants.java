/*
 * {{{ header & license
 * ValueConstants.java
 * Copyright (c) 2004 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.pdoubleya.xhtmlrenderer.css.constants;


import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import org.w3c.dom.css.CSSValue;
import java.util.*;

import org.w3c.dom.css.CSSPrimitiveValue;

import com.pdoubleya.xhtmlrenderer.css.XRValue;



/** 
 * Utility class for working with <code>CSSValue</code> instances.
*/
public class ValueConstants {
    /**
     * Type descriptions--a crude approximation taken by scanning CSSValue
     * statics
     */
    private final static List TYPE_DESCRIPTIONS;

    /**
     * Returns true if the specified value was absolute (even if we have a
     * computed value for it)
     *
     * @return   The absoluteUnit value
     */
    public static boolean isAbsoluteUnit(CSSValue cssValue) {
        // WARN: this will fail if not a primitive value
        if ( !( cssValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE )) 
            return false;
        
        // HACK: in case someone passes an instance of XRValue
        short type = 0;
        if ( cssValue instanceof XRValue ) {
            CSSValue nested = ((XRValue)cssValue).cssValue();
            type = ((CSSPrimitiveValue)nested).getPrimitiveType();
        } else {
            type = ((CSSPrimitiveValue)cssValue).getPrimitiveType();
        }

        // TODO: check this list...
        switch ( type ) {
            // relative length or size
            case CSSPrimitiveValue.CSS_EMS:
            case CSSPrimitiveValue.CSS_EXS:
            case CSSPrimitiveValue.CSS_PERCENTAGE:
                return false;
            
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
            default:
            System.out.println(cssValue.getCssText() + ", returning false");
                return false;
        }
    }
    
    /**
     * Gets the cssValueTypeDesc attribute of the XRValueImpl object
     *
     * @return   The cssValueTypeDesc value
     */
    public static String getCssValueTypeDesc(CSSValue cssValue) {
        switch ( cssValue.getCssValueType() ) {
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
     * Gets the length attribute of the XRValueImpl object
     *
     * @return   The length value
     */
    public static boolean isNumber(CSSPrimitiveValue cssValue) {
        return isNumber(cssValue.getPrimitiveType());   
    }

    /**
     * Gets the length attribute of the XRValueImpl object
     *
     * @return   The length value
     */
    public static boolean isNumber(short cssPrimitiveType) {
        switch ( cssPrimitiveType ) {
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
            for ( int i = 0; i < fields.length; i++ ) {
                Field f = fields[i];
                int mod = f.getModifiers();
                if ( Modifier.isFinal( mod ) &&
                        Modifier.isStatic( mod ) &&
                        Modifier.isPublic( mod ) ) {

                    Short val = (Short)f.get( null );
                    String name = f.getName();
                    if ( name.startsWith( "CSS_" ) ) {
                        if ( !name.equals( "CSS_INHERIT" ) &&
                                !name.equals( "CSS_PRIMITIVE_VALUE" ) &&
                                !name.equals( "CSS_VALUE_LIST" ) &&
                                !name.equals( "CSS_CUSTOM" ) ) {

                            map.put( val, name.substring( "CSS_".length() ) );
                        }
                    }
                }
            }
            // now sort by the key--the short constant for the public fields
            List keys = new ArrayList( map.keySet() );
            Collections.sort( keys );

            // then add to out static list, in the order the keys appear. this means
            // list.get(index) will return the item at index, which should be the description
            // for that constant
            Iterator iter = keys.iterator();
            while ( iter.hasNext() ) {
                TYPE_DESCRIPTIONS.add( map.get( iter.next() ) );
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }
    
    /**
     * A text representation of the CSS type for this value.
     *
     * @return   Returns
     */
    public static String cssType(int cssType, int primitiveValueType) {
        String desc = null;
        if ( cssType == CSSValue.CSS_PRIMITIVE_VALUE ) {
            if ( primitiveValueType >= TYPE_DESCRIPTIONS.size() ) {
                desc = "{unknown: " + primitiveValueType + "}";
            } else {
                desc = (String)TYPE_DESCRIPTIONS.get( primitiveValueType );
                if ( desc == null ) {
                    desc = "{UNKNOWN VALUE TYPE}";
                }
            }
        } else {
            desc = "{value list}";
        }
        return desc;
    }
} // end class
