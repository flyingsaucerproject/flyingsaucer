/*
 * TextContent.java
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
package org.xhtmlrenderer.layout.content;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.Context;

import java.util.List;

/**
 * Represents the content of text nodes and :before and :after content.
 * The associated element is the parent element.
 * The style is null, should be resolved from the context.
 * ChildContent is null, use getText
 */
public class TextContent implements Content {
    private Element _elem;//will need this for handling dynamic content!
    private String _pseudo;
    private String _text;
    private boolean removableWhitespace = false;

    TextContent(Element e, String text) {
        _elem = e;
        _text = text;
    }

    TextContent(String pseudoElement, Element e, String text) {
        _pseudo = pseudoElement;
        _elem = e;
        _text = text;
    }

    public String getPseudoElement() {
        return _pseudo;
    }

    public Element getElement() {
        return _elem;
    }

    public CascadedStyle getStyle() {
        return null;
    }

    public List getChildContent(Context c) {
        return null;
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
    }

    public String toString() {
        return "TextContent: " + _text;
    }

    public boolean isRemovableWhitespace() {
        return removableWhitespace;
    }

    public void setRemovableWhitespace(boolean removableWhitespace) {
        this.removableWhitespace = removableWhitespace;
    }
}
