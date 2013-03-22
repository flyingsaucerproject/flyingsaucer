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
package org.xhtmlrenderer.demo.browser.swt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

import javax.xml.transform.sax.SAXSource;

import org.eclipse.swt.graphics.Device;
import org.xhtmlrenderer.demo.browser.DirectoryLister;
import org.xhtmlrenderer.demo.browser.PlainTextXMLReader;
import org.xhtmlrenderer.demo.browser.DemoMarker;
import org.xhtmlrenderer.demo.browser.swt.DemosNavigation.Demo;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.swt.NaiveUserAgent;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.Uu;
import org.xml.sax.InputSource;

public class BrowserUserAgent extends NaiveUserAgent {

    private DemosNavigation _demos;
    private History _history;

    public BrowserUserAgent(Device device) {
        super(device);
        _demos = new DemosNavigation(this);
        _history = new History();
    }

    public String resolveURIX(String uri) {
        final String burl = getBaseURL();

        if (uri == null) {
            return null;
        }
        try {
            URI base;
            if (burl == null || burl.length() == 0) {
                base = new File(".").toURI();
            } else {
                base = new URI(burl);
            }
            uri = base.resolve(new URI(uri)).toString();
        } catch (URISyntaxException e) {
            XRLog.general(Level.WARNING, "URI is malformed: " + burl + " or "
                    + uri);
            return null;
        }

        if (uri.startsWith("demoNav:")) {
            String action = uri.substring(8);
            Demo demo = null;
            if (action.equalsIgnoreCase("back")) {
                demo = _demos.previous();
            } else if (action.equalsIgnoreCase("forward")) {
                demo = _demos.next();
            }
            if (demo != null) {
                uri = demo.getUrl();
            }
        }

        return uri;
    }
    public String resolveURI(String uri) {
        final String burl = getBaseURL();

        URL ref = null;

        if (uri == null) return null;
        if (uri.trim().equals("")) return burl; //jar URLs don't resolve this right

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
            Demo demo = null;
            if (short_url.equalsIgnoreCase("back")) {
                demo = _demos.previous();
            } else if (short_url.equalsIgnoreCase("forward")) {
                demo = _demos.next();
            }
            if (demo != null) {
                try {
                    String duri = demo.getUrl();
                    if (duri.startsWith("demo:")) {
                        ref = new URL(resolveURI(duri));
                    } else {
                        ref = new URL(duri);
                    }
                } catch (MalformedURLException e) {
                    Uu.p("URI/URL is malformed: " + burl + " or " + uri);
                }
            } else {
                if (!short_url.startsWith("/")) {
                    short_url = "/" + short_url;
                }
                ref = marker.getClass().getResource(short_url);
            }
            Uu.p("Demo navigation URI, ref = " + ref);
        } else if (uri.startsWith("javascript")) {
            Uu.p("Javascript URI, ignoring: " + uri);
        } else if (uri.startsWith("news")) {
            Uu.p("News URI, ignoring: " + uri);
        } else {
            try {
                URL base;
                if (burl == null || burl.length() == 0) {
                    base = new File(".").toURL();
                } else {
                    base = new URL(burl);
                }
                ref = new URL(base, uri);
            } catch (MalformedURLException e) {
                Uu.p("URI/URL is malformed: " + burl + " or " + uri);
            }
        }

        if (ref == null)
            return null;
        else
            return ref.toExternalForm();
    }


    public void setBaseURL(String url) {
        super.setBaseURL(resolveURI(url));
    }

    public String resolveFullURI(String uri) {
        uri = resolveURI(uri);
        if (uri == null) {
            return null;
        }
        if (uri.startsWith("demo:")) {
            uri = uri.substring(5);
            if (!uri.startsWith("/")) {
                uri = "/" + uri;
            }
            URL url = getClass().getResource(uri);
            if (url == null) {
                return "";
            }
            return url.toExternalForm();
        }
        return uri;
    }

    public XMLResource getXMLResource(String uri) {
        uri = resolveFullURI(uri);
        if (uri != null && uri.startsWith("file:")) {
            File file;
            try {
                StringBuffer sbURI = GeneralUtil.htmlEscapeSpace(uri);
                int i = sbURI.indexOf("#");
                if (i >= 0) {
                    // delete fragment portion
                    sbURI.delete(i, sbURI.length());
                }

                XRLog.general("Encoded URI: " + sbURI);
                file = new File(new URI(sbURI.toString()));
            } catch (URISyntaxException e) {
                XRLog.exception("Invalid file URI " + uri, e);
                return getNotFoundDocument(uri);
            }
            if (file.isDirectory()) {
                String dirlist = DirectoryLister.list(file);
                return XMLResource.load(new StringReader(dirlist));
            }
        }
        XMLResource xr = null;
        URLConnection uc;
        InputStream inputStream = null;
        try {
            uc = new URL(uri).openConnection();
            uc.connect();
            String contentType = uc.getContentType();
            // Maybe should popup a choice when content/unknown!
            if (contentType.equals("text/plain")
                    || contentType.equals("content/unknown")) {
                inputStream = uc.getInputStream();
                SAXSource source = new SAXSource(new PlainTextXMLReader(
                    inputStream), new InputSource());
                xr = XMLResource.load(source);
            } else if (contentType.startsWith("image")) {
                String doc = "<img src='" + uri + "'/>";
                xr = XMLResource.load(new StringReader(doc));
            } else {
                inputStream = uc.getInputStream();
                xr = XMLResource.load(inputStream);
            }
        } catch (MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // swallow
                }
            }
        }

        if (xr == null) {
            xr = getNotFoundDocument(uri);
        }
        return xr;
    }

    public CSSResource getCSSResource(String uri) {
        uri = resolveFullURI(uri);
        return super.getCSSResource(uri);
    }

    public ImageResource getImageResource(String uri) {
        uri = resolveFullURI(uri);
        return super.getImageResource(uri);
    }

    /**
     * Used internally when a document can't be loaded--returns XHTML as an
     * XMLResource indicating that fact.
     * 
     * @param uri The URI which could not be loaded.
     * 
     * @return An XMLResource containing XML which about the failure.
     */
    private XMLResource getNotFoundDocument(String uri) {
        XMLResource xr;
        String notFound = "<html><h1>Document not found</h1><p>Could not access URI <pre>"
                + uri + "</pre></p></html>";

        xr = XMLResource.load(new StringReader(notFound));
        return xr;
    }

    public boolean isVisited(String uri) {
        return _history.contains(uri);
    }

    public DemosNavigation getDemos() {
        return _demos;
    }

    public History getHistory() {
        return _history;
    }

}
