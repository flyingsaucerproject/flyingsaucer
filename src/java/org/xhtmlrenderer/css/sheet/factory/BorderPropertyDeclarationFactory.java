/*
 * {{{ header & license
 * BorderPropertyDeclarationFactory.java
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
import org.xhtmlrenderer.css.constants.Idents;


/**
 * A PropertyDeclarationFactory for CSS 2 "border" shorthand property, instantiating
 * XRProperties.
 *
 * @author   Patrick Wright
 */
public class BorderPropertyDeclarationFactory extends AbstractPropertyDeclarationFactory {
    /** Singleton instance. */
    private static BorderPropertyDeclarationFactory _instance;

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

    /** Constructor for the BorderPropertyDeclarationFactory object */
    private BorderPropertyDeclarationFactory() { }

    /**
     * * Returns the singleton instance.
     *
     * @return   Returns
     */
    public static synchronized PropertyDeclarationFactory instance() {
        if ( _instance == null ) {
            _instance = new BorderPropertyDeclarationFactory();
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

    /**
     * Internal version of {@link #buildDeclarations(org.w3c.dom.css.CSSStyleDeclaration, String, int)}, with value,
     * priority and important already extracted for easy access. Override this in subclass to implement.
     *
     * @param primVals  The SAC value for this property
     * @param priority  Priority string for this value
     * @param important True if author-marked important!
     * @param propName  property name
     * @param origin    The origin of the stylesheet; constant from {@link org.xhtmlrenderer.css.sheet.Stylesheet}, e.g.
     *                  Stylesheet.AUTHOR
     * @return Iterator of {@link org.xhtmlrenderer.css.sheet.PropertyDeclaration} for the shorthand margin property.
     */
    protected Iterator doBuildDeclarations(CSSPrimitiveValue[] primVals, String priority, boolean important, String propName, int origin) {
        List declarations = new ArrayList();

        // border explodes differently based on number of supplied values
        // but values for color, style, width can be given in any order
        // loop over the ones given and sniff them out to see if they
        // look like color, style, width, then apply to all four sides
        CSSPrimitiveValue primitive = null;
        CSSPrimitiveValue[] primitives = new CSSPrimitiveValue[4];

        String sides[] = null;
        for ( int i = 0, len = primVals.length; i < len; i++ ) {
            primitive = primVals[i];
            String val = primitive.getCssText();
            if ( Idents.looksLikeAColor( val ) ) {
                sides = LISTS[COLOR_IDX];
            } else if ( Idents.looksLikeABorderStyle( val ) ) {
                sides = LISTS[STYLE_IDX];
            } else {
                // it is a length
                sides = LISTS[WIDTH_IDX];
            }
            primitives[0] = primitive;
            primitives[1] = primitive;
            primitives[2] = primitive;
            primitives[3] = primitive;
            addProperties( declarations, primitives, sides, origin, important );
        }
        return declarations.iterator();
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2005/01/24 14:25:34  pdoubleya
 * Added to CVS.
 *
 *
 */
