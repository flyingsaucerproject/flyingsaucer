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
import javax.xml.parsers.SAXParserFactory; 
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
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
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
    private static boolean useConfiguredParser;
    
    static {
        XML_RESOURCE_BUILDER = new XMLResourceBuilder();
        useConfiguredParser = true;
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
    
    public static final XMLReader newXMLReader() {
        XMLReader saxParser = null;
        String xmlReaderClass = Configuration.valueFor("xr.load.xml-reader");
        
        //TODO: if it doesn't find the parser, note that in a static boolean--otherwise
        // you get exceptions on every load
        try {
            if ( xmlReaderClass != null && 
                 !xmlReaderClass.toLowerCase().equals("default") && 
                 XMLResource.useConfiguredParser ) {
                try {
                    Class.forName(xmlReaderClass);
                } catch (Exception ex) {
                    XMLResource.useConfiguredParser = false;
                    XRLog.load(Level.WARNING,
                            "The XMLReader class you specified as a configuration property " +
                            "could not be found. Class.forName() failed on "
                            + xmlReaderClass +". Please check classpath. Use value 'default' in " +
                            "FS configuration if necessary. Will now try JDK default.");
                }
                if ( XMLResource.useConfiguredParser ) {
                    saxParser = XMLReaderFactory.createXMLReader( xmlReaderClass );
                }
            }
        } catch ( Exception ex ) {
            XRLog.load(Level.WARNING,
                    "Could not instantiate custom XMLReader class for XML parsing: "
                    + xmlReaderClass +". Please check classpath. Use value 'default' in " +
                    "FS configuration if necessary. Will now try JDK default.", ex);
        }
        if ( saxParser == null ) {
            try {
                // JDK default
                // HACK: if 
                if ( System.getProperty("org.xml.sax.driver") == null ) {
                    String newDefault = "org.apache.crimson.parser.XMLReaderImpl";
                     XRLog.load(Level.WARNING,
                    "No value for system property 'org.xml.sax.driver'.");
                }
                saxParser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                xmlReaderClass = "{JDK default}";
            } catch ( Exception ex ) {
                XRLog.general(ex.getMessage());
            }
        }
        if ( saxParser == null ) {
            throw new XRRuntimeException("Could not instantiate any SAX 2 parser, including JDK default. " +
                    "The name of the class to use should have been read from the org.xml.sax.driver System " +
                    "property, which is set to: " + System.getProperty("org.xml.sax.driver"));
        }
        XRLog.load( "SAX XMLReader in use (parser): " + saxParser.getClass().getName() );
        return saxParser;
    }
    
    private static class XMLResourceBuilder {
        XMLResource createXMLResource(XMLResource target) {
            Source input = null;
            DOMResult output = null;
            TransformerFactory xformFactory = null;
            Transformer idTransform = null;
            XMLReader saxParser = null;
            long st = 0L;
            
            saxParser = XMLResource.newXMLReader();
            addHandlers(saxParser);
            setParserFeatures(saxParser);
            
            st = System.currentTimeMillis();
            try {
                input = new SAXSource( saxParser, target.getResourceInputSource());
                output = new DOMResult();
                xformFactory = TransformerFactory.newInstance();
                idTransform = xformFactory.newTransformer();
            } catch ( Exception ex ) {
                throw new XRRuntimeException("Failed on configuring SAX to DOM transformer.", ex);
            }
            
            try {
                idTransform.transform( input, output );
            } catch (Exception ex) {
                throw new XRRuntimeException("Failed on loading the requested XML resource using TRaX transformer.", ex);
            }
            
            long end = System.currentTimeMillis();
            
            target.setElapsedLoadTime(end - st);
            
            XRLog.load("Loaded document in ~" + target.getElapsedLoadTime() + "ms");
            
            target.setDocument((Document)output.getNode());
            return target;
        }
        
        /**
         * Adds the default EntityResolved and ErrorHandler for the SAX parser.
         */
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
        
        /**
         * Sets all standard features for SAX parser, using values from Configuration.
         */
        private void setParserFeatures(XMLReader saxParser) {
            if ( Configuration.isFalse("xr.load.configure-features", false)) {
                XRLog.load( Level.FINE, "SAX Parser: by request, not changing any parser features.");
                return;
            }
            
            // perf: validation off
            setFeature(saxParser, "http://xml.org/sax/features/validation","xr.load.validation");
            
            // mem: intern strings
            setFeature(saxParser, "http://xml.org/sax/features/string-interning", "xr.load.string-interning");
            
            // perf: namespaces
            setFeature(saxParser, "http://xml.org/sax/features/namespaces", "xr.load.namespaces");
            setFeature(saxParser, "http://xml.org/sax/features/namespace-prefixes", "xr.load.namespace-prefixes");
        }
        
        /**
         * Attempts to set requested feature on the parser; logs exception if not supported
         * or not recognized.
         */
        private void setFeature(XMLReader saxParser, String featureUri, String configName) {
            try {
                saxParser.setFeature( featureUri, Configuration.isTrue(configName, false));
                
                XRLog.load(Level.FINE, "SAX Parser feature: " +
                        featureUri.substring(featureUri.lastIndexOf("/")) +
                        " set to " +
                        saxParser.getFeature(featureUri));
            } catch ( SAXNotSupportedException ex ) {
                XRLog.load( Level.WARNING, "SAX feature not supported on this XMLReader: " + featureUri);
            } catch ( SAXNotRecognizedException ex ) {
                XRLog.load( Level.WARNING, "SAX feature not recognized on this XMLReader: " + featureUri +
                        ". Feature may be properly named, but not recognized by this parser.");
            }
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2005/03/16 19:26:31  pdoubleya
 * Fixed to use proper javax.xml.transform instantiation for parser, and only try to load custom parser once, so that you don't get exceptions on each page.
 *
 * Revision 1.4  2005/02/05 18:09:39  pdoubleya
 * Add specific SAX class name if none was specified and if system property is not already set.
 *
 * Revision 1.3  2005/02/05 17:19:47  pdoubleya
 * Refactoring for features support, static factory method for XMLReaders.
 *
 * Revision 1.2  2005/02/05 11:33:33  pdoubleya
 * Added load() to XMLResource, and accept overloaded input: InputSource, stream, URL.
 *
 * Revision 1.1  2005/02/03 20:39:35  pdoubleya
 * Added to CVS.
 *
 *
 */