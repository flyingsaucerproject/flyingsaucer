/*
 * {{{ header & license
 * LogStartupConfig.java
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
package org.xhtmlrenderer.util;

import java.io.*;
import java.util.*;
import java.util.logging.*;


/**
 * <p>
 *
 * This class is loaded by XRLog the first time XRLog is used.</p> <p>
 *
 * This class loads the LogManager with the logging properties from
 * Configuration, using keys prefixed by xr.util-logging. The rest of the key
 * should correspond to log-level settings as specified in the LogManager API
 * docs.</p> <p>
 *
 * Note that log level settings, handlers, and formatters in this
 * logging-properties file follows the conventions in LogManager. Configuration
 * also supports a formatting key when using the XRSimpleLogFormatter--since
 * LogManager properties doesn't support such special-case parameters. </p>
 *
 * @author   Patrick Wright
 */
public class LogStartupConfig {
    /** Constructor for the LogStartupConfig object */
    public LogStartupConfig() {
        try {
            LogManager lm = LogManager.getLogManager();

            // pull logging properties from configuration
            // they are all prefixed as shown
            String prefix = "xr.util-logging.";
            Iterator iter = Configuration.keysByPrefix( prefix );
            Properties props = new Properties();
            while ( iter.hasNext() ) {
                String fullkey = (String)iter.next();
                String lmkey = fullkey.substring( prefix.length() );
                props.setProperty( lmkey, Configuration.valueFor( fullkey ) );
            }

            // load our properties into our log manager
            // log manager can only read properties from an InputStream
            File f = File.createTempFile( "xr-log", null );
            FileOutputStream fos = new FileOutputStream( f );
            props.store( fos, "# Temporary properties file" );
            fos.close();

            FileInputStream fis = new FileInputStream( f );
            lm.readConfiguration( fis );
            fis.close();
            f.delete();
        } catch ( Throwable th ) {
            th.printStackTrace();
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2005/01/29 20:19:27  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.3  2004/10/18 12:07:17  pdoubleya
 * LogManager now initialized from main Configuration.
 *
 * Revision 1.2  2004/10/14 11:12:42  pdoubleya
 * Logging properties location now read from Configuration. Updated comments.
 *
 * Revision 1.1  2004/10/13 23:00:33  pdoubleya
 * Added to CVS.
 *
 */

