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

package org.xhtmlrenderer.swing;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.xpath.XPathAPI;

import org.xhtmlrenderer.DefaultCSSMarker;

/**
 * Handles a general XML document
 *
 * @author  Torbjörn Gannholm
 */
public class NoNamespaceHandler implements org.xhtmlrenderer.extend.NamespaceHandler {
    
    static final String _namespace = "http://www.w3.org/XML/1998/namespace";
        
    public String getNamespace() {
        return _namespace;
    }

    /*
    public String getClass(org.w3c.dom.Element e) {
System.err.println("NoNamespace class!");
        return null;
    }
    
    public String getElementStyling(org.w3c.dom.Element e) {
         return null;
    }
    
    public String getID(org.w3c.dom.Element e) {
        return null;
    }
    
    public String getLang(org.w3c.dom.Element e) {
        return e.getAttributeNS(_namespace, "lang");
    }
    */

    public String getDocumentTitle(org.w3c.dom.Document doc) {
        return null;
    }    
    
    public String getInlineStyle(org.w3c.dom.Document doc) {
        return null;
    }
    
    public String[] getStylesheetURIs(org.w3c.dom.Document doc) {
        java.util.List list = new java.util.ArrayList();
        //get the processing-instructions (actually for XmlDocuments)
        try {
            org.w3c.dom.NodeList nl = XPathAPI.selectNodeList(doc.getDocumentElement(), "//processing-instruction('xml-stylesheet')");
            for ( int i=0, len=nl.getLength(); i < len; i++ ) {
                org.w3c.dom.Node piNode = nl.item(i);
                String pi = piNode.getNodeValue();
                String s = pi.substring(pi.indexOf("type=")+5);
                String type = s.substring(1, s.indexOf(s.charAt(0),1));
                if(type.equals("text/css")) {
                    s = pi.substring(pi.indexOf("href=")+5);
                    String href = s.substring(1, s.indexOf(s.charAt(0),1));
                list.add(href);
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();   
        }
        
        String[] uris = new String[list.size()];
        for(int i=0; i<uris.length; i++) {
            uris[i] = (String) list.get(i);
        }
        return uris;
    }
    
    /* Not a good idea. NO good way of witing a selector. Use UserAgent defaults and css defaults
    public java.io.Reader getDefaultStylesheet() {
        java.io.Reader reader = null;
        try {

            //Object marker = new org.xhtmlrenderer.DefaultCSSMarker();
            
            //if(marker.getClass().getResourceAsStream("default.css") != null) {
            if(this.getClass().getResourceAsStream("nonamespace.css") != null) {

            //reader = new java.io.InputStreamReader(marker.getClass().getResource("default.css").openStream());
            reader = new java.io.InputStreamReader(this.getClass().getResource("nonamespace.css").openStream());
            } else {
                System.err.println("Could not find css for "+this.getClass().getName());
            }

        } catch (Exception ex) {

            ex.printStackTrace();

        }
        
        return reader;

    }*/
    
    public java.io.Reader getDefaultStylesheet() {
        return null;
    }
    
}
