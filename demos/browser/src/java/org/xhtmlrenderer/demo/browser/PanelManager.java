/*
 * PanelManager.java
 * Copyright (c) 2005 Torbj�rn Gannholm
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
package org.xhtmlrenderer.demo.browser;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.xml.transform.sax.SAXSource;

import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.ResourceQueue;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.render.AWTFSImage;
import org.xhtmlrenderer.resource.*;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.GraphicsUtil;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;
import org.xml.sax.InputSource;


/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-jun-15
 * Time: 07:38:59
 * To change this template use File | Settings | File Templates.
 */
public class PanelManager implements UserAgentCallback, ResourceQueue {
    private String baseUrl;
    private int index = -1;
    private ArrayList history = new ArrayList();

    /**
     * an LRU cache
     */
    private int imageCacheCapacity = 16;
    private java.util.LinkedHashMap imageCache =
            new java.util.LinkedHashMap(imageCacheCapacity, 0.75f, true) {
                protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
                    return size() > imageCacheCapacity;
                }
            };
    private ImageLoaderThread imageLoaderThread;

    public PanelManager() {
        this.imageLoaderThread = new ImageLoaderThread();
        this.imageLoaderThread.start();
    }

    public CSSResource getCSSResource(String uri) {
        InputStream is = null;
        uri = resolveURI(uri);
        try {
            URLConnection uc = new URL(uri).openConnection();
            uc.connect();
            is = uc.getInputStream();
        } catch (MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (FileNotFoundException e11) {
            XRLog.exception("Can't load CSS from URI (not found): " + uri);
        } catch (IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }
        return new CSSResource(is);
    }

    public ImageResource getImageResource(String orgUri) {
        String uri = resolveURI(orgUri);

        return doGetImageResource(orgUri, uri);
    }

    private ImageResource doGetImageResource(String orgUri, String resolvedUri) {
        ImageResource ir;
        /*
        Possibilities:
          in cache?
            return it
          not in cache
            in queue?
                return temporary-image-wrapper
         */

        ir = (ImageResource) imageCache.get(resolvedUri);

        //TODO: check that cached image is still valid
        if (ir == null) {
            XRLog.load("Image not in cache, checking queue: " + imageQueue + " for uri " + orgUri);
            ImageItem item = (ImageItem) imageQueue.get(orgUri);
            if (item == null) {
                XRLog.load("Not found in queue size " + imageQueue.size() + ", loading immediate");
                for (Iterator it = imageQueue.keySet().iterator(); it.hasNext();) {
                    String s = (String) it.next();
                    XRLog.load("Matching: " + orgUri.equals(s) + "? " + orgUri + " == " + s);
                }
                InputStream is = getImageInputStream(resolvedUri);
                if (is != null) {
                    try {
                        Image img = loadImage(is);
                        ir = new ImageResource(new AWTFSImage(img));
                        imageCache.put(resolvedUri, ir);
                    } catch (IOException e) {
                        XRLog.exception("Can't read image file; unexpected problem for URI '" + resolvedUri + "'", e);
                    }
                }
            } else {
                XRLog.load("OK, in queue, deferring");
                ir = new DeferredImageResource(item);
                imageCache.put(resolvedUri, ir);
            }
        } else {
            XRLog.load("---- image read from cache: " + resolvedUri);
        }
        if (ir == null) {
            XRLog.load("Image URI requested, but neither in cache nor in queue; maybe load failed: " + resolvedUri);
            ir = new ImageResource(null);
        }
        return ir;
    }

    private Image loadImage(InputStream is) throws IOException {
        Image img = ImageIO.read(is);
        if (img == null) {
            throw new IOException("ImageIO.read() returned null");
        }
        img = GraphicsUtil.cleanImage(img);
        return img;
    }

    private InputStream getImageInputStream(String uri) {
        InputStream is = null;
        try {
            URLConnection uc = new URL(uri).openConnection();
            uc.connect();
            is = uc.getInputStream();
        } catch (MalformedURLException e1) {
            XRLog.exception("bad URL given: " + uri, e1);
        } catch (FileNotFoundException e11) {
            XRLog.exception("Can't load image from URI (not found): " + uri);
        } catch (IOException e11) {
            XRLog.exception("IO problem for " + uri, e11);
        }
        return is;
    }

    public XMLResource getXMLResource(String uri) {
        return this.getXMLResource(uri, null);
    }

    public XMLResource getXMLResource(String uri, NamespaceHandler nsh) {
        imageLoaderThread.setQueueIsFull(false);
        nsh.setResourceQueue(this);
        uri = resolveURI(uri);
        if (uri != null && uri.startsWith("file:")) {
            File file = null;
            try {
                StringBuffer sbURI = GeneralUtil.htmlEscapeSpace(uri);

                XRLog.general("Encoded URI: " + sbURI);
                file = new File(new URI(sbURI.toString()));
            } catch (URISyntaxException
                    e) {
                XRLog.exception("Invalid file URI " + uri, e);
                return getNotFoundDocument(uri);
            }
            if (file.isDirectory()) {
                String dirlist = DirectoryLister.list(file);
                return XMLResource.load(new StringReader(dirlist), nsh);
            }
        }
        XMLResource xr = null;
        try {
            URLConnection uc = new URL(uri).openConnection();
            uc.connect();
            String contentType = uc.getContentType();
            //Maybe should popup a choice when content/unknown!
            if (contentType.equals("text/plain") || contentType.equals("content/unknown")) {
                //sorry, mucking about a bit here - tobe
                SAXSource source = new SAXSource(new PlainTextXMLReader(uc.getInputStream()), new InputSource());
                xr = XMLResource.load(source, nsh);
            } else if (contentType.startsWith("image")) {
                String doc = "<img src='" + uri + "'/>";
                xr = XMLResource.load(new StringReader(doc), nsh);
            } else
                xr = XMLResource.load(uc.getInputStream(), nsh);
        } catch (MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }
        if (xr == null) {
            xr = getNotFoundDocument(uri);
        }
        this.imageLoaderThread.setQueueIsFull(true);
        this.imageLoaderThread.run();
        return xr;
    }

    private XMLResource getNotFoundDocument(String uri) {
        XMLResource xr;
        String notFound = "<html><h1>Document not found</h1><p>Could not access URI <pre>" + uri + "</pre></p></html>";
        XRLog.load(notFound);
        xr = XMLResource.load(new StringReader(notFound), null);
        return xr;
    }

    public boolean isVisited(String uri) {
        if (uri == null) return false;
        uri = resolveURI(uri);
        return history.contains(uri);
    }

    public void setBaseURL(String url) {
        if (baseUrl != null && baseUrl.startsWith("error:")) baseUrl = null;

        baseUrl = resolveURI(url);
        if (baseUrl == null) baseUrl = "error:FileNotFound";
        //setBaseURL is called by view when document is loaded
        if (index >= 0) {
            String historic = (String) history.get(index);
            if (historic.equals(baseUrl)) return;//moved in history
        }
        index++;
        for (int i = index; i < history.size(); history.remove(i)) ;
        history.add(index, baseUrl);
    }

    public String resolveURI(String uri) {
        URL ref = null;
        if (uri == null) return baseUrl;
        if (uri.trim().equals("")) return baseUrl;//jar URLs don't resolve this right
        if (uri.startsWith("demo:")) {
            DemoMarker marker = new DemoMarker();
            String short_url = uri.substring(5);
            if (!short_url.startsWith("/")) {
                short_url = "/" + short_url;
            }
            ref = marker.getClass().getResource(short_url);
            Uu.p("ref = " + ref);
        } else if (uri.startsWith("demoNav:")) {
            DemoMarker marker = new DemoMarker();
            String short_url = uri.substring("demoNav:".length());
            if (!short_url.startsWith("/")) {
                short_url = "/" + short_url;
            }
            ref = marker.getClass().getResource(short_url);
            Uu.p("ref = " + ref);
        } else {
            try {
                URL base;
                if (baseUrl == null || baseUrl.length() == 0) {
                    base = new File(".").toURL();
                } else {
                    base = new URL(baseUrl);
                }
                ref = new URL(base, uri);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        XRLog.load("Resolved uri " + uri + " to " + (ref == null ? "null" : ref.toExternalForm()));

        if (ref == null)
            return null;
        else
            return ref.toExternalForm();
    }

    public ImageResource getImageResource(String originURI, String uri) {
        if (originURI != null)
            try {
                String u = new URL(new URL(originURI), uri).toString();
                uri = u;
            } catch (MalformedURLException e) {
                // nothing
            }
        return doGetImageResource(uri, uri);
    }

    public String getBaseURL() {
        return baseUrl;
    }


    public String getForward
            () {
        index++;
        return (String) history.get(index);
    }

    public String getBack
            () {
        index--;
        return (String) history.get(index);
    }

    public boolean hasForward
            () {
        if (index + 1 < history.size() && index >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasBack
            () {
        if (index >= 0) {
            return true;
        } else {
            return false;
        }
    }

    Map imageQueue = new HashMap();

    public void queueImageItem(ImageItem item) {
        String orgUri = item.getImageSource();
        String resolved = resolveURI(item.getImageSource());
        if (!imageCache.containsKey(resolved)) {
            XRLog.load("Queueing image: " + item);
            imageQueue.put(orgUri, item);
        } else {
            XRLog.load("URI: " + resolved + " not in cache: " + imageCache);
        }
    }

    public void queueImageItems(Set items) {
        XRLog.load("Queueing " + items.size() + " images");

        for (Iterator it = items.iterator(); it.hasNext();) {
            queueImageItem((ImageItem) it.next());
        }
        XRLog.load("Queue contains: " + imageQueue);
    }

    /**
     * Thread responsible for processing a queue of image references to be downloaded
     * and instantiated as images.
     */
    class ImageLoaderThread extends Thread {
        private boolean imageLoaded;
        /**
         * If true, means that no more items will be added to the queue;
         * current items in queue should be loaded, then the thread should end.
         */
        private boolean queueIsFull;
        private boolean running;

        ImageLoaderThread() {
            this.setDaemon(true);
            this.setPriority(MIN_PRIORITY);
        }

        public void run() {
            if (running) {
                XRLog.load("Already running, not starting again");
                return;
            }
            running = true;
            while (true) {
                if (imageQueue.size() > 0) {
                    XRLog.load("queue not empty: " + imageQueue.size());
                    Iterator iter = imageQueue.values().iterator();
                    while (iter.hasNext()) {
                        ImageItem item = null;
                        try {
                            item = (ImageItem) iter.next();
                        } catch (NoSuchElementException e) {
                            XRLog.load(Level.WARNING, "Unexpected, pull from image queue returned no item.");
                        }
                        if (item != null) {
                            String url = resolveURI(item.getImageSource());
                            XRLog.load("loading queued image: " + url);
                            InputStream is = getImageInputStream(url);
                            if (is != null) {
                                try {
                                    imageLoaded = false;
                                    Image img = loadImage(is);
                                    ImageResource oldIR = (ImageResource) imageCache.get(url);

                                    ImageObserver obs = new ImageObserver() {
                                        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                                            return infoflags == ALLBITS;
                                        }
                                    };
                                    item.setHeight(String.valueOf(img.getHeight(null)));
                                    item.setWidth(String.valueOf(img.getWidth(null)));
                                    if (oldIR == null) {
                                        imageCache.put(url, new ImageResource(new AWTFSImage(img)));
                                    } else {
                                        if (oldIR instanceof DeferredImageResource) {
                                            DeferredImageResource dir = (DeferredImageResource) oldIR;
                                            ImageLoaderCallback cb = dir.getLoaderCallback();
                                            cb.imageLoaded(item, img);
                                        }
                                    }

                                } catch (IOException e) {
                                    XRLog.exception("Can't read image file; unexpected problem for URI '" + url + "'", e);
                                }
                            } // // got image input stream
                            try {
                                iter.remove();
                                XRLog.load("Image read from queue and removed: " + url);
                            } catch (IllegalStateException e) {
                                XRLog.load(Level.WARNING, "Image queue: could not remove item from queue, " +
                                        "iterator.remove() failed for URL:" + url, e);
                            }
                        } // got item from queue
                    } // iterate queue
                } // queue empty?
                if (queueIsFull && imageQueue.size() == 0) {
                    XRLog.load("Queue is full, all processed");
                    running = false;
                    break;
                }
            }
        }

        public void setQueueIsFull(boolean b) {
            this.queueIsFull = b;
        }
    }
}
