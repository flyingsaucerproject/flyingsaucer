/*
 *
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
 * Rulesets should be created by the CSS parser. A list of Rulesets make up a CSS.
 *
 * A ruleset contains a list of selectors and a list of property declarations.
 *
 * @author  Torbjörn Gannholm
 */
public class Ruleset {

    
    /** Creates a new instance of Ruleset */
    public Ruleset() {
    }
    
    /** TODO: returns the list of property declarations of this ruleset
     *  This method's signature may change
     */
    /*public java.util.List getPropertyDeclarations() {
        return declarations;
    }*/
    public Object getStyleDeclaration() {
        return styleDeclaration;
    }
    
    /** Leave parameter as Object, tests of logic rely on it. Refactor!? */
    public void setStyleDeclaration(Object declaration) {
        //declarations.add(declaration);
        styleDeclaration = declaration;
    }
    
    public Selector createSelector(int axis, String elementName) {
        Selector s = new Selector(this, axis, elementName);
        selectors.add(s);
        return s;
    }
    
    public java.util.List getSelectors() {
        return selectors;
    }
    
    private java.util.List selectors = new java.util.ArrayList();
    private Object styleDeclaration;
    
}
