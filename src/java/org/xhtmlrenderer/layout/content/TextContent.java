/*
 * TextContent.java
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
 * Represents the content of text nodes and :before and :after content. The
 * associated element is the parent element. The style is null, should be
 * resolved from the context. ChildContent is null, use getText
 *
 * @author Torbjörn Gannholm
 */
public class TextContent implements Content {
    /**
     * Description of the Field
     */
    private Element _elem;//will need this for handling dynamic content!
    /**
     * Description of the Field
     */
    private String _pseudo;
    /**
     * Description of the Field
     */
    private String _text;
    /**
     * Description of the Field
     */
    private boolean removableWhitespace = false;

    /**
     * Constructor for the TextContent object
     *
     * @param e    PARAM
     * @param text PARAM
     */
    TextContent(Element e, String text) {
        _elem = e;
        _text = text;
    }

    /**
     * Constructor for the TextContent object
     *
     * @param pseudoElement PARAM
     * @param e             PARAM
     * @param text          PARAM
     */
    TextContent(String pseudoElement, Element e, String text) {
        _pseudo = pseudoElement;
        _elem = e;
        _text = text;
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {
        return "TextContent: " + _text;
    }

    /**
     * Sets the text attribute of the TextContent object
     *
     * @param text The new text value
     */
    public void setText(String text) {
        _text = text;
    }

    /**
     * Sets the removableWhitespace attribute of the TextContent object
     *
     * @param removableWhitespace The new removableWhitespace value
     */
    public void setRemovableWhitespace(boolean removableWhitespace) {
        this.removableWhitespace = removableWhitespace;
    }

    /**
     * Gets the pseudoElement attribute of the TextContent object
     *
     * @return The pseudoElement value
     */
    public String getPseudoElement() {
        return _pseudo;
    }

    /**
     * Gets the element attribute of the TextContent object
     *
     * @return The element value
     */
    public Element getElement() {
        return _elem;
    }

    /**
     * Gets the style attribute of the TextContent object
     *
     * @return The style value
     */
    public CascadedStyle getStyle() {
        return null;
    }

    /**
     * Gets the childContent attribute of the TextContent object
     *
     * @param c PARAM
     * @return The childContent value
     */
    public List getChildContent(LayoutContext c) {
        return null;
    }

    /**
     * Gets the text attribute of the TextContent object
     *
     * @return The text value
     */
    public String getText() {
        return _text;
    }

    /**
     * Gets the removableWhitespace attribute of the TextContent object
     *
     * @return The removableWhitespace value
     */
    public boolean isRemovableWhitespace() {
        return removableWhitespace;
    }
}

