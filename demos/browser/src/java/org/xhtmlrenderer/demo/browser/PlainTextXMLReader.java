package org.xhtmlrenderer.demo.browser;

import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Read plain text file as if it was xml with a text-tag around it.
 * <p/>
 * Fulfills minimum requirements.
 * <p/>
 * Maybe not the easiest way to do this :-)
 */
public class PlainTextXMLReader implements XMLReader {
    private EntityResolver entityResolver;
    private DTDHandler dtdHandler;
    private ContentHandler contentHandler;
    private ErrorHandler errorHandler;
    private BufferedReader text;

    public PlainTextXMLReader(InputStream is) {
        text = new BufferedReader(new InputStreamReader(is));
    }

    public boolean getFeature(String s) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (s.equals("http://xml.org/sax/features/namespaces")) {
            return true;
        }
        if (s.equals("http://xml.org/sax/features/namespace-prefixes")) {
            return false;
        }
        throw new SAXNotRecognizedException(s);
    }

    public void setFeature(String s, boolean b) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (s.equals("http://xml.org/sax/features/namespaces")) {
            if (!b)
                throw new SAXNotSupportedException(s);
            else
                return;
        }
        if (s.equals("http://xml.org/sax/features/namespace-prefixes")) {
            if (b)
                throw new SAXNotSupportedException(s);
            else
                return;
        }
        throw new SAXNotRecognizedException(s);
    }

    public Object getProperty(String s) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotRecognizedException(s);
    }

    public void setProperty(String s, Object o) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotRecognizedException(s);
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    public void setDTDHandler(DTDHandler dtdHandler) {
        this.dtdHandler = dtdHandler;
    }

    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void parse(InputSource inputSource) throws IOException, SAXException {
        contentHandler.startDocument();
        contentHandler.startElement("http://www.w3.org/1999/xhtml", "pre", "pre", new AttributesImpl());

        String line;
        do {
            line = text.readLine();
            if (line == null) break;
            char[] chars = (line + "\n").toCharArray();
            contentHandler.characters(chars, 0, chars.length);
        } while (line != null);

        contentHandler.endElement("http://www.w3.org/1999/xhtml", "pre", "pre");
        contentHandler.endDocument();
    }

    public void parse(String s) throws IOException, SAXException {
        throw new SAXNotRecognizedException(s);
    }
}
