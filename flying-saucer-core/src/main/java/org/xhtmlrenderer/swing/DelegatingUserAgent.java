/*
 * DelegatingUserAgent.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
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
 *
 */
package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.util.IOUtil;
import org.xhtmlrenderer.util.StreamResource;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * <p>NaiveUserAgent is a simple implementation of {@link org.xhtmlrenderer.extend.UserAgentCallback} which places no restrictions on what
 * XML, CSS or images are loaded, and reports visited links without any filtering. The most straightforward process
 * available in the JDK is used to load the resources in question--either using java.io or java.net classes.
 * <p/>
 * <p>The NaiveUserAgent has a small cache for images,
 * the size of which (number of images) can be passed as a constructor argument. There is no automatic cleaning of
 * the cache; call {@link #shrinkImageCache()} to remove the least-accessed elements--for example, you might do this
 * when a new document is about to be loaded. The NaiveUserAgent is also a DocumentListener; if registered with a
 * source of document events (like the panel hierarchy), it will respond to the
 * {@link org.xhtmlrenderer.event.DocumentListener#documentStarted()} call and attempt to shrink its cache.
 * <p/>
 * <p>This class is meant as a starting point--it will work out of the box, but you should really implement your
 * own, tuned to your application's needs.
 *
 * @author Torbjoern Gannholm
 */
public class DelegatingUserAgent implements UserAgentCallback, DocumentListener {
    private UriResolver _uriResolver;
    private ImageResourceLoader _imageResourceLoader;


    /**
     * Creates a new instance of NaiveUserAgent with a max image cache of 16 images.
     */
    public DelegatingUserAgent() {
        this._uriResolver = new UriResolver();
    }

    public void setImageResourceLoader(ImageResourceLoader loader) {
        _imageResourceLoader = loader;
    }

    /**
     * If the image cache has more items than the limit specified for this class, the least-recently used will
     * be dropped from cache until it reaches the desired size.
     */
    public void shrinkImageCache() {
        _imageResourceLoader.shrink();
    }

    /**
     * Empties the image cache entirely.
     */
    public void clearImageCache() {
        _imageResourceLoader.clear();
    }

    /**
     * Gets a Reader for the resource identified
     *
     * @param uri PARAM
     * @return The stylesheet value
     */
    protected InputStream resolveAndOpenStream(String uri) {
        return IOUtil.openStreamAtUrl(_uriResolver.resolve(uri));
    }

    /**
     * Retrieves the CSS located at the given URI.  It's assumed the URI does point to a CSS file--the URI will
     * be accessed (using java.io or java.net), opened, read and then passed into the CSS parser.
     * The result is packed up into an CSSResource for later consumption.
     *
     * @param uri Location of the CSS source.
     * @return A CSSResource containing the parsed CSS.
     */
    public CSSResource getCSSResource(String uri) {
        return new CSSResource(resolveAndOpenStream(uri));
    }

    /**
     * Retrieves the image located at the given URI. It's assumed the URI does point to an image--the URI will
     * be accessed (using java.io or java.net), opened, read and then passed into the JDK image-parsing routines.
     * The result is packed up into an ImageResource for later consumption.
     *
     * @param uri Location of the image source.
     * @return An ImageResource containing the image.
     */
    public ImageResource getImageResource(String uri) {
        return _imageResourceLoader.get(resolveURI(uri));
    }

    /**
     * Retrieves the XML located at the given URI. It's assumed the URI does point to a XML--the URI will
     * be accessed (using java.io or java.net), opened, read and then passed into the XML parser (XMLReader)
     * configured for Flying Saucer. The result is packed up into an XMLResource for later consumption.
     *
     * @param uri Location of the XML source.
     * @return An XMLResource containing the image.
     */
    public XMLResource getXMLResource(String uri) {
        String ruri = _uriResolver.resolve(uri);
        StreamResource sr = new StreamResource(ruri);
        try {
            sr.connect();
            BufferedInputStream bis = sr.bufferedStream();
            return XMLResource.load(bis);
        } catch (IOException e) {
            return null;
        } finally {
            sr.close();
        }
    }

    public byte[] getBinaryResource(String uri) {
        String ruri = _uriResolver.resolve(uri);
        StreamResource sr = new StreamResource(ruri);
        try {
            sr.connect();
            BufferedInputStream bis = sr.bufferedStream();
            ByteArrayOutputStream result = new ByteArrayOutputStream(sr.hasStreamLength() ? sr.streamLength() : 4 * 1024);
            byte[] buf = new byte[10240];
            int i;
            while ((i = bis.read(buf)) != -1) {
                result.write(buf, 0, i);
            }

            return result.toByteArray();
        } catch (IOException e) {
            return null;
        } finally {
            sr.close();
        }
    }


    /**
     * Returns true if the given URI was visited, meaning it was requested at some point since initialization.
     *
     * @param uri A URI which might have been visited.
     * @return Always false; visits are not tracked in the NaiveUserAgent.
     */
    public boolean isVisited(String uri) {
        return false;
    }

    /**
     * URL relative to which URIs are resolved.
     *
     * @param uri A URI which anchors other, possibly relative URIs.
     */
    public void setBaseURL(String uri) {
        _uriResolver.setBaseUri(uri);
    }

    /**
     * Resolves the URI; if absolute, leaves as is, if relative, returns an absolute URI based on the baseUrl for
     * the agent.
     *
     * @param uri A URI, possibly relative.
     * @return A URI as String, resolved, or null if there was an exception (for example if the URI is malformed).
     */
    public String resolveURI(String uri) {
        return _uriResolver.resolve(uri);
    }

    /**
     * Returns the current baseUrl for this class.
     */
    public String getBaseURL() {
        return _uriResolver.getBaseUri();
    }

    public void documentStarted() {
        _imageResourceLoader.stopLoading();
        shrinkImageCache();
    }

    public void documentLoaded() { /* ignore*/ }

    public void onLayoutException(Throwable t) { /* ignore*/ }

    public void onRenderException(Throwable t) { /* ignore*/ }

    public void setRepaintListener(RepaintListener listener) {
        //_imageResourceLoader.setRepaintListener(listener);
    }
}
