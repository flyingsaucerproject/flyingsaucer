/*
 * TableCellContent.java
 * Copyright (c) 2005 Torbjörn Gannholm
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
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.LayoutContext;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-jun-04
 * Time: 22:52:27
 * To change this template use File | Settings | File Templates.
 */
public class TableCellContent extends AbstractCachingContent implements Content {
    /**
     * Description of the Field
     */
    final private Element _elem;
    /**
     * Description of the Field
     */
    final private CascadedStyle _style;
    /**
     * Description of the Field
     */
    final private LinkedList _children;

    /**
     * Constructor for the TableContent object
     *
     * @param e     PARAM
     * @param style PARAM
     */
    TableCellContent(Element e, CascadedStyle style) {
        _elem = e;
        _style = style;
        _children = null;
    }

    /**
     * anonymous table
     */
    TableCellContent() {
        _elem = null;
        _style = null;
        _children = new LinkedList();
    }

    /**
     * for anonymous tables, child content is added as it is parsed by ContentUtil
     */
    void addChild(Node e) {
        _children.addLast(e);
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {
        return "Table: " + _elem.getNodeName();
    }

    /**
     * Gets the element attribute of the TableContent object
     *
     * @return The element value
     */
    public Element getElement() {
        return _elem;
    }

    /**
     * Gets the style attribute of the TableContent object
     *
     * @return The style value
     */
    public CascadedStyle getStyle() {
        return _style;
    }

    protected List makeChildContent(LayoutContext c) {
        return ContentUtil.getChildContentList(c, this);
    }

    Iterator getChildIterator() {
        if (_children != null) return _children.iterator();
        return null;
    }
}
