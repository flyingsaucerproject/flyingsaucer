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

import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.swing.AWTFSImage;
import org.xhtmlrenderer.util.GraphicsUtil;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xml.sax.InputSource;

import javax.imageio.ImageIO;
import javax.xml.transform.sax.SAXSource;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-jun-15
 * Time: 07:38:59
 * To change this template use File | Settings | File Templates.
 */
public class PanelManager implements UserAgentCallback {
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

    public ImageResource getImageResource(String uri) {
        ImageResource ir = null;
        uri = resolveURI(uri);
        ir = (ImageResource) imageCache.get(uri);
        //TODO: check that cached image is still valid
        if (ir == null) {
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
            if (is != null) {
                try {
                    Image img = ImageIO.read(is);
                    if (img == null) {
                        throw new IOException("ImageIO.read() returned null");
                    }
                    img = GraphicsUtil.cleanImage(img);
                    ir = new ImageResource(new AWTFSImage(img));
                    imageCache.put(uri, ir);
                } catch (IOException e) {
                    XRLog.exception("Can't read image file; unexpected problem for URI '" + uri + "'", e);
                }
            }
        }
        if (ir == null) ir = new ImageResource(null);
        return ir;
    }

    public XMLResource getXMLResource(String uri) {
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
                return XMLResource.load(new StringReader(dirlist));
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
                xr = XMLResource.load(source);
            } else if (contentType.startsWith("image")) {
                String doc = "<img src='" + uri + "'/>";
                xr = XMLResource.load(new StringReader(doc));
            } else
                xr = XMLResource.load(uc.getInputStream());
        } catch (MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }
        if (xr == null) {
            xr = getNotFoundDocument(uri);
        }
        return xr;
    }

    private XMLResource getNotFoundDocument
            (String
                    uri) {
        XMLResource xr;
        String notFound = "<html><h1>Document not found</h1><p>Could not access URI <pre>" + uri + "</pre></p></html>";
        System.out.println(notFound);
        xr = XMLResource.load(new StringReader(notFound));
        return xr;
    }

    public boolean isVisited
            (String
                    uri) {
        if (uri == null) return false;
        uri = resolveURI(uri);
        return history.contains(uri);
    }

    public void setBaseURL (String url) {
        if(baseUrl !=null &&  baseUrl.startsWith("error:")) baseUrl = null;
        
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

    public String resolveURI
            (String
                    uri) {
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

        if (ref == null)
            return null;
        else
            return ref.toExternalForm();
    }

    public String getBaseURL
            () {
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
}
