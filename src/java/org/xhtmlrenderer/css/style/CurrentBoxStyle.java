/*
 *
 * CurrentBoxStyle.java
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

package org.xhtmlrenderer.css.style;

/**
 * Represents the outer box to be used for evaluating positioning of internal boxes
 *
 * @author Torbjörn Gannholm
 */
public class CurrentBoxStyle extends CalculatedStyle {

    java.awt.Rectangle _rect;

    /**
     * Creates a new instance of CurrentBoxStyle
     */
    public CurrentBoxStyle(java.awt.Rectangle rect) {
        _rect = rect;
    }

    /**
     * Returns true if property has been defined in this style.
     *
     * @param propName PARAM
     * @return Returns
     */
    public boolean hasProperty(String propName) {
        if (propName.equals("width")) return true;
        if (propName.equals("height")) return true;
        if (propName.equals("top")) return true;
        if (propName.equals("left")) return true;
        if (propName.equals("bottom")) return true;
        if (propName.equals("right")) return true;
        return false;
    }


    /**
     * Returns a XRProperty by name. Because we are a derived style, the
     * property will already be resolved at this point--the method is
     * synchronized in order to allow this resolution to happen safely. Thus, on
     * this XRProperty you can call actualValue() to get something meaningful.
     *
     * @param propName PARAM
     * @return Returns
     */
    public DerivedProperty propertyByName(String propName) {
        //TODO: return these values when DerivedProperty and DerivedValue are disconnected from CSS
        return null;
    }
}
