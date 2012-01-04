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

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Description of the Class
 *
 * @author empty
 */
public class Uu extends Util {
    /**
     * Description of the Field
     */
    private static Util util;
    /**
     * Description of the Field
     */
    private static Util utilAsString;

    /**
     * Constructor for the Uu object
     */
    private Uu() {
        super(System.out);
    }

    /**
     * Description of the Method
     */
    public static void on() {
        init();
        util.setOn(true);
    }

    /**
     * Description of the Method
     */
    public static void off() {
        init();
        util.setOn(false);
    }

    /**
     * Description of the Method
     *
     * @param object PARAM
     */
    public static void p(Object object) {
        init();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        utilAsString.setPrintWriter(pw);
        utilAsString.print(object);// our log adds a newline
        pw.flush();
        if (XRLog.isLoggingEnabled()) {
            XRLog.general(sw.getBuffer().toString());
        }
    }

    /**
     * Description of the Method
     *
     * @param object PARAM
     */
    public static void pr(Object object) {
        init();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        utilAsString.setPrintWriter(pw);
        utilAsString.print(object);// our log adds a newline
        pw.flush();
        if (XRLog.isLoggingEnabled()) {
            XRLog.general(sw.getBuffer().toString());
        }
        //util.print( object );
    }

    /**
     * Description of the Method
     *
     * @param msec PARAM
     */
    public static void sleep(int msec) throws InterruptedException {
        Thread.sleep(msec);
    }

    /**
     * Description of the Method
     */
    public static void dump_stack() {
        p(stack_to_string(new Exception()));
    }

    /**
     * Description of the Method
     *
     * @param args PARAM
     */
    public static void main(String args[]) {
        try {
            Uu.p(new Object());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     */
    private static void init() {
        if (util == null) {
            util = new Util(System.out);
        }
        if (utilAsString == null) {
            utilAsString = new Util(System.out);
        }
    }// end main()
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2009/04/25 11:57:05  pdoubleya
 * Small opt, avoid log calls where logging is disabled, patch from Peter Fassev issue #263
 *
 * Revision 1.3  2005/09/29 06:15:07  tobega
 * Patch from Peter Brant:
 * List of changes:
 *  - Fix extents height calculation
 *  - Small refactoring to Boxing to combine a method
 *  - Make render and layout threads interruptible and add
 * RootPanel.shutdown() method to shut them down in an orderly manner
 *  - Fix NPE in Graphics2DRenderer.  It looks like
 * BasicPanel.intrinsic_size will always be null anyway?
 *  - Fix NPE in RootPanel when enclosingScrollPane is null.
 *  - Both RenderLoop.collapseRepaintEvents and
 * LayoutLoop.collapseLayoutEvents will go into an infinite loop if the
 * next event isn't collapsible.  I added a common implementation to
 * RenderQueue which doesn't have this problem.
 *
 * Revision 1.2  2005/01/29 20:18:38  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.1  2004/12/12 03:33:05  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.4  2004/11/22 21:34:05  joshy
 * created new whitespace handler.
 * new whitespace routines only work if you set a special property. it's
 * off by default.
 *
 * turned off fractional font metrics
 *
 * fixed some bugs in Uu and Xx
 *
 * - j
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 14:06:57  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

