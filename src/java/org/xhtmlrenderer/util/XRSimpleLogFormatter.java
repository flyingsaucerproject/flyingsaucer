/*
 * {{{ header & license
 * LogStartupConfig.java
 * Copyright (c) 2004 Patrick Wright
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
import java.text.MessageFormat;


/**
 * A java.util.logging.Formatter class that writes a bare-bones log messages,
 * with no origin class name and no date/time.
 *
 * @author   Patrick Wright
 */
public class XRSimpleLogFormatter extends Formatter {
    private final static String defaultFmt = "{1}:\n  {4}\n";
    private final static String msgFmt;
    private final MessageFormat mformat;
    
    static {
        msgFmt = Configuration.valueFor("xr.simple-log-format", defaultFmt).trim() + "\n";
    }
    
    public XRSimpleLogFormatter() {
        super();
        mformat = new MessageFormat(msgFmt);
    }
    
    /**
     * Format the given log record and return the formatted string.
     *
     * @param record  PARAM
     * @return        Returns
     */
    public String format( LogRecord record ) {
        String args[] = { 
            String.valueOf(record.getMillis()),
            record.getLoggerName(),
            record.getSourceClassName(),
            record.getSourceMethodName(),
            record.getMessage()       
        };
        String log = mformat.format(args);
        return log;
    }

    /**
     * Localize and format the message string from a log record.
     *
     * @param record  PARAM
     * @return        Returns
     */
    public String formatMessage( LogRecord record ) {
        return super.formatMessage( record );
    }

    /**
     * Return the header string for a set of formatted records.
     *
     * @param h  PARAM
     * @return   The head value
     */
    public String getHead( Handler h ) {
        return super.getHead( h );
    }

    /**
     * Return the tail string for a set of formatted records.
     *
     * @param h  PARAM
     * @return   The tail value
     */
    public String getTail( Handler h ) {
        return super.getTail( h );
    }

}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2004/10/14 11:13:22  pdoubleya
 * Added to CVS.
 *
 */

