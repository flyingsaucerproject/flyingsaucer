package com.pdoubleya.xhtmlrenderer.util;

import java.util.logging.*;


/**
 * Utility class for working with java.logging Logger classes
 *
 * @author    Patrick Wright
 *
 */
public class LoggerUtil {
    /**
     * Instantiate a Logger for debug messages for a given class.
     *
     * @param cls  PARAM
     * @return     The debugLogger value
     */
    public static Logger getDebugLogger( Class cls ) {
        Logger l = Logger.getLogger( cls.getName() );
        l.setLevel( Level.ALL );
        return l;
    }

}

