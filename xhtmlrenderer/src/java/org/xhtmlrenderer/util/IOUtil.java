package org.xhtmlrenderer.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

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

    /**
     * Attempts to open a connection, and a stream, to the URI provided. timeouts will be set for opening the connection
     * and reading from it. will return the stream, or null if unable to open or read or a timeout occurred. Does not
     * buffer the stream.
     */
    public static InputStream openStreamAtUrl(String uri) {
        InputStream is = null;
        try {
            final URLConnection uc = new URL(uri).openConnection();

            // If using Java 5+ you can set timeouts for the URL connection--useful if the remote
            // server is down etc.; the default timeout is pretty long
            //
            //uc.setConnectTimeout(10 * 1000);
            //uc.setReadTimeout(30 * 1000);
            //
            // TODO:CLEAN-JDK1.4
            // Since we target 1.4, we use a couple of system properties--note these are only supported
            // in the Sun JDK implementation--see the Net properties guide in the JDK
            // e.g. file:///usr/java/j2sdk1.4.2_17/docs/guide/net/properties.html
            System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(10 * 1000));
            System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(30 * 1000));

            uc.connect();

            is = uc.getInputStream();
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (FileNotFoundException e) {
            XRLog.exception("item at URI " + uri + " not found");
        } catch (IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }

        return is;

    }
}
