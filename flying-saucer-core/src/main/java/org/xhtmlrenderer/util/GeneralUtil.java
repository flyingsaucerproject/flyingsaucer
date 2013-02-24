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

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Description of the Class
 *
 * @author Patrick Wright
 */
public class GeneralUtil {

    /**
     * Used to format an Object's hashcode into a 0-padded 10 char String, e.g.
     * for 24993066 returns "0024993066"
     */
    public final static java.text.DecimalFormat PADDED_HASH_FORMAT = new java.text.DecimalFormat("0000000000");

    /**
     * Description of the Method
     *
     * @param obj      PARAM
     * @param resource PARAM
     * @return Returns
     */
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
     *
     * @param ex PARAM
     */
    public static void dumpShortException(Exception ex) {
        String s = ex.getMessage();
        if (s == null || s.trim().equals("null")) {
            s = "{no ex. message}";
        }
        System.out.println(s + ", " + ex.getClass());
        StackTraceElement[] stes = ex.getStackTrace();
        for (int i = 0; i < stes.length && i < 5; i++) {
            StackTraceElement ste = stes[i];
            System.out.println("  " + ste.getClassName() + "." + ste.getMethodName() + "(ln " + ste.getLineNumber() + ")");
        }
    }

    /**
     * Returns a String tracking the last n method calls, from oldest to most
     * recent. You can use this as a simple tracing mechanism to find out the
     * calls that got to where you execute the <code>trackBack()</code> call
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
     * The <code>trackBack()</code> method itself is always excluded from the dump.
     * Note the output may not be useful if HotSpot has been optimizing the
     * code.
     *
     * @param cnt How far back in the call tree to go; if call tree is smaller, will
     *            be limited to call tree.
     * @return see desc
     */
    public static String trackBack(int cnt) {
        Exception ex = new Exception();
        StringBuffer sb = new StringBuffer();
        List list = new ArrayList(cnt);
        StackTraceElement[] stes = ex.getStackTrace();
        if (cnt >= stes.length) {
            cnt = stes.length - 1;
        }

        // >= 1 to not include this method
        for (int i = cnt; i >= 1; i--) {
            StackTraceElement ste = stes[i];
            sb.append(GeneralUtil.classNameOnly(ste.getClassName()));
            sb.append(".");
            sb.append(ste.getMethodName());
            sb.append("(ln ").append(ste.getLineNumber()).append(")");
            list.add(sb.toString());
            sb = new StringBuffer();
        }

        Iterator iter = list.iterator();
        StringBuffer padding = new StringBuffer("");
        StringBuffer trackback = new StringBuffer();
        while (iter.hasNext()) {
            String s = (String) iter.next();
            trackback.append(padding).append(s).append("\n");
            padding.append("   ");
        }
        return trackback.toString();
    }


    /**
     * Given an Object instance, returns just the classname with no package
     *
     * @param o PARAM
     * @return Returns
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
     *
     * @param cname PARAM
     * @return Returns
     */
    public static String classNameOnly(String cname) {
        String s = "[null object ref]";
        if (cname != null) {
            s = cname.substring(cname.lastIndexOf('.') + 1);
        }
        return s;
    }

    /**
     * Description of the Method
     *
     * @param o PARAM
     * @return Returns
     */
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

    public static StringBuffer htmlEscapeSpace(String uri) {
        StringBuffer sbURI = new StringBuffer((int) (uri.length() * 1.5));
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
        char c[] = new char[1024];
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
        FileOutputStream fos = new FileOutputStream(f);
        try {
            OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
            BufferedWriter bw = new BufferedWriter(osw);
            PrintWriter pw = new PrintWriter(bw);
            try {
                pw.print(content);
                pw.flush();
                bw.flush();
            } finally {
                try {
                    pw.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
                // ignore
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
        if (s == null || s.length() == 0 || s.trim().length() == 0) {
            return 0;
        }

        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isDigit(c)) {
                buffer.append(c);
            } else {
                // If we hit a non-numeric with numbers already in the
                // buffer, we're done.
                if (buffer.length() > 0) {
                    break;
                }
            }
        }

        if (buffer.length() == 0) {
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
     * http://www.rgagnon.com/javadetails/java-0306.html.
     *
     * @param s The String which may contain characters to escape.
     * @return The string with the characters as HTML entities.
     */
    public static String escapeHTML(String s){
    	if (s == null) {
    		return "";
    	}
        StringBuffer sb = new StringBuffer();
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
                /*
             case 'ÔøΩ': sb.append("&agrave;");break;
             case 'ÔøΩ': sb.append("&Agrave;");break;
             case 'ÔøΩ': sb.append("&acirc;");break;
             case 'ÔøΩ': sb.append("&Acirc;");break;
             case 'ÔøΩ': sb.append("&auml;");break;
             case 'ÔøΩ': sb.append("&Auml;");break;
             case 'ÔøΩ': sb.append("&aring;");break;
             case 'ÔøΩ': sb.append("&Aring;");break;
             case 'ÔøΩ': sb.append("&aelig;");break;
             case 'ÔøΩ': sb.append("&AElig;");break;
             case 'ÔøΩ': sb.append("&ccedil;");break;
             case 'ÔøΩ': sb.append("&Ccedil;");break;
             case 'ÔøΩ': sb.append("&eacute;");break;
             case 'ÔøΩ': sb.append("&Eacute;");break;
             case 'ÔøΩ': sb.append("&egrave;");break;
             case 'ÔøΩ': sb.append("&Egrave;");break;
             case 'ÔøΩ': sb.append("&ecirc;");break;
             case 'ÔøΩ': sb.append("&Ecirc;");break;
             case 'ÔøΩ': sb.append("&euml;");break;
             case 'ÔøΩ': sb.append("&Euml;");break;
             case 'ÔøΩ': sb.append("&iuml;");break;
             case 'ÔøΩ': sb.append("&Iuml;");break;
             case 'ÔøΩ': sb.append("&ocirc;");break;
             case 'ÔøΩ': sb.append("&Ocirc;");break;
             case 'ÔøΩ': sb.append("&ouml;");break;
             case 'ÔøΩ': sb.append("&Ouml;");break;
             case 'ÔøΩ': sb.append("&oslash;");break;
             case 'ÔøΩ': sb.append("&Oslash;");break;
             case 'ÔøΩ': sb.append("&szlig;");break;
             case 'ÔøΩ': sb.append("&ugrave;");break;
             case 'ÔøΩ': sb.append("&Ugrave;");break;
             case 'ÔøΩ': sb.append("&ucirc;");break;
              case 'ÔøΩ': sb.append("&Ucirc;");break;
             case 'ÔøΩ': sb.append("&uuml;");break;
             case 'ÔøΩ': sb.append("&Uuml;");break;
             case 'ÔøΩ': sb.append("&reg;");break;
             case 'ÔøΩ': sb.append("&copy;");break;
             case 'ÔøΩ': sb.append("&euro;"); break;
                */
                // be carefull with this one (non-breaking whitee space)
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

/*
 * $Id$
 *
 * $Log$
 * Revision 1.19  2009/05/09 15:16:43  pdoubleya
 * FindBugs: proper disposal of IO resources
 *
 * Revision 1.18  2009/04/25 11:03:27  pdoubleya
 * Fix some potential IO resource leaks, patches from Peter Fassev in issue #263. Clarifying docs on inputStreamToString() (we don't close the stream, the caller does), and a couple of other minor edits.
 *
 * Revision 1.17  2008/03/13 16:46:47  peterbrant
 * Comment out non-ASCII characters in escapeHTML() for now.  Will only work if the compiler assumes the source file encoding is ISO-8859-1 (or maybe Cp1252).  Does not work on Linux (with a default encoding of UTF-8).  Should be replaced with equivalent Unicode escapes.
 *
 * Revision 1.16  2008/03/01 19:27:28  pdoubleya
 * Utility method to convert certain character to HTML entity equivalents.
 *
 * Revision 1.15  2007/05/11 22:51:35  peterbrant
 * Patch from Sean Bright
 *
 * Revision 1.14  2007/04/10 20:46:38  pdoubleya
 * Fix, was not checking if resource was actually available before opening it
 *
 * Revision 1.13  2006/07/26 18:17:09  pdoubleya
 * Added convenience method, write string to file.
 *
 * Revision 1.12  2006/07/17 22:16:31  pdoubleya
 * Added utility methods, InputStream to String, and simple HTML escaping.
 *
 * Revision 1.11  2005/06/26 01:02:22  tobega
 * Now checking for SecurityException on System.getProperty
 *
 * Revision 1.10  2005/06/13 06:50:17  tobega
 * Fixed a bug in table content resolution.
 * Various "tweaks" in other stuff.
 *
 * Revision 1.9  2005/04/03 21:51:31  joshy
 * fixed code that gets the XMLReader on the mac
 * added isMacOSX() to GeneralUtil
 * added app name and single menu bar to browser
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2005/02/02 11:17:18  pdoubleya
 * Added trackBack() method.
 *
 * Revision 1.7  2005/01/29 20:21:08  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.6  2005/01/24 14:33:47  pdoubleya
 * Added exception dump.
 *
 * Revision 1.5  2004/10/23 14:06:56  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 * Revision 1.4  2004/10/19 15:00:53  joshy
 * updated the build file
 * removed some extraneous files
 * update the home page to point to the new jnlp files
 * updated the resource loader to use the marker class
 * updated the text of the about box
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/18 23:43:02  joshy
 * final updates today
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.2  2004/10/18 17:10:13  pdoubleya
 * Added additional condition, and error check.
 *
 * Revision 1.1  2004/10/13 23:00:32  pdoubleya
 * Added to CVS.
 *
 */

