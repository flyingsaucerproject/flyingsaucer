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

import org.joshy.html.css.DefaultCSSMarker;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class XhtmlDocument implements Document {
    
    private org.w3c.dom.Document _doc;
    private java.net.URI _uri;
    private java.net.URI _namespace;
    
    /** Creates a new instance of XhtmlDocument */
    public XhtmlDocument(java.io.InputStream is, java.net.URI uri) {
        _uri = uri;
        
        try{
            _namespace = new java.net.URI("http://www.w3.org/1999/xhtml");
        }
        catch(java.net.URISyntaxException e) {
            e.printStackTrace();
        }
        
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();

        try{
            DocumentBuilder builder = fact.newDocumentBuilder();
        

        //builder.setErrorHandler(error_handler);

            _doc = builder.parse(is);
        }
        catch(javax.xml.parsers.ParserConfigurationException e) {
            
        }
        catch(org.xml.sax.SAXException e) {
            
        }
        catch(java.io.IOException e) {
            
        }
   }
    
    public String getClass(org.w3c.dom.Element e) {
        return e.getAttribute("class");
    }
    
    public org.w3c.dom.Document getDomDocument() {
        return _doc;
    }
    
    public String getElementStyling(org.w3c.dom.Element e) {
         return e.getAttribute("style");
   }
    
    public String getID(org.w3c.dom.Element e) {
        return e.getAttribute("id");
    }
    
    public String getInlineStyle() {
        StringBuffer style = new StringBuffer();
        try {
            org.w3c.dom.NodeList nl = XPathAPI.selectNodeList(_doc.getDocumentElement(), "//style[@type='text/css']");
            for ( int i=0, len=nl.getLength(); i < len; i++ ) {
                org.w3c.dom.Node elem = nl.item(i);
                org.w3c.dom.NodeList children = elem.getChildNodes();
                for(int j=0; j < children.getLength(); j++) {
                    org.w3c.dom.Node txt = children.item(j);
                    if(txt.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        style.append(txt.getNodeValue());
                    }
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();   
        }
        
        return style.toString();
    }
    
    public String getLang(org.w3c.dom.Element e) {
        return e.getAttribute("lang");
    }
    
    public java.net.URI[] getStylesheetURIs() {
        java.util.List list = new java.util.ArrayList();
        //get the processing-instructions (actually for XmlDocuments)
        try {
            org.w3c.dom.NodeList nl = XPathAPI.selectNodeList(_doc.getDocumentElement(), "//processing-instruction('xml-stylesheet')");
            for ( int i=0, len=nl.getLength(); i < len; i++ ) {
                org.w3c.dom.Node piNode = nl.item(i);
                String pi = piNode.getNodeValue();
                String s = pi.substring(pi.indexOf("type=")+5);
                String type = s.substring(1, s.indexOf(s.charAt(0),1));
                if(type.equals("text/css")) {
                    s = pi.substring(pi.indexOf("href=")+5);
                    String href = s.substring(1, s.indexOf(s.charAt(0),1));
                list.add(new java.net.URI(href));
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();   
        }
        //get the link elements
        try {
            org.w3c.dom.NodeList nl = XPathAPI.selectNodeList(_doc.getDocumentElement(), "//link[@type='text/css']/@href");
            for ( int i=0, len=nl.getLength(); i < len; i++ ) {
                org.w3c.dom.Node hrefNode = nl.item(i);
                String href = hrefNode.getNodeValue();
                list.add(new java.net.URI(href));
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();   
        }
        
        java.net.URI[] uris = new java.net.URI[list.size()];
        for(int i=0; i<uris.length; i++) {
            uris[i] = (java.net.URI) list.get(i);
        }
        return uris;
    }
    
    public java.net.URI getURI() {
        return _uri;
    }
    
    public java.io.Reader getDefaultStylesheet() {
        
        java.io.Reader reader = null;
        try {

            Object marker = new DefaultCSSMarker();
            
            if(marker.getClass().getResourceAsStream("default.css") != null) {

            reader = new java.io.InputStreamReader(marker.getClass().getResource("default.css").openStream());
            }

        } catch (Exception ex) {

            ex.printStackTrace();

        }
        
        return reader;

    }
    
    public java.net.URI getNamespace() {
        return _namespace;
    }
    
}
