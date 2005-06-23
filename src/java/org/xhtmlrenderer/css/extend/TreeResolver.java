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
}
