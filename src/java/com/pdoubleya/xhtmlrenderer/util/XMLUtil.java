package com.pdoubleya.xhtmlrenderer.util;


import java.net.URL;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/** Booch utility class for XML processing using DOM */
public class XMLUtil {
    public static Document documentFromURL( final URL docURL )
        throws Exception {

        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fact.newDocumentBuilder();
        builder.setErrorHandler( null );
        return builder.parse( docURL.openStream() );
    }

    public static Document documentFromFile( final File docFile )
        throws Exception {

        URL url = docFile.toURL();
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fact.newDocumentBuilder();
        builder.setErrorHandler( null );
        return builder.parse( url.openStream() );
    }

    public static Document documentFromString( final String documentContents )
        throws Exception {

        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fact.newDocumentBuilder();
        builder.setErrorHandler( null );
        return builder.parse( documentContents );
    }

} // end class
