/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 * Copyright (c) 2007 Wisconsin Court System
 * Copyright (c) 2008 Patrick Wright
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
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.Formatter;
import java.util.Properties;
import java.util.Iterator;
import java.util.List;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * An {@link XRLogger} interface that uses <code>java.util.logging</code>.
 */
public class JDKXRLogger implements XRLogger {
    private static boolean initPending = true;
    
    /** {@inheritdoc} */
    public void log(String where, Level level, String msg) {
        if (initPending) {
            init();
        }
        
        getLogger(where).log(level, msg);
    }

    /** {@inheritdoc} */
    public void log(String where, Level level, String msg, Throwable th) {
        if (initPending) {
            init();
        }

        getLogger(where).log(level, msg, th);
    }

    /** {@inheritdoc} */
    public void setLevel(String logger, Level level) {
        getLogger(logger).setLevel(level);
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
                Properties props = retrieveLoggingProperties();

                if(!XRLog.isLoggingEnabled()) {
                    Configuration.setConfigLogger(Logger.getLogger(XRLog.CONFIG));
                    return;
                }
                initializeJDKLogManager(props);

                Configuration.setConfigLogger(Logger.getLogger(XRLog.CONFIG));
            } catch (SecurityException e) {
                // may happen in a sandbox environment
            } catch (FileNotFoundException e) {
                throw new XRRuntimeException("Could not initialize logs. " + e.getLocalizedMessage(), e);
            } catch (IOException e) {
                throw new XRRuntimeException("Could not initialize logs. " + e.getLocalizedMessage(), e);
            }
        }
    }

    private static Properties retrieveLoggingProperties() {
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
        return props;
    }

    private static void initializeJDKLogManager(final Properties fsLoggingProperties) throws IOException {
        final List loggers = retrieveLoggers();

        configureLoggerHandlerForwarding(fsLoggingProperties, loggers);

        // load our properties into our log manager
        Enumeration keys = fsLoggingProperties.keys();
        Map handlers = new HashMap();
        Map handlerFormatterMap = new HashMap();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String prop = fsLoggingProperties.getProperty(key);
            if (key.endsWith("level")) {
                configureLogLevel(key, prop);
            } else if (key.endsWith("handlers")) {
                handlers = configureLogHandlers(loggers, prop);
            } else if (key.endsWith("formatter")) {
                String k2 = key.substring(0, key.length() - ".formatter".length());
                handlerFormatterMap.put(k2, prop);
            }
        }

        // formatters apply to a specific handler we have initialized previously,
        // hence we need to wait until we've parsed the handler class
        for (Iterator it = handlerFormatterMap.keySet().iterator(); it.hasNext();) {
            String handlerClassName = (String) it.next();
            String formatterClassName = (String) handlerFormatterMap.get(handlerClassName);
            assignFormatter(handlers, handlerClassName, formatterClassName);
        }
    }

    private static void configureLoggerHandlerForwarding(Properties fsLoggingProperties, List loggers) {
        String val = fsLoggingProperties.getProperty("use-parent-handler");

        boolean flag = val == null ? false : Boolean.valueOf(val).booleanValue();
        for (Iterator it = loggers.iterator(); it.hasNext();) {
            Logger logger = (Logger) it.next();
            logger.setUseParentHandlers(flag);
        }
    }

    private static void assignFormatter(Map handlers, String handlerClassName, String formatterClassName) {
        Handler handler = (Handler) handlers.get(handlerClassName);
        if (handler != null) {
            try {
                Class fclass = Class.forName(formatterClassName);
                Formatter f = (Formatter) fclass.newInstance();
                handler.setFormatter(f);
            } catch (ClassNotFoundException e) {
                throw new XRRuntimeException("Could not initialize logging properties; " +
                        "Formatter class not found: " + formatterClassName);
            } catch (IllegalAccessException e) {
                throw new XRRuntimeException("Could not initialize logging properties; " +
                        "Can't instantiate Formatter class (IllegalAccessException): " + formatterClassName);
            } catch (InstantiationException e) {
                throw new XRRuntimeException("Could not initialize logging properties; " +
                        "Can't instantiate Formatter class (InstantiationException): " + formatterClassName);
            }
        }
    }

    /**
     * Returns a List of all Logger instances used by Flying Saucer from the JDK LogManager; these will
     * be automatically created if they aren't already available.
     */
    private static List retrieveLoggers() {
        List loggerNames = XRLog.listRegisteredLoggers();
        List loggers = new ArrayList(loggerNames.size());
        Iterator it = loggerNames.iterator();
        while (it.hasNext()) {
            final String ln = (String) it.next();
            loggers.add(Logger.getLogger(ln));
        }
        return loggers;
    }

    /**
     * For each logger provided, assigns the logger an instance of the named log output handlers. Will attempt
     * to instantiate each handler; any which can't be instantiated will cause the method to throw a RuntimeException.
     *
     * @param loggers List of Logger instances.
     * @param handlerClassList A space-separated string (following the configuration convention for JDK logging
     * configuration files, for handlers) of FQN of log handlers.
     *
     * @return Map of handler class names to handler instances.
     */
    private static Map configureLogHandlers(List loggers, final String handlerClassList) {
        final String[] names = handlerClassList.split(" ");
        final Map handlers = new HashMap(names.length);
        for (int i = 0; i < names.length; i++) {
            final String name = names[i];
            try {
                Class handlerClass = Class.forName(name);
                Handler handler = (Handler) handlerClass.newInstance();
                handlers.put(name, handler);
            } catch (ClassNotFoundException e) {
                throw new XRRuntimeException("Could not initialize logging properties; " +
                        "Handler class not found: " + name);
            } catch (IllegalAccessException e) {
                throw new XRRuntimeException("Could not initialize logging properties; " +
                        "Can't instantiate Handler class (IllegalAccessException): " + name);
            } catch (InstantiationException e) {
                throw new XRRuntimeException("Could not initialize logging properties; " +
                        "Can't instantiate Handler class (InstantiationException): " + name);
            }
        }

        // now assign each handler to each FS logger
        for (Iterator iterator = loggers.iterator(); iterator.hasNext();) {
            Logger logger = (Logger) iterator.next();
            for (Iterator ith = handlers.values().iterator(); ith.hasNext();) {
                Handler handler = (Handler) ith.next();
                logger.addHandler(handler);
            }
        }
        return handlers;
    }

    /**
     * Parses the levelValue into a Level instance and assigns to the Logger instance named by loggerName; if the
     * the levelValue is invalid (e.g. misspelled), assigns Level.OFF to the logger.
     */
    private static void configureLogLevel(String loggerName, String levelValue) {
        final Level level = LoggerUtil.parseLogLevel(levelValue, Level.OFF);
        final Logger logger = Logger.getLogger(loggerName);
        logger.setLevel(level);
    }
}
