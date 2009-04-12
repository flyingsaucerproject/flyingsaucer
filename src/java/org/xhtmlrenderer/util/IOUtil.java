package org.xhtmlrenderer.util;

import java.io.*;

/**
 * @author patrick
 */
public class IOUtil {
    public static File copyFile(File page, File outputDir) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(new FileInputStream(page));
            File outputFile;
            outputFile = new File(outputDir, page.getName());
            out = new BufferedOutputStream(new FileOutputStream(outputFile));

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.flush();
            out.close();
            return outputFile;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // swallow
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // swallow
                }
            }
        }
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
