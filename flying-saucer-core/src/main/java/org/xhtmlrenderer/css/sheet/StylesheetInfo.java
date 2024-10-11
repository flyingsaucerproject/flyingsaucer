/*
 * StylesheetInfo.java
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
package org.xhtmlrenderer.css.sheet;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

/**
 * A reference to a stylesheet. If no stylesheet is set, the matcher will try to
 * find the stylesheet by uri, first from the StylesheetFactory cache, then by
 * loading the uri if it is not cached. <p>
 *
 * Therefore, either a stylesheet must be set, or an uri must be set <p>
 *
 * Origin defaults to USER_AGENT and media defaults to "all"
 *
 * @author   Torbjoern Gannholm
 */
public class StylesheetInfo {

    private Stylesheet stylesheet = null;//just to be able to attach "dummy" stylesheets. Also, might save a lookup if it's already looked up
    private final String title;
    @Nullable
    private String uri;
    private final Origin origin;
    @Nullable
    private final String type;
    private final List<String> mediaTypes;
    @Nullable
    private final String content;

    /**
     * Origin of stylesheet
     */
    public enum Origin {
        USER_AGENT,
        USER,
        AUTHOR
    }

    public StylesheetInfo(Origin origin, @Nullable String type, @Nullable String uri, List<String> mediaTypes,
                          String title, @Nullable String content) {
        this.origin = origin;
        this.type = type;
        this.uri = uri;
        this.mediaTypes = mediaTypes;
        this.title = title;
        this.content = content;
    }

    /**
     * @param m  a single media identifier
     * @return   true if the stylesheet referenced applies to the medium
     */
    public boolean appliesToMedia(String m) {
        String mLowerCase = m.toLowerCase();
        return mLowerCase.equals("all") ||
            mediaTypes.contains("all") || mediaTypes.contains(mLowerCase);
    }

    /**
     * Sets the uri attribute of the StylesheetInfo object
     *
     * @param uri  The new uri value
     */
    public void setUri(@Nullable String uri) {
        this.uri = uri;
    }

    public static List<String> mediaTypes(String media) {
        if (media.isEmpty()) {
            //default for HTML is "screen", but that is silly and firefox seems to assume "all"
            return List.of("all");
        }

        return Stream.of(media.split(","))
                .map(mediaType -> mediaType.trim().toLowerCase())
                .toList();
    }

    /**
     * Sets the stylesheet attribute of the StylesheetInfo object
     *
     * @param stylesheet  The new stylesheet value
     */
    public void setStylesheet( Stylesheet stylesheet ) {
        this.stylesheet = stylesheet;
    }

    /**
     * Gets the uri attribute of the StylesheetInfo object
     *
     * @return   The uri value
     */
    @Nullable
    @CheckReturnValue
    public String getUri() {
        return uri;
    }

    /**
     * Gets the media attribute of the StylesheetInfo object
     *
     * @return   The media value
     */
    @NonNull
    @CheckReturnValue
    public List<String> getMedia() {
        return mediaTypes;
    }

    /**
     * Gets the origin attribute of the StylesheetInfo object
     *
     * @return   The origin value
     */
    public Origin getOrigin() {
        return origin;
    }

    @Nullable
    public String getType() {
        return type;
    }

    /**
     * Gets the title attribute of the StylesheetInfo object
     *
     * @return   The title value
     */
    @Nullable
    @CheckReturnValue
    public String getTitle() {
        return title;
    }

    /**
     * Gets the stylesheet attribute of the StylesheetInfo object
     *
     * @return   The stylesheet value
     */
    public Stylesheet getStylesheet() {
        return stylesheet;
    }

    @Nullable
    @CheckReturnValue
    public String getContent() {
        return content;
    }

    public boolean isInline() {
        return this.content != null;
    }
}

