/*
 *
 * XhtmlDocument.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
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

package org.xhtmlrenderer.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xhtmlrenderer.css.extend.StylesheetFactory;
import org.xhtmlrenderer.css.extend.TreeResolver;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.extend.NamespaceHandler;

/**
 * Handles a general XML document
 *
 * @author Torbjoern Gannholm
 */
public class NoNamespaceHandler implements NamespaceHandler {

    static final String _namespace = "http://www.w3.org/XML/1998/namespace";

    public String getNamespace() {
        return _namespace;
    }

    public String getAttributeValue(org.w3c.dom.Element e, String attrName) {
        return e.getAttribute(attrName);
    }
    
    public String getAttributeValue(Element e, String namespaceURI, String attrName) {
        if (namespaceURI == TreeResolver.NO_NAMESPACE) {
            return e.getAttribute(attrName);
        } else if (namespaceURI == null) {
            if (e.getLocalName() == null) { // No namespaces
                return e.getAttribute(attrName);
            } else {
                NamedNodeMap attrs = e.getAttributes();
                int l = attrs.getLength();
                for (int i = 0; i < l; i++) {
                    Attr attr = (Attr)attrs.item(i);
                    if (attrName.equals(attr.getLocalName())) {
                        return attr.getValue();
                    }
                }
                
                return "";
            }
        } else {
            return e.getAttributeNS(namespaceURI, attrName);
        }
    }

    public String getClass(org.w3c.dom.Element e) {
        return null;
    }

    public String getID(org.w3c.dom.Element e) {
        return null;
    }

    public String getLang(org.w3c.dom.Element e) {
        if(e == null) {
            return "";
        }
        return e.getAttribute("lang");
    }

    public String getElementStyling(org.w3c.dom.Element e) {
        return null;
    }

    public String getNonCssStyling(Element e) {
        return null;
    }

    public String getLinkUri(org.w3c.dom.Element e) {
        return null;
    }

    public String getDocumentTitle(org.w3c.dom.Document doc) {
        return null;
    }
    
    public String getAnchorName(Element e) {
        return null;
    }

    public boolean isImageElement(Element e) {
        return false;
    }

    public String getImageSourceURI(Element e) {
        return null;
    }
    
    public boolean isFormElement(Element e) {
        return false;
    }

    private Pattern _typePattern = Pattern.compile("type\\s?=\\s?");
    private Pattern _hrefPattern = Pattern.compile("href\\s?=\\s?");
    private Pattern _titlePattern = Pattern.compile("title\\s?=\\s?");
    private Pattern _alternatePattern = Pattern.compile("alternate\\s?=\\s?");
    private Pattern _mediaPattern = Pattern.compile("media\\s?=\\s?");

    public StylesheetInfo[] getStylesheets(org.w3c.dom.Document doc) {
        List list = new ArrayList();
        //get the processing-instructions (actually for XmlDocuments)
        //type and href are required to be set
        NodeList nl = doc.getChildNodes();
        for (int i = 0, len = nl.getLength(); i < len; i++) {
            Node node = nl.item(i);
            if (node.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE) continue;
            ProcessingInstruction piNode = (ProcessingInstruction) node;
            if (!piNode.getTarget().equals("xml-stylesheet")) continue;
            StylesheetInfo info;
            info = new StylesheetInfo();
            info.setOrigin(StylesheetInfo.AUTHOR);
            String pi = piNode.getData();
            Matcher m = _alternatePattern.matcher(pi);
            if (m.matches()) {
                int start = m.end();
                String alternate = pi.substring(start + 1, pi.indexOf(pi.charAt(start), start + 1));
                //TODO: handle alternate stylesheets
                if (alternate.equals("yes")) continue;//DON'T get alternate stylesheets for now
            }
            m = _typePattern.matcher(pi);
            if (m.find()) {
                int start = m.end();
                String type = pi.substring(start + 1, pi.indexOf(pi.charAt(start), start + 1));
                //TODO: handle other stylesheet types
                if (!type.equals("text/css")) continue;//for now
                info.setType(type);
            }
            m = _hrefPattern.matcher(pi);
            if (m.find()) {
                int start = m.end();
                String href = pi.substring(start + 1, pi.indexOf(pi.charAt(start), start + 1));
                info.setUri(href);
            }
            m = _titlePattern.matcher(pi);
            if (m.find()) {
                int start = m.end();
                String title = pi.substring(start + 1, pi.indexOf(pi.charAt(start), start + 1));
                info.setTitle(title);
            }
            m = _mediaPattern.matcher(pi);
            if (m.find()) {
                int start = m.end();
                String media = pi.substring(start + 1, pi.indexOf(pi.charAt(start), start + 1));
                info.setMedia(media);
            } else {
                info.addMedium("screen");
            }
            list.add(info);
        }

        return (StylesheetInfo[])list.toArray(new StylesheetInfo[list.size()]);
    }

    public StylesheetInfo getDefaultStylesheet(StylesheetFactory factory) {
        return null;
    }

}
