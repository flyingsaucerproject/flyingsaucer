/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * An {@link XRLogger} interface that uses <code>java.util.logging</code>.
 */
public class JDKXRLogger implements XRLogger {
    private static boolean initPending = true;
    
    public void log(String where, Level level, String msg) {
        if (initPending) {
            init();
        }
        
        getLogger(where).log(level, msg);
    }

    public void log(String where, Level level, String msg, Throwable th) {
        if (initPending) {
            init();
        }
        
        getLogger(where).log(level, msg, th);
    }   
    
    /**
     * Same purpose as Logger.getLogger(), except that the static initialization
     * for XRLog will initialize the LogManager with logging levels and other
     * configuration. Use this instead of Logger.getLogger()
     *
     * @param log PARAM
     * @return The logger value
     */
    private static Logger getLogger(String log) {
        return Logger.getLogger(log);
    }    

    private static void init() {
        synchronized (JDKXRLogger.class) {
            if (!initPending) {
                return;
            }
            //now change this immediately, in case something fails
            initPending = false;
            try {
                // pull logging properties from configuration
                // they are all prefixed as shown
                String prefix = "xr.util-logging.";
                Iterator iter = Configuration.keysByPrefix(prefix);
                Properties props = new Properties();
                while (iter.hasNext()) {
                    String fullkey = (String) iter.next();
                    String lmkey = fullkey.substring(prefix.length());
                    String value = Configuration.valueFor(fullkey);
                    props.setProperty(lmkey, value);
                }
                
                if(!XRLog.isLoggingEnabled()) {
                    Configuration.setConfigLogger(Logger.getLogger(XRLog.CONFIG));
                    return;
                }

                // load our properties into our log manager
                // log manager can only read properties from an InputStream
                File f = File.createTempFile("xr-log", null);
                FileOutputStream fos = new FileOutputStream(f);
                props.store(fos, "# Temporary properties file");
                fos.close();

                FileInputStream fis = new FileInputStream(f);
                LogManager.getLogManager().readConfiguration(fis);
                fis.close();
                f.delete();

                Configuration.setConfigLogger(Logger.getLogger(XRLog.CONFIG));
            } catch (SecurityException e) {
                //throw new XRRuntimeException("Could not initialize logs. " + e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                throw new XRRuntimeException("Could not initialize logs. " + e.getLocalizedMessage(), e);
            } catch (IOException e) {
                throw new XRRuntimeException("Could not initialize logs. " + e.getLocalizedMessage(), e);
            }
        }
    }

    public void setLevel(String logger, Level level) {
        getLogger(logger).setLevel(level);
    }
}
