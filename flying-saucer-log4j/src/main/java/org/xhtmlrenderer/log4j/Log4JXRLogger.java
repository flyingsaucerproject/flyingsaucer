/*
 * {{{ header & license
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
package org.xhtmlrenderer.log4j;

import com.google.errorprone.annotations.CheckReturnValue;
import org.apache.logging.log4j.LogManager;
import org.jspecify.annotations.Nullable;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

@SuppressWarnings("SpellCheckingInspection")
public class Log4JXRLogger implements XRLogger {

    private static final String DEFAULT_LOGGER_NAME = "org.xhtmlrenderer.other";

    private static final Map<String, String> LOGGER_NAME_MAP = new HashMap<>();

    static {
        LOGGER_NAME_MAP.put(XRLog.CONFIG, "org.xhtmlrenderer.config");
        LOGGER_NAME_MAP.put(XRLog.EXCEPTION, "org.xhtmlrenderer.exception");
        LOGGER_NAME_MAP.put(XRLog.GENERAL, "org.xhtmlrenderer.general");
        LOGGER_NAME_MAP.put(XRLog.INIT, "org.xhtmlrenderer.init");
        LOGGER_NAME_MAP.put(XRLog.JUNIT, "org.xhtmlrenderer.junit");
        LOGGER_NAME_MAP.put(XRLog.LOAD, "org.xhtmlrenderer.load");
        LOGGER_NAME_MAP.put(XRLog.MATCH, "org.xhtmlrenderer.match");
        LOGGER_NAME_MAP.put(XRLog.CASCADE, "org.xhtmlrenderer.cascade");
        LOGGER_NAME_MAP.put(XRLog.XML_ENTITIES, "org.xhtmlrenderer.load.xmlentities");
        LOGGER_NAME_MAP.put(XRLog.CSS_PARSE, "org.xhtmlrenderer.cssparse");
        LOGGER_NAME_MAP.put(XRLog.LAYOUT, "org.xhtmlrenderer.layout");
        LOGGER_NAME_MAP.put(XRLog.RENDER, "org.xhtmlrenderer.render");
    }

    private String defaultLoggerName = DEFAULT_LOGGER_NAME;
    private Map<String, String> loggerNameMap = LOGGER_NAME_MAP;

    @Override
    public void log(String where, Level level, String msg) {
        LogManager.getLogger(getLoggerName(where)).log(toLog4JLevel(level), msg);
    }

    @Override
    public void log(String where, Level level, String msg, @Nullable Throwable th) {
        LogManager.getLogger(getLoggerName(where)).log(toLog4JLevel(level), msg, th);
    }

    @CheckReturnValue
    private org.apache.logging.log4j.Level toLog4JLevel(Level level) {
        if (level == Level.SEVERE) {
            return org.apache.logging.log4j.Level.ERROR;
        } else if (level == Level.WARNING) {
            return org.apache.logging.log4j.Level.WARN;
        } else if (level == Level.INFO) {
            return org.apache.logging.log4j.Level.INFO;
        } else if (level == Level.CONFIG) {
            return org.apache.logging.log4j.Level.INFO;
        } else if (level == Level.FINE || level == Level.FINER || level == Level.FINEST) {
            return org.apache.logging.log4j.Level.DEBUG;
        } else {
            return org.apache.logging.log4j.Level.INFO;
        }
    }

    @CheckReturnValue
    private String getLoggerName(String xrLoggerName) {
        String result = loggerNameMap.get(xrLoggerName);
        return requireNonNullElse(result, defaultLoggerName);
    }

    @Override
    public void setLevel(String logger, Level level) {
        throw new UnsupportedOperationException("log4j should be not be configured here");
    }

    @CheckReturnValue
    public Map<String, String> getLoggerNameMap() {
        return loggerNameMap;
    }

    public void setLoggerNameMap(Map<String, String> loggerNameMap) {
        this.loggerNameMap = requireNonNull(loggerNameMap);
    }

    @CheckReturnValue
    public String getDefaultLoggerName() {
        return defaultLoggerName;
    }

    public void setDefaultLoggerName(String defaultLoggerName) {
        this.defaultLoggerName = requireNonNull(defaultLoggerName);
    }

}
