/*
 * UserAgentCallback.java
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
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
package org.xhtmlrenderer.extend;

import java.awt.Image;


/**
 * To be implemented by any user agent using the panel <p/>
 *
 * Provides services only the current user agent should provide
 *
 * @author   Torbjörn Gannholm
 */
public interface UserAgentCallback {

    /**
     * returns null if UserAgent does not wish to access the URI
     *
     * @param uri  PARAM
     * @return     The stylesheet value
     */
    public java.io.Reader getStylesheet( String uri );

    /**
     * returns null if UserAgent does not wish to access the URI
     *
     * @param uri  PARAM
     * @return     The image value
     */
    public Image getImage( String uri );

    /**
     * UserAgent should consider if it should answer truthfully or not for
     * privacy reasons
     *
     * @param uri  PARAM
     * @return     The visited value
     */
    public boolean isVisited( String uri );
}

