/*
 * {{{ header & license
 * LogStartupConfig.java
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
import java.util.logging.*;


/**
 * <p>
 *
 * This class is auto-loaded by the Logging system if the *System* property
 * java.util.logging.config.class is set to this class name (fully-qualified)
 * the first time the LogManager is loaded directly or indirectly. <p>
 *
 * This class loads the LogManager with the logging properties from a file,
 * which must be on the CLASSPATH to be read. The location of the file is read
 * from Configuration, using the key xr.logging-properties-default. 
 *
 * <p>Note that
 * log level settings, handlers, and formatters in this logging-properties file follows the conventions in
 * LogManager. You cannot override logging settings in the main Configuration
 * file. Configuration is only used to identify the location of the logging
 * properties file. Configuration does support a formatting key when using
 * the XRSimpleLogFormatter--since LogManager properties doesn't support such
 * special-case parameters. Phew!
 *
 * @author   Patrick Wright
 */
public class LogStartupConfig {
    /** Location of the logging configuration file, on the CLASSPATH. */
    public final static String LOG_CONFIG_NAME;

    /** Constructor for the LogStartupConfig object */
    public LogStartupConfig() {
        LogManager lm = LogManager.getLogManager();

        try {
            InputStream is = GeneralUtil.openStreamFromClasspath( LOG_CONFIG_NAME );
            lm.readConfiguration( is );
            is.close();
        } catch ( IOException ex ) {
            System.err.println( "Log configuration: could not load log configuration file at " + LOG_CONFIG_NAME + ", using JDK/JRE defaults." );
            ex.printStackTrace();
        }
    }

    static {
        LOG_CONFIG_NAME = Configuration.valueFor( "xr.logging-properties-default" );
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2004/10/14 11:12:42  pdoubleya
 * Logging properties location now read from Configuration. Updated comments.
 *
 * Revision 1.1  2004/10/13 23:00:33  pdoubleya
 * Added to CVS.
 *
 */

