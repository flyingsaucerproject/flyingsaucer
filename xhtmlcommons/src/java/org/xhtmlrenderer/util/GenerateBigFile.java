package org.xhtmlrenderer.util;

import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-jan-05
 * Time: 09:21:10
 * To change this template use File | Settings | File Templates.
 */
public class GenerateBigFile {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage:");
            System.out.println("GenerateBigFile output-file");
            System.exit(1);
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileOutputStream(args[0]));
            out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Big test file</title></head><body>");
            for (int i = 0; i < 10000; i++) {
                //style: 10pt Times #000000;
                String[] styles = {"10pt", "12pt", "14pt", "18pt", "24pt"};
                String[] fonts = {"Times", "Helvetica", "Courier"};
                String style = styles[(int) Math.floor(Math.random() * styles.length)];
                String font = fonts[(int) Math.floor(Math.random() * fonts.length)];
                String colour = Integer.toHexString((int) Math.floor(Math.random() * 256)) + Integer.toHexString((int) Math.floor(Math.random() * 256)) + Integer.toHexString((int) Math.floor(Math.random() * 256));
                out.println("<p style=\"font: " + style + " " + font + "; color: #" + colour + "\">Some Styled text to see how we can handle it</p>");
            }
            out.println("</body></html>");
        } catch (Exception e) {//I know, never do this :-)
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
}
