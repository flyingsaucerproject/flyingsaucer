/*
 * {{{ header & license
 * XRElement.java
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
package org.xhtmlrenderer.css;

import java.util.*;
import org.w3c.dom.Node;


/**
 * A DOM {@link org.w3c.dom.Element} that we have wrapped for XR use. In
 * particular, this class represents the relationship between a DOM <code>Element</code>
 * , its matched styles, and its derived styles (after cascade/inherit). This
 * association is necessary because in order to derive all styles, and to
 * compute relative values, we need to have parent/child <code>Element</code>
 * associations available. The intention is that <code>XRElements</code> are
 * instantiated after processing a DOM, and a CSS selector matcher calls {@link
 * #addMatchedStyle(XRStyleRule)} for each style that matches the element. Once
 * all styles are matched, {@link #derivedStyle()} returns a {@link
 * XRDerivedStyle} instance with all the applicable properties for the element.
 * If using the {@link org.xhtmlrenderer.css.bridge.XRStyleReference} for style
 * lookup, you will not need to use this class directly, as it handles
 * instantiation of XRElements.
 *
 * @author   Patrick Wright
 * @see      org.xhtmlrenderer.css.bridge.XRStyleReference
 */
//ASK: if we are going to use DOM anyway, should this extend Element?
public interface XRElement {
    /**
     * The DOM {@link org.w3c.dom.Element} we are wrapping.
     *
     * @return   See desc.
     */
    Node domNode();


    /**
     * Our parent XRElement.
     *
     * @return   Parent XRElement, null if called on root element.
     */
    XRElement parentXRElement();


    /**
     * A derived set of properties for this element, taken from the matched
     * styles added using {@link #addMatchedStyle(XRStyleRule)}. There is only
     * one instance of XRDerivedStyle for an XRElement, however, if each call to
     * {@link #addMatchedStyle(XRStyleRule)} can cause {@link #derivedStyle()}
     * to return different {@link XRDerivedStyle} instances. Generally, you
     * should complete all matching before requesting the derived style,
     * otherwise property values may be inconsistent. See documentation for
     * {@link XRDerivedStyle} for more details.
     *
     * @return   Returns a XRDerivedStyle instance with the unique CSS
     *      properties that apply to this Element, after matching.
     */
    XRDerivedStyle derivedStyle();


    /**
     * Associates a style rule with this element--selector matches. Note this
     * should be a pure-selector match, regardless of cascade or other such
     * rules. Those are processed during derivation. The sequence in which they
     * are added is not important, as long as the rule itself has sequencing
     * information (see {@link XRStyleRule} for details).
     *
     * @param style  The XRStyleRule found to have matched this Element.
     */
    void addMatchedStyle( XRStyleRule style );

    /**
     * Convenience method for debugging--returns an Iterator of the selector
     * strings matched to this element, as Strings.
     *
     * @return   Iterator of String selectors matched to this Element.
     */
    Iterator listMatchedStyleSelectors();
}// end interface

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2004/10/23 13:03:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

