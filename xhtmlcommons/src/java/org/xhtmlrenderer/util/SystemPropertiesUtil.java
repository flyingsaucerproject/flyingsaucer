/*
 * {{{ header & license
 * Copyright (c) 2008 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.util;

/**
 * Utility methods for working with System properties.
 */
public class SystemPropertiesUtil {
    /**
     * Attempts to retrieve a system property; if the property is not found, or if a SecurityException is thrown (for
     * example, in a sandbox environment) will return the default value. Will swallow stack traces and any
     * SecurityExceptions, and will not log any output to the console.
     *
     * @param propertyName property to retrieve
     * @param defaultVal value to use if not found, or not allowed to use the property
     * @return
     */
    public static String getPropertyOrDefaultSandbox(String propertyName, String defaultVal) {
        String val = defaultVal;
        try {
            val = System.getProperty(propertyName);
        } catch (SecurityException e) {
            // can happen in sandbox
        }
        return val;
    }
}
