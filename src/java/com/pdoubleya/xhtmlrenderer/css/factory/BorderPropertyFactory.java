/*
 * {{{ header & license
 * BorderPropertyFactory.java
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
package com.pdoubleya.xhtmlrenderer.css.factory;

import java.util.*;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import com.pdoubleya.xhtmlrenderer.css.constants.CSSName;
import com.pdoubleya.xhtmlrenderer.css.impl.XRValueImpl;


/**
 * A PropertyFactory for CSS 2 "border" shorthand property, instantiating XRProperties.
 *
 * @author    Patrick Wright
 *
 */
public class BorderPropertyFactory extends AbstractPropertyFactory {
    /** Singleton instance. */
    private static BorderPropertyFactory _instance;

    /** TODO--used in property explosion */
    private final static String WIDTH_PRP[];
    /** TODO--used in property explosion */
    private final static String STYLE_PRP[];
    /** TODO--used in property explosion */
    private final static String COLOR_PRP[];
    /** TODO--used in property explosion */
    private final static String LISTS[][];


    /** Constructor for the BorderPropertyFactory object */
    private BorderPropertyFactory() { }


    /**
     * * Returns the singleton instance.
     *
     * @return   Returns
     */
    public static synchronized PropertyFactory instance() {
        if ( _instance == null ) {
            _instance = new BorderPropertyFactory();
        }
        return _instance;
    }

    // thread-safe
    /**
     * If <code>propName</code> describes a shorthand property, explodes it into
     * the specific properties it is a shorthand for, and returns those as an
     * Iterator of {@link com.pdoubleya.xhtmlrenderer.css.XRProperty} instances;
     * or just instantiates a single <code>XRProperty</code> for non-shorthand
     * props.
     *
     * @param style     The CSSStyleDeclaration from the SAC parser.
     * @param propName  The String property name for the property to explode.
     * @param sequence  Sequence in which the declaration was found in the
     *      containing stylesheet.
     * @return          Iterator of one or more XRProperty instances representing the exploded values.
     */
    public Iterator explodeProperties( CSSStyleDeclaration style, String propName, int sequence ) {
        List list = new ArrayList();
        CSSValue cssValue = style.getPropertyCSSValue( propName );
        String priority = style.getPropertyPriority( propName );

        // CAREFUL: note that with steadyState parser impl, their value class impl
        // both primitive and value list interfaces! use getCssValueType(), not instanceof!!
        if ( cssValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE ) {
            // TODO
        } else {
            // is a value list
            CSSValueList vList = (CSSValueList)cssValue;

            // border-style explodes differently based on number of supplied values
            CSSPrimitiveValue primitive = null;

            primitive = (CSSPrimitiveValue)vList.item( 0 );
            list.add( newProperty( (String)LISTS[0][0], primitive, priority, style, sequence ) );
            list.add( newProperty( (String)LISTS[0][1], primitive, priority, style, sequence ) );
            list.add( newProperty( (String)LISTS[0][2], primitive, priority, style, sequence ) );
            list.add( newProperty( (String)LISTS[0][3], primitive, priority, style, sequence ) );

            primitive = (CSSPrimitiveValue)vList.item( 1 );
            list.add( newProperty( (String)LISTS[1][0], primitive, priority, style, sequence ) );
            list.add( newProperty( (String)LISTS[1][1], primitive, priority, style, sequence ) );
            list.add( newProperty( (String)LISTS[1][2], primitive, priority, style, sequence ) );
            list.add( newProperty( (String)LISTS[1][3], primitive, priority, style, sequence ) );

            primitive = (CSSPrimitiveValue)vList.item( 2 );
            list.add( newProperty( (String)LISTS[2][0], primitive, priority, style, sequence ) );
            list.add( newProperty( (String)LISTS[2][1], primitive, priority, style, sequence ) );
            list.add( newProperty( (String)LISTS[2][2], primitive, priority, style, sequence ) );
            list.add( newProperty( (String)LISTS[2][3], primitive, priority, style, sequence ) );
        }
        return list.iterator();
    }

    static {
        WIDTH_PRP = new String[]{
                CSSName.BORDER_WIDTH_TOP,
                CSSName.BORDER_WIDTH_RIGHT,
                CSSName.BORDER_WIDTH_BOTTOM,
                CSSName.BORDER_WIDTH_LEFT
                };

        STYLE_PRP = new String[]{
                CSSName.BORDER_STYLE_TOP,
                CSSName.BORDER_STYLE_RIGHT,
                CSSName.BORDER_STYLE_BOTTOM,
                CSSName.BORDER_STYLE_LEFT
                };

        COLOR_PRP = new String[]{
                CSSName.BORDER_COLOR_TOP,
                CSSName.BORDER_COLOR_RIGHT,
                CSSName.BORDER_COLOR_BOTTOM,
                CSSName.BORDER_COLOR_LEFT
                };

        LISTS = new String[3][];
        LISTS[0] = WIDTH_PRP;
        LISTS[1] = STYLE_PRP;
        LISTS[2] = COLOR_PRP;
    }
}

