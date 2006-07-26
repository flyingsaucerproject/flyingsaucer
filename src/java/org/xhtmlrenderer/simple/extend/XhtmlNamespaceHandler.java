/*
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
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
package org.xhtmlrenderer.simple.extend;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Handles xhtml documents, including presentational html attributes (see css 2.1 spec, 6.4.4).
 * In this class ONLY handling (css equivalents) of presentational properties
 * (according to css 2.1 spec, section 6.4.4) should be specified.
 *
 * @author Torbjörn Gannholm
 */
public class XhtmlNamespaceHandler extends XhtmlCssOnlyNamespaceHandler {

    public String getNonCssStyling(org.w3c.dom.Element e) {
        StringBuffer style = new StringBuffer();
        if (e.getNodeName().equals("table")) {
            String s;
            if (!(s = e.getAttribute("width")).equals("")) {
                style.append("width: ");
                style.append(s);
                style.append(";");
            }
            if (!(s = e.getAttribute("border")).equals("")) {
                style.append("border: ");
                style.append(s);
                style.append(" inset black;");
            }
            if (!(s = e.getAttribute("cellspacing")).equals("")) {
                style.append("border-collapse: separate; border-spacing: ");
                style.append(s);
                style.append(";");
            }
        } else if (e.getNodeName().equals("td") || e.getNodeName().equals("th")) {
            String s;
            //check for cellpadding
            for (Node n = e.getParentNode(); n != null; n = n.getParentNode()) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element t = (Element) n;
                    if (t.getNodeName().equals("table")) {
                        if (!(s = t.getAttribute("cellpadding")).equals("")) {
                            style.append("padding: ");
                            style.append(s);
                            style.append(";");
                        }
                        if (!(s = t.getAttribute("border")).equals("")) {
                            style.append("border: 1 outset black;");
                        }
                        break;
                    }
                }
            }
            if (!(s = e.getAttribute("width")).equals("")) {
                style.append("width: ");
                style.append(s);
                style.append(";");
            }
            if (!(s = e.getAttribute("align")).equals("")) {
                style.append("text-align: ");
                style.append(s.toLowerCase());
                style.append(";");
            }
            if (!(s = e.getAttribute("valign")).equals("")) {
                style.append("vertical-align: ");
                style.append(s.toLowerCase());
                style.append(";");
            }
        }
        return style.toString();
    }
}

