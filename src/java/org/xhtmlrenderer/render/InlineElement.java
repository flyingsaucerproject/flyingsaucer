/*
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
package org.xhtmlrenderer.render;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.LayoutContext;

import java.util.LinkedList;

/**
 * User: tobe
 * Date: 2005-nov-02
 * Time: 23:08:21
 */
public class InlineElement {
    private InlineElement parent;
    private Style startStyle;
    private Style endStyle;
    private Element element;
    private String pseudoElement;

    public InlineElement(Element element, String pseudoElement, InlineElement parent) {
        this.element = element;
        this.pseudoElement = pseudoElement;
        this.parent = parent;
    }

    public void setStartStyle(Style style) {
        startStyle = style;
    }

    public void setEndStyle(Style style) {
        endStyle = style;
    }

    public InlineElement getParent() {
        return parent;
    }

    public void restyleStart(LayoutContext c, int pushstyles, LinkedList pushedStyles) {
        if (pushstyles > 1) parent.restyleStart(c, pushstyles--, pushedStyles);
        CascadedStyle cascaded;
        if (pseudoElement == null) {
            cascaded = c.getCss().getCascadedStyle(element, true);
        } else {
            cascaded = c.getCss().getPseudoElementStyle(element, pseudoElement);
        }
        if (pushedStyles != null) pushedStyles.addLast(cascaded);
        c.pushStyle(cascaded);
        startStyle.setCalculatedStyle(c.getCurrentStyle());
    }

    public void restyleEnd(LayoutContext c, int popstyles) {
        endStyle.setCalculatedStyle(c.getCurrentStyle());
        c.popStyle();
        if (popstyles > 1) parent.restyleEnd(c, popstyles--);
    }
}

/*
 * $Id$
 *
 */
