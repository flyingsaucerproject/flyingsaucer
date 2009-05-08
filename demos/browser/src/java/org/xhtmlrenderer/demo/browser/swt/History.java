/*
 * {{{ header & license
 * Copyright (c) 2007 Vianney le Clément
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
package org.xhtmlrenderer.demo.browser.swt;

import java.util.ArrayList;
import java.util.List;

public class History {

    private List _list = new ArrayList();
    private int _current = -1;

    public boolean hasBack() {
        return _current > 0;
    }

    public boolean hasForward() {
        return _current < _list.size() - 1;
    }

    public void add(String uri) {
        _current++;
        // clear forward history
        while (_list.size() > _current) {
            _list.remove(_current);
        }
        // add new uri
        _list.add(uri);
    }

    public boolean contains(String uri) {
        return _list.contains(uri);
    }

    public String back() {
        if (!hasBack()) {
            throw new IndexOutOfBoundsException();
        }
        _current--;
        return (String) _list.get(_current);
    }

    public String forward() {
        if (!hasForward()) {
            throw new IndexOutOfBoundsException();
        }
        _current++;
        return (String) _list.get(_current);
    }

    public String getCurrent() {
        if (_current < 0) {
            return null;
        } else {
            return (String) _list.get(_current);
        }
    }

}
