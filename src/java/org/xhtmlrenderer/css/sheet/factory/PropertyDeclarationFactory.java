/*
 * {{{ header & license
 * PropertyDeclarationFactory.java
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

import java.util.Iterator;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.xhtmlrenderer.css.constants.CSSName;


/**
 * Interface for factories that build PropertyDeclarations from info gathered in
 * a SAC CSSStyleDeclaration. There will normally be one factory per shorthand
 * property type (like "border"), and one generic factory.
 *
 * @author   Patrick Wright
 */
public interface PropertyDeclarationFactory {
    /**
     * Explodes a single property declaration in raw form (as provided by a SAC
     * parser) into one or more PropertyDeclaration instances. Normally one
     * would expect just on PropertyDeclaration for a CSS property, but
     * shorthand declarations are resolved into many individual property
     * assignments. The exact form of the explosion is left to the implementing
     * class.
     *
     * @param style    The {@link org.w3c.dom.css.CSSStyleDeclaration} from the
     *      SAC parser.
     * @param cssName  The String property name for the property to explode.
     * @param origin   PARAM
     * @return         Iterator of one or more PropertyDeclaration instances
     *      representing the exploded values.
     */
    Iterator buildDeclarations( CSSStyleDeclaration style, CSSName cssName, int origin );

    /**
     * Description of the Method
     *
     * @param cssValue   PARAM
     * @param cssName    PARAM
     * @param origin     PARAM
     * @param important  PARAM
     * @return           Returns
     */
    Iterator buildDeclarations( CSSValue cssValue, CSSName cssName, int origin, boolean important );
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2005/01/29 20:19:25  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.4  2005/01/29 12:14:22  pdoubleya
 * Removed priority as a parameter, added alternate build when only CSSValue is available; could be used in a SAC DocumentHandler after the CSSValue is initialized from a property.
 *
 * Revision 1.3  2005/01/24 19:01:00  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.2  2005/01/24 14:54:53  pdoubleya
 * Comments referred to old class.
 *
 * Revision 1.1  2005/01/24 14:25:36  pdoubleya
 * Added to CVS.
 *
 *
 */

