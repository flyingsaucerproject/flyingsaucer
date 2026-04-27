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

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.simple.NoNamespaceHandler;
import org.xhtmlrenderer.util.Configuration;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Locale.ROOT;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;
import static org.xhtmlrenderer.css.sheet.StylesheetInfo.Origin.AUTHOR;
import static org.xhtmlrenderer.css.sheet.StylesheetInfo.Origin.USER_AGENT;
import static org.xhtmlrenderer.css.sheet.StylesheetInfo.mediaTypes;
import static org.xhtmlrenderer.util.TextUtil.readTextContent;

/**
 * Handles xhtml but only css styling is honored,
 * no presentational html attributes (see css 2.1 spec, 6.4.4)
 */
public class XhtmlCssOnlyNamespaceHandler extends NoNamespaceHandler {

    private static final String NAMESPACE = "http://www.w3.org/1999/xhtml";

    @Nullable
    private static volatile StylesheetInfo _defaultStylesheet;
    private static final AtomicLong inlineCssCounter = new AtomicLong();

    /**
     * Gets the namespace attribute of the XhtmlNamespaceHandler object
     *
     * @return The namespace value
     */
    @Override
    @CheckReturnValue
    public String getNamespace() {
        return NAMESPACE;
    }

    /**
     * Gets the class attribute of the XhtmlNamespaceHandler object
     */
    @Override
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
        return getAttribute(e, "id");
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
    @CheckReturnValue
    public String getElementStyling(Element e) {
        StyleBuilder style = new StyleBuilder();
        switch (e.getNodeName()) {
            case "td":
            case "th": {
                style.append(e, "colspan", "-fs-table-cell-colspan: ");
                style.append(e, "rowspan", "-fs-table-cell-rowspan: ");
                break;
            }
            case "img": {
                style.appendWidth(e);
                style.appendHeight(e);
                break;
            }
            case "colgroup":
            case "col": {
                style.append(e, "span", "-fs-table-cell-colspan: ");
                style.appendWidth(e);
                break;
            }
        }
        style.appendRawStyle(e.getAttribute("style"));
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

    static String collapseWhiteSpace(String text) {
        int length = text.length();
        char last = '?';

        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) c = ' ';

            if (c != ' ' || last != ' ') {
                result.append(c);
                last = c;
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

    @Nullable
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

    @Nullable
    @CheckReturnValue
    protected StylesheetInfo readStyleElement(Element style) {
        String css = extractContent(style);
        if (css.isEmpty()) {
            return null;
        }

        String media = style.getAttribute("media");
        String uri = "inline:" + inlineCssCounter.incrementAndGet(); // just some unique value to cache by
        return new StylesheetInfo(AUTHOR, uri, mediaTypes(media), css);
    }

    @CheckReturnValue
    private static String extractContent(Element style) {
        StringBuilder buf = new StringBuilder();
        Node current = style.getFirstChild();
        while (current != null) {
            if (current instanceof CharacterData characterData) {
                buf.append(characterData.getData());
            }
            current = current.getNextSibling();
        }

        return buf.toString().trim();
    }

    @Nullable
    protected StylesheetInfo readLinkElement(Element link) {
        String rel = link.getAttribute("rel").toLowerCase(ROOT);
        if (rel.contains("alternate")) {
            return null;
        }//DON'T get alternate stylesheets
        if (!rel.contains("stylesheet")) {
            return null;
        }

        String uri = link.getAttribute("href");
        return new StylesheetInfo(AUTHOR, uri, mediaTypes(link.getAttribute("media")), null);
    }

    /**
     * Gets the stylesheetLinks attribute of the XhtmlNamespaceHandler object
     */
    @Override
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

                    String elemName = requireNonNullElseGet(elem.getLocalName(), () -> elem.getTagName());

                    StylesheetInfo info = switch (elemName) {
                        case "link" -> readLinkElement(elem);
                        case "style" -> readStyleElement(elem);
                        default -> null;
                    };
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
    @CheckReturnValue
    public Optional<StylesheetInfo> getDefaultStylesheet() {
        if (_defaultStylesheet == null) {
            synchronized (this) {
                if (_defaultStylesheet == null) {
                    _defaultStylesheet = new StylesheetInfo(USER_AGENT, getDefaultStylesheetUrl().toString(), mediaTypes(""), null);
                }
            }
        }
        return Optional.ofNullable(_defaultStylesheet);
    }

    @CheckReturnValue
    private URL getDefaultStylesheetUrl() {
        String defaultStyleSheet = Configuration.valueFor("xr.css.user-agent-default-css") + "XhtmlNamespaceHandler.css";
        return requireNonNull(getClass().getResource(defaultStyleSheet), () ->
                "Can't load default CSS from " + defaultStyleSheet + "." +
                        "This file must be on your CLASSPATH. Please check before continuing.");
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
