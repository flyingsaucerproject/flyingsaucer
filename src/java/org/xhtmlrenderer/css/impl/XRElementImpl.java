/*
 * {{{ header & license
 * XRElementImpl.java
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
package org.xhtmlrenderer.css.impl;

import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.XRDerivedStyle;
import org.xhtmlrenderer.css.XRElement;
import org.xhtmlrenderer.css.XRStyleRule;


/**
 * Implementation of XRElement, see interface for comments.
 *
 * @author   Patrick Wright
 */
public class XRElementImpl implements XRElement {

    /** The Document our Node belongs to */
    private Document _document;
    /**
     * The DOM Node we are wrapping. HACK: there was some problem in an existing
     * API that required storage of Node, not Element...? (PWW 15/08/04)
     */
    private Node _node;
    /**
     * The parent XRElement, null if none--this would be the XRElement that owns
     * the parent DOM Node for our own DOM Node.
     */
    private XRElement _xrParent;
    /** List of the styles added to us by matching process. */
    private List _matchedStyles;
    /** Our derived style instance. */
    private XRDerivedStyle _derivedStyle;


    /**
     * //JDOC
     *
     * @param node             PARAM
     * @param parentXRElement  PARAM
     */
    public XRElementImpl( Node node, XRElement parentXRElement ) {

        this();
        _document = node.getOwnerDocument();
        _node = node;
        _xrParent = parentXRElement;
    }


    /** Constructor for the XRElementImpl object */
    private XRElementImpl() {
        _matchedStyles = new ArrayList();
    }


    /**
     * Returns the enclosed DOM Node.
     *
     * @return   Returns
     */
    public Node domNode() {
        return _node;
    }


    /**
     * Returns the parent XRElement, will be null if this is the root.
     *
     * @return   Returns
     */
    public XRElement parentXRElement() {
        return _xrParent;
    }


    /**
     * //JDOC
     *
     * @return   Returns
     */
    //ASK: synchronized?
    public XRDerivedStyle derivedStyle() {
        if ( _derivedStyle == null ) {
            _derivedStyle = new XRDerivedStyleImpl( this, _matchedStyles.iterator() );
        }
        return _derivedStyle;
    }


    /**
     * Associates an XRStyleRule as matched with the XRElement.
     *
     * @param style  The feature to be added to the MatchedStyle attribute
     */
    public void addMatchedStyle( XRStyleRule style ) {
        _derivedStyle = null;
        _matchedStyles.add( style );
    }


    /**
     * Convenience method for debugging--returns an iterator of the selector
     * strings matched to this element
     *
     * @return   Returns
     */
    public Iterator listMatchedStyleSelectors() {
        List sel = new ArrayList();
        Iterator iter = _matchedStyles.iterator();
        while ( iter.hasNext() ) {
            sel.add( ( (XRStyleRule)iter.next() ).cssSelectorText() );
        }
        return sel.iterator();
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2004/10/23 13:21:14  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

