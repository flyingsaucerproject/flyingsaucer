/*
 * NaiveUserAgent.java
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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import javax.imageio.ImageIO;

import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.util.FontUtil;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.XRLog;

/**
 * <p>NaiveUserAgent is a simple implementation of {@link UserAgentCallback} which places no restrictions on what
 * XML, CSS or images are loaded, and reports visited links without any filtering. The most straightforward process
 * available in the JDK is used to load the resources in question--either using java.io or java.net classes.
 *
 * <p>The NaiveUserAgent has a small cache for images,
 * the size of which (number of images) can be passed as a constructor argument. There is no automatic cleaning of
 * the cache; call {@link #shrinkImageCache()} to remove the least-accessed elements--for example, you might do this
 * when a new document is about to be loaded. The NaiveUserAgent is also a DocumentListener; if registered with a
 * source of document events (like the panel hierarchy), it will respond to the
 * {@link org.xhtmlrenderer.event.DocumentListener#documentStarted()} call and attempt to shrink its cache.
 *
 * <p>This class is meant as a starting point--it will work out of the box, but you should really implement your
 * own, tuned to your application's needs.
 *
 * @author Torbjoern Gannholm
 */
public class NaiveUserAgent implements UserAgentCallback, DocumentListener {

    private static final int DEFAULT_IMAGE_CACHE_SIZE = 16;
    /**
     * a (simple) LRU cache
     */
    protected LinkedHashMap _imageCache;
    private int _imageCacheCapacity;
    private String _baseURL;

    /**
     * Creates a new instance of NaiveUserAgent with a max image cache of 16 images.
     */
    public NaiveUserAgent() {
        this(DEFAULT_IMAGE_CACHE_SIZE);
    }

    /**
     * Creates a new NaiveUserAgent with a cache of a specific size.
     *
     * @param imgCacheSize Number of images to hold in cache before LRU images are released.
     */
    public NaiveUserAgent(final int imgCacheSize) {
        this._imageCacheCapacity = imgCacheSize;

        // note we do *not* override removeEldestEntry() here--users of this class must call shrinkImageCache().
        // that's because we don't know when is a good time to flush the cache
        this._imageCache = new java.util.LinkedHashMap(_imageCacheCapacity, 0.75f, true);
    }

    /**
     * If the image cache has more items than the limit specified for this class, the least-recently used will
     * be dropped from cache until it reaches the desired size.
     */
    public void shrinkImageCache() {
        int ovr = _imageCache.size() - _imageCacheCapacity;
        Iterator it = _imageCache.keySet().iterator();
        while (it.hasNext() && ovr-- > 0) {
            it.next();
            it.remove();
        }
    }

    /**
     * Empties the image cache entirely.
     */
    public void clearImageCache() {
        _imageCache.clear();
    }

    /**
     * Gets a Reader for the resource identified
     *
     * @param uri PARAM
     * @return The stylesheet value
     */
    //TOdO:implement this with nio.
    protected InputStream resolveAndOpenStream(final String uri) {
        java.io.InputStream is = null;
        String resolvedUri = resolveURI(uri);
        try {
            if (FontUtil.isEmbeddedBase64Font(uri)) {
                is = FontUtil.getEmbeddedBase64Data(uri);
            } else {
                is = openStream(resolvedUri);
            }
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("bad URL given: " + resolvedUri, e);
        } catch (java.io.FileNotFoundException e) {
            XRLog.exception("item at URI " + resolvedUri + " not found");
        } catch (java.io.IOException e) {
            XRLog.exception("IO problem for " + resolvedUri, e);
        }
        return is;
    }
    
    protected InputStream openStream(String uri) throws MalformedURLException, IOException {
        return openConnection(uri).getInputStream();
    }

    /**
     * Opens a connections to uri.
     * 
     * This can be overwritten to customize handling of connections by type.
     * 
     * @param uri the uri to connect to
     * @return URLConnection opened connection to uri
     * @throws IOException if an I/O exception occurs.
     */
    protected URLConnection openConnection(String uri) throws IOException {
        URLConnection connection = new URL(uri).openConnection();
        if (connection instanceof HttpURLConnection) {
            connection = onHttpConnection((HttpURLConnection) connection);
        }
        return connection;
    }

    /**
     * Customized handling of @link{HttpUrlConnection}.
     * 
     * 
     * @param origin the original connection
     * @return @link{URLConnection} 
     * 
     * @throws MalformedURLException if an unknown protocol is specified.
     * @throws IOException if an I/O exception occurs.
     */
    protected URLConnection onHttpConnection(HttpURLConnection origin) throws MalformedURLException, IOException {
        URLConnection connection = origin;
        int status = origin.getResponseCode();

        if (needsRedirect(status)) {
            // get redirect url from "location" header field
            String newUrl = origin.getHeaderField("Location");
            
            if (origin.getInstanceFollowRedirects()) {
                XRLog.load("Connection is redirected to: " + newUrl);
                // open the new connnection again
                connection = new URL(newUrl).openConnection();
            } else {
                XRLog.load("Redirect is required but not allowed to: " + newUrl);
            }
        }
        return connection;
    }

    /**
     * Verify that return code of connection represents a redirection.
     * 
     * But it is final because redirection processing is determined.
     * 
     * @param status return code of connection
     * @return boolean true if return code is a 3xx
     */
    protected final boolean needsRedirect(int status) {
        return 
                status != HttpURLConnection.HTTP_OK
                && (
                    status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER
                );
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
        ImageResource ir;
        if (ImageUtil.isEmbeddedBase64Image(uri)) {
            BufferedImage image = ImageUtil.loadEmbeddedBase64Image(uri);
            ir = createImageResource(null, image);
        } else {
            uri = resolveURI(uri);
            ir = (ImageResource) _imageCache.get(uri);
            //TODO: check that cached image is still valid
            if (ir == null) {
                InputStream is = resolveAndOpenStream(uri);
                if (is != null) {
                    try {
                        BufferedImage img = ImageIO.read(is);
                        if (img == null) {
                            throw new IOException("ImageIO.read() returned null");
                        }
                        ir = createImageResource(uri, img);
                        _imageCache.put(uri, ir);
                    } catch (FileNotFoundException e) {
                        XRLog.exception("Can't read image file; image at URI '" + uri + "' not found");
                    } catch (IOException e) {
                        XRLog.exception("Can't read image file; unexpected problem for URI '" + uri + "'", e);
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
            }
            if (ir == null) {
                ir = createImageResource(uri, null);
            }
        }
        return ir;
    }

    /**
     * Factory method to generate ImageResources from a given Image. May be overridden in subclass. 
     *
     * @param uri The URI for the image, resolved to an absolute URI.
     * @param img The image to package; may be null (for example, if image could not be loaded).
     *
     * @return An ImageResource containing the image.
     */
    protected ImageResource createImageResource(String uri, Image img) {
        return new ImageResource(uri, AWTFSImage.createImage(img));
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
        InputStream inputStream = resolveAndOpenStream(uri);
        XMLResource xmlResource;
        try {
            xmlResource = XMLResource.load(inputStream);
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

    public byte[] getBinaryResource(String uri) {
        InputStream is = resolveAndOpenStream(uri);
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
     * @param url A URI which anchors other, possibly relative URIs.
     */
    public void setBaseURL(String url) {
        _baseURL = url;
    }

    /**
     * Resolves the URI; if absolute, leaves as is, if relative, returns an absolute URI based on the baseUrl for
     * the agent.
     *
     * @param uri A URI, possibly relative.
     *
     * @return A URI as String, resolved, or null if there was an exception (for example if the URI is malformed).
     */
    public String resolveURI(String uri) {
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
                } catch (Exception e1) {
                    XRLog.exception("The default NaiveUserAgent doesn't know how to resolve the base URL for " + uri);
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
                // Otherwise it would simply return child.
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

    /**
     * Returns the current baseUrl for this class.
     */
    public String getBaseURL() {
        return _baseURL;
    }

    public void documentStarted() {
        shrinkImageCache();
    }

    public void documentLoaded() { /* ignore*/ }

    public void onLayoutException(Throwable t) { /* ignore*/ }

    public void onRenderException(Throwable t) { /* ignore*/ }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.40  2009/05/15 16:20:10  pdoubleya
 * ImageResource now tracks the URI for the image that was created and handles mutable images.
 *
 * Revision 1.39  2009/04/12 11:16:51  pdoubleya
 * Remove proposed patch for URLs that are incorrectly handled on Windows; need a more reliable solution.
 *
 * Revision 1.38  2008/04/30 23:14:18  peterbrant
 * Do a better job of cleaning up open file streams (patch by Christophe Marchand)
 *
 * Revision 1.37  2007/11/23 07:03:30  pdoubleya
 * Applied patch from N. Barozzi to allow either toolkit or buffered images to be used, see https://xhtmlrenderer.dev.java.net/servlets/ReadMsg?list=dev&msgNo=3847
 *
 * Revision 1.36  2007/10/31 23:14:43  peterbrant
 * Add rudimentary support for @font-face rules
 *
 * Revision 1.35  2007/06/20 12:24:31  pdoubleya
 * Fix bug in shrink cache, trying to modify iterator without using safe remove().
 *
 * Revision 1.34  2007/06/19 21:25:41  pdoubleya
 * Cleanup for caching in NUA, making it more suitable to use as a reusable UAC. NUA is also now a document listener and uses this to try and trim its cache down. PanelManager and iTextUA are now NUA subclasses.
 *
 * Revision 1.33  2007/05/20 23:25:33  peterbrant
 * Various code cleanups (e.g. remove unused imports)
 *
 * Patch from Sean Bright
 *
 * Revision 1.32  2007/05/09 21:52:06  pdoubleya
 * Fix for rendering problems introduced by removing GraphicsUtil class. Use Image instead of BufferedImage in most cases, convert to AWT image if necessary. Not complete, requires cleanup.
 *
 * Revision 1.31  2007/05/05 21:08:27  pdoubleya
 * Changed image-related interfaces (FSImage, ImageUtil, scaling) to all use BufferedImage, since there were no Image-specific APIs we depended on, and we have more control over what we do with BIs as compared to Is.
 *
 * Revision 1.30  2007/05/05 18:05:21  pdoubleya
 * Remove references to GraphicsUtil and the class itself, no longer needed
 *
 * Revision 1.29  2007/04/10 20:46:02  pdoubleya
 * Fix, was not closing XML source stream when done
 *
 * Revision 1.28  2007/02/07 16:33:31  peterbrant
 * Initial commit of rewritten table support and associated refactorings
 *
 * Revision 1.27  2006/06/28 13:46:59  peterbrant
 * ImageIO.read() can apparently return sometimes null instead of throwing an exception when processing an invalid image
 *
 * Revision 1.26  2006/04/27 13:28:48  tobega
 * Handle situations without base url and no file access gracefully
 *
 * Revision 1.25  2006/04/25 00:23:20  peterbrant
 * Fixes from Mike Curtis
 *
 * Revision 1.23  2006/04/08 08:21:24  tobega
 * relative urls and linked stylesheets
 *
 * Revision 1.22  2006/02/02 02:47:33  peterbrant
 * Support non-AWT images
 *
 * Revision 1.21  2005/10/25 19:40:38  tobega
 * Suggestion from user to use File.toURI.toURL instead of File.toURL because the latter is buggy
 *
 * Revision 1.20  2005/10/09 09:40:27  tobega
 * Use current directory as default base URL
 *
 * Revision 1.19  2005/08/11 01:35:37  joshy
 * removed debugging
 * updated stylesheet to use right aligns
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.17  2005/06/25 19:27:47  tobega
 * UAC now supplies Resources
 *
 * Revision 1.16  2005/06/25 17:23:35  tobega
 * first refactoring of UAC: ImageResource
 *
 * Revision 1.15  2005/06/21 17:52:10  joshy
 * new hover code
 * removed some debug statements
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2005/06/20 23:45:56  joshy
 * hack to fix the mangled background images on osx
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2005/06/20 17:26:45  joshy
 * debugging for image issues
 * font scale stuff
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2005/06/15 11:57:18  tobega
 * Making Browser a better model application with UserAgentCallback
 *
 * Revision 1.11  2005/06/15 11:53:47  tobega
 * Changed UserAgentCallback to getInputStream instead of getReader. Fixed up some consequences of previous change.
 *
 * Revision 1.10  2005/06/13 06:50:16  tobega
 * Fixed a bug in table content resolution.
 * Various "tweaks" in other stuff.
 *
 * Revision 1.9  2005/06/03 00:29:49  tobega
 * fixed potential bug
 *
 * Revision 1.8  2005/06/01 21:36:44  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.7  2005/03/28 14:24:22  pdoubleya
 * Remove stack trace on loading images.
 *
 * Revision 1.6  2005/02/02 12:14:01  pdoubleya
 * Clean, format, buffer reader.
 *
 *
 */
