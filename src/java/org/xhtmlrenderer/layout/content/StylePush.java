/*
 * StylePush.java
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
import org.xhtmlrenderer.render.Style;


/**
 * A marker for changing the style of the first letter
 *
 * @author Torbjörn Gannholm
 */
public class StylePush {
    /**
     * Description of the Field
     */
    private String _pseudo;
    /**
     * Description of the Field
     */
    private Element _elem;//needed for dynamic stuff

    private Style _style;

    /**
     * Constructor for the StylePush object
     *
     * @param pseudo PARAM
     * @param e      PARAM
     */
    StylePush(String pseudo, Element e) {
        _pseudo = pseudo;
        _elem = e;
    }

    /**
     * Gets the pseudoElement attribute of the StylePush object
     *
     * @return The pseudoElement value
     */
    public String getPseudoElement() {
        return _pseudo;
    }

    /**
     * Gets the element attribute of the StylePush object
     *
     * @return The element value
     */
    public Element getElement() {
        return _elem;
    }

    public Style getStyle() {
        return _style;
    }

    public void setStyle(Style style) {
        _style = style;
    }
}

/*
 * $Id$
 *
 */
