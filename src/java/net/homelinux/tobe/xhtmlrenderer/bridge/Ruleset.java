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

package net.homelinux.tobe.xhtmlrenderer.bridge;

import org.xhtmlrenderer.css.impl.XRPropertyImpl;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class Ruleset implements net.homelinux.tobe.xhtmlrenderer.Ruleset {
    
    org.w3c.dom.css.CSSStyleRule base;
    int origin;
    java.util.List _props;
    
    /** Creates a new instance of Ruleset */
    public Ruleset(org.w3c.dom.css.CSSStyleRule rule, int orig) {
        base = rule;
        origin = orig;
        
        _props = new java.util.LinkedList();
        pullPropertiesFromDOMRule();
    }
    
    private void pullPropertiesFromDOMRule( ) {
        org.w3c.dom.css.CSSStyleDeclaration decl = base.getStyle();
        for ( int i = 0; i < decl.getLength(); i++ ) {

            String propName = decl.item(i);
            java.util.Iterator iter = XRPropertyImpl.fromCSSPropertyDecl( base, decl, propName, 0 );
            boolean importance = decl.getPropertyPriority(decl.item(i)).compareToIgnoreCase("important") == 0;
            while ( iter.hasNext() ) {
                XRPropertyImpl xrProp = (XRPropertyImpl)iter.next();
                PropertyDeclaration prop = new PropertyDeclaration(xrProp, importance, origin);
                _props.add( prop );
            }
        }
    }
    
    public java.util.Iterator getPropertyDeclarations() {
         return _props.iterator();
    }
    
    public org.w3c.css.sac.SelectorList getSelectorList() {
            org.w3c.css.sac.SelectorList list = null;

            try {
                // note, we parse the selector for this instance, not the one from the CSS Style, which
                // might still be multi-part; selector for XRStyleRule is always single (no commas)
                list = CSOM_PARSER.parseSelectors( new org.w3c.css.sac.InputSource( new java.io.StringReader( base.getSelectorText() ) ) );
            } catch ( java.io.IOException ex ) {
                ex.printStackTrace();
            }
            
            return list;
    }

    /** Convenience parser for selector text */
    private final static com.steadystate.css.parser.CSSOMParser CSOM_PARSER;
    static {
        CSOM_PARSER = new com.steadystate.css.parser.CSSOMParser();

    }    
}
