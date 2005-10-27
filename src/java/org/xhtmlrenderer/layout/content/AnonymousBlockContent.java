/*
 * AnonymousBlockContent.java
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

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.LayoutContext;

import java.util.List;


/**
 * Represents content that should be layed out as an anonymous block. The
 * associated element is the parent element.
 *
 * @author Torbjörn Gannholm
 */
public class AnonymousBlockContent implements Content {
    /**
     * Description of the Field
     */
    private Element _elem;
    /**
     * Description of the Field
     */
    private List _inline;

    /**
     * Constructor for the AnonymousBlockContent object
     *
     * @param parent     PARAM
     * @param inlineList PARAM
     */
    AnonymousBlockContent(Element parent, List inlineList) {
        _elem = parent;
        _inline = inlineList;
    }

    /**
     * Gets the element attribute of the AnonymousBlockContent object
     *
     * @return The element value
     */
    public Element getElement() {
        return _elem;
    }

    /**
     * Gets the childContent attribute of the AnonymousBlockContent object
     *
     * @param c PARAM
     * @return The childContent value
     */
    public List getChildContent(LayoutContext c) {
        return _inline;
    }

    /**
     * Gets the style attribute of the AnonymousBlockContent object
     *
     * @return The style value
     */
    public CascadedStyle getStyle() {
        return null;
    }
}

