/*
 *
 * DOMStaticXhtmlAttributeResolver.java
 * Copyright (c) 2004 Torbjoern Gannholm
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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xhtmlrenderer.css.extend.AttributeResolver;
import org.xhtmlrenderer.css.extend.TreeResolver;

/**
 * Works for Xhtml in a DOM tree
 */
public class DOMStaticXhtmlAttributeResolver implements AttributeResolver {
    @Override
    public String getAttributeValue(Object e, String attrName) {
        return ((Element) e).getAttribute(attrName);
    }

    @Override
    public String getAttributeValue(Object o, String namespaceURI, String attrName) {
        Element e = (Element)o;
        if (namespaceURI == TreeResolver.NO_NAMESPACE) {
            return e.getAttribute(attrName);
        } else if (namespaceURI == null) {
            if (e.getLocalName() == null) { // No namespaces
                return e.getAttribute(attrName);
            } else {
                NamedNodeMap attrs = e.getAttributes();
                int l = attrs.getLength();
                for (int i = 0; i < l; i++) {
                    Attr attr = (Attr)attrs.item(i);
                    if (attrName.equals(attr.getLocalName())) {
                        return attr.getValue();
                    }
                }

                return "";
            }
        } else {
            return e.getAttributeNS(namespaceURI, attrName);
        }
    }

    @Override
    public String getClass(Object e) {
        return ((Element) e).getAttribute("class");
    }

    @Override
    public String getID(Object e) {
        return ((Element) e).getAttribute("id");
    }

    @Override
    public String getNonCssStyling(Object e) {
        return null;
    }

    @Override
    public String getLang(Object e) {
        return ((Element) e).getAttribute("lang");
    }

    @Override
    public String getElementStyling(Object el) {
        Element e = ((Element) el);
        StringBuilder style = new StringBuilder();
        if (e.getNodeName().equals("td")) {
            String s;
            if (!(s = e.getAttribute("colspan")).isEmpty()) {
                style.append("-fs-table-cell-colspan: ");
                style.append(s);
                style.append(";");
            }
            if (!(s = e.getAttribute("rowspan")).isEmpty()) {
                style.append("-fs-table-cell-rowspan: ");
                style.append(s);
                style.append(";");
            }
        }
        style.append(e.getAttribute("style"));
        return style.toString();
    }

    @Override
    public boolean isActive(Object e) {
        return false;
    }

    @Override
    public boolean isFocus(Object e) {
        return false;
    }

    @Override
    public boolean isHover(Object e) {
        return false;
    }

    @Override
    public boolean isLink(Object el) {
        Element e = ((Element) el);
        return e.getNodeName().equalsIgnoreCase("a") && !e.getAttribute("href").isEmpty();
    }

    @Override
    public boolean isVisited(Object e) {
        return false;
    }

}
