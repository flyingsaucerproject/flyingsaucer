/*
 * {{{ header & license
 * BorderPropertyFactory.java
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
import org.xhtmlrenderer.css.RuleNormalizer;
import org.xhtmlrenderer.css.constants.CSSName;


/**
 * A PropertyFactory for CSS 2 "border" shorthand property, instantiating
 * XRProperties.
 *
 * @author   Patrick Wright
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
    /** TODO--used in property explosion */
    private final static int WIDTH_IDX;
    /** TODO--used in property explosion */
    private final static int STYLE_IDX;
    /** TODO--used in property explosion */
    private final static int COLOR_IDX;

    /** Constructor for the BorderPropertyFactory object */
    private BorderPropertyFactory() { }

    // thread-safe
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
            System.err.println( "BORDER: PRIMITIVE VALUE. NOT CURRENTLY CODED." );
        } else {
            // is a value list
            CSSValueList vList = (CSSValueList)cssValue;

            // border explodes differently based on number of supplied values
            // but values for color, style, width can be given in any order
            // loop over the ones given and sniff them out to see if they
            // look like color, style, width, then apply to all four sides

            CSSPrimitiveValue primitive = null;

            String sides[] = null;
            for ( int i = 0, len = vList.getLength(); i < len; i++ ) {
                primitive = (CSSPrimitiveValue)vList.item( i );
                String val = primitive.getCssText();
                if ( RuleNormalizer.looksLikeAColor( val ) ) {
                    sides = LISTS[COLOR_IDX];
                } else if ( RuleNormalizer.looksLikeABorderStyle( val ) ) {
                    sides = LISTS[STYLE_IDX];
                } else {
                    // it is a length
                    sides = LISTS[WIDTH_IDX];
                }
                for ( int j = 0; j < 4; j++ ) {
                    list.add(
                            newProperty(
                            sides[j],
                            primitive,
                            priority,
                            style,
                            sequence
                             )
                             );
                }
            }
        }
        return list.iterator();
    }


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

        WIDTH_IDX = 0;
        STYLE_IDX = 1;
        COLOR_IDX = 2;

        LISTS = new String[3][];
        LISTS[WIDTH_IDX] = WIDTH_PRP;
        LISTS[STYLE_IDX] = STYLE_PRP;
        LISTS[COLOR_IDX] = COLOR_PRP;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2004/10/23 13:14:12  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

