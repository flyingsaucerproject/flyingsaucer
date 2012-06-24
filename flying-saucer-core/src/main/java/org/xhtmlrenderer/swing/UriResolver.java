package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.util.XRLog;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.util.logging.Level;

public class UriResolver {
    private String _baseUri;

    public String resolve(final String uri) {
        if (uri == null) return null;
        String ret = null;
        if (_baseUri == null) {//first try to set a base URL
            try {
                URL result = new URL(uri);
                setBaseUri(result.toExternalForm());
            } catch (MalformedURLException e) {
                try {
                    setBaseUri(new File(".").toURI().toURL().toExternalForm());
                } catch (Exception e1) {
                    XRLog.exception("The default NaiveUserAgent doesn't know how to resolve the base URL for " + uri);
                    return null;
                }
            }
        }
        // test if the URI is valid; if not, try to assign the base url as its parent
        try {
            return new URL(uri).toString();
        } catch (MalformedURLException e) {
            XRLog.load(Level.FINE, "Could not read " + uri + " as a URL; may be relative. Testing using parent URL " + _baseUri);
            try {
                URL result = new URL(new URL(_baseUri), uri);
                ret = result.toString();
                XRLog.load(Level.FINE, "Was able to read from " + uri + " using parent URL " + _baseUri);
            } catch (MalformedURLException e1) {
                XRLog.exception("The default NaiveUserAgent cannot resolve the URL " + uri + " with base URL " + _baseUri);
            }
        }
        return ret;

    }

    public void setBaseUri(final String baseUri) {
        _baseUri = baseUri;
    }

    public String getBaseUri() {
        return _baseUri;
    }
}
