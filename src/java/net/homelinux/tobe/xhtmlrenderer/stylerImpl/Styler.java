/*
 *
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

package net.homelinux.tobe.xhtmlrenderer.stylerImpl;

import net.homelinux.tobe.xhtmlrenderer.PropertyDeclaration;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class Styler implements net.homelinux.tobe.xhtmlrenderer.Styler {
    
    java.util.HashMap _styleMap = new java.util.HashMap();
    
    java.util.HashMap _styleCache = new java.util.HashMap();
    
    net.homelinux.tobe.xhtmlrenderer.Matcher _matcher;
    
    java.awt.Rectangle _rect;
    
    /** Creates a new instance of Styler */
    public Styler() {
    }
    
    public net.homelinux.tobe.xhtmlrenderer.stylerImpl.CalculatedStyle getCalculatedStyle(org.w3c.dom.Element e) {
        return (net.homelinux.tobe.xhtmlrenderer.stylerImpl.CalculatedStyle) _styleMap.get(e);
    }
    
    //changing this should cause a restyle
    public void setMatcher(net.homelinux.tobe.xhtmlrenderer.Matcher m) {
        _matcher = m;
    }

    /**
     * Applies matches to Element and its children, recursively. StyleMap should
     * have been re-loaded before calling this.
     *
     * @param elem  PARAM
     */
    public void restyleTree( org.w3c.dom.Element elem ) {
            net.homelinux.tobe.xhtmlrenderer.stylerImpl.CalculatedStyle parent = null;

            // if this is the root, we will have no parent XRElement; otherwise
            // we will check to see if our parent was loaded. Since we expect to load
            // from root to leaves, we should always find a parent
            // this means, however, that root will have a null parent
            if ( elem.getOwnerDocument().getDocumentElement() == elem ) {
                _styleCache = new java.util.HashMap();
                parent = new CurrentBoxStyle(_rect);
            } else {
                org.w3c.dom.Node pnode = elem.getParentNode();
                if(pnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) parent = getCalculatedStyle( (org.w3c.dom.Element) pnode );
                if ( parent == null ) {
                    throw new RuntimeException( "Applying matches to elements, found an element with no mapped parent; can't continue." );
                }
            }
            net.homelinux.tobe.xhtmlrenderer.CascadedStyle matched = _matcher.getCascadedStyle(elem);
            
            net.homelinux.tobe.xhtmlrenderer.stylerImpl.CalculatedStyle cs = null;
            StringBuffer sb = new StringBuffer();
            sb.append(parent).append(":").append(matched);
            String fingerprint = sb.toString();
            cs = (net.homelinux.tobe.xhtmlrenderer.stylerImpl.CalculatedStyle) _styleCache.get(fingerprint);
            
            if(cs == null) {
                cs = new CalculatedStyle(parent, matched);
                _styleCache.put(fingerprint, cs);
            }
            _styleMap.put( elem, cs );

        // apply rules from style attribute on element, if any
        // elementStyling is now responsibility of Matcher

        org.w3c.dom.NodeList nl = elem.getChildNodes();
        for ( int i = 0, len = nl.getLength(); i < len; i++ ) {
            org.w3c.dom.Node n = nl.item( i );
            if ( n instanceof org.w3c.dom.Element ) {
                restyleTree( (org.w3c.dom.Element)n );
            }
        }
    }
    
    //changing this should cause a restyle
    public void setViewportRectangle(java.awt.Rectangle rect) {
        _rect = rect;
//System.err.println("Bounds "+rect.height+" "+rect.width);
    }
    
}
