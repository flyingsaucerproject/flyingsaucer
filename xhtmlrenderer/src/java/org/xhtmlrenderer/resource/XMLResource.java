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
import java.io.Reader;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
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
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * @author Patrick Wright
 */
public class XMLResource extends AbstractResource {
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

    private XMLResource(InputSource source) {
        super(source);
    }

    public static XMLResource load(InputStream stream) {
        return XML_RESOURCE_BUILDER.createXMLResource(new XMLResource(stream));
    }

    public static XMLResource load(InputSource source) {
        return XML_RESOURCE_BUILDER.createXMLResource(new XMLResource(source));
    }

    public static XMLResource load(Reader reader) {
        return XML_RESOURCE_BUILDER.createXMLResource(new XMLResource(new InputSource(reader)));
    }

    public static XMLResource load(Source source) {
        return XML_RESOURCE_BUILDER.createXMLResource(source);
    }

    public Document getDocument() {
        return document;
    }

    /*package*/
    void setDocument(Document document) {
        this.document = document;
    }

    public static final XMLReader newXMLReader() {
        XMLReader xmlReader = null;
        String xmlReaderClass = Configuration.valueFor("xr.load.xml-reader");
        
        //TODO: if it doesn't find the parser, note that in a static boolean--otherwise
        // you get exceptions on every load
        try {
            if (xmlReaderClass != null &&
                    !xmlReaderClass.toLowerCase().equals("default") &&
                    XMLResource.useConfiguredParser) {
                try {
                    Class.forName(xmlReaderClass);
                } catch (Exception ex) {
                    XMLResource.useConfiguredParser = false;
                    XRLog.load(Level.WARNING,
                            "The XMLReader class you specified as a configuration property " +
                            "could not be found. Class.forName() failed on "
                            + xmlReaderClass + ". Please check classpath. Use value 'default' in " +
                            "FS configuration if necessary. Will now try JDK default.");
                }
                if (XMLResource.useConfiguredParser) {
                    xmlReader = XMLReaderFactory.createXMLReader(xmlReaderClass);
                }
            }
        } catch (Exception ex) {
            XRLog.load(Level.WARNING,
                    "Could not instantiate custom XMLReader class for XML parsing: "
                    + xmlReaderClass + ". Please check classpath. Use value 'default' in " +
                    "FS configuration if necessary. Will now try JDK default.", ex);
        }
        if (xmlReader == null) {
            try {
                // JDK default
                // HACK: if
                /*CHECK: does this code do anything?
                if (System.getProperty("org.xml.sax.driver") == null) {
                    String newDefault = "org.apache.crimson.parser.XMLReaderImpl";
                    XRLog.load(Level.WARNING,
                            "No value for system property 'org.xml.sax.driver'.");
                }
                */
                xmlReader = XMLReaderFactory.createXMLReader();
                xmlReaderClass = "{JDK default}";
            } catch (Exception ex) {
                XRLog.general(ex.getMessage());
            }
        }
        if (xmlReader == null) {
            try {
                XRLog.load(Level.WARNING, "falling back on the default parser");
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                xmlReader = parser.getXMLReader();
                xmlReaderClass = "SAXParserFactory default";
            } catch (Exception ex) {
                XRLog.general(ex.getMessage());
            }
        }
        if (xmlReader == null) {
            throw new XRRuntimeException("Could not instantiate any SAX 2 parser, including JDK default. " +
                    "The name of the class to use should have been read from the org.xml.sax.driver System " +
                    "property, which is set to: "/*CHECK: is this meaningful? + System.getProperty("org.xml.sax.driver")*/);
        }
        XRLog.load("SAX XMLReader in use (parser): " + xmlReader.getClass().getName());
        return xmlReader;
    }

    private static class XMLResourceBuilder {
        XMLResource createXMLResource(XMLResource target) {
            Source input = null;
            DOMResult output = null;
            TransformerFactory xformFactory = null;
            Transformer idTransform = null;
            XMLReader xmlReader = null;
            long st = 0L;

            xmlReader = XMLResource.newXMLReader();
            addHandlers(xmlReader);
            setParserFeatures(xmlReader);

            st = System.currentTimeMillis();
            try {
                input = new SAXSource(xmlReader, target.getResourceInputSource());
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                dbf.setValidating(false);//validation is the root of all evil in xml - tobe
                output = new DOMResult(dbf.newDocumentBuilder().newDocument());
                xformFactory = TransformerFactory.newInstance();
                idTransform = xformFactory.newTransformer();
            } catch (Exception ex) {
                throw new XRRuntimeException(
                        "Failed on configuring SAX to DOM transformer.", ex);
            }

            try {
                idTransform.transform(input, output);
            } catch (Exception ex) {
                throw new XRRuntimeException(
                        "Can't load the XML resource (using TRaX transformer). " + ex.getMessage(), ex);
            }

            long end = System.currentTimeMillis();

            target.setElapsedLoadTime(end - st);

            XRLog.load("Loaded document in ~" + target.getElapsedLoadTime() + "ms");

            target.setDocument((Document) output.getNode());
            return target;
        }

        /**
         * Adds the default EntityResolved and ErrorHandler for the SAX parser.
         */
        private void addHandlers(XMLReader xmlReader) {
            try {
                // add our own entity resolver
                xmlReader.setEntityResolver(FSEntityResolver.instance());
                xmlReader.setErrorHandler(new ErrorHandler() {

                    public void error(SAXParseException ex) {
                        XRLog.load(ex.getMessage());
                    }

                    public void fatalError(SAXParseException ex) {
                        XRLog.load(ex.getMessage());
                    }

                    public void warning(SAXParseException ex) {
                        XRLog.load(ex.getMessage());
                    }
                });
            } catch (Exception ex) {
                throw new XRRuntimeException("Failed on configuring SAX parser/XMLReader.", ex);
            }
        }

        /**
         * Sets all standard features for SAX parser, using values from Configuration.
         */
        private void setParserFeatures(XMLReader xmlReader) {
            try {        // perf: validation off
                xmlReader.setFeature("http://xml.org/sax/features/validation", false);
                // perf: namespaces
                xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
            } catch (SAXException s) {
                // nothing to do--some parsers will not allow setting features
                XRLog.load(Level.WARNING, "Could not set validation/namespace features for XML parser," +
                        "exception thrown.", s);
            }
            if (Configuration.isFalse("xr.load.configure-features", false)) {
                XRLog.load(Level.FINE, "SAX Parser: by request, not changing any parser features.");
                return;
            }
            
            // perf: validation off
            setFeature(xmlReader, "http://xml.org/sax/features/validation", "xr.load.validation");
            
            // mem: intern strings
            setFeature(xmlReader, "http://xml.org/sax/features/string-interning", "xr.load.string-interning");
            
            // perf: namespaces
            setFeature(xmlReader, "http://xml.org/sax/features/namespaces", "xr.load.namespaces");
            setFeature(xmlReader, "http://xml.org/sax/features/namespace-prefixes", "xr.load.namespace-prefixes");
        }

        /**
         * Attempts to set requested feature on the parser; logs exception if not supported
         * or not recognized.
         */
        private void setFeature(XMLReader xmlReader, String featureUri, String configName) {
            try {
                xmlReader.setFeature(featureUri, Configuration.isTrue(configName, false));

                XRLog.load(Level.FINE, "SAX Parser feature: " +
                        featureUri.substring(featureUri.lastIndexOf("/")) +
                        " set to " +
                        xmlReader.getFeature(featureUri));
            } catch (SAXNotSupportedException ex) {
                XRLog.load(Level.WARNING, "SAX feature not supported on this XMLReader: " + featureUri);
            } catch (SAXNotRecognizedException ex) {
                XRLog.load(Level.WARNING, "SAX feature not recognized on this XMLReader: " + featureUri +
                        ". Feature may be properly named, but not recognized by this parser.");
            }
        }

        public XMLResource createXMLResource(Source source) {
            DOMResult output = null;
            TransformerFactory xformFactory = null;
            Transformer idTransform = null;
            long st = 0L;

            st = System.currentTimeMillis();
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                dbf.setValidating(false);//validation is the root of all evil in xml - tobe
                output = new DOMResult(dbf.newDocumentBuilder().newDocument());
                xformFactory = TransformerFactory.newInstance();
                idTransform = xformFactory.newTransformer();
            } catch (Exception ex) {
                throw new XRRuntimeException("Failed on configuring SAX to DOM transformer.", ex);
            }

            try {
                idTransform.transform(source, output);
            } catch (Exception ex) {
                throw new XRRuntimeException("Can't load the XML resource (using TRaX transformer). " + ex.getMessage(), ex);
            }

            long end = System.currentTimeMillis();

            //HACK: should rather use a default constructor
            XMLResource target = new XMLResource((InputSource) null);

            target.setElapsedLoadTime(end - st);

            XRLog.load("Loaded document in ~" + target.getElapsedLoadTime() + "ms");

            target.setDocument((Document) output.getNode());
            return target;
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.20  2007/05/15 22:01:42  peterbrant
 * Remove unused code
 *
 * Revision 1.19  2006/07/26 18:09:42  pdoubleya
 * Clean exception throws.
 *
 * Revision 1.18  2006/02/02 02:47:36  peterbrant
 * Support non-AWT images
 *
 * Revision 1.17  2005/10/22 00:09:18  peterbrant
 * Rollback to 1.15
 *
 * Revision 1.15  2005/07/02 09:40:24  tobega
 * More robust parsing
 *
 * Revision 1.14  2005/06/26 01:02:21  tobega
 * Now checking for SecurityException on System.getProperty
 *
 * Revision 1.13  2005/06/25 22:16:23  tobega
 * Browser now handles both plain text files and images
 *
 * Revision 1.12  2005/06/15 10:56:14  tobega
 * cleaned up a bit of URL mess, centralizing URI-resolution and loading to UserAgentCallback
 *
 * Revision 1.11  2005/06/13 06:50:16  tobega
 * Fixed a bug in table content resolution.
 * Various "tweaks" in other stuff.
 *
 * Revision 1.10  2005/06/01 21:36:41  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.9  2005/04/20 19:13:18  tobega
 * Fixed vertical align. Middle works and all look pretty much like in firefox
 *
 * Revision 1.8  2005/04/03 21:51:31  joshy
 * fixed code that gets the XMLReader on the mac
 * added isMacOSX() to GeneralUtil
 * added app name and single menu bar to browser
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2005/03/28 18:33:03  pdoubleya
 * Don't show stack trace if XML can't be loaded.
 *
 * Revision 1.6  2005/03/22 15:34:23  pdoubleya
 * Changed to use XMLReaderFactory, appears to solve namespaces issue (thanks to Elliot Rusty Harold, again!).
 *
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