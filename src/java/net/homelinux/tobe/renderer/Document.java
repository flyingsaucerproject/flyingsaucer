/*
 *
 * Document.java
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

package net.homelinux.tobe.renderer;

/**
 * Provides knowledge specific to a certain document type, like resolving style-sheets
 */
public interface Document {
    
    //javax.xml.transform.Source getSource();
    
    java.net.URI getURI();
    
    org.w3c.dom.Document getDomDocument();
    
    java.io.Reader getDefaultStylesheet();
    
    String[] getInlineStyles();
    
    java.net.URI[] getStylesheetURIs();
    
    /** may return null */
    public String getClass(org.w3c.dom.Element e);
    
    /** may return null */
    public String getID(org.w3c.dom.Element e);
    
    /** may return null */
    public String getElementStyling(org.w3c.dom.Element e);
    
    /** may return null */
    public String getLang(org.w3c.dom.Element e);
    
}
