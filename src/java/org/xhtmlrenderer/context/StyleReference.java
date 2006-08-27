/*
 * StyleReference.java
 * Copyright (c) 2004, 2005 Torbj�rn Gannholm
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.extend.AttributeResolver;
import org.xhtmlrenderer.css.extend.lib.DOMTreeResolver;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.sheet.InlineStyleInfo;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.extend.UserInterface;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.util.XRLog;


/**
 * @author Torbj�rn Gannholm
 */
public class StyleReference {
    /**
     * The Context this StyleReference operates in; used for property
     * resolution.
     */
    private SharedContext _context;

    /**
     * Description of the Field
     */
    private NamespaceHandler _nsh;

    /**
     * Description of the Field
     */
    private Document _doc;

    /**
     * Description of the Field
     */
    private StylesheetFactoryImpl _stylesheetFactory;

    /**
     * Instance of our element-styles matching class. Will be null if new rules
     * have been added since last match.
     */
    private org.xhtmlrenderer.css.newmatch.Matcher _matcher;

    /** */
    private UserAgentCallback _uac;
    
    /**
     * Default constructor for initializing members.
     *
     * @param userAgent PARAM
     */
    public StyleReference(UserAgentCallback userAgent) {
        _uac = userAgent;
        _stylesheetFactory = new StylesheetFactoryImpl(userAgent);
    }

    /**
     * Sets the documentContext attribute of the StyleReference object
     *
     * @param context The new documentContext value
     * @param nsh     The new documentContext value
     * @param doc     The new documentContext value
     * @param ui
     */
    public void setDocumentContext(SharedContext context, NamespaceHandler nsh, Document doc, UserInterface ui) {
        _context = context;
        _nsh = nsh;
        _doc = doc;
        AttributeResolver attRes = new StandardAttributeResolver(_nsh, _uac, ui);

        List infos = getStylesheets();
        XRLog.match("media = " + _context.getMedia());
        _matcher = new org.xhtmlrenderer.css.newmatch.Matcher(
                new DOMTreeResolver(), attRes, _stylesheetFactory, infos, _context.getMedia());
    }

    /**
     * Description of the Method
     *
     * @param e PARAM
     * @return Returns
     */
    public boolean isHoverStyled(Element e) {
        return _matcher.isHoverStyled(e);
    }

    /**
     * Returns a Map keyed by CSS property names (e.g. 'border-width'), and the
     * assigned value as a SAC CSSValue instance. The properties should have
     * been matched to the element when the Context was established for this
     * StyleReference on the Document to which the Element belongs. See {@link
     * org.xhtmlrenderer.swing.BasicPanel#setDocument(Document, java.net.URL)}
     * for an example of how to establish a StyleReference and associate to a
     * Document.
     *
     * @param e The DOM Element for which to find properties
     * @return Map of CSS property names to CSSValue instance assigned to it.
     */
    public java.util.Map getCascadedPropertiesMap(Element e) {
        CascadedStyle cs = _matcher.getCascadedStyle(e, false);//this is only for debug, I think
        java.util.LinkedHashMap props = new java.util.LinkedHashMap();
        for (java.util.Iterator i = cs.getCascadedPropertyDeclarations(); i.hasNext();) {
            PropertyDeclaration pd = (PropertyDeclaration) i.next();

            String propName = pd.getPropertyName();
            CSSName cssName = CSSName.getByPropertyName(propName);
            props.put(propName, cs.propertyByName(cssName).getValue());
        }
        return props;
    }

    /**
     * Gets the pseudoElementStyle attribute of the StyleReference object
     *
     * @param node          PARAM
     * @param pseudoElement PARAM
     * @return The pseudoElementStyle value
     */
    public CascadedStyle getPseudoElementStyle(Node node, String pseudoElement) {
        Element e = null;
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            e = (Element) node;
        } else {
            e = (Element) node.getParentNode();
        }
        return _matcher.getPECascadedStyle(e, pseudoElement);
    }

    /**
     * Gets the CascadedStyle for an element. This must then be converted in the
     * current context to a CalculatedStyle (use getDerivedStyle)
     *
     * @param e       The element
     * @param restyle
     * @return The style value
     */
    public CascadedStyle getCascadedStyle(Element e, boolean restyle) {
        if (e == null) return CascadedStyle.emptyCascadedStyle;
        return _matcher.getCascadedStyle(e, restyle);
    }
    
    public CascadedStyle getPageStyle() {
        return _matcher.getPageCascadedStyle();
    }
    
    public CascadedStyle getPageStyle(String pseudoPage) {
        return _matcher.getPageCascadedStyle(pseudoPage);
    }

    /**
     * Flushes any stylesheet associated with this stylereference (based on the user agent callback) that are in cache.
     */
    public void flushStyleSheets() {
        String uri = _uac.getBaseURL();
        StylesheetInfo info = new StylesheetInfo();
        info.setUri(uri);
        info.setOrigin(StylesheetInfo.AUTHOR);
        if (_stylesheetFactory.containsStylesheet(uri)) {
            _stylesheetFactory.removeCachedStylesheet(uri);
            XRLog.cssParse("Removing stylesheet '" + uri + "' from cache by request.");
        } else {
            XRLog.cssParse("Requested removing stylesheet '" + uri + "', but it's not in cache.");

        }
    }
    
    public void flushAllStyleSheets() {
        _stylesheetFactory.flushCachedStylesheets();
    }

    /**
     * Gets StylesheetInfos for all stylesheets and inline styles associated
     * with the current document. Default (user agent) stylesheet and the inline
     * style for the current media are loaded and cached in the
     * StyleSheetFactory by URI.
     *
     * @return The stylesheets value
     */
    private List getStylesheets() {
        List infos = new LinkedList();
        long st = System.currentTimeMillis();

        String uri = _nsh.getNamespace();
        StylesheetInfo info = new StylesheetInfo();
        info.setUri(_nsh.getNamespace());
        info.setOrigin(StylesheetInfo.USER_AGENT);
        info.setMedia("all");
        info.setType("text/css");
        if (!_stylesheetFactory.containsStylesheet(uri)) {
            java.io.InputStream stream = _nsh.getDefaultStylesheet();
            if (stream != null) {
                Stylesheet sheet = _stylesheetFactory.parse(stream, info);
                _stylesheetFactory.putStylesheet(uri, sheet);
                infos.add(info);
            }
        } else
            infos.add(info);

        StylesheetInfo[] refs = _nsh.getStylesheetLinks(_doc);
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                uri = _uac.resolveURI(refs[i].getUri());
                refs[i].setUri(uri);
            }
        }
        infos.addAll(Arrays.asList(refs));

        uri = _uac.getBaseURL();
        info = new StylesheetInfo();
        info.setUri(uri);
        info.setOrigin(StylesheetInfo.AUTHOR);
        Stylesheet sheet = null;
        if (_stylesheetFactory.containsStylesheet(uri)) {
            sheet = _stylesheetFactory.getCachedStylesheet(uri);
        } else {
            InlineStyleInfo[] inlineStyle = _nsh.getInlineStyle(_doc);
            sheet = _stylesheetFactory.parseInlines(inlineStyle, info);
            _stylesheetFactory.putStylesheet(uri, sheet);
        }
        info.setStylesheet(sheet);//add it here because matcher cannot look it up, uri:s are in a twist
        infos.add(info);

        // TODO: here we should also get user stylesheet from userAgent

        long el = System.currentTimeMillis() - st;
        XRLog.load("TIME: parse stylesheets  " + el + "ms");

        return infos;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.9  2006/08/27 00:36:14  peterbrant
 * Initial commit of (initial) R7 work
 *
 * Revision 1.8  2006/01/03 23:02:37  peterbrant
 * Remove unused variable
 *
 * Revision 1.7  2005/12/30 01:32:43  peterbrant
 * First merge of parts of pagination work
 *
 * Revision 1.6  2005/11/11 01:33:15  peterbrant
 * Add ability to clear all cached stylesheets
 *
 * Revision 1.5  2005/10/27 00:08:51  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.4  2005/06/26 15:48:10  tobega
 * Converted to almost standard html4 default css, which shook out a bug: position should not inherit
 *
 * Revision 1.3  2005/06/25 19:27:46  tobega
 * UAC now supplies Resources
 *
 * Revision 1.2  2005/06/23 17:03:40  tobega
 * css now independent of DOM
 *
 * Revision 1.1  2005/06/22 23:48:40  tobega
 * Refactored the css package to allow a clean separation from the core.
 *
 * Revision 1.34  2005/06/16 12:59:23  pdoubleya
 * Cleaned up support for reloading documents.
 *
 * Revision 1.33  2005/06/16 11:29:12  pdoubleya
 * First cut support for reload page, flushes inline stylesheets.
 *
 * Revision 1.32  2005/06/16 07:24:48  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.31  2005/06/15 11:53:45  tobega
 * Changed UserAgentCallback to getInputStream instead of getReader. Fixed up some consequences of previous change.
 *
 * Revision 1.30  2005/06/01 21:36:37  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.29  2005/05/17 06:56:23  tobega
 * Inline backgrounds now work correctly, as does mixing of inlines and blocks for style inheritance
 *
 * Revision 1.28  2005/05/08 15:37:29  tobega
 * Fixed up style caching so it really works (internalize CascadedStyles and let each CalculatedStyle keep track of its derived children)
 *
 * Revision 1.27  2005/05/08 14:51:22  tobega
 * Removed the need for the Styler
 *
 * Revision 1.26  2005/05/08 14:36:54  tobega
 * Refactored away the need for having a context in a CalculatedStyle
 *
 * Revision 1.25  2005/03/24 23:18:38  pdoubleya
 * Added use of SharedContext (Kevin).
 *
 * Revision 1.24  2005/01/29 20:19:22  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.23  2005/01/29 12:49:24  pdoubleya
 * Fixed cast on get...PropertiesMap().
 *
 * Revision 1.22  2005/01/24 19:01:09  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.21  2005/01/24 14:36:30  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.20  2005/01/16 18:50:03  tobega
 * Re-introduced caching of styles, which make hamlet and alice scroll nicely again. Background painting still slow though.
 *
 * Revision 1.19  2005/01/08 15:56:54  tobega
 * Further work on extensibility interfaces. Documented it - see website.
 *
 * Revision 1.18  2005/01/08 11:55:16  tobega
 * Started massaging the extension interfaces
 *
 * Revision 1.17  2005/01/04 10:19:11  tobega
 * resolve selectors to styles direcly on match, should reduce memory footprint and not affect speed very much.
 *
 * Revision 1.16  2005/01/03 23:40:40  tobega
 * Cleaned out unnecessary styling/matching code. styling/matching is now called during boxing/rendering rather than as a separate stage.
 *
 * Revision 1.15  2004/12/29 10:39:27  tobega
 * Separated current state Context into LayoutContext and the rest into SharedContext.
 *
 * Revision 1.14  2004/12/28 01:48:22  tobega
 * More cleaning. Magically, the financial report demo is starting to look reasonable, without any effort being put on it.
 *
 * Revision 1.13  2004/12/11 18:18:08  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
 * Revision 1.22  2004/12/05 18:11:36  tobega
 * Now uses style cache for pseudo-element styles. Also started preparing to replace inline node handling with inline content handling.
 *
 * Revision 1.21  2004/12/05 14:35:38  tobega
 * Cleaned up some usages of Node (and removed unused stuff) in layout code. The goal is to pass "better" objects than Node wherever possible in an attempt to shake out the bugs in tree-traversal (probably often unnecessary tree-traversal)
 *
 * Revision 1.20  2004/12/05 00:48:53  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.19  2004/12/02 19:46:35  tobega
 * Refactored handling of inline styles to fit with StylesheetInfo and media handling (is also now correct if there should be more than one style element)
 *
 * Revision 1.18  2004/12/01 14:02:51  joshy
 * modified media to use the value from the rendering context
 * added the inline-block box
 * - j
 *
 * Revision 1.17  2004/11/30 23:47:56  tobega
 * At-media rules should now work (not tested). Also fixed at-import rules, which got broken at previous modification.
 *
 * Revision 1.16  2004/11/29 23:25:37  tobega
 * Had to redo thinking about Stylesheets and StylesheetInfos. Now StylesheetInfos are passed around instead of Stylesheets because any Stylesheet should only be linked to its URI. Bonus: the external sheets get lazy-loaded only if needed for the medium.
 *
 * Revision 1.15  2004/11/28 23:29:00  tobega
 * Now handles media on Stylesheets, still need to handle at-media-rules. The media-type should be set in Context.media (set by default to "screen") before calling setContext on StyleReference.
 *
 * Revision 1.14  2004/11/15 22:22:08  tobega
 * Now handles @import stylesheets
 *
 * Revision 1.13  2004/11/15 19:46:13  tobega
 * Refactoring in preparation for handling @import stylesheets
 *
 * Revision 1.12  2004/11/15 12:42:22  pdoubleya
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

