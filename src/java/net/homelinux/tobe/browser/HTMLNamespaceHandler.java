/*
 * HTMLNamespaceHandler.java
 *
 * Created on den 15 oktober 2004, 20:20
 */

package net.homelinux.tobe.browser;
import net.homelinux.tobe.XhtmlNamespaceHandler;

import org.apache.xpath.XPathAPI;

/**
 *
 * @author  Torbjörn Gannholm
 *
 * A quick and dirty proof-of-concept. There are more difficulties in HTML
 */
public class HTMLNamespaceHandler extends XhtmlNamespaceHandler {
    
    public String getElementStyling(org.w3c.dom.Element e) {
        StringBuffer sb = new StringBuffer();
        String s = e.getAttribute("style").toLowerCase();
        if(!s.equals("")) {
            sb.append(s);
            if(!s.endsWith(";")) sb.append(";");
        }
        s = e.getAttribute("align");
        if(e.getTagName().equals("img")) {
            if(s.equals("left") || s.equals("right")) {
                sb.append("float: ").append(s).append(";");
            } else if(!s.equals("")) {
                sb.append("vertical-align: ").append(s).append(";");
            }
        } else {
            if(!s.equals("")) {
                sb.append("text-align: ").append(s).append(";");
            }
        }
        s = e.getAttribute("bgcolor");
        if(!s.equals("")) {
            sb.append("background-color: ").append(s).append(";");
        }
        s = e.getAttribute("border");
        if(!s.equals("")) {
            sb.append("border-width: ").append(s).append(";");
        }
        s = e.getAttribute("color");
        if(!s.equals("")) {
            sb.append("color: ").append(s).append(";");
        }
        /*s = e.getAttribute("height");
        if(!s.equals("")) {
            sb.append("height: ").append(s).append(";");
        }*/
        s = e.getAttribute("valign");
        if(!s.equals("")) {
            sb.append("vertical-align: ").append(s).append(";");
        }
        /*s = e.getAttribute("width");
        if(!s.equals("")) {
            sb.append("width: ").append(s).append(";");
        }*/
        //a special one
        if(e.getTagName().equals("center")) {
            sb.append("text-align: center;");
        }
        if(e.getTagName().equals("font")) {
            s = e.getAttribute("size");
            if(!s.equals("")) {
                sb.append("font-size: ");
                if(s.startsWith("+")) {
                    sb.append("larger").append(";");
                } else if(s.startsWith("-")) {
                    sb.append("smaller").append(";");
                } else if(s.equals("1")) {
                    sb.append("xx-small").append(";");
                } else if(s.equals("2")) {//x-small has no equivalent
                    sb.append("small").append(";");
                } else if(s.equals("3")) {
                    sb.append("medium").append(";");
                } else if(s.equals("4")) {
                    sb.append("large").append(";");
                } else if(s.equals("5")) {
                    sb.append("x-large").append(";");
                } else if(s.equals("6")) {
                    sb.append("xx-large").append(";");
                } else if(s.equals("7")) {//7 has no equivalent now
                    sb.append("xx-large").append(";");
                }
            }
            //color is general
            s = e.getAttribute("face");
            if(!s.equals("")) {
                sb.append("font-family: ").append(s).append(";");
            }
        }
        return sb.toString();
    }

    public String getDocumentTitle(org.w3c.dom.Document doc) {
        String title = "";
        try {
            org.apache.xpath.objects.XObject xo = XPathAPI.eval(doc.getDocumentElement(), "//head/title/text()");
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
            org.apache.xpath.objects.XObject xo = XPathAPI.eval(doc.getDocumentElement(), "//style[@type='text/css']");
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
            org.apache.xpath.objects.XObject xo = XPathAPI.eval(doc.getDocumentElement(), "//link[@type='text/css']/@href");
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

            //Object marker = new org.xhtmlrenderer.DefaultCSSMarker();
            
            //if(marker.getClass().getResourceAsStream("default.css") != null) {
            if(this.getClass().getResourceAsStream("html.css") != null) {

            //reader = new java.io.InputStreamReader(marker.getClass().getResource("default.css").openStream());
            reader = new java.io.InputStreamReader(this.getClass().getResource("html.css").openStream());
            } else {
                System.err.println("Could not find css for "+this.getClass().getName());
            }

        } catch (Exception ex) {

            ex.printStackTrace();

        }
        
        return reader;

    }
    
}
