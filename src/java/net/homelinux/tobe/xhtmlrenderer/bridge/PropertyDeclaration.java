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

package net.homelinux.tobe.xhtmlrenderer.bridge;

import com.pdoubleya.xhtmlrenderer.css.impl.XRPropertyImpl;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class PropertyDeclaration implements net.homelinux.tobe.xhtmlrenderer.PropertyDeclaration {
    
    com.pdoubleya.xhtmlrenderer.css.XRProperty base;
    boolean important;
    int origin;
    
    /** Creates a new instance of PropertyDeclaration */
    public PropertyDeclaration(com.pdoubleya.xhtmlrenderer.css.XRProperty p, boolean imp, int orig) {
        base = p;
        important = imp;
        origin = orig;
    }
    
    public int getImportanceAndOrigin() {
        if(origin == net.homelinux.tobe.xhtmlrenderer.Stylesheet.USER_AGENT) return net.homelinux.tobe.xhtmlrenderer.PropertyDeclaration.USER_AGENT;
        else if(origin == net.homelinux.tobe.xhtmlrenderer.Stylesheet.USER) {
            if(important) return net.homelinux.tobe.xhtmlrenderer.PropertyDeclaration.USER_IMPORTANT;
            return net.homelinux.tobe.xhtmlrenderer.PropertyDeclaration.USER_NORMAL;
        }
        else {
            if(important) return net.homelinux.tobe.xhtmlrenderer.PropertyDeclaration.AUTHOR_IMPORTANT;
            return net.homelinux.tobe.xhtmlrenderer.PropertyDeclaration.AUTHOR_NORMAL;
        }
    }
    
    public String getName() {
        return base.propertyName();
    }
    
    public org.w3c.dom.css.CSSValue getValue() {
        return base.specifiedValue().cssValue();
    }
    
}
