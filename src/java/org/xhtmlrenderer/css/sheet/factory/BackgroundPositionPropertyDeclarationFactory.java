/*
 * {{{ header & license
 * DefaultPropertyDeclarationFactory.java
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
 * A PropertyDeclarationFactory for CSS 2 shorthand properties that should not be exploded,
 * specifically background-position and font-family,
 * instantiating PropertyDeclarations; Singleton, use {@link
 * #instance()}.
 *
 * @author   Patrick Wright
 */
public class BackgroundPositionPropertyDeclarationFactory extends AbstractPropertyDeclarationFactory {
    /** Singleton instance. */
    private static BackgroundPositionPropertyDeclarationFactory _instance;

    /** Constructor for the DefaultPropertyDeclarationFactory object */
    private BackgroundPositionPropertyDeclarationFactory() { }

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

        StringBuffer pos = new StringBuffer();
        for (int i = 0; i < primVals.length; i++) {
            CSSPrimitiveValue primVal = primVals[i];
            pos.append(primVal.getCssText().trim() + " ");
        }
        pos.deleteCharAt(pos.length() - 1); // remove spc

        // CLEAN: don't convert to ident until we have a DerivedValue
        // String val = Idents.convertIdent(CSSName.BACKGROUND_POSITION, pos.toString().trim());
        String val = pos.toString().trim();

        // handle for single value--first will be taken as horizontal; as per CSS spec
        // if there is no " ", then we have a single value
        if (val.indexOf(" ") == -1) {
            // check that the single value is a length
            if ( Idents.looksLikeALength(val))
                val += " 50%";
        }

        FSCssValue fsCssValue = new FSCssValue( propName, primVals[0], val );
        List declarations = new ArrayList( 1 );
        declarations.add( newPropertyDeclaration( propName, fsCssValue, origin, important ) );
        return declarations.iterator();
    }

    /**
     * Returns the singleton instance.
     *
     * @return   Returns
     */
    public static synchronized PropertyDeclarationFactory instance() {
        if ( _instance == null ) {
            _instance = new BackgroundPositionPropertyDeclarationFactory();
        }
        return _instance;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2005/01/24 14:25:33  pdoubleya
 * Added to CVS.
 *
 *
 */

