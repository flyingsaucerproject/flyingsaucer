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
package org.xhtmlrenderer.swt;

import com.google.errorprone.annotations.CheckReturnValue;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.jspecify.annotations.Nullable;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.util.IOUtil;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.XRLog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.xhtmlrenderer.util.IOUtil.getInputStream;
import static org.xhtmlrenderer.util.ImageUtil.isEmbeddedBase64Image;

/**
 * Naive user agent, copy of org.xhtmlrenderer.swing.NaiveUserAgent (but
 * modified for SWT, of course).
 *
 * @author Vianney le Clément
 */
public class NaiveUserAgent implements UserAgentCallback {

    /**
     * an LRU cache
     */
    private final int _imageCacheCapacity = 16;
    private final Map<String, ImageResource> _imageCache = new LinkedHashMap<>(_imageCacheCapacity, 0.75f, true);

    @Nullable
    private String _baseURL;

    private final Device _device;

    /**
     * Creates a new instance of NaiveUserAgent
     */
    public NaiveUserAgent(Device device) {
        _device = device;
    }

    @Override
    public CSSResource getCSSResource(String uri) {
        return new CSSResource(getInputStream(resolveURI(uri)));
    }

    @Override
    public ImageResource getImageResource(String uri) {
        if (isEmbeddedBase64Image(uri)) {
            return loadEmbeddedBase64ImageResource(uri);
        }

        uri = resolveURI(uri);
        ImageResource ir = _imageCache.get(uri);
        // TODO: check that cached image is still valid
        if (ir == null) {
                try (InputStream is = getInputStream(resolveURI(uri))) {
                    if (is != null) {
                        ir = createImageResource(uri, is);
                        if (_imageCache.size() >= _imageCacheCapacity) {
                            // prevent the cache from growing too big
                            ImageResource old = _imageCache
                                    .remove(_imageCache.keySet().iterator().next());
                            ((SWTFSImage) old.getImage()).getImage().dispose();
                        }
                        _imageCache.put(uri, ir);
                    }
                } catch (SWTException | IOException e) {
                    XRLog.exception(
                            "Can't read image file; unexpected problem for URI '"
                            + uri + "'", e);
                }
        }
        if (ir == null) {
            ir = new ImageResource(uri, null);
        }
        return ir;
    }

    /**
     * Factory method to generate ImageResources from a given Image. May be
     * overridden in subclass.
     *
     * @param uri The URI for the image, resolved to an absolute URI.
     * @param is Stream of the image; may be null (for example, if image could
     * not be loaded).
     *
     * @return An ImageResource containing the image.
     */
    protected ImageResource createImageResource(@Nullable String uri, InputStream is) {
        return new ImageResource(uri, new SWTFSImage(new Image(_device, is), this, uri));
    }

    private ImageResource loadEmbeddedBase64ImageResource(final String uri) {
        byte[] image = ImageUtil.getEmbeddedBase64Image(uri);
        if (image != null) {
            return createImageResource(null, new ByteArrayInputStream(image));
        }
        return new ImageResource(null, null);
    }

    @Override
    public XMLResource getXMLResource(String uri) {
        if (uri == null) {
            XRLog.exception("null uri requested");
            return null;
        }
        try (InputStream inputStream = getInputStream(resolveURI(uri))) {
            if (inputStream == null) {
                XRLog.exception("couldn't get InputStream for " + uri);
                return null;
            }
            return XMLResource.load(inputStream);
        } catch (Exception e) {
            XRLog.exception("unable to load xml resource: " + uri, e);
            return null;
        }
    }

    /**
     * Gets the visited attribute of the NaiveUserAgent object
     */
    @Override
    public boolean isVisited(String uri) {
        return false;
    }

    @Override
    public void setBaseURL(String url) {
        _baseURL = url;
    }

    @Override
    @Nullable
    @CheckReturnValue
    public String resolveURI(@Nullable String uri) {
        if (uri == null) return null;

        if (_baseURL == null) {//first try to set a base URL
            try {
                URI result = new URI(uri);
                if (result.isAbsolute()) setBaseURL(result.toString());
            } catch (URISyntaxException e) {
                XRLog.exception("The default NaiveUserAgent could not use the URL as base url: " + uri, e);
            }
            if (_baseURL == null) { // still not set -> fallback to current working directory
                try {
                    setBaseURL(new File(".").toURI().toURL().toExternalForm());
                } catch (IOException e) {
                    XRLog.exception("The default NaiveUserAgent doesn't know how to resolve the base URL for %s (caused by: %s)".formatted(uri, e));
                    return null;
                }
            }
        }

        // _baseURL is guaranteed to be non-null at this point.
        // test if the URI is valid; if not, try to assign the base url as its parent
        Throwable t;
        try {
            URI result = new URI(uri);
            if (result.isAbsolute()) {
                return result.toString();
            }
            XRLog.load(uri + " is not a URL; may be relative. Testing using parent URL " + _baseURL);
            URI baseURI = new URI(_baseURL);
            if(!baseURI.isOpaque()) {
                // uri.resolve(child) only works for opaque URIs.
                // Otherwise, it would simply return child.
                return baseURI.resolve(result).toString();
            }
            // Fall back to previous resolution using URL
            try {
                return new URL(new URL(_baseURL), uri).toExternalForm();
            } catch (MalformedURLException ex) {
                t = ex;
            }
        } catch (URISyntaxException e) {
            t = e;
        }
        XRLog.exception("The default NaiveUserAgent cannot resolve the URL " + uri + " with base URL " + _baseURL, t);
        return null;
    }

    @CheckReturnValue
    @Nullable
    @Override
    public String getBaseURL() {
        return _baseURL;
    }

    /**
     * Dispose all images in cache and clean the cache.
     */
    public void disposeCache() {
        for (ImageResource ir : _imageCache.values()) {
            ((SWTFSImage) ir.getImage()).getImage().dispose();
        }
        _imageCache.clear();
    }

    @Override
    @CheckReturnValue
    public byte @Nullable [] getBinaryResource(String uri) {
        return IOUtil.readBytes(resolveURI(uri));
    }
}
