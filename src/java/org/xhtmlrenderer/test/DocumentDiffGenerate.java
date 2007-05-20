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
package org.xhtmlrenderer.test;

import java.io.File;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class DocumentDiffGenerate {
    /**
     * The main program for the DocumentDiffGenerate class
     *
     * @param args           The command line arguments
     * @exception Exception  Throws
     */
    public static void main( String[] args )
        throws Exception {
        DocumentDiffTest ddt = new DocumentDiffTest();
		if(args.length == 2) {
			DocumentDiffTest.generateTestFile(args[0],args[1],500,500);
		} else {
			ddt.generateDiffs( new File( "tests/diff" ), 500, 500 );
		}
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.6  2007/05/20 23:25:32  peterbrant
 * Various code cleanups (e.g. remove unused imports)
 *
 * Patch from Sean Bright
 *
 * Revision 1.5  2005/04/22 17:09:47  joshy
 * minor changes to the document diff.
 * removed system.exit
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2005/01/29 20:22:18  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.3  2004/10/23 14:01:42  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

