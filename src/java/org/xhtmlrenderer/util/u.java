package org.xhtmlrenderer.util;

import org.xhtmlrenderer.util.Util;
import org.xhtmlrenderer.util.XRLog;
import java.io.*;

public class u extends Util {
    private u() {
        super(System.out);
    }
    private static Util util;
    private static Util utilAsString;
    private static void init() {
        if(util == null) {
            util = new Util(System.out);
        }
        if(utilAsString == null) {
            utilAsString = new Util(System.out);
        }
    }
    public static void on() {
        init();
        util.setOn(true);
    }
    public static void off() {
        init();
        util.setOn(false);
    }
    public static void p(Object object) {
        init();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        utilAsString.setPrintWriter(pw);
        utilAsString.print(object); // our log adds a newline
        pw.flush();
        XRLog.general(sw.getBuffer().toString());
    }
    public static void pr(Object object) {
        init();
        util.print(object);
    }
    public static void sleep(int msec) {
        try {
            Thread.currentThread().sleep(msec);
        } catch (InterruptedException ex) {
            p(ex);
        }
    }
    public static void dump_stack() {
        p(stack_to_string(new Exception()));
    }
    public static void main(String args[]) {
        try {
            u.p(new Object());    
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    } // end main()
}
