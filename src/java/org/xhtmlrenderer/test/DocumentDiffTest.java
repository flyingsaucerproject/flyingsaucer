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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XMLUtil;
import org.xhtmlrenderer.util.XRLog;

/**
 * Description of the Class
 *
 * @author empty
 */
public class DocumentDiffTest {
    public static final int width = 500;
    public static final int height = 500;

    /**
     * Description of the Method
     *
     * @param dir    PARAM
     * @param width  PARAM
     * @param height PARAM
     * @throws Exception Throws
     */
    public void runTests(File dir, int width, int height)
            throws Exception {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                runTests(files[i], width, height);
                continue;
            }
            if (files[i].getName().endsWith(".xhtml")) {
                String testfile = files[i].getAbsolutePath();
                String difffile = testfile.substring(0, testfile.length() - 6) + ".diff";
                XRLog.log("unittests", Level.WARNING, "test file = " + testfile);
                //Uu.p( "diff file = " + difffile );
                try {
                    boolean is_correct = compareTestFile(testfile, difffile, width, height);
                    XRLog.log("unittests", Level.WARNING, "is correct = " + is_correct);
                } catch (Throwable thr) {
                    XRLog.log("unittests", Level.WARNING, thr.toString());
                    thr.printStackTrace();
                }
            }
        }

    }

    /**
     * Description of the Method
     *
     * @param dir    PARAM
     * @param width  PARAM
     * @param height PARAM
     * @throws Exception Throws
     */
    public void generateDiffs(File dir, int width, int height)
            throws Exception {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                generateDiffs(files[i], width, height);
                continue;
            }
            if (files[i].getName().endsWith(".xhtml")) {
                String testfile = files[i].getAbsolutePath();
                String difffile = testfile.substring(0, testfile.length() - 6) + ".diff";
                //Uu.p("test file = " + testfile);
                generateTestFile(testfile, difffile, width, height);
                Uu.p("generated = " + difffile);
            }
        }

    }

    /**
     * Description of the Method
     *
     * @param test   PARAM
     * @param diff   PARAM
     * @param width  PARAM
     * @param height PARAM
     * @throws Exception Throws
     */
    public static void generateTestFile(String test, String diff, int width, int height)
            throws Exception {
        Uu.p("test = " + test);
        String out = xhtmlToDiff(test, width, height);
        //Uu.p("diff = \n" + out);
        Uu.string_to_file(out, new File(diff));
    }

    /**
     * Description of the Method
     *
     * @param xhtml  PARAM
     * @param width  PARAM
     * @param height PARAM
     * @return Returns
     * @throws Exception Throws
     */
    public static String xhtmlToDiff(String xhtml, int width, int height)
            throws Exception {
        Document doc = XMLUtil.documentFromFile(xhtml);
        Graphics2DRenderer renderer = new Graphics2DRenderer();
        renderer.setDocument(doc, new File(xhtml).toURL().toString());

        BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D) buff.getGraphics();

        Dimension dim = new Dimension(width, height);
        renderer.layout(g, dim);
        renderer.render(g);

        StringBuffer sb = new StringBuffer();
        getDiff(sb, renderer.getPanel().getRootBox(), "");
        return sb.toString();
    }

    /**
     * Description of the Method
     *
     * @param test   PARAM
     * @param diff   PARAM
     * @param width  PARAM
     * @param height PARAM
     * @return Returns
     * @throws Exception Throws
     */
    public boolean compareTestFile(String test, String diff, int width, int height)
            throws Exception {
        String tin = xhtmlToDiff(test, width, height);
        String din = null;
        try {
            din = Uu.file_to_string(diff);
        } catch (FileNotFoundException ex) {
            XRLog.log("unittests", Level.WARNING, "diff file missing");
            return false;
        }
        //XRLog.log("unittests",Level.WARNING,"tin = " + tin);
        //XRLog.log("unittests",Level.WARNING,"din = " + din);
        if (tin.equals(din)) {
            return true;
        }
        XRLog.log("unittests", Level.WARNING, "warning not equals");
        File dfile = new File("correct.diff");
        File tfile = new File("test.diff");
        XRLog.log("unittests", Level.WARNING, "writing to " + dfile + " and " + tfile);
        Uu.string_to_file(tin, tfile);
        Uu.string_to_file(din, dfile);
        //System.exit(-1);
        return false;
    }

    /**
     * Gets the diff attribute of the DocumentDiffTest object
     *
     * @param sb  PARAM
     * @param box PARAM
     * @param tab PARAM
     */
    public static void getDiff(StringBuffer sb, Box box, String tab) {
        /* sb.append(tab + box.getTestString() + "\n"); */
        for (int i = 0; i < box.getChildCount(); i++) {
            getDiff(sb, (Box) box.getChild(i), tab + " ");
        }

    }

    /**
     * The main program for the DocumentDiffTest class
     *
     * @param args The command line arguments
     * @throws Exception Throws
     */
    public static void main(String[] args)
            throws Exception {

        XRLog.setLevel("plumbing.general", Level.OFF);
        //String testfile = "tests/diff/background/01.xhtml";
        //String difffile = "tests/diff/background/01.diff";
        String file = null;
        if (args.length == 0) {
            file = "tests/diff";
        } else {
            file = args[0];
        }
        DocumentDiffTest ddt = new DocumentDiffTest();
        if (new File(file).isDirectory()) {
            ddt.runTests(new File(file), width, height);
        } else {
            System.out.println(xhtmlToDiff(file, 1280, 768));
        }
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.19  2007/05/23 00:12:18  peterbrant
 * Code cleanup (patch from Sean Bright)
 *
 * Revision 1.18  2007/05/20 23:25:32  peterbrant
 * Various code cleanups (e.g. remove unused imports)
 *
 * Patch from Sean Bright
 *
 * Revision 1.17  2007/02/07 16:33:40  peterbrant
 * Initial commit of rewritten table support and associated refactorings
 *
 * Revision 1.16  2005/10/22 23:00:30  peterbrant
 * Fix memory leak (all box trees ever built remained in memory)
 *
 * Revision 1.15  2005/06/01 21:36:45  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.14  2005/04/22 17:09:47  joshy
 * minor changes to the document diff.
 * removed system.exit
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2005/04/07 16:34:52  pdoubleya
 * Silly cleanups
 *
 * Revision 1.12  2005/01/29 20:22:18  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.11  2004/12/12 03:33:04  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.10  2004/12/01 01:57:02  joshy
 * more updates for float support.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/30 21:23:18  joshy
 * updated the unit tests
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/30 20:28:28  joshy
 * support for multiple floats on a single line.
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
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

