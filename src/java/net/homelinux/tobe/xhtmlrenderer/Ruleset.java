/*
 * {{{ header & license
 * Ruleset.java
 * Copyright (c) 2004 Torbjörn Gannholm
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
/*
 * Ruleset.java
 *
 * Created on den 29 augusti 2004, 22:22
 */

package net.homelinux.tobe.xhtmlrenderer;

/**
 *
 * @author  Torbjörn Gannholm
 */
public interface Ruleset {
    
    public org.w3c.css.sac.SelectorList getSelectorList();
    
    /** the Objects from the iterator should be of type PropertyDeclaration */
    public java.util.Iterator getPropertyDeclarations();
}
