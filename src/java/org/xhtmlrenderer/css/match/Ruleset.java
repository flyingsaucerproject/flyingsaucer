/*
 * Ruleset.java
 * Copyright (c) 2004 Torbjörn Gannholm
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
 *
 */

package org.xhtmlrenderer.css.match;



/**
 * Rulesets should be created by the CSS parser. A list of Rulesets make up a
 * CSS. A ruleset contains a list of selectors and a list of property
 * declarations.
 *
 * @author   Torbjörn Gannholm
 */
public class Ruleset {

    /** Description of the Field */
    private java.util.List selectors = new java.util.ArrayList();
    /** Description of the Field */
    private Object styleDeclaration;


    /** Creates a new instance of Ruleset */
    public Ruleset() { }

    /**
     * Description of the Method
     *
     * @param axis         PARAM
     * @param elementName  PARAM
     * @return             Returns
     */
    public Selector createSelector( int axis, String elementName ) {
        Selector s = new Selector( this, axis, elementName );
        selectors.add( s );
        return s;
    }

    /**
     * Leave parameter as Object, tests of logic rely on it. Refactor!?
     *
     * @param declaration  The new styleDeclaration value
     */
    public void setStyleDeclaration( Object declaration ) {
        //declarations.add(declaration);
        styleDeclaration = declaration;
    }

    /**
     * TODO: returns the list of property declarations of this ruleset This
     * method's signature may change
     *
     * @return   The styleDeclaration value
     */
    /*
     * public java.util.List getPropertyDeclarations() {
     * return declarations;
     * }
     */
    public Object getStyleDeclaration() {
        return styleDeclaration;
    }

    /**
     * Gets the selectors attribute of the Ruleset object
     *
     * @return   The selectors value
     */
    public java.util.List getSelectors() {
        return selectors;
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2004/10/23 13:29:06  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

