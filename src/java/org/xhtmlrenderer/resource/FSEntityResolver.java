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

        // The XHMTL 1.1 DTD
        this.addMapping( "-//W3C//DTD XHTML 1.1//EN", "resources/dtd/xhtml11.dtd" );

        // The XHTML 1.1 element sets
        this.addMapping( "-//W3C//ELEMENTS XHTML Inline Style 1.0//EN", "resources/element/xhtml-inlstyle-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Text 1.0//EN", "resources/element/xhtml-text-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Inline Structural 1.0//EN", "resources/element/xhtml-inlstruct-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Inline Phrasal 1.0//EN", "resources/element/xhtml-inlphras-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Block Structural 1.0//EN", "resources/element/xhtml-blkstruct-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Block Phrasal 1.0//EN", "resources/element/xhtml-blkphras-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Hypertext 1.0//EN", "resources/element/xhtml-hypertext-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Lists 1.0//EN", "resources/element/xhtml-list-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Editing Elements 1.0//EN", "resources/element/xhtml-edit-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML BIDI Override Element 1.0//EN", "resources/element/xhtml-bdo-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Ruby 1.0//EN", "resources/element/xhtml-ruby-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Presentation 1.0//EN", "resources/element/xhtml-pres-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Inline Presentation 1.0//EN", "resources/element/xhtml-inlpres-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Block Presentation 1.0//EN", "resources/element/xhtml-inlpres-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Link Element 1.0//EN", "resources/element/xhtml-link-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Metainformation 1.0//EN", "resources/element/xhtml-meta-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Base Element 1.0//EN", "resources/element/xhtml-base-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Scripting 1.0//EN", "resources/element/xhtml-script-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Style Sheets 1.0//EN", "resources/element/xhtml-style-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Images 1.0//EN", "resources/element/xhtml-image-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Client-side Image Maps 1.0//EN", "resources/element/xhtml-csismap-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Server-side Image Maps 1.0//EN", "resources/element/xhtml-ssismap-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Param Element 1.0//EN", "resources/element/xhtml-param-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Embedded Object 1.0//EN", "resources/element/xhtml-object-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Tables 1.0//EN", "resources/element/xhtml-table-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Forms 1.0//EN", "resources/element/xhtml-form-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Document Structure 1.0//EN", "resources/element/xhtml-struct-1.mod" );
        this.addMapping( "-//W3C//ELEMENTS XHTML Legacy Markup 1.0//EN", "resources/element/xhtml-legacy-1.mod" );

        // The XHTML 1.1. entity sets
        this.addMapping( "-//W3C//ENTITIES XHTML Modular Framework 1.0//EN", "resources/entity/xhtml-framework-1.mod" );
        this.addMapping( "-//W3C//ENTITIES XHTML Datatypes 1.0//EN", "resources/entity/xhtml-datatypes-1.mod" );
        this.addMapping( "-//W3C//ENTITIES XHTML Qualified Names 1.0//EN", "resources/entity/xhtml-qname-1.mod" );
        this.addMapping( "-//W3C//ENTITIES XHTML Intrinsic Events 1.0//EN", "resources/entity/xhtml-events-1.mod" );
        this.addMapping( "-//W3C//ENTITIES XHTML Common Attributes 1.0//EN", "resources/entity/xhtml-attribs-1.mod" );
        this.addMapping( "-//W3C//ENTITIES XHTML 1.1 Document Model 1.0//EN", "resources/entity/xhtml11-model-1.mod" );
        this.addMapping( "-//W3C//ENTITIES XHTML Character Entities 1.0//EN", "resources/entity/xhtml-charent-1.mod" );

        // The XHTML 1.1 notation sets
        this.addMapping( "-//W3C//NOTATIONS XHTML Notations 1.0//EN", "resources/notation/xhtml-notations-1.mod" );

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
                XRLog.xmlEntities( Level.WARNING,
                        "Can't find a local reference for Entity for public ID: " + publicID +
                        " and expected to. The local URL should be: " + url + ". Not finding " +
                        "this probably means a CLASSPATH configuration problem; this resource " +
                        "should be included with the renderer and so not finding it means it is " +
                        "not on the CLASSPATH, and should be. Will let parser use the default in " +
                        "this case." );
            }
            local = new InputSource( is );
            XRLog.xmlEntities(Level.FINE, "Entity public: " + publicID + " -> " + url +
            (local == null ? ", NOT FOUND" : " (local)"));
        } else {
            XRLog.xmlEntities("Entity public: " + publicID + ", no local mapping. Parser will probably pull from network.");
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
 * Revision 1.3  2005/03/27 18:36:26  pdoubleya
 * Added separate logging for entity resolution.
 *
 * Revision 1.2  2005/03/21 09:13:50  pdoubleya
 * Added XHTML 1.1 references (Kevin).
 *
 * Revision 1.1  2005/02/03 20:39:34  pdoubleya
 * Added to CVS.
 *
 *
 */

