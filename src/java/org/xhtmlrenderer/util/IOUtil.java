package org.xhtmlrenderer.util;

import java.io.*;

/**
 * @author patrick
 */
public class IOUtil {
    public static void copyFile(File page, File outputDir) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(page));
        File outputFile = new File(outputDir, page.getName());
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.flush();
        out.close();
    }

    public static void deleteAllFiles(final File dir) throws IOException {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (!file.delete()) {
                throw new IOException("Cleanup directory " + dir + ", can't delete file " + file);
            }
        }
    }
}
