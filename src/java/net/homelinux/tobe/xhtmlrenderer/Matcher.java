/*
 * {{{ header & license
 * Matcher.java
 * Copyright (c) 2004 Torbjörn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
/*
 * Matcher.java
 *
 * Created on den 29 augusti 2004, 22:27
 */

package net.homelinux.tobe.xhtmlrenderer;

/**
 *
 * @author  Torbjörn Gannholm
 */
public interface Matcher {
    
    public void setAttributeResolver(AttributeResolver ar);
    
    public void setDocument(org.w3c.dom.Document doc);
    
    /** iterator should return objects of type Stylesheet in the correct order of declaration */
    public void setStylesheets(java.util.Iterator i);
    
    /** Contract: Objects in iterator are of type PropertyDeclaration and are given in order of rising specificity of the matching selector, subordered by order of declaration */
    public java.util.Iterator getMatchedPropertyDeclarations(org.w3c.dom.Element e);
    
}
