/*
 * {{{ header & license
 * PropertyFactory.java
 * Copyright (c) 2004 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.pdoubleya.xhtmlrenderer.css.factory;

import java.util.*;
import org.w3c.dom.css.CSSStyleDeclaration;


/**
 * A PropertyFactory takes CSS style declarations as SAC interface instances,
 * and generates XRProperties from them, to help with processing shorthand
 * properties.
 *
 * @author   Patrick Wright
 */
public interface PropertyFactory {
    /**
     * Explodes a single property declaration into one or more XRProperty
     * instances. The exact form of the explosion is left to the implementing
     * class.
     *
     * @param style     The CSSStyleDeclaration from the SAC parser.
     * @param propName  The String property name for the property to explode.
     * @param sequence  Sequence in which the declaration was found in the
     *      containing stylesheet.
     * @return          Iterator of one or more XRProperty instances representing the exploded values.
     */
    Iterator explodeProperties( CSSStyleDeclaration style, String propName, int sequence );
}

