/*
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.render;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.extend.ContentFunction;
import org.xhtmlrenderer.layout.Styleable;

public class InlineBox implements Styleable
{
    private Element element;
    
    private String _text;
    private boolean _removableWhitespace;
    private boolean _startsHere;
    private boolean _endsHere;
    
    private Style _style;
    
    private ContentFunction _contentFunction;
    
    public InlineBox(String text) {
        _text = text;
    }
    
    public String getText() {
        return _text;
    }
    
    public void setText(String text) {
        _text = text;
    }

    public boolean isRemovableWhitespace() {
        return _removableWhitespace;
    }

    public void setRemovableWhitespace(boolean removeableWhitespace) {
        _removableWhitespace = removeableWhitespace;
    }

    public boolean isEndsHere() {
        return _endsHere;
    }

    public void setEndsHere(boolean endsHere) {
        _endsHere = endsHere;
    }

    public boolean isStartsHere() {
        return _startsHere;
    }

    public void setStartsHere(boolean startsHere) {
        _startsHere = startsHere;
    }

    public Style getStyle() {
        return _style;
    }

    public void setStyle(Style style) {
        _style = style;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public ContentFunction getContentFunction() {
        return _contentFunction;
    }

    public void setContentFunction(ContentFunction contentFunction) {
        _contentFunction = contentFunction;
    }

    public boolean isDynamicFunction() {
        return _contentFunction != null;
    }    
}
