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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

public class Util {

    /*
     * ------------ static stuff -------------
     */
    /*
     * ---- general print functions -----
     */
    private void print(Object o) {
        if (o == null) {
            ps("null");
            return;
        }
        if (o instanceof Object[]) {
            print_array((Object[]) o);
            return;
        }
        if (o instanceof int[]) {
            print_array((int[]) o);
        }
        if (o instanceof String) {
            ps((String) o, false);
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

        ps(o.toString(), false);
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
    
    public void print_date(Date date) {
        DateFormat date_format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        print(date_format.format(date));
    }

    public void print_calendar(Calendar cal) {
        print(cal.getTime());
    }

    private void ps(String s) {
        ps(s, true);
    }

    private void ps(String s, boolean line) {
        if (line) {
            System.out.println(s);
        } else {
            System.out.print(s);
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
    
    public static String stack_to_string(Exception e) {
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
            int off = source.indexOf(target, n);
            if (off == -1) {
                output.append(source.substring(n));
                break;
            }
            output.append(source, n, off);
            output.append(replacement);
            n = off + target.length();
        }
        return output.toString();
    }
    
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
