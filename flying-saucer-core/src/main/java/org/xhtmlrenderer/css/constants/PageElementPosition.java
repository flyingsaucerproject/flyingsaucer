/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
package org.xhtmlrenderer.css.constants;

import java.util.HashMap;
import java.util.Map;

public class PageElementPosition {
    private static final Map ALL = new HashMap();
    private static int _maxAssigned = 0;
    
    public final int FS_ID;
    
    private final String _ident;
    
    public static final PageElementPosition START = addValue("start");
    public static final PageElementPosition FIRST = addValue("first");
    public static final PageElementPosition LAST = addValue("last");
    public static final PageElementPosition LAST_EXCEPT = addValue("last-except");

    private PageElementPosition(String ident) {
        this._ident = ident;
        this.FS_ID = _maxAssigned++;
    }
    
    private final static PageElementPosition addValue(String ident) {
        PageElementPosition val = new PageElementPosition(ident);
        ALL.put(ident, val);
        return val;
    }
    
    public String toString() {
        return _ident;
    }
    
    public static PageElementPosition valueOf(String ident) {
        return (PageElementPosition)ALL.get(ident);
    }
    
    public int hashCode() {
        return FS_ID;
    }
    
    public boolean equals(Object o) {
        if (o == null || ! (o instanceof PageElementPosition)) {
            return false;
        }
        
        return FS_ID == ((PageElementPosition)o).FS_ID;
    }
}
