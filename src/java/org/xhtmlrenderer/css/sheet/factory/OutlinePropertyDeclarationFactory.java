/*
 * {{{ header & license
 * OutlinePropertyDeclarationFactory.java
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
 * A PropertyDeclarationFactory for CSS 2 "outline" shorthand property, instantiating
 * PropertyDeclarations; Singleton, use {@link #instance()}.
 *
 * @author   Patrick Wright
 */
public class OutlinePropertyDeclarationFactory extends AbstractPropertyDeclarationFactory {
    /** Singleton instance. */
    private static OutlinePropertyDeclarationFactory _instance;

    /** Constructor for the OutlinePropertyDeclarationFactory object */
    private OutlinePropertyDeclarationFactory() { }

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
        // outline shorthand can have color, image, repeat,
        // attachment, position in any order; so loop whatever's
        // provided and sniff for the value-type
        CSSPrimitiveValue primitive = null;
        CSSPrimitiveValue primitives[] = new CSSPrimitiveValue[1];
        String names[] = new String[1];

        for ( int i=0; i < primVals.length; i++ ) {
            primitive = primVals[ i ];

            String val = primitive.getCssText().trim();
            String expPropName = "";
            if ( Idents.looksLikeAColor( val ) ) {
                expPropName = CSSName.OUTLINE_COLOR;
                primitive = new FSCssValue(CSSName.OUTLINE_COLOR, primitive);
            } else if ( Idents.looksLikeABorderStyle( val ) ) {
                expPropName = CSSName.OUTLINE_STYLE;
            } else {
                // HACK: as with Background, assume it is a length value
                expPropName = CSSName.OUTLINE_WIDTH;
            }
            names[0] = expPropName;
            primitives[0] = primitive;
            addProperties( declarations, primitives, names, origin, important );
        }
        
        // CAREFUL: note that with steadyState parser impl, their value class impl
        // both primitive and value list interfaces! use getCssValueType(), not instanceof!!
        /*if ( primVals.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE ) {
            addProperties( declarations, new CSSPrimitiveValue[]{(CSSPrimitiveValue)primVals}, new String[]{propName}, origin, important );
        } else {
            // is a value list
            CSSValueList vList = (CSSValueList)primVals;

            // outline shorthand can have color, image, repeat,
            // attachment, position in any order; so loop whatever's
            // provided and sniff for the value-type
            CSSPrimitiveValue primitive = null;
            CSSPrimitiveValue primitives[] = new CSSPrimitiveValue[1];
            String names[] = new String[1];

            CSSPrimitiveValue bgPosPrimitive = null;
            StringBuffer bgPos = null;
            for ( int i = 0, len = vList.getLength(); i < len; i++ ) {
                primitive = (CSSPrimitiveValue)vList.item( i );

                String val = primitive.getCssText().trim();
                String expPropName = "";
                if ( Idents.looksLikeAColor( val ) ) {
                    expPropName = CSSName.OUTLINE_COLOR;
                    primitive = new FSCssValue(CSSName.OUTLINE_COLOR, primitive);
                } else if ( Idents.looksLikeABorderStyle( val ) ) {
                    expPropName = CSSName.OUTLINE_STYLE;
                } else {
                    // HACK: as with Background, assume it is a length value
                    expPropName = CSSName.OUTLINE_WIDTH;
                }
                names[0] = expPropName;
                primitives[0] = primitive;
                addProperties( declarations, primitives, names, origin, important );
            }// loop values element list
        }// if a primitive */
        return declarations.iterator();
    }

    /**
     * Returns the singleton instance.
     *
     * @return   Returns
     */
    public static synchronized PropertyDeclarationFactory instance() {
        if ( _instance == null ) {
            _instance = new OutlinePropertyDeclarationFactory();
        }
        return _instance;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2005/01/24 14:25:36  pdoubleya
 * Added to CVS.
 *
 *
 */

