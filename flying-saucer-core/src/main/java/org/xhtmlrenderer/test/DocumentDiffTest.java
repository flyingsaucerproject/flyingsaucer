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

import org.w3c.dom.Document;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XMLUtil;
import org.xhtmlrenderer.util.XRLog;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;

import static org.xhtmlrenderer.util.ImageUtil.withGraphics;

public class DocumentDiffTest {
    private static final int width = 500;
    private static final int height = 500;

    private void runTests(File dir, int width, int height) throws Exception {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                runTests(file, width, height);
                continue;
            }
            if (file.getName().endsWith(".xhtml")) {
                String testfile = file.getAbsolutePath();
                String difffile = testfile.substring(0, testfile.length() - 6) + ".diff";
                XRLog.log("unittests", Level.WARNING, "test file = " + testfile);
                boolean is_correct = compareTestFile(testfile, difffile, width, height);
                XRLog.log("unittests", Level.WARNING, "is correct = " + is_correct);
            }
        }

    }

    public void generateDiffs(File dir, int width, int height) throws Exception {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                generateDiffs(file, width, height);
                continue;
            }
            if (file.getName().endsWith(".xhtml")) {
                String testfile = file.getAbsolutePath();
                String difffile = testfile.substring(0, testfile.length() - 6) + ".diff";
                generateTestFile(testfile, difffile, width, height);
                Uu.p("generated = " + difffile);
            }
        }

    }

    public static void generateTestFile(String test, String diff, int width, int height) throws Exception {
        Uu.p("test = " + test);
        String out = xhtmlToDiff(test, width, height);
        string_to_file(out, new File(diff));
    }

    public static String xhtmlToDiff(String xhtml, int width, int height) throws Exception {
        Document doc = XMLUtil.documentFromFile(xhtml);
        Graphics2DRenderer renderer = new Graphics2DRenderer();
        renderer.setDocument(doc, new File(xhtml).toURI().toURL().toString());

        BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        withGraphics(buff, g -> {
            Dimension dim = new Dimension(width, height);
            renderer.layout(g, dim);
            renderer.render(g);
        });

        getDiff(renderer.getPanel().getRootBox(), "");
        return "";
    }

    public boolean compareTestFile(String test, String diff, int width, int height) throws Exception {
        String tin = xhtmlToDiff(test, width, height);
        String din;
        try {
            din = file_to_string(diff);
        } catch (FileNotFoundException ex) {
            XRLog.log("unittests", Level.WARNING, "diff file missing");
            return false;
        }
        if (tin.equals(din)) {
            return true;
        }
        XRLog.log("unittests", Level.WARNING, "warning not equals");
        File dfile = new File("correct.diff");
        File tfile = new File("test.diff");
        XRLog.log("unittests", Level.WARNING, "writing to " + dfile + " and " + tfile);
        string_to_file(tin, tfile);
        string_to_file(din, dfile);
        return false;
    }

    /**
     * Gets the diff attribute of the DocumentDiffTest object
     */
    private static void getDiff(Box box, String tab) {
        for (int i = 0; i < box.getChildCount(); i++) {
            getDiff(box.getChild(i), tab + " ");
        }
    }

    /**
     * The main program for the DocumentDiffTest class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) throws Exception {

        XRLog.setLevel("plumbing.general", Level.OFF);
        //String testfile = "tests/diff/background/01.xhtml";
        //String difffile = "tests/diff/background/01.diff";
        String file;
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

    private static String file_to_string(String filename) throws IOException {
        File file = new File(filename);
        return file_to_string(file);
    }

    private static String file_to_string(File file) throws IOException {
        FileReader reader = null;
        StringWriter writer = null;
        String str;
        try {
            reader = new FileReader(file);
            writer = new StringWriter();
            char[] buf = new char[1000];
            while (true) {
                int n = reader.read(buf, 0, 1000);
                if (n == -1) {
                    break;
                }
                writer.write(buf, 0, n);
            }
            str = writer.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
        return str;
    }

    public static void string_to_file(String text, File file)
            throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            StringReader reader = new StringReader(text);
            char[] buf = new char[1000];
            while (true) {
                int n = reader.read(buf, 0, 1000);
                if (n == -1) {
                    break;
                }
                writer.write(buf, 0, n);
            }
            writer.flush();
        }
    }
}
