/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
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
 * }}}
 */
package org.xhtmlrenderer.css;



/**
 * @author   Torbj�rn Gannholm
 */
public class StaticHtmlAttributeResolver implements org.xhtmlrenderer.css.match.AttributeResolver {

    /** Creates a new instance of StaticHtmlAttributeResolver */
    public StaticHtmlAttributeResolver() { }

    /**
     * Gets the attributeValue attribute of the StaticHtmlAttributeResolver
     * object
     *
     * @param e         PARAM
     * @param attrName  PARAM
     * @return          The attributeValue value
     */
    public String getAttributeValue( org.w3c.dom.Element e, String attrName ) {
        return e.getAttribute( attrName );
    }

    /**
     * Gets the class attribute of the StaticHtmlAttributeResolver object
     *
     * @param e  PARAM
     * @return   The class value
     */
    public String getClass( org.w3c.dom.Element e ) {
        return e.getAttribute( "class" );
    }

    /**
     * Gets the iD attribute of the StaticHtmlAttributeResolver object
     *
     * @param e  PARAM
     * @return   The iD value
     */
    public String getID( org.w3c.dom.Element e ) {
        return e.getAttribute( "id" );
    }

    /**
     * Gets the lang attribute of the StaticHtmlAttributeResolver object
     *
     * @param e  PARAM
     * @return   The lang value
     */
    public String getLang( org.w3c.dom.Element e ) {
        return e.getAttribute( "lang" );
    }

    /**
     * Gets the pseudoClass attribute of the StaticHtmlAttributeResolver object
     *
     * @param e   PARAM
     * @param pc  PARAM
     * @return    The pseudoClass value
     */
    public boolean isPseudoClass( org.w3c.dom.Element e, int pc ) {
        if ( pc == LINK_PSEUDOCLASS && e.getNodeName().equalsIgnoreCase( "a" ) && !e.getAttribute( "href" ).equals( "" ) ) {
            return true;
        }
        return false;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2004/10/23 13:03:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

