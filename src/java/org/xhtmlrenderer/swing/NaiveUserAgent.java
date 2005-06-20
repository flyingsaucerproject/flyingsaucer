/*
 * NaiveUserAgent.java
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
package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.util.XRLog;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;


/**
 * @author Torbjörn Gannholm
 */
public class NaiveUserAgent implements org.xhtmlrenderer.extend.UserAgentCallback {

    /**
     * Description of the Field
     */
    private HashMap imageCache;
    private String baseURL;

    /**
     * Creates a new instance of NaiveUserAgent
     */
    public NaiveUserAgent() {
    }

    /**
     * Gets a Reader for the resource identified
     *
     * @param uri PARAM
     * @return The stylesheet value
     */
    //TOdO:implement this with nio.
    public InputStream getInputStream(String uri) {
        java.io.InputStream is = null;
        uri = resolveURI(uri);
        try {
            is = new URL(uri).openStream();
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (java.io.IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }
        return is;
    }

    /**
     * Gets the image attribute of the NaiveUserAgent object
     *
     * @param uri PARAM
     * @return The image value
     */
    //TODO: better caching than using SoftReference
    public Image getImage(String uri) {
        java.io.InputStream is = null;
        Image img = null;
        uri = resolveURI(uri);
        if (imageCache != null) {
            SoftReference ref = (SoftReference) imageCache.get(uri);
            if (ref != null) {
                img = (Image) ref.get();
            }
            if (img != null) {
                return img;
            }
        }
        try {
            is = new URL(uri).openStream();
        } catch (FileNotFoundException ex) {
            XRLog.exception("Can't find image file for URI: '" + uri + "'; skipping.");
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("Bad URI given for image file: '" + uri + "'");
        } catch (java.io.IOException e) {
            XRLog.exception("Can't load image file; IO problem for URI '" + uri + "'", e);
        }

        if (is != null) {
            try {
                img = ImageIO.read(is);
				System.out.println("img started as = " + img);
				img = img.getScaledInstance(100, 100, Image.SCALE_FAST);
				System.out.println("img now = " + img);
                if (imageCache == null) {
                    imageCache = new HashMap();
                }
                imageCache.put(uri, new SoftReference(img));
            } catch (Exception e) {
                XRLog.exception("Can't read image file; unexpected problem for URI '" + uri + "'", e);
            }
        }
		
		System.out.println("generated image = " + img);
        return img;
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
        baseURL = url;
    }

    public String resolveURI(String uri) {
        URL result;
        try {
            result = new URL(uri);
        } catch (MalformedURLException e) {
            try {
                result = new URL(new URL(baseURL), uri);
            } catch (MalformedURLException e1) {
                return null;
            }
        }
        return result.toString();

    }

    public String getBaseURL() {
        return baseURL;
    }
}

/*
 * $Id$
 *
 * $Log$
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

