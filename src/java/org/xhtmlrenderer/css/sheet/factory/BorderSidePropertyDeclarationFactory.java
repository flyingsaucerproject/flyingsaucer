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

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.Idents;
import org.xhtmlrenderer.css.value.FSCssValue;
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
        CSSName explodes[] = (CSSName[])PROP_EXPLODE.get( cssName );
        CSSPrimitiveValue primitives[] = null;
        CSSName names[] = null;

        if ( explodes == null ) {
            throw new XRRuntimeException( "Found no mapping for border side property named '" + cssName + "'." );
        }

        for ( int i = 0; i < primVals.length; i++ ) {
            CSSPrimitiveValue primitive = primVals[i];

            String val = primitive.getCssText().trim();
            CSSName matchedProp = matchPropName( val, explodes );
            primitives = new CSSPrimitiveValue[]{new FSCssValue( matchedProp, primitive )};
            names = new CSSName[]{matchedProp};
            addProperties( declarations, primitives, names, origin, important );
        }

        return declarations.iterator();
    }

    /**
     * Description of the Method
     *
     * @param val        PARAM
     * @param explodeTo  PARAM
     * @return           Returns
     */
    private CSSName matchPropName( String val, CSSName[] explodeTo ) {
        CSSName cssName = null;
        if ( Idents.looksLikeAColor( val ) ) {
            cssName = explodeTo[COLOR_IDX];
        } else if ( Idents.looksLikeABorderStyle( val ) ) {
            cssName = explodeTo[STYLE_IDX];
        } else {
            // HACK: as with background, we should go ahead and check this is a valid CSS length value (PWW 24-08-04)
            cssName = explodeTo[WIDTH_IDX];
        }
        return cssName;
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
        CSSName top[] = new CSSName[]{CSSName.BORDER_WIDTH_TOP, CSSName.BORDER_STYLE_TOP, CSSName.BORDER_COLOR_TOP};

        CSSName right[] = new CSSName[]{CSSName.BORDER_WIDTH_RIGHT, CSSName.BORDER_STYLE_RIGHT, CSSName.BORDER_COLOR_RIGHT};

        CSSName bottom[] = new CSSName[]{CSSName.BORDER_WIDTH_BOTTOM, CSSName.BORDER_STYLE_BOTTOM, CSSName.BORDER_COLOR_BOTTOM};

        CSSName left[] = new CSSName[]{CSSName.BORDER_WIDTH_LEFT, CSSName.BORDER_STYLE_LEFT, CSSName.BORDER_COLOR_LEFT};

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
 * Revision 1.3  2005/01/29 12:14:21  pdoubleya
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

