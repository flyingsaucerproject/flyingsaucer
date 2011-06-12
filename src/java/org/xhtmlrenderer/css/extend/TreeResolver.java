/*
 * TreeResolver.java
 * Copyright (c) 2005 Scott Cytacki
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
package org.xhtmlrenderer.css.extend;

/**
 * @author scott
 *         <p/>
 *         Gives the css matcher access to the information it needs about the tree structure.
 *         <p/>
 *         Elements are the "things" in the tree structure that can be matched by the matcher.
 */
public interface TreeResolver {
    // XXX Where should this go (used by parser, TreeResolver, and AttributeResolver
    public static final String NO_NAMESPACE = "";
    
    /**
     * returns the parent element of an element, or null if this was the root element
     */
    Object getParentElement(Object element);

    /**
     * returns the name of the element so that it may match against the selectors
     */
    String getElementName(Object element);

    /**
     * The previous sibling element, or null if none exists
     */
    Object getPreviousSiblingElement(Object node);

    /**
     * returns true if this element is the first child element of its parent
     */
    boolean isFirstChildElement(Object element);
    
    /**
     * returns true if this element is the last child element of its parent
     */
    boolean isLastChildElement(Object element);
    
    /**
     * Returns the index of the position of the submitted element among its element node siblings.
     * @param element
     * @return -1 in case of error, 0 indexed position otherwise
     */
    int getPositionOfElement(Object element);
    
    /**
     * Returns <code>true</code> if <code>element</code> has the local name
     * <code>name</code> and namespace URI <code>namespaceURI</code>.
     * @param element
     * @param namespaceURI The namespace to match, may be null to signify any
     * namespace.  Use {@link #NO_NAMESPACE} to signify that <code>name</code> 
     * should only match when there is no namespace defined on <code>element</code>.
     * @param name The name to match, may not be null
     */
    boolean matchesElement(Object element, String namespaceURI, String name);
}
