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
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2004-dec-05
 * Time: 16:23:09
 * To change this template use File | Settings | File Templates.
 */
public class TextContent implements Content {
    private Element _elem;//will need this for handling dynamic content!
    private CalculatedStyle _style;
    private StringBuffer _sb;

    public TextContent(Element e, CalculatedStyle style) {
        _elem = e;
        _style = style;
        _sb = new StringBuffer();
    }

    public Element getElement() {
        return _elem;
    }

    public CalculatedStyle getStyle() {
        return _style;
    }

    public Object getContent(Context c) {
        return _sb.toString();
    }

    public void append(String text) {
        _sb.append(text);
    }

    public String toString() {
        return "TextContent:\nStyle: " + _style + "\nText: " + _sb.toString();
    }

}
