/*
 * TBStyleReference.java
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
package org.xhtmlrenderer.css.bridge;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;
import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.StyleReference;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.StylesheetFactory;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.DerivedProperty;
import org.xhtmlrenderer.css.value.BorderColor;
import org.xhtmlrenderer.extend.AttributeResolver;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * Does not really implement StyleReference, but anyway
 *
 * @author empty
 */
public class TBStyleReference implements StyleReference {
    /**
     * The Context this StyleReference operates in; used for property resolution.
     */
    private Context _context;

    /**
     * Description of the Field
     */
    private UserAgentCallback _userAgent;

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
    private AttributeResolver _attRes;

    /**
     * Description of the Field
     */
    private StylesheetFactory _stylesheetFactory;

    /**
     * Instance of our element-styles matching class. Will be null if new rules
     * have been added since last match.
     */
    private org.xhtmlrenderer.css.newmatch.Matcher _tbStyleMap;

    /**
     * Description of the Field
     */
    private org.xhtmlrenderer.css.style.Styler _styler;

    /**
     * used for caching Stylesheet instances, wonder why...
     */
    private List _stylesheets;

    /**
     * Default constructor for initializing members.
     *
     * @param userAgent PARAM
     */
    public TBStyleReference(UserAgentCallback userAgent) {
        _userAgent = userAgent;
        _stylesheetFactory = new StylesheetFactory(userAgent);
    }


    /**
     * Checks whether a property is defined at all for an Element, inherited or
     * not.
     *
     * @param elem The DOM Element to find the property for
     * @param prop The property name
     * @return True if the Element, or an ancestor, has the property
     *         1     *      defined.
     */
    public boolean hasProperty(Element elem, String prop) {
        return hasProperty((Node) elem, prop, false);
    }


    /**
     * Checks whether a property is defined at all for an Element, searching
     * ancestor Elements for the property if requested.
     *
     * @param elem    The DOM Element to find the property for
     * @param prop    The property name
     * @param inherit If true, searches ancestors for the Element for the
     *                property as well.
     * @return True if the Element has the property defined.
     */
    public boolean hasProperty(Element elem, String prop, boolean inherit) {
        return hasProperty((Node) elem, prop, false);
    }


    /**
     * Checks whether a property is defined at all for an Node, searching
     * ancestor Nodes for the property if requested.
     *
     * @param node    The DOM Node to find the property for
     * @param prop    The property name
     * @param inherit If true, searches ancestors for the Node for the property
     *                as well.
     * @return True if the Node has the property defined.
     */
    public boolean hasProperty(Node node, String prop, boolean inherit) {
        return _styler.getCalculatedStyle((Element) node).hasProperty(prop);
    }

    /**
     * Description of the Method
     *
     * @param e PARAM
     * @return Returns
     */
    public boolean wasHoverRestyled(Element e) {
        boolean isHoverStyled = _tbStyleMap.isHoverStyled(e);
        //XRLog.general("Element "+e+" tested for hover styling "+isHoverStyled);
        if (_tbStyleMap.isHoverStyled(e)) {
            _styler.restyleTree(e);
            return true;
        }
        return false;
    }

    /**
     * Sets the documentContext attribute of the TBStyleReference object
     *
     * @param context The new documentContext value
     * @param nsh     The new documentContext value
     * @param ar      The new documentContext value
     * @param doc     The new documentContext value
     */
    public void setDocumentContext(Context context, NamespaceHandler nsh, AttributeResolver ar, Document doc) {
        _context = context;
        _nsh = nsh;
        _doc = doc;
        _attRes = ar;

        parseStylesheets();
        matchStyles();
    }


    /**
     * Returns the background Color assigned to an Element
     *
     * @param elem The DOM element to find the property for.
     * @return The background-color Color property
     */
    public Color getBackgroundColor(Element elem) {
        return _styler.getCalculatedStyle(elem).getBackgroundColor();
    }


    /**
     * Returns the border Color assigned to an Element
     *
     * @param elem The DOM element to find the property for.
     * @return The border-color Color property
     */
    public BorderColor getBorderColor(Element elem) {
        return _styler.getCalculatedStyle(elem).getBorderColor();
    }


    /**
     * Returns the border width (all sides) assigned to an Element
     *
     * @param elem The DOM element to find the property for.
     * @return The Border property (for widths)
     */
    public Border getBorderWidth(Element elem) {
        return _styler.getCalculatedStyle(elem).getBorderWidth();
    }


    /**
     * Returns the foreground Color assigned to an Element, searching ancestor
     * Elements for an inheritable value if not found on the Element.
     *
     * @param elem The DOM element to find the property for.
     * @return The CSS color Color property
     */
    public Color getColor(Element elem) {
        return _styler.getCalculatedStyle(elem).getColor();
    }


    /**
     * Returns the foreground Color assigned to an Element, searching ancestor
     * Elements for an inheritable value if requested.
     *
     * @param elem    The DOM element to find the property for.
     * @param inherit If true and property not found on this element, searches
     *                through element ancestors for property
     * @return The foreground Color property
     */
    public Color getColor(Element elem, boolean inherit) {
        return _styler.getCalculatedStyle(elem).getColor();
    }


    /**
     * Returns the a property assigned to an Element that can be interpreted as
     * a Point with floating-point positioning, and, if not found and inherit is
     * true, searches for an inheritable property by that name assigned to
     * parent and ancestor elements.
     *
     * @param elem    The DOM element to find the property for.
     * @param prop    The property name
     * @param inherit If true and property not found on this element, searches
     *                through element ancestors for property
     * @return The named property as a Point
     */
    public Point getFloatPairProperty(Element elem, String prop, boolean inherit) {
        DerivedProperty xrProp = _styler.getCalculatedStyle(elem).propertyByName(prop);

        if (xrProp.computedValue().isValueList()) {
            CSSValueList vl = (CSSValueList) xrProp.computedValue().cssValue();

            Point pt = new Point();

            pt.setLocation(((CSSPrimitiveValue) vl.item(0)).getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE),
                    ((CSSPrimitiveValue) vl.item(1)).getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE));

            return pt;
        } else {
            XRLog.layout("Property : " + xrProp + " is not a value list " + xrProp.computedValue().cssValue().getClass().getName());
        }
        return null;
    }


    /**
     * Returns the value of a property matched to an element cast as a float,
     * searching ancestor Elements for an inheritable value if not found on the
     * Element.
     *
     * @param elem The DOM Element to find the property for
     * @param prop The property name
     * @return The named property as a float
     */
    public float getFloatProperty(Element elem, String prop) {
        return getFloatProperty((Node) elem, prop, 0F, false);
    }


    /**
     * Returns the value of a property matched to an element cast as a float,
     * inheriting the value from the nearest ancestor if requested
     *
     * @param elem    The DOM Element to find the property for
     * @param prop    The property name
     * @param inherit If true, searches ancestor Elements for the property if
     *                not defined on this Element.
     * @return The named property as a float
     */
    public float getFloatProperty(Element elem, String prop, boolean inherit) {
        return getFloatProperty((Node) elem, prop, 0F, inherit);
    }


    /**
     * Returns the value of a property matched to an element cast as a float,
     * using the parent float value to determine relative values if requested
     * (e.g. if the property is a percentage), and inheriting the value if
     * necessary.
     *
     * @param elem         The DOM Element to find the property for
     * @param prop         The property name
     * @param parent_value The Element's parent value for the same property
     * @return The named property as a float
     */
    public float getFloatProperty(Element elem, String prop, float parent_value) {
        return getFloatProperty((Node) elem, prop, parent_value, false);
    }


    /**
     * Returns the value of a property matched to an element cast as a float,
     * using the parent float value to determine relative values if requested
     * (e.g. if the property is a percentage), inheriting the value if
     * requested.
     *
     * @param elem         The DOM Element to find the property for
     * @param prop         The property name
     * @param parent_value The Element's parent value for the same property
     * @param inherit      If true, inherits the value from the Element's
     *                     parent
     * @return The named property as a float
     */
    public float getFloatProperty(Element elem, String prop, float parent_value, boolean inherit) {
        return getFloatProperty((Node) elem, prop, parent_value, inherit);
    }


    /**
     * Same as <code>getFloatProperty(Element, String, float, boolean)</code>,
     * but for Node elements
     *
     * @param node         The DOM Node to find the property for
     * @param prop         The property name
     * @param parent_value The Node's parent value for the same property
     * @param inherit      If true, inherits the value from the Node's parent
     * @return The named property as a float
     */
    public float getFloatProperty(Node node, String prop, float parent_value, boolean inherit) {
        return _styler.getCalculatedStyle((Element) node).propertyByName(prop).computedValue().asFloat();
    }


    /**
     * Returns the margin width (all sides) assigned to an Element
     *
     * @param elem The DOM element to find the property for.
     * @return The margin property as a Border (for widths)
     */
    public Border getMarginWidth(Element elem) {
        return _styler.getCalculatedStyle(elem).getMarginWidth();
    }


    /**
     * Returns the padding width (all sides) assigned to an Element
     *
     * @param elem The DOM element to find the property for.
     * @return The padding property as a Border (for widths)
     */
    public Border getPaddingWidth(Element elem) {
        return _styler.getCalculatedStyle(elem).getPaddingWidth();
    }


    /**
     * Returns the value of a property as a W3C CSSValue instance, inheriting
     * from the parent element if requested.
     *
     * @param elem    The DOM Element to find the property for
     * @param prop    The property name
     * @param inherit If true, inherits the value from the Element's parent
     * @return The property value as CSSValue
     */
    public CSSValue getProperty(Element elem, String prop, boolean inherit) {
        return _styler.getCalculatedStyle(elem).propertyByName(prop).computedValue().cssValue();
    }


    /**
     * Returns the value of a property as a String array, for example, for
     * font-family declarations, inheriting the property if nece.
     *
     * @param elem The DOM Element to find the property for
     * @param prop The property name
     * @return The property value as String array
     */
    public String[] getStringArrayProperty(Element elem, String prop) {
        return _styler.getCalculatedStyle(elem).propertyByName(prop).computedValue().asStringArray();
    }


    /**
     * Returns the value of a property as a String, inheriting the property if
     * necessary.
     *
     * @param elem The DOM Element to find the property for
     * @param prop The property name
     * @return The property value as String
     */
    public String getStringProperty(Element elem, String prop) {
        return _getStringProperty((Node) elem, prop, false);
    }


    /**
     * Returns the value of a property as a String, inheriting the value from
     * the Element's parent if requested.
     *
     * @param elem    The DOM Element to find the property for
     * @param prop    The property name
     * @param inherit If true, inherits the property from the Element's parent
     *                if necessary.
     * @return The property value as String
     */
    public String getStringProperty(Element elem, String prop, boolean inherit) {
        return _getStringProperty((Node) elem, prop, inherit);
    }


    /**
     * Returns the value of a property as a String from a DOM Node instance,
     * searching ancestor Elements for an inheritable value if not found on the
     * Element.
     *
     * @param node The DOM Node to find the property for
     * @param prop The property name
     * @return The property value as String
     */
    public String getStringProperty(Node node, String prop) {
        return _getStringProperty(node, prop, false);
    }


    /**
     * Returns the value of a property as a String, inheriting the value from
     * the Node's parent if requested.
     *
     * @param node    The DOM Node to find the property for.
     * @param prop    The property name
     * @param inherit If true, inherits the property from the Element's parent
     *                if necessary
     * @return The property value as String
     */
    public String getStringProperty(Node node, String prop, boolean inherit) {
        return _getStringProperty(node, prop, false);
    }

    /**
     * Gets the derivedPropertiesMap attribute of the TBStyleReference object
     *
     * @param e PARAM
     * @return The derivedPropertiesMap value
     */
    public java.util.Map getDerivedPropertiesMap(Element e) {
        org.xhtmlrenderer.css.style.CalculatedStyle cs = _styler.getCalculatedStyle(e);
        java.util.LinkedHashMap props = new java.util.LinkedHashMap();
        for (java.util.Iterator i = cs.getAvailablePropertyNames().iterator(); i.hasNext();) {
            String propName = (String) i.next();
            props.put(propName, cs.propertyByName(propName).computedValue().cssValue());
        }
        return props;
    }

    /**
     * Gets the firstLetterStyle attribute of the TBStyleReference object
     *
     * @param e PARAM
     * @return The firstLetterStyle value
     */
    public CalculatedStyle getFirstLetterStyle(Element e) {
        return null;//not supported yet
    }

    /**
     * Gets the pseudoElementStyle attribute of the TBStyleReference object
     *
     * @param e             PARAM
     * @param pseudoElement PARAM
     * @return The pseudoElementStyle value
     */
    public CascadedStyle getPseudoElementStyle(Element e, String pseudoElement) {
        return _tbStyleMap.getPECascadedStyle(e, pseudoElement);
    }

    /**
     * Gets the style attribute of the TBStyleReference object
     *
     * @param e PARAM
     * @return The style value
     */
    public CalculatedStyle getStyle(Element e) {
        return _styler.getCalculatedStyle(e);
    }

    /**
     * Loads all stylesheets and inline styles associated with the current
     * document. Default (user agent) stylesheet is loaded as well. Sheets are
     * loaded into _stylesheets List, and cached in the StyleSheetFactory by
     * URI.
     */
    private void parseStylesheets() {
        java.io.Reader reader;
        _stylesheets = new LinkedList();
        long st = System.currentTimeMillis();

        String uri = _nsh.getNamespace();
        Stylesheet sheet = (Stylesheet) _stylesheetFactory.getStylesheet(uri);
        if (sheet == null) {
            reader = _nsh.getDefaultStylesheet();
            StylesheetInfo info = new StylesheetInfo();
            info.setUri(_nsh.getNamespace());
            info.setOrigin(StylesheetInfo.USER_AGENT);
            info.setMedia("all");
            info.setType("text/css");
            if (reader != null) {
                sheet = _stylesheetFactory.parse(reader, info);
                _stylesheetFactory.putStylesheet(uri, sheet);
            }
        }
        if (sheet != null) {
            _stylesheets.add(sheet);
        }

        StylesheetInfo[] refs = _nsh.getStylesheetLinks(_doc);
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                java.net.URL baseUrl = _context.getRenderingContext().getBaseURL();
                try {
                    uri = new java.net.URL(baseUrl, refs[i].getUri()).toString();
                    sheet = _stylesheetFactory.getStylesheet(uri);
                    if (sheet == null) {
                        reader = _userAgent.getReaderForURI(uri);
                        if (reader != null) {
                            sheet = _stylesheetFactory.parse(reader, refs[i]);
                            _stylesheetFactory.putStylesheet(uri, sheet);
                        }
                    }
                    if (sheet != null) {
                        _stylesheets.add(sheet);
                    }
                } catch (java.net.MalformedURLException e) {
                    XRLog.exception("bad URL for associated stylesheet", e);
                }
            }
        }

        String baseUri = _context.getRenderingContext().getBaseURL().toString();
        uri = baseUri + "?-fs-media-?" + _context.media;
        sheet = _stylesheetFactory.getStylesheet(uri);
        if (sheet == null) {
            String inlineStyle = _nsh.getInlineStyle(_doc, _context.media);
            if (inlineStyle != null) {
                reader = new java.io.StringReader(inlineStyle);
                StylesheetInfo info = new StylesheetInfo();
                info.setUri(baseUri);
                info.setType("text/css");
                info.setOrigin(StylesheetInfo.AUTHOR);
                info.setMedia(_context.media);
                sheet = _stylesheetFactory.parse(reader, info);
                _stylesheetFactory.putStylesheet(uri, sheet);
            }
        }
        if (sheet != null) {
            _stylesheets.add(sheet);
        }

        //here we should also get user stylesheet from userAgent

        long el = System.currentTimeMillis() - st;
        XRLog.load("TIME: parse stylesheets  " + el + "ms");
    }


    /**
     * <p/>
     * <p/>
     * Attempts to match any styles loaded to Elements in the supplied Document,
     * using CSS2 matching guidelines re: selection to prepare internal lookup
     * routines for property lookup methods (e.g. {@link #getProperty(Element,
            * String, boolean)}). This should be called after all stylesheets and
     * styles are loaded, but before any properties are retrieved. </p>
     */
    private void matchStyles() {
        long st = System.currentTimeMillis();
        try {

            XRLog.match("No of stylesheets = " + _stylesheets.size());
            List sortedXRRules = new ArrayList();
            Iterator iter = _stylesheets.iterator();

            _tbStyleMap = new org.xhtmlrenderer.css.newmatch.Matcher(_doc, _attRes, _stylesheets.iterator(), _context.media);
            _tbStyleMap.setStylesheetFactory(_stylesheetFactory);

            // now we have a match-map, apply against our entire Document....restyleTree() is recursive
            Element root = _doc.getDocumentElement();
            _styler = new org.xhtmlrenderer.css.style.Styler();
            _styler.setMatcher(_tbStyleMap);
            _styler.styleTree(root);
        } catch (RuntimeException re) {
            throw new XRRuntimeException("Failed on matchStyles(), unknown RuntimeException.", re);
        } catch (Exception e) {
            throw new XRRuntimeException("Failed on matchStyles(), unknown Exception.", e);
        }
        long el = System.currentTimeMillis() - st;
        XRLog.match("TIME: match styles  " + el + "ms");
    }


    /**
     * Returns the value of a property as a String, inheriting the value from
     * the Node's parent if requested.
     *
     * @param node    The DOM Node to find the property for.
     * @param prop    The property name
     * @param inherit If true, inherits the property from the Element's parent
     *                if necessary
     * @return The property value as String
     */
    //Tobe: this is actually called with a non-element!
    private String _getStringProperty(Node node, String prop, boolean inherit) {
        Element elem = nearestElementAncestor(node);

        if (elem == null) {
            return null;
        }
        return _styler.getCalculatedStyle(elem).propertyByName(prop).computedValue().asString();
    }

    /**
     * Description of the Method
     *
     * @param node PARAM
     * @return Returns
     */
    private org.w3c.dom.Element nearestElementAncestor(Node node) {
        while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
            node = node.getParentNode();
        }

        return (Element) node;
    }

}

/*
 * :folding=java:collapseFolds=1:noTabs=true:tabSize=4:indentSize=4:
 */
/*
 * $Id$
 *
 * $Log$
 * Revision 1.15  2004/11/28 23:29:00  tobega
 * Now handles media on Stylesheets, still need to handle at-media-rules. The media-type should be set in Context.media (set by default to "screen") before calling setContext on TBStyleReference.
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

