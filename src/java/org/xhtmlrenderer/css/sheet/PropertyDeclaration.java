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

package org.xhtmlrenderer.css.sheet;

import org.xhtmlrenderer.css.impl.XRPropertyImpl;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class PropertyDeclaration {

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

    /** ImportanceAndOrigin of stylesheet - how many different */
    public final static int IMPORTANCE_AND_ORIGIN_COUNT = 6;

    
    org.xhtmlrenderer.css.XRProperty base;
    boolean important;
    int origin;
    
    /** Creates a new instance of PropertyDeclaration */
    public PropertyDeclaration(org.xhtmlrenderer.css.XRProperty p, boolean imp, int orig) {
        base = p;
        important = imp;
        origin = orig;
    }
    
    public int getImportanceAndOrigin() {
        if(origin == Stylesheet.USER_AGENT) return PropertyDeclaration.USER_AGENT;
        else if(origin == Stylesheet.USER) {
            if(important) return PropertyDeclaration.USER_IMPORTANT;
            return PropertyDeclaration.USER_NORMAL;
        }
        else {
            if(important) return PropertyDeclaration.AUTHOR_IMPORTANT;
            return PropertyDeclaration.AUTHOR_NORMAL;
        }
    }
    
    public String getName() {
        return base.propertyName();
    }
    
    public org.w3c.dom.css.CSSValue getValue() {
        return base.specifiedValue().cssValue();
    }
    
}
