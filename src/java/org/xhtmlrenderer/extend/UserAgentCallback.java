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

import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.resource.XMLResource;

import java.io.InputStream;


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
     * Returns an {@link java.io.InputStream} for a resource identified by a
     * URI (String). Returns  null if UserAgent does not wish to access the
     * resource.
     *
     * @param uri The URI for the resource (any string that the instance can resolve).
     * @return A Reader for the stylesheet, or null if it can't be read
     *         or if the stylesheet should be ignored.
     */
    public InputStream getInputStream(String uri);

    public CSSResource getCSSResource(String uri);

    public ImageResource getImageResource(String uri);

    public XMLResource getXMLResource(String uri);

    /**
     * UserAgent should consider if it should answer truthfully or not for
     * privacy reasons
     *
     * @param uri PARAM
     * @return The visited value
     */
    public boolean isVisited(String uri);

    /**
     * Does not need to be a correct URL, only an identifier that the
     * implementation can resolve.
     *
     * @param url
     */
    void setBaseURL(String url);

    /**
     * @return the base uri, possibly in the implementations private uri-space
     */
    String getBaseURL();

    /**
     * Used to find a uri that may be relative to the BaseURL.
     * The returned value will always only be used via methods in the same
     * implementation of this interface, therefore may be a private uri-space.
     *
     * @param uri an absolute or relative (to baseURL) uri to be resolved.
     * @return the full uri in uri-spaces known to the current implementation.
     */
    String resolveURI(String uri);
}

