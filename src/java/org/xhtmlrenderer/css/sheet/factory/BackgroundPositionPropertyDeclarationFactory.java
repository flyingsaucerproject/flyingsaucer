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
import org.xhtmlrenderer.css.constants.Idents;
import org.xhtmlrenderer.css.value.FSCssValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A PropertyDeclarationFactory for CSS 2 shorthand properties that should not
 * be exploded, specifically background-position and font-family, instantiating
 * PropertyDeclarations; Singleton, use {@link #instance()}.
 *
 * @author Patrick Wright
 */
public class BackgroundPositionPropertyDeclarationFactory extends AbstractPropertyDeclarationFactory {
    /**
     * Singleton instance.
     */
    private static BackgroundPositionPropertyDeclarationFactory _instance;

    /**
     * Constructor for the DefaultPropertyDeclarationFactory object
     */
    private BackgroundPositionPropertyDeclarationFactory() {
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

        StringBuffer pos = new StringBuffer();
        for (int i = 0; i < primVals.length; i++) {
            CSSPrimitiveValue primVal = primVals[i];
            pos.append(primVal.getCssText().trim() + " ");
        }
        pos.deleteCharAt(pos.length() - 1);// remove spc

        String val = pos.toString().trim();

        val = canonicalizeValue(val);

        FSCssValue fsCssValue = new FSCssValue(primVals[0], val);
        List declarations = new ArrayList(1);

        declarations.add(newPropertyDeclaration(cssName, fsCssValue, origin, important));
        return declarations.iterator();
    }

    static String canonicalizeValue(String val) {
        // handle for single value--first will be taken as horizontal; as per CSS spec
        // if there is no " ", then we have a single value
        if (val.indexOf(" ") == -1) {
            // check that the single value is a length
            if (Idents.looksLikeALength(val)) {
                val += " 50%";
            } else {
                val += " center";
            }
        }
        //canonicalize
        String[] vals = val.split(" ");
        if (vals[0].equals("top")
                || vals[0].equals("bottom")
                || vals[1].equals("left")
                || vals[1].equals("right")) {
            val = vals[0];
            vals[0] = vals[1];
            vals[1] = val;
        }
        //interpret idents
        if (vals[0].equals("left"))
            vals[0] = "0%";
        else if (vals[0].equals("center"))
            vals[0] = "50%";
        else if (vals[0].equals("right")) vals[0] = "100%";
        if (vals[1].equals("top"))
            vals[1] = "0%";
        else if (vals[1].equals("center"))
            vals[1] = "50%";
        else if (vals[1].equals("bottom")) vals[1] = "100%";
        return vals[0] + " " + vals[1];
    }

    /**
     * Returns the singleton instance.
     *
     * @return Returns
     */
    public static synchronized PropertyDeclarationFactory instance() {
        if (_instance == null) {
            _instance = new BackgroundPositionPropertyDeclarationFactory();
        }
        return _instance;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.6  2005/06/02 23:38:29  tobega
 * Now handles background-position idents
 *
 * Revision 1.5  2005/05/08 13:02:37  tobega
 * Fixed a bug whereby styles could get lost for inline elements, notably if root element was inline. Did a few other things which probably has no importance at this moment, e.g. refactored out some unused stuff.
 *
 * Revision 1.4  2005/01/29 20:24:25  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.3  2005/01/29 12:14:20  pdoubleya
 * Removed priority as a parameter, added alternate build when only CSSValue is available; could be used in a SAC DocumentHandler after the CSSValue is initialized from a property.
 *
 * Revision 1.2  2005/01/24 19:00:57  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.1  2005/01/24 14:25:33  pdoubleya
 * Added to CVS.
 *
 *
 */

