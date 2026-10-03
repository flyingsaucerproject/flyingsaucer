package org.xhtmlrenderer.demo.browser;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Read plain text file as if it was xml with a text-tag around it.
 * <p>
 * Fulfills minimum requirements.
 * <p>
 * Maybe not the easiest way to do this :-)
 */
public class PlainTextXMLReader implements XMLReader {
    private EntityResolver entityResolver;
    private DTDHandler dtdHandler;
    private ContentHandler contentHandler;
    private ErrorHandler errorHandler;
    private final BufferedReader text;

    public PlainTextXMLReader(InputStream is) {
        text = new BufferedReader(new InputStreamReader(is, UTF_8));
    }

    @Override
    public boolean getFeature(String s) throws SAXNotRecognizedException {
        if (s.equals("http://xml.org/sax/features/namespaces")) {
            return true;
        }
        if (s.equals("http://xml.org/sax/features/namespace-prefixes")) {
            return false;
        }
        throw new SAXNotRecognizedException(s);
    }

    @Override
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

    @Override
    public Object getProperty(String s) throws SAXNotRecognizedException {
        throw new SAXNotRecognizedException(s);
    }

    @Override
    public void setProperty(String s, Object o) throws SAXNotRecognizedException {
        throw new SAXNotRecognizedException(s);
    }

    @Override
    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    @Override
    public void setDTDHandler(DTDHandler dtdHandler) {
        this.dtdHandler = dtdHandler;
    }

    @Override
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    @Override
    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
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

    @Override
    public void parse(String s) throws SAXException {
        throw new SAXNotRecognizedException(s);
    }
}
