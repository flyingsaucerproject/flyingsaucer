/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
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

import java.util.logging.*;


/**
 * Utility class for using the java.util.logging package. Relies on the standard
 * configuration for logging, but gives easier access to the various logs
 * (plumbing.load, .init, .render)
 *
 * @author   empty
 */
public class XRLog {

    /** Description of the Field */
    private final static String EXCEPTION = "plumbing.exception";
    /** Description of the Field */
    private final static String GENERAL = "plumbing.general";
    /** Description of the Field */
    private final static String INIT = "plumbing.init";
    /** Description of the Field */
    private final static String JUNIT = "plumbing.junit";
    /** Description of the Field */
    private final static String LOAD = "plumbing.load";
    /** Description of the Field */
    private final static String MATCH = "plumbing.match";
    /** Description of the Field */
    private final static String CASCADE = "plumbing.cascade";
    /** Description of the Field */
    private final static String LAYOUT = "plumbing.layout";
    /** Description of the Field */
    private final static String RENDER = "plumbing.render";

    /**
     * Description of the Method
     *
     * @param msg  PARAM
     */
    public static void cascade( String msg ) {
        cascade( Level.INFO, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     */
    public static void cascade( Level level, String msg ) {
        log( CASCADE, level, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     * @param th     PARAM
     */
    public static void cascade( Level level, String msg, Throwable th ) {
        log( CASCADE, level, msg, th );
    }

    /**
     * Description of the Method
     *
     * @param msg  PARAM
     */
    public static void exception( String msg ) {
        exception( msg, null );
    }

    /**
     * Description of the Method
     *
     * @param msg  PARAM
     * @param th   PARAM
     */
    public static void exception( String msg, Throwable th ) {
        log( EXCEPTION, Level.WARNING, msg, th );
    }

    /**
     * Description of the Method
     *
     * @param msg  PARAM
     */
    public static void general( String msg ) {
        general( Level.INFO, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     */
    public static void general( Level level, String msg ) {
        log( GENERAL, level, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     * @param th     PARAM
     */
    public static void general( Level level, String msg, Throwable th ) {
        log( GENERAL, level, msg, th );
    }

    /**
     * Description of the Method
     *
     * @param msg  PARAM
     */
    public static void init( String msg ) {
        init( Level.INFO, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     */
    public static void init( Level level, String msg ) {
        log( INIT, level, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     * @param th     PARAM
     */
    public static void init( Level level, String msg, Throwable th ) {
        log( INIT, level, msg, th );
    }

    /**
     * Description of the Method
     *
     * @param msg  PARAM
     */
    public static void junit( String msg ) {
        // CLEAN
        //System.out.println("JUNIT: " + msg);
        junit( Level.FINEST, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     */
    public static void junit( Level level, String msg ) {
        log( JUNIT, level, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     * @param th     PARAM
     */
    public static void junit( Level level, String msg, Throwable th ) {
        log( JUNIT, level, msg, th );
    }

    /**
     * Description of the Method
     *
     * @param msg  PARAM
     */
    public static void load( String msg ) {
        load( Level.INFO, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     */
    public static void load( Level level, String msg ) {
        log( LOAD, level, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     * @param th     PARAM
     */
    public static void load( Level level, String msg, Throwable th ) {
        log( LOAD, level, msg, th );
    }

    /**
     * Description of the Method
     *
     * @param msg  PARAM
     */
    public static void match( String msg ) {
        match( Level.INFO, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     */
    public static void match( Level level, String msg ) {
        log( MATCH, level, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     * @param th     PARAM
     */
    public static void match( Level level, String msg, Throwable th ) {
        log( MATCH, level, msg, th );
    }

    /**
     * Description of the Method
     *
     * @param msg  PARAM
     */
    public static void layout( String msg ) {
        layout( Level.INFO, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     */
    public static void layout( Level level, String msg ) {
        log( LAYOUT, level, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     * @param th     PARAM
     */
    public static void layout( Level level, String msg, Throwable th ) {
        log( LAYOUT, level, msg, th );
    }

    /**
     * Description of the Method
     *
     * @param msg  PARAM
     */
    public static void render( String msg ) {
        render( Level.INFO, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     */
    public static void render( Level level, String msg ) {
        log( RENDER, level, msg );
    }

    /**
     * Description of the Method
     *
     * @param level  PARAM
     * @param msg    PARAM
     * @param th     PARAM
     */
    public static void render( Level level, String msg, Throwable th ) {
        log( RENDER, level, msg, th );
    }

    /**
     * Description of the Method
     *
     * @param where  PARAM
     * @param level  PARAM
     * @param msg    PARAM
     */
    public static void log( String where, Level level, String msg ) {
        getLogger( where ).log( level, msg );
    }

    /**
     * Description of the Method
     *
     * @param where  PARAM
     * @param level  PARAM
     * @param msg    PARAM
     * @param th     PARAM
     */
    public static void log( String where, Level level, String msg, Throwable th ) {
        getLogger( where ).log( level, msg, th );
    }

    /**
     * Description of the Method
     *
     * @param args  PARAM
     */
    public static void main( String args[] ) {
        try {
            XRLog.cascade( "Cascade msg" );
            XRLog.cascade( Level.WARNING, "Cascade msg" );
            XRLog.exception( "Exception msg" );
            XRLog.exception( "Exception msg", new Exception() );
            XRLog.general( "General msg" );
            XRLog.general( Level.WARNING, "General msg" );
            XRLog.init( "Init msg" );
            XRLog.init( Level.WARNING, "Init msg" );
            XRLog.load( "Load msg" );
            XRLog.load( Level.WARNING, "Load msg" );
            XRLog.match( "Match msg" );
            XRLog.match( Level.WARNING, "Match msg" );
            XRLog.layout( "Layout msg" );
            XRLog.layout( Level.WARNING, "Layout msg" );
            XRLog.render( "Render msg" );
            XRLog.render( Level.WARNING, "Render msg" );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    /**
     * Same purpose as Logger.getLogger(), except that the static initialization
     * for XRLog will initialize the LogManager with logging levels and other
     * configuration. Use this instead of Logger.getLogger()
     *
     * @param log  PARAM
     * @return     The logger value
     */
    public static Logger getLogger( String log ) {
        return Logger.getLogger( log );
    }

    static {
        try {
            new LogStartupConfig();
        } catch ( Exception ex ) {
            throw new XRRuntimeException( "Could not initialize logs." );
        }
    }// end main()
}// end class

/*
 * $Id$
 *
 * $Log$
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

