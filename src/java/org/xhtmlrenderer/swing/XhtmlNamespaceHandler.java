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

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.css.sheet.InlineStyleInfo;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles xhtml documents
 *
 * @author Torbjörn Gannholm
 */
public class XhtmlNamespaceHandler extends NoNamespaceHandler {

    static final String _namespace = "http://www.w3.org/1999/xhtml";

    public String getNamespace() {
        return _namespace;
    }

    /*public String getAttributeValue(org.w3c.dom.Element e, String attrName) {
        return e.getAttribute(attrName);
    }*/

    public String getClass(org.w3c.dom.Element e) {
        return e.getAttribute("class");
    }

    public String getID(org.w3c.dom.Element e) {
        return e.getAttribute("id");
    }

    /*public String getLang(org.w3c.dom.Element e) {
        return e.getAttribute("lang");
    }*/

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
        }
        style.append(e.getAttribute("style"));
        return style.toString();
    }

    public String getLinkUri(org.w3c.dom.Element e) {
        String href = null;
        if (e.getNodeName().equalsIgnoreCase("a") && !e.getAttribute("href").equals("")) {
            href = e.getAttribute("href");
        }
        return href;
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

        public java.lang.String getBaseIdentifier() {
            return null;
        }

        public boolean handlesNullPrefixes() {
            return true;
        }
    };

    public String getDocumentTitle(org.w3c.dom.Document doc) {
        String title = "";
        try {
            //org.apache.xpath.objects.XObject xo = XPathAPI.eval(doc.getDocumentElement(), "//xh:head/xh:title/text()", pres);
            org.apache.xpath.objects.XObject xo = XPathAPI.eval(doc.getDocumentElement(), "//head/title/text()");
            org.w3c.dom.NodeList nl = xo.nodelist();
            if (nl.getLength() == 0) {
                System.err.println("Apparently no title element for this document.");
                title = "TITLE UNKNOWN";
            } else {
                title = nl.item(0).getNodeValue();
            }
        } catch (Exception ex) {
            System.err.println("Error retrieving document title. " + ex.getMessage());
            title = "";
        }
        return title;
    }

    public InlineStyleInfo[] getInlineStyle(org.w3c.dom.Document doc) {
        List list = new ArrayList();
        try {
            //org.apache.xpath.objects.XObject xo = XPathAPI.eval(doc.getDocumentElement(), "//xh:style[@type='text/css']", pres);
            //NOTE: type is required and we only handle css
            org.apache.xpath.objects.XObject xo = XPathAPI.eval(doc.getDocumentElement(), "//style[@type='text/css']");
            org.w3c.dom.NodeList nl = xo.nodelist();
            for (int i = 0, len = nl.getLength(); i < len; i++) {
                StringBuffer style = new StringBuffer();
                org.w3c.dom.Element elem = (org.w3c.dom.Element) nl.item(i);
                String m = elem.getAttribute("media");
                if ("".equals(m)) m = "all";//default for HTML is "screen", but that is silly and firefox assumes "all"
                StylesheetInfo info = new StylesheetInfo();
                info.setMedia(m);
                info.setType(elem.getAttribute("type"));//I'd be surprised if this was not text/css!
                info.setTitle(elem.getAttribute("title"));
                info.setOrigin(StylesheetInfo.AUTHOR);
                org.w3c.dom.NodeList children = elem.getChildNodes();
                for (int j = 0; j < children.getLength(); j++) {
                    org.w3c.dom.Node txt = children.item(j);
                    if (txt.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        style.append(txt.getNodeValue());
                    }
                }
                InlineStyleInfo isi = new InlineStyleInfo();
                isi.setInfo(info);
                isi.setStyle(style.toString());
                list.add(isi);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        InlineStyleInfo[] infos = new InlineStyleInfo[list.size()];
        for (int i = 0; i < infos.length; i++) {
            infos[i] = (InlineStyleInfo) list.get(i);
        }
        return infos;
    }

    public StylesheetInfo[] getStylesheetLinks(org.w3c.dom.Document doc) {
        List list = new ArrayList();
        //get the processing-instructions (actually for XmlDocuments)
        StylesheetInfo[] refs = super.getStylesheetLinks(doc);
        list.addAll(java.util.Arrays.asList(refs));
        //get the link elements
        try {
            //this namespace handling is horrible!
            //org.apache.xpath.objects.XObject xo = XPathAPI.eval(doc.getDocumentElement(), "//xh:link[@type='text/css']/@xh:href", pres);
            org.apache.xpath.objects.XObject xo = XPathAPI.eval(doc.getDocumentElement(), "//link[contains(@rel,'stylesheet')]");
            org.w3c.dom.NodeList nl = xo.nodelist();
            for (int i = 0, len = nl.getLength(); i < len; i++) {
                StylesheetInfo info = new StylesheetInfo();
                info.setOrigin(StylesheetInfo.AUTHOR);
                org.w3c.dom.Element link = (org.w3c.dom.Element) nl.item(i);
                String a = link.getAttribute("rel");
                if (a.indexOf("alternate") != -1) continue;//DON'T get alternate stylesheets
                a = link.getAttribute("type");
                if ("".equals(a)) a = "text/css";//HACK: is not entirely correct because default may be set by META tag or HTTP headers
                info.setType(a);
                a = link.getAttribute("href");
                info.setUri(a);
                a = link.getAttribute("media");
                if ("".equals(a)) a = "screen";//the default in HTML
                info.setMedia(a);
                a = link.getAttribute("title");
                info.setTitle(a);
                if (info.getType().equals("text/css")) list.add(info);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        refs = new StylesheetInfo[list.size()];
        for (int i = 0; i < refs.length; i++) {
            refs[i] = (StylesheetInfo) list.get(i);
        }
        return refs;
    }

    public java.io.Reader getDefaultStylesheet() {
        java.io.Reader reader = null;
        try {

            //Object marker = new org.xhtmlrenderer.DefaultCSSMarker();
            
            //if(marker.getClass().getResourceAsStream("default.css") != null) {
            //TODO: weakness: if no configuration found, it bombs out. Should we be so reliable on correct config? Maybe a warning will do?
            String defaultStyleSheetLocation = Configuration.valueFor("xr.css.user-agent-default-css");
            if (this.getClass().getResourceAsStream(defaultStyleSheetLocation) != null) {

                //reader = new java.io.InputStreamReader(marker.getClass().getResource("default.css").openStream());
                reader = new java.io.InputStreamReader(this.getClass().getResource(defaultStyleSheetLocation).openStream());
            } else {
                XRLog.exception("Can't load default CSS from " + defaultStyleSheetLocation + "." +
                        "This file must be on your CLASSPATH. Please check before continuing.");
            }

        } catch (java.io.IOException ex) {

            XRLog.exception("Bad IO", ex);

        }

        return reader;

    }

    public JComponent getCustomComponent(Element e, Context c) {
        JComponent cc = null;
        if (e == null) return null;
        try {
            if (e.getNodeName().equals("img")) {
                //TODO: this is a hack. Go via ua to get url content
                ImageIcon ii = new ImageIcon(new URL(c.getRenderingContext().getBaseURL(), e.getAttribute("src")));
                cc = new JButton(ii);
                //cc.setSize(ii.getIconWidth(), ii.getIconHeight());
                //cc.setBounds(0,0,ii.getIconWidth(),ii.getIconHeight());
            } else if (e.getNodeName().equals("input")) {
                String type = e.getAttribute("type");
                if (type == null || type.equals("")) {
                    type = "button";
                }
                String label = e.getAttribute("value");
                if (label == null || label.equals("")) {
                    if (type.equals("reset")) {
                        label = "Reset";
                    }
                    if (type.equals("submit")) {
                        label = "Submit";
                    }
                }
                if (type.equals("button") || type.equals("reset") || type.equals("submit")) {
                    JButton button = new JButton();
                    button.setText(label);
                    cc = button;
                } else if (type.equals("image")) {
                    JButton button = new JButton();
                    if (e.hasAttribute("src")) {
                        //HACK: should get image from ua
                        ImageIcon ii = new ImageIcon(new URL(c.getRenderingContext().getBaseURL(), e.getAttribute("src")));
                        button.setIcon(ii);
                        button.setText(null);
                        button.setBorderPainted(false);
                        button.setMargin(new Insets(0, 0, 0, 0));
                        button.setPreferredSize(new Dimension(button.getIcon().getIconHeight(),
                                button.getIcon().getIconHeight()));

                    }
                    cc = button;
                } else if (type.equals("checkbox")) {
                    JCheckBox checkbox = new JCheckBox();
                    checkbox.setText("");
                    checkbox.setOpaque(false);
                    if (e.hasAttribute("checked") &&
                            e.getAttribute("checked").equals("checked")) {
                        checkbox.setSelected(true);
                    }
                    cc = checkbox;
                } else if (type.equals("password")) {
                    JPasswordField pw = new JPasswordField();
                    if (e.hasAttribute("size")) {
                        pw.setColumns(Integer.parseInt(e.getAttribute("size")));
                    } else {
                        pw.setColumns(15);
                    }
                    if (e.hasAttribute("maxlength")) {
                        final int maxlength = Integer.parseInt(e.getAttribute("maxlength"));
                        pw.setDocument(new PlainDocument() {
                            public void insertString(int offset, String str, AttributeSet attr)
                                    throws BadLocationException {
                                if (str == null) {
                                    return;
                                }
                                if ((getLength() + str.length()) <= maxlength) {
                                    super.insertString(offset, str, attr);
                                }
                            }
                        });
                    }
                    cc = pw;
                } else if (type.equals("radio")) {
                    JRadioButton radio = new JRadioButton();
                    radio.setText("");
                    radio.setOpaque(false);
                    if (e.hasAttribute("checked") &&
                            e.getAttribute("checked").equals("checked")) {
                        radio.setSelected(true);
                    }

                    /*if (e.hasAttribute("name")) {
                        String name = e.getAttribute("name");
                        List other_comps = c.getInputFieldComponents(c.getForm(), name);
                        if (other_comps.size() > 0) {
                            for (int i = 0; i < other_comps.size(); i++) {
                                SharedContext.FormComponent other_comp = (SharedContext.FormComponent) other_comps.get(i);
                                if (other_comp.component instanceof JRadioButton) {
                                    JRadioButton other_radio = (JRadioButton) other_comp.component;
                                    //Uu.p("found a matching component: " + other_radio);
                                }
                            }
                        }
                    }*/
                    cc = radio;
                } else if (type.equals("text")) {
                    JTextField text = new JTextField();
                    if (e.hasAttribute("value")) {
                        text.setText(e.getAttribute("value"));
                    }
                    if (e.hasAttribute("size")) {
                        text.setColumns(Integer.parseInt(e.getAttribute("size")));
                    } else {
                        text.setColumns(15);
                    }
                    if (e.hasAttribute("maxlength")) {
                        final int maxlength = Integer.parseInt(e.getAttribute("maxlength"));
                        text.setDocument(new PlainDocument() {
                            public void insertString(int offset, String str, AttributeSet attr)
                                    throws BadLocationException {
                                if (str == null) {
                                    return;
                                }
                                if ((getLength() + str.length()) <= maxlength) {
                                    super.insertString(offset, str, attr);
                                }
                            }
                        });
                    }
                    if (e.hasAttribute("readonly") &&
                            e.getAttribute("readonly").equals("readonly")) {
                        text.setEditable(false);
                    }
                    cc = text;
                }//hidden?
                //HACK:
                if (cc == null) XRLog.layout("unknown input type " + type);
            } else if (e.getNodeName().equals("textarea")) {
                int rows = 4;
                int cols = 10;
                if (e.hasAttribute("rows")) {
                    rows = Integer.parseInt(e.getAttribute("rows"));
                }
                if (e.hasAttribute("cols")) {
                    cols = Integer.parseInt(e.getAttribute("cols"));
                }

                JTextArea ta = new JTextArea(rows, cols);
                JScrollPane sp = new JScrollPane(ta);
                sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                if (e.getFirstChild() != null) {
                    //Uu.p("setting text to: " + elem.getFirstChild().getNodeValue());
                    ta.setText(e.getFirstChild().getNodeValue());
                }
                cc = sp;

            } else if (e.getNodeName().equals("select")) {
                JComboBox select = new JComboBox();

                NodeList options = e.getElementsByTagName("option");
                int selected = -1;
                for (int i = 0; i < options.getLength(); i++) {
                    Element value = (Element) options.item(i);
                    String svalue = value.getFirstChild().getNodeValue();
                    select.addItem(svalue);
                    if (value.hasAttribute("selected") && value.getAttribute("selected").equals("selected")) {
                        selected = i;
                    }
                }

                if (selected != -1) {
                    select.setSelectedIndex(selected);
                }
                cc = select;
            }
        } catch (MalformedURLException ex) {

        }
        if (cc != null) {
            cc.setSize(cc.getPreferredSize());
            if (e.hasAttribute("disabled") &&
                    e.getAttribute("disabled").equals("disabled")) {
                cc.setEnabled(false);
            }
        }
        return cc;
    }

}
