/*
 * PanelManager.java
 * Copyright (c) 2005 Torbjörn Gannholm
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
import org.xhtmlrenderer.util.XRLog;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-jun-15
 * Time: 07:38:59
 * To change this template use File | Settings | File Templates.
 */
public class PanelManager implements UserAgentCallback {
    private String baseUrl;

    public InputStream getInputStream(String uri) {
        InputStream is = null;
        uri = resolveURI(uri);
        if (uri.startsWith("file://")) {
            File file = null;
            try {
                file = new File(new URI(uri));
            } catch (URISyntaxException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            if (file.isDirectory()) {
                String dirlist = new DirectoryLister().list(file);
                try {
                    is = new ByteArrayInputStream(dirlist.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return is;
            }
        }
        try {
            is = new URL(uri).openStream();
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (java.io.IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }
        return is;
    }

    public Image getImage(String uri) {
        Image img = null;
        InputStream is = getInputStream(uri);
        if (is != null) {
            try {
                img = ImageIO.read(is);
            } catch (Exception e) {
                XRLog.exception("Can't read image file; unexpected problem for URI '" + uri + "'", e);
            }
        }
        return img;
    }

    public boolean isVisited(String uri) {
        return false;
    }

    public void setBaseURL(String url) {
        baseUrl = resolveURI(url);
    }

    public String resolveURI(String uri) {
        URL ref = null;

        if (uri.startsWith("demo:")) {
            DemoMarker marker = new DemoMarker();
            String short_url = uri.substring(5);
            if (!short_url.startsWith("/")) {
                short_url = "/" + short_url;
            }
            ref = marker.getClass().getResource(short_url);
        } else {
            try {
                URL base = new URL(baseUrl);
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

    public String getBaseURL() {
        return baseUrl;
    }
}
