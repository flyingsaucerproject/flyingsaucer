/*
 * {{{ header & license
 * BorderSidePropertyDeclarationFactory.java
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
import org.xhtmlrenderer.css.constants.Idents;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.value.FSCssValue;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;


/**
 * A PropertyDeclarationFactory for CSS 2 "border-top/right/bottom/left" shorthand
 * properties, instantiating PropertyDeclarations; Singleton, use {@link
 * #instance()}.
 *
 * @author   Patrick Wright
 */
public class BorderSidePropertyDeclarationFactory extends AbstractPropertyDeclarationFactory {
    /** Singleton instance. */
    private static BorderSidePropertyDeclarationFactory _instance;

    /** TODO--used in property explosion */
    private final static Map PROP_EXPLODE;

    /** Description of the Field */
    private final static int WIDTH_IDX;
    /** Description of the Field */
    private final static int STYLE_IDX;
    /** Description of the Field */
    private final static int COLOR_IDX;

    /** Default constructor; don't use, use instance() instead. */
    private BorderSidePropertyDeclarationFactory() { }

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
        String explodes[] = (String[])PROP_EXPLODE.get( propName );
        CSSPrimitiveValue primitives[] = null;
        String names[] = null;

        if ( explodes == null ) {
            throw new XRRuntimeException( "Found no mapping for border side property named '" + propName + "'." );
        }

        for ( int i = 0; i < primVals.length; i++ ) {
            CSSPrimitiveValue primitive = primVals[i];

            String val = primitive.getCssText().trim();
            String matchedProp = matchPropName( val, explodes );
            primitives = new CSSPrimitiveValue[]{new FSCssValue( matchedProp, primitive )};
            names = new String[]{matchedProp};
            addProperties( declarations, primitives, names, origin, important );
        }

        /*
         * // CAREFUL: note that with steadyState parser impl, their value class impl
         * // both primitive and value list interfaces! use getCssValueType(), not instanceof!!
         * if ( primVals.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE ) {
         * String val = ((CSSPrimitiveValue)primVals).getCssText().trim();
         * String matchedProp = matchPropName(val, explodes);
         * primitives = new CSSPrimitiveValue[]{new FSCssValue(matchedProp, (CSSPrimitiveValue)primVals)};
         * names = new String[]{matchedProp};
         * addProperties( declarations, primitives, names, origin, important );
         * } else {
         * // is a value list
         * CSSValueList vList = (CSSValueList)primVals;
         * // border-style explodes differently based on number of supplied values
         * CSSPrimitiveValue primitive = null;
         * for ( int i = 0, len = vList.getLength(); i < len; i++ ) {
         * primitive = (CSSPrimitiveValue)vList.item( i );
         * String val = primitive.getCssText().trim();
         * String matchedProp = matchPropName(val, explodes);
         * primitives = new CSSPrimitiveValue[]{new FSCssValue(matchedProp, (CSSPrimitiveValue)primitive)};
         * names = new String[]{matchedProp};
         * addProperties( declarations, primitives, names, origin, important );
         * }
         * }
         */
        return declarations.iterator();
    }

    /**
     * Description of the Method
     *
     * @param val        PARAM
     * @param explodeTo  PARAM
     * @return           Returns
     */
    private String matchPropName( String val, String[] explodeTo ) {
        String propName = "";
        if ( Idents.looksLikeAColor( val ) ) {
            propName = explodeTo[COLOR_IDX];
        } else if ( Idents.looksLikeABorderStyle( val ) ) {
            propName = explodeTo[STYLE_IDX];
        } else {
            // HACK: as with background, we should go ahead and check this is a valid CSS length value (PWW 24-08-04)
            propName = explodeTo[WIDTH_IDX];
        }
        return propName;
    }

    /**
     * Returns the singleton instance.
     *
     * @return   See desc.
     */
    public static synchronized PropertyDeclarationFactory instance() {
        if ( _instance == null ) {
            _instance = new BorderSidePropertyDeclarationFactory();
        }
        return _instance;
    }
    static {
        WIDTH_IDX = 0;
        STYLE_IDX = 1;
        COLOR_IDX = 2;

        PROP_EXPLODE = new HashMap();
        String top[] = new String[]{CSSName.BORDER_WIDTH_TOP, CSSName.BORDER_STYLE_TOP, CSSName.BORDER_COLOR_TOP};

        String right[] = new String[]{CSSName.BORDER_WIDTH_RIGHT, CSSName.BORDER_STYLE_RIGHT, CSSName.BORDER_COLOR_RIGHT};

        String bottom[] = new String[]{CSSName.BORDER_WIDTH_BOTTOM, CSSName.BORDER_STYLE_BOTTOM, CSSName.BORDER_COLOR_BOTTOM};

        String left[] = new String[]{CSSName.BORDER_WIDTH_LEFT, CSSName.BORDER_STYLE_LEFT, CSSName.BORDER_COLOR_LEFT};

        PROP_EXPLODE.put( CSSName.BORDER_TOP_SHORTHAND, top );
        PROP_EXPLODE.put( CSSName.BORDER_RIGHT_SHORTHAND, right );
        PROP_EXPLODE.put( CSSName.BORDER_BOTTOM_SHORTHAND, bottom );
        PROP_EXPLODE.put( CSSName.BORDER_LEFT_SHORTHAND, left );
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

