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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.XRLog;

/**
 * Naive user agent, copy of org.xhtmlrenderer.swing.NaiveUserAgent (but
 * modified for SWT, of course).
 *
 * @author Vianney le Clément
 *
 */
public class NaiveUserAgent implements UserAgentCallback {

    /**
     * an LRU cache
     */
    private int _imageCacheCapacity = 16;
    private LinkedHashMap _imageCache = new LinkedHashMap(_imageCacheCapacity,
            0.75f, true);

    private String _baseURL;

    private final Device _device;

    /**
     * Creates a new instance of NaiveUserAgent
     */
    public NaiveUserAgent(Device device) {
        _device = device;
    }

    /**
     * Gets a Reader for the resource identified
     *
     * @param uri PARAM
     * @return The stylesheet value
     */
    // TODO implement this with nio.
    protected InputStream getInputStream(String uri) {
        java.io.InputStream is = null;
        uri = resolveURI(uri);
        try {
            is = new URL(uri).openStream();
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (java.io.FileNotFoundException e) {
            XRLog.exception("item at URI " + uri + " not found");
        } catch (java.io.IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }
        return is;
    }

    public CSSResource getCSSResource(String uri) {
        return new CSSResource(getInputStream(uri));
    }

    public ImageResource getImageResource(String uri) {
        ImageResource ir = null;
        if (ImageUtil.isEmbeddedBase64Image(uri)) {
            ir = loadEmbeddedBase64ImageResource(uri);
        } else {
            uri = resolveURI(uri);
            ir = (ImageResource) _imageCache.get(uri);
            // TODO: check that cached image is still valid
            if (ir == null) {
                InputStream is = getInputStream(uri);
                if (is != null) {
                    try {
                        ir = createImageResource(uri, is);
                        if (_imageCache.size() >= _imageCacheCapacity) {
                            // prevent the cache from growing too big
                            ImageResource old = (ImageResource) _imageCache
                                    .remove(_imageCache.keySet().iterator().next());
                            ((SWTFSImage) old.getImage()).getImage().dispose();
                        }
                        _imageCache.put(uri, ir);
                    } catch (SWTException e) {
                        XRLog.exception(
                                "Can't read image file; unexpected problem for URI '"
                                + uri + "'", e);
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            // swallow
                        }
                    }
                }
            }
            if (ir == null) {
                ir = new ImageResource(uri, null);
            }
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
    protected ImageResource createImageResource(String uri, InputStream is) {
        return new ImageResource(uri, new SWTFSImage(new Image(_device, is), this, uri));
    }
    
    private ImageResource loadEmbeddedBase64ImageResource(final String uri) {
        byte[] image = ImageUtil.getEmbeddedBase64Image(uri);
        if (image != null) {
            return createImageResource(null, new ByteArrayInputStream(image));
        }
        return new ImageResource(null, null);
    }

    public XMLResource getXMLResource(String uri) {
        if (uri == null) {
            XRLog.exception("null uri requested");
            return null;
        }
        InputStream inputStream = getInputStream(uri);
        if (inputStream == null) {
            XRLog.exception("couldn't get InputStream for " + uri);
            return null;
        }
        XMLResource xmlResource;
        try {
            xmlResource = XMLResource.load(inputStream);
        } catch (Exception e) {
            XRLog.exception("unable to load xml resource: " + uri, e);
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // swallow
                }
            }
        }
        return xmlResource;
    }

    /**
     * Gets the visited attribute of the NaiveUserAgent object
     *
     * @param uri PARAM
     * @return The visited value
     */
    public boolean isVisited(String uri) {
        return false;
    }

    public void setBaseURL(String url) {
        _baseURL = url;
    }

    public String resolveURI(String uri) {
        if (uri == null) return null;
        String ret = null;
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
                } catch (Exception e1) {
                    XRLog.exception("The default NaiveUserAgent doesn't know how to resolve the base URL for " + uri);
                    return null;
                }
            }
        }
        // test if the URI is valid; if not, try to assign the base url as its parent
        try {
            URI result = new URI(uri);
            if (!result.isAbsolute()) {
                XRLog.load(uri + " is not a URL; may be relative. Testing using parent URL " + _baseURL);
                result=new URI(_baseURL).resolve(result);
            }
            ret = result.toString();
        } catch (URISyntaxException e) {
            XRLog.exception("The default NaiveUserAgent cannot resolve the URL " + uri + " with base URL " + _baseURL);
        }
        return ret;
    }

    public String getBaseURL() {
        return _baseURL;
    }

    /**
     * Dispose all images in cache and clean the cache.
     */
    public void disposeCache() {
        for (Iterator iter = _imageCache.values().iterator(); iter.hasNext();) {
            ImageResource ir = (ImageResource) iter.next();
            ((SWTFSImage) ir.getImage()).getImage().dispose();
        }
        _imageCache.clear();
    }

    public byte[] getBinaryResource(String uri) {
        InputStream is = getInputStream(uri);
        if (is==null) return null;
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buf = new byte[10240];
            int i;
            while ((i = is.read(buf)) != -1) {
                result.write(buf, 0, i);
            }
            is.close();
            is = null;

            return result.toByteArray();
        } catch (IOException e) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
