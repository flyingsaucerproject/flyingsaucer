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

import org.eclipse.swt.graphics.Device;
import org.xhtmlrenderer.demo.browser.DemoMarker;
import org.xhtmlrenderer.demo.browser.DirectoryLister;
import org.xhtmlrenderer.demo.browser.PlainTextXMLReader;
import org.xhtmlrenderer.demo.browser.swt.DemosNavigation.Demo;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.resource.HTMLResource;
import org.xhtmlrenderer.swt.NaiveUserAgent;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.IOUtil;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;
import org.xml.sax.InputSource;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

@ParametersAreNonnullByDefault
public class BrowserUserAgent extends NaiveUserAgent {

    private final DemosNavigation _demos;
    private final History _history;

    public BrowserUserAgent(Device device) {
        super(device);
        _demos = new DemosNavigation(this);
        _history = new History();
    }

    @Override
    @Nullable
    @CheckReturnValue
    public String resolveURI(@Nullable String uri) {
        if (uri == null) return null;

        final String burl = getBaseURL();
        if (uri.trim().isEmpty()) return burl; //jar URLs don't resolve this right

        URL ref = null;
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
                    Uu.p("URI/URL is malformed: %s or %s (caused by: %s)".formatted(burl, uri, e));
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
                if (burl == null || burl.isEmpty()) {
                    base = new File(".").toURI().toURL();
                } else {
                    base = new URL(burl);
                }
                ref = new URL(base, uri);
            } catch (MalformedURLException e) {
                Uu.p("URI/URL is malformed: %s or %s (caused by: %s)".formatted(burl, uri, e));
            }
        }

        if (ref == null)
            return null;
        else
            return ref.toExternalForm();
    }


    @Override
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

    @Override
    public HTMLResource getXMLResource(String uri) {
        uri = resolveFullURI(uri);
        if (uri != null && uri.startsWith("file:")) {
            File file;
            try {
                StringBuilder sbURI = GeneralUtil.htmlEscapeSpace(uri);
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
                String dirList = DirectoryLister.list(file);
                return HTMLResource.load(new StringReader(dirList));
            }
        }
        HTMLResource xr = null;
        URLConnection uc;
        InputStream inputStream = null;
        try {
            uc = new URL(uri).openConnection();
            uc.connect();
            String contentType = uc.getContentType();
            // Maybe should pop up a choice when content/unknown!
            if (contentType.equals("text/plain")
                    || contentType.equals("content/unknown")) {
                inputStream = uc.getInputStream();
                xr = HTMLResource.load(inputStream);
            } else if (contentType.startsWith("image")) {
                String doc = "<img src='" + uri + "'/>";
                xr = HTMLResource.load(new StringReader(doc));
            } else {
                inputStream = uc.getInputStream();
                xr = HTMLResource.load(inputStream);
            }
        } catch (MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        } finally {
            IOUtil.close(inputStream);
        }

        if (xr == null) {
            xr = getNotFoundDocument(uri);
        }
        return xr;
    }

    @Override
    public CSSResource getCSSResource(String uri) {
        uri = resolveFullURI(uri);
        return super.getCSSResource(uri);
    }

    @Override
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
    private HTMLResource getNotFoundDocument(String uri) {
        HTMLResource xr;
        String notFound = "<html><h1>Document not found</h1><p>Could not access URI <pre>"
                + uri + "</pre></p></html>";

        xr = HTMLResource.load(new StringReader(notFound));
        return xr;
    }

    @Override
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
