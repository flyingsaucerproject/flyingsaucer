/*
 * {{{ header & license
 * BorderStylePropertyDeclarationFactory.java
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
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;


/**
 * A PropertyDeclarationFactory for CSS 2 "border-style" shorthand property, instantiating
 * PropertyDeclarations; Singleton, use {@link #instance()}.
 *
 * @author   Patrick Wright
 */
public class BorderStylePropertyDeclarationFactory extends AbstractPropertyDeclarationFactory {
    /** Singleton instance. */
    private static BorderStylePropertyDeclarationFactory _instance;

    /**
     * List of property names, in order, when expanding 1 prop to 4. Careful,
     * this is closely in sync with the explosion code below, don't change
     * willy-nilly.
     */
    private static String ONE_TO_FOUR[] = {
                CSSName.BORDER_STYLE_TOP,
                CSSName.BORDER_STYLE_RIGHT,
                CSSName.BORDER_STYLE_BOTTOM,
                CSSName.BORDER_STYLE_LEFT};

    /**
     * List of property names, in order, when expanding 2 prop to 4. Careful,
     * this is closely in sync with the explosion code below, don't change
     * willy-nilly.
     */
    private static String TWO_TO_FOUR[] = {
                CSSName.BORDER_STYLE_TOP,
                CSSName.BORDER_STYLE_BOTTOM,
                CSSName.BORDER_STYLE_RIGHT,
                CSSName.BORDER_STYLE_LEFT};

    /**
     * List of property names, in order, when expanding 3 prop to 4. Careful,
     * this is closely in sync with the explosion code below, don't change
     * willy-nilly.
     */
    private static String THREE_TO_FOUR[] = {
                CSSName.BORDER_STYLE_TOP,
                CSSName.BORDER_STYLE_RIGHT,
                CSSName.BORDER_STYLE_LEFT,
                CSSName.BORDER_STYLE_BOTTOM};

    /**
     * List of property names, in order, when expanding 4 prop to 4. Careful,
     * this is closely in sync with the explosion code below, don't change
     * willy-nilly.
     */
    private static String FOUR_TO_FOUR[] = {
                CSSName.BORDER_STYLE_TOP,
                CSSName.BORDER_STYLE_RIGHT,
                CSSName.BORDER_STYLE_BOTTOM,
                CSSName.BORDER_STYLE_LEFT};

    /** Default constructor; don't use, use instance() instead. */
    private BorderStylePropertyDeclarationFactory() { }

    /**
     * Subclassed implementation of redirected buildDeclarations() from abstract
     * superclass.
     *
     * @param primVals   The SAC value for this property
     * @param priority   Priority string for this value
     * @param important  True if author-marked important!
     * @param propName   property name
     * @param origin     The origin of the stylesheet; constant from {@link
     *      org.xhtmlrenderer.css.sheet.Stylesheet}, e.g. Stylesheet.AUTHOR
     * @return           Iterator of PropertyDeclarations for the shorthand
     *      margin property.
     */
    protected Iterator doBuildDeclarations( CSSPrimitiveValue[] primVals,
                                            String priority,
                                            boolean important,
                                            String propName,
                                            int origin ) {

        List declarations = new ArrayList();
        CSSPrimitiveValue primitive = null;
        CSSPrimitiveValue primitives[] = null;
        String names[] = null;

        switch ( primVals.length ) {
            case 1:
                primitive = primVals[0];
                primitives = new CSSPrimitiveValue[]{
                            primitive,
                            primitive,
                            primitive,
                            primitive};

                addProperties( declarations, primitives, ONE_TO_FOUR, origin, important );
                break;
            case 2:
                primitives = new CSSPrimitiveValue[]{
                            primVals[ 0 ],
                            primVals[ 0 ],
                            primVals[ 1 ],
                            primVals[ 1 ]};

                addProperties( declarations, primitives, TWO_TO_FOUR, origin, important );
                break;
            case 3:
                primitives = new CSSPrimitiveValue[]{
                            primVals[ 0 ],
                            primVals[ 1 ],
                            primVals[ 1 ],
                            primVals[ 2 ]};

                addProperties( declarations, primitives, THREE_TO_FOUR, origin, important );
                break;
            case 4:
                primitives = new CSSPrimitiveValue[]{
                            primVals[ 0 ],
                            primVals[ 1 ],
                            primVals[ 2 ],
                            primVals[ 3 ]};

                addProperties( declarations, primitives, FOUR_TO_FOUR, origin, important );
                break;
        }

        // CAREFUL: note that with steadyState parser impl, their value class impl
        // both primitive and value list interfaces! use getCssValueType(), not instanceof!!
        /*
         * if ( primVals.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE ) {
         * CSSPrimitiveValue primitive = (CSSPrimitiveValue)primVals;
         * CSSPrimitiveValue primitives[] = {
         * primitive,
         * primitive,
         * primitive,
         * primitive};
         * addProperties( declarations, primitives, ONE_TO_FOUR, origin, important );
         * } else {
         * // is a value list
         * CSSValueList vList = (CSSValueList)primVals;
         * // border-style explodes differently based on number of supplied values
         * CSSPrimitiveValue primitive = null;
         * CSSPrimitiveValue primitives[] = null;
         * String names[] = null;
         * switch ( vList.getLength() ) {
         * case 1:
         * // bug in CSSValue implementation! should be a primitive value
         * // not a list
         * throw new XRRuntimeException( "'border-style' property has only one value, but SAC parser marked it as a value list." );
         * case 2:
         * primitives = new CSSPrimitiveValue[]{
         * (CSSPrimitiveValue)vList.item( 0 ),
         * (CSSPrimitiveValue)vList.item( 0 ),
         * (CSSPrimitiveValue)vList.item( 1 ),
         * (CSSPrimitiveValue)vList.item( 1 )};
         * addProperties( declarations, primitives, TWO_TO_FOUR, origin, important );
         * break;
         * case 3:
         * primitives = new CSSPrimitiveValue[]{
         * (CSSPrimitiveValue)vList.item( 0 ),
         * (CSSPrimitiveValue)vList.item( 1 ),
         * (CSSPrimitiveValue)vList.item( 1 ),
         * (CSSPrimitiveValue)vList.item( 2 )};
         * addProperties( declarations, primitives, THREE_TO_FOUR, origin, important );
         * break;
         * case 4:
         * primitives = new CSSPrimitiveValue[]{
         * (CSSPrimitiveValue)vList.item( 0 ),
         * (CSSPrimitiveValue)vList.item( 1 ),
         * (CSSPrimitiveValue)vList.item( 2 ),
         * (CSSPrimitiveValue)vList.item( 3 )};
         * addProperties( declarations, primitives, FOUR_TO_FOUR, origin, important );
         * break;
         * }
         * }
         */
        return declarations.iterator();
    }

    /**
     * Returns the singleton instance.
     *
     * @return   See desc.
     */
    public static synchronized PropertyDeclarationFactory instance() {
        if ( _instance == null ) {
            _instance = new BorderStylePropertyDeclarationFactory();
        }
        return _instance;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2005/01/24 14:25:34  pdoubleya
 * Added to CVS.
 *
 *
 */

