/*
 * StylesheetFactory.java
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
package org.xhtmlrenderer.css.sheet;

import com.steadystate.css.parser.CSSOMParser;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.*;
import org.w3c.dom.stylesheets.MediaList;
import org.xhtmlrenderer.extend.UserAgentCallback;
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
     *
     * @param userAgent PARAM
     */
    public StylesheetFactory(UserAgentCallback userAgent) {
        _userAgent = userAgent;
    }

    /**
     * Description of the Method
     *
     * @param stream PARAM
     * @param info
     * @return Returns
     */
    public Stylesheet parse(InputStream stream, StylesheetInfo info) {
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
    public Stylesheet parse(StylesheetInfo info) {
        InputStream is = _userAgent.getInputStream(info.getUri());

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
        InputStream is = _userAgent.getInputStream(info.getUri());
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
    public Ruleset parseStyleDeclaration(int origin, String styleDeclaration) {
        try {
            java.io.StringReader reader = new java.io.StringReader("* {" + styleDeclaration + "}");
            InputSource is = new InputSource(reader);
            CSSStyleSheet style = parser.parseStyleSheet(is);
            reader.close();
            return new Ruleset((CSSStyleRule) style.getCssRules().item(0), origin);
        } catch (Exception ex) {
            throw new XRRuntimeException("Cannot parse style declaration from string.", ex);
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
    public void putStylesheet(Object key, Stylesheet sheet) {
        _cache.put(key, sheet);
    }

    /**
     * @param key
     * @return true if a Stylesheet with this key has been put in the cache.
     *         Note that the Stylesheet may be null.
     */
    //TODO: work out how to handle caching properly, with cache invalidation
    public boolean containsStylesheet(Object key) {
        return _cache.containsKey(key);
    }

    /**
     * Returns a cached sheet by its key; null if no entry for that key.
     *
     * @param key The key for this sheet; same as key passed to
     *            putStylesheet();
     * @return The stylesheet
     */
    public Stylesheet getCachedStylesheet(Object key) {
        return (Stylesheet) _cache.get(key);
    }

    /**
     * Removes a cached sheet by its key.
     *
     * @param key The key for this sheet; same as key passed to
     *            putStylesheet();
     */
    public Object removeCachedStylesheet(Object key) {
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

/*
 * $Id$
 *
 * $Log$
 * Revision 1.20  2005/06/16 12:59:23  pdoubleya
 * Cleaned up support for reloading documents.
 *
 * Revision 1.19  2005/06/16 11:29:12  pdoubleya
 * First cut support for reload page, flushes inline stylesheets.
 *
 * Revision 1.18  2005/06/16 07:24:46  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.17  2005/06/15 11:53:45  tobega
 * Changed UserAgentCallback to getInputStream instead of getReader. Fixed up some consequences of previous change.
 *
 * Revision 1.16  2005/06/01 21:36:36  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.15  2005/03/24 23:17:58  pdoubleya
 * Added debug dump on bad CSS input.
 *
 * Revision 1.14  2005/02/03 23:08:26  pdoubleya
 * .
 *
 * Revision 1.13  2005/01/29 20:19:21  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.12  2005/01/10 23:24:46  tobega
 * Created image cache
 *
 * Revision 1.11  2004/12/11 18:18:07  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
 * Revision 1.10  2004/12/02 19:46:36  tobega
 * Refactored handling of inline styles to fit with StylesheetInfo and media handling (is also now correct if there should be more than one style element)
 *
 * Revision 1.9  2004/11/30 23:47:57  tobega
 * At-media rules should now work (not tested). Also fixed at-import rules, which got broken at previous modification.
 *
 * Revision 1.8  2004/11/29 23:25:40  tobega
 * Had to redo thinking about Stylesheets and StylesheetInfos. Now StylesheetInfos are passed around instead of Stylesheets because any Stylesheet should only be linked to its URI. Bonus: the external sheets get lazy-loaded only if needed for the medium.
 *
 * Revision 1.7  2004/11/28 23:29:02  tobega
 * Now handles media on Stylesheets, still need to handle at-media-rules. The media-type should be set in Context.media (set by default to "screen") before calling setContext on StyleReference.
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

