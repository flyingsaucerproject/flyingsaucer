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
package org.xhtmlrenderer.test;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;
import org.w3c.dom.Document;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.util.x;
import java.awt.Graphics2D;
import java.awt.Dimension;

/**
 * Description of the Class
 *
 * @author   empty
 */
public class DocumentDiffTest {

    /**
     * Description of the Method
     *
     * @param dir            PARAM
     * @param width          PARAM
     * @param height         PARAM
     * @exception Exception  Throws
     */
    public void runTests( File dir, int width, int height )
        throws Exception {
        File[] files = dir.listFiles();
        for ( int i = 0; i < files.length; i++ ) {
            if ( files[i].isDirectory() ) {
                runTests( files[i], width, height );
                continue;
            }
            if ( files[i].getName().endsWith( ".xhtml" ) ) {
                String testfile = files[i].getAbsolutePath();
                String difffile = testfile.substring( 0, testfile.length() - 6 ) + ".diff";
                u.p( "test file = " + testfile );
                //u.p( "diff file = " + difffile );
                boolean is_correct = compareTestFile( testfile, difffile, 500, 500 );
                u.p( "is correct = " + is_correct );
            }
        }

    }

    /**
     * Description of the Method
     *
     * @param dir            PARAM
     * @param width          PARAM
     * @param height         PARAM
     * @exception Exception  Throws
     */
    public void generateDiffs( File dir, int width, int height )
        throws Exception {
        File[] files = dir.listFiles();
        for ( int i = 0; i < files.length; i++ ) {
            if ( files[i].isDirectory() ) {
                generateDiffs( files[i], width, height );
                continue;
            }
            if ( files[i].getName().endsWith( ".xhtml" ) ) {
                String testfile = files[i].getAbsolutePath();
                String difffile = testfile.substring( 0, testfile.length() - 6 ) + ".diff";
                //u.p("test file = " + testfile);
                generateTestFile( testfile, difffile, 500, 500 );
                u.p( "generated = " + difffile );
            }
        }

    }

    /**
     * Description of the Method
     *
     * @param test           PARAM
     * @param diff           PARAM
     * @param width          PARAM
     * @param height         PARAM
     * @exception Exception  Throws
     */
    public static void generateTestFile( String test, String diff, int width, int height )
        throws Exception {
            u.p("test = " + test);
        String out = xhtmlToDiff( test, width, height );
        //u.p("diff = \n" + out);
        u.string_to_file( out, new File( diff ) );
    }

    /**
     * Description of the Method
     *
     * @param xhtml          PARAM
     * @param width          PARAM
     * @param height         PARAM
     * @return               Returns
     * @exception Exception  Throws
     */
    public static String xhtmlToDiff( String xhtml, int width, int height )
        throws Exception {
        Document doc = x.loadDocument( xhtml );
        Graphics2DRenderer renderer = new Graphics2DRenderer();
        renderer.setDocument( doc , new File(xhtml).toURL());
        
        BufferedImage buff = new BufferedImage( width, height, BufferedImage.TYPE_4BYTE_ABGR );
        Graphics2D g = (Graphics2D)buff.getGraphics();

        //panel.setSize( width, height );

        //panel.setThreadedLayout(false);
        
        renderer.layout(g, new Dimension(width,height));
        renderer.render(g);
        
        //panel.paintComponent( g );
        
        
        StringBuffer sb = new StringBuffer();
        getDiff( sb, renderer.getRenderingContext().getRootBox(), "" );
        return sb.toString();
    }

    /**
     * Description of the Method
     *
     * @param test           PARAM
     * @param diff           PARAM
     * @param width          PARAM
     * @param height         PARAM
     * @return               Returns
     * @exception Exception  Throws
     */
    public boolean compareTestFile( String test, String diff, int width, int height )
        throws Exception {
        String tin = xhtmlToDiff( test, width, height );
        String din = null;
        try {
            din = u.file_to_string( diff );
        } catch (FileNotFoundException ex) {
            return false;
        }
        //u.p("tin = ");
        //u.p(tin);
        //u.p("din = ");
        //u.p(din);
        if ( tin.equals( din ) ) {
            return true;
        }
        u.p("warning not equals");
        File dfile = new File("correct.diff");
        File tfile = new File("test.diff");
        u.p("writing to " + dfile + " and " + tfile);
        u.string_to_file(tin,tfile);
        u.string_to_file(din,dfile);
        return false;
    }

    /**
     * Gets the diff attribute of the DocumentDiffTest object
     *
     * @param sb   PARAM
     * @param box  PARAM
     * @param tab  PARAM
     */
    public static void getDiff( StringBuffer sb, Box box, String tab ) {
        sb.append( tab + box.getTestString() + "\n" );
        for ( int i = 0; i < box.getChildCount(); i++ ) {
            getDiff( sb, (Box)box.getChild( i ), tab + " " );
        }

    }

    /**
     * The main program for the DocumentDiffTest class
     *
     * @param args           The command line arguments
     * @exception Exception  Throws
     */
    public static void main( String[] args )
        throws Exception {
        String testfile = "tests/diff/background/01.xhtml";
        String difffile = "tests/diff/background/01.diff";

        DocumentDiffTest ddt = new DocumentDiffTest();
        ddt.runTests( new File( "tests/diff" ), 500, 500 );
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.7  2004/11/12 02:23:59  joshy
 * added new APIs for rendering context, xhtmlpanel, and graphics2drenderer.
 * initial support for font mapping additions
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/07 23:24:19  joshy
 * added menu item to generate diffs
 * added diffs for multi-colored borders and inline borders
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/04 15:35:46  joshy
 * initial float support
 * includes right and left float
 * cannot have more than one float per line per side
 * floats do not extend beyond enclosing block
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/11/01 14:24:20  joshy
 * added a boolean for turning off threading
 * fixed the diff tests
 * removed some dead code
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 14:01:42  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

