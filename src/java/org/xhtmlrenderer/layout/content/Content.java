/*
 * Content.java
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

import java.util.List;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.layout.Context;


/**
 * Represents content in the visual model. This is the link between element,
 * style and renderable content.
 *
 * @author   Torbjörn Gannholm
 */
public interface Content {
    /**
     * Gets the element attribute of the Content object
     *
     * @return   The element value
     */
    public Element getElement();

    /**
     * @param c
     * @return   either a String (for TextContent) or a List of Content objects.
     *      The List may be headed by FirstLineStyle and/or FirstLetterStyle if
     *      these are applicable
     */
    public List getChildContent( Context c );

    /**
     * Gets the style attribute of the Content object
     *
     * @return   The style value
     */
    public CascadedStyle getStyle();
}

