/*
 * {{{ header & license
 * BorderStylePropertyFactory.java
 * Copyright (c) 2004 Patrick Wright
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
package org.xhtmlrenderer.css.factory;

import java.util.*;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.impl.XRPropertyImpl;
import org.xhtmlrenderer.css.impl.XRValueImpl;


/**
 * Used to pull 'border-style' properties out of CSS. Explosion of shorthand is
 * same as for margin, but leaving this in a separate class in case later want
 * to do other things here, like initial values, validations, etc.
 *
 * @author    Patrick Wright
 *
 */
public class BorderStylePropertyFactory extends AbstractPropertyFactory {
    /** Singleton instance. */
    private static BorderStylePropertyFactory _instance;


    /** Constructor for the BorderStylePropertyFactory object */
    private BorderStylePropertyFactory() { }


    /**
     * Returns the singleton instance.
     *
     * @return   Returns
     */
    public static synchronized PropertyFactory instance() {
        if ( _instance == null ) {
            _instance = new BorderStylePropertyFactory();
        }
        return _instance;
    }

    // thread-safe
    /**
     * If <code>propName</code> describes a shorthand property, explodes it into
     * the specific properties it is a shorthand for, and returns those as an
     * Iterator of {@link org.xhtmlrenderer.css.XRProperty} instances;
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
            list.addAll( explodeOne( (CSSPrimitiveValue)cssValue, priority, style, sequence ) );
        } else {
            // is a value list
            CSSValueList vList = (CSSValueList)cssValue;

            // border-style explodes differently based on number of supplied values
            CSSPrimitiveValue primitive = null;
            switch ( vList.getLength() ) {
                case 1:
                    // would be bug in CSSValue implementation! but who cares
                    primitive = (CSSPrimitiveValue)vList.item( 0 );
                    list.addAll( explodeOne( primitive, priority, style, sequence ) );
                    break;
                case 2:
                    primitive = (CSSPrimitiveValue)vList.item( 0 );
                    list.add( newProperty( CSSName.BORDER_STYLE_TOP, primitive, priority, style, sequence ) );
                    list.add( newProperty( CSSName.BORDER_STYLE_BOTTOM, primitive, priority, style, sequence ) );

                    primitive = (CSSPrimitiveValue)vList.item( 1 );
                    list.add( newProperty( CSSName.BORDER_STYLE_RIGHT, primitive, priority, style, sequence ) );
                    list.add( newProperty( CSSName.BORDER_STYLE_LEFT, primitive, priority, style, sequence ) );
                    break;
                case 3:
                    primitive = (CSSPrimitiveValue)vList.item( 0 );
                    list.add( newProperty( CSSName.BORDER_STYLE_TOP, primitive, priority, style, sequence ) );

                    primitive = (CSSPrimitiveValue)vList.item( 1 );
                    list.add( newProperty( CSSName.BORDER_STYLE_RIGHT, primitive, priority, style, sequence ) );
                    list.add( newProperty( CSSName.BORDER_STYLE_LEFT, primitive, priority, style, sequence ) );

                    primitive = (CSSPrimitiveValue)vList.item( 2 );
                    list.add( newProperty( CSSName.BORDER_STYLE_BOTTOM, primitive, priority, style, sequence ) );
                    break;
                case 4:
                    primitive = (CSSPrimitiveValue)vList.item( 0 );
                    list.add( newProperty( CSSName.BORDER_STYLE_TOP, primitive, priority, style, sequence ) );

                    primitive = (CSSPrimitiveValue)vList.item( 1 );
                    list.add( newProperty( CSSName.BORDER_STYLE_RIGHT, primitive, priority, style, sequence ) );

                    primitive = (CSSPrimitiveValue)vList.item( 2 );
                    list.add( newProperty( CSSName.BORDER_STYLE_BOTTOM, primitive, priority, style, sequence ) );

                    primitive = (CSSPrimitiveValue)vList.item( 3 );
                    list.add( newProperty( CSSName.BORDER_STYLE_LEFT, primitive, priority, style, sequence ) );
                    break;
                default:
                    System.err.println( "Property '" + propName + "' has more than 4 values assigned." );
                    break;
            }
        }// is a value list

        return list.iterator();
    }


    /**
     * Explodes a single shorthand property declaration into components (one per side).
     *
     * @param primitive  PARAM
     * @param priority   PARAM
     * @param style      PARAM
     * @param sequence   PARAM
     * @return           Returns
     */
    private List explodeOne( CSSPrimitiveValue primitive,
            String priority,
            CSSStyleDeclaration style,
            int sequence ) {

        List newList = new ArrayList();
        XRValueImpl val = null;
        val = new XRValueImpl( primitive, priority );

        // HACK: current BorderPainter in Josh's code expects single border-style to still be there
        // even though in principle it is explodable.
        newList.add( new XRPropertyImpl( CSSName.BORDER_STYLE_SHORTHAND, sequence, val ) );

        newList.add( new XRPropertyImpl( CSSName.BORDER_STYLE_TOP, sequence, val ) );
        newList.add( new XRPropertyImpl( CSSName.BORDER_STYLE_RIGHT, sequence, val ) );
        newList.add( new XRPropertyImpl( CSSName.BORDER_STYLE_BOTTOM, sequence, val ) );
        newList.add( new XRPropertyImpl( CSSName.BORDER_STYLE_LEFT, sequence, val ) );
        return newList;
    }
}

