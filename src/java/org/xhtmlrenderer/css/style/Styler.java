/*
 * Styler.java
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
package org.xhtmlrenderer.css.style;

import org.xhtmlrenderer.css.sheet.PropertyDeclaration;


/**
 * @author   Torbjörn Gannholm
 */
public class Styler {
    /** Description of the Field */
    private java.util.HashMap _styleMap = new java.util.HashMap();
    //java.util.HashMap _peStyleMap = new java.util.HashMap();

    /** Description of the Field */
    private java.util.HashMap _styleCache = new java.util.HashMap();

    /** Description of the Field */
    private org.xhtmlrenderer.css.newmatch.Matcher _matcher;

    /** Description of the Field */
    private java.awt.Rectangle _rect;

    /** Creates a new instance of Styler  */
    public Styler() { }

    /**
     * Applies matches to Element and its children, recursively. StyleMap should
     * have been re-loaded before calling this. <p/>
     *
     * TODO: change matchElement, then this method is not needed
     *
     * @param elem  PARAM
     */
    public void styleTree( org.w3c.dom.Element elem ) {
        CalculatedStyle parent = null;

        if ( elem.getOwnerDocument().getDocumentElement() == elem ) {
            _styleCache = new java.util.HashMap();
            parent = new CurrentBoxStyle( _rect );
        } else {
            org.w3c.dom.Node pnode = elem.getParentNode();
            if ( pnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
                parent = getCalculatedStyle( (org.w3c.dom.Element)pnode );
            }
            if ( parent == null ) {
                throw new RuntimeException( "Applying matches to elements, found an element with no mapped parent; can't continue." );
            }
        }
        //org.xhtmlrenderer.css.newmatch.CascadedStyle matched = _matcher.matchElement(elem);
        org.xhtmlrenderer.css.newmatch.CascadedStyle matched = _matcher.getCascadedStyle( elem );

        CalculatedStyle cs = null;
        StringBuffer sb = new StringBuffer();
        sb.append( parent.hashCode() ).append( ":" ).append( matched.hashCode() );
        String fingerprint = sb.toString();
        cs = (CalculatedStyle)_styleCache.get( fingerprint );

        if ( cs == null ) {
            cs = new CalculatedStyle( parent, matched );
            _styleCache.put( fingerprint, cs );
        }
        _styleMap.put( elem, cs );
        //System.err.println(elem.getNodeName()+" "+cs);

        // apply rules from style attribute on element, if any
        // elementStyling is now responsibility of Matcher

        org.w3c.dom.NodeList nl = elem.getChildNodes();
        for ( int i = 0, len = nl.getLength(); i < len; i++ ) {
            org.w3c.dom.Node n = nl.item( i );
            if ( n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
                styleTree( (org.w3c.dom.Element)n );
            }
        }
    }

    /**
     * Applies matches to Element and its children, recursively. StyleMap should
     * have been re-loaded before calling this.
     *
     * @param elem  PARAM
     */
    public void restyleTree( org.w3c.dom.Element elem ) {
        CalculatedStyle parent = null;

        if ( elem.getOwnerDocument().getDocumentElement() == elem ) {
            _styleCache = new java.util.HashMap();
            parent = new CurrentBoxStyle( _rect );
        } else {
            org.w3c.dom.Node pnode = elem.getParentNode();
            if ( pnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
                parent = getCalculatedStyle( (org.w3c.dom.Element)pnode );
            }
            if ( parent == null ) {
                throw new RuntimeException( "Applying matches to elements, found an element with no mapped parent; can't continue." );
            }
        }
        org.xhtmlrenderer.css.newmatch.CascadedStyle matched = _matcher.matchElement( elem );
        //org.xhtmlrenderer.css.newmatch.CascadedStyle matched = _matcher.getCascadedStyle(elem);

        CalculatedStyle cs = null;
        StringBuffer sb = new StringBuffer();
        sb.append( parent.hashCode() ).append( ":" ).append( matched.hashCode() );
        String fingerprint = sb.toString();
        cs = (CalculatedStyle)_styleCache.get( fingerprint );

        if ( cs == null ) {
            cs = new CalculatedStyle( parent, matched );
            _styleCache.put( fingerprint, cs );
        }
        _styleMap.put( elem, cs );
        //System.err.println(elem.getNodeName()+" "+cs);

        // apply rules from style attribute on element, if any
        // elementStyling is now responsibility of Matcher

        org.w3c.dom.NodeList nl = elem.getChildNodes();
        for ( int i = 0, len = nl.getLength(); i < len; i++ ) {
            org.w3c.dom.Node n = nl.item( i );
            if ( n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
                restyleTree( (org.w3c.dom.Element)n );
            }
        }
    }

    /**
     * May return null
     *
     * @param m  The new matcher value
     */
    /*
     * public CalculatedStyle getPECalculatedStyle(org.w3c.dom.Element e, String pseudoElement) {
     * java.util.Map elm = (java.util.Map) _peStyleMap.get(e);
     * if(elm == null) {
     * elm = resolvePEStyles(e);
     * _peStyleMap.put(e, elm);
     * }
     * return (CalculatedStyle) elm.get(pseudoElement);
     * }
     * private java.util.Map resolvePEStyles(org.w3c.dom.Element e) {
     * java.util.Map elm = new java.util.HashMap();
     * CalculatedStyle parent = getCalculatedStyle(e);
     * java.util.Map peCascades = _matcher.getPECascadedStyleMap(e);
     * for(java.util.Iterator i = peCascades.entrySet().iterator(); i.hasNext();) {
     * java.util.Map.Entry me = (java.util.Map.Entry) i.next();
     * org.xhtmlrenderer.css.newmatch.CascadedStyle matched = (org.xhtmlrenderer.css.newmatch.CascadedStyle) me.getValue();
     * CalculatedStyle cs = null;
     * StringBuffer sb = new StringBuffer();
     * sb.append(parent.hashCode()).append(":").append(matched.hashCode());
     * String fingerprint = sb.toString();
     * cs = (CalculatedStyle) _styleCache.get(fingerprint);
     * if(cs == null) {
     * cs = new CalculatedStyle(parent, matched);
     * _styleCache.put(fingerprint, cs);
     * }
     * elm.put( me.getKey(), cs );
     * }
     * return elm;
     * }
     */
    //changing this should cause a restyle
    public void setMatcher( org.xhtmlrenderer.css.newmatch.Matcher m ) {
        _matcher = m;
    }

    //changing this should cause a restyle
    /**
     * Sets the viewportRectangle attribute of the Styler object
     *
     * @param rect  The new viewportRectangle value
     */
    public void setViewportRectangle( java.awt.Rectangle rect ) {
        _rect = rect;
        //System.err.println("Bounds "+rect.height+" "+rect.width);
    }

    /**
     * Gets the calculatedStyle attribute of the Styler object
     *
     * @param e  PARAM
     * @return   The calculatedStyle value
     */
    public CalculatedStyle getCalculatedStyle( org.w3c.dom.Element e ) {
        return (CalculatedStyle)_styleMap.get( e );
    }
} // end class

/*

 * $Id$

 *

 * $Log$
 * Revision 1.8  2004/11/15 12:42:23  pdoubleya
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


