/*
 * BlockContent.java
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
package org.xhtmlrenderer.layout.content;

import java.util.List;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.Context;


/**
 * Represents the content of a "block"-display element
 *
 * @author   Torbjörn Gannholm
 */
public class TableContent implements Content {
    /** Description of the Field */
    private Element _elem;
    /** Description of the Field */
    private CascadedStyle _style;
    /** Description of the Field */
    private List _childContent = null;

    /**
     * Constructor for the TableContent object
     *
     * @param e      PARAM
     * @param style  PARAM
     */
    TableContent( Element e, CascadedStyle style ) {
        _elem = e;
        _style = style;
    }

    /**
     * anonymous table
     *
     * @param childContent  PARAM
     * @param style         PARAM
     */
    TableContent( List childContent, CascadedStyle style ) {
        _childContent = childContent;
        _style = style;
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return   A string representation of the object.
     */
    public String toString() {
        return "Table: " + _elem.getNodeName();
    }

    /**
     * Gets the element attribute of the TableContent object
     *
     * @return   The element value
     */
    public Element getElement() {
        return _elem;
    }

    /**
     * Gets the style attribute of the TableContent object
     *
     * @return   The style value
     */
    public CascadedStyle getStyle() {
        return _style;
    }

    /**
     * Gets the childContent attribute of the TableContent object
     *
     * @param c  PARAM
     * @return   The childContent value
     */
    public List getChildContent( Context c ) {
        return ContentUtil.getChildContentList( c, this );
    }

}

