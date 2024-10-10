package org.xhtmlrenderer.util;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * User: pdoubleya
 * Date: May 15, 2009
 */
public class StreamResource implements AutoCloseable {
    private final String _uri;
    @Nullable
    private URLConnection _conn;
    @Nullable
    private InputStream _inputStream;

    public StreamResource(final String uri) {
        _uri = uri;
    }

    public void connect() {
        _conn = establishConnection(_uri);
    }

    @CheckReturnValue
    @Nullable
    private static URLConnection establishConnection(String uri) {
        try {
            URLConnection conn = new URL(uri).openConnection();

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

            conn.setRequestProperty("Accept", "*/*");

            conn.connect();
            conn.getContentLength();
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("bad URL given: %s".formatted(uri), e);
        } catch (FileNotFoundException e) {
            XRLog.exception("item at URI %s not found: %s".formatted(uri, e));
        } catch (IOException e) {
            XRLog.exception("IO problem for %s".formatted(uri), e);
        }
        return null;
    }

    public BufferedInputStream bufferedStream() throws IOException {
        _inputStream = _conn.getInputStream();
        return new BufferedInputStream(_inputStream);
    }

    @Override
    public void close() {
        IOUtil.close(_inputStream);
    }
}
