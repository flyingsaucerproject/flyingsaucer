/*
 * Copyright (c) 2005 Torbjoern Gannholm
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.css.extend.StylesheetFactory;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.simple.NoNamespaceHandler;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.XRLog;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.xhtmlrenderer.util.TextUtil.readTextContent;

/**
 * Handles xhtml but only css styling is honored,
 * no presentational html attributes (see css 2.1 spec, 6.4.4)
 */
@ParametersAreNonnullByDefault
public class XhtmlCssOnlyNamespaceHandler extends NoNamespaceHandler {

    private static final String _namespace = "http://www.w3.org/1999/xhtml";
    private static StylesheetInfo _defaultStylesheet;
    private static boolean _defaultStylesheetError;

    /**
     * Gets the namespace attribute of the XhtmlNamespaceHandler object
     *
     * @return The namespace value
     */
    @Override
    @Nonnull
    @CheckReturnValue
    public String getNamespace() {
        return _namespace;
    }

    /**
     * Gets the class attribute of the XhtmlNamespaceHandler object
     */
    @Override
    @Nonnull
    @CheckReturnValue
    public String getClass(Element e) {
        return e.getAttribute("class");
    }

    /**
     * Gets the iD attribute of the XhtmlNamespaceHandler object
     */
    @Override
    @Nullable
    @CheckReturnValue
    public String getID(Element e) {
        String result = e.getAttribute("id").trim();
        return result.isEmpty() ? null : result;
    }

    protected String convertToLength(String value) {
        if (isInteger(value)) {
            return value + "px";
        } else {
            return value;
        }
    }

    protected boolean isInteger(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (! (c >= '0' && c <= '9')) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    protected String getAttribute(Element e, String attrName) {
        String result = e.getAttribute(attrName).trim();
        return result.isEmpty() ? null : result;
    }

    /**
     * Gets the elementStyling attribute of the XhtmlNamespaceHandler object
     * @return The elementStyling value
     */
    @Override
    @Nonnull
    @CheckReturnValue
    public String getElementStyling(Element e) {
        StringBuilder style = new StringBuilder();
        switch (e.getNodeName()) {
            case "td":
            case "th": {
                String s;
                s = getAttribute(e, "colspan");
                if (s != null) {
                    style.append("-fs-table-cell-colspan: ");
                    style.append(s);
                    style.append(";");
                }
                s = getAttribute(e, "rowspan");
                if (s != null) {
                    style.append("-fs-table-cell-rowspan: ");
                    style.append(s);
                    style.append(";");
                }
                break;
            }
            case "img": {
                String s;
                s = getAttribute(e, "width");
                if (s != null) {
                    style.append("width: ");
                    style.append(convertToLength(s));
                    style.append(";");
                }
                s = getAttribute(e, "height");
                if (s != null) {
                    style.append("height: ");
                    style.append(convertToLength(s));
                    style.append(";");
                }
                break;
            }
            case "colgroup":
            case "col": {
                String s;
                s = getAttribute(e, "span");
                if (s != null) {
                    style.append("-fs-table-cell-colspan: ");
                    style.append(s);
                    style.append(";");
                }
                s = getAttribute(e, "width");
                if (s != null) {
                    style.append("width: ");
                    style.append(convertToLength(s));
                    style.append(";");
                }
                break;
            }
        }
        style.append(e.getAttribute("style"));
        return style.toString();
    }

    /**
     * Gets the linkUri attribute of the XhtmlNamespaceHandler object
     */
    @Override
    @Nullable
    @CheckReturnValue
    public String getLinkUri(Element e) {
        return e.getNodeName().equalsIgnoreCase("a") && e.hasAttribute("href") ? e.getAttribute("href") : null;
    }

    @Override
    @Nullable
    @CheckReturnValue
    public String getAnchorName(@Nullable Element e) {
        if (e != null && "a".equalsIgnoreCase(e.getNodeName()) && e.hasAttribute("name")) {
            return e.getAttribute("name");
        }
        return null;
    }

    private static String collapseWhiteSpace(String text) {
        StringBuilder result = new StringBuilder();
        int l = text.length();
        for (int i = 0; i < l; i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                result.append(' ');
                while (++i < l) {
                    c = text.charAt(i);
                    if (! Character.isWhitespace(c)) {
                        i--;
                        break;
                    }
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Returns the title of the document as located in the contents of /html/head/title, or "" if none could be found.
     *
     * @param doc the document to search for a title
     * @return The document's title, or "" if none found
     */
    @Override
    @Nonnull
    @CheckReturnValue
    public String getDocumentTitle(Document doc) {
        String title = "";

        Element html = doc.getDocumentElement();
        Element head = findFirstChild(html, "head");
        if (head != null) {
            Element titleElem = findFirstChild(head, "title");
            if (titleElem != null) {
                title = collapseWhiteSpace(readTextContent(titleElem).trim());
            }
        }

        return title;
    }

    private Element findFirstChild(Element parent, String targetName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(targetName)) {
                return (Element)n;
            }
        }

        return null;
    }

    protected StylesheetInfo readStyleElement(Element style) {
        String media = style.getAttribute("media");
        if (media.isEmpty()) {
            media = "all";
        }//default for HTML is "screen", but that is silly and firefox seems to assume "all"
        StylesheetInfo info = new StylesheetInfo();
        info.setMedia(media);
        info.setType(style.getAttribute("type"));
        info.setTitle(style.getAttribute("title"));
        info.setOrigin(StylesheetInfo.AUTHOR);

        StringBuilder buf = new StringBuilder();
        Node current = style.getFirstChild();
        while (current != null) {
            if (current instanceof CharacterData) {
                buf.append(((CharacterData)current).getData());
            }
            current = current.getNextSibling();
        }

        String css = buf.toString().trim();
        if (!css.isEmpty()) {
            info.setContent(css);
            return info;
        } else {
            return null;
        }
    }

    protected StylesheetInfo readLinkElement(Element link) {
        String rel = link.getAttribute("rel").toLowerCase();
        if (rel.contains("alternate")) {
            return null;
        }//DON'T get alternate stylesheets
        if (!rel.contains("stylesheet")) {
            return null;
        }

        String type = link.getAttribute("type");
        if (!(type.isEmpty() || type.equals("text/css"))) {
            return null;
        }

        StylesheetInfo info = new StylesheetInfo();

        if (type.isEmpty()) {
            type = "text/css";
        } // HACK is not entirely correct because default may be set by META tag or HTTP headers
        info.setType(type);

        info.setOrigin(StylesheetInfo.AUTHOR);

        info.setUri(link.getAttribute("href"));
        String media = link.getAttribute("media");
        if (media.isEmpty()) {
            media = "all";
        }
        info.setMedia(media);

        String title = link.getAttribute("title");
        info.setTitle(title);

        return info;
    }

    /**
     * Gets the stylesheetLinks attribute of the XhtmlNamespaceHandler object
     */
    @Override
    @Nonnull
    @CheckReturnValue
    public List<StylesheetInfo> getStylesheets(Document doc) {
        //get the processing-instructions (actually for XmlDocuments)
        List<StylesheetInfo> result = new ArrayList<>(super.getStylesheets(doc));

        //get the link elements
        Element html = doc.getDocumentElement();
        Element head = findFirstChild(html, "head");
        if (head != null) {
            Node current = head.getFirstChild();
            while (current != null) {
                if (current.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element)current;
                    StylesheetInfo info = null;
                    String elemName = elem.getLocalName();
                    if (elemName == null)
                    {
                        elemName = elem.getTagName();
                    }
                    if (elemName.equals("link")) {
                        info = readLinkElement(elem);
                    } else if (elemName.equals("style")) {
                        info = readStyleElement(elem);
                    }
                    if (info != null) {
                        result.add(info);
                    }
                }
                current = current.getNextSibling();
            }
        }

        return result;
    }

    @Override
    @Nullable
    @CheckReturnValue
    public StylesheetInfo getDefaultStylesheet(StylesheetFactory factory) {
        synchronized (XhtmlCssOnlyNamespaceHandler.class) {
            if (_defaultStylesheet != null) {
                return _defaultStylesheet;
            }

            if (_defaultStylesheetError) {
                return null;
            }

            StylesheetInfo info = new StylesheetInfo();
            info.setUri(getNamespace());
            info.setOrigin(StylesheetInfo.USER_AGENT);
            info.setMedia("all");
            info.setType("text/css");

            try (InputStream is = getDefaultStylesheetStream()) {
                if (_defaultStylesheetError) {
                    return null;
                }

                Stylesheet sheet = factory.parse(new InputStreamReader(is), info);
                info.setStylesheet(sheet);
            } catch (IOException e) {
                _defaultStylesheetError = true;
                XRLog.exception("Could not parse default stylesheet", e);
            }

            _defaultStylesheet = info;

            return _defaultStylesheet;
        }
    }

    private InputStream getDefaultStylesheetStream() {
        String defaultStyleSheet = Configuration.valueFor("xr.css.user-agent-default-css") + "XhtmlNamespaceHandler.css";
        InputStream stream = getClass().getResourceAsStream(defaultStyleSheet);
        if (stream == null) {
            XRLog.exception("Can't load default CSS from " + defaultStyleSheet + "." +
                    "This file must be on your CLASSPATH. Please check before continuing.");
            _defaultStylesheetError = true;
        }

        return stream;
    }

    private Map<String, String> getMetaInfo(Document doc) {
        Map<String, String> metadata = new HashMap<>();

        Element html = doc.getDocumentElement();
        Element head = findFirstChild(html, "head");
        if (head != null) {
            Node current = head.getFirstChild();
            while (current != null) {
                if (current.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element)current;
                    String elemName = elem.getLocalName();
                    if (elemName == null)
                    {
                        elemName = elem.getTagName();
                    }
                    if (elemName.equals("meta")) {
                        String http_equiv = elem.getAttribute("http-equiv");
                        String content = elem.getAttribute("content");

                        if (!http_equiv.isEmpty() && !content.isEmpty()) {
                            metadata.put(http_equiv, content);
                        }
                    }
                }
                current = current.getNextSibling();
            }
        }

        return metadata;
    }

    @Override
    @Nonnull
    @CheckReturnValue
    public String getLang(@Nullable Element e) {
        if (e == null) {
            return "";
        }
        String lang = e.getAttribute("lang");
        if (lang.isEmpty()) {
            lang = getMetaInfo(e.getOwnerDocument()).get("Content-Language");
            if (lang == null) {
                lang = "";
            }
        }
        return lang;
    }
}
