/*
 *
 * TBStyleReference.java
 * Copyright (c) 2004 Patrick Wright, Torbjörn Gannholm
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

package net.homelinux.tobe.xhtmlrenderer.bridge;

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
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import org.joshy.html.Border;
import org.joshy.html.Context;
import org.joshy.html.css.JStyle;
import org.joshy.html.css.StyleReference;

import net.homelinux.tobe.xhtmlrenderer.stylerImpl.DerivedProperty;

import com.pdoubleya.xhtmlrenderer.css.XRProperty;
import com.pdoubleya.xhtmlrenderer.css.XRStyleRule;
import com.pdoubleya.xhtmlrenderer.css.impl.XRElementImpl;
import com.pdoubleya.xhtmlrenderer.css.impl.XRStyleRuleImpl;
import com.pdoubleya.xhtmlrenderer.css.impl.XRStyleSheetImpl;
import com.pdoubleya.xhtmlrenderer.css.value.BorderColor;
import com.pdoubleya.xhtmlrenderer.util.LoggerUtil;

import com.steadystate.css.parser.CSSOMParser;

import org.apache.xpath.XPathAPI;

/**
 * <p>
 *
 * Implementation of {@link org.joshy.html.css.StyleReference} which uses the
 * output of a SAC CSS parser to parse stylesheets, uses the {@link
 * net.homelinux.tobe.css.StyleMap} and related classes as a CSS-DOM matcher,
 * and a {@link org.joshy.html.Context} instance for property resolution where
 * neeeded. Idiomatic use is </p>
 * <ul>
 *   <li> to {@link XRStyleReference#XRStyleReference(Context)} with a Context
 *   </li>
 *   <li> parse one or more stylesheets using the {@link #parse(Reader, int)}
 *   commands</li>
 *   <li> match the loaded sheets against DOM elements using {@link
 *   #matchStyles(Document)}</li>
 *   <li> lookup properties with the {@link #getProperty(Element, String,
 *   boolean)} methods--there are several <code>get</code> methods you can use.
 *   </li>
 * </ul>
 * <p>
 *
 * The call to {@link #matchStyles(Document)} is optional, as matching will take
 * place anyway when properties are retrieved. However, calling it on your own
 * gives you more control over when that takes place.</p> <p>
 *
 * This class is NOT designed for multi-threaded use.</p>
 *
 * @author   Patrick Wright
 */
public class TBStyleReference implements StyleReference {
    /** Internal Logger used for debug output. */
    private final static Logger sDbgLogger = LoggerUtil.getDebugLogger( TBStyleReference.class );

    /** The Context this StyleReference operates in; used for property resolution. */
    private Context _context;

    /** Count of number of sheets loaded so far. */
    private int _sheetCnt;

    /**
     * Whether a match has been completed since a call to a parse method. Used
     * to flag that match is required before retrieving properties.
     */
    private boolean _matchedSinceLastParse;
    
    /** Whether elements in the current document have been checked for style attributes */
    private boolean _elementStyleAttributesPulled;
    
    /** Map from Element to XRStyleRules created from style attribute on it */
    private Map _elementXRStyleMap;

    /**
     * ASK: holdover from Josh's processing code...apparently used as a trap to
     * make sure the same <code><style></code> elements within a document were
     * not read more than once.
     */
    private List _inlineStyleElements;

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
    private Map _ruleSetMap;

    /**
     * Instantiates a new XRStyleReference for a given Context.
     *
     * @param context  Context instance used for property resolution when
     *      necessary.
     */
    public TBStyleReference(Context context) {
        this();
        _context = context;
    }

    /** Default constructor for initializing members. */
    private TBStyleReference() {
        _inlineStyleElements = new ArrayList();
        _stylesheets = new LinkedList();
        _ruleSetMap = new HashMap();
        _elementXRStyleMap = new HashMap();
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
        if ( !_matchedSinceLastParse ) {
            matchStyles( elem.getOwnerDocument() );
        }
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
        if ( !_matchedSinceLastParse ) {
            matchStyles( elem.getOwnerDocument() );
        }
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
        if ( !_matchedSinceLastParse ) {
            matchStyles( node.getOwnerDocument() );
        }
        return _styler.getCalculatedStyle((Element) node).hasProperty( prop );
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
    public void matchStyles( Document document ) {
        try {
        if ( _tbStyleMap == null ) {
            if ( !_elementStyleAttributesPulled ) {
                try {
                    NodeList nl = XPathAPI.selectNodeList(document.getDocumentElement(), "//*[@style!='']");
                    // HACK: better on first parse and avoid this second sweep (PWW 24-08-04)
                    for ( int i=0, len=nl.getLength(); i < len; i++ ) {
                        parseElementStyling((Element)nl.item(i));
                    }
                    _elementStyleAttributesPulled = true;
                } catch (Exception ex) {
                    System.err.println("Couldn't pull element style attribute.");
                    ex.printStackTrace();
                }
            }
            
            // we are going to pass a list of pre-sorted XRStyleRules to the mapping routines
            // The sort is on origin, specificity, and sequence...in principle, this means
            // that rules appearing later in the sort always override rules appearing higher
            List sortedXRRules = new ArrayList();
            Iterator iter = _stylesheets.iterator();

            _tbStyleMap = new net.homelinux.tobe.xhtmlrenderer.matcherImpl.Matcher(document, new StaticHtmlAttributeResolver(), _stylesheets.iterator());
            _tbStyleMap.setStylesheetFactory(new StylesheetFactory());
            
        }

        // now we have a match-map, apply against our entire Document....restyleTree() is recursive
        Element root = document.getDocumentElement();
        _styler = new net.homelinux.tobe.xhtmlrenderer.stylerImpl.Styler();
        _styler.setMatcher(_tbStyleMap);
        _styler.restyleTree( root );
        _styler.setViewportRectangle(_context.getViewport().getBounds());
        _matchedSinceLastParse = true;
        }
        catch(RuntimeException re) {
            re.printStackTrace();
            throw re;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Parses the CSS style sheet enclosed by the Reader for later property
     * resoution. After parse completes, styles from the sheet are available for
     * any of the get methods for property lookup. Styles are added to any
     * styles already loaded.
     *
     * @param reader           A Reader from which to read the style
     *      information.
     * @param origin           The origin of the enclosed style information--an
     *      int constant from XRStyleSheet, e.g. {@link
     *      com.pdoubleya.xhtmlrenderer.css.XRStyleSheet#AUTHOR}. Used to
     *      determine precedence of rules derived from the parse sheet.
     * @exception IOException  On errors reading the Reader.
     */
    public void parse( Reader reader, int origin )
        throws IOException {

        // TODO: need sheet sequence and origin
        loadSheet( reader, origin );
        _matchedSinceLastParse = false;
    }

    /**
     * Same as {@link #parse(Reader, int)} for {@link
     * com.pdoubleya.xhtmlrenderer.css.XRStyleSheet#USER_AGENT} stylesheet.
     *
     * @param reader           See {@link #parse(Reader, int)}
     * @exception IOException  See {@link #parse(Reader, int)}
     */
    public void parse( Reader reader )
        throws IOException {
        parse( reader, Stylesheet.USER_AGENT );
    }

    /**
     * Same as {@link #parse(Reader, int)} for a String datasource.
     *
     * @param source           A String containing CSS style rules
     * @param origin           See {@link #parse(Reader, int)}
     * @exception IOException  {@link #parse(Reader, int)}
     */
    // TODO: add origin to these parse calls everywhere
    public void parse( String source, int origin )
        throws IOException {

        parse( new StringReader( source ), origin );
        _matchedSinceLastParse = false;
    }


    /**
     * Same as {@link #parse(Reader, int)} for {@link
     * com.pdoubleya.xhtmlrenderer.css.XRStyleSheet#USER_AGENT} stylesheet and
     * String that contains the styles.
     *
     * @param source           See {@link #parse(Reader, int)}
     * @exception IOException  {@link #parse(Reader, int)}
     */
    public void parse( String source )
        throws IOException {
        parse( source, Stylesheet.USER_AGENT );
    }


    /**
     * Parses the CSS style information from a <?xml-stylesheet?> PI
     *  and loads these rules into the associated RuleBank.
     *
     * @param root             Root of the document for which to search for link tags.
     * @exception IOException  Throws
     */
    public void parseDeclaredStylesheets( Element root )
    throws IOException {
        try {
            NodeList nl = XPathAPI.selectNodeList(root, "//processing-instruction('xml-stylesheet')");
            for ( int i=0, len=nl.getLength(); i < len; i++ ) {
                Node piNode = nl.item(i);
                String pi = piNode.getNodeValue();
                String s = pi.substring(pi.indexOf("type=")+5);
                String type = s.substring(1, s.indexOf(s.charAt(0),1));
                if(type.equals("text/css")) {
                    s = pi.substring(pi.indexOf("href=")+5);
                    String href = s.substring(1, s.indexOf(s.charAt(0),1));
                    parseStylesheet(href);
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();   
        }
    }

    /**
     * Parses the CSS style information from a "<link>" Elements (for example in
     * XHTML), and loads these rules into the associated RuleBank.
     *
     * @param root             Root of the document for which to search for link tags.
     * @exception IOException  Throws
     */
    public void parseLinkedStyles( Element root )
    throws IOException {
        try {
            NodeList nl = XPathAPI.selectNodeList(root, "//link[@type='text/css']/@href");
            for ( int i=0, len=nl.getLength(); i < len; i++ ) {
                Node hrefNode = nl.item(i);
                String href = hrefNode.getNodeValue();
                parseStylesheet(href);
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();   
        }
    }

    /**
     * Parses the CSS style information from a URL
     * and loads these rules into the associated RuleBank.
     *
     * @param root             Root of the document for which to search for link tags.
     * @exception IOException  Throws
     */
    public void parseStylesheet( String href )
    throws IOException {
        try {
                // HACK: need clean way to check if this is local reference...local references will need to be resolved to server reference, won't they?
                Reader reader = null;
                try {
                    URL url = null;
                    if ( !href.startsWith("http://") && !href.startsWith("file://"))
                        url = new URL(_context.getBaseURL(),href);

                    url = new URL(href);
                    InputStream is = url.openStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    reader = new InputStreamReader(bis);
                } catch ( java.net.MalformedURLException ex ) {
                    System.err.println("Stylesheet link " + href + " doesn't appear to be a url.");   
                }
                
                if ( reader == null ) {
                    File f = new File(href);
                    if ( f.exists()) {
                        reader = new BufferedReader(new FileReader(f));
                        System.out.println("Loading CSS " + href + " as a file.");
                    } else {
                        System.err.println("Can't figure out how to load stylesheet '" + href + "'.");   
                    }
                } 
                if ( reader != null ) {
                    parse(reader, Stylesheet.AUTHOR);
                    reader.close();
                }
        } catch ( Exception ex ) {
            ex.printStackTrace();   
        }
    }

    /**
     * Parses the CSS style information from a "<style>" Element (for example in
     * XHTML), and loads these styles for later lookup, and processes child
     * nodes as well. Inline stylesheets are always considered {@link
     * com.pdoubleya.xhtmlrenderer.css.XRStyleSheet#AUTHOR} stylesheets for the
     * purposes of cascading.
     *
     * @param elem             The Element from which to pull a style attribute.
     * @exception IOException  If there was an error reading the styling
     *      information from the element.
     */
    // CLEAN: the throws is a holdover because we are linking the mthod calls, not needed (PWW 13/08/04)
    public void parseInlineStyles( Element elem )
        throws IOException {

        if ( elem.getNodeName().equals( "style" ) ) {

            // check if we've already imported it
            // ASK: (check is from Josh's code, not sure why this is necessary)? (PWW 13/08/04)

            if ( !_inlineStyleElements.contains( elem ) ) {

                // import the style
                parse( org.joshy.x.text( elem ), Stylesheet.AUTHOR );

                // TODO: we actually need to trap the rules that come out of this parse
                // and force the association to the Element, allowing no other match.
                // In this case we don't want parse() to call addRule() (PWW 15/08/04)
                _inlineStyleElements.add( elem );
            }
        }

        // do all of the children
        NodeList nl = elem.getChildNodes();
        for ( int i = 0; i < nl.getLength(); i++ ) {
            Node n = nl.item( i );
            if ( n.getNodeType() == Node.ELEMENT_NODE ) {
                parseInlineStyles( (Element)n );
            }
        }
        _matchedSinceLastParse = false;
    }


    /**
     * Parses the CSS style information from the inline "style" attribute on the
     * DOM Element, and loads these styles for later lookup, automatically
     * associating those styles as matched to the Element.
     *
     * @param elem             The Element from which to pull a style attribute.
     * @exception IOException  Throws
     */
    public void parseElementStyling( Element elem )
        throws IOException {

    /* should not be done here!
     _matchedSinceLastParse = false;

        // Pull attribute
        Node styleNode = elem.getAttributes().getNamedItem("style");
        if ( styleNode == null ) {
            System.err.println("Element requested to parse style attribute but it had none.");
        } else {
            String styleStr = "* { " + styleNode.getNodeValue() + " }";
            

            // can't use parse routines because we will lose track
            // that style belongs only to this element (as if it had
            // an id attribute)
            // HACK: too heavyweight for general use moving fwd (PWW 24-08-04)
            XRStyleSheet sheet = null;
            try {
                CSSOMParser parser = new CSSOMParser();
                StringReader reader = new StringReader(styleStr);
                InputSource is = new InputSource( reader );
                CSSStyleSheet style = parser.parseStyleSheet( is );
                reader.close();
                
                // HACK: this is overkill, but the constructors of the
                // XRStyleRuleImpl class are not set up for non-sheet
                // rule loading.
                sheet = XRStyleSheetImpl.newAuthorStyleSheet( style, 0 );
                XRStyleRule rule = (XRStyleRule)sheet.styleRules().next();
                _elementXRStyleMap.put(elem, rule);
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
        }*/
    }


    /**
     * Returns the background Color assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The background-color Color property
     */
    public Color getBackgroundColor( Element elem ) {
        if ( !_matchedSinceLastParse ) {
            matchStyles( elem.getOwnerDocument() );
        }
        return _styler.getCalculatedStyle(elem).getBackgroundColor( );
    }


    /**
     * Returns the border Color assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The border-color Color property
     */
    public BorderColor getBorderColor( Element elem ) {
        if ( !_matchedSinceLastParse ) {
            matchStyles( elem.getOwnerDocument() );
        }
        return _styler.getCalculatedStyle(elem).getBorderColor( );
    }


    /**
     * Returns the border width (all sides) assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The Border property (for widths)
     */
    public Border getBorderWidth( Element elem ) {
        if ( !_matchedSinceLastParse ) {
            matchStyles( elem.getOwnerDocument() );
        }
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
        if ( !_matchedSinceLastParse ) {
            matchStyles( elem.getOwnerDocument() );
        }
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
        // ***
        if ( !_matchedSinceLastParse ) {
            matchStyles( elem.getOwnerDocument() );
        }
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
        // ***
        if ( !_matchedSinceLastParse ) {
            matchStyles( elem.getOwnerDocument() );
        }
        
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
        if ( !_matchedSinceLastParse ) {
            matchStyles( node.getOwnerDocument() );
        }
        
        return _styler.getCalculatedStyle((Element) node).propertyByName( prop ).computedValue().asFloat();
    }


    /**
     * Returns the margin width (all sides) assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The margin property as a Border (for widths)
     */
    public Border getMarginWidth( Element elem ) {
        if ( !_matchedSinceLastParse ) {
            matchStyles( elem.getOwnerDocument() );
        }
        return _styler.getCalculatedStyle(elem).getMarginWidth( );
    }


    /**
     * Returns the padding width (all sides) assigned to an Element
     *
     * @param elem  The DOM element to find the property for.
     * @return      The padding property as a Border (for widths)
     */
    public Border getPaddingWidth( Element elem ) {
        if ( !_matchedSinceLastParse ) {
            matchStyles( elem.getOwnerDocument() );
        }
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
        if ( !_matchedSinceLastParse ) {
            matchStyles( elem.getOwnerDocument() );
        }
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
        if ( !_matchedSinceLastParse ) {
            matchStyles( elem.getOwnerDocument() );
        }
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
        if ( !_matchedSinceLastParse ) {
            matchStyles( node.getOwnerDocument() );
        }
        
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



    /**
     * Parses the stylesheet into rules and loads for internal lookups.
     *
     * @param cssReader  PARAM
     * @param origin     PARAM
     * @return           Returns
     */
    private void loadSheet( Reader cssReader, int origin ) {
        try {
            CSSOMParser parser = new CSSOMParser();
            InputSource is = new InputSource( cssReader );
            CSSStyleSheet style = parser.parseStyleSheet( is );

            _sheetCnt++;
        _stylesheets.add(new Stylesheet(style, origin));
            
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        
        
    }


}
/*
 * :folding=java:collapseFolds=1:noTabs=true:tabSize=4:indentSize=4:
 */

