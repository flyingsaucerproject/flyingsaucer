/*
 * {{{ header & license
 * OutlinePropertyDeclarationFactory.java
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
import org.xhtmlrenderer.css.constants.Idents;
import org.xhtmlrenderer.css.value.FSCssValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A PropertyDeclarationFactory for CSS 2 "outline" shorthand property,
 * instantiating PropertyDeclarations; Singleton, use {@link #instance()}.
 *
 * @author Patrick Wright
 */
public class OutlinePropertyDeclarationFactory extends AbstractPropertyDeclarationFactory {
    /**
     * Singleton instance.
     */
    private static OutlinePropertyDeclarationFactory _instance;

    /**
     * Constructor for the OutlinePropertyDeclarationFactory object
     */
    private OutlinePropertyDeclarationFactory() {
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

        List declarations = new ArrayList();
        // outline shorthand can have color, image, repeat,
        // attachment, position in any order; so loop whatever's
        // provided and sniff for the value-type
        CSSPrimitiveValue primitive = null;
        CSSPrimitiveValue primitives[] = new CSSPrimitiveValue[1];
        CSSName names[] = new CSSName[1];

        for (int i = 0; i < primVals.length; i++) {
            primitive = primVals[i];

            String val = primitive.getCssText().trim();
            CSSName expPropName = null;
            if (Idents.looksLikeAColor(val)) {
                expPropName = CSSName.OUTLINE_COLOR;
                primitive = new FSCssValue(primitive);
            } else if (Idents.looksLikeABorderStyle(val)) {
                expPropName = CSSName.OUTLINE_STYLE;
            } else {
                // HACK: as with Background, assume it is a length value
                expPropName = CSSName.OUTLINE_WIDTH;
            }
            names[0] = expPropName;
            primitives[0] = primitive;
            addProperties(declarations, primitives, names, origin, important);
        }

        return declarations.iterator();
    }

    /**
     * Returns the singleton instance.
     *
     * @return Returns
     */
    public static synchronized PropertyDeclarationFactory instance() {
        if (_instance == null) {
            _instance = new OutlinePropertyDeclarationFactory();
        }
        return _instance;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2005/05/08 13:02:37  tobega
 * Fixed a bug whereby styles could get lost for inline elements, notably if root element was inline. Did a few other things which probably has no importance at this moment, e.g. refactored out some unused stuff.
 *
 * Revision 1.4  2005/01/29 20:19:25  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.3  2005/01/29 12:14:21  pdoubleya
 * Removed priority as a parameter, added alternate build when only CSSValue is available; could be used in a SAC DocumentHandler after the CSSValue is initialized from a property.
 *
 * Revision 1.2  2005/01/24 19:00:59  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.1  2005/01/24 14:25:36  pdoubleya
 * Added to CVS.
 *
 *
 */

