/*
 * ReplacedContent.java
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
package org.xhtmlrenderer.layout.inline.content;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.style.CalculatedStyle;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2004-dec-05
 * Time: 17:07:42
 * To change this template use File | Settings | File Templates.
 */
public class ReplacedContent implements Content {
    private Element _elem;
    private CalculatedStyle _style;

    public ReplacedContent(Element e, CalculatedStyle style) {
        _elem = e;
        _style = style;
    }

    public Element getElement() {
        return _elem;
    }

    public CalculatedStyle getStyle() {
        return _style;
    }

    public String getText() {
        return _elem.getNodeValue();
    }

    public String toString() {
        return "FloatedBlock: " + _elem.getNodeName();
    }

}
