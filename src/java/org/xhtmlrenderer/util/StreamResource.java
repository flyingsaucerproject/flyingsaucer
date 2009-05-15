package org.xhtmlrenderer.util;

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.net.URLConnection;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: pdoubleya
 * Date: May 15, 2009
 * Time: 11:56:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class StreamResource {
    private final String _uri;
    private URLConnection _conn;
    private int _slen;
    private InputStream _inputStream;

    public StreamResource(final String uri) {
        _uri = uri;
    }

    public void connect() {
        try {
            _conn = new URL(_uri).openConnection();

            // If using Java 5+ you can set timeouts for the URL connection--useful if the remote
            // server is down etc.; the default timeout is pretty long
            //
            //uc.setConnectTimeout(10 * 1000);
            //uc.setReadTimeout(30 * 1000);
            //
            // TODO:CLEAN-JDK1.4
            // Since we target 1.4, we use a couple of system properties--note these are only supported
            // in the Sun JDK implementation--see the Net properties guide in the JDK
            // e.g. file:///usr/java/j2sdk1.4.2_17/docs/guide/net/properties.html
            System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(10 * 1000));
            System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(30 * 1000));

            _conn.connect();
            _slen = _conn.getContentLength();
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("bad URL given: " + _uri, e);
        } catch (FileNotFoundException e) {
            XRLog.exception("item at URI " + _uri + " not found");
        } catch (IOException e) {
            XRLog.exception("IO problem for " + _uri, e);
        }
    }

    public boolean hasStreamLength() {
        return _slen >= 0;
    }

    public int streamLength() {
        return _slen;
    }

    public BufferedInputStream bufferedStream() throws IOException {
        _inputStream = _conn.getInputStream();
        return new BufferedInputStream(_inputStream);
    }

    public void close() {
        if (_inputStream != null) {
            try {
                _inputStream.close();
            } catch (IOException e) {
                // swallow
            }
        }
    }
}
