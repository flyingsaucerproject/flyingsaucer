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
 * <p>
 *
 * Stores runtime configuration information for application parameters that may
 * vary on restarting. This implements the Singleton pattern, but through static
 * methods. That is, the first time Configuration is used, the properties are
 * loaded into the Singleton instance. Subsequent calls to valueFor() retrieve
 * values from the Singleton. To look up a property, use <code>Configuration.valueFor("property name");</code>
 * </p> <p>
 *
 * Properties may be overridden using a second properties file, or individually
 * using System properties specified on the command line. To override using a
 * second properties file, specify the System property xr-props. This should be
 * the location of the second file relative to the CLASSPATH, e.g. <code>java -Dxr-props=resources/conf/myprops.conf</code>
 * </p><p>
 *
 * To override a property using the System properties, just re-define the
 * property on the command line. e.g. <code>java -Dxr.property-name=new_value</code>
 * You can override as many properties as you like. </p> <p>
 *
 * Note that overrides are driven by the property names in the default
 * configuration file. Specifying a property name not in that file will have no
 * effect--the property will not be loaded or available for lookup.
 * Configuration is NOT used to control logging levels or output; see
 * LogStartupConfig.</p>
 *
 * <p>There are convenience converstion method for all the primitive types, 
 * in methods like valueAsInt(). A default must always be provided for these
 * methods. The default is returned if the value is not found, or if the 
 * conversion from String fails. If the value is not present, or the conversion
 * fails, a warning message is written to the log.</p>
 *
 * @author   Patrick Wright
 */
public class Configuration {
    /** Our backing data store of properties. */
    private Properties properties;

    /** The logger for messages on loading configuration. */
    private final Logger logger;

    /** The Singleton instance of the class. */
    private static Configuration sInstance;

    /** The location of our default properties file; must be on the CLASSPATH. */
    private final static String SF_FILE_NAME = "resources/conf/xhtmlrenderer.conf";

    /** Default constructor. */
    private Configuration() {
        // We need to create our logger with a separate ConsoleHandler
        // because the regular one is initialized using the Configuration
        // class--unfortunately, this means we can't control the output
        // without recompiling.
        logger = Logger.getLogger( "plumbing.init" );
        Handler handler = new ConsoleHandler();

        // Change this value if you want more detail about the
        // Configuration load--e.g. FINER for details on what
        // the properties are, what overrides are in place
        handler.setLevel( Level.INFO );
        logger.addHandler( handler );

        loadDefaultProperties();
        loadOverrideProperties();
        loadSystemProperties();
        logAfterLoad();
    }


    /** Loads the default set of properties, which may be overridden. */
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
        logger.info( "Configuration loaded from " + SF_FILE_NAME );
    }

    /**
     * Loads overriding property values from a second configuration file; this
     * is optional. See class documentation.
     */
    private void loadOverrideProperties() {
        String override = System.getProperty( "xr-props" );
        if ( override != null ) {
            logger.fine( "Configuration override file specified as " + override + ", looking for it." );
            InputStream readStream = GeneralUtil.openStreamFromClasspath( override );
            if ( readStream == null ) {
                logger.warning( "Could not find override properties file at " + override + ", using defaults." );
                return;
            } else {
                Properties temp = new Properties();
                try {
                    temp.load( readStream );
                } catch ( IOException iex ) {
                    logger.log( Level.WARNING, "Error while loading override properties file; skipping.", iex );
                    return;
                }

                Enumeration elem = this.properties.keys();
                int cnt = 0;
                while ( elem.hasMoreElements() ) {
                    String key = (String)elem.nextElement();
                    String val = temp.getProperty( key );
                    if ( val != null ) {
                        this.properties.setProperty( key, val );
                        logger.finer( "  " + key + " -> " + val );
                        cnt++;
                    }
                }
                logger.finer( "Configuration: " + cnt + " properties overridden from override properties file." );
            }
        }
    }

    /**
     * Loads overriding property values from a System properties; this is
     * optional. See class documentation.
     */
    private void loadSystemProperties() {
        Enumeration elem = properties.keys();
        logger.fine( "Overriding loaded configuration from System properties." );
        int cnt = 0;
        while ( elem.hasMoreElements() ) {
            String key = (String)elem.nextElement();
            String val = System.getProperty( key );
            if ( val != null ) {
                properties.setProperty( key, val );
                logger.finer( "  Overrode value for " + key );
                cnt++;
            }
        }
        logger.finer( "Configuration: " + cnt + " properties overridden from System properties." );
    }

    /** Writes a log of loaded properties to the plumbing.init Logger. */
    private void logAfterLoad() {
        Enumeration elem = properties.keys();
        logger.finer( "Configuration contains " + properties.size() + " keys." );
        logger.finer( "List of configuration properties, after override:" );
        while ( elem.hasMoreElements() ) {
            String key = (String)elem.nextElement();
            String val = properties.getProperty( key );
            logger.finer( "  " + key + " = " + val );
        }
        logger.finer( "Properties list complete." );
    }

    /**
     * Returns the value for key in the Configuration. A warning is issued to
     * the log if the property is not defined.
     *
     * @param key  Name of the property.
     * @return     Value assigned to the key, as a String.
     */
    public static String valueFor( String key ) {
        Configuration conf = instance();
        String val = conf.properties.getProperty( key );
        if ( val == null ) {
            conf.logger.warning( "CONFIGURATION: no value found for key " + key );
        }
        return val;
    }

    /**
     * Returns the value for key in the Configuration as a byte, or the default
     * provided value if not found or if the value is not a valid byte. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key         Name of the property.
     * @param defaultVal  PARAM
     * @return            Value assigned to the key, as a String.
     */
    public static int valueAsByte( String key, byte defaultVal ) {
        String val = valueFor( key );
        if ( val == null ) {
            return defaultVal;
        }

        byte bval = -1;
        try {
            bval = Byte.valueOf( val ).byteValue();
        } catch ( NumberFormatException nex ) {
            XRLog.exception( "Property '" + key + "' was requested as a byte, but " +
                    "value of '" + val + "' is not a byte. Check configuration." );
            bval = defaultVal;
        }
        return bval;
    }

    /**
     * Returns the value for key in the Configuration as a short, or the default
     * provided value if not found or if the value is not a valid short. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key         Name of the property.
     * @param defaultVal  PARAM
     * @return            Value assigned to the key, as a String.
     */
    public static int valueAsShort( String key, short defaultVal ) {
        String val = valueFor( key );
        if ( val == null ) {
            return defaultVal;
        }

        short sval = -1;
        try {
            sval = Short.valueOf( val ).shortValue();
        } catch ( NumberFormatException nex ) {
            XRLog.exception( "Property '" + key + "' was requested as a short, but " +
                    "value of '" + val + "' is not a short. Check configuration." );
            sval = defaultVal;
        }
        return sval;
    }

    /**
     * Returns the value for key in the Configuration as an integer, or a
     * default value if not found or if the value is not a valid integer. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key         Name of the property.
     * @param defaultVal  PARAM
     * @return            Value assigned to the key, as a String.
     */
    public static int valueAsInt( String key, int defaultVal ) {
        String val = valueFor( key );
        if ( val == null ) {
            return defaultVal;
        }

        int ival = -1;
        try {
            ival = Integer.valueOf( val ).intValue();
        } catch ( NumberFormatException nex ) {
            XRLog.exception( "Property '" + key + "' was requested as an integer, but " +
                    "value of '" + val + "' is not an integer. Check configuration." );
            ival = defaultVal;
        }
        return ival;
    }

    /**
     * Returns the value for key in the Configurationas a long, or the default
     * provided value if not found or if the value is not a valid long. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key         Name of the property.
     * @param defaultVal  PARAM
     * @return            Value assigned to the key, as a String.
     */
    public static long valueAsLong( String key, long defaultVal ) {
        String val = valueFor( key );
        if ( val == null ) {
            return defaultVal;
        }

        long lval = -1;
        try {
            lval = Long.valueOf( val ).longValue();
        } catch ( NumberFormatException nex ) {
            XRLog.exception( "Property '" + key + "' was requested as a long, but " +
                    "value of '" + val + "' is not a long. Check configuration." );
            lval = defaultVal;
        }
        return lval;
    }

    /**
     * Returns the value for key in the Configuration as a float, or the default
     * provided value if not found or if the value is not a valid float. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key         Name of the property.
     * @param defaultVal  PARAM
     * @return            Value assigned to the key, as a String.
     */
    public static float valueAsFloat( String key, float defaultVal ) {
        String val = valueFor( key );
        if ( val == null ) {
            return defaultVal;
        }

        float fval = -1;
        try {
            fval = Float.valueOf( val ).floatValue();
        } catch ( NumberFormatException nex ) {
            XRLog.exception( "Property '" + key + "' was requested as a float, but " +
                    "value of '" + val + "' is not a float. Check configuration." );
            fval = defaultVal;
        }
        return fval;
    }

    /**
     * Returns the value for key in the Configuration as a double, or the
     * default provided value if not found or if the value is not a valid
     * double. A warning is issued to the log if the property is not defined, or
     * if the conversion from String fails.
     *
     * @param key         Name of the property.
     * @param defaultVal  PARAM
     * @return            Value assigned to the key, as a String.
     */
    public static double valueAsDouble( String key, double defaultVal ) {
        String val = valueFor( key );
        if ( val == null ) {
            return defaultVal;
        }

        double dval = -1;
        try {
            dval = Double.valueOf( val ).doubleValue();
        } catch ( NumberFormatException nex ) {
            XRLog.exception( "Property '" + key + "' was requested as a double, but " +
                    "value of '" + val + "' is not a double. Check configuration." );
            dval = defaultVal;
        }
        return dval;
    }

    /**
     * Returns the value for key in the Configuration, or the default provided
     * value if not found. A warning is issued to the log if the property is not
     * defined, and if the default is null.
     *
     * @param key         Name of the property.
     * @param defaultVal  PARAM
     * @return            Value assigned to the key, as a String.
     */
    public static String valueFor( String key, String defaultVal ) {
        Configuration conf = instance();
        String val = conf.valueFor( key );
        val = ( val == null ? defaultVal : val );
        if ( val == null ) {
            conf.logger.warning( "CONFIGURATION: no value found for key " + key + " and no default given." );
        }
        return val;
    }


    /**
     * Command-line execution for testing. No arguments.
     *
     * @param args  Ignored
     */
    public static void main( String args[] ) {
        try {
            System.out.println( "byte: " + String.valueOf( Configuration.valueAsByte( "xr.test-config-byte", (byte)15 ) ) );
            System.out.println( "short: " + String.valueOf( Configuration.valueAsShort( "xr.test-config-short", (short)20 ) ) );
            System.out.println( "int: " + String.valueOf( Configuration.valueAsInt( "xr.test-config-int", 25 ) ) );
            System.out.println( "long: " + String.valueOf( Configuration.valueAsLong( "xr.test-config-long", 30L ) ) );
            System.out.println( "float: " + String.valueOf( Configuration.valueAsFloat( "xr.test-config-float", 45.5F ) ) );
            System.out.println( "double: " + String.valueOf( Configuration.valueAsDouble( "xr.test-config-double", 50.75D ) ) );
            System.out.println( "boolean: " + String.valueOf( Configuration.isTrue( "xr.test-config-boolean", false ) ) );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns true if the value is "true" (ignores case), or the default
     * provided value if not found or if the value is not a valid boolean (true
     * or false, ignores case). A warning is issued to the log if the property
     * is not defined, and if the default is null.
     *
     * @param key         Name of the property.
     * @param defaultVal  PARAM
     * @return            Value assigned to the key, as a String.
     */
    public static boolean isTrue( String key, boolean defaultVal ) {
        String val = valueFor( key );
        if ( val == null ) {
            return defaultVal;
        }

        if ( "true|false".indexOf( val ) == -1 ) {
            XRLog.exception( "Property '" + key + "' was requested as a boolean, but " +
                    "value of '" + val + "' is not a boolean. Check configuration." );
            return defaultVal;
        } else {
            return Boolean.valueOf( val ).booleanValue();
        }
    }

    /**
     * Returns true if the value is not "true" (ignores case), or the default
     * provided value if not found or if the value is not a valid boolean (true
     * or false, ignores case). A warning is issued to the log if the property
     * is not defined, or the value is not a valid boolean.
     *
     * @param key         Name of the property.
     * @param defaultVal  PARAM
     * @return            Value assigned to the key, as a String.
     */
    public static boolean isFalse( String key, boolean defaultVal ) {
        return !isTrue( key, defaultVal );
    }

    /**
     * @return   The singleton instance of the class.
     */
    private static synchronized Configuration instance() {
        if ( Configuration.sInstance == null ) {
            Configuration.sInstance = new Configuration();
        }
        return Configuration.sInstance;
    }// end main()
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2004/10/14 15:06:27  pdoubleya
 * Added conversion methods for primitive datatypes, and testing in main().
 *
 * Revision 1.3  2004/10/14 12:55:28  pdoubleya
 * Use plumbing.init logging hierarchy instead of plumbing.config.
 *
 * Revision 1.2  2004/10/14 11:12:05  pdoubleya
 * Value for is now static, going against Singleton instance. Created separate logger/handler for Configuration use only, and there is a single Logger. Loggers now use INFO, FINER, FINEST for logging progess in loading configuration. Updated comments.
 *
 * Revision 1.1  2004/10/13 23:00:31  pdoubleya
 * Added to CVS.
 *
 */

