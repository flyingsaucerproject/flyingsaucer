/*
 * NaiveUserAgent.java
 *
 * Created on den 5 november 2004, 18:08
 */

package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.util.XRLog;

import java.io.InputStreamReader;

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
    
    public java.io.Reader getReaderForURI(String uri) {
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

    public boolean isVisited(String uri) {
        return false;
    }

}
