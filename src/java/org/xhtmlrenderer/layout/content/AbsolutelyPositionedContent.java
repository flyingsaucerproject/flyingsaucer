/*
 * AbsolutelyPositionedContent.java
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
import org.xhtmlrenderer.layout.Context;

import java.util.List;


/**
 * Represents content that is to be positioned out of the normal rendering
 * context
 *
 * @author Torbjörn Gannholm
 */
public class AbsolutelyPositionedContent extends AbstractCachingContent implements Content {
    /**
     * Description of the Field
     */
    private Element _elem;
    /**
     * Description of the Field
     */
    private CascadedStyle _style;

    /**
     * Constructor for the AbsolutelyPositionedContent object
     *
     * @param e     PARAM
     * @param style PARAM
     */
    AbsolutelyPositionedContent(Element e, CascadedStyle style) {
        _elem = e;
        _style = style;
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {
        return "AbsolutelyPositioned: " + _elem.getNodeName();
    }

    /**
     * Gets the element attribute of the AbsolutelyPositionedContent object
     *
     * @return The element value
     */
    public Element getElement() {
        return _elem;
    }

    /**
     * Gets the style attribute of the AbsolutelyPositionedContent object
     *
     * @return The style value
     */
    public CascadedStyle getStyle() {
        return _style;
    }

    protected List makeChildContent(Context c) {
        return ContentUtil.getChildContentList(c, this);
    }
}

