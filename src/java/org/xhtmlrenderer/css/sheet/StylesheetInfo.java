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
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2004-nov-28
 * Time: 17:19:45
 * To change this template use File | Settings | File Templates.
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

    private String title;
    private String uri;
    private String media;
    private int origin;
    private String type;
}
