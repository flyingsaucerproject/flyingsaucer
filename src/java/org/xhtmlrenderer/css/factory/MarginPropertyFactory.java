/*
 * {{{ header & license
 * MarginPropertyFactory.java
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
 * A PropertyFactory for CSS 2 "margin" shorthand property, instantiating
 * XRProperties; Singleton, use {@link #instance()}.
 *
 * @author   Patrick Wright
 */
public class MarginPropertyFactory extends AbstractPropertyFactory {
    /** Singleton instance. */
    private static MarginPropertyFactory _instance;


    /** Constructor for the MarginPropertyFactory object */
    private MarginPropertyFactory() { }

    /**
     * If <code>propName</code> describes a shorthand property, explodes it into
     * the specific properties it is a shorthand for, and returns those as an
     * Iterator of {@link org.xhtmlrenderer.css.XRProperty} instances; or just
     * instantiates a single <code>XRProperty</code> for non-shorthand props.
     *
     * @param style     The CSSStyleDeclaration from the SAC parser.
     * @param propName  The String property name for the property to explode.
     * @param sequence  Sequence in which the declaration was found in the
     *      containing stylesheet.
     * @return          Iterator of one or more XRProperty instances
     *      representing the exploded values.
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

            // margin explodes differently based on number of supplied values
            CSSPrimitiveValue primitive = null;
            switch ( vList.getLength() ) {
                case 1:
                    // bug in CSSValue implementation! but who cares
                    primitive = (CSSPrimitiveValue)vList.item( 0 );
                    list.addAll( explodeOne( primitive, priority, style, sequence ) );
                    break;
                case 2:
                    primitive = (CSSPrimitiveValue)vList.item( 0 );
                    list.add( newProperty( CSSName.MARGIN_TOP, primitive, priority, style, sequence ) );
                    list.add( newProperty( CSSName.MARGIN_BOTTOM, primitive, priority, style, sequence ) );

                    primitive = (CSSPrimitiveValue)vList.item( 1 );
                    list.add( newProperty( CSSName.MARGIN_RIGHT, primitive, priority, style, sequence ) );
                    list.add( newProperty( CSSName.MARGIN_LEFT, primitive, priority, style, sequence ) );
                    break;
                case 3:
                    primitive = (CSSPrimitiveValue)vList.item( 0 );
                    list.add( newProperty( CSSName.MARGIN_TOP, primitive, priority, style, sequence ) );

                    primitive = (CSSPrimitiveValue)vList.item( 1 );
                    list.add( newProperty( CSSName.MARGIN_RIGHT, primitive, priority, style, sequence ) );
                    list.add( newProperty( CSSName.MARGIN_LEFT, primitive, priority, style, sequence ) );

                    primitive = (CSSPrimitiveValue)vList.item( 2 );
                    list.add( newProperty( CSSName.MARGIN_BOTTOM, primitive, priority, style, sequence ) );
                    break;
                case 4:
                    primitive = (CSSPrimitiveValue)vList.item( 0 );
                    list.add( newProperty( CSSName.MARGIN_TOP, primitive, priority, style, sequence ) );

                    primitive = (CSSPrimitiveValue)vList.item( 1 );
                    list.add( newProperty( CSSName.MARGIN_RIGHT, primitive, priority, style, sequence ) );

                    primitive = (CSSPrimitiveValue)vList.item( 2 );
                    list.add( newProperty( CSSName.MARGIN_BOTTOM, primitive, priority, style, sequence ) );

                    primitive = (CSSPrimitiveValue)vList.item( 3 );
                    list.add( newProperty( CSSName.MARGIN_LEFT, primitive, priority, style, sequence ) );
                    break;
                default:
                    System.err.println( "Property '" + propName + "' has more than 4 values assigned." );
                    break;
            }
        }// is a value list

        return list.iterator();
    }


    /**
     * Explodes a single shorthand property declaration into components (one per
     * side).
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

        List list = new ArrayList();
        XRValueImpl val = null;
        val = new XRValueImpl( primitive, priority );
        list.add( new XRPropertyImpl( CSSName.MARGIN_TOP, sequence, val ) );
        list.add( new XRPropertyImpl( CSSName.MARGIN_RIGHT, sequence, val ) );
        list.add( new XRPropertyImpl( CSSName.MARGIN_BOTTOM, sequence, val ) );
        list.add( new XRPropertyImpl( CSSName.MARGIN_LEFT, sequence, val ) );
        return list;
    }


    /**
     * Returns the singleton instance.
     *
     * @return   Returns
     */
    public static synchronized PropertyFactory instance() {
        if ( _instance == null ) {
            _instance = new MarginPropertyFactory();
        }
        return _instance;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2004/10/23 13:14:13  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

