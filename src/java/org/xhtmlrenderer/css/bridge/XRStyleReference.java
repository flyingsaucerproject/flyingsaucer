/*
 * {{{ header & license
 * XRStyleReference.java
 * Copyright (c) 2004 Patrick Wright
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
 * }}}
 */
package org.xhtmlrenderer.css.bridge;

import java.awt.Color;
import java.awt.Point;
import java.io.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.*;

import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LangCondition;
import org.w3c.css.sac.PositionalCondition;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import com.steadystate.css.parser.CSSOMParser;

import org.apache.xpath.XPathAPI;

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.css.StaticHtmlAttributeResolver;
import org.xhtmlrenderer.css.StyleReference;

import org.xhtmlrenderer.css.XRElement;
import org.xhtmlrenderer.css.XRProperty;
import org.xhtmlrenderer.css.XRStyleRule;
import org.xhtmlrenderer.css.XRStyleSheet;
import org.xhtmlrenderer.css.impl.XRElementImpl;
import org.xhtmlrenderer.css.impl.XRStyleRuleImpl;
import org.xhtmlrenderer.css.impl.XRStyleSheetImpl;

import org.xhtmlrenderer.css.match.AttributeResolver;
import org.xhtmlrenderer.css.match.Ruleset;
import org.xhtmlrenderer.css.match.StyleMap;
import org.xhtmlrenderer.css.value.BorderColor;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.XRLog;

import org.xhtmlrenderer.DefaultCSSMarker;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.u;


/**
 * <p>
 *
 * Implementation of {@link org.xhtmlrenderer.css.StyleReference} which uses the
 * output of a SAC CSS parser to parse stylesheets, uses the {@link
 * org.xhtmlrenderer.css.match.StyleMap} and related classes as a CSS-DOM matcher,
 * and a {@link org.xhtmlrenderer.layout.Context} instance for property
 * resolution where neeeded. Idiomatic use is </p>
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
public class XRStyleReference implements StyleReference {
    /** The Context this StyleReference operates in; used for property resolution. */
    private Context _context;

    /** Count of number of sheets loaded so far. */
    private int _sheetCnt;

    /**
     * Whether a match has been completed since a call to a parse method. Used
     * to flag that match is required before retrieving properties.
     */
    private boolean _matchedSinceLastParse;

    /**
     * Whether elements in the current document have been checked for style
     * attributes
     */
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
    private StyleMap _tbStyleMap;

    /** One-one map from DOM Elements to XRElements. */
    private Map _nodeXRElementMap;

    /** seems to need a list of XRStyleRules.... */
    private List _xrStyleRuleList;

    /**
     * Map from XRStyleRules to the Rulesets that contain them. Rulesets are
     * passed to Tobe's matching routine, hence...
     */
    private Map _ruleSetMap;

    /**
     * Instantiates a new XRStyleReference for a given Context.
     *
     * @param context  Context instance used for property resolution when
     *      necessary.
     */
    public XRStyleReference( Context context ) {
        this();
        _context = context;
    }

    /** Default constructor for initializing members. */
    private XRStyleReference() {
        _inlineStyleElements = new ArrayList();
        _xrStyleRuleList = new LinkedList();
        _ruleSetMap = new HashMap();
        _nodeXRElementMap = new HashMap();
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
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( node );
        return xrElement.derivedStyle().hasProperty( prop );
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
    private void matchStyles( Document document ) {
        if ( _tbStyleMap == null ) {
            if ( !_elementStyleAttributesPulled ) {
                try {
                    NodeList nl = XPathAPI.selectNodeList( document.getDocumentElement(), "//*[@style!='']" );
                    // HACK: better on first parse and avoid this second sweep (PWW 24-08-04)
                    for ( int i = 0, len = nl.getLength(); i < len; i++ ) {
                        parseElementStyling( (Element)nl.item( i ) );
                    }
                    _elementStyleAttributesPulled = true;
                } catch ( Exception ex ) {
                    System.err.println( "Couldn't pull element style attribute." );
                    ex.printStackTrace();
                }
            }

            // we are going to pass a list of pre-sorted XRStyleRules to the mapping routines
            // The sort is on origin, specificity, and sequence...in principle, this means
            // that rules appearing later in the sort always override rules appearing higher
            List sortedXRRules = new ArrayList();
            Iterator iter = _xrStyleRuleList.iterator();
            while ( iter.hasNext() ) {
                sortedXRRules.add( iter.next() );
            }
            Collections.sort( sortedXRRules, XRStyleRuleImpl.STYLE_RULE_COMPARATOR );

            List sortedRulesets = new ArrayList();
            iter = sortedXRRules.iterator();
            while ( iter.hasNext() ) {

                Ruleset rs = (Ruleset)_ruleSetMap.get( iter.next() );
                sortedRulesets.add( rs );
            }

            _tbStyleMap = StyleMap.createMap( document, sortedRulesets, new StaticHtmlAttributeResolver() );
        }

        // now we have a match-map, apply against our entire Document....applyMatches() is recursive
        Element root = document.getDocumentElement();
        applyMatches( root );
        _matchedSinceLastParse = true;
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
     *      org.xhtmlrenderer.css.XRStyleSheet#AUTHOR}. Used to determine
     *      precedence of rules derived from the parse sheet.
     * @exception IOException  On errors reading the Reader.
     */
    private void parse( Reader reader, int origin )
        throws IOException {

        // TODO: need sheet sequence and origin
        loadSheet( reader, origin );
        _matchedSinceLastParse = false;
    }

    /**
     * Same as {@link #parse(Reader, int)} for {@link
     * org.xhtmlrenderer.css.XRStyleSheet#USER_AGENT} stylesheet.
     *
     * @param reader           See {@link #parse(Reader, int)}
     * @exception IOException  See {@link #parse(Reader, int)}
     */
    private void parse( Reader reader )
        throws IOException {
        parse( reader, XRStyleSheet.USER_AGENT );
    }

    /**
     * Same as {@link #parse(Reader, int)} for a String datasource.
     *
     * @param source           A String containing CSS style rules
     * @param origin           See {@link #parse(Reader, int)}
     * @exception IOException  {@link #parse(Reader, int)}
     */
    // TODO: add origin to these parse calls everywhere
    private void parse( String source, int origin )
        throws IOException {

        parse( new StringReader( source ), origin );
        _matchedSinceLastParse = false;
    }


    /**
     * Same as {@link #parse(Reader, int)} for {@link
     * org.xhtmlrenderer.css.XRStyleSheet#USER_AGENT} stylesheet and String that
     * contains the styles.
     *
     * @param source           See {@link #parse(Reader, int)}
     * @exception IOException  {@link #parse(Reader, int)}
     */
    private void parse( String source )
        throws IOException {
        parse( source, XRStyleSheet.USER_AGENT );
    }


    /**
     * Parses the CSS style information from a <?xml-stylesheet?> PI and loads
     * these rules into the associated RuleBank.
     *
     * @param root             Root of the document for which to search for link
     *      tags.
     * @exception IOException  Throws
     */
    private void parseDeclaredStylesheets( Element root )
        throws IOException {
        try {
            NodeList nl = XPathAPI.selectNodeList( root, "//processing-instruction('xml-stylesheet')" );
            for ( int i = 0, len = nl.getLength(); i < len; i++ ) {
                Node piNode = nl.item( i );
                String pi = piNode.getNodeValue();
                String s = pi.substring( pi.indexOf( "type=" ) + 5 );
                String type = s.substring( 1, s.indexOf( s.charAt( 0 ), 1 ) );
                if ( type.equals( "text/css" ) ) {
                    s = pi.substring( pi.indexOf( "href=" ) + 5 );
                    String href = s.substring( 1, s.indexOf( s.charAt( 0 ), 1 ) );
                    parseStylesheet( href );
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    /**
     * Parses the CSS style information from a "
     * <link> " Elements (for example in XHTML), and loads these rules into the
     * associated RuleBank.
     *
     * @param root             Root of the document for which to search for link
     *      tags.
     * @exception IOException  Throws
     */
    private void parseLinkedStyles( Element root )
        throws IOException {
        try {
            NodeList nl = XPathAPI.selectNodeList( root, "//link[@type='text/css']/@href" );
            for ( int i = 0, len = nl.getLength(); i < len; i++ ) {
                Node hrefNode = nl.item( i );
                String href = hrefNode.getNodeValue();
                parseStylesheet( href );
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    /**
     * Parses the CSS style information from a URL and loads these rules into
     * the associated RuleBank.
     *
     * @param href             PARAM
     * @exception IOException  Throws
     */
    private void parseStylesheet( String href )
        throws IOException {
        try {
            // HACK: need clean way to check if this is local reference...local references will need to be resolved to server reference, won't they?
            Reader reader = null;
            try {
                URL url = null;
                if ( !href.startsWith( "http://" ) && !href.startsWith( "file://" ) ) {
                    url = new URL( _context.getBaseURL(), href );
                } else {
                    url = new URL( href );
                }

                InputStream is = url.openStream();
                BufferedInputStream bis = new BufferedInputStream( is );
                reader = new InputStreamReader( bis );
            } catch ( java.net.MalformedURLException ex ) {
                System.err.println( "Stylesheet link " + href + " doesn't appear to be a url." );
            }

            if ( reader == null ) {
                File f = new File( href );
                if ( f.exists() ) {
                    reader = new BufferedReader( new FileReader( f ) );
                    System.out.println( "Loading CSS " + href + " as a file." );
                } else {
                    System.err.println( "Can't figure out how to load stylesheet '" + href + "' with base URL " + _context.getBaseURL() );
                }
            }
            if ( reader != null ) {
                parse( reader, XRStyleSheet.AUTHOR );
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
     * org.xhtmlrenderer.css.XRStyleSheet#AUTHOR} stylesheets for the purposes
     * of cascading.
     *
     * @param elem             The Element from which to pull a style attribute.
     * @exception IOException  If there was an error reading the styling
     *      information from the element.
     */
    // CLEAN: the throws is a holdover because we are linking the mthod calls, not needed (PWW 13/08/04)
    private void parseInlineStyles( Element elem )
        throws IOException {

        if ( elem.getNodeName().equals( "style" ) ) {

            // check if we've already imported it
            // ASK: (check is from Josh's code, not sure why this is necessary)? (PWW 13/08/04)

            if ( !_inlineStyleElements.contains( elem ) ) {

                // import the style
                parse( org.xhtmlrenderer.util.x.text( elem ), XRStyleSheet.AUTHOR );

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
    private void parseElementStyling( Element elem )
        throws IOException {

        _matchedSinceLastParse = false;

        // Pull attribute
        Node styleNode = elem.getAttributes().getNamedItem( "style" );
        if ( styleNode == null ) {
            System.err.println( "Element requested to parse style attribute but it had none." );
        } else {
            String styleStr = "* { " + styleNode.getNodeValue() + " }";

            // can't use parse routines because we will lose track
            // that style belongs only to this element (as if it had
            // an id attribute)
            // HACK: too heavyweight for general use moving fwd (PWW 24-08-04)
            XRStyleSheet sheet = null;
            try {
                CSSOMParser parser = new CSSOMParser();
                StringReader reader = new StringReader( styleStr );
                InputSource is = new InputSource( reader );
                CSSStyleSheet style = parser.parseStyleSheet( is );
                reader.close();

                // HACK: this is overkill, but the constructors of the
                // XRStyleRuleImpl class are not set up for non-sheet
                // rule loading.
                sheet = XRStyleSheetImpl.newAuthorStyleSheet( style, 0 );
                XRStyleRule rule = (XRStyleRule)sheet.styleRules().next();
                _elementXRStyleMap.put( elem, rule );
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * Returns the XRElement instance associated with a Node, once {@link
     * #matchStyles(Document)} has been called on the Document. Every Node in a
     * matched Document has one XRElement in the system. If the Document has not
     * been matched, returns null.
     *
     * @param node  See comments.
     * @return      See comments.
     */
    private XRElement getNodeXRElement( Node node ) {
        return (XRElement)_nodeXRElementMap.get( node );
    }

    /**
     * Returns the XRProperty associated with the given Element.
     *
     * @param elem      PARAM
     * @param propName  PARAM
     * @return          The xRProperty value
     */
    /*public XRProperty getXRProperty( Element elem, String propName ) {
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( elem );
        return xrElement.derivedStyle().propertyByName( _context, propName );
    }*/


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
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( elem );
        return xrElement.derivedStyle().getBackgroundColor( _context );
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
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( elem );
        return xrElement.derivedStyle().getBorderColor( _context );
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
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( elem );
        return xrElement.derivedStyle().getBorderWidth( _context );
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
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( elem );
        return xrElement.derivedStyle().getColor( _context );
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
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( elem );
        return xrElement.derivedStyle().getColor( _context );
    }


    /**
     * Returns the a property assigned to an Element that can be interpreted as
     * a Point with floating-point positioning, and, if not found and inherit is
     * true, searches for an inheritable property by that name assigned to
     * parent and ancestor elements.
     *
     * @param elem     The DOM element to find the property for.
     * @param prop     The property name
     * @param inherit  If true and property not found on this element, searches
     *      through element ancestors for property
     * @return         The named property as a Point
     */
    public Point getFloatPairProperty( Element elem, String prop, boolean inherit ) {
        // ***
        if ( !_matchedSinceLastParse ) {
            matchStyles( elem.getOwnerDocument() );
        }
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( elem );

        XRProperty xrProp = xrElement.derivedStyle().propertyByName( _context, prop );

        if ( xrProp.actualValue().isValueList() ) {
            CSSValueList vl = (CSSValueList)xrProp.actualValue().cssValue();

            Point pt = new Point();

            pt.setLocation(
                    ( (CSSPrimitiveValue)vl.item( 0 ) ).getFloatValue( CSSPrimitiveValue.CSS_PERCENTAGE ),
                    ( (CSSPrimitiveValue)vl.item( 1 ) ).getFloatValue( CSSPrimitiveValue.CSS_PERCENTAGE )
                     );

            return pt;
        } else {
            XRLog.cascade( Level.WARNING, "Property : " + xrProp + " is not a value list " + xrProp.actualValue().cssValue().getClass().getName() );
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
        if ( !_matchedSinceLastParse ) {
            matchStyles( node.getOwnerDocument() );
        }
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( node );

        return xrElement.derivedStyle().propertyByName( _context, prop ).actualValue().asFloat();
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
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( elem );
        return xrElement.derivedStyle().getMarginWidth( _context );
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
        XRLog.cascade( Level.FINEST, "getPaddingWidth() on Element: " + elem.hashCode() );
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( elem );
        return xrElement.derivedStyle().getPaddingWidth( _context );
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
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( elem );
        XRProperty xrProp = xrElement.derivedStyle().propertyByName( _context, prop );
        return xrProp.actualValue().cssValue();
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
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( elem );
        return xrElement.derivedStyle().propertyByName( _context, prop ).actualValue().asStringArray();
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
    private String _getStringProperty( Node node, String prop, boolean inherit ) {
        if ( !_matchedSinceLastParse ) {
            matchStyles( node.getOwnerDocument() );
        }

        XRElement xrElement = (XRElement)_nodeXRElementMap.get( node );
        if ( xrElement == null ) {
            return null;
        }
        return xrElement.derivedStyle().propertyByName( _context, prop ).actualValue().asString();
    }


    /**
     * Applies matches to Element and its children, recursively. StyleMap should
     * have been re-loaded before calling this.
     *
     * @param elem  PARAM
     */
    private void applyMatches( Element elem ) {
        XRElement xrElement = (XRElement)_nodeXRElementMap.get( elem );
        if ( xrElement == null ) {
            XRElement parent = null;

            // if this is the root, we will have no parent XRElement; otherwise
            // we will check to see if our parent was loaded. Since we expect to load
            // from root to leaves, we should always find a parent
            // this means, however, that root will have a null parent
            if ( elem.getOwnerDocument().getDocumentElement() != elem ) {
                parent = (XRElement)_nodeXRElementMap.get( elem.getParentNode() );
                if ( parent == null ) {
                    throw new RuntimeException( "Applying matches to elements, found an element with no mapped parent; can't continue." );
                }
            }
            xrElement = new XRElementImpl( elem, parent );
            _nodeXRElementMap.put( elem, xrElement );
        }

        // StyleMap will return a List of XRStyleRules matched to the element,
        // in the same sort order we originally passed in.
        List styleList = _tbStyleMap.getMappedProperties( elem );
        Iterator iter = styleList.iterator();
        while ( iter.hasNext() ) {
            XRStyleRule rule = (XRStyleRule)iter.next();
            xrElement.addMatchedStyle( rule );
        }
        // apply rules from style attribute on element, if any
        XRStyleRule attrRule = (XRStyleRule)_elementXRStyleMap.get( elem );
        if ( attrRule != null ) {
            xrElement.addMatchedStyle( attrRule );
        }

        NodeList nl = elem.getChildNodes();
        for ( int i = 0, len = nl.getLength(); i < len; i++ ) {
            Node n = nl.item( i );
            if ( n instanceof Element ) {
                applyMatches( (Element)n );
            }
        }
    }


    /**
     * Adds the top-level or leftmost selector of the XRStyleReference object.
     *
     * @param rs        The feature to be added to the ChainedSelector attribute
     * @param selector  The feature to be added to the ChainedSelector attribute
     * @return          Returns
     */
    private org.xhtmlrenderer.css.match.Selector addSelector( Ruleset rs, Selector selector ) {
        org.xhtmlrenderer.css.match.Selector s = null;
        if ( selector.getSelectorType() == Selector.SAC_DIRECT_ADJACENT_SELECTOR ) {
            s = addSelector( rs, ( (SiblingSelector)selector ).getSelector() );
            addChainedSelector( s, selector );
        } else if ( selector.getSelectorType() == Selector.SAC_CHILD_SELECTOR ) {
            s = addSelector( rs, ( (DescendantSelector)selector ).getAncestorSelector() );
            addChainedSelector( s, selector );
        } else if ( selector.getSelectorType() == Selector.SAC_DESCENDANT_SELECTOR ) {
            s = addSelector( rs, ( (DescendantSelector)selector ).getAncestorSelector() );
            addChainedSelector( s, selector );
        } else if ( selector.getSelectorType() == Selector.SAC_CONDITIONAL_SELECTOR ) {
            Condition cond = ( (ConditionalSelector)selector ).getCondition();
            s = addSelector( rs, ( (ConditionalSelector)selector ).getSimpleSelector() );
            addConditions( s, cond );
        } else if ( selector.getSelectorType() == Selector.SAC_ELEMENT_NODE_SELECTOR ) {
            s = rs.createSelector( org.xhtmlrenderer.css.match.Selector.DESCENDANT_AXIS, ( (ElementSelector)selector ).getLocalName() );
        } else {
            System.err.println( "bad selector in addSelector" );
        }

        return s;
    }

    /**
     * Adds a feature to the ChainedSelector attribute of the XRStyleReference
     * object.
     *
     * @param s         The feature to be added to the ChainedSelector attribute
     * @param selector  The feature to be added to the ChainedSelector attribute
     */
    // CLN: Taken from TobeRuleBank (PWW 08/07/2004)
    // Tobe fixed 2004-08-26
    private void addChainedSelector( org.xhtmlrenderer.css.match.Selector s, Selector selector ) {
        int axis = 0;
        SimpleSelector simple = null;
        switch ( selector.getSelectorType() ) {
            case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
                axis = org.xhtmlrenderer.css.match.Selector.IMMEDIATE_SIBLING_AXIS;
                simple = ( (SiblingSelector)selector ).getSiblingSelector();
                break;
            case Selector.SAC_CHILD_SELECTOR:
                axis = org.xhtmlrenderer.css.match.Selector.CHILD_AXIS;
                simple = ( (DescendantSelector)selector ).getSimpleSelector();
                break;
            case Selector.SAC_DESCENDANT_SELECTOR:
                axis = org.xhtmlrenderer.css.match.Selector.DESCENDANT_AXIS;
                simple = ( (DescendantSelector)selector ).getSimpleSelector();
                break;
            default:
                System.err.println( "Bad selector" );
        }

        Condition cond = null;
        if ( simple.getSelectorType() == Selector.SAC_CONDITIONAL_SELECTOR ) {
            cond = ( (ConditionalSelector)simple ).getCondition();
            //if ConditionalSelectors can be nested, we are in trouble here
            simple = ( (ConditionalSelector)simple ).getSimpleSelector();
        }
        if ( simple.getSelectorType() == Selector.SAC_ELEMENT_NODE_SELECTOR ) {
            s = s.appendChainedSelector( axis, ( (ElementSelector)simple ).getLocalName() );
        }
        if ( cond != null ) {
            addConditions( s, cond );
        }
    }

    /**
     * Adds a feature to the Rule attribute of the XRStyleReference object
     *
     * @param rule  The feature to be added to the Rule attribute
     */
    // CLN: * Taken from TobeRuleBank (PWW 08/07/2004)
    // Tobe fixed 2004-08-26
    private void addRule( XRStyleRule rule ) {
        _tbStyleMap = null;//New rule means we have to remap elements to their styles
        Ruleset rs = new Ruleset();
        rs.setStyleDeclaration( rule );
        _ruleSetMap.put( rule, rs );

        org.w3c.css.sac.SelectorList selector_list = rule.selectorsAsSACList();
        for ( int i = 0; i < selector_list.getLength(); i++ ) {
            Selector selector = selector_list.item( i );
            org.xhtmlrenderer.css.match.Selector s = addSelector( rs, selector );
        }
    }


    /**
     * Parses the stylesheet into rules and loads for internal lookups.
     *
     * @param cssReader  PARAM
     * @param origin     PARAM
     * @return           Returns
     */
    private XRStyleSheet loadSheet( Reader cssReader, int origin ) {
        XRStyleSheet sheet = null;
        try {
            CSSOMParser parser = new CSSOMParser();
            InputSource is = new InputSource( cssReader );
            CSSStyleSheet style = parser.parseStyleSheet( is );

            _sheetCnt++;
            switch ( origin ) {
                case XRStyleSheet.USER_AGENT:
                    sheet = XRStyleSheetImpl.newUserAgentStyleSheet( style, _sheetCnt );
                    break;
                case XRStyleSheet.AUTHOR:
                    sheet = XRStyleSheetImpl.newAuthorStyleSheet( style, _sheetCnt );
                    break;
                case XRStyleSheet.USER:
                    sheet = XRStyleSheetImpl.newUserStyleSheet( style, _sheetCnt );
                    break;
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        // add sheet rules locally
        Iterator rules = sheet.styleRules();
        while ( rules.hasNext() ) {
            XRStyleRule styleRule = (XRStyleRule)rules.next();
            addRule( styleRule );
            _xrStyleRuleList.add( styleRule );
        }
        return sheet;
    }


    /**
     * @param s     The feature to be added to the Conditions attribute
     * @param cond  The feature to be added to the Conditions attribute
     */
    // CLN: Taken from TobeRuleBank (PWW 08/07/2004)
    private void addConditions( org.xhtmlrenderer.css.match.Selector s, Condition cond ) {
        switch ( cond.getConditionType() ) {
            case Condition.SAC_AND_CONDITION:
                CombinatorCondition comb = (CombinatorCondition)cond;
                addConditions( s, comb.getFirstCondition() );
                addConditions( s, comb.getSecondCondition() );
                break;
            case Condition.SAC_ATTRIBUTE_CONDITION:
                AttributeCondition attr = (AttributeCondition)cond;
                if ( attr.getSpecified() ) {
                    s.addAttributeEqualsCondition( attr.getLocalName(), attr.getValue() );
                } else {
                    s.addAttributeExistsCondition( attr.getLocalName() );
                }
                break;
            case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
                attr = (AttributeCondition)cond;
                s.addAttributeMatchesFirstPartCondition( attr.getLocalName(), attr.getValue() );
                break;
            case Condition.SAC_CLASS_CONDITION:
                attr = (AttributeCondition)cond;
                s.addClassCondition( attr.getValue() );
                break;
            case Condition.SAC_ID_CONDITION:
                attr = (AttributeCondition)cond;
                s.addIDCondition( attr.getValue() );
                break;
            case Condition.SAC_LANG_CONDITION:
                LangCondition lang = (LangCondition)cond;
                s.addLangCondition( lang.getLang() );
                break;
            case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
                attr = (AttributeCondition)cond;
                s.addAttributeMatchesListCondition( attr.getLocalName(), attr.getValue() );
                break;
            case Condition.SAC_POSITIONAL_CONDITION:
                PositionalCondition pos = (PositionalCondition)cond;
                s.addFirstChildCondition();
                break;
            case Condition.SAC_PSEUDO_CLASS_CONDITION:
                attr = (AttributeCondition)cond;
                if ( attr.getValue().equals( "link" ) ) {
                    s.setPseudoClass( AttributeResolver.LINK_PSEUDOCLASS );
                }
                if ( attr.getValue().equals( "visited" ) ) {
                    s.setPseudoClass( AttributeResolver.VISITED_PSEUDOCLASS );
                }
                if ( attr.getValue().equals( "hover" ) ) {
                    s.setPseudoClass( AttributeResolver.HOVER_PSEUDOCLASS );
                }
                if ( attr.getValue().equals( "active" ) ) {
                    s.setPseudoClass( AttributeResolver.ACTIVE_PSEUDOCLASS );
                }
                if ( attr.getValue().equals( "focus" ) ) {
                    s.setPseudoClass( AttributeResolver.FOCUS_PSEUDOCLASS );
                }
                break;
            default:
                System.err.println( "Bad condition" );
        }
    }

    public void setDocumentContext(Context context, org.xhtmlrenderer.extend.NamespaceHandler nsh, org.xhtmlrenderer.extend.AttributeResolver ar, Document doc) {
        _context = context;
        Element html = (Element)doc.getDocumentElement();

        try {
            Object marker = new DefaultCSSMarker();

            String defaultStyleSheetLocation = Configuration.valueFor( "xr.css.user-agent-default-css" );
            if ( marker.getClass().getResourceAsStream( defaultStyleSheetLocation ) != null ) {
                URL stream = marker.getClass().getResource( defaultStyleSheetLocation );
                String str = u.inputstream_to_string( stream.openStream() );
                parse( new StringReader( str ),
                        XRStyleSheet.USER_AGENT );
            } else {
                XRLog.exception(
                        "Can't load default CSS from " + defaultStyleSheetLocation + "." +
                        "This file must be on your CLASSPATH. Please check before continuing." );
            }

            parseDeclaredStylesheets( html );
            parseLinkedStyles( html );
            parseInlineStyles( html );
        } catch ( Exception ex ) {
            XRLog.exception( "Could not parse CSS in the XHTML source: declared, linked or inline.", ex );
        }

    }
    
    public java.util.Map getDerivedPropertiesMap(Element e) {
        XRElement xrElem = getNodeXRElement( e );
        Iterator iter = xrElem.derivedStyle().listXRProperties();
        java.util.LinkedHashMap props = new java.util.LinkedHashMap();
        while ( iter.hasNext() ) {
            XRProperty prop = (XRProperty)iter.next();
            prop = xrElem.derivedStyle().propertyByName( _context, prop.propertyName() );
            props.put( prop.propertyName(), prop.actualValue().cssValue() );
        }
        return props;
    }
    
    public org.xhtmlrenderer.css.newmatch.CascadedStyle getPseudoElementStyle(Element e, String pseudoElement) {
        return null;//not supported
    }
    
    public org.xhtmlrenderer.css.style.CalculatedStyle getStyle(Element e) {
        return null;//not supported
    }
    
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.13  2004/11/08 23:15:56  tobega
 * Changed pseudo-element styling to just return CascadedStyle
 *
 * Revision 1.12  2004/11/08 08:22:16  tobega
 * Added support for pseudo-elements
 *
 * Revision 1.11  2004/11/07 01:31:38  tobega
 * Added hooks for handling First-letter pseudo-element
 *
 * Revision 1.10  2004/11/07 01:17:55  tobega
 * DOMInspector now works with any StyleReference
 *
 * Revision 1.9  2004/11/05 23:53:59  tobega
 * no message
 *
 * Revision 1.8  2004/11/04 21:50:37  tobega
 * Preparation for new matching/styling code
 *
 * Revision 1.7  2004/10/23 13:06:54  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */
