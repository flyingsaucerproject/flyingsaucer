/*
 * {{{ header & license
 * XMLUtil.java
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
package org.xhtmlrenderer.util;


import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * Booch utility class for XML processing using DOM
 *
 * @author   empty
 */
public class XMLUtil {


    /** Description of the Field */
    private Util util;

    /** Constructor for the XMLUtil object */
    public XMLUtil() {
        util = new Util( System.out );
    }

    /**
     * Constructor for the XMLUtil object
     *
     * @param out  PARAM
     */
    public XMLUtil( OutputStream out ) {
        util = new Util( out );
    }

    /**
     * Constructor for the XMLUtil object
     *
     * @param pw  PARAM
     */
    public XMLUtil( PrintWriter pw ) {
        util = new Util( pw );
    }

    /**
     * Constructor for the XMLUtil object
     *
     * @param util  PARAM
     */
    public XMLUtil( Util util ) {
        this.util = util;
    }

    /**
     * Description of the Method
     *
     * @param doc  PARAM
     */
    public void p( Document doc ) {
        util.print( "document:" );
        NodeList children = doc.getChildNodes();
        for ( int i = 0; i < children.getLength(); i++ ) {
            p( children.item( i ) );
        }
    }

    /**
     * Description of the Method
     *
     * @param node  PARAM
     */
    public void p( Node node ) {
        util.print( "node: " + node );
        util.print( "name: " + node.getNodeName() );
        util.print( "value: " + node.getNodeValue() );

        NodeList children = node.getChildNodes();
        for ( int i = 0; i < children.getLength(); i++ ) {
            p( children.item( i ) );
        }
    }

    /**
     * Description of the Method
     *
     * @param docURL         PARAM
     * @return               Returns
     * @exception Exception  Throws
     */
    public static Document documentFromURL( final URL docURL )
        throws Exception {

        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fact.newDocumentBuilder();
        builder.setErrorHandler( null );
        return builder.parse( docURL.openStream() );
    }

    /**
     * Description of the Method
     *
     * @param docFile        PARAM
     * @return               Returns
     * @exception Exception  Throws
     */
    public static Document documentFromFile( final File docFile )
        throws Exception {

        URL url = docFile.toURL();
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fact.newDocumentBuilder();
        builder.setErrorHandler( null );
        return builder.parse( url.openStream() );
    }

    /**
     * Description of the Method
     *
     * @param documentContents  PARAM
     * @return                  Returns
     * @exception Exception     Throws
     */
    public static Document documentFromString( final String documentContents )
        throws Exception {

        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fact.newDocumentBuilder();
        builder.setErrorHandler( null );
        return builder.parse( documentContents );
    }

    /**
     * Description of the Method
     *
     * @param string         PARAM
     * @return               Returns
     * @exception Exception  Throws
     */
    public static Document loadDocument( String string )
        throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( string );
        return doc;
    }

    /*
     * public static void saveDocument(Document doc, String filename) throws Exception {
     * //Util.print("about to save: " + doc);
     * //p(doc);
     * XMLSerializer xs = new XMLSerializer();
     * xs.setOutputByteStream(new FileOutputStream(filename));
     * OutputFormat of = new OutputFormat();
     * of.setMethod("xml");
     * of.setIndenting(true);
     * of.setIndent(4);
     * of.setPreserveSpace(true);
     * of.setOmitDocumentType(true);
     * xs.setOutputFormat(of);
     * xs.serialize(doc);
     * }
     */
    /**
     * Description of the Method
     *
     * @return               Returns
     * @exception Exception  Throws
     */
    public static Document newDocument()
        throws Exception {
        Document output = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        return output;
    }

    /**
     * Description of the Method
     *
     * @param doc    PARAM
     * @param name   PARAM
     * @param value  PARAM
     * @return       Returns
     */
    public static Element ne( Document doc, String name, String value ) {
        return newElement( doc, name, value );
    }

    /**
     * Description of the Method
     *
     * @param doc    PARAM
     * @param name   PARAM
     * @param value  PARAM
     * @return       Returns
     */
    public static Element newElement( Document doc, String name, String value ) {
        if ( value == null ) {
            value = "";
        }
        Element elem = doc.createElement( name );
        Text text = doc.createTextNode( value );
        elem.appendChild( text );
        return elem;
    }

    /**
     * Description of the Method
     *
     * @param doc            PARAM
     * @param xsl            PARAM
     * @param pw             PARAM
     * @exception Exception  Throws
     */
    public static void transform( Document doc, String xsl, PrintWriter pw )
        throws Exception {
        //System.out.println("transforming with xsl: " + xsl);
        //Util.print("doc = " + doc);
        DOMSource xml_source = new DOMSource( doc );
        File xsl_file = new File( xsl );
        //Util.print("xsl_file = " + xsl_file);
        StreamSource xsl_source = new StreamSource( xsl_file );
        //Util.print("xsl_source = " + xsl_source);
        Transformer transformer = TransformerFactory.newInstance().newTransformer( xsl_source );
        StreamResult result = new StreamResult( pw );
        transformer.transform( xml_source, result );
    }

    /**
     * Description of the Method
     *
     * @param element  PARAM
     */
    public static void clear( Element element ) {
        NodeList nl = element.getChildNodes();
        for ( int i = 0; i < nl.getLength(); i++ ) {
            element.removeChild( nl.item( 0 ) );
        }
    }

    /**
     * Description of the Method
     *
     * @param node  PARAM
     * @param name  PARAM
     * @return      Returns
     */
    public static Element child( Element node, String name ) {
        NodeList nl = node.getElementsByTagName( name );
        if ( nl == null ) {
            return null;
        }
        return (Element)nl.item( 0 );
    }

    /**
     * Description of the Method
     *
     * @param node  PARAM
     * @param name  PARAM
     * @return      Returns
     */
    public static NodeList children( Element node, String name ) {
        NodeList nl = node.getElementsByTagName( name );
        return nl;
    }

    /**
     * Description of the Method
     *
     * @param node  PARAM
     * @return      Returns
     */
    public static String text( Node node ) {
        NodeList nl = node.getChildNodes();
        Node item = nl.item( 0 );
        return item.getNodeValue();
    }

}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2004/10/23 14:06:57  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

