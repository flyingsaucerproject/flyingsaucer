/*
 * StyleReference.java
 * Copyright (c) 2004 Torbj�rn Gannholm
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
package org.xhtmlrenderer.css;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.sheet.InlineStyleInfo;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.StylesheetFactory;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.AttributeResolver;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.extend.UserInterface;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.util.XRLog;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * @author empty
 */
public class StyleReference {
    /**
     * The Context this StyleReference operates in; used for property resolution.
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
    private StylesheetFactory _stylesheetFactory;

    /**
     * Instance of our element-styles matching class. Will be null if new rules
     * have been added since last match.
     */
    private org.xhtmlrenderer.css.newmatch.Matcher _matcher;

    /**
     * Description of the Field
     */
    private org.xhtmlrenderer.css.style.Styler _styler;

    private UserAgentCallback _uac;

    /**
     * Default constructor for initializing members.
     *
     * @param userAgent PARAM
     */
    public StyleReference(UserAgentCallback userAgent) {
        _uac = userAgent;
        _stylesheetFactory = new StylesheetFactory(userAgent);
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
        _matcher = new org.xhtmlrenderer.css.newmatch.Matcher(attRes, _stylesheetFactory, infos.iterator(), _context.getMedia());
        _styler = new org.xhtmlrenderer.css.style.Styler();
    }

    /**
     * Gets the derivedPropertiesMap attribute of the StyleReference object
     *
     * @param e PARAM
     * @return The derivedPropertiesMap value
     */
    public java.util.Map getCascadedPropertiesMap(Element e) {
        CascadedStyle cs = _matcher.getCascadedStyle(e);
        java.util.LinkedHashMap props = new java.util.LinkedHashMap();
        for (java.util.Iterator i = cs.getMatchedPropertyDeclarations(); i.hasNext();) {
            String propName = (String) i.next();
            props.put(propName, cs.propertyByName(propName).getValue());
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
     * Gets the CascadedStyle for an element.
     * This must then be converted in the current context to a CalculatedStyle (use getDerivedStyle)
     *
     * @param e The element
     * @return The style value
     */
    public CascadedStyle getCascadedStyle(Element e) {
        return _matcher.getCascadedStyle(e);
    }

    public CalculatedStyle getDerivedStyle(CalculatedStyle parent, CascadedStyle matched) {
        return _styler.getDerivedStyle(parent, matched);
    }

    /**
     * Gets StylesheetInfos for all stylesheets and inline styles associated with the current
     * document. Default (user agent) stylesheet and the inline style for the current media
     * are loaded and cached in the StyleSheetFactory by URI.
     */
    private List getStylesheets() {
        java.io.Reader reader;
        List infos = new LinkedList();
        long st = System.currentTimeMillis();

        String uri = _nsh.getNamespace();
        StylesheetInfo info = new StylesheetInfo();
        info.setUri(_nsh.getNamespace());
        info.setOrigin(StylesheetInfo.USER_AGENT);
        info.setMedia("all");
        info.setType("text/css");
        if (!_stylesheetFactory.containsStylesheet(uri)) {
            reader = _nsh.getDefaultStylesheet();
            if (reader != null) {
                Stylesheet sheet = _stylesheetFactory.parse(reader, info);
                _stylesheetFactory.putStylesheet(uri, sheet);
            }
        }
        infos.add(info);

        StylesheetInfo[] refs = _nsh.getStylesheetLinks(_doc);
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                java.net.URL baseUrl = _context.getRenderingContext().getBaseURL();
                try {
                    uri = new java.net.URL(baseUrl, refs[i].getUri()).toString();
                    refs[i].setUri(uri);
                } catch (java.net.MalformedURLException e) {
                    XRLog.exception("bad URL for associated stylesheet", e);
                }
            }
        }
        infos.addAll(Arrays.asList(refs));

        uri = _context.getRenderingContext().getBaseURL().toString();
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

        //here we should also get user stylesheet from userAgent

        long el = System.currentTimeMillis() - st;
        XRLog.load("TIME: parse stylesheets  " + el + "ms");

        return infos;
    }

}

/*
 * :folding=java:collapseFolds=1:noTabs=true:tabSize=4:indentSize=4:
 */
/*
 * $Id$
 *
 * $Log$
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
 * Separated current state Context into ContextImpl and the rest into SharedContext.
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
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
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

