/*
 * NaiveUserAgent.java
 *
 * Created on den 5 november 2004, 18:08
 */

package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.util.XRLog;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.util.HashMap;

/**
 * @author Torbjörn Gannholm
 */
public class NaiveUserAgent implements org.xhtmlrenderer.extend.UserAgentCallback {

    /**
     * Creates a new instance of NaiveUserAgent
     */
    public NaiveUserAgent() {
    }
    
    /*public org.xhtmlrenderer.extend.NamespaceHandler getNamespaceHandler(String namespace) {
        return new XhtmlNamespaceHandler();
    }*/
    
    public java.io.Reader getStylesheet(String uri) {
        java.io.InputStream is = null;
        InputStreamReader isr = null;
        try {
            //is = _baseURI.resolve(uri).toURL().openStream();
            is = (new java.net.URL(uri)).openStream();
            isr = new java.io.InputStreamReader(is);
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (java.io.IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }
        return isr;
    }

    private HashMap imageCache;

    public Image getImage(String uri) {
        java.io.InputStream is = null;
        Image img = null;
        if (imageCache != null) {
            SoftReference ref = (SoftReference) imageCache.get(uri);
            if (ref != null) img = (Image) ref.get();
            if (img != null) return img;
        }
        try {
            //is = _baseURI.resolve(uri).toURL().openStream();
            is = (new java.net.URL(uri)).openStream();
            img = ImageIO.read(is);
            if (imageCache == null) imageCache = new HashMap();
            imageCache.put(uri, new SoftReference(img));
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (java.io.IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }
        return img;
    }

    public boolean isVisited(String uri) {
        return false;
    }

}
