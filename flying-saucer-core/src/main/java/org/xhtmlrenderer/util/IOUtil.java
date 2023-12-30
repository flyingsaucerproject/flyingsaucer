package org.xhtmlrenderer.util;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;

/**
 * @author patrick
 */
@ParametersAreNonnullByDefault
public class IOUtil {
    public static void copyFile(File page, File outputDir) throws IOException {
        File outputFile = new File(outputDir, page.getName());

        try (OutputStream out = new BufferedOutputStream(newOutputStream(outputFile.toPath()))) {
            try (InputStream in = new BufferedInputStream(newInputStream(page.toPath()))) {
                copyBytes(in, out);
            }
            out.flush();
        }
    }

    private static void copyBytes(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }
    }

    public static void deleteAllFiles(final File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    throw new IOException("Cleanup directory " + dir + ", can't delete file " + file);
                }
            }
        }
    }

    /**
     * Attempts to open a connection, and a stream, to the URI provided. timeouts will be set for opening the connection
     * and reading from it. will return the stream, or null if unable to open or read or a timeout occurred. Does not
     * buffer the stream.
     */
    public static InputStream openStreamAtUrl(String uri) {
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

            uc.setRequestProperty("Accept", "*/*");

            uc.connect();

            return uc.getInputStream();
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (FileNotFoundException e) {
            XRLog.exception("item at URI " + uri + " not found");
        } catch (IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }

        return null;
    }

    /**
     * Gets a Reader for the resource identified
     *
     * @return The stylesheet value
     */
    @Nullable
    @CheckReturnValue
    public static InputStream getInputStream(String uri) {
        try {
            return new URL(uri).openStream();
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (java.io.FileNotFoundException e) {
            XRLog.exception("item at URI " + uri + " not found");
        } catch (java.io.IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static byte[] readBytes(String uri) {
        try (InputStream is = getInputStream(uri)) {
            if (is == null) return null;
            return readBytes(is);
        } catch (IOException e) {
            XRLog.load(Level.WARNING, "Unable to read " + uri, e);
            return null;
        }
    }

    @Nonnull
    @CheckReturnValue
    public static byte[] readBytes(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream(is.available());
        copyBytes(is, result);
        return result.toByteArray();
    }

    public static void close(@Nullable Closeable in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }
}
