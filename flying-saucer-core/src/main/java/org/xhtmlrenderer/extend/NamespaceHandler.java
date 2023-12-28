/*
 * Document.java
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
package org.xhtmlrenderer.extend;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.extend.StylesheetFactory;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Provides knowledge specific to a certain document type, like resolving
 * style-sheets
 *
 * @author Torbjoern Gannholm
 */
@ParametersAreNonnullByDefault
public interface NamespaceHandler {

    /**
     * @return the namespace handled
     */
    @Nonnull
    @CheckReturnValue
    String getNamespace();

    /**
     * @return the default CSS stylesheet for this namespace
     */
    @Nullable
    @CheckReturnValue
    StylesheetInfo getDefaultStylesheet(StylesheetFactory factory);

    /**
     * @param doc the document
     * @return the title for this document, if any exists
     */
    @Nullable
    @CheckReturnValue
    String getDocumentTitle(Document doc);

    /**
     * @param doc the document
     * @return all links to CSS stylesheets (type="text/css") in this
     *         document
     */
    @Nonnull
    @CheckReturnValue
    List<StylesheetInfo> getStylesheets(Document doc);

    /**
     * may return null. Required to return null if attribute does not exist and
     * not null if attribute exists.
     */
    @Nonnull
    @CheckReturnValue
    String getAttributeValue(Element e, String attrName);

    @Nonnull
    @CheckReturnValue
    String getAttributeValue(Element e, @Nullable String namespaceURI, String attrName);

    @Nullable
    @CheckReturnValue
    String getClass(Element e);

    @Nullable
    @CheckReturnValue
    String getID(Element e);

    @Nullable
    @CheckReturnValue
    String getElementStyling(Element e);

    /**
     * @return The corresponding css properties for styling that is obtained in other ways.
     */
    @Nullable
    @CheckReturnValue
    String getNonCssStyling(Element e);

    @Nonnull
    @CheckReturnValue
    String getLang(Element e);

    /**
     * should return null if element is not a link
     */
    @Nullable
    @CheckReturnValue
    String getLinkUri(Element e);

    @Nullable
    @CheckReturnValue
    String getAnchorName(Element e);

    /**
     * @return Returns true if the Element represents an image.
     */
    @CheckReturnValue
    boolean isImageElement(Element e);

    /**
     * Determines whether the specified Element represents a
     * &lt;form&gt;.
     *
     * @param e The Element to evaluate.
     * @return true if the Element is a &lt;form&gt; element, false otherwise.
     */
    @CheckReturnValue
    boolean isFormElement(Element e);

    /**
     * For an element where isImageElement returns true, retrieves the URI associated with that Image, as
     * reported by the element; makes no guarantee that the URI is correct, complete or points to anything in
     * particular. For elements where {@link #isImageElement(Element)} returns false, this method may
     * return false, and may also return false if the Element is not correctly formed and contains no URI; check the
     * return value carefully.
     *
     * @param e The element to extract image info from.
     * @return String containing the URI for the image.
     */
    @Nullable
    @CheckReturnValue
    String getImageSourceURI(Element e);
}

