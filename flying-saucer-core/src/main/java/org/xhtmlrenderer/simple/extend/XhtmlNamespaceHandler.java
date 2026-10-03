/*
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
package org.xhtmlrenderer.simple.extend;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.regex.Pattern;

import static java.util.Locale.ROOT;
import static org.w3c.dom.Node.ELEMENT_NODE;

/**
 * Handles xhtml documents, including presentational html attributes (see css 2.1 spec, 6.4.4).
 * In this class ONLY handling (css equivalents) of presentational properties
 * (according to css 2.1 spec, section 6.4.4) should be specified.
 *
 * @author Torbjoern Gannholm
 */
public class XhtmlNamespaceHandler extends XhtmlCssOnlyNamespaceHandler {
    private static final Pattern RE_MANGLED_COLOR = Pattern.compile("[0-9a-f]{6}");

    @Override
    @CheckReturnValue
    public boolean isImageElement(Element e) {
        return e.getNodeName().equalsIgnoreCase("img");
    }

    @Override
    @CheckReturnValue
    public boolean isFormElement(Element e) {
        return e.getNodeName().equalsIgnoreCase("form");
    }

    @Override
    @CheckReturnValue
    public String getImageSourceURI(Element e) {
        return e.getAttribute("src");
    }

    @Override
    @CheckReturnValue
    public String getNonCssStyling(Element e) {
        return switch (e.getNodeName()) {
            case "table" -> applyTableStyles(e);
            case "td", "th" -> applyTableCellStyles(e);
            case "tr" -> applyTableRowStyles(e);
            case "img" -> applyImgStyles(e);
            case "p", "div" -> applyBlockAlign(e);
            default -> "";
        };
    }

    private String applyBlockAlign(Element e) {
        String s = e.getAttribute("align").trim().toLowerCase(ROOT);
        return switch (s) {
            case "left",
                 "right",
                 "center",
                 "justify" -> "text-align: " + s + ";";
            default -> "";
        };
    }

    private String applyImgStyles(Element e) {
        StyleBuilder style = new StyleBuilder();
        style.applyFloatingAlign(e);
        return style.toString();
    }

    private String applyTableCellStyles(Element e) {
        StyleBuilder style = new StyleBuilder();

        // check for cell padding
        Element table = findTable(e);
        if (table != null) {
            style.appendLength(table, "cellpadding", "padding: ");

            String s = getAttribute(table, "border");
            if (s != null && !s.equals("0")) {
                style.appendRawStyle("border: 1px outset black;");
            }
        }
        style.appendWidth(e);
        style.appendHeight(e);
        style.applyTableContentAlign(e);
        appendBackgroundColor(e, style);
        appendBackgroundImage(e, style);
        return style.toString();
    }

    private void appendBackgroundColor(Element e, StyleBuilder style) {
        String s = e.getAttribute("bgcolor").trim();
        if (!s.isEmpty()) {
            String color = looksLikeAMangledColor(s) ? '#' + s : s;
            style.appendStyle("background-color: ", color);
        }
    }

    private void appendBackgroundImage(Element e, StyleBuilder style) {
        style.appendUrl(e, "background", "background-image: ");
    }

    private String applyTableStyles(Element e) {
        StyleBuilder style = new StyleBuilder();
        style.appendLength(e, "width", "width: ");
        style.appendLength(e, "border", "border: ", " inset black;");
        style.appendLength(e, "cellspacing", "border-collapse: separate; border-spacing: ");
        appendBackgroundColor(e, style);
        appendBackgroundImage(e, style);
        style.applyFloatingAlign(e);
        return style.toString();
    }

    private String applyTableRowStyles(Element e) {
        StyleBuilder style = new StyleBuilder();
        style.applyTableContentAlign(e);
        return style.toString();
    }

    boolean looksLikeAMangledColor(String s) {
        return RE_MANGLED_COLOR.matcher(s).matches();
    }

    @Nullable
    Element findTable(Node cell) {
        return ancestor(cell, "table", 5);
    }

    @Nullable
    Element ancestor(Node element, String tagName, int maxDepth) {
        Node parent = element.getParentNode();
        if (parent == null || maxDepth <= 0) {
            return null;
        }

        return parent.getNodeType() == ELEMENT_NODE && parent.getNodeName().equals(tagName) ?
            (Element) parent :
            ancestor(parent, tagName, maxDepth - 1);
    }
}

