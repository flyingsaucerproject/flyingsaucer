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

package net.homelinux.tobe.renderer.css;

import java.awt.Color;
import java.awt.Point;
import java.io.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.*;

import org.w3c.css.sac.InputSource;
//import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSPrimitiveValue;
//import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import org.joshy.html.Border;
import org.joshy.html.Context;
//import org.joshy.html.css.JStyle;
import org.joshy.html.css.StyleReference;

import net.homelinux.tobe.xhtmlrenderer.stylerImpl.DerivedProperty;

import net.homelinux.tobe.xhtmlrenderer.bridge.StaticHtmlAttributeResolver;
import net.homelinux.tobe.xhtmlrenderer.Stylesheet;

import net.homelinux.tobe.renderer.UserAgentCallback;
import net.homelinux.tobe.renderer.Document;

import com.pdoubleya.xhtmlrenderer.css.value.BorderColor;
import com.pdoubleya.xhtmlrenderer.util.LoggerUtil;

/** Does not really implement StyleReference, but anyway */
 public class TBStyleReference implements StyleReference {
    /** Internal Logger used for debug output. */
    private final static Logger sDbgLogger = LoggerUtil.getDebugLogger( TBStyleReference.class );

    /** The Context this StyleReference operates in; used for property resolution. */
    private Context _context;
    
    private UserAgentCallback _userAgent;
    
    private Document _doc;
    
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
    private net.homelinux.tobe.xhtmlrenderer.Matcher _tbStyleMap;
    
    private net.homelinux.tobe.xhtmlrenderer.Styler _styler;

    
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
    public TBStyleReference(Context context, UserAgentCallback userAgent, Document doc) {
        this(userAgent);
        setDocumentContext(context, doc);
    }

    /** Default constructor for initializing members. */
    public TBStyleReference(UserAgentCallback userAgent) {
        _userAgent = userAgent;
        //_inlineStyleElements = new ArrayList();
        //_ruleSetMap = new HashMap();
        //_elementXRStyleMap = new HashMap();
        _stylesheetFactory = new StylesheetFactory();
    }
    
    public void setDocumentContext(Context context, Document doc) {
        _context = context;
        _doc = doc;
        
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
        
        java.net.URI uri = _doc.getNamespace();
        Stylesheet sheet = (Stylesheet) _stylesheetFactory.getStylesheet(uri);
        if(sheet == null) {
            reader = _doc.getDefaultStylesheet();
            if(reader != null) {
                sheet = _stylesheetFactory.parse(Stylesheet.USER_AGENT, reader);
                _stylesheetFactory.putStylesheet(uri, sheet);
            }
        }
        if(sheet != null){
            _stylesheets.add(sheet);
        }
        
        java.net.URI[] uris = _doc.getStylesheetURIs();
        if(uris != null) {
            for(int i=0; i<uris.length; i++) {
                java.net.URI baseUri = _doc.getURI();
                uri = baseUri.resolve(uris[i]);
                sheet = _stylesheetFactory.getStylesheet(uri);
                if(sheet == null) {
                    java.io.InputStream is = _userAgent.getInputStreamForURI(uri);
                    if(is != null) {
                        reader = new InputStreamReader(is);
                        sheet = _stylesheetFactory.parse(Stylesheet.AUTHOR, reader);
                        _stylesheetFactory.putStylesheet(uri, sheet);
                    }
                }
                if(sheet != null){
                    _stylesheets.add(sheet);
                }
            }
        }
        
        String[] styles = _doc.getInlineStyles();
        if(styles != null) {
            for(int i=0; i<styles.length; i++) {
                reader = new java.io.StringReader(styles[i]);
                _stylesheets.add(_stylesheetFactory.parse(Stylesheet.AUTHOR, reader));
            }
        }
        
        //here we should also get user stylesheet from userAgent
        //
                    long el = System.currentTimeMillis() - st;
                    System.out.println("TIME: parse stylesheets  " + el + "ms");
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
            
            org.w3c.dom.Document document = _doc.getDomDocument();
            
            List sortedXRRules = new ArrayList();
            Iterator iter = _stylesheets.iterator();

            _tbStyleMap = new net.homelinux.tobe.xhtmlrenderer.matcherImpl.Matcher(document, new StaticHtmlAttributeResolver(), _stylesheets.iterator());
            _tbStyleMap.setStylesheetFactory(_stylesheetFactory);
            
        // now we have a match-map, apply against our entire Document....restyleTree() is recursive
        Element root = document.getDocumentElement();
        _styler = new net.homelinux.tobe.xhtmlrenderer.stylerImpl.Styler();
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
                    System.out.println("TIME: match styles  " + el + "ms");
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
    public BorderColor getBorderColor( Element elem ) {
        return _styler.getCalculatedStyle(elem).getBorderColor( );
    }


    /**
     * Returns the border width (all sides) assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The Border property (for widths)
     */
    public Border getBorderWidth( Element elem ) {
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
            sDbgLogger.warning( "Property : " + xrProp + " is not a value list " + xrProp.computedValue().cssValue().getClass().getName() );
        }
        return null;// xrElement.derivedStyle().getColor();
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
    public Border getMarginWidth( Element elem ) {
        return _styler.getCalculatedStyle(elem).getMarginWidth( );
    }


    /**
     * Returns the padding width (all sides) assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The padding property as a Border (for widths)
     */
    public Border getPaddingWidth( Element elem ) {
        sDbgLogger.finest( "getPaddingWidth() on Element: " + elem.hashCode() );
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
    
    //Below are irrelevant

    public void matchStyles(org.w3c.dom.Document document) {
    }    

    public void parse(String source) throws IOException {
    }
    
    public void parse(Reader reader) throws IOException {
    }
    
    public void parse(Reader reader, int origin) throws IOException {
    }
    
    public void parse(String source, int origin) throws IOException {
    }
    
    public void parseDeclaredStylesheets(Element root) throws IOException {
    }
    
    public void parseElementStyling(Element elem) throws IOException {
    }
    
    public void parseInlineStyles(Element elem) throws IOException {
    }
    
    public void parseLinkedStyles(Element elem) throws IOException {
    }
    
}
/*
 * :folding=java:collapseFolds=1:noTabs=true:tabSize=4:indentSize=4:
 */

