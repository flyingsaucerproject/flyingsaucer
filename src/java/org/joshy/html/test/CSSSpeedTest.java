package org.joshy.html.test;

import org.joshy.html.*;
import java.awt.image.BufferedImage;
import org.joshy.html.swing.DOMInspector;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.joshy.x;
import org.joshy.u;
import org.w3c.dom.*;
import java.io.File;
import org.joshy.html.box.Box;
import java.util.Date;
import org.joshy.html.css.DefaultCSSMarker;
import java.io.*;

public class CSSSpeedTest {
    public static void main(String[] args) throws Exception {
        
        // load doc
        Document doc = x.loadDocument("demos/hamlet.xhtml");
        Element html = (Element)doc.getDocumentElement();
        Element body = x.child(html,"body");
        
        // create buffer
        BufferedImage buff = new BufferedImage(500,500, BufferedImage.TYPE_4BYTE_ABGR);
        
        // get graphics
        Graphics g = buff.getGraphics();
        
        
        // create context
        Context c = new Context();
        Point origin = new Point(0,0);
        Point last = new Point(0,0);
        //c.canvas = this;
        c.css = new CSSBank();
        Object marker = new DefaultCSSMarker();
        //u.p("getting: " + marker.getClass().getResource("default.css"));
        InputStream stream = marker.getClass().getResourceAsStream("default.css");
        c.css.parse(new InputStreamReader(stream));
        c.css.parseInlineStyles(html);
        c.graphics = g;
        c.setExtents(new Rectangle(0,0,500,500));
        //c.viewport = this.viewport;
        c.cursor = last;
        c.setMaxWidth(0);
        
        
        // create layout
        BodyLayout layout = new BodyLayout();
        
               
        int total = 0;
        for(int i=0; i<10; i++) {
            u.sleep(100);
            long start_time = new java.util.Date().getTime();
            // execute layout
            //u.p("body = " + body);
            Box body_box = layout.layout(c,body);
            long end_time = new java.util.Date().getTime();
            u.p("ending count = " + (end_time-start_time) + " msec");
            total += (end_time-start_time);
        }
        u.p("avg = " + (total/10));
    }
}
