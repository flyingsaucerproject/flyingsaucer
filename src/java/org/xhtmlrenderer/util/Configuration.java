/*
 * {{{ header & license
 * Configuration.java
 * Copyright (c) 2004 Patrick Wright
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
package org.xhtmlrenderer.util;

import java.io.*;
import java.util.*;
import java.util.logging.*;


/**
 * Stores runtime configuration information for application parameters that may
 * vary on restarting. This implements the Singleton pattern.
 *
 * @author   Patrick Wright
 */
public class Configuration {

    /** */
    private Properties properties;
    /** Description of the Field */
    private final Logger devLogger = Logger.getLogger( "plumbing.config" );
    /** */
    private final static Logger LOGGER = Logger.getLogger( "plumbing.resources" );

    /** */
    private static Configuration sInstance;

    /** */
    private final static String SF_FILE_NAME = "resources/conf/xhtmlrenderer.conf";

    /** */
    private Configuration() {
        System.out.println(LogManager.getLogManager().getProperty("plumbing.config.level"));
        
        loadDefaultProperties();
        loadOverrideProperties();
        loadSystemProperties();
        logAfterLoad();
    }

    /**
     * @param pKey  PARAM
     * @return      Returns
     */
    public String valueFor( String pKey ) {
        String val = this.properties.getProperty( pKey );
        if ( val == null ) {
            LOGGER.info( "CONFIGURATION: no value found for key " + pKey );
        }
        return val;
    }

    /**
     * @param pKey      PARAM
     * @param pDefault  PARAM
     * @return          Returns
     */
    public String valueFor( String pKey, String pDefault ) {
        String val = this.properties.getProperty( pKey );
        if ( val == null && pDefault == null ) {
            LOGGER.info( "CONFIGURATION: no value found for key " + pKey );
        }
        return val;
    }

    /** */
    private void loadDefaultProperties() {
        try {
            InputStream readStream = GeneralUtil.openStreamFromClasspath( SF_FILE_NAME );

            if ( readStream == null ) {
                throw new XRRuntimeException( "No configuration files found in classpath using URL: " + SF_FILE_NAME );
            } else {
                this.properties = new Properties();
                this.properties.load( readStream );
            }
        } catch ( RuntimeException rex ) {
            throw rex;
        } catch ( Exception ex ) {
            throw new XRRuntimeException(
                    "Could not load properties file for configuration.",
                    ex );
        }
        devLogger.info( "Configuration loaded from " + SF_FILE_NAME );
    }

    /** Description of the Method */
    private void loadOverrideProperties() {
        String override = System.getProperty( "xr-props" );
        if ( override != null ) {
            devLogger.info( "Configuration override file specified as " + override + ", looking for it." );
            InputStream readStream = GeneralUtil.openStreamFromClasspath( override );
            if ( readStream == null ) {
                devLogger.info( "Could not find override properties file at " + override + ", using defaults." );
                return;
            } else {
                Properties temp = new Properties();
                try {
                    temp.load( readStream );
                } catch ( IOException iex ) {
                    LOGGER.log( Level.WARNING, "Error while loading override properties file; skipping.", iex );
                    return;
                }

                Enumeration elem = this.properties.keys();
                int cnt = 0;
                while ( elem.hasMoreElements() ) {
                    String key = (String)elem.nextElement();
                    String val = temp.getProperty( key );
                    if ( val != null ) {
                        this.properties.setProperty( key, val );
                        devLogger.info( "  " + key + " -> " + val );
                        cnt++;
                    }
                }
                devLogger.info( "Configuration: " + cnt + " properties overridden from override properties file." );
            }
        }
    }

    /** Description of the Method */
    private void loadSystemProperties() {
        Enumeration elem = properties.keys();
        devLogger.info( "Overriding loaded configuration from System properties." );
        int cnt = 0;
        while ( elem.hasMoreElements() ) {
            String key = (String)elem.nextElement();
            String val = System.getProperty( key );
            if ( val != null ) {
                properties.setProperty( key, val );
                devLogger.info( "  Overrode value for " + key );
                cnt++;
            }
        }
        devLogger.info( "Configuration: " + cnt + " properties overridden from System properties." );
    }

    /** Description of the Method */
    private void logAfterLoad() {
        Enumeration elem = properties.keys();
        devLogger.info( "Configuration contains " + properties.size() + " keys." );
        devLogger.info( "List of configuration properties, after override:" );
        while ( elem.hasMoreElements() ) {
            String key = (String)elem.nextElement();
            String val = properties.getProperty( key );
            devLogger.info( "  " + key + " = " + val );
        }
        devLogger.info( "Properties list complete." );
    }



    /**
     * @return   An instance of .
     */
    public static synchronized Configuration instance() {
        if ( Configuration.sInstance == null ) {
            Configuration.sInstance = new Configuration();
        }
        return Configuration.sInstance;
    }


    /**
     * Description of the Method
     *
     * @param args  PARAM
     */
    public static void main( String args[] ) {
        try {
            System.out.println( Configuration.instance().valueFor( "xr.test-prop" ) );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }// end main()
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2004/10/13 23:00:31  pdoubleya
 * Added to CVS.
 *
 */

