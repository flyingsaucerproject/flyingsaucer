/*
 * DocumentProcessor.java
 *
 * Created on den 14 oktober 2004, 23:54
 */

package net.homelinux.tobe.renderer.builder;

import org.xml.sax.ContentHandler;
//import org.xml.sax.helpers.NamespaceSupport;
import org.w3c.dom.*;
import net.homelinux.tobe.renderer.XRDocument;
import java.util.List;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class DocumentProcessor implements ContentHandler {
    
    XRDocument _doc;
    Document _dom;
    //NamespaceSupport _ns;
    Element _current;
    
    /** Creates a new instance of DocumentProcessor */
    public DocumentProcessor(XRDocument doc) {
        _doc = doc;
        _dom = doc.getDomDocument();
        //_ns = new NamespaceSupport();
    }
    
    public void characters(char[] values, int start, int length) throws org.xml.sax.SAXException {
        Text text = _dom.createTextNode(new String(values, start, length));
        _current.appendChild(text);
    }
    
    public void endDocument() throws org.xml.sax.SAXException {
    }
    
    public void endElement(String str, String str1, String str2) throws org.xml.sax.SAXException {
        _current.normalize();//concatenate contiguous text nodes
        Node parent = _current.getParentNode();
        if(parent.getNodeType() == Node.ELEMENT_NODE) _current = (Element) parent;
        else _current = null;
    }
    
    public void endPrefixMapping(String str) throws org.xml.sax.SAXException {
    }
    
    public void ignorableWhitespace(char[] values, int param, int param2) throws org.xml.sax.SAXException {
    }
    
    public void processingInstruction(String str, String str1) throws org.xml.sax.SAXException {
    }
    
    public void setDocumentLocator(org.xml.sax.Locator locator) {
    }
    
    public void skippedEntity(String str) throws org.xml.sax.SAXException {
    }
    
    public void startDocument() throws org.xml.sax.SAXException {
    }
    
    public void startElement(String namespaceURI, String localName, String qname, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
        Element started;
        Attr a;
        try {
            if(namespaceURI.equals("")) {
                if(qname.equals("")) {
                    started = _dom.createElement(localName);
                } else {
                    started = _dom.createElement(qname);
                }
            } else {
                started = _dom.createElementNS(namespaceURI, localName);
            }
            if(_current == null) {
                _dom.appendChild(started);

            } else {
                _current.appendChild(started);
            }
            parseAttributeNodes(started, attributes);
            _current = started;
        }
        catch(DOMException de) {
            throw new org.xml.sax.SAXException(de);
        }
    }
    
    public void startPrefixMapping(String str, String str1) throws org.xml.sax.SAXException {
    }
    
    private void parseAttributeNodes(Element e, org.xml.sax.Attributes attributes) {
        int attCount = attributes.getLength();
        for(int i = 0; i < attCount; i++) {
            String nu = attributes.getURI(i);
            String ln = attributes.getLocalName(i);
            String qn = attributes.getQName(i);
            String type = attributes.getType(i);
            String value = attributes.getValue(i);
            if(nu.equals("")) {
                if(qn.equals("")) {
                    e.setAttribute(ln, value);
                } else {
                    e.setAttribute(qn, value);
                }
            } else {
                e.setAttributeNS(nu,ln,value);
            }
        }
    }
    
}
