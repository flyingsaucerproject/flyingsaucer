/*
 * {{{ header & license
 * FontPropertyDeclarationFactory.java
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


/**
 * A PropertyDeclarationFactory for CSS 2 "font" shorthand property, instantiating
 * PropertyDeclarations; Singleton, use {@link #instance()}.
 *
 * @author   Patrick Wright
 */
public class FontPropertyDeclarationFactory extends AbstractPropertyDeclarationFactory {
    /** Singleton instance. */
    private static FontPropertyDeclarationFactory _instance;

    /** Constructor for the FontPropertyDeclarationFactory object */
    private FontPropertyDeclarationFactory() { }

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
                                            String priority,
                                            boolean important,
                                            CSSName cssName,
                                            int origin ) {

        List declarations = new ArrayList();
        List families = new ArrayList();
        CSSPrimitiveValue primitive = null;
        CSSPrimitiveValue primitives[] = new CSSPrimitiveValue[1];
        CSSPrimitiveValue familyPrimitive = null;
        CSSName names[] = new CSSName[1];
        Boolean hasSize = Boolean.valueOf(false);
        
        for ( int i=0; i < primVals.length; i++ ) {
            primitive = primVals[i];
            String val = primitive.getCssText().trim();

            Object[] ret = parseSingle( val, primitive, hasSize, families );
            names[0] = (CSSName)ret[0];
            primitives[0] = (CSSPrimitiveValue)ret[1];
            
            hasSize = (Boolean)ret[2];
            
            // if family was found, we add outside this loop 
            // once we have them all
            if ( ret[3] != null ) {
                if ( familyPrimitive == null  ) {
                    familyPrimitive = (CSSPrimitiveValue)ret[3];
                }
                continue;
            }

            addProperties( declarations, primitives, names, origin, important );
        }
        
        if ( families.size() > 0 ) {
            StringBuffer sb = new StringBuffer();
            String sep = "";
            Iterator iter = families.iterator();
            while ( iter.hasNext() ) {
                sb.append( sep ).append( iter.next() );
                sep = ", ";
            }
            
            names[0] = CSSName.FONT_FAMILY;
            primitives[0] = new FSCssValue( CSSName.FONT_FAMILY, familyPrimitive, sb.toString());
            addProperties( declarations, primitives, names, origin, important );
        }

        return declarations.iterator();
    }
    
    private Object[] parseSingle( String val, CSSPrimitiveValue primitive, Boolean hasSize, List families ) {
        CSSPrimitiveValue familyPrimitive = null;
        CSSName expPropName = null;
        if ( Idents.looksLikeAFontStyle( val ) ) {
            expPropName = CSSName.FONT_STYLE;
        } else if ( Idents.looksLikeAFontVariant( val ) ) {
            expPropName = CSSName.FONT_VARIANT;
        } else if ( Idents.looksLikeAFontWeight( val ) ) {
            expPropName = CSSName.FONT_WEIGHT;
        } else if ( !hasSize.booleanValue() && Idents.looksLikeAFontSize( val ) ) {
            expPropName = CSSName.FONT_SIZE;
            hasSize = Boolean.valueOf(true);
        } else if ( hasSize.booleanValue() && Idents.looksLikeALineHeight( val ) ) {
            expPropName = CSSName.LINE_HEIGHT;
        } else {
            // HACK: assume it is a font-family
            families.add( val );
            familyPrimitive = primitive;
        }
        return new Object[]{ expPropName, primitive, hasSize, familyPrimitive };
    }

    /**
     * Returns the singleton instance.
     *
     * @return   Returns
     */
    public static synchronized PropertyDeclarationFactory instance() {
        if ( _instance == null ) {
            _instance = new FontPropertyDeclarationFactory();
        }
        return _instance;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2005/01/24 19:00:59  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.1  2005/01/24 14:25:35  pdoubleya
 * Added to CVS.
 *
 *
 */

