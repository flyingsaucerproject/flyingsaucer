/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Who?
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
package org.xhtmlrenderer.resource;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.logging.Level;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import org.w3c.dom.Document;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 *
 * @author Patrick Wright
 */
public class DefaultResourceProvider implements ResourceProvider {
    private final XMLResourceBuilder xmlResourceBuilder;
    
    static {
    }
    
    /** Creates a new instance of DefaultResourceProvider */
    /*package*/ DefaultResourceProvider() {
        xmlResourceBuilder = new XMLResourceBuilder();
    }
    
    public XMLResource newXMLResource(org.xml.sax.InputSource is) {
        return xmlResourceBuilder.createXMLResource(is);
    }
    
    public XMLResource newXMLResource(URL url) {
        InputSource source = null;
        try {
            source = new InputSource(new BufferedInputStream(url.openStream()));
        } catch ( Exception ex ) {
            throw new XRRuntimeException("Can't open URL during load of XML resource.", ex);
        }
        return xmlResourceBuilder.createXMLResource( source );
    }

    public ImageResource newImageResource(org.xml.sax.InputSource is) {
        return null;
    }
    
    public CSSResource newCSSResource(org.xml.sax.InputSource is) {
        return null;
    }
    
    private class XMLResourceBuilder {
        XMLResource createXMLResource(InputSource source) {
            Source input = null;
            DOMResult output = null;
            TransformerFactory xformFactory = null;
            Transformer idTransform = null;
            XMLReader saxParser = null;
            long st = 0L;
            
            String xmlReaderClass = Configuration.valueFor("xr.load.xml-reader");
            try {
                if ( xmlReaderClass != null && !xmlReaderClass.toLowerCase().equals("default")) {
                    saxParser = XMLReaderFactory.createXMLReader( xmlReaderClass );
                }
            } catch ( Exception ex ) {
                XRLog.load(Level.WARNING,
                        "Could not instantiate XMLReader class for XML parsing: "
                        + xmlReaderClass +". Please check classpath. Use value 'default' in " +
                        "FS configuration if necessary. Will now try JDK default.", ex);
            }
            if ( saxParser == null ) {
                try {
                    // JDK default
                    saxParser = XMLReaderFactory.createXMLReader();
                    xmlReaderClass = "{JDK default}";
                } catch ( Exception ex ) {
                    XRLog.general(ex.getMessage());
                }
            }
            XRLog.load( "SAX XMLReader in use (parser): " + saxParser.getClass().getName() );
            
            try {
                // add our own entity resolver
                saxParser.setEntityResolver(FSEntityResolver.instance());
                saxParser.setErrorHandler(
                        new ErrorHandler() {
                    
                    public void error( SAXParseException ex ) {
                        XRLog.load(ex.getMessage());
                    }
                    
                    public void fatalError( SAXParseException ex ) {
                        XRLog.load(ex.getMessage());
                    }
                    
                    public void warning( SAXParseException ex ) {
                        XRLog.load(ex.getMessage());
                    }
                } );
                
                // perf: validation off
                saxParser.setFeature("http://xml.org/sax/features/validation", false);
                
                // mem: intern strings
                //saxParser.setFeature( "http://xml.org/sax/features/string-interning", true );
                
                // performance: turn off namespaces
                //saxParser.setFeature("http://xml.org/sax/features/namespaces", false);
                //saxParser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
                
                st = System.currentTimeMillis();
                input = new SAXSource( saxParser, source );
                output = new DOMResult();
                xformFactory = TransformerFactory.newInstance();
                idTransform = xformFactory.newTransformer();
            } catch ( Exception ex ) {
                throw new XRRuntimeException("Failed on configuring SAX parser/XMLReader.", ex);
            }
            
            try {
                idTransform.transform( input, output );
            } catch (Exception ex) {
                throw new XRRuntimeException("Failed on loading the requested XML resource.", ex);
            }
            
            long end = System.currentTimeMillis();
            
            XMLResource xr = new XMLResource(source);
            xr.setElapsedLoadTime(end - st);
            
            XRLog.load("Loaded document in ~" + xr.getElapsedLoadTime() + "ms");
            
            xr.setDocument((Document)output.getNode());
            return xr;
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2005/02/03 20:39:34  pdoubleya
 * Added to CVS.
 *
 *
 */