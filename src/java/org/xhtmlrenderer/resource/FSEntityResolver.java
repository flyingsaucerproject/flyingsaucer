/*
 * {{{ header & license
 * GeneralUtil.java
 * Copyright (c) 2004, 2005 Patrick Wright
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
package org.xhtmlrenderer.resource;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.XRLog;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * <p>
 *
 * A SAX EntityResolver for common entity references and DTDs in X/HTML
 * processing. Maps official entity references to local copies to avoid network
 * lookup. The local copies are stored in the source tree under /entities, and
 * the references here are resolved by a system ClassLoader. As long as the
 * entity files are in the classpath (or bundled in the FS jar), they will be
 * picked up.</p> <p>
 *
 * The basic form of this class comes from Elliot Rusty Harold, on
 * http://www.cafeconleche.org/books/xmljava/chapters/ch07s02.html</p> <p>
 *
 * This class is a Singleton; use {@link #instance} to retrieve it.</p>
 *
 * @author   Patrick Wright
 */
public class FSEntityResolver implements EntityResolver {

    /** Description of the Field */
    private Map entities = new HashMap();
    /** Singleton instance, use {@link #instance()} to retrieve. */
    private static FSEntityResolver instance;

    // fill the list of URLs
    /** Constructor for the FSEntityResolver object */
    private FSEntityResolver() {
        // The XHTML 1.0 DTDs
        this.addMapping( "-//W3C//DTD HTML 4.01//EN", "resources/dtd/html-4.01-strict.dtd" );
        this.addMapping( "-//W3C//DTD HTML 4.01 Transitional//EN", "resources/dtd/html-4.01-transitional.dtd" );
        this.addMapping( "-//W3C//DTD HTML 4.01 Frameset//EN", "resources/dtd/html-4.01-frameset.dtd" );

        this.addMapping( "-//W3C//DTD XHTML 1.0 Strict//EN", "resources/dtd/xhtml1-strict.dtd" );
        this.addMapping( "-//W3C//DTD XHTML 1.0 Transitional//EN", "resources/dtd/xhtml1-transitional.dtd" );
        this.addMapping( "-//W3C//DTD XHTML 1.0 Frameset//EN", "resources/dtd/xhtml1-frameset.dtd" );

        // The HTML 4 entity sets
        this.addMapping( "-//W3C//ENTITIES Latin 1//EN//HTML", "resources/entity/html-lat1.ent" );
        this.addMapping( "-//W3C//ENTITIES Symbols//EN//HTML", "resources/entity/html-symbol.ent" );
        this.addMapping( "-//W3C//ENTITIES Special//EN//HTML", "resources/entity/html-special.ent" );

        // The XHTML 1.0 entity sets
        this.addMapping( "-//W3C//ENTITIES Latin 1 for XHTML//EN", "resources/entity/xhtml-lat1.ent" );
        this.addMapping( "-//W3C//ENTITIES Symbols for XHTML//EN", "resources/entity/xhtml-symbol.ent" );
        this.addMapping( "-//W3C//ENTITIES Special for XHTML//EN", "resources/entity/xhtml-special.ent" );
    }

    /**
     * Description of the Method
     *
     * @param publicID          PARAM
     * @param systemID          PARAM
     * @return                  Returns
     * @exception SAXException  Throws
     */
    public InputSource resolveEntity( String publicID,
                                      String systemID )
        throws SAXException {

        InputSource local = null;
        String url = (String)entities.get( publicID );
        if ( url != null ) {
            InputStream is = GeneralUtil.openStreamFromClasspath( this, url );
            
            if ( is == null ) {
                XRLog.load( Level.WARNING,
                        "Can't find a local reference for Entity for public ID: " + publicID +
                        " and expected to. The local URL should be: " + url + ". Not finding " +
                        "this probably means a CLASSPATH configuration problem; this resource " +
                        "should be included with the renderer and so not finding it means it is " +
                        "not on the CLASSPATH, and should be. Will let parser use the default in " +
                        "this case." );
            }
            local = new InputSource( is );
            XRLog.load(Level.FINE, "Entity public: " + publicID + " -> " + url +
            (local == null ? ", NOT FOUND" : " (local)"));
        } else {
            XRLog.load("Entity public: " + publicID + ", no local mapping. Parser will probably pull from network.");
        }
        return local;
    }

    /**
     * Adds a feature to the Mapping attribute of the FSEntityResolver object
     *
     * @param publicID  The feature to be added to the Mapping attribute
     * @param URL       The feature to be added to the Mapping attribute
     */
    private void addMapping( String publicID, String URL ) {
        entities.put( publicID, URL );
    }

    /**
     * Gets an instance of this class.
     *
     * @return   An instance of .
     */
    public static synchronized FSEntityResolver instance() {
        if ( instance == null ) {
            instance = new FSEntityResolver();
        }
        return instance;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2005/02/03 20:39:34  pdoubleya
 * Added to CVS.
 *
 *
 */

