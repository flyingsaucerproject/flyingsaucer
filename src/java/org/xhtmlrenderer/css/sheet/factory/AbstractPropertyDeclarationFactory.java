/*
 * {{{ header & license
 * AbstractPropertyDeclarationFactory.java
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


/**
 * A abstract class for {@link PropertyDeclarationFactory}; you can extend this to
 * create a basic PDFactory. PDFactories are expected to be Singletons, which
 * this class can't create for you; define an <code>instance()</code> method that returns a
 * PropertyDeclarationFactory in your subclass. If you
 *
 * @author   Patrick Wright
 */
public abstract class AbstractPropertyDeclarationFactory implements PropertyDeclarationFactory {
    /**
     * Explodes a single property declaration in raw form (as provided by a SAC
     * parser) into one or more {@link PropertyDeclaration} instances. Normally one
     * would expect just on {@link PropertyDeclaration} for a CSS property, but
     * shorthand declarations are resolved into many individual property
     * assignments. The exact form of the explosion is left to the implementing
     * class.
     *
     * @param style     The {@link org.w3c.dom.css.CSSStyleDeclaration} from the
     *      SAC parser.
     * @param cssName  The String property name for the property to explode.
     * @param origin    PARAM
     * @return          Iterator of one or more PropertyDeclaration instances
     *      representing the exploded values.
     */
    public final Iterator buildDeclarations( CSSStyleDeclaration style,
                                             CSSName cssName,
                                             int origin ) {

        Object attr[] = extractStyleAttributes( style, cssName );
        CSSValue cssValue = (CSSValue)attr[0];
        boolean important = ( (Boolean)attr[1] ).booleanValue();

        // split out declations into multiple primitive values.
        // our CSSValue may be primitive or may be a list
        // this split allows our subclasses to avoid checking themselves
        // all they get is an array of primitives
        
        // declare array for primitive values, unscoped
        CSSPrimitiveValue pvalues[] = null;

        // primitive or list
        if ( cssValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE ) {
            pvalues = new CSSPrimitiveValue[1];
            pvalues[0] = (CSSPrimitiveValue)cssValue;
        } else {
            CSSValueList list = (CSSValueList)cssValue;
            pvalues = new CSSPrimitiveValue[list.getLength()];
            for (int i = 0; i < list.getLength(); i++) {
                CSSPrimitiveValue pvalue = (CSSPrimitiveValue)list.item(i);
                pvalues[i] = pvalue;
            }
        }
        
        return doBuildDeclarations( pvalues, important, cssName, origin );
    }
    
    public final Iterator buildDeclarations( CSSValue cssValue, CSSName cssName, int origin, boolean important ) {
        CSSPrimitiveValue pvalues[] = null;

        // primitive or list
        if ( cssValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE ) {
            pvalues = new CSSPrimitiveValue[1];
            pvalues[0] = (CSSPrimitiveValue)cssValue;
        } else {
            CSSValueList list = (CSSValueList)cssValue;
            pvalues = new CSSPrimitiveValue[list.getLength()];
            for (int i = 0; i < list.getLength(); i++) {
                CSSPrimitiveValue pvalue = (CSSPrimitiveValue)list.item(i);
                pvalues[i] = pvalue;
            }
        }
        
        //XRLog.cssParse("Building declarations for: " + cssName);
        return doBuildDeclarations( pvalues, important, cssName, origin );
    }
    

    /**
     * Internal version of {@link #buildDeclarations(org.w3c.dom.css.CSSStyleDeclaration, CSSName, int)},
     *  with value, priority and important already extracted for easy access. Override this in subclass to
     * implement.
     *
     * @param primVals   The SAC value for this property
     * @param priority   Priority string for this value
     * @param important  True if author-marked important!
     * @param cssName   property name
     * @param origin     The origin of the stylesheet; constant from {@link
     *      org.xhtmlrenderer.css.sheet.Stylesheet}, e.g. Stylesheet.AUTHOR
     * @return           Iterator of {@link PropertyDeclaration} for the shorthand
     *      margin property.
     */
    protected abstract Iterator doBuildDeclarations( CSSPrimitiveValue[] primVals,
                                                     boolean important,
                                                     CSSName cssName,
                                                     int origin );

    /**
     * Creates a new PropertyDeclaration. This is a utility method.
     *
     * @param cssName   Name of the CSS property to create.
     * @param primitive  The CSSPrimitiveValue
     * @param origin     The origin of the stylesheet; constant from {@link
     *      org.xhtmlrenderer.css.sheet.Stylesheet}, e.g. Stylesheet.AUTHOR
     * @param important  True if the CSS declaration marks the property as
     *      important
     * @return           The new PropertyDeclaration representing this property.
     */
    protected PropertyDeclaration newPropertyDeclaration( CSSName cssName,
                                                          CSSPrimitiveValue primitive,
                                                          int origin,
                                                          boolean important ) {

        return new PropertyDeclaration(cssName, primitive, important, origin );
    }

    /**
     * Creates a group of PropertyDeclarations based on fixed arrays passed in,
     * and adds to the list you provide. This basically just saves some looping
     * in the client call.
     *
     * @param declarations  The list to add to.
     * @param primitives    Array of CSSPrimitiveValues
     * @param cssNames         Array of properties, matching the primitive values
     *      array.
     * @param origin        The origin of the Stylesheet, constant from
     *      Stylesheet class e.g. Stylesheet.AUTHOR
     * @param important     True if the original shorthand property is marked
     */
    protected void addProperties( List declarations,
                                  CSSPrimitiveValue[] primitives,
                                  CSSName[] cssNames,
                                  int origin,
                                  boolean important ) {

        for ( int i = 0; i < primitives.length; i++ ) {
            declarations.add(
                        newPropertyDeclaration(
                        cssNames[i],
                        primitives[i],
                        origin,
                        important ) );
        }
        return;
    }


    /**
     * Convenience method to extract the {@link CSSValue}, priority, and importance from
     * a SAC {@link CSSStyleDeclaration}.
     *
     * @param style     The {@link org.w3c.dom.css.CSSStyleDeclaration} from which to extract the
     *      attributes.
     * @param cssName  Name of the property for which to get the attributes.
     * @return          Object array with CSSValue, String priority and Boolean
     *      importance in fixed sequence: always 3, always that sequence.
     */
    protected Object[] extractStyleAttributes( CSSStyleDeclaration style, CSSName cssName ) {
        CSSValue cssValue = style.getPropertyCSSValue( cssName.toString() );
        Boolean important = Boolean.valueOf(
                    style.getPropertyPriority( cssName.toString() ).compareToIgnoreCase( "important" ) == 0 );
        return new Object[]{cssValue, important};
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2005/01/29 12:14:20  pdoubleya
 * Removed priority as a parameter, added alternate build when only CSSValue is available; could be used in a SAC DocumentHandler after the CSSValue is initialized from a property.
 *
 * Revision 1.3  2005/01/24 19:00:57  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.2  2005/01/24 14:53:11  pdoubleya
 * Comments referred to old class.
 *
 * Revision 1.1  2005/01/24 14:25:33  pdoubleya
 * Added to CVS.
 *
 *
 */

