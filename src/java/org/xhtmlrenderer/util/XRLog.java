/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


/**
 * Utility class for using the java.util.logging package. Relies on the standard
 * configuration for logging, but gives easier access to the various logs
 * (plumbing.load, .init, .render)
 *
 * @author empty
 */
public class XRLog {

    /**
     * Description of the Field
     */
    private final static String CONFIG = "plumbing.config";
    /**
     * Description of the Field
     */
    private final static String EXCEPTION = "plumbing.exception";
    /**
     * Description of the Field
     */
    private final static String GENERAL = "plumbing.general";
    /**
     * Description of the Field
     */
    private final static String INIT = "plumbing.init";
    /**
     * Description of the Field
     */
    private final static String JUNIT = "plumbing.junit";
    /**
     * Description of the Field
     */
    private final static String LOAD = "plumbing.load";
    /**
     * Description of the Field
     */
    private final static String MATCH = "plumbing.match";
    /**
     * Description of the Field
     */
    private final static String CASCADE = "plumbing.cascade";
    /**
     * Description of the Field
     */
    private final static String XML_ENTITIES = "plumbing.load.xml-entities";
    /**
     * Description of the Field
     */
    private final static String CSS_PARSE = "plumbing.css-parse";
    /**
     * Description of the Field
     */
    private final static String LAYOUT = "plumbing.layout";
    /**
     * Description of the Field
     */
    private final static String RENDER = "plumbing.render";

    /**
     * Description of the Field
     */
    private static boolean initPending = true;

    private static boolean loggingEnabled = true;

    /**
     * Description of the Method
     *
     * @param msg PARAM
     */
    public static void cssParse(String msg) {
        cssParse(Level.INFO, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     */
    public static void cssParse(Level level, String msg) {
        log(CSS_PARSE, level, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     * @param th    PARAM
     */
    public static void cssParse(Level level, String msg, Throwable th) {
        log(CSS_PARSE, level, msg, th);
    }

    /**
     * Description of the Method
     *
     * @param msg PARAM
     */
    public static void xmlEntities(String msg) {
        xmlEntities(Level.INFO, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     */
    public static void xmlEntities(Level level, String msg) {
        log(XML_ENTITIES, level, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     * @param th    PARAM
     */
    public static void xmlEntities(Level level, String msg, Throwable th) {
        log(XML_ENTITIES, level, msg, th);
    }

    /**
     * Description of the Method
     *
     * @param msg PARAM
     */
    public static void cascade(String msg) {
        cascade(Level.INFO, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     */
    public static void cascade(Level level, String msg) {
        log(CASCADE, level, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     * @param th    PARAM
     */
    public static void cascade(Level level, String msg, Throwable th) {
        log(CASCADE, level, msg, th);
    }

    /**
     * Description of the Method
     *
     * @param msg PARAM
     */
    public static void exception(String msg) {
        exception(msg, null);
    }

    /**
     * Description of the Method
     *
     * @param msg PARAM
     * @param th  PARAM
     */
    public static void exception(String msg, Throwable th) {
        log(EXCEPTION, Level.WARNING, msg, th);
    }

    /**
     * Description of the Method
     *
     * @param msg PARAM
     */
    public static void general(String msg) {
        general(Level.INFO, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     */
    public static void general(Level level, String msg) {
        log(GENERAL, level, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     * @param th    PARAM
     */
    public static void general(Level level, String msg, Throwable th) {
        log(GENERAL, level, msg, th);
    }

    /**
     * Description of the Method
     *
     * @param msg PARAM
     */
    public static void init(String msg) {
        init(Level.INFO, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     */
    public static void init(Level level, String msg) {
        log(INIT, level, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     * @param th    PARAM
     */
    public static void init(Level level, String msg, Throwable th) {
        log(INIT, level, msg, th);
    }

    /**
     * Description of the Method
     *
     * @param msg PARAM
     */
    public static void junit(String msg) {
        junit(Level.FINEST, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     */
    public static void junit(Level level, String msg) {
        log(JUNIT, level, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     * @param th    PARAM
     */
    public static void junit(Level level, String msg, Throwable th) {
        log(JUNIT, level, msg, th);
    }

    /**
     * Description of the Method
     *
     * @param msg PARAM
     */
    public static void load(String msg) {
        load(Level.INFO, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     */
    public static void load(Level level, String msg) {
        log(LOAD, level, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     * @param th    PARAM
     */
    public static void load(Level level, String msg, Throwable th) {
        log(LOAD, level, msg, th);
    }

    /**
     * Description of the Method
     *
     * @param msg PARAM
     */
    public static void match(String msg) {
        match(Level.INFO, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     */
    public static void match(Level level, String msg) {
        log(MATCH, level, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     * @param th    PARAM
     */
    public static void match(Level level, String msg, Throwable th) {
        log(MATCH, level, msg, th);
    }

    /**
     * Description of the Method
     *
     * @param msg PARAM
     */
    public static void layout(String msg) {
        layout(Level.INFO, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     */
    public static void layout(Level level, String msg) {
        log(LAYOUT, level, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     * @param th    PARAM
     */
    public static void layout(Level level, String msg, Throwable th) {
        log(LAYOUT, level, msg, th);
    }

    /**
     * Description of the Method
     *
     * @param msg PARAM
     */
    public static void render(String msg) {
        render(Level.INFO, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     */
    public static void render(Level level, String msg) {
        log(RENDER, level, msg);
    }

    /**
     * Description of the Method
     *
     * @param level PARAM
     * @param msg   PARAM
     * @param th    PARAM
     */
    public static void render(Level level, String msg, Throwable th) {
        log(RENDER, level, msg, th);
    }

    /**
     * Description of the Method
     *
     * @param where PARAM
     * @param level PARAM
     * @param msg   PARAM
     */
    public static void log(String where, Level level, String msg) {
        if (initPending) {
            init();
        }
        if ( isLoggingEnabled()) getLogger(where).log(level, msg);
    }

    /**
     * Description of the Method
     *
     * @param where PARAM
     * @param level PARAM
     * @param msg   PARAM
     * @param th    PARAM
     */
    public static void log(String where, Level level, String msg, Throwable th) {
        if (initPending) {
            init();
        }
        getLogger(where).log(level, msg, th);
    }

    /**
     * Description of the Method
     *
     * @param args PARAM
     */
    public static void main(String args[]) {
        try {
            XRLog.cascade("Cascade msg");
            XRLog.cascade(Level.WARNING, "Cascade msg");
            XRLog.exception("Exception msg");
            XRLog.exception("Exception msg", new Exception());
            XRLog.general("General msg");
            XRLog.general(Level.WARNING, "General msg");
            XRLog.init("Init msg");
            XRLog.init(Level.WARNING, "Init msg");
            XRLog.load("Load msg");
            XRLog.load(Level.WARNING, "Load msg");
            XRLog.match("Match msg");
            XRLog.match(Level.WARNING, "Match msg");
            XRLog.layout("Layout msg");
            XRLog.layout(Level.WARNING, "Layout msg");
            XRLog.render("Render msg");
            XRLog.render(Level.WARNING, "Render msg");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     */
    private static void init() {
        synchronized (XRLog.class) {
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

                    if ( lmkey.equals("loggingEnabled")) {
                        setLoggingEnabled(Configuration.isTrue(fullkey, true));
                    }
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

                Configuration.setConfigLogger(Logger.getLogger(CONFIG));
            } catch (SecurityException e) {
                //throw new XRRuntimeException("Could not initialize logs. " + e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                throw new XRRuntimeException("Could not initialize logs. " + e.getLocalizedMessage());
            } catch (IOException e) {
                throw new XRRuntimeException("Could not initialize logs. " + e.getLocalizedMessage());
            }
        }
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

    public static void setLevel(String log, Level level) {
        getLogger(log).setLevel(level);
    }

    static {
    }// end main()

    /**
     * Whether logging is on or off.
     * @return Returns true if logging is enabled, false if not. Corresponds
     * to configuration file property xr.util-logging.loggingEnabled, or to
     * value passed to setLoggingEnabled(bool).
     */
    public static boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * Turns logging on or off, without affecting logging configuration.
     *
     * @param loggingEnabled Flag whether logging is enabled or not;
     * if false, all logging calls fail silently. Corresponds
     * to configuration file property xr.util-logging.loggingEnabled
     */
    public static void setLoggingEnabled(boolean loggingEnabled) {
        XRLog.loggingEnabled = loggingEnabled;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.13  2006/07/17 22:15:59  pdoubleya
 * Added loggingEnabled switch to XRLog and config file; default logging to off there and in Configuration. Fix for Issue Tracker #123.
 *
 * Revision 1.12  2005/07/13 22:49:15  joshy
 * updates to get the jnlp to work without being signed
 *
 * Revision 1.11  2005/06/26 01:21:35  tobega
 * Fixed possible infinite loop in init()
 *
 * Revision 1.10  2005/05/06 16:54:32  joshy
 * forgot to add this level stuff
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2005/04/07 16:14:28  pdoubleya
 * Updated to clarify relationship between Configuration and XRLog on load; Configuration must load first, but holds off on logging until XRLog is initialized. LogStartupConfig no longer used.
 *
 * Revision 1.8  2005/03/27 18:36:26  pdoubleya
 * Added separate logging for entity resolution.
 *
 * Revision 1.7  2005/01/29 20:18:38  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.6  2005/01/29 12:18:15  pdoubleya
 * Added cssParse logging.
 *
 * Revision 1.5  2005/01/24 19:01:10  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.4  2005/01/24 14:33:07  pdoubleya
 * Added junit logging hierarchy.
 *
 * Revision 1.3  2004/10/23 14:06:57  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

