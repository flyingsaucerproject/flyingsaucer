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

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import org.w3c.dom.Document;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.GeneralUtil;
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
public class XMLResource extends AbstractResource {
    private String documentTitle;
    private List inlineStyles;
    private Document document;
    private static final XMLResourceBuilder XML_RESOURCE_BUILDER;
    
    static {
        XML_RESOURCE_BUILDER = new XMLResourceBuilder();
    }
    
    private XMLResource(InputStream stream) { 
        super(stream);
    }
    
    private XMLResource(URL url) {
        super(url);
    }
    
    private XMLResource(InputSource source) {
        super(source);
    }
    
    public static XMLResource load(InputStream stream) {
        return XML_RESOURCE_BUILDER.createXMLResource(new XMLResource(stream));
    }
    
    public static XMLResource load(URL url) {
        return XML_RESOURCE_BUILDER.createXMLResource(new XMLResource(url));
    }

    public static XMLResource load(InputSource source) {
        return XML_RESOURCE_BUILDER.createXMLResource(new XMLResource(source));
    }
    
    public String getDocumentTitle() {
        return documentTitle;
    }

    /*package*/ void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public List getInlineStyles() {
        return inlineStyles;
    }

    /*package*/ void setInlineStyles(List inlineStyles) {
        this.inlineStyles = inlineStyles;
    }

    public Document getDocument() {
        return document;
    }

    /*package*/ void setDocument(Document document) {
        this.document = document;
    }
    
    private static class XMLResourceBuilder {
        XMLResource createXMLResource(XMLResource target) {
            Source input = null;
            DOMResult output = null;
            TransformerFactory xformFactory = null;
            Transformer idTransform = null;
            XMLReader saxParser = null;
            long st = 0L;
            
            saxParser = createXMLReader();
            addHandlers(saxParser);
            
            try {
                st = System.currentTimeMillis();
                input = new SAXSource( saxParser, target.getResourceInputSource());
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
            
            target.setElapsedLoadTime(end - st);
            
            XRLog.load("Loaded document in ~" + target.getElapsedLoadTime() + "ms");
            
            target.setDocument((Document)output.getNode());
            return target;
        }
        
        private XMLReader createXMLReader() {
            XMLReader saxParser = null;
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
            return saxParser;
        }
        
        private void addHandlers(XMLReader saxParser) {
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
            } catch (Exception ex ) {
                throw new XRRuntimeException("Failed on configuring SAX parser/XMLReader.", ex);
            }
        }
        
        private void setParserFeatures(XMLReader saxParser) {
            try {
                // perf: validation off
                saxParser.setFeature("http://xml.org/sax/features/validation", false);
                
                // mem: intern strings
                //saxParser.setFeature( "http://xml.org/sax/features/string-interning", true );
                
                // performance: turn off namespaces
                //saxParser.setFeature("http://xml.org/sax/features/namespaces", false);
                //saxParser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            } catch ( Exception ex ) {
                throw new XRRuntimeException("Failed on setting features and properties for XMLReader before XML document load.", ex);
            }
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2005/02/05 11:33:33  pdoubleya
 * Added load() to XMLResource, and accept overloaded input: InputSource, stream, URL.
 *
 * Revision 1.1  2005/02/03 20:39:35  pdoubleya
 * Added to CVS.
 *
 *
 */