/*
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
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
 * @author Torbjoern Gannholm
 */
public class XhtmlNamespaceHandler extends XhtmlCssOnlyNamespaceHandler {
    /**
     * {@inheritDoc}
     */
    public boolean isImageElement(Element e) {
        return (e != null && e.getNodeName().equalsIgnoreCase("img"));
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isFormElement(Element e) {
        return (e != null && e.getNodeName().equalsIgnoreCase("form"));
    }

    public String getImageSourceURI(Element e) {
        String uri = null;
        if (e != null) {
            uri = e.getAttribute("src");
        }
        return uri;
    }

    public String getNonCssStyling(Element e) {
        if (e.getNodeName().equals("table")) {
            return applyTableStyles(e);            
        } else if (e.getNodeName().equals("td") || e.getNodeName().equals("th")) {
            return applyTableCellStyles(e);
        } else if (e.getNodeName().equals("tr")) {
            return applyTableRowStyles(e);
        } else if (e.getNodeName().equals("img")) {
            return applyImgStyles(e);
        } else if (e.getNodeName().equals("p") || e.getNodeName().equals("div")) {
            return applyBlockAlign(e);
        }
        return "";
    }
    
    private String applyBlockAlign(Element e) {
        StringBuffer style = new StringBuffer();
        applyTextAlign(e, style);
        return style.toString();
    }
    
    private String applyImgStyles(Element e) {
        StringBuffer style = new StringBuffer();
        applyFloatingAlign(e, style);
        return style.toString();
    }

    private String applyTableCellStyles(Element e) {
        StringBuffer style = new StringBuffer();
        String s;
        //check for cellpadding
        Element table = findTable(e);
        if (table != null) {
            s = getAttribute(table, "cellpadding");
            if (s != null) {
                style.append("padding: ");
                style.append(convertToLength(s));
                style.append(";");
            }
            s = getAttribute(table, "border");
            if (s != null && ! s.equals("0")) {
                style.append("border: 1px outset black;");
            }
        }
        s = getAttribute(e, "width");
        if (s != null) {
            style.append("width: ");
            style.append(convertToLength(s));
            style.append(";");
        }
        s = getAttribute(e, "height");
        if (s != null) {
            style.append("height: ");
            style.append(convertToLength(s));
            style.append(";");
        }        
        applyTableContentAlign(e, style);
        s = getAttribute(e, "bgcolor");
        if (s != null) {
            s = s.toLowerCase();
            style.append("background-color: ");
            if (looksLikeAMangledColor(s)) {
                style.append('#');
                style.append(s);
            } else {
                style.append(s);
            }
            style.append(';');
        }
        s = getAttribute(e, "background");
        if (s != null) {
            style.append("background-image: url(");
            style.append(s);
            style.append(");");
        }
        return style.toString();
    }

    private String applyTableStyles(Element e) {
        StringBuffer style = new StringBuffer();
        String s;
        s = getAttribute(e, "width");
        if (s != null) {
            style.append("width: ");
            style.append(convertToLength(s));
            style.append(";");
        }
        s = getAttribute(e, "border");
        if (s != null) {
            style.append("border: ");
            style.append(convertToLength(s));
            style.append(" inset black;");
        }
        s = getAttribute(e, "cellspacing");
        if (s != null) {
            style.append("border-collapse: separate; border-spacing: ");
            style.append(convertToLength(s));
            style.append(";");
        }
        s = getAttribute(e, "bgcolor");
        if (s != null) {
            s = s.toLowerCase();
            style.append("background-color: ");
            if (looksLikeAMangledColor(s)) {
                style.append('#');
                style.append(s);
            } else {
                style.append(s);
            }
            style.append(';');
        }
        s = getAttribute(e, "background");
        if (s != null) {
            style.append("background-image: url(");
            style.append(s);
            style.append(");");
        }
        applyFloatingAlign(e, style);
        return style.toString();
    }
    
    private String applyTableRowStyles(Element e) {
        StringBuffer style = new StringBuffer();
        applyTableContentAlign(e, style);
        return style.toString();
    }
    
    private void applyFloatingAlign(Element e, StringBuffer style) {
        String s;
        s = getAttribute(e, "align");
        if (s != null) {
            s = s.toLowerCase().trim();
            if (s.equals("left")) {
                style.append("float: left;");
            } else if (s.equals("right")) {
                style.append("float: right;");
            } else if (s.equals("center")) {
                style.append("margin-left: auto; margin-right: auto;");
            }
        }
    }
    
    private void applyTextAlign(Element e, StringBuffer style) {
        String s;
        s = getAttribute(e, "align");
        if (s != null) {
            s = s.toLowerCase().trim();
            if (s.equals("left") || s.equals("right") || 
                    s.equals("center") || s.equals("justify")) {
                style.append("text-align: ");
                style.append(s);
                style.append(";");
            }
        }
    }
    
    private void applyTableContentAlign(Element e, StringBuffer style) {
        String s;
        s = getAttribute(e, "align");
        if (s != null) {
            style.append("text-align: ");
            style.append(s.toLowerCase());
            style.append(";");
        }
        s = getAttribute(e, "valign");
        if (s != null) {
            style.append("vertical-align: ");
            style.append(s.toLowerCase());
            style.append(";");
        }
    }
    
    private boolean looksLikeAMangledColor(String s) {
        if (s.length() != 6) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean valid = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
            if (! valid) {
                return false;
            }
        }
        return true;
    }
    
    private Element findTable(Element cell) {
        Node n = cell.getParentNode();
        Element next;
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            next = (Element)n;
            if (next.getNodeName().equals("tr")) {
                n = next.getParentNode();
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    next = (Element)n;
                    String name = next.getNodeName();
                    if (name.equals("table")) {
                        return next;
                    }
                    
                    if (name.equals("tbody") || name.equals("tfoot") || name.equals("thead")) {
                        n = next.getParentNode();
                        if (n.getNodeType() == Node.ELEMENT_NODE) {
                            next =(Element)n;
                            if (next.getNodeName().equals("table")) {
                                return next;
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
}

