/*
 * DOMTreeResolver.java
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
package org.xhtmlrenderer.css.extend.lib;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.css.extend.TreeResolver;

/**
 * @author scott
 *         <p/>
 *         works for a w3c DOM tree
 */
public class DOMTreeResolver implements TreeResolver {
    public Object getParentElement(Object element) {
        Node parent = ((org.w3c.dom.Element) element).getParentNode();
        if (parent.getNodeType() != Node.ELEMENT_NODE) parent = null;
        return parent;
    }

    public Object getPreviousSiblingElement(Object element) {
        Node sibling = ((Element) element).getPreviousSibling();
        while (sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE) {
            sibling = sibling.getPreviousSibling();
        }
        if (sibling == null || sibling.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        return sibling;
    }

    public String getElementName(Object element) {
        String name = ((Element) element).getLocalName();
        if (name == null) name = ((Element) element).getNodeName();
        return name;
    }

    public boolean isFirstChildElement(Object element) {
        org.w3c.dom.Node parent = ((org.w3c.dom.Element) element).getParentNode();
        NodeList nl = parent.getChildNodes();

        int i = 0;
        while (i < nl.getLength() && nl.item(i).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
            i++;
        }
        return (nl.item(i) == element);
    }
}
