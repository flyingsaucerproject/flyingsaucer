/*
 *
 * StaticHtmlAttributeResolver.java
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

package org.xhtmlrenderer.swing;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class StaticXhtmlAttributeResolver implements org.xhtmlrenderer.extend.AttributeResolver {
    
    /** Creates a new instance of StaticHtmlAttributeResolver */
    public StaticXhtmlAttributeResolver() {
    }
    
    public String getAttributeValue(org.w3c.dom.Element e, String attrName) {
        return e.getAttribute(attrName);
    }
    
    public String getClass(org.w3c.dom.Element e) {
        return e.getAttribute("class");
    }
    
    public String getID(org.w3c.dom.Element e) {
        return e.getAttribute("id");
   }
    
    public String getLang(org.w3c.dom.Element e) {
        return e.getAttribute("lang");
    }
    
    public String getElementStyling(org.w3c.dom.Element e) {
        return e.getAttribute("style");
    }
    
    public boolean isActive(org.w3c.dom.Element e) {
        return false;
    }
    
    public boolean isFocus(org.w3c.dom.Element e) {
        return false;
    }
    
    public boolean isHover(org.w3c.dom.Element e) {
        return false;
    }
    
    public boolean isLink(org.w3c.dom.Element e) {
        if(e.getNodeName().equalsIgnoreCase("a") && !e.getAttribute("href").equals("")) return true;
        return false;
   }
    
    public boolean isVisited(org.w3c.dom.Element e) {
        return false;
    }
    
}
