/*
 *
 * TBStyleReference.java
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

package org.xhtmlrenderer.css.bridge;

import java.awt.Color;
import java.awt.Point;
import java.io.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.*;

import org.w3c.css.sac.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSPrimitiveValue;
//import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import org.xhtmlrenderer.css.Border;
//import org.joshy.html.Context;
//import org.joshy.html.css.JStyle;
import org.xhtmlrenderer.css.StyleReference;
import org.xhtmlrenderer.layout.*;

import org.xhtmlrenderer.css.style.DerivedProperty;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;

import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.StylesheetFactory;

import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.AttributeResolver;

import org.xhtmlrenderer.css.value.BorderColor;
import org.xhtmlrenderer.util.XRLog;

/** Does not really implement StyleReference, but anyway */
 public class TBStyleReference implements StyleReference {

    /** The Context this StyleReference operates in; used for property resolution. */
    private Context _context;
    
    private UserAgentCallback _userAgent;
    
    //private XRDocument _doc;
    private NamespaceHandler _nsh;
    private Document _doc;
    private AttributeResolver _attRes;
    
    private StylesheetFactory _stylesheetFactory;

   /** Whether elements in the current document have been checked for style attributes */
    //private boolean _elementStyleAttributesPulled;
    
    /** Map from Element to XRStyleRules created from style attribute on it */
    //private Map _elementXRStyleMap;

    /**
     * ASK: holdover from Josh's processing code...apparently used as a trap to
     * make sure the same <code><style></code> elements within a document were
     * not read more than once.
     */
    //private List _inlineStyleElements;

    /**
     * Instance of our element-styles matching class. Will be null if new rules
     * have been added since last match.
     */
    private org.xhtmlrenderer.css.newmatch.Matcher _tbStyleMap;
    
    private org.xhtmlrenderer.css.style.Styler _styler;

    
    /** seems to need a list of XRStyleRules.... */
    //private List _xrStyleRuleList;
    private List _stylesheets;

    /**
     * Map from XRStyleRules to the Rulesets that contain them. Rulesets are passed
     * to Tobe's matching routine, hence...
     */
    //private Map _ruleSetMap;

    /**
     * Instantiates a new XRStyleReference for a given Context.
     *
     * @param context  Context instance used for property resolution when
     *      necessary.
     */
    /*public TBStyleReference(Context context, UserAgentCallback userAgent, Document doc) {
        this(userAgent);
        setDocumentContext(context, doc);
    }*/

    /** Default constructor for initializing members. */
    public TBStyleReference(UserAgentCallback userAgent) {
        _userAgent = userAgent;
        //_inlineStyleElements = new ArrayList();
        //_ruleSetMap = new HashMap();
        //_elementXRStyleMap = new HashMap();
        _stylesheetFactory = new StylesheetFactory();
    }
    
    public void setDocumentContext(Context context, NamespaceHandler nsh, AttributeResolver ar, Document doc) {
        _context = context;
        _nsh = nsh;
        _doc = doc;
        _attRes = ar;
        
        parseStylesheets();
        matchStyles();
    }


    /**
     * Checks whether a property is defined at all for an Element, inherited or
     * not.
     *
     * @param elem  The DOM Element to find the property for
     * @param prop  The property name
     * @return      True if the Element, or an ancestor, has the property
     *      defined.
     */
    public boolean hasProperty( Element elem, String prop ) {
        return hasProperty( (Node)elem, prop, false );
    }


    /**
     * Checks whether a property is defined at all for an Element, searching
     * ancestor Elements for the property if requested.
     *
     * @param elem     The DOM Element to find the property for
     * @param prop     The property name
     * @param inherit  If true, searches ancestors for the Element for the
     *      property as well.
     * @return         True if the Element has the property defined.
     */
    public boolean hasProperty( Element elem, String prop, boolean inherit ) {
        return hasProperty( (Node)elem, prop, false );
    }


    /**
     * Checks whether a property is defined at all for an Node, searching
     * ancestor Nodes for the property if requested.
     *
     * @param node     The DOM Node to find the property for
     * @param prop     The property name
     * @param inherit  If true, searches ancestors for the Node for the property
     *      as well.
     * @return         True if the Node has the property defined.
     */
    public boolean hasProperty( Node node, String prop, boolean inherit ) {
        return _styler.getCalculatedStyle((Element) node).hasProperty( prop );
    }
    
    private void parseStylesheets() {
        java.io.Reader reader;
        _stylesheets = new LinkedList();
        long st = System.currentTimeMillis();
        
        String uri = _nsh.getNamespace();
        Stylesheet sheet = (Stylesheet) _stylesheetFactory.getStylesheet(uri);
        if(sheet == null) {
            reader = _nsh.getDefaultStylesheet();
            if(reader != null) {
                sheet = _stylesheetFactory.parse(Stylesheet.USER_AGENT, reader);
                _stylesheetFactory.putStylesheet(uri, sheet);
            }
        }
        if(sheet != null){
            _stylesheets.add(sheet);
        }
        
        String[] uris = _nsh.getStylesheetURIs(_doc);
        if(uris != null) {
            for(int i=0; i<uris.length; i++) {
                java.net.URL baseUrl = _context.getBaseURL();
                try {
                    uri = new java.net.URL(baseUrl,uris[i]).toString();
                    sheet = _stylesheetFactory.getStylesheet(uri);
                    if(sheet == null) {
                        reader = _userAgent.getReaderForURI(uri);
                        if(reader != null) {
                            sheet = _stylesheetFactory.parse(Stylesheet.AUTHOR, reader);
                            _stylesheetFactory.putStylesheet(uri, sheet);
                        }
                    }
                    if(sheet != null){
                        _stylesheets.add(sheet);
                    }
                }
                catch(java.net.MalformedURLException e) {
                    XRLog.exception("bad URL for associated stylesheet", e);
                }
            }
        }
        
        uri = _context.getBaseURL().toString();
        sheet = _stylesheetFactory.getStylesheet(uri);
        if(sheet == null) {
            String inlineStyle = _nsh.getInlineStyle(_doc);
            if(inlineStyle != null) {
                reader = new java.io.StringReader(inlineStyle);
                sheet = _stylesheetFactory.parse(Stylesheet.AUTHOR, reader);
                _stylesheetFactory.putStylesheet(uri, sheet);
            }
        }
        if(sheet != null){
            _stylesheets.add(sheet);
        }
        
        //here we should also get user stylesheet from userAgent
        //
        
        long el = System.currentTimeMillis() - st;
        XRLog.load("TIME: parse stylesheets  " + el + "ms");
    }


    /**
     * <p>
     *
     * Attempts to match any styles loaded to Elements in the supplied Document,
     * using CSS2 matching guidelines re: selection to prepare internal lookup
     * routines for property lookup methods (e.g. {@link #getProperty(Element,
     * String, boolean)}). This should be called after all stylesheets and
     * styles are loaded, but before any properties are retrieved. </p>
     *
     * @param document  A DOM Document, e.g. XHTML instance to match on.
     */
    private void matchStyles( ) {
        long st = System.currentTimeMillis();
        try {
            
            XRLog.match("No of stylesheets = "+_stylesheets.size());            
            List sortedXRRules = new ArrayList();
            Iterator iter = _stylesheets.iterator();

            _tbStyleMap = new org.xhtmlrenderer.css.newmatch.Matcher(_doc, _attRes, _stylesheets.iterator());
            _tbStyleMap.setStylesheetFactory(_stylesheetFactory);
            
        // now we have a match-map, apply against our entire Document....restyleTree() is recursive
        Element root = _doc.getDocumentElement();
        _styler = new org.xhtmlrenderer.css.style.Styler();
        _styler.setMatcher(_tbStyleMap);
        _styler.restyleTree( root );
        //_styler.setViewportRectangle(_context.getViewport().getBounds());
        }
        catch(RuntimeException re) {
            re.printStackTrace();
            throw re;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
                     long el = System.currentTimeMillis() - st;
                    XRLog.match("TIME: match styles  " + el + "ms");
   }


    /**
     * Returns the background Color assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The background-color Color property
     */
    public Color getBackgroundColor( Element elem ) {
        return _styler.getCalculatedStyle(elem).getBackgroundColor( );
    }


    /**
     * Returns the border Color assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The border-color Color property
     */
    public BorderColor getBorderColor(Element elem) {
        return _styler.getCalculatedStyle(elem).getBorderColor( );
    }


    /**
     * Returns the border width (all sides) assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The Border property (for widths)
     */
    public Border getBorderWidth(Element elem) {
        return _styler.getCalculatedStyle(elem).getBorderWidth( );
    }


    /**
     * Returns the foreground Color assigned to an Element, searching ancestor
     * Elements for an inheritable value if not found on the Element.
     *
     * @param elem  The DOM element to find the property for.
     * @return      The CSS color Color property
     */
    public Color getColor( Element elem ) {
        return _styler.getCalculatedStyle(elem).getColor( );
    }


    /**
     * Returns the foreground Color assigned to an Element, searching ancestor
     * Elements for an inheritable value if requested.
     *
     * @param elem     The DOM element to find the property for.
     * @param inherit  If true and property not found on this element, searches
     *      through element ancestors for property
     * @return         The foreground Color property
     */
    public Color getColor( Element elem, boolean inherit ) {
        return _styler.getCalculatedStyle(elem).getColor( );
    }


    /**
     * Returns the a property assigned to an Element that can be interpreted as
     * a Point with floating-point positioning, and, if not found and inherit is
     * true, searches for an inheritable property by that name assigned to
     * parent and ancestor elements.
     *
     * @param elem     The DOM element to find the property for.
     * @param inherit  If true and property not found on this element, searches
     *      through element ancestors for property
     * @param prop     The property name
     * @return         The named property as a Point
     */
    public Point getFloatPairProperty( Element elem, String prop, boolean inherit ) {
        DerivedProperty xrProp = _styler.getCalculatedStyle(elem).propertyByName( prop );

        if ( xrProp.computedValue().isValueList() ) {
            CSSValueList vl = (CSSValueList)xrProp.computedValue().cssValue();

            Point pt = new Point();

            pt.setLocation(
                    ( (CSSPrimitiveValue)vl.item( 0 ) ).getFloatValue( CSSPrimitiveValue.CSS_PERCENTAGE ),
                    ( (CSSPrimitiveValue)vl.item( 1 ) ).getFloatValue( CSSPrimitiveValue.CSS_PERCENTAGE )
                     );

            return pt;
        } else {
            XRLog.layout( "Property : " + xrProp + " is not a value list " + xrProp.computedValue().cssValue().getClass().getName() );
        }
        return null;
    }


    /**
     * Returns the value of a property matched to an element cast as a float,
     * searching ancestor Elements for an inheritable value if not found on the
     * Element.
     *
     * @param elem  The DOM Element to find the property for
     * @param prop  The property name
     * @return      The named property as a float
     */
    public float getFloatProperty( Element elem, String prop ) {
        return getFloatProperty( (Node)elem, prop, 0F, false );
    }


    /**
     * Returns the value of a property matched to an element cast as a float,
     * inheriting the value from the nearest ancestor if requested
     *
     * @param elem     The DOM Element to find the property for
     * @param prop     The property name
     * @param inherit  If true, searches ancestor Elements for the property if
     *      not defined on this Element.
     * @return         The named property as a float
     */
    public float getFloatProperty( Element elem, String prop, boolean inherit ) {
        return getFloatProperty( (Node)elem, prop, 0F, inherit );
    }


    /**
     * Returns the value of a property matched to an element cast as a float,
     * using the parent float value to determine relative values if requested
     * (e.g. if the property is a percentage), and inheriting the value if
     * necessary.
     *
     * @param elem          The DOM Element to find the property for
     * @param prop          The property name
     * @param parent_value  The Element's parent value for the same property
     * @return              The named property as a float
     */
    public float getFloatProperty( Element elem, String prop, float parent_value ) {
        return getFloatProperty( (Node)elem, prop, parent_value, false );
    }


    /**
     * Returns the value of a property matched to an element cast as a float,
     * using the parent float value to determine relative values if requested
     * (e.g. if the property is a percentage), inheriting the value if
     * requested.
     *
     * @param elem          The DOM Element to find the property for
     * @param prop          The property name
     * @param parent_value  The Element's parent value for the same property
     * @param inherit       If true, inherits the value from the Element's
     *      parent
     * @return              The named property as a float
     */
    public float getFloatProperty( Element elem, String prop, float parent_value, boolean inherit ) {
        return getFloatProperty( (Node)elem, prop, parent_value, inherit );
    }


    /**
     * Same as <code>getFloatProperty(Element, String, float, boolean)</code>,
     * but for Node elements
     *
     * @param node          The DOM Node to find the property for
     * @param prop          The property name
     * @param parent_value  The Node's parent value for the same property
     * @param inherit       If true, inherits the value from the Node's parent
     * @return              The named property as a float
     */
    public float getFloatProperty( Node node, String prop, float parent_value, boolean inherit ) {
        return _styler.getCalculatedStyle((Element) node).propertyByName( prop ).computedValue().asFloat();
    }


    /**
     * Returns the margin width (all sides) assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The margin property as a Border (for widths)
     */
    public Border getMarginWidth(Element elem) {
        return _styler.getCalculatedStyle(elem).getMarginWidth( );
    }


    /**
     * Returns the padding width (all sides) assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The padding property as a Border (for widths)
     */
    public Border getPaddingWidth(Element elem) {
        return _styler.getCalculatedStyle(elem).getPaddingWidth( );
    }


    /**
     * Returns the value of a property as a W3C CSSValue instance, inheriting
     * from the parent element if requested.
     *
     * @param elem     The DOM Element to find the property for
     * @param prop     The property name
     * @param inherit  If true, inherits the value from the Element's parent
     * @return         The property value as CSSValue
     */
    public CSSValue getProperty( Element elem, String prop, boolean inherit ) {
        return _styler.getCalculatedStyle(elem).propertyByName( prop ).computedValue().cssValue();
    }


    /**
     * Returns the value of a property as a String array, for example, for
     * font-family declarations, inheriting the property if nece.
     *
     * @param elem  The DOM Element to find the property for
     * @param prop  The property name
     * @return      The property value as String array
     */
    public String[] getStringArrayProperty( Element elem, String prop ) {
        return _styler.getCalculatedStyle(elem).propertyByName( prop ).computedValue().asStringArray();
    }


    /**
     * Returns the value of a property as a String, inheriting the property if
     * necessary.
     *
     * @param elem  The DOM Element to find the property for
     * @param prop  The property name
     * @return      The property value as String
     */
    public String getStringProperty( Element elem, String prop ) {
        return _getStringProperty( (Node)elem, prop, false );
    }


    /**
     * Returns the value of a property as a String, inheriting the value from
     * the Element's parent if requested.
     *
     * @param elem     The DOM Element to find the property for
     * @param prop     The property name
     * @param inherit  If true, inherits the property from the Element's parent
     *      if necessary.
     * @return         The property value as String
     */
    public String getStringProperty( Element elem, String prop, boolean inherit ) {
        return _getStringProperty( (Node)elem, prop, inherit );
    }


    /**
     * Returns the value of a property as a String from a DOM Node instance,
     * searching ancestor Elements for an inheritable value if not found on the
     * Element.
     *
     * @param node  The DOM Node to find the property for
     * @param prop  The property name
     * @return      The property value as String
     */
    public String getStringProperty( Node node, String prop ) {
        return _getStringProperty( node, prop, false );
    }


    /**
     * Returns the value of a property as a String, inheriting the value from
     * the Node's parent if requested.
     *
     * @param node     The DOM Node to find the property for.
     * @param prop     The property name
     * @param inherit  If true, inherits the property from the Element's parent
     *      if necessary
     * @return         The property value as String
     */
    public String getStringProperty( Node node, String prop, boolean inherit ) {
        return _getStringProperty( node, prop, false );
    }


    /**
     * Returns the value of a property as a String, inheriting the value from
     * the Node's parent if requested.
     *
     * @param node     The DOM Node to find the property for.
     * @param prop     The property name
     * @param inherit  If true, inherits the property from the Element's parent
     *      if necessary
     * @return         The property value as String
     */
    //Tobe: this is actually called with a non-element!
    private String _getStringProperty( Node node, String prop, boolean inherit ) {
        Element elem = nearestElementAncestor(node);

        if ( elem == null ) {
            return null;
        }
        return _styler.getCalculatedStyle(elem).propertyByName( prop ).computedValue().asString();
    }
    
    private org.w3c.dom.Element nearestElementAncestor(Node node) {
        while(node != null && node.getNodeType() != Node.ELEMENT_NODE){
            node = node.getParentNode();
        }
        
        return (Element) node;
    }
    
    public java.util.Map getDerivedPropertiesMap(Element e) {
        org.xhtmlrenderer.css.style.CalculatedStyle cs = _styler.getCalculatedStyle(e);
        java.util.LinkedHashMap props = new java.util.LinkedHashMap();
        for(java.util.Iterator i = cs.getAvailablePropertyNames().iterator(); i.hasNext();) {
            String propName = (String) i.next();
            props.put(propName, cs.propertyByName(propName).computedValue().cssValue());
        }
        return props;
    }
    
    public CalculatedStyle getFirstLetterStyle(Element e) {
        return null;//not supported yet
    }
    
    public CascadedStyle getPseudoElementStyle(Element e, String pseudoElement) {
        return _tbStyleMap.getPECascadedStyle(e, pseudoElement);
    }
    
    public CalculatedStyle getStyle(Element e) {
        return _styler.getCalculatedStyle(e);
    }
    
    public boolean wasHoverRestyled(Element e) {
        if(_tbStyleMap.isHoverStyled(e)) {
            _styler.restyleTree(e);
            return true;
        }
        return false;
    }
    
}
/*
 * :folding=java:collapseFolds=1:noTabs=true:tabSize=4:indentSize=4:
 */

