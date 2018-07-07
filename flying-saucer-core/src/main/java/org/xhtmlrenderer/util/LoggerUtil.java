/*
 * {{{ header & license
 * LoggerUtil.java
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

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Utility class for working with java.logging Logger classes
 *
 * @author Patrick Wright
 */
public class LoggerUtil {

    /**
     * Instantiate a Logger for debug messages for a given class.
     *
     * @param cls PARAM
     * @return The debugLogger value
     */
    public static Logger getDebugLogger(Class cls) {
        Logger l = Logger.getLogger(cls.getName());
        l.setLevel(Level.ALL);
        return l;
    }

    public static Level parseLogLevel(String val, Level defaultLogLevel) {
        if ("ALL".equals(val)) {
            return Level.ALL;
        }
        if ("CONFIG".equals(val)) {
            return Level.CONFIG;
        }
        if ("FINE".equals(val)) {
            return Level.FINE;
        }
        if ("FINER".equals(val)) {
            return Level.FINER;
        }
        if ("FINEST".equals(val)) {
            return Level.FINEST;
        }
        if ("INFO".equals(val)) {
            return Level.INFO;
        }
        if ("OFF".equals(val)) {
            return Level.OFF;
        }
        if ("SEVERE".equals(val)) {
            return Level.SEVERE;
        }
        if ("WARNING".equals(val)) {
            return Level.WARNING;
        }
        return defaultLogLevel;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2008/01/27 16:40:29  pdoubleya
 * Issues 186 and 130: fix configuration so that logging setup does not override any current settings for JDK logging classes. Disable logging by default.
 *
 * Revision 1.3  2005/01/29 20:19:27  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.2  2004/10/23 14:06:57  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

