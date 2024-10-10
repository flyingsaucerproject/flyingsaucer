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

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import static javax.xml.parsers.SAXParserFactory.newInstance;


/**
 * @author Patrick Wright
 */
public class XMLResource extends AbstractResource {
    private static final XMLResourceBuilder XML_RESOURCE_BUILDER = new XMLResourceBuilder();
    private static final AtomicBoolean useConfiguredParser = new AtomicBoolean(true);

    private Document document;

    private XMLResource(InputStream stream) {
        super(stream);
    }

    private XMLResource(@Nullable InputSource source) {
        super(source);
    }

    public static XMLResource load(URL source) {
        return load(new InputSource(source.toString()));
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

    public static XMLResource load(String xml) {
        return load(new StringReader(xml));
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

    public static XMLReader newXMLReader() {
        XMLReader xmlReader = null;
        String xmlReaderClass = Configuration.valueFor("xr.load.xml-reader");

        //TODO: if it doesn't find the parser, note that in a static boolean - otherwise
        // you get exceptions on every load
        if (useConfiguredParser.get()) {
            try {
                if (xmlReaderClass != null && !xmlReaderClass.equalsIgnoreCase("default")) {
                    try {
                        Class<?> readerClass = Class.forName(xmlReaderClass);
                        xmlReader = (XMLReader) readerClass.getDeclaredConstructor().newInstance();
                    } catch (Exception ex) {
                        useConfiguredParser.set(false);
                        XRLog.load(Level.SEVERE, """
                                The XMLReader class could not be found: '%s'.
                                Caused by: %s.
                                Falling back to JDK default xml reader.
                                Hint: Use value 'default' in FS configuration if necessary.
                                """.formatted(xmlReaderClass, ex));
                    }
                }
            } catch (Exception ex) {
                XRLog.load(Level.WARNING,
                        "Could not instantiate custom XMLReader class for XML parsing: "
                                + xmlReaderClass + ". Please check classpath. Use value 'default' in " +
                                "FS configuration if necessary. Will now try JDK default.", ex);
            }
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
                xmlReader = newInstance().newSAXParser().getXMLReader();
            } catch (Exception ex) {
                XRLog.general(ex.getMessage());
            }
        }
        if (xmlReader == null) {
            try {
                XRLog.load(Level.WARNING, "falling back on the default parser");
                SAXParser parser = newInstance().newSAXParser();
                xmlReader = parser.getXMLReader();
            } catch (Exception ex) {
                XRLog.general(Level.WARNING, ex.getMessage(), ex);
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

        private final XMLReaderPool parserPool = new XMLReaderPool();
        private final IdentityTransformerPool transformerPool = new IdentityTransformerPool();

        private XMLResource createXMLResource(XMLResource target) {
            long start = System.currentTimeMillis();
            Document document = parse(target);
            long end = System.currentTimeMillis();

            target.setElapsedLoadTime(end - start);
            XRLog.load("Loaded document in ~" + target.getElapsedLoadTime() + "ms");

            target.setDocument(document);
            return target;
        }

        private Document parse(XMLResource target) {
            XMLReader xmlReader = parserPool.get();
            try {
                return transform(new SAXSource(xmlReader, target.getResourceInputSource()));
            } finally {
                parserPool.release(xmlReader);
            }
        }

        XMLResource createXMLResource(Source source) {
            Document document;

            long st = System.currentTimeMillis();

            document = transform(source);

            long end = System.currentTimeMillis();

            //HACK: should rather use a default constructor
            XMLResource target = new XMLResource((InputSource) null);

            target.setElapsedLoadTime(end - st);

            XRLog.load("Loaded document in ~" + target.getElapsedLoadTime() + "ms");

            target.setDocument(document);
            return target;
        }

        private Document transform(Source source) {
            DOMResult result = new DOMResult();
            Transformer idTransform = transformerPool.get();
            try {
                idTransform.transform(source, result);
            } catch (Exception ex) {
                throw new XRRuntimeException("Can't load the XML resource (using TrAX transformer). " + ex.getMessage(), ex);
            } finally {
                transformerPool.release(idTransform);
            }
            return (Document) result.getNode();
        }

    }


    private static class XMLReaderPool extends ObjectPool<XMLReader> {

        private final boolean preserveElementContentWhitespace = Configuration
                .isFalse("xr.load.ignore-element-content-whitespace", true);

        private XMLReaderPool() {
            this(Configuration.valueAsInt("xr.load.parser-pool-capacity", 3));
        }

        private XMLReaderPool(int capacity) {
            super(capacity);
        }

        @Override
        protected XMLReader newValue() {
            XMLReader xmlReader = newXMLReader();
            if (preserveElementContentWhitespace) {
                xmlReader = new WhitespacePreservingFilter(xmlReader);
            }
            addHandlers(xmlReader);
            setParserFeatures(xmlReader);
            return xmlReader;
        }

        /**
         * Adds the default EntityResolved and ErrorHandler for the DOM parser.
         */
        private void addHandlers(XMLReader xmlReader) {
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

            // util
            setFeature(xmlReader, "http://xml.org/sax/features/use-entity-resolver2", true);
            setFeature(xmlReader, "http://xml.org/sax/features/xmlns-uris", true);
        }

        /**
         * Attempts to set requested feature on the parser; logs exception if not supported
         * or not recognized.
         */
        private void setFeature(XMLReader xmlReader, String featureUri, String configName) {
            setFeature(xmlReader, featureUri, Configuration.isTrue(configName, false));
        }

        private void setFeature(XMLReader xmlReader, String featureUri, boolean value) {
            try {
                xmlReader.setFeature(featureUri, value);

                XRLog.load(Level.FINE, "SAX Parser feature: " +
                        featureUri.substring(featureUri.lastIndexOf('/')) +
                        " set to " +
                        xmlReader.getFeature(featureUri));
            } catch (SAXNotSupportedException ex) {
                XRLog.load(Level.WARNING, "SAX feature not supported on this XMLReader: " + featureUri, ex);
            } catch (SAXNotRecognizedException ex) {
                XRLog.load(Level.WARNING, "SAX feature not recognized on this XMLReader: " + featureUri +
                        ". Feature may be properly named, but not recognized by this parser.", ex);
            }
        }

    }


    private static class WhitespacePreservingFilter
            extends XMLFilterImpl implements EntityResolver2 {

        private WhitespacePreservingFilter(XMLReader parent) {
            super(parent);
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            getContentHandler().characters(ch, start, length);
        }

        @Override
        @Nullable
        public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
            EntityResolver resolver = getEntityResolver();
            if (resolver instanceof EntityResolver2) {
                return ((EntityResolver2) resolver).getExternalSubset(name, baseURI);
            }
            return null;
        }

        @Override
        public InputSource resolveEntity(String name,
                                         String publicId,
                                         String baseURI,
                                         String systemId) throws SAXException, IOException {
            EntityResolver resolver = getEntityResolver();
            if (resolver instanceof EntityResolver2) {
                return ((EntityResolver2) resolver)
                        .resolveEntity(name, publicId, baseURI, systemId);
            }
            return resolveEntity(publicId, systemId);
        }
    }

    private static class IdentityTransformerPool extends ObjectPool<Transformer> {
        private final TransformerFactory transformerFactory;
        private IdentityTransformerPool(int capacity) {
            super(capacity);
            TransformerFactory tf = TransformerFactory.newInstance();
            try {
                tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (TransformerConfigurationException e) {
                XRLog.init(Level.WARNING, "Problem configuring TrAX factory", e);
            }
            this.transformerFactory = tf;
        }

        private IdentityTransformerPool() {
            this(Configuration.valueAsInt("xr.load.parser-pool-capacity", 3));
        }

        @Override
        protected Transformer newValue() {
            try {
                return transformerFactory.newTransformer();
            } catch (TransformerConfigurationException ex) {
                throw new XRRuntimeException("Failed on configuring TrAX transformer.", ex);
            }
        }
    }


    private static abstract class ObjectPool<T> {
        private final Queue<Reference<T>> pool;

        private ObjectPool(int capacity) {
            pool = new ArrayBlockingQueue<>(capacity);
        }

        protected abstract T newValue();

        T get() {
            T obj = null;
            Reference<T> ref = pool.poll();
            if (ref != null) {
                obj = ref.get();
            }

            if (obj == null) {
                obj = newValue();
            }
            return obj;
        }

        void release(T obj) {
            pool.offer(new SoftReference<>(obj));
        }
    }
}
