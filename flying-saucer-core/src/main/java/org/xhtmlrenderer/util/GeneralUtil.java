/*
 * {{{ header & license
 * GeneralUtil.java
 * Copyright (c) 2004, 2005 Patrick Wright
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Patrick Wright
 */
public class GeneralUtil {

    /**
     * Used to format an Object's hashcode into a 0-padded 10 char String, e.g.
     * for 24993066 returns "0024993066"
     */
    public static final DecimalFormat PADDED_HASH_FORMAT = new DecimalFormat("0000000000");

    public static InputStream openStreamFromClasspath(Object obj, String resource) {
        InputStream readStream = null;
        try {
            ClassLoader loader = obj.getClass().getClassLoader();
            if (loader == null) {
                readStream = ClassLoader.getSystemResourceAsStream(resource);
            } else {
                readStream = loader.getResourceAsStream(resource);
            }
            if (readStream == null) {
                URL stream = resource.getClass().getResource(resource);
                if (stream != null) readStream = stream.openStream();
            }
        } catch (Exception ex) {
            XRLog.exception("Could not open stream from CLASSPATH: " + resource, ex);
        }
        return readStream;
    }

    public static URL getURLFromClasspath(Object obj, String resource) {
        URL url = null;
        try {
            ClassLoader loader = obj.getClass().getClassLoader();
            if (loader == null) {
                url = ClassLoader.getSystemResource(resource);
            } else {
                url = loader.getResource(resource);
            }
            if (url == null) {
                url = resource.getClass().getResource(resource);
            }
        } catch (Exception ex) {
            XRLog.exception("Could not get URL from CLASSPATH: " + resource, ex);
        }
        return url;
    }

    /**
     * Dumps an exception to the console, only the last 5 lines of the stack
     * trace.
     */
    public static void dumpShortException(Exception ex) {
        String s = ex.getMessage();
        if (s == null || s.trim().equals("null")) {
            s = "{no ex. message}";
        }
        System.out.println(s + ", " + ex.getClass());
        StackTraceElement[] stackTrace = ex.getStackTrace();
        for (int i = 0; i < stackTrace.length && i < 5; i++) {
            StackTraceElement ste = stackTrace[i];
            System.out.println("  " + ste.getClassName() + "." + ste.getMethodName() + "(ln " + ste.getLineNumber() + ")");
        }
    }

    /**
     * Returns a String tracking the last n method calls, from oldest to most
     * recent. You can use this as a simple tracing mechanism to find out the
     * calls that got to where you execute the {@code trackBack()} call
     * from. Example:</p>
     * <pre>
     * // called from Box.calcBorders(), line 639
     * String tback = GeneralUtil.trackBack(6);
     * System.out.println(tback);
     * </pre> produces
     * <pre>
     * Boxing.layoutChildren(ln 204)
     * BlockBoxing.layoutContent(ln 81)
     * Boxing.layout(ln 72)
     * Boxing.layout(ln 133)
     * Box.totalLeftPadding(ln 306)
     * Box.calcBorders(ln 639)
     * </pre>
     * The {@code trackBack()} method itself is always excluded from the dump.
     * Note the output may not be useful if HotSpot has been optimizing the
     * code.
     *
     * @param cnt How far back in the call tree to go; if call tree is smaller, will
     *            be limited to call tree.
     */
    public static String trackBack(int cnt) {
        Exception ex = new Exception("Getting stack trace...");
        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>(cnt);
        StackTraceElement[] stackTrace = ex.getStackTrace();
        if (cnt >= stackTrace.length) {
            cnt = stackTrace.length - 1;
        }

        // >= 1 to not include this method
        for (int i = cnt; i >= 1; i--) {
            StackTraceElement ste = stackTrace[i];
            sb.append(classNameOnly(ste.getClassName()));
            sb.append(".");
            sb.append(ste.getMethodName());
            sb.append("(ln ").append(ste.getLineNumber()).append(")");
            list.add(sb.toString());
            sb = new StringBuilder();
        }

        StringBuilder padding = new StringBuilder();
        StringBuilder trackback = new StringBuilder();
        for (String s : list) {
            trackback.append(padding).append(s).append("\n");
            padding.append("   ");
        }
        return trackback.toString();
    }


    /**
     * Given an Object instance, returns just the classname with no package
     */
    public static String classNameOnly(Object o) {
        String s = "[null object ref]";
        if (o != null) {
            s = classNameOnly(o.getClass().getName());
        }
        return s;
    }

    /**
     * Given a String classname, returns just the classname with no package
     */
    public static String classNameOnly(String cname) {
        String s = "[null object ref]";
        if (cname != null) {
            s = cname.substring(cname.lastIndexOf('.') + 1);
        }
        return s;
    }

    public static String paddedHashCode(Object o) {
        String s = "0000000000";
        if (o != null) {
            s = PADDED_HASH_FORMAT.format(o.hashCode());
        }
        return s;
    }

    public static boolean isMacOSX() {
        try {
            if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
                return true;
            }
        } catch (SecurityException e) {
            System.err.println(e.getLocalizedMessage());
        }
        return false;
    }

    public static StringBuilder htmlEscapeSpace(String uri) {
        StringBuilder sbURI = new StringBuilder((int) (uri.length() * 1.5));
        char ch;
        for (int i = 0; i < uri.length(); ++i) {
            ch = uri.charAt(i);
            if (ch == ' ') {
                sbURI.append("%20");
            } else if (ch == '\\') {
                sbURI.append('/');
            } else {
                sbURI.append(ch);
            }
        }
        return sbURI;
    }

    /**
     * Reads all content from a given InputStream into a String using the default platform encoding.
     *
     * @param is the InputStream to read from. Must already be open, and will NOT be closed by this function. Failing to
     * close this stream after the call will result in a resource leak.
     *
     * @return String containing contents read from the stream
     * @throws IOException if the stream could not be read
     */
    public static String inputStreamToString(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        StringWriter sw = new StringWriter();
        char[] c = new char[1024];
        while (true) {
            int n = br.read(c, 0, c.length);
            if (n < 0) break;
            sw.write(c, 0, n);
        }
        isr.close();
        return sw.toString();
    }

    public static void writeStringToFile(String content, String encoding, String fileName)
            throws IOException {
        File f = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(f)) {
            OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
            BufferedWriter bw = new BufferedWriter(osw);
            try (PrintWriter pw = new PrintWriter(bw)) {
                pw.print(content);
                pw.flush();
                bw.flush();
            }
        }
        System.out.println("Wrote file: " + f.getAbsolutePath());
    }

    /**
     * Parses an integer from a string using less restrictive rules about which
     * characters we won't accept.  This scavenges the supplied string for any
     * numeric character, while dropping all others.
     *
     * @param s The string to parse
     * @return The number represented by the passed string, or 0 if the string
     *         is null, empty, white-space only, contains only non-numeric
     *         characters, or simply evaluates to 0 after parsing (e.g. "0")
     */
    public static int parseIntRelaxed(String s) {
        // An edge-case short circuit...
        if (s == null || s.isEmpty() || s.trim().isEmpty()) {
            return 0;
        }

        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isDigit(c)) {
                buffer.append(c);
            } else {
                // If we hit a non-numeric with numbers already in the
                // buffer, we're done.
                if (!buffer.isEmpty()) {
                    break;
                }
            }
        }

        if (buffer.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(buffer.toString());
        } catch (NumberFormatException exception) {
            // The only way we get here now is if s > Integer.MAX_VALUE
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Converts any special characters into their corresponding HTML entities , for example < to &lt;. This is done using a character
     * by character test, so you may consider other approaches for large documents. Make sure you declare the
     * entities that might appear in this replacement, e.g. the latin-1 entities
     * This method was taken from a code-samples website, written and hosted by Real Gagnon, at
     * <a href="http://www.rgagnon.com/javadetails/java-0306.html">...</a>.
     *
     * @param s The String which may contain characters to escape.
     * @return The string with the characters as HTML entities.
     */
    public static String escapeHTML(String s){
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                // be careful with this one (non-breaking white space)
                case ' ':
                    sb.append("&nbsp;");
                    break;

                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
}
