/*
 * Ruleset.java
 * Copyright (c) 2004 Patrick Wright, Torbjörn Gannholm
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
package org.xhtmlrenderer.css.sheet;

import java.util.*;

import org.xhtmlrenderer.util.XRRuntimeException;
import org.xhtmlrenderer.css.constants.CSSName;


/**
 * @author   Torbjörn Gannholm
 * @author   Patrick Wright
 */
public class Ruleset {
    /** Description of the Field */
    private int _origin;

    /** Description of the Field */
    private java.util.List _props;

    /**
     * Our list of SAC Selectors, pulled from the CSSStyleRule used to
     * initialize this Ruleset
     */
    private org.w3c.css.sac.SelectorList sacSelectorList;

    /** Convenience parser for selector text */
    private final static com.steadystate.css.parser.CSSOMParser CSOM_PARSER;

    /**
     * Creates a new instance of Ruleset
     *
     * @param rule  PARAM
     * @param orig  PARAM
     */
    public Ruleset( org.w3c.dom.css.CSSStyleRule rule, int orig ) {
        _origin = orig;
        _props = new java.util.LinkedList();
        pullPropertiesFromDOMRule( rule );
        pullSelectorsFromDOMRule( rule );
    }

    /**
     * Returns an Iterator of PropertyDeclarations pulled from this
     * CSSStyleRule.
     *
     * @return   The propertyDeclarations value
     */
    public java.util.Iterator getPropertyDeclarations() {
        return _props.iterator();
    }

    /**
     * Returns the SAC SelectorList associated with this CSSStyleRule.
     *
     * @return   The selectorList value
     */
    public org.w3c.css.sac.SelectorList getSelectorList() {
        return sacSelectorList;
    }

    /**
     * Extracts the CSS SAC SelectorList from a CSSStyleRule.
     *
     * @param sacRule  PARAM
     */
    private void pullSelectorsFromDOMRule( org.w3c.dom.css.CSSStyleRule sacRule ) {
        try {
            // note, we parse the selector for this instance, not the one from the CSS Style, which
            // might still be multi-part; selector for TODO is always single (no commas)
            sacSelectorList =
                    CSOM_PARSER.parseSelectors(
                    new org.w3c.css.sac.InputSource(
                    new java.io.StringReader( sacRule.getSelectorText() ) ) );
        } catch ( java.io.IOException ex ) {
            throw new XRRuntimeException( "Could not pull SAC Selectors from SAC CSSStyleRule.", ex );
        }
    }

    /**
     * Given a CSSStyleRule, pulls all properties into instances of
     * PropertyDeclaration which are stored in our _props List.
     *
     * @param sacRule  PARAM
     */
    private void pullPropertiesFromDOMRule( org.w3c.dom.css.CSSStyleRule sacRule ) {
        org.w3c.dom.css.CSSStyleDeclaration decl = sacRule.getStyle();
        
        // a style declaration is a block of property assignments
        // so looping items in the declaration means looping properties
        //
        // here we create a PropertyDeclaration for each property, expanding
        // shorthand properties along the way.
        for ( int i = 0; i < decl.getLength(); i++ ) {
            String propName = decl.item( i );
            CSSName cssName = CSSName.getByPropertyName(propName);

            PropertyDeclaration prop = null;
            Iterator iter = PropertyDeclaration.newFactory(cssName).buildDeclarations(decl, cssName, _origin);

            while (iter.hasNext()) {
                // the cast is just for doc purposes
                _props.add((PropertyDeclaration)iter.next());
            }
        }
    }

    static {
        CSOM_PARSER = new com.steadystate.css.parser.CSSOMParser();
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2005/01/24 19:01:08  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.4  2005/01/24 14:36:30  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.3  2004/11/15 12:42:23  pdoubleya
 * Across this checkin (all may not apply to this particular file)
 * Changed default/package-access members to private.
 * Changed to use XRRuntimeException where appropriate.
 * Began move from System.err.println to std logging.
 * Standard code reformat.
 * Removed some unnecessary SAC member variables that were only used in initialization.
 * CVS log section.
 *
 *
 */

