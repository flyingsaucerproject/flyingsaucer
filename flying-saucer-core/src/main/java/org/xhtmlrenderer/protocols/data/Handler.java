/*
 * {{{ header & license
 * Copyright (c) 2008 Sean Bright
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
package org.xhtmlrenderer.protocols.data;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {

    protected void parseURL(URL u, String spec, int start, int limit) {
        String sub = spec.substring(start, limit);

        // Make sure we have a comma
        if (sub.indexOf(',') < 0) {
            throw new RuntimeException("Improperly formatted data URL");
        }

        setURL(u, "data", "", -1, "", "", sub, "", "");
    }

    protected URLConnection openConnection(URL u) throws IOException {
        return new DataURLConnection(u);
    }

}
