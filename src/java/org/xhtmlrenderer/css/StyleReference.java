/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.AttributeResolver;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.layout.Context;


/**
 * <p/>
 * <p/>
 * A StyleReference is a convenience front-end to look up CSS2 style rules and
 * properties. The storage of properties takes place through the parse()
 * methods. Storage may be through an internal RuleBank or some other mechanism.
 * Resolution of property cascade and inheritance happens when properties are
 * looked up. Matching of styles to elements happens when matchStyles(Document)
 * is called. The order to use this is then: parse()* - matchStyles() -
 * getProperty(). If styles are added after the fact then matchStyles() should
 * be called again.</p> <p>
 * <p/>
 * StyleReference provides a number of easy-to-use access methods for finding
 * the value of a property assigned to an Element based on loaded StyleSheets.
 * The RuleBank holding these properties is pluggable so different caching and
 * lookup schemes are possible for StyleSheets.</p> <p>
 * <p/>
 * All the property accessor method (getZZZ()) search against properties already
 * matched to Elements. Several offer the option of searching for the property
 * matched to the Element's ancestors if it is not matched to the Element
 * itself. The RuleBank is used to determine the Element-property match.
 * Property names are always the CSS2 property names, as strings.</p> <p>
 * <p/>
 * NOTE: pulled from CSSBank and CSSAccessor to have a common ancestor interface
 * </p>
 *
 * @author Patrick Wright
 */
public interface StyleReference {

    /**
     * List the derived (calculated) properties for an Element.
     * with the property name as key and the cssValue as value.
     * <p/>
     * Used by the DOMInspector
     */
    public java.util.Map getDerivedPropertiesMap(Element e);


    /**
     * Does what is needed to handle a new document (called when a new document is loaded).
     *
     * @param context layout context
     * @param nsh     NamespaceHandler for the document
     * @param ar      AttributeResolver for the document
     * @param doc     DOM document
     */
    public void setDocumentContext(Context context, NamespaceHandler nsh, AttributeResolver ar, Document doc);

    /**
     * Handle the pseudoElements, may return null
     */
    public CascadedStyle getPseudoElementStyle(Node node, String pseudoElement);

    /**
     * get the CalculatedStyle once for the element, then query that
     */
    public CalculatedStyle getStyle(Node node);

    /**
     * Handle a restyle because of a change in hover state. If the element (and its children) was restyled
     * because of this, true is returned
     */
    public boolean wasHoverRestyled(Element e);

}// end interface

/*
 * $Id$
 *
 * $Log$
 * Revision 1.11  2004/12/05 14:35:38  tobega
 * Cleaned up some usages of Node (and removed unused stuff) in layout code. The goal is to pass "better" objects than Node wherever possible in an attempt to shake out the bugs in tree-traversal (probably often unnecessary tree-traversal)
 *
 * Revision 1.10  2004/12/05 00:48:54  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.9  2004/11/09 23:57:41  tobega
 * Added hook to StyleReference for dynamic hover restyling
 *
 * Revision 1.8  2004/11/08 23:15:55  tobega
 * Changed pseudo-element styling to just return CascadedStyle
 *
 * Revision 1.7  2004/11/08 08:22:15  tobega
 * Added support for pseudo-elements
 *
 * Revision 1.6  2004/11/07 01:31:38  tobega
 * Added hooks for handling First-letter pseudo-element
 *
 * Revision 1.5  2004/11/07 01:17:55  tobega
 * DOMInspector now works with any StyleReference
 *
 * Revision 1.4  2004/11/04 21:50:54  tobega
 * Preparation for new matching/styling code
 *
 * Revision 1.3  2004/10/23 13:03:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

