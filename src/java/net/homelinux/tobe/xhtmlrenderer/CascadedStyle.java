/*
 *
 * CascadedStyle.java
 * Copyright (c) 2004 Patrick Wright
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

package net.homelinux.tobe.xhtmlrenderer;

public interface CascadedStyle {
    /**
     * Value of a single PropertyDeclaration, the one that the cascade has determined for this element, by name.
     *
     * @param propName  PARAM
     * @return          Returns
     */
    PropertyDeclaration propertyByName( String propName );


    /**
     * Returns true if the named property was defined and has a value in this
     * rule set.
     *
     * @param propName  PARAM
     * @return          Returns
     */
    boolean hasProperty( String propName );
    
    java.util.Iterator getPropertyDeclarations();

}
