/*
 * InlineContent.java
 * Copyright (c) 2004 Torbj�rn Gannholm
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
 * Represents the content of an "inline"-display element.
 * This can never be returned in a content-list, instead, a StylePush and a StylePop marker
 * is placed in the list around its child contents.
 */
class InlineContent implements Content {
    private Element _elem;
    private CascadedStyle _style;

    InlineContent(Element e, CascadedStyle style) {
        _elem = e;
        _style = style;
    }

    public Element getElement() {
        return _elem;
    }

    public CascadedStyle getStyle() {
        return _style;
    }

    public List getChildContent(Context c) {
        return ContentUtil.getChildContentList(c, this);
    }

    public String toString() {
        return "Block: " + _elem.getNodeName();
    }

}
