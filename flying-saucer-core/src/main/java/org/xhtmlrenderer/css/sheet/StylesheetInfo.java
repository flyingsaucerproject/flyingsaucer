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

import java.util.ArrayList;
import java.util.List;



/**
 * A reference to a stylesheet. If no stylesheet is set, the matcher will try to
 * find the stylesheet by uri, first from the StylesheetFactory cache, then by
 * loading the uri if it is not cached. <p>
 *
 * Therefore, either a stylesheet must be set, or a uri must be set <p>
 *
 * Origin defaults to USER_AGENT and media defaults to "all"
 *
 * @author   Torbjoern Gannholm
 */
public class StylesheetInfo {

    /** Description of the Field */
    private Stylesheet stylesheet = null;//just to be able to attach "dummy" stylesheets. Also might save a lookup if it's already looked up
            /** Description of the Field */
    private String title;
    /** Description of the Field */
    private String uri;
    /** Description of the Field */
    private int origin = USER_AGENT;
    /** Description of the Field */
    private String type;
    
    private List mediaTypes = new ArrayList();
    
    private String content;

    /** Origin of stylesheet - user agent  */
    public final static int USER_AGENT = 0;

    /** Origin of stylesheet - user  */
    public final static int USER = 1;

    /** Origin of stylesheet - author  */
    public final static int AUTHOR = 2;
    

    /**
     * @param m  a single media identifier
     * @return   true if the stylesheet referenced applies to the medium
     */
    public boolean appliesToMedia(String m) {
        return m.toLowerCase().equals("all") || 
            mediaTypes.contains("all") || mediaTypes.contains(m.toLowerCase());
    }

    /**
     * Sets the uri attribute of the StylesheetInfo object
     *
     * @param uri  The new uri value
     */
    public void setUri( String uri ) {
        this.uri = uri;
    }

    /**
     * Sets the media attribute of the StylesheetInfo object
     *
     * @param media  The new media value
     */
    public void setMedia( String media ) {
        String[] mediaTypes = media.split(",");
        List l = new ArrayList(mediaTypes.length);
        for (int i = 0; i < mediaTypes.length; i++) {
            l.add(mediaTypes[i].trim().toLowerCase());
        }
        this.mediaTypes = l;
    }
    
    public void setMedia(List mediaTypes) {
        this.mediaTypes = mediaTypes;
    }
    
    public void addMedium(String medium) {
        mediaTypes.add(medium);
    }

    /**
     * Sets the origin attribute of the StylesheetInfo object
     *
     * @param origin  The new origin value
     */
    public void setOrigin( int origin ) {
        this.origin = origin;
    }

    /**
     * Sets the type attribute of the StylesheetInfo object
     *
     * @param type  The new type value
     */
    public void setType( String type ) {
        this.type = type;
    }

    /**
     * Sets the title attribute of the StylesheetInfo object
     *
     * @param title  The new title value
     */
    public void setTitle( String title ) {
        this.title = title;
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
    public String getUri() {
        return uri;
    }

    /**
     * Gets the media attribute of the StylesheetInfo object
     *
     * @return   The media value
     */
    public List getMedia() {
        return mediaTypes;
    }

    /**
     * Gets the origin attribute of the StylesheetInfo object
     *
     * @return   The origin value
     */
    public int getOrigin() {
        return origin;
    }

    /**
     * Gets the type attribute of the StylesheetInfo object
     *
     * @return   The type value
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the title attribute of the StylesheetInfo object
     *
     * @return   The title value
     */
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    public boolean isInline() {
        return this.content != null;
    }
}

