/*
 * {{{ header & license
 * Flying Saucer - Copyright (c) 2024
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.net.URL;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.xml.transform.TransformerException;

/**
 * HTMLResource uses JSoup to parse XML resources.
 */
@ParametersAreNonnullByDefault
public class HTMLResource extends AbstractResource {
    private org.w3c.dom.Document document;

    private HTMLResource(InputStream stream) {
        super(stream);
        try {
            Document jsoupDoc = Jsoup.parse(stream, StandardCharsets.UTF_8.name(), "", Parser.xmlParser());
            this.document = convertJsoupToW3CDocument(jsoupDoc);
        } catch (IOException | ParserConfigurationException | TransformerException | SAXException e) {
            XRLog.load(java.util.logging.Level.SEVERE, "Failed to parse and convert HTML document.", e);
            throw new XRRuntimeException("Failed to parse and convert HTML document.", e);
        }
    }

    public static HTMLResource load(URL source) {
        try (InputStream stream = source.openStream()) {
            return new HTMLResource(stream);
        } catch (IOException e) {
            XRLog.load(java.util.logging.Level.SEVERE, "Failed to load HTML from URL.", e);
            throw new XRRuntimeException("Failed to load HTML from URL.", e);
        }
    }

    public static HTMLResource load(InputStream stream) {
        return new HTMLResource(stream);
    }

    public static HTMLResource load(Reader reader) {
        try {
            InputStream stream = convertReaderToInputStream(reader);
            return new HTMLResource(stream);
        } catch (IOException e) {
            XRLog.load(java.util.logging.Level.SEVERE, "Failed to load HTML from Reader.", e);
            throw new XRRuntimeException("Failed to load HTML from Reader.", e);
        }
    }

    public static HTMLResource load(String xml) {
        return load(new StringReader(xml));
    }

    public org.w3c.dom.Document getDocument() {
        return document;
    }

    private static org.w3c.dom.Document convertJsoupToW3CDocument(Document jsoupDoc) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        String html = jsoupDoc.outerHtml();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
    }

    private static InputStream convertReaderToInputStream(Reader reader) throws IOException {
        char[] charBuffer = new char[8 * 1024];
        StringBuilder builder = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = reader.read(charBuffer, 0, charBuffer.length)) != -1) {
            builder.append(charBuffer, 0, numCharsRead);
        }
        return new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8));
    }
}