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

package net.homelinux.tobe;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.xpath.XPathAPI;

/**
 * Handles xhtml documents
 *
 * @author  Torbjörn Gannholm
 */
public class XhtmlNamespaceHandler extends NoNamespaceHandler {
    
    static final String _namespace = "http://www.w3.org/1999/xhtml";
       
    public String getNamespace() {
        return _namespace;
    }
     
    public String getClass(org.w3c.dom.Element e) {
        return e.getAttribute("class");
    }
    
    public String getElementStyling(org.w3c.dom.Element e) {
         return e.getAttribute("style");
    }
    
    public String getID(org.w3c.dom.Element e) {
        return e.getAttribute("id");
    }
    
    public String getLang(org.w3c.dom.Element e) {
        String lang = e.getAttribute("lang");
        if(lang == null || lang.equals("")) lang = super.getLang(e);
        return lang;
    }
    
    //xpath needs prefix for element when namespace-aware
    org.apache.xml.utils.PrefixResolver pres = new org.apache.xml.utils.PrefixResolver() {
      public java.lang.String getNamespaceForPrefix(java.lang.String prefix) {
          return _namespace;
      }
      
      public java.lang.String getNamespaceForPrefix(java.lang.String prefix,
                                              org.w3c.dom.Node context) {
          return _namespace;
      }
      
      public java.lang.String getBaseIdentifier() { return null; }
      
      public boolean handlesNullPrefixes() {return true; }
    };

    public String getDocumentTitle(org.w3c.dom.Document doc) {
        String title = "";
        try {
            org.apache.xpath.objects.XObject xo = XPathAPI.eval(doc.getDocumentElement(), "//xh:head/xh:title/text()", pres);
            org.w3c.dom.NodeList nl = xo.nodelist();
            if ( nl.getLength() == 0 ) { 
                System.err.println("Apparently no title element for this document.");
                title = "TITLE UNKNOWN";
            } else {
                title = nl.item(0).getNodeValue();
            }
        } catch ( Exception ex ) {
            System.err.println("Error retrieving document title. " + ex.getMessage());
            title = "";
        }
        return title;
    }    
    
    public String getInlineStyle(org.w3c.dom.Document doc) {
        StringBuffer style = new StringBuffer();
        try {
            org.apache.xpath.objects.XObject xo = XPathAPI.eval(doc.getDocumentElement(), "//xh:style[@type='text/css']", pres);
            org.w3c.dom.NodeList nl = xo.nodelist();
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
    
    public java.net.URI[] getStylesheetURIs(org.w3c.dom.Document doc) {
        java.util.List list = new java.util.ArrayList();
        //get the processing-instructions (actually for XmlDocuments)
        java.net.URI[] pis = super.getStylesheetURIs(doc);
        list.addAll(java.util.Arrays.asList(pis));
        //get the link elements
        try {
            //this namespace handling is horrible!
            org.apache.xpath.objects.XObject xo = XPathAPI.eval(doc.getDocumentElement(), "//xh:link[@type='text/css']/@xh:href", pres);
            org.w3c.dom.NodeList nl = xo.nodelist();
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
    
    public java.io.Reader getDefaultStylesheet() {
        java.io.Reader reader = null;
        try {

            //Object marker = new DefaultCSSMarker();
            
            if(this.getClass().getResourceAsStream("default.css") != null) {

            reader = new java.io.InputStreamReader(this.getClass().getResource("default.css").openStream());
            } else {
                System.err.println("Could not find css for "+this.getClass().getName());
            }

        } catch (Exception ex) {

            ex.printStackTrace();

        }
        
        return reader;

    }
    
}
