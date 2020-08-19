/*
 * {{{ header & license
 * XRSimpleLogFormatter.java
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


/**
 * A java.util.logging.Formatter class that writes a bare-bones log messages,
 * with no origin class name and no date/time.
 *
 * @author Patrick Wright
 */
public class XRSimpleLogFormatter extends Formatter {

    private static final String MSG_FMT;

    private static final String EXMSG_FMT;

    private final MessageFormat mformat;

    private final MessageFormat exmformat;

    static {
        MSG_FMT = Configuration.valueFor("xr.simple-log-format", "{1}:\n  {5}\n").trim() + "\n";
        EXMSG_FMT = Configuration.valueFor("xr.simple-log-format-throwable", "{1}:\n  {5}\n{8}").trim() + "\n";
    }

    /**
     * Constructor for the XRSimpleLogFormatter object
     */
    public XRSimpleLogFormatter() {
        super();
        mformat = new MessageFormat(MSG_FMT);
        exmformat = new MessageFormat(EXMSG_FMT);
    }

    /**
     * Format the given log record and return the formatted string.
     *
     * @param record PARAM
     * @return Returns
     */
    public String format(LogRecord record) {
        Throwable th = record.getThrown();
        String thName = "";
        String thMessage = "";
        String trace = null;

        if (th != null) {
            StringWriter sw = new StringWriter();
            th.printStackTrace(new PrintWriter(sw));
            trace = sw.toString();

            thName = th.getClass().getName();
            thMessage = th.getMessage();
        }

        String[] args = {
                String.valueOf(record.getMillis()),
                record.getLoggerName(),
                record.getLevel().toString(),
                record.getSourceClassName(),
                record.getSourceMethodName(),
                record.getMessage(),
                thName,
                thMessage,
                trace
        };

        return th == null ? mformat.format(args) : exmformat.format(args);
    }

    /**
     * Localize and format the message string from a log record.
     *
     * @param record PARAM
     * @return Returns
     */
    @Override
    public String formatMessage(LogRecord record) {
        return super.formatMessage(record);
    }

    /**
     * Return the header string for a set of formatted records.
     *
     * @param h PARAM
     * @return The head value
     */
    @Override
    public String getHead(Handler h) {
        return super.getHead(h);
    }

    /**
     * Return the tail string for a set of formatted records.
     *
     * @param h PARAM
     * @return The tail value
     */
    @Override
    public String getTail(Handler h) {
        return super.getTail(h);
    }

}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.6  2005/04/07 16:15:47  pdoubleya
 * Typo.
 *
 * Revision 1.5  2005/01/29 20:18:37  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.4  2004/10/23 14:06:57  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 * Revision 1.3  2004/10/18 12:08:37  pdoubleya
 * Incorrect Configuration key fixed.
 *
 * Revision 1.2  2004/10/14 12:53:26  pdoubleya
 * Added handling for exception messages with stack trace and separate message format.
 *
 * Revision 1.1  2004/10/14 11:13:22  pdoubleya
 * Added to CVS.
 *
 */

