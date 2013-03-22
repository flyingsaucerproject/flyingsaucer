/*
 * {{{ header & license
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
 * }}}
 */
package org.xhtmlrenderer.layout;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.style.CalculatedStyle;

/**
 * All objects appearing the layout tree must implement this interface.  It
 * can roughly be thought of as a styled element (although an {@link InlineLayoutBox}
 * may be split across many lines) and some <code>Styleable</code> objects may not
 * define an element at all (e.g. anonymous inline boxes) and some 
 * <code>Styleable</code> objects don't correspond to a real element 
 * (e.g. <code>:before</code> and <code>:after</code> pseudo-elements))
 */
public interface Styleable {
    public CalculatedStyle getStyle();
    public void setStyle(CalculatedStyle style);
    
    public Element getElement();
    public void setElement(Element e);
    
    public String getPseudoElementOrClass();
}
