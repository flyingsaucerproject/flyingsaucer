/*
 * StylesheetFactory.java
 * Copyright (c) 2004 Torbjörn Gannholm
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
package org.xhtmlrenderer.css.sheet;

import com.steadystate.css.parser.CSSOMParser;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.stylesheets.MediaList;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.io.Reader;
import java.net.URL;


/**
 * A Factory class for Cascading Style Sheets. Sheets are parsed using a single
 * parser instance for all sheets. Sheets are cached by URI using a LRU test,
 * but timestamp of file is not checked.
 *
 * @author Torbjörn Gannholm
 */
// ASK: tested for multi-thread access? (PW 12-11-04)
// TODO: add timestamp check (PW 12-11-04)
public class StylesheetFactory {

    /**
     * the UserAgentCallback to resolve uris
     */
    private UserAgentCallback _userAgent;

    /**
     * Description of the Field
     */
    private CSSOMParser parser = new CSSOMParser();

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

    /**
     * Creates a new instance of StylesheetFactory
     */
    public StylesheetFactory(UserAgentCallback userAgent) {
        _userAgent = userAgent;
    }

    /**
     * Description of the Method
     *
     * @param reader PARAM
     * @param info
     * @return Returns
     */
    public Stylesheet parse(java.io.Reader reader, StylesheetInfo info) {
        InputSource is = new InputSource(reader);
        CSSStyleSheet style = null;
        try {
            style = parser.parseStyleSheet(is);
        } catch (java.io.IOException e) {
            throw new XRRuntimeException("IOException on parsing style seet from a Reader; don't know the URI.", e);
        }

        Stylesheet sheet = new Stylesheet(info);
        pullRulesets(style, sheet);

        return sheet;
    }

    /**
     * Description of the Method
     *
     * @param info
     * @return Returns null if uri could not be loaded
     *         TODO: what about relative uris? what are they relative to? how resolve?
     */
    public Stylesheet parse(StylesheetInfo info) {
        Reader r = _userAgent.getReaderForURI(info.getUri());
        if (r != null) return parse(r, info);
        return null;
    }

    /**
     * Given the SAC sheet input, extracts all CSSStyleRules and loads Rulesets
     * from them.
     *
     * @param cssSheet   The SAC CSSStyleSheet instance that holds the sheet rules
     *                   and etc. from which rules are taken. Usually the output of a SAC parser.
     * @param stylesheet stylesheet to which rules are added
     */
    private void pullRulesets(org.w3c.dom.css.CSSStyleSheet cssSheet, Stylesheet stylesheet) {
        org.w3c.dom.css.CSSRuleList rl = cssSheet.getCssRules();
        int nr = rl.getLength();
        for (int i = 0; i < nr; i++) {
            if (rl.item(i).getType() == org.w3c.dom.css.CSSRule.IMPORT_RULE) {
                //note: the steadystate parser does not fetch and load imported stylesheets
                CSSImportRule cssir = (CSSImportRule) rl.item(i);
                String href = cssir.getHref();
                MediaList media = cssir.getMedia();
                String uri = null;
                try {
                    uri = new java.net.URL(new URL(stylesheet.getURI()), href).toString();
                    StylesheetInfo info = new StylesheetInfo();
                    info.setOrigin(stylesheet.getOrigin());
                    info.setUri(uri);
                    info.setMedia(media.getMediaText());
                    info.setType("text/css");
                    stylesheet.addStylesheet(info);
                } catch (java.net.MalformedURLException e) {
                    XRLog.exception("bad URL for imported stylesheet", e);
                }
            } else if (rl.item(i).getType() == org.w3c.dom.css.CSSRule.STYLE_RULE) {
                stylesheet.addRuleset(new Ruleset((org.w3c.dom.css.CSSStyleRule) rl.item(i), stylesheet.getOrigin()));
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param origin           PARAM
     * @param styleDeclaration PARAM
     * @return Returns
     */
    public Ruleset parseStyleDeclaration(int origin, String styleDeclaration) {
        try {
            java.io.StringReader reader = new java.io.StringReader("* {" + styleDeclaration + "}");
            InputSource is = new InputSource(reader);
            CSSStyleSheet style = parser.parseStyleSheet(is);
            reader.close();
            return new Ruleset((CSSStyleRule) style.getCssRules().item(0), StylesheetInfo.AUTHOR);
        } catch (Exception ex) {
            throw new XRRuntimeException("Cannot parse style declaration from string.", ex);
        }
    }

    /**
     * Adds a stylesheet to the factory cache. Will overwrite older entry for
     * same key.
     *
     * @param key   Key to use to reference sheet later; must be unique in factory.
     * @param sheet The sheet to cache.
     */
    public void putStylesheet(Object key, Stylesheet sheet) {
        _cache.put(key, sheet);
    }

    /**
     * Returns a cached sheet by its key; null if no entry for that key.
     *
     * @param key The key for this sheet; same as key passed to putStylesheet();
     * @return The stylesheet
     */
    public Stylesheet getCachedStylesheet(Object key) {
        return (Stylesheet) _cache.get(key);
    }

    /**
     * Returns a cached sheet by its key; loads and caches it if not in cache; null if not able to load
     *
     * @param info The StylesheetInfo for this sheet
     * @return The stylesheet
     */
    public Stylesheet getStylesheet(StylesheetInfo info) {
        Stylesheet s = getCachedStylesheet(info.getUri());
        if (s == null && !containsStylesheet(info.getUri())) {
            s = parse(info);
            putStylesheet(info.getUri(), s);
        }
        return s;
    }

    /**
     * @param key
     * @return true if a Stylesheet with this key has been put in the cache. Note that the Stylesheet may be null.
     */
    public boolean containsStylesheet(Object key) {
        return _cache.containsKey(key);
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.8  2004/11/29 23:25:40  tobega
 * Had to redo thinking about Stylesheets and StylesheetInfos. Now StylesheetInfos are passed around instead of Stylesheets because any Stylesheet should only be linked to its URI. Bonus: the external sheets get lazy-loaded only if needed for the medium.
 *
 * Revision 1.7  2004/11/28 23:29:02  tobega
 * Now handles media on Stylesheets, still need to handle at-media-rules. The media-type should be set in Context.media (set by default to "screen") before calling setContext on TBStyleReference.
 *
 * Revision 1.6  2004/11/15 22:22:08  tobega
 * Now handles @import stylesheets
 *
 * Revision 1.5  2004/11/15 20:06:31  tobega
 * Should now handle @import stylesheets, at least those with absolute urls
 *
 * Revision 1.4  2004/11/15 19:46:14  tobega
 * Refactoring in preparation for handling @import stylesheets
 *
 * Revision 1.3  2004/11/15 12:42:23  pdoubleya
 * Across this checkin (all may not apply to this particular file)
 * Changed default/package-access members to private.
 * Changed to use XRRuntimeException where appropriate.
 * Began move from System.err.println to std logging.
 * Standard code reformat.
 * Removed some unnecessary SAC member variables that were only used in initialization.
 * CVS log section.
 *
 *
 */
