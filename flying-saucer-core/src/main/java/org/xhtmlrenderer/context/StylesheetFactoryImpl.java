/*
 * StylesheetFactoryImpl.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
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
package org.xhtmlrenderer.context;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.xhtmlrenderer.css.extend.StylesheetFactory;
import org.xhtmlrenderer.css.parser.CSSErrorHandler;
import org.xhtmlrenderer.css.parser.CSSParser;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.XRLog;
import org.xml.sax.InputSource;

/**
 * A Factory class for Cascading Style Sheets. Sheets are parsed using a single
 * parser instance for all sheets. Sheets are cached by URI using a LRU test,
 * but timestamp of file is not checked.
 *
 * @author Torbjoern Gannholm
 */
public class StylesheetFactoryImpl implements StylesheetFactory {
    /**
     * the UserAgentCallback to resolve uris
     */
    private UserAgentCallback _userAgentCallback;

    private int _cacheCapacity = 16;

    /**
     * an LRU cache
     */
    private java.util.LinkedHashMap _cache =
            new java.util.LinkedHashMap(_cacheCapacity, 0.75f, true) {
                private static final long serialVersionUID = 1L;

                protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
                    return size() > _cacheCapacity;
                }
            };
    private CSSParser _cssParser;

    public StylesheetFactoryImpl(UserAgentCallback userAgentCallback) {
        _userAgentCallback = userAgentCallback;
        _cssParser = new CSSParser(new CSSErrorHandler() {
            public void error(String uri, String message) {
                XRLog.cssParse(Level.WARNING, "(" + uri + ") " + message);
            }
        });
    }

    public synchronized Stylesheet parse(Reader reader, StylesheetInfo info) {
        try {
            return _cssParser.parseStylesheet(info.getUri(), info.getOrigin(), reader);
        } catch (IOException e) {
            XRLog.cssParse(Level.WARNING, "Couldn't parse stylesheet at URI " + info.getUri() + ": " + e.getMessage(), e);
            return new Stylesheet(info.getUri(), info.getOrigin());
        }
    }

    /**
     * @return Returns null if uri could not be loaded
     */
    private Stylesheet parse(StylesheetInfo info) {
        CSSResource cr = _userAgentCallback.getCSSResource(info.getUri());
        if (cr==null) return null;  
        // Whether by accident or design, InputStream will never be null
        // since the null resource stream is wrapped in a BufferedInputStream
        InputSource inputSource=cr.getResourceInputSource();
        if (inputSource==null) return null;
        InputStream is = inputSource.getByteStream();
        if (is==null) return null;
        try {
            return parse(new InputStreamReader(is, Configuration.valueFor("xr.stylesheets.charset-name", "UTF-8")), info);
        } catch (UnsupportedEncodingException e) {
            // Shouldn't happen
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public synchronized Ruleset parseStyleDeclaration(int origin, String styleDeclaration) {
        return _cssParser.parseDeclaration(origin, styleDeclaration);
    }

    /**
     * Adds a stylesheet to the factory cache. Will overwrite older entry for
     * same key.
     *
     * @param key   Key to use to reference sheet later; must be unique in
     *              factory.
     * @param sheet The sheet to cache.
     */
    public synchronized void putStylesheet(Object key, Stylesheet sheet) {
        _cache.put(key, sheet);
    }

    /**
     * @param key
     * @return true if a Stylesheet with this key has been put in the cache.
     *         Note that the Stylesheet may be null.
     */
    //TODO: work out how to handle caching properly, with cache invalidation
    public synchronized boolean containsStylesheet(Object key) {
        return _cache.containsKey(key);
    }

    /**
     * Returns a cached sheet by its key; null if no entry for that key.
     *
     * @param key The key for this sheet; same as key passed to
     *            putStylesheet();
     * @return The stylesheet
     */
    public synchronized Stylesheet getCachedStylesheet(Object key) {
        return (Stylesheet) _cache.get(key);
    }

    /**
     * Removes a cached sheet by its key.
     *
     * @param key The key for this sheet; same as key passed to
     *            putStylesheet();
     */
    public synchronized Object removeCachedStylesheet(Object key) {
        return _cache.remove(key);
    }
    
    public synchronized void flushCachedStylesheets() {
        _cache.clear();
    }

    /**
     * Returns a cached sheet by its key; loads and caches it if not in cache;
     * null if not able to load
     *
     * @param info The StylesheetInfo for this sheet
     * @return The stylesheet
     */
    //TODO: this looks a bit odd
    public Stylesheet getStylesheet(StylesheetInfo info) {
        XRLog.load("Requesting stylesheet: " + info.getUri());

        Stylesheet s = getCachedStylesheet(info.getUri());
        if (s == null && !containsStylesheet(info.getUri())) {
            s = parse(info);
            putStylesheet(info.getUri(), s);
        }
        return s;
    }

    public void setUserAgentCallback(UserAgentCallback userAgent) {
        _userAgentCallback = userAgent;
    }
    
    public void setSupportCMYKColors(boolean b) {
        _cssParser.setSupportCMYKColors(b);
    }
}
