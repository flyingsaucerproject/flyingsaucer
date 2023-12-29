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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;

/**
 * Create a ZIP-format file from the contents of some directory. All files
 * in the directory are included. To use, instantiate with a reference to
 * the directory to ZIP, and to the output file to create, then call
 * {@link #zipDirectory()} to create the output file.
 * <p>
 * Note that this is ZIP-compatible, not GZIP-compatible (ZIP is both an archive format
 * and a compression format, GZIP is just a compression format).
 */
public class Zipper {
    private final File sourceDir;
    private final File outputFile;

    public Zipper(File sourceDir, File outputFile) {
        this.sourceDir = sourceDir;
        this.outputFile = outputFile;
        if (!this.outputFile.delete()) {
            throw new IllegalArgumentException("Can't delete outputfile " + outputFile.getAbsolutePath());
        }
    }

    public static void main(String[] args) throws IOException {
        File sourceDir = getSourceDir(args);
        File outputFile = new File(System.getProperty("user.home") + File.separator + sourceDir.getName() + ".zip");
        new Zipper(sourceDir, outputFile).zipDirectory();
        System.out.println("Created zip file " + outputFile.getPath());
    }

    public File zipDirectory() throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(newOutputStream(outputFile.toPath()))) {
            recurseAndZip(sourceDir, zos);
            return outputFile;
        }
    }

    private static void recurseAndZip(File file, ZipOutputStream zos) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    recurseAndZip(f, zos);
                }
            }
        } else {
            byte[] buf = new byte[1024];
            int len;
            ZipEntry entry = new ZipEntry(file.getName());
            try (BufferedInputStream bis = new BufferedInputStream(newInputStream(file.toPath()))) {
                zos.putNextEntry(entry);
                while ((len = bis.read(buf)) >= 0) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
            }
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
