/*
 *
 * XhtmlDocument.java
 * Copyright (c) 2004 Torbjörn Gannholm
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
 *
 */

package net.homelinux.tobe.renderer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.xpath.XPathAPI;

import org.xhtmlrenderer.DefaultCSSMarker;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.InputSource;

import net.homelinux.tobe.renderer.builder.DocumentProcessor;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class XRDocument implements net.homelinux.tobe.xhtmlrenderer.AttributeResolver {
    
    private UserAgentCallback _ua;
    private org.w3c.dom.Document _doc;
    private java.net.URI _uri;
    private java.util.HashMap _nsHandlers = new java.util.HashMap();
    
    /** Creates a new instance of XhtmlDocument */
    public XRDocument(UserAgentCallback ua, java.io.InputStream is, java.net.URI uri) {
        _ua = ua;
        _uri = uri;
        
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setNamespaceAware(true);
        
        try{
            DocumentBuilder builder = fact.newDocumentBuilder();
            
        

        //builder.setErrorHandler(error_handler);

            //_doc = builder.parse(is);
            _doc = builder.newDocument();
            XMLReader xmlReader = XMLReaderFactory.createXMLReader( "org.apache.crimson.parser.XMLReaderImpl" );
            xmlReader.setContentHandler(new DocumentProcessor(this));
            xmlReader.parse(new InputSource(is));
        }
        catch(javax.xml.parsers.ParserConfigurationException e) {
            e.printStackTrace();
        }
        catch(org.xml.sax.SAXException e) {
            e.printStackTrace();
        }
        catch(java.io.IOException e) {
            e.printStackTrace();
        }
    }
    
    public java.net.URI getURI() {
        return _uri;
    }
    
    public org.w3c.dom.Document getDomDocument() {
        return _doc;
    }

    public String getDocumentTitle() {
        String title = "";
        title = getNsHandler(_doc.getDocumentElement().getNamespaceURI()).getDocumentTitle(_doc);
        if(title == null) title = _uri.toString();
        return title;
    }    
    
    /** @deprecated, work out a better plan to cache default stylesheets */
    public java.net.URI getNamespace() {
        java.net.URI nsUri = null;
        try {
            nsUri = new java.net.URI(_doc.getDocumentElement().getNamespaceURI());
        }
        catch(java.net.URISyntaxException e) {
            e.printStackTrace();
        }
        return nsUri;
    }
    
    private NamespaceHandler getNsHandler(String ns) {
        NamespaceHandler nsh = (NamespaceHandler) _nsHandlers.get(ns);
        if(nsh == null) {
            nsh = _ua.getNamespaceHandler(ns);
            _nsHandlers.put(ns, nsh);
        }
        return nsh;
    }
    
    //following three should be private
    
    public String getInlineStyle() {
        return getNsHandler(_doc.getDocumentElement().getNamespaceURI()).getInlineStyle(_doc);
    }
    
    public java.net.URI[] getStylesheetURIs() {
        return getNsHandler(_doc.getDocumentElement().getNamespaceURI()).getStylesheetURIs(_doc);
    }
    
    public java.io.Reader getDefaultStylesheet() {
        
        return getNsHandler(_doc.getDocumentElement().getNamespaceURI()).getDefaultStylesheet();

    }
    
    //the AttributeResolver interface
    
    public String getClass(org.w3c.dom.Element e) {
        return getNsHandler(e.getNamespaceURI()).getClass(e);
    }
    
    public String getElementStyling(org.w3c.dom.Element e) {
        return getNsHandler(e.getNamespaceURI()).getElementStyling(e);
   }
    
    public String getID(org.w3c.dom.Element e) {
        return getNsHandler(e.getNamespaceURI()).getID(e);
    }
    
    public String getLang(org.w3c.dom.Element e) {
        return getNsHandler(e.getNamespaceURI()).getLang(e);
    }
    
    /** This is probably unnecessary! */
    public String getAttributeValue(org.w3c.dom.Element e, String attrName) {
        return e.getAttribute(attrName);
    }
    
    /** find a good way to implement this later */
    public boolean isPseudoClass(org.w3c.dom.Element e, int pc) {
        return false;
    }
    
}
