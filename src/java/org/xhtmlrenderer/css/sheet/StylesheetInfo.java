/*
 *
 * StylesheetInfo.java
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
package org.xhtmlrenderer.css.sheet;

/**
 * A reference to a stylesheet. If no stylesheet is set, the matcher will try to find the stylesheet by uri,
 * first from the StylesheetFactory cache, then by loading the uri if it is not cached.
 * <p>Therefore, either a stylesheet must be set, or a uri must be set
 * <p>Origin defaults to USER_AGENT and media defaults to "all"
 */
public class StylesheetInfo {

    /**
     * Origin of stylesheet - user agent
     */
    public final static int USER_AGENT = 0;

    /**
     * Origin of stylesheet - user
     */
    public final static int USER = 1;

    /**
     * Origin of stylesheet - author
     */
    public final static int AUTHOR = 2;

    /**
     * @param m a single media identifier
     * @return true if the stylesheet referenced applies to the medium
     */
    public boolean appliesToMedia(String m) {
        return !(m.indexOf("all") == -1 && media.indexOf("all") == -1 && media.indexOf(m) == -1);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Stylesheet getStylesheet() {
        return stylesheet;
    }

    public void setStylesheet(Stylesheet stylesheet) {
        this.stylesheet = stylesheet;
    }

    private Stylesheet stylesheet = null;//just to be able to attach "dummy" stylesheets. Also might save a lookup if it's already looked up
    private String title;
    private String uri;
    private String media = "all";
    private int origin = USER_AGENT;
    private String type;
}
