package org.xhtmlrenderer.util;


import java.util.logging.*;

/** 
Utility class for using the java.util.logging package. Relies
on the standard configuration for logging, but gives easier access
to the various logs (plumbing.load, .init, .render)
*/
public class XRLog {
    
    static {
        try {
          new LogStartupConfig();
        } catch (Exception ex) {
          throw new XRRuntimeException("Could not initialize logs.");
        }    
    }
    
    private static final String EXCEPTION = "plumbing.exception";
    private static final String GENERAL = "plumbing.general";
    private static final String INIT = "plumbing.init";
    private static final String LOAD = "plumbing.load";
    private static final String MATCH = "plumbing.match";
    private static final String CASCADE = "plumbing.cascade";
    private static final String LAYOUT = "plumbing.layout";
    private static final String RENDER = "plumbing.render";
    
    /** Same purpose as Logger.getLogger(), except that the static
    initialization for XRLog will initialize the LogManager with logging
    levels and other configuration. Use this instead of Logger.getLogger() */
    public static Logger getLogger(String log) {
        return Logger.getLogger(log);
    }
    
    public static void cascade(String msg) { cascade(Level.INFO, msg); } 
    public static void cascade(Level level, String msg) { log(CASCADE, level, msg); } 
    public static void cascade(Level level, String msg, Throwable th) { log(CASCADE, level, msg, th); } 

    public static void exception(String msg) { exception(msg, null); } 
    public static void exception(String msg, Throwable th) { log(EXCEPTION, Level.WARNING, msg, th); } 

    public static void general(String msg) { general(Level.INFO, msg); } 
    public static void general(Level level, String msg) { log(GENERAL, level, msg); } 
    public static void general(Level level, String msg, Throwable th) { log(GENERAL, level, msg, th); } 

    public static void init(String msg) { init(Level.INFO, msg); } 
    public static void init(Level level, String msg) { log(INIT, level, msg); } 
    public static void init(Level level, String msg, Throwable th) { log(INIT, level, msg, th); } 
    
    public static void load(String msg) { load(Level.INFO, msg); } 
    public static void load(Level level, String msg) { log(LOAD, level, msg); } 
    public static void load(Level level, String msg, Throwable th) { log(LOAD, level, msg, th); } 

    public static void match(String msg) { match(Level.INFO, msg); } 
    public static void match(Level level, String msg) { log(MATCH, level, msg); } 
    public static void match(Level level, String msg, Throwable th) { log(MATCH, level, msg, th); } 

    public static void layout(String msg) { layout(Level.INFO, msg); } 
    public static void layout(Level level, String msg) { log(LAYOUT, level, msg); } 
    public static void layout(Level level, String msg, Throwable th) { log(LAYOUT, level, msg, th); } 

    public static void render(String msg) { render(Level.INFO, msg); } 
    public static void render(Level level, String msg) { log(RENDER, level, msg); } 
    public static void render(Level level, String msg, Throwable th) { log(RENDER, level, msg, th); } 
    
    public static void log(String where, Level level, String msg) {
        getLogger(where).log(level, msg); 
    } 

    public static void log(String where, Level level, String msg, Throwable th) {
        getLogger(where).log(level, msg, th); 
    } 
    
    public static void main(String args[]) {
        try {
            XRLog.cascade("Cascade msg");    
            XRLog.cascade(Level.WARNING, "Cascade msg");    
            XRLog.exception("Exception msg");    
            XRLog.exception("Exception msg", new Exception());    
            XRLog.general("General msg");    
            XRLog.general(Level.WARNING, "General msg");    
            XRLog.init("Init msg");    
            XRLog.init(Level.WARNING, "Init msg");    
            XRLog.load("Load msg");    
            XRLog.load(Level.WARNING, "Load msg");    
            XRLog.match("Match msg");    
            XRLog.match(Level.WARNING, "Match msg");    
            XRLog.layout("Layout msg");    
            XRLog.layout(Level.WARNING, "Layout msg");    
            XRLog.render("Render msg");    
            XRLog.render(Level.WARNING, "Render msg");    
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    } // end main()
} // end class
