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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DemosNavigation {

    private static final String FILE_LIST_URL = "demo:/demos/file-list.txt";

    private List _demos;
    private int _current;
    private boolean _lock = false;

    public DemosNavigation(BrowserUserAgent uac) {
        BufferedReader reader = null;
        _demos = new ArrayList();
        try {
            URL url = new URL(uac.resolveFullURI(FILE_LIST_URL));
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                _demos.add(new Demo(fields[1], fields[0]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        _current = -1;
    }

    public int size() {
        return _demos.size();
    }

    public Iterator iterate() {
        return _demos.iterator();
    }

    public Demo getCurrent() {
        if (_current == -1) {
            return null;
        }
        return (Demo) _demos.get(_current);
    }

    public void setCurrent(int index) {
        if (index < 0 || index >= _demos.size()) {
            throw new IndexOutOfBoundsException();
        }
        if (!_lock) {
            _current = index;
            _lock = true;
        }
    }

    public Demo next() {
        if (_demos.isEmpty()) {
            return null;
        }
        if (!_lock) {
            _current++;
            if (_current >= _demos.size()) {
                _current = 0;
            }
            _lock = true;
        }
        return getCurrent();
    }

    public Demo previous() {
        if (_demos.isEmpty()) {
            return null;
        }
        if (!_lock) {
            _current--;
            if (_current < 0) {
                _current = _demos.size() - 1;
            }
            _lock = true;
        }
        return getCurrent();
    }

    public void setCurrent(Demo demo) {
        setCurrent(_demos.indexOf(demo));
    }

    /**
     * Permit navigation again (e.g. when the document has loaded)
     */
    public void unlock() {
        _lock = false;
    }

    public static class Demo {
        String _url, _name;

        public Demo(String url, String name) {
            _url = url;
            _name = name;
        }

        public String getUrl() {
            return _url;
        }

        public String getName() {
            return _name;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Demo)) return false;

            Demo demo = (Demo) o;

            if (!_name.equals(demo._name)) return false;
            if (!_url.equals(demo._url)) return false;

            return true;
        }

        public int hashCode() {
            int result = _url.hashCode();
            result = 31 * result + _name.hashCode();
            return result;
        }
    }

}
