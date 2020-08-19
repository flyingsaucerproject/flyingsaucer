/*
 * {{{ header & license
 * Copyright (c) 2004, 2005, 2008 Joshua Marinacci, Patrick Wright
 * Copyright (c) 2008 Patrick Wright
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


/**
 * Utility class for using the java.util.logging package. Relies on the standard
 * configuration for logging, but gives easier access to the various logs
 * (plumbing.load, .init, .render)
 *
 * @author empty
 */
public class XRLog {

    private static final List<String> LOGGER_NAMES = new ArrayList<String>(20);
    public static final String CONFIG = registerLoggerByName("org.xhtmlrenderer.config");
    public static final String EXCEPTION = registerLoggerByName("org.xhtmlrenderer.exception");
    public static final String GENERAL = registerLoggerByName("org.xhtmlrenderer.general");
    public static final String INIT = registerLoggerByName("org.xhtmlrenderer.init");
    public static final String JUNIT = registerLoggerByName("org.xhtmlrenderer.junit");
    public static final String LOAD = registerLoggerByName("org.xhtmlrenderer.load");
    public static final String MATCH = registerLoggerByName("org.xhtmlrenderer.match");
    public static final String CASCADE = registerLoggerByName("org.xhtmlrenderer.cascade");
    public static final String XML_ENTITIES = registerLoggerByName("org.xhtmlrenderer.load.xml-entities");
    public static final String CSS_PARSE = registerLoggerByName("org.xhtmlrenderer.css-parse");
    public static final String LAYOUT = registerLoggerByName("org.xhtmlrenderer.layout");
    public static final String RENDER = registerLoggerByName("org.xhtmlrenderer.render");

    private static boolean initPending = true;
    private static XRLogger loggerImpl;

    private static boolean loggingEnabled = true;

    private static String registerLoggerByName(final String loggerName) {
        LOGGER_NAMES.add(loggerName);
        return loggerName;
    }

    /**
     * Returns a list of all loggers that will be accessed by XRLog. Each entry is a String with a logger
     * name, which can be used to retrieve the logger using the corresponding Logging API; example name might be
     * "org.xhtmlrenderer.config"
     *
     * @return List of loggers, never null.
     */
    public static List<String> listRegisteredLoggers() {
        // defensive copy
        return new ArrayList<String>(LOGGER_NAMES);
    }


    public static void cssParse(String msg) {
        cssParse(Level.INFO, msg);
    }

    public static void cssParse(Level level, String msg) {
        log(CSS_PARSE, level, msg);
    }

    public static void cssParse(Level level, String msg, Throwable th) {
        log(CSS_PARSE, level, msg, th);
    }

    public static void xmlEntities(String msg) {
        xmlEntities(Level.INFO, msg);
    }

    public static void xmlEntities(Level level, String msg) {
        log(XML_ENTITIES, level, msg);
    }

    public static void xmlEntities(Level level, String msg, Throwable th) {
        log(XML_ENTITIES, level, msg, th);
    }

    public static void cascade(String msg) {
        cascade(Level.INFO, msg);
    }

    public static void cascade(Level level, String msg) {
        log(CASCADE, level, msg);
    }

    public static void cascade(Level level, String msg, Throwable th) {
        log(CASCADE, level, msg, th);
    }

    public static void exception(String msg) {
        exception(msg, null);
    }

    public static void exception(String msg, Throwable th) {
        log(EXCEPTION, Level.WARNING, msg, th);
    }

    public static void general(String msg) {
        general(Level.INFO, msg);
    }

    public static void general(Level level, String msg) {
        log(GENERAL, level, msg);
    }

    public static void general(Level level, String msg, Throwable th) {
        log(GENERAL, level, msg, th);
    }

    public static void init(String msg) {
        init(Level.INFO, msg);
    }

    public static void init(Level level, String msg) {
        log(INIT, level, msg);
    }

    public static void init(Level level, String msg, Throwable th) {
        log(INIT, level, msg, th);
    }

    public static void junit(String msg) {
        junit(Level.FINEST, msg);
    }

    public static void junit(Level level, String msg) {
        log(JUNIT, level, msg);
    }

    public static void junit(Level level, String msg, Throwable th) {
        log(JUNIT, level, msg, th);
    }

    public static void load(String msg) {
        load(Level.INFO, msg);
    }

    public static void load(Level level, String msg) {
        log(LOAD, level, msg);
    }

    public static void load(Level level, String msg, Throwable th) {
        log(LOAD, level, msg, th);
    }

    public static void match(String msg) {
        match(Level.INFO, msg);
    }

    public static void match(Level level, String msg) {
        log(MATCH, level, msg);
    }

    public static void match(Level level, String msg, Throwable th) {
        log(MATCH, level, msg, th);
    }

    public static void layout(String msg) {
        layout(Level.INFO, msg);
    }

    public static void layout(Level level, String msg) {
        log(LAYOUT, level, msg);
    }

    public static void layout(Level level, String msg, Throwable th) {
        log(LAYOUT, level, msg, th);
    }

    public static void render(String msg) {
        render(Level.INFO, msg);
    }

    public static void render(Level level, String msg) {
        log(RENDER, level, msg);
    }

    public static void render(Level level, String msg, Throwable th) {
        log(RENDER, level, msg, th);
    }

    public static synchronized void log(String where, Level level, String msg) {
        if (initPending) {
            init();
        }
        if (isLoggingEnabled()) {
            loggerImpl.log(where, level, msg);
        }
    }

    public static synchronized void log(String where, Level level, String msg, Throwable th) {
        if (initPending) {
            init();
        }
        if (isLoggingEnabled()) {
            loggerImpl.log(where, level, msg, th);
        }
    }

    public static void main(String[] args) {
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

    private static void init() {
        synchronized (XRLog.class) {
            if (!initPending) {
                return;
            }

            XRLog.setLoggingEnabled(Configuration.isTrue("xr.util-logging.loggingEnabled", true));

            if (loggerImpl == null) {
                loggerImpl = new JDKXRLogger();
            }

            initPending = false;
        }
    }

    public static synchronized void setLevel(String log, Level level) {
        if (initPending) {
            init();
        }
        loggerImpl.setLevel(log, level);
    }

    /**
     * Whether logging is on or off.
     *
     * @return Returns true if logging is enabled, false if not. Corresponds
     * to configuration file property xr.util-logging.loggingEnabled, or to
     * value passed to setLoggingEnabled(bool).
     */
    public static synchronized boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * Turns logging on or off, without affecting logging configuration.
     *
     * @param loggingEnabled Flag whether logging is enabled or not;
     *                       if false, all logging calls fail silently. Corresponds
     *                       to configuration file property xr.util-logging.loggingEnabled
     */
    public static synchronized void setLoggingEnabled(boolean loggingEnabled) {
        XRLog.loggingEnabled = loggingEnabled;
    }

    public static synchronized XRLogger getLoggerImpl() {
        return loggerImpl;
    }

    public static synchronized void setLoggerImpl(XRLogger loggerImpl) {
        XRLog.loggerImpl = loggerImpl;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.20  2010/01/13 01:28:46  peterbrant
 * Add synchronization to XRLog#log to avoid spurious errors on initializatoin
 *
 * Revision 1.19  2008/01/27 16:40:29  pdoubleya
 * Issues 186 and 130: fix configuration so that logging setup does not override any current settings for JDK logging classes. Disable logging by default.
 *
 * Revision 1.18  2007/09/10 20:28:26  peterbrant
 * Make underlying logging implementation pluggable / Add log4j logging implementation (not currently compiled with Ant to avoid additional compile time dependency)
 *
 * Revision 1.17  2007/06/02 20:00:34  peterbrant
 * Revert earlier change to default CSS parse logging level / Use WARNING explicitly for CSS parse errors
 *
 * Revision 1.16  2007/06/01 21:44:08  peterbrant
 * CSS parsing errors should be logged at WARNING, not INFO level
 *
 * Revision 1.15  2006/08/17 17:32:25  joshy
 * intial patch to fix the logging config issues
 * https://xhtmlrenderer.dev.java.net/issues/show_bug.cgi?id=130
 *
 * Revision 1.14  2006/07/26 17:59:01  pdoubleya
 * Use proper form for logging exceptions.
 *
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

