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
 * This class is auto-loaded by the Logging system if
 * the system property
 * java.util.logging.config.class
 * is set to this class name (fully-qualified). This class
 * loads the LogManager with the logging properties from
 * the xr-logging.properties file, which must be on the 
 * CLASSPATH to be read.
 *
 * @author   Patrick Wright
 */
public class LogStartupConfig {
    /** Description of the Field */
    public final static String LOG_CONFIG_NAME = "resources/conf/xr-logging.properties";

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

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2004/10/13 23:00:33  pdoubleya
 * Added to CVS.
 *
 */

