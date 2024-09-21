package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.util.XRLog;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

@ParametersAreNonnullByDefault
public class UriResolver {
    private String _baseUri;

    @Nullable
    @CheckReturnValue
    public String resolve(@Nullable final String uri) {
        if (uri == null) return null;
        return resolveUri(uri);
    }

    @Nonnull
    @CheckReturnValue
    public String resolveUri(final String uri) {
        if (_baseUri == null) {//first try to set a base URL
            try {
                URL result = new URL(uri);
                setBaseUri(result.toExternalForm());
            } catch (MalformedURLException e) {
                try {
                    setBaseUri(new File(".").toURI().toURL().toExternalForm());
                } catch (MalformedURLException e1) {
                    throw new IllegalStateException("The default NaiveUserAgent doesn't know how to resolve the base URL for " + uri, e1);
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
                String ret = result.toString();
                XRLog.load(Level.FINE, "Was able to read from " + uri + " using parent URL " + _baseUri);
                return ret;
            } catch (MalformedURLException e1) {
                throw new IllegalStateException("The default NaiveUserAgent cannot resolve the URL " + uri + " with base URL " + _baseUri, e1);
            }
        }
    }

    public void setBaseUri(final String baseUri) {
        _baseUri = baseUri;
    }

    @Nonnull
    @CheckReturnValue
    public String getBaseUri() {
        return _baseUri;
    }
}
