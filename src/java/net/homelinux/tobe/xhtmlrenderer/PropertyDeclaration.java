/*
 *
 * PropertyDeclaration.java
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

package net.homelinux.tobe.xhtmlrenderer;

/**
 *
 * @author  Torbjörn Gannholm
 */
public interface PropertyDeclaration {

    /** ImportanceAndOrigin of stylesheet - user agent */
    public final static int CSS_DEFAULT = 0;

    /** ImportanceAndOrigin of stylesheet - user agent */
    public final static int USER_AGENT = 1;

    /** ImportanceAndOrigin of stylesheet - user normal */
    public final static int USER_NORMAL = 2;

    /** ImportanceAndOrigin of stylesheet - author normal */
    public final static int AUTHOR_NORMAL = 3;

    /** ImportanceAndOrigin of stylesheet - author important */
    public final static int AUTHOR_IMPORTANT = 4;

    /** ImportanceAndOrigin of stylesheet - user important */
    public final static int USER_IMPORTANT = 5;

    public int getImportanceAndOrigin();
    
    public String getName();
    
    /** the value should be a primitive value, no shorthand notations */
    public org.w3c.dom.css.CSSValue getValue();
    
}
