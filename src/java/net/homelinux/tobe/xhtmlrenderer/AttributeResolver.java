/*
 * {{{ header & license
 * AttributeResolver.java
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

package net.homelinux.tobe.xhtmlrenderer;

/**
 * In XML, an application may or may not know how to find the ID and/or class and/or attribute defaults of an element.
 *
 * To enable matching of identity conditions, class conditions, language, and attribute defaults you need to provide an AttributeResolver to the StyleMap.
 *
 * NOTE: The application is required to look in a document's internal subset for default attribute values,
 * but the application is not required to use its built-in knowledge of a namespace or look in the external subset.
 *
 * @author  Torbjörn Gannholm
 */
public interface AttributeResolver {
    
    /** may return null. Required to return null if attribute does not exist and not null if attribute exists. */
    public String getAttributeValue(org.w3c.dom.Element e, String attrName);
    
    /** may return null */
    public String getClass(org.w3c.dom.Element e);
    
    /** may return null */
    public String getID(org.w3c.dom.Element e);
    
    /** may return null */
    public String getElementStyling(org.w3c.dom.Element e);
    
    /** may return null */
    public String getLang(org.w3c.dom.Element e);
    
    public boolean isPseudoClass(org.w3c.dom.Element e, int pc);
    
    static public final int LINK_PSEUDOCLASS = 1;
    static public final int VISITED_PSEUDOCLASS = 2;
    static public final int HOVER_PSEUDOCLASS = 4;
    static public final int ACTIVE_PSEUDOCLASS = 8;
    static public final int FOCUS_PSEUDOCLASS = 16;
    
}
