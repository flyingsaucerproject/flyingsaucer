/*
 * {{{ header & license
 * DefaultPropertyDeclarationFactory.java
 * Copyright (c) 2004, 2005 Patrick Wright
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

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.value.FSCssValue;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A PropertyDeclarationFactory for CSS 2 non-shorthand properties that don't
 * require special handling, instantiating PropertyDeclarations; Singleton, use
 * {@link #instance()}.
 *
 * @author Patrick Wright
 */
public class DefaultPropertyDeclarationFactory extends AbstractPropertyDeclarationFactory {
    /**
     * Singleton instance.
     */
    private static DefaultPropertyDeclarationFactory _instance;

    /**
     * Constructor for the DefaultPropertyDeclarationFactory object
     */
    private DefaultPropertyDeclarationFactory() {
    }

    /**
     * Subclassed implementation of redirected buildDeclarations() from abstract
     * superclass.
     *
     * @param primVals  The SAC value for this property
     * @param important True if author-marked important!
     * @param cssName   property name
     * @param origin    The origin of the stylesheet; constant from {@link
     *                  org.xhtmlrenderer.css.sheet.Stylesheet}, e.g. Stylesheet.AUTHOR
     * @return Iterator of PropertyDeclarations for the shorthand
     *         margin property.
     */
    protected Iterator doBuildDeclarations(CSSPrimitiveValue[] primVals,
                                           boolean important,
                                           CSSName cssName,
                                           int origin) {

        if (primVals.length > 1) {
            throw new XRRuntimeException("Tried to use " + this.getClass().getName() +
                    " to create PropertyDeclarations for '" + cssName + "', but the" +
                    " CSS style information is not primitive--is a list of values." +
                    " Should be handled by a shorthand property factory.");
        }
        FSCssValue fsCssValue = new FSCssValue(primVals[0]);
        List declarations = new ArrayList(1);
        declarations.add(newPropertyDeclaration(cssName, fsCssValue, origin, important));
        return declarations.iterator();
    }

    /**
     * Returns the singleton instance.
     *
     * @return Returns
     */
    public static synchronized PropertyDeclarationFactory instance() {
        if (_instance == null) {
            _instance = new DefaultPropertyDeclarationFactory();
        }
        return _instance;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.7  2006/05/08 21:36:02  pdoubleya
 * Log and skip properties we can't parse into declarations...
 *
 * Revision 1.6  2005/05/08 13:02:37  tobega
 * Fixed a bug whereby styles could get lost for inline elements, notably if root element was inline. Did a few other things which probably has no importance at this moment, e.g. refactored out some unused stuff.
 *
 * Revision 1.5  2005/02/02 12:12:25  pdoubleya
 * .
 *
 * Revision 1.4  2005/01/29 20:21:04  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.3  2005/01/29 12:14:21  pdoubleya
 * Removed priority as a parameter, added alternate build when only CSSValue is available; could be used in a SAC DocumentHandler after the CSSValue is initialized from a property.
 *
 * Revision 1.2  2005/01/24 19:00:58  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.1  2005/01/24 14:25:35  pdoubleya
 * Added to CVS.
 *
 *
 */

