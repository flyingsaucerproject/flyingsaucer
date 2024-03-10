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
package org.xhtmlrenderer.util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import static java.util.Arrays.asList;

public class Util {
    private PrintWriter pw;
    private boolean on = true;

    public Util(PrintWriter writer) {
        this.pw = writer;
    }

    public Util(OutputStream out) {
        this.pw = new PrintWriter(out);
    }

    /*
     * ------------ static stuff -------------
     */
    /*
     * ---- general print functions -----
     */
    public void print(Object o) {
        println(o, false);
    }

    public void println(Object o) {
        println(o, true);
    }

    public void println(Object o, boolean line) {
        if (o == null) {
            ps("null");
            return;
        }
        //ps("in p: " + o.getClass());
        if (o instanceof Object[]) {
            print_array((Object[]) o);
            return;
        }
        if (o instanceof int[]) {
            print_array((int[]) o);
        }
        if (o instanceof String) {
            ps((String) o, line);
            return;
        }
        if (o instanceof Exception) {
            ps(stack_to_string((Exception) o));
            return;
        }
        if (o instanceof Vector) {
            print_vector((Vector<?>) o);
            return;
        }
        if (o instanceof Map<?,?>) {
            print_map((Map<?,?>) o);
            return;
        }
        if (o instanceof Date) {
            print_date((Date) o);
            return;
        }
        if (o instanceof Calendar) {
            print_calendar((Calendar) o);
            return;
        }

        ps(o.toString(), line);
    }


    /*
     * --- data type specific print functions ----
     */
    public void print_vector(Vector<?> v) {
        ps("vector: size=" + v.size());
        for (int i = 0; i < v.size(); i++) {
            ps(v.elementAt(i).toString());
        }
    }

    public void print_array(int[][] array) {
        print("array: size=" + array.length + " by " + array[0].length);
        for (int[] ints : array) {
            for (int anInt : ints) {
                //pr("i = " + i + " j = " + j);
                ps(anInt + " ", false);
            }
            print("");
        }
    }

    public void print_array(Object[] array) {
        print("array: size=" + array.length);
        for (Object o : array) {
            ps(" " + o.toString(), false);
        }
    }

    public void print_array(int[] array) {
        print("array: size=" + array.length);
        for (int j : array) {
            ps(" " + j, false);
        }
    }

    public void print_map(Map<?,?> h) {
        print("hashtable size=" + h.size());
        for (Object key : h.keySet()) {
            print(key + " = ");
            print(h.get(key).toString());
        }
    }

    public void print_array(byte[] array) {
        print("byte array: size = " + array.length);
        for (byte b : array) {
            print("" + b);
        }
    }

    public void print_date(Date date) {
        DateFormat date_format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        print(date_format.format(date));
    }

    public void print_calendar(Calendar cal) {
        print(cal.getTime());
    }

    public void printUnixtime(long sec) {
        print(new Date(sec * 1000));
    }

    /**
     * Sets the on attribute of the Util object
     *
     * @param on The new on value
     */
    public void setOn(boolean on) {
        this.on = on;
    }


    /**
     * Sets the printWriter attribute of the Util object
     *
     * @param writer The new printWriter value
     */
    public void setPrintWriter(PrintWriter writer) {
        this.pw = writer;
    }

    private void ps(String s) {
        ps(s, true);
    }

    private void ps(String s, boolean line) {
        if (!on) {
            return;
        }
        if (line) {
            if (pw == null) {
                System.out.println(s);
            } else {
                //System.out.println(s);
                pw.println(s);
                //pw.println("<br>");
            }
        } else {
            if (pw == null) {
                System.out.print(s);
            } else {
                //System.out.print(s);
                pw.print(s);
                //pw.print("<br>");
            }
        }
    }


    /*
     * ----- other stuff ----
     */
    public static String file_to_string(String filename) throws IOException {
        File file = new File(filename);
        return file_to_string(file);
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

    public static int string_to_int(String str) {
        return Integer.parseInt(str);
    }

    public static String stack_to_string(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

    public static String stack_to_string(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

    public static String file_to_string(File file)
            throws IOException {
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

    public static String replace(String source, String target, String replacement) {
        StringBuilder output = new StringBuilder();
        int n = 0;
        while (true) {
            //print("n = " + n);
            int off = source.indexOf(target, n);
            if (off == -1) {
                output.append(source.substring(n));
                break;
            }
            output.append(source.substring(n, off));
            output.append(replacement);
            n = off + target.length();
        }
//        output.append(source.substring(off+target.length()));
        return output.toString();
    }

    public static String[] vector_to_strings(Vector<?> v) {
        int len = v.size();
        String[] ret = new String[len];
        for (int i = 0; i < len; i++) {
            ret[i] = v.elementAt(i).toString();
        }
        return ret;
    }

    public static String[] list_to_strings(List<?> l) {
        int len = l.size();
        String[] ret = new String[len];
        for (int i = 0; i < len; i++) {
            ret[i] = l.get(i).toString();
        }
        return ret;
    }

    public static List<?> toList(Object[] array) {
        return to_list(array);
    }

    public static List<Object> to_list(Object[] array) {
        return asList(array);
    }

    /*
     * public void pr(Date date) {
     * DateFormat date_format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
     * pr(date_format.format(date));
     * }
     */
    /*
     * public void pr(Calendar cal) {
     * pr(cal.getTime());
     * }
     */

    public static void sleep(long msec) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException ex) {
            org.xhtmlrenderer.util.Uu.p(stack_to_string(ex));
        }
    }

    public static void center(JFrame frame) {
        //p("centering");
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((int) ((screen_size.getWidth() - frame.getWidth()) / 2),
                (int) ((screen_size.getHeight() - frame.getHeight()) / 2));
    }

    public static void center(JDialog frame) {
        //p("centering");
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((int) ((screen_size.getWidth() - frame.getWidth()) / 2),
                (int) ((screen_size.getHeight() - frame.getHeight()) / 2));
    }


    /**
     * Gets the number attribute of the Util class
     */
    public static boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNullOrEmpty(String str, boolean trim) {
        return str == null || str.isEmpty() || (trim && str.trim().isEmpty());
    }

    public static boolean isEqual(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    public static boolean isEqual(String str1, String str2, boolean ignoreCase) {
        return Objects.equals(str1, str2) || str1 != null && ignoreCase && str1.equalsIgnoreCase(str2);
    }
}
