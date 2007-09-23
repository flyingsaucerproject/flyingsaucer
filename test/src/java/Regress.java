/*
 * {{{ header & license
 * Copyright (c) 2006 Patrick Wright
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

import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.swing.BoxRenderer;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.FSImageWriter;
import org.xhtmlrenderer.util.Zipper;
import org.xhtmlrenderer.util.IOUtil;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * Creates a directory containing output from the renderer as text, given a set of input files (XHTML, XML).
 * These files serve as the reference image for a comparison with later runs of the renderer. For any input
 * file _input_, the zip will contain _input_, _input.layout.txt_, _input.rendered.txt_ and _input.png_. To use,
 * create an instance of Regress with input directory, output directory and width, then call {@link #snapshot()}.
 * <pre>
 * Regress regress = new Regress(sourceDir, outputDir, width);
 * regress.snapshot();
 * </pre>
 * One the regress is done, you can use Zipper to pack it up:
 * <pre>
 *  new Zipper(outputDir, outputZip).zipDirectory();
 * </pre>
 * Regress will try to render all files in the source directory; a failure to render one file (e.g. if an exception
 * is thrown) will not stop the rendering process.
 *
 * Files in the source directory with the following extensions are included: htm, html, xht, xhtml, and xml. * 
 *
 * You can also run this from the command line, passing in the source directory as argument 1, and the output file
 * name as argument 2. The output is created in the standard temp directory in a subdirectory called "reference". The
 * path to the output directory will be printed to the console when complete.
 */
public class Regress {
    public static final List EXTENSIONS = Arrays.asList(new String[]{"htm","html","xht","xhtml","xml",});
    private final File sourceDir;
    private final File outputDir;
    private final int width;
    private int fileCount;
    private int failedCount;
    public static final String RENDER_SFX = ".render.txt";
    public static final String LAYOUT_SFX = ".layout.txt";
    public static final String PNG_SFX = ".png";
    private static final String LINE_SEPARATOR = "\n"; // use one single sep to avoid OS problems when moving ref files

    public Regress(File sourceDir, File outputDir, int width) {
        this.sourceDir = sourceDir;
        this.outputDir = outputDir;
        this.width = width;
    }

    public static void main(String[] args) throws Exception {
        final File sourceDir = getSourceDir(args);
        final File outputZip = getOutputZipFile(args);
        final File outputDir = createOutputDir();
        final int width = 1024;

        System.out.println("Running regression against files in " + sourceDir + " to output directory " + outputDir);
        Regress regress = new Regress(sourceDir, outputDir, width);
        regress.snapshot();
        System.out.println("Ran regressions against " + regress.getFileCount() + " files in source directory; " + regress.getFailedCount() + " failed to generate");
        new Zipper(outputDir, outputZip).zipDirectory();
        System.out.println("Built regressions ZIP file: " + outputZip.getPath());
    }

    private int getFailedCount() {
        return failedCount;
    }

    private int getFileCount() {
        return fileCount;
    }

    private static File getOutputZipFile(String[] args) throws IOException {
        if (args.length < 2) {
            usageAndExit("Need directory name containing input files to render.");
        }
        String path = args[1];
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            usageAndExit("Directory not found: " + file.getParentFile().getPath());
        }
        file.delete();
        file.createNewFile();
        return file;

    }

    public void snapshot() throws IOException {
        IOUtil.deleteAllFiles(outputDir);
        final boolean wasLogging = enableLogging(false);
        try {
            Iterator iter = listInputFiles(sourceDir);
            while (iter.hasNext()) {
                File file = (File) iter.next();
                saveBoxModel(file, outputDir, width);
                saveImage(file, outputDir, width);
            }
        } finally {
            enableLogging(wasLogging);
        }
    }

    private static File getSourceDir(String[] args) {
        if (args.length < 1) {
            usageAndExit("Need directory name containing input files to render.");
        }
        String sourceDirPath = args[0];
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists()) {
            usageAndExit("Source directory not found: " + sourceDirPath);
        }
        return sourceDir;
    }

    private static void usageAndExit(String msg) {
        System.err.println(msg);
        System.exit(-1);
    }

    private static File createOutputDir() {
        File parent = new File(System.getProperty("java.io.tmpdir"));
        File reference = new File(parent, "reference");
        reference.mkdirs();
        return reference;
    }

    private boolean enableLogging(final boolean isEnabled) {
        final String prop = "xr.util-logging.loggingEnabled";
        final boolean orgVal = Boolean.valueOf(System.getProperty(prop)).booleanValue();
        System.setProperty(prop, Boolean.valueOf(isEnabled).toString());
        return orgVal;
    }

    private Iterator listInputFiles(final File sourceDir) {
        File[] f = sourceDir.listFiles(new FilenameFilter() {
            public boolean accept(File file, String s) {
                String ext = s.substring(s.lastIndexOf(".") + 1);
                return EXTENSIONS.contains(ext);
            }
        });
        return Arrays.asList(f).iterator();
    }

    private void saveBoxModel(File page, File outputDir, int width) throws IOException {
        BoxRenderer renderer = new BoxRenderer(page, width);
        Box box;
        try {
            box = renderer.render();
        } catch (Exception e) {
            System.err.println("Could not render input file, skipping: " + page + " err: " + e.getMessage());
            failedCount++;
            return;
        }
        LayoutContext layoutContext = renderer.getLayoutContext();
        String inputFileName = page.getName();
        IOUtil.copyFile(page, outputDir);
        writeToFile(outputDir, inputFileName + RENDER_SFX, box.dump(layoutContext, "", Box.DUMP_RENDER));
        writeToFile(outputDir, inputFileName + LAYOUT_SFX, box.dump(layoutContext, "", Box.DUMP_LAYOUT));
        fileCount++;
    }

    private void saveImage(File page, File reference, int width) throws IOException {
        try {
            Java2DRenderer j2d = new Java2DRenderer(page, width);

            // this renders and returns the image, which is stored in the J2R; will not
            // be re-rendered, calls to getImage() return the same instance
            BufferedImage img = j2d.getImage();

            // write it out, full size, PNG
            // FSImageWriter instance can be reused for different ../images,
            // defaults to PNG
            FSImageWriter imageWriter = new FSImageWriter();
            final File outputFile = new File(reference, page.getName() + ".png");
            outputFile.delete();
            String fileName = outputFile.getPath();
            imageWriter.write(img, fileName);
        } catch (Exception e) {
            System.err.println("Could not render input file to image, skipping: " + page + " err: " + e.getMessage());
            return;
        }
    }

    private void writeToFile(File outputDir, String fileName, String output) throws IOException {
        final File outputFile = new File(outputDir, fileName);
        outputFile.delete();
        OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
        PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
        pw.print(output);
        pw.print(LINE_SEPARATOR);
        pw.flush();
        fw.close();
    }
}
