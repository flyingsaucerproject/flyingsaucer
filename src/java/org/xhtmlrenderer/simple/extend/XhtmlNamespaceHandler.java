/*
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
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
package org.xhtmlrenderer.simple.extend;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.css.sheet.InlineStyleInfo;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.swing.NoNamespaceHandler;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * Handles xhtml documents
 *
 * @author Torbjörn Gannholm
 */
public class XhtmlNamespaceHandler extends NoNamespaceHandler {

    /**
     * Description of the Field
     */
    protected HashMap imageComponents;

    /**
     * Description of the Field
     */
    protected LinkedHashMap forms;

    /**
     * Description of the Field
     */
    final static String _namespace = "http://www.w3.org/1999/xhtml";

    /**
     * Gets the namespace attribute of the XhtmlNamespaceHandler object
     *
     * @return The namespace value
     */
    public String getNamespace() {
        return _namespace;
    }

    /**
     * Gets the class attribute of the XhtmlNamespaceHandler object
     *
     * @param e PARAM
     * @return The class value
     */
    public String getClass(org.w3c.dom.Element e) {
        return e.getAttribute("class");
    }

    /**
     * Gets the iD attribute of the XhtmlNamespaceHandler object
     *
     * @param e PARAM
     * @return The iD value
     */
    public String getID(org.w3c.dom.Element e) {
        return e.getAttribute("id");
    }

    /**
     * Gets the elementStyling attribute of the XhtmlNamespaceHandler object
     *
     * @param e PARAM
     * @return The elementStyling value
     */
    public String getElementStyling(org.w3c.dom.Element e) {
        StringBuffer style = new StringBuffer();
        if (e.getNodeName().equals("td")) {
            String s;
            if (!(s = e.getAttribute("colspan")).equals("")) {
                style.append("-fs-table-cell-colspan: ");
                style.append(s);
                style.append(";");
            }
            if (!(s = e.getAttribute("rowspan")).equals("")) {
                style.append("-fs-table-cell-rowspan: ");
                style.append(s);
                style.append(";");
            }
        } else if (e.getNodeName().equals("img")) {
            String s;
            if (!(s = e.getAttribute("width")).equals("")) {
                style.append("width: ");
                style.append(s);
                style.append(";");
            }
            if (!(s = e.getAttribute("height")).equals("")) {
                style.append("height: ");
                style.append(s);
                style.append(";");
            }
        }
        style.append(e.getAttribute("style"));
        return style.toString();
    }

    /**
     * Gets the linkUri attribute of the XhtmlNamespaceHandler object
     *
     * @param e PARAM
     * @return The linkUri value
     */
    public String getLinkUri(org.w3c.dom.Element e) {
        String href = null;
        if (e.getNodeName().equalsIgnoreCase("a") && e.hasAttribute("href")) {
            href = e.getAttribute("href");
        }
        return href;
    }

    /**
     * Gets the documentTitle attribute of the XhtmlNamespaceHandler object
     *
     * @param doc PARAM
     * @return The documentTitle value
     */
    public String getDocumentTitle(org.w3c.dom.Document doc) {
        String title = "TITLE UNKNOWN";
        try {
            for (int i = 0; i < 1; i++) {//smart HACK
                Element html = doc.getDocumentElement();
                NodeList nl = html.getElementsByTagName("head");
                if (nl.getLength() == 0) {
                    break;
                }
                nl = ((Element) nl.item(0)).getElementsByTagName("title");
                if (nl.getLength() == 0) {
                    break;
                }
                nl = ((Element) nl.item(0)).getChildNodes();
                if (nl.getLength() == 0) {
                    break;
                }
                title = nl.item(0).getNodeValue();
            }
        } catch (Exception ex) {
            System.err.println("Error retrieving document title. " + ex.getMessage());
            title = "";
        }
        return title;
    }

    /**
     * Gets the inlineStyle attribute of the XhtmlNamespaceHandler object
     *
     * @param doc PARAM
     * @return The inlineStyle value
     */
    public InlineStyleInfo[] getInlineStyle(org.w3c.dom.Document doc) {
        List list = new ArrayList();
        org.w3c.dom.NodeList nl = null;
        for (int i = 0; i < 1; i++) {//smart HACK
            Element html = doc.getDocumentElement();
            nl = html.getElementsByTagName("head");
            if (nl.getLength() == 0) {
                break;
            }
            nl = ((Element) nl.item(0)).getElementsByTagName("style");
        }
        for (int i = 0, len = nl.getLength(); i < len; i++) {
            StringBuffer style = new StringBuffer();
            org.w3c.dom.Element elem = (org.w3c.dom.Element) nl.item(i);
            String m = elem.getAttribute("media");
            if ("".equals(m)) {
                m = "all";
            }//default for HTML is "screen", but that is silly and firefox seems to assume "all"
            StylesheetInfo info = new StylesheetInfo();
            info.setMedia(m);
            info.setType(elem.getAttribute("type"));
            info.setTitle(elem.getAttribute("title"));
            info.setOrigin(StylesheetInfo.AUTHOR);
            org.w3c.dom.NodeList children = elem.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                org.w3c.dom.Node txt = children.item(j);
                if (txt instanceof CharacterData) {
                    style.append(txt.getNodeValue());
                }
            }
            InlineStyleInfo isi = new InlineStyleInfo();
            isi.setInfo(info);
            isi.setStyle(style.toString());
            list.add(isi);
        }

        InlineStyleInfo[] infos = new InlineStyleInfo[list.size()];
        for (int i = 0; i < infos.length; i++) {
            infos[i] = (InlineStyleInfo) list.get(i);
        }
        return infos;
    }

    /**
     * Gets the stylesheetLinks attribute of the XhtmlNamespaceHandler object
     *
     * @param doc PARAM
     * @return The stylesheetLinks value
     */
    public StylesheetInfo[] getStylesheetLinks(org.w3c.dom.Document doc) {
        List list = new ArrayList();
        //get the processing-instructions (actually for XmlDocuments)
        StylesheetInfo[] refs = super.getStylesheetLinks(doc);
        list.addAll(java.util.Arrays.asList(refs));
        //get the link elements
        org.w3c.dom.NodeList nl = null;
        for (int i = 0; i < 1; i++) {//smart HACK
            Element html = doc.getDocumentElement();
            nl = html.getElementsByTagName("head");
            if (nl.getLength() == 0) {
                break;
            }
            nl = ((Element) nl.item(0)).getElementsByTagName("link");
        }
        for (int i = 0, len = nl.getLength(); i < len; i++) {
            Element link = (Element) nl.item(i);
            if (!link.getAttribute("rel").equals("stylesheet")) {
                continue;
            }
            StylesheetInfo info = new StylesheetInfo();
            info.setOrigin(StylesheetInfo.AUTHOR);
            String a = link.getAttribute("rel");
            if (a.indexOf("alternate") != -1) {
                continue;
            }//DON'T get alternate stylesheets
            a = link.getAttribute("type");
            if ("".equals(a)) {
                a = "text/css";
            }//HACK: is not entirely correct because default may be set by META tag or HTTP headers
            info.setType(a);
            a = link.getAttribute("href");
            info.setUri(a);
            a = link.getAttribute("media");
            if ("".equals(a)) {
                a = "screen";
            }//the default in HTML
            info.setMedia(a);
            a = link.getAttribute("title");
            info.setTitle(a);
            if (info.getType().equals("text/css")) {
                list.add(info);
            }
        }

        refs = new StylesheetInfo[list.size()];
        for (int i = 0; i < refs.length; i++) {
            refs[i] = (StylesheetInfo) list.get(i);
        }
        return refs;
    }

    /**
     * Gets the defaultStylesheet attribute of the XhtmlNamespaceHandler object
     *
     * @return The defaultStylesheet value
     */
    public InputStream getDefaultStylesheet() {
        InputStream stream = null;
        try {
            //TODO: weakness: if no configuration found, it bombs out. Should we be so reliable on correct config? Maybe a warning will do?
            String defaultStyleSheetLocation = Configuration.valueFor("xr.css.user-agent-default-css");
            if (this.getClass().getResourceAsStream(defaultStyleSheetLocation) != null) {
                stream = this.getClass().getResource(defaultStyleSheetLocation).openStream();
            } else {
                XRLog.exception("Can't load default CSS from " + defaultStyleSheetLocation + "." +
                        "This file must be on your CLASSPATH. Please check before continuing.");
            }

        } catch (java.io.IOException ex) {

            XRLog.exception("Bad IO", ex);

        }

        return stream;
    }

    /**
     * Gets the customComponent attribute of the XhtmlNamespaceHandler object
     *
     * @param e         PARAM
     * @param c         PARAM
     * @param setWidth
     * @param setHeight
     * @return The customComponent value
     */
    public JComponent getCustomComponent(Element e, Context c, int setWidth, int setHeight) {
        JComponent cc = null;
        if (e == null) {
            return null;
        }
        if (e.getNodeName().equals("img")) {
            cc = getImageComponent(e);
            if (cc != null) {
                return cc;
            }
            JButton jb = null;
            Image im = null;
            im = c.getCtx().getUac().getImageResource(e.getAttribute("src")).getImage();
            if (im == null) {
                jb = new JButton("Image unreachable. " + e.getAttribute("alt"));
            } else {
                Image i2 = im.getScaledInstance(setWidth, setHeight, Image.SCALE_FAST);
                ImageIcon ii = new ImageIcon(i2, e.getAttribute("alt"));
                jb = new JButton(ii);
            }
            jb.setBorder(BorderFactory.createEmptyBorder());
            jb.setSize(jb.getPreferredSize());
            addImageComponent(e, jb);
            return jb;
        }
        //form components
        Element parentForm = getParentForm(e);
        //parentForm may be null! No problem! Assume action is this document and method is get.
        XhtmlForm form = getForm(parentForm);
        if (form == null) {
            form = new XhtmlForm(c, parentForm);
            addForm(parentForm, form);
        }
        cc = form.addComponent(c, e);
        return cc;
    }

    /**
     * Adds a feature to the ImageComponent attribute of the
     * XhtmlNamespaceHandler object
     *
     * @param e  The feature to be added to the ImageComponent attribute
     * @param cc The feature to be added to the ImageComponent attribute
     */
    protected void addImageComponent(Element e, JComponent cc) {
        if (imageComponents == null) {
            imageComponents = new HashMap();
        }
        imageComponents.put(e, cc);
    }

    /**
     * Adds a feature to the Form attribute of the XhtmlNamespaceHandler object
     *
     * @param e The feature to be added to the Form attribute
     * @param f The feature to be added to the Form attribute
     */
    protected void addForm(Element e, XhtmlForm f) {
        if (forms == null) {
            forms = new LinkedHashMap();
        }
        forms.put(e, f);
    }

    /**
     * Gets the imageComponent attribute of the XhtmlNamespaceHandler object
     *
     * @param e PARAM
     * @return The imageComponent value
     */
    protected JComponent getImageComponent(Element e) {
        if (imageComponents == null) {
            return null;
        }
        return (JComponent) imageComponents.get(e);
    }

    /**
     * Gets the form attribute of the XhtmlNamespaceHandler object
     *
     * @param e PARAM
     * @return The form value
     */
    protected XhtmlForm getForm(Element e) {
        if (forms == null) {
            return null;
        }
        return (XhtmlForm) forms.get(e);
    }

    /**
     * Gets the parentForm attribute of the XhtmlNamespaceHandler object
     *
     * @param e PARAM
     * @return The parentForm value
     */
    protected Element getParentForm(Element e) {
        Node n = e;
        do {
            n = n.getParentNode();
        } while (n.getNodeType() == Node.ELEMENT_NODE && !n.getNodeName().equals("form"));
        if (n.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        return (Element) n;
    }

}

