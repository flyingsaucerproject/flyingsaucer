package org.xhtmlrenderer.util;

import org.xhtmlrenderer.util.Util;
public class u extends Util {
    private u() {
        super(System.out);
    }
    private static Util util;
    private static void init() {
        if(util == null) {
            util = new Util(System.out);
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
        util.println(object);
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
}
