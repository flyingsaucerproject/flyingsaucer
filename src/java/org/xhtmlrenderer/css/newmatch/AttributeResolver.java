/*
 *
 * AttributeResolver.java
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

package org.xhtmlrenderer.css.newmatch;

/**
 * In XML, an application may or may not know how to find the ID and/or class and/or attribute defaults of an element.
 * <p/>
 * To enable matching of identity conditions, class conditions, language, and attribute defaults you need to provide an AttributeResolver to the StyleMap.
 * <p/>
 * NOTE: The application is required to look in a document's internal subset for default attribute values,
 * but the application is not required to use its built-in knowledge of a namespace or look in the external subset.
 *
 * @author Torbjörn Gannholm
 */
public interface AttributeResolver {

    /**
     * may return null. Required to return null if attribute does not exist and not null if attribute exists.
     */
    public String getAttributeValue(org.w3c.dom.Element e, String attrName);

    /**
     * may return null
     */
    public String getClass(org.w3c.dom.Element e);

    /**
     * may return null
     */
    public String getID(org.w3c.dom.Element e);

    /**
     * may return null
     */
    public String getElementStyling(org.w3c.dom.Element e);

    /**
     * may return null
     */
    public String getLang(org.w3c.dom.Element e);

    public boolean isLink(org.w3c.dom.Element e);

    public boolean isVisited(org.w3c.dom.Element e);

    public boolean isHover(org.w3c.dom.Element e);

    public boolean isActive(org.w3c.dom.Element e);

    public boolean isFocus(org.w3c.dom.Element e);

}
