/*
 * {{{ header & license
 * BorderColorPropertyDeclarationFactory.java
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
package org.xhtmlrenderer.css.sheet.factory;

import java.util.*;

import org.w3c.dom.css.CSSPrimitiveValue;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.value.FSCssValue;


/**
 * A PropertyDeclarationFactory for CSS 2 "border-color" shorthand property, instantiating
 * PropertyDeclarations; Singleton, use {@link #instance()}.
 *
 * @author   Patrick Wright
 */
public class BorderColorPropertyDeclarationFactory extends AbstractPropertyDeclarationFactory {
    /** Singleton instance. */
    private static BorderColorPropertyDeclarationFactory _instance;

    /**
     * List of property names, in order, when expanding 1 prop to 4. Careful,
     * this is closely in sync with the explosion code below, don't change
     * willy-nilly.
     */
    private final static CSSName ONE_TO_FOUR[] = {
                CSSName.BORDER_COLOR_TOP,
                CSSName.BORDER_COLOR_RIGHT,
                CSSName.BORDER_COLOR_BOTTOM,
                CSSName.BORDER_COLOR_LEFT};

    /**
     * List of property names, in order, when expanding 2 prop to 4. Careful,
     * this is closely in sync with the explosion code below, don't change
     * willy-nilly.
     */
    private final static CSSName TWO_TO_FOUR[] = {
                CSSName.BORDER_COLOR_TOP,
                CSSName.BORDER_COLOR_BOTTOM,
                CSSName.BORDER_COLOR_RIGHT,
                CSSName.BORDER_COLOR_LEFT};

    /**
     * List of property names, in order, when expanding 3 prop to 4. Careful,
     * this is closely in sync with the explosion code below, don't change
     * willy-nilly.
     */
    private final static CSSName THREE_TO_FOUR[] = {
                CSSName.BORDER_COLOR_TOP,
                CSSName.BORDER_COLOR_RIGHT,
                CSSName.BORDER_COLOR_LEFT,
                CSSName.BORDER_COLOR_BOTTOM};

    /**
     * List of property names, in order, when expanding 4 prop to 4. Careful,
     * this is closely in sync with the explosion code below, don't change
     * willy-nilly.
     */
    private final static CSSName FOUR_TO_FOUR[] = {
                CSSName.BORDER_COLOR_TOP,
                CSSName.BORDER_COLOR_RIGHT,
                CSSName.BORDER_COLOR_BOTTOM,
                CSSName.BORDER_COLOR_LEFT};

    /** Default constructor; don't use, use instance() instead. */
    private BorderColorPropertyDeclarationFactory() { }

    /**
     * Subclassed implementation of redirected buildDeclarations() from abstract
     * superclass.
     *
     * @param primVals   The SAC value for this property
     * @param priority   Priority string for this value
     * @param important  True if author-marked important!
     * @param cssName   property name
     * @param origin     The origin of the stylesheet; constant from {@link
     *      org.xhtmlrenderer.css.sheet.Stylesheet}, e.g. Stylesheet.AUTHOR
     * @return           Iterator of PropertyDeclarations for the shorthand
     *      margin property.
     */
    protected Iterator doBuildDeclarations( CSSPrimitiveValue[] primVals,
                                            boolean important,
                                            CSSName cssName,
                                            int origin ) {

        List declarations = new ArrayList();

        // border-color explodes differently based on number of supplied values
        CSSPrimitiveValue p1 = null;
        CSSPrimitiveValue p2 = null;
        CSSPrimitiveValue p3 = null;
        CSSPrimitiveValue p4 = null;
        CSSPrimitiveValue[] primitives = null;

        switch ( primVals.length ) {
            case 1:
                // HACK: can use any color-property name here, just helps FSCssValue
                // figure out ident conversion (PWW 20-11-04)
                CSSPrimitiveValue primitive = new FSCssValue( CSSName.BORDER_COLOR_BOTTOM, primVals[0] );
                primitives = new CSSPrimitiveValue[] {
                            primitive,
                            primitive,
                            primitive,
                            primitive};

                addProperties( declarations, primitives, ONE_TO_FOUR, origin, important );
                break;
            case 2:
                p1 = new FSCssValue( CSSName.BORDER_COLOR_BOTTOM, primVals[ 0 ] );
                p2 = new FSCssValue( CSSName.BORDER_COLOR_BOTTOM, primVals[ 1 ] );
                primitives = new CSSPrimitiveValue[]{p1, p1, p2, p2};

                addProperties( declarations, primitives, TWO_TO_FOUR, origin, important );
                break;
            case 3:
                p1 = new FSCssValue( CSSName.BORDER_COLOR_BOTTOM, primVals[ 0 ] );
                p2 = new FSCssValue( CSSName.BORDER_COLOR_BOTTOM, primVals[ 1 ] );
                p3 = new FSCssValue( CSSName.BORDER_COLOR_BOTTOM, primVals[ 2 ] );
                primitives = new CSSPrimitiveValue[]{p1, p2, p2, p3};

                addProperties( declarations, primitives, THREE_TO_FOUR, origin, important );
                break;
            case 4:
                p1 = new FSCssValue( CSSName.BORDER_COLOR_BOTTOM, primVals[ 0 ] );
                p2 = new FSCssValue( CSSName.BORDER_COLOR_BOTTOM, primVals[ 1 ] );
                p3 = new FSCssValue( CSSName.BORDER_COLOR_BOTTOM, primVals[ 2 ] );
                p4 = new FSCssValue( CSSName.BORDER_COLOR_BOTTOM, primVals[ 3 ] );
                primitives = new CSSPrimitiveValue[]{p1, p2, p3, p4};

                addProperties( declarations, primitives, FOUR_TO_FOUR, origin, important );
                break;
        }
        return declarations.iterator();
    }

    /*
     * // CAREFUL: note that with steadyState parser impl, their value class impl
     * // both primitive and value list interfaces! use getCssValueType(), not instanceof!!
     * if ( primVals.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE ) {
     * // HACK: can use any color-property name here, just helps FSCssValue
     * // figure out ident conversion (PWW 20-11-04)
     * CSSPrimitiveValue primitive = new FSCssValue(CSSName.BORDER_COLOR_BOTTOM, (CSSPrimitiveValue)primVals);
     * CSSPrimitiveValue primitives[] = {
     * primitive,
     * primitive,
     * primitive,
     * primitive};
     * addProperties( declarations, primitives, ONE_TO_FOUR, origin, important );
     * } else {
     * // is a value list
     * CSSValueList vList = (CSSValueList)primVals;
     * // border-color explodes differently based on number of supplied values
     * CSSPrimitiveValue p1, p2, p3, p4 = null;
     * CSSPrimitiveValue primitives[] = null;
     * String names[] = null;
     * switch ( vList.getLength() ) {
     * case 1:
     * // bug in CSSValue implementation! should be a primitive value
     * // not a list
     * throw new XRRuntimeException( "'border-color' property has only one value, but SAC parser marked it as a value list." );
     * case 2:
     * p1 = new FSCssValue(CSSName.BORDER_COLOR_BOTTOM,(CSSPrimitiveValue)vList.item( 0 ));
     * p2 = new FSCssValue(CSSName.BORDER_COLOR_BOTTOM,(CSSPrimitiveValue)vList.item( 1 ));
     * primitives = new CSSPrimitiveValue[]{ p1, p1, p2, p2 };
     * addProperties( declarations, primitives, TWO_TO_FOUR, origin, important );
     * break;
     * case 3:
     * p1 = new FSCssValue(CSSName.BORDER_COLOR_BOTTOM,(CSSPrimitiveValue)vList.item( 0 ));
     * p2 = new FSCssValue(CSSName.BORDER_COLOR_BOTTOM,(CSSPrimitiveValue)vList.item( 1 ));
     * p3 = new FSCssValue(CSSName.BORDER_COLOR_BOTTOM,(CSSPrimitiveValue)vList.item( 2 ));
     * primitives = new CSSPrimitiveValue[]{ p1, p2, p2, p3 };
     * addProperties( declarations, primitives, THREE_TO_FOUR, origin, important );
     * break;
     * case 4:
     * p1 = new FSCssValue(CSSName.BORDER_COLOR_BOTTOM,(CSSPrimitiveValue)vList.item( 0 ));
     * p2 = new FSCssValue(CSSName.BORDER_COLOR_BOTTOM,(CSSPrimitiveValue)vList.item( 1 ));
     * p3 = new FSCssValue(CSSName.BORDER_COLOR_BOTTOM,(CSSPrimitiveValue)vList.item( 2 ));
     * p4 = new FSCssValue(CSSName.BORDER_COLOR_BOTTOM,(CSSPrimitiveValue)vList.item( 3 ));
     * primitives = new CSSPrimitiveValue[]{ p1, p2, p3, p4 };
     * addProperties( declarations, primitives, FOUR_TO_FOUR, origin, important );
     * break;
     * }
     * }
     * return declarations.iterator();
     * }
     */
    /**
     * Returns the singleton instance.
     *
     * @return   See desc.
     */
    public static synchronized PropertyDeclarationFactory instance() {
        if ( _instance == null ) {
            _instance = new BorderColorPropertyDeclarationFactory();
        }
        return _instance;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2005/01/29 12:14:20  pdoubleya
 * Removed priority as a parameter, added alternate build when only CSSValue is available; could be used in a SAC DocumentHandler after the CSSValue is initialized from a property.
 *
 * Revision 1.2  2005/01/24 19:00:58  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.1  2005/01/24 14:25:34  pdoubleya
 * Added to CVS.
 *
 *
 */

