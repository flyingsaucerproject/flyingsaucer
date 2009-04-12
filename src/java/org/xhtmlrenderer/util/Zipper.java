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
package org.xhtmlrenderer.util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Create a ZIP-format file from the contents of some directory. All files
 * in the directory are included. To use, instantiate with a reference to
 * the directory to ZIP, and to the output file to create, then call
 * {@link #zipDirectory()} to create the output file.
 * <p/>
 * Note that this is ZIP-compatible, not GZIP-compatible (ZIP is both an archive format
 * and a compression format, GZIP is just a compression format).
 */
public class Zipper {
    private final File sourceDir;
    private final File outputFile;

    public Zipper(File sourceDir, File outputFile) {
        this.sourceDir = sourceDir;
        this.outputFile = outputFile;
        this.outputFile.delete();
    }

    public static void main(String[] args) {
        File sourceDir = getSourceDir(args);
        File outputFile = new File(System.getProperty("user.home") + File.separator + sourceDir.getName() + ".zip");
        try {
            new Zipper(sourceDir, outputFile).zipDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Created zip file " + outputFile.getPath());
    }

    public File zipDirectory() throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile));
        recurseAndZip(sourceDir, zos);
        zos.close();
        return outputFile;
    }

    private static void recurseAndZip(File file, ZipOutputStream zos) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file1 = files[i];
                    recurseAndZip(file1, zos);
                }
            }
        } else {
            byte[] buf = new byte[1024];
            int len;
            ZipEntry entry = new ZipEntry(file.getName());
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            zos.putNextEntry(entry);
            while ((len = bis.read(buf)) >= 0) {
                zos.write(buf, 0, len);
            }
            bis.close();
            zos.closeEntry();
        }
    }

    private static File getSourceDir(String[] args) {
        if (args.length != 1) {
            usageAndExit("Need directory name containing input files to render.");
        }
        String sourceDirPath = args[0];
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists()) {
            usageAndExit(sourceDirPath);
        }
        return sourceDir;
    }

    private static void usageAndExit(String msg) {
        System.err.println("Source directory not found: " + msg);
        System.exit(-1);
    }
}
