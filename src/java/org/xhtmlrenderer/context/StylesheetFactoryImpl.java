/*
 * StylesheetFactoryImpl.java
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
package org.xhtmlrenderer.context;

import com.steadystate.css.parser.CSSOMParser;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.*;
import org.w3c.dom.stylesheets.MediaList;
import org.xhtmlrenderer.css.extend.StylesheetFactory;
import org.xhtmlrenderer.css.sheet.InlineStyleInfo;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.resource.CSSResource;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.io.*;
import java.net.URL;

/**
 * A Factory class for Cascading Style Sheets. Sheets are parsed using a single
 * parser instance for all sheets. Sheets are cached by URI using a LRU test,
 * but timestamp of file is not checked.
 *
 * @author Torbjörn Gannholm
 */
public class StylesheetFactoryImpl implements StylesheetFactory {

    /**
     * the UserAgentCallback to resolve uris
     */
    private UserAgentCallback _userAgent;

    /**
     * Description of the Field
     */
    private int _cacheCapacity = 16;

    /**
     * an LRU cache
     */
    private java.util.LinkedHashMap _cache =
            new java.util.LinkedHashMap(_cacheCapacity, 0.75f, true) {
                protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
                    return size() > _cacheCapacity;
                }
            };
    private CSSOMParser parser;

    /**
     * Creates a new instance of StylesheetFactory
     *
     * @param userAgent PARAM
     */
    public StylesheetFactoryImpl(UserAgentCallback userAgent) {
        _userAgent = userAgent;
        try {
            Object obj = Class.forName("com.steadystate.css.parser.SACParser").newInstance();
            org.w3c.css.sac.Parser psr = (org.w3c.css.sac.Parser) obj;
            parser = new CSSOMParser(psr);
        } catch (Exception ex) {
            XRLog.exception("Bad!  Couldn't load the CSS parser. Everything after this will fail.");
        }
    }

    /**
     * Description of the Method
     *
     * @param stream PARAM
     * @param info
     * @return Returns
     */
    synchronized Stylesheet parse(InputStream stream, StylesheetInfo info) {
        Reader r = new InputStreamReader(stream);
        InputSource is = new InputSource(r);
        CSSStyleSheet style = null;
        try {
            style = parser.parseStyleSheet(is);
        } catch (java.io.IOException e) {
            throw new XRRuntimeException("IOException on parsing style seet from a Reader; don't know the URI.", e);
        }

        Stylesheet sheet = new Stylesheet(info.getUri(), info.getOrigin());
        CSSRuleList rl = style.getCssRules();
        pullRulesets(rl, sheet, info);

        return sheet;
    }

    /**
     * Description of the Method
     *
     * @param info
     * @return Returns null if uri could not be loaded
     */
    private Stylesheet parse(StylesheetInfo info) {
        CSSResource cr = _userAgent.getCSSResource(info.getUri());
        InputStream is = cr.getResourceInputSource().getByteStream();
        Stylesheet sheet = null;

        try {
            if (is != null) {
                sheet = parse(is, info);
            }
        } catch (Exception e) {
            debugBadStyleSheet(info);
            if (e instanceof XRRuntimeException) {
                throw (XRRuntimeException) e;
            } else {
                throw new XRRuntimeException("Failed on parsing CSS sheet at " + info.getUri(), e);
            }

        }
        return sheet;
    }

    private void debugBadStyleSheet(StylesheetInfo info) {
        InputStream is = _userAgent.getCSSResource(info.getUri()).getResourceInputSource().getByteStream();
        if (is != null) {
            try {
                Reader r = new InputStreamReader(is);
                LineNumberReader lnr = new LineNumberReader(new BufferedReader(r));
                StringBuffer sb = new StringBuffer();
                String line = null;
                while ((line = lnr.readLine()) != null) {
                    sb.append(line + "\n");
                }
                XRLog.cssParse(sb.toString());
            } catch (Exception ex) {
                XRLog.cssParse("Failed to read CSS sheet at " + info.getUri() + " for debugging.");
            }

        }
    }

    /**
     * Description of the Method
     *
     * @param isis PARAM
     * @param main PARAM
     * @return Returns
     */
    public Stylesheet parseInlines(InlineStyleInfo[] isis, StylesheetInfo main) {
        Stylesheet sheet = new Stylesheet(main.getUri(), main.getOrigin());
        if (isis != null) {
            for (int i = 0; i < isis.length; i++) {
                try {
                    InputStream stream = new ByteArrayInputStream(isis[i].getStyle().getBytes("UTF-8"));
                    StylesheetInfo info = isis[i].getInfo();
                    info.setUri(main.getUri());
                    Stylesheet is = parse(stream, info);
                    info.setStylesheet(is);
                    sheet.addStylesheet(info);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        return sheet;
    }

    /**
     * Description of the Method
     *
     * @param origin           PARAM
     * @param styleDeclaration PARAM
     * @return Returns
     */
    public synchronized Ruleset parseStyleDeclaration(int origin, String styleDeclaration) {
        try {
            java.io.StringReader reader = new java.io.StringReader("* {" + styleDeclaration + "}");
            InputSource is = new InputSource(reader);
            CSSStyleSheet style = parser.parseStyleSheet(is);
            reader.close();
            return new Ruleset((CSSStyleRule) style.getCssRules().item(0), origin);
        } catch (Exception ex) {
            throw new XRRuntimeException("Cannot parse style declaration from string." + ex.getMessage(), ex);
        }
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

    /**
     * Given the SAC sheet input, extracts all CSSStyleRules and loads Rulesets
     * from them.
     *
     * @param rl         The DOM-Level-2-Style CSSRuleList instance that holds
     *                   the sheet rules and etc. from which rules are taken.
     * @param stylesheet stylesheet to which rules are added
     * @param sheetInfo
     */
    private void pullRulesets(CSSRuleList rl, Stylesheet stylesheet, StylesheetInfo sheetInfo) {
        int nr = rl.getLength();
        for (int i = 0; i < nr; i++) {
            if (rl.item(i).getType() == org.w3c.dom.css.CSSRule.STYLE_RULE) {
                stylesheet.addRuleset(new Ruleset((org.w3c.dom.css.CSSStyleRule) rl.item(i), stylesheet.getOrigin()));
            } else if (rl.item(i).getType() == org.w3c.dom.css.CSSRule.IMPORT_RULE) {
                //note: the steadystate parser does not fetch and load imported stylesheets
                CSSImportRule cssir = (CSSImportRule) rl.item(i);
                String href = cssir.getHref();
                MediaList mediaList = cssir.getMedia();
                String media = mediaList.getMediaText();
                if (media.equals("")) {
                    media = sheetInfo.getMedia();
                }
                String uri = null;
                try {
                    uri = new java.net.URL(new URL(stylesheet.getURI()), href).toString();
                    StylesheetInfo info = new StylesheetInfo();
                    info.setOrigin(stylesheet.getOrigin());
                    info.setUri(uri);
                    info.setMedia(media);
                    info.setType("text/css");
                    stylesheet.addStylesheet(info);
                } catch (java.net.MalformedURLException e) {
                    XRLog.exception("bad URL for imported stylesheet", e);
                }
            } else if (rl.item(i).getType() == org.w3c.dom.css.CSSRule.MEDIA_RULE) {
                //create a "dummy" stylesheet
                CSSMediaRule cssmr = (CSSMediaRule) rl.item(i);
                StylesheetInfo info = new StylesheetInfo();
                info.setMedia(cssmr.getMedia().getMediaText());
                info.setOrigin(stylesheet.getOrigin());
                info.setType("text/css");
                Stylesheet mr = new Stylesheet(info.getUri(), info.getOrigin());
                info.setStylesheet(mr);//there, the "dummy" connection is made
                pullRulesets(cssmr.getCssRules(), mr, info);
                stylesheet.addStylesheet(info);
            }
        }
    }
}
