/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
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
package org.xhtmlrenderer.css;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.apache.xpath.XPathAPI;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.util.XRLog;

import org.xhtmlrenderer.DefaultCSSMarker;
import org.xhtmlrenderer.util.Configuration;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class CSSBank extends CSSAccessor {

    /*
     * internal vars
     */
    /*
     * CLN: removed (PWW 13/08/04)
     */
    //private List sheets;

    /*
     * CLN: removed (PWW 13/08/04)
     */
    //private List style_nodes;

    /*
     * CLN: public to private (PWW 13/08/04)
     */
    /** Description of the Field */
    private List styles;

    /*
     * CLN: package-public to private (PWW 13/08/04)
     */
    /** Description of the Field */
    private CSSParser parser;

    /*
     * CLN: public to private (PWW 13/08/04)
     */
    /** Description of the Field */
    private RuleBank rule_bank;

    /** Constructor for the CSSBank object */
    public CSSBank() {
        this( new RuleFinder() );
    }

    // Instantiates for a specific RuleBank
    /**
     * Constructor for the CSSBank object
     *
     * @param rb  PARAM
     */
    public CSSBank( RuleBank rb ) {
        styles = new ArrayList();

        rule_bank = rb;

        parser = new CSSParser( this.rule_bank );
    }


    /**
     * Description of the Method
     *
     * @param reader           PARAM
     * @exception IOException  Throws
     */
    private void parse( Reader reader )
        throws IOException {

        parser.parse( reader );

    }

    // HACK: origin flag is new feature in XRStyleReference, added here for
    // common interface, though it is ignored in CSSBank
    /**
     * Description of the Method
     *
     * @param reader           PARAM
     * @param origin           PARAM
     * @exception IOException  Throws
     */
    private void parse( Reader reader, int origin )
        throws IOException {

        parser.parse( reader );

    }

    /**
     * Description of the Method
     *
     * @param reader           PARAM
     * @exception IOException  Throws
     */
    private void parse( String reader )
        throws IOException {

        parser.parse( reader );

    }

    /**
     * Same as {@link #parse(Reader, int)} for a String datasource.
     *
     * @param source           A String containing CSS style rules
     * @param origin           See {@link #parse(Reader, int)}
     * @exception IOException  {@link #parse(Reader, int)}
     */
    // HACK: origin flag is new feature in XRStyleReference, added here for
    // common interface, though it is ignored in CSSBank
    private void parse( String source, int origin )
        throws IOException {
        parse( source );
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
                URL url = new URL( href );
                InputStream is = url.openStream();
                BufferedInputStream bis = new BufferedInputStream( is );
                InputStreamReader reader = new InputStreamReader( bis );
                parse( reader, XRStyleSheet.AUTHOR );
                is.close();
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }


    /**
     * Description of the Method
     *
     * @param elem             PARAM
     * @exception IOException  Throws
     */
    private void parseInlineStyles( Element elem )
        throws IOException {

        parser.parseInlineStyles( elem );

    }

    /**
     * Parses the CSS style information from the inline "style" attribute on the
     * DOM Element, and loads these rules into the associated RuleBank,
     * automatically associating those styles as matched to the Element.
     *
     * @param elem             The Element from which to pull a style attribute.
     * @exception IOException  Throws
     */
    private void parseElementStyling( Element elem )
        throws IOException {

        // TODO: code parsing for Element style attribute
        throw new RuntimeException( "NOT CODED: pulling style attributes from an Element." );
    }

    /**
     * <p>
     *
     * Attempts to match any loaded to Elements in the supplied Document, using
     * CSS2 matching rules on selection. This should be called after all
     * stylesheets and styles are loaded, but before any properties are
     * retrieved. </p>
     *
     * @param document  PARAM
     */
    private void matchStyles( Document document ) {
        System.out.println( "!! CSSBank does not currently implement anything in matchStyles()." );
    }


    /**
     * Description of the Method
     *
     * @param root  PARAM
     */
    private void parseDeclaredStylesheets( Element root ) {
        u.p( "parsing declared stylesheets is unsupported with the CSSBank" );
    }


    /**
     * Gets the property attribute of the CSSBank object
     *
     * @param elem     PARAM
     * @param prop     PARAM
     * @param inherit  PARAM
     * @return         The property value
     */
    public CSSValue getProperty(Element elem, String prop, boolean inherit) {

        //RuleFinder rf = new RuleFinder(this.styles);

        CSSStyleDeclaration style_dec = rule_bank.findRule( elem, prop, inherit );


        if ( style_dec == null ) {

            //u.p("print there is no style declaration at all for: " + elem.getNodeName());

            //u.p("looking for property: " + prop);

            return null;
        }

        CSSValue val = style_dec.getPropertyCSSValue( prop );

        if ( val == null ) {

            u.p( "WARNING!! elem " + elem.getNodeName() + " doesn't have the property: " + prop );

        }

        //u.p("returning: " + val);

        return val;
    }

    /**
     * Description of the Method
     *
     * @param sheet            PARAM
     * @exception IOException  Throws
     */
    private void pullOutStyles( CSSStyleSheet sheet )
        throws IOException {

        parser.pullOutStyles( sheet );

    }


    /*
     * ========= property accessors ============
     */
    /**
     * Gets the property attribute of the CSSBank object
     *
     * @param node  PARAM
     * @param prop  PARAM
     * @return      The property value
     */
    private Object getProperty( Node node, String prop ) {

        if ( node.getNodeType() == node.TEXT_NODE ) {

            return getProperty( node.getParentNode(), prop );
        }

        if ( node.getNodeType() == node.ELEMENT_NODE ) {

            return getProperty( (Element)node, prop );
        }

        u.p( "unknown node type: " + node );

        u.p( "type = " + node.getNodeType() );

        return null;
    }
    
    public void setDocumentContext(org.xhtmlrenderer.layout.Context context, org.xhtmlrenderer.extend.NamespaceHandler nsh, org.xhtmlrenderer.extend.AttributeResolver ar, Document doc) {
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
        return new java.util.HashMap();
    }
    
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.7  2004/11/07 01:17:55  tobega
 * DOMInspector now works with any StyleReference
 *
 * Revision 1.6  2004/11/05 23:56:41  tobega
 * no message
 *
 * Revision 1.5  2004/10/28 13:39:46  joshy
 * removed dead code
 *
 * Revision 1.4  2004/10/23 13:03:45  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

