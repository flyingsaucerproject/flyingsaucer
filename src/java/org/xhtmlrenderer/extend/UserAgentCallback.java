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

import java.awt.*;


/**
 * <p>To be implemented by any user agent using the panel. "User agent" is a
 * term defined by the W3C in the documentation for XHTML and CSS; in most
 * cases, you can think of this as the rendering component for a browser.</p>
 * <p/>
 * <p>This interface defines a simple callback mechanism for Flying Saucer to
 * interact with a user agent. The FS toolkit provides a default implementation
 * for this interface which in most cases you can leave as is. You can provide your
 * own UserAgentCallback when constructing an {@link org.xhtmlrenderer.simple.XHTMLPanel}
 * or {@link org.xhtmlrenderer.swing.BasicPanel}.</p>
 *
 * @author Torbjörn Gannholm
 */
public interface UserAgentCallback {

    /**
     * Returns a {@link java.io.Reader} for a CSS stylesheet identified by a
     * URI (String). Returns  null if UserAgent does not wish to access the
     * stylesheet or URI.
     *
     * @param uri The URI for a CSS stylesheet.
     * @return A Reader for the stylesheet, or null if it can't be read
     *         or if the stylesheet should be ignored.
     */
    public java.io.Reader getReader(String uri);

    /**
     * Returns an {@link java.awt.Image} for a given URI (String), or null if the Image
     * is not available or should not be rendered.
     *
     * @param uri The URI for an Image.
     * @return A Reader for the image, or null if it can't or shouldn't be
     *         rendered.
     */
    public Image getImage(String uri);

    /**
     * UserAgent should consider if it should answer truthfully or not for
     * privacy reasons
     *
     * @param uri PARAM
     * @return The visited value
     */
    public boolean isVisited(String uri);

    void setBaseURL(String url);

    String resolveURI(String uri);

    String getBaseURL();
}

