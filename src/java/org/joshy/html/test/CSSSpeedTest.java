
/* 
 * {{{ header & license 
 * Copyright (c) 2004 Joshua Marinacci 
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

import org.joshy.html.css.*;

import java.io.*;

import com.pdoubleya.xhtmlrenderer.css.bridge.XRStyleReference;


public class CSSSpeedTest {

    public static void main(String[] args) throws Exception {

        

        // load doc

        Document doc = x.loadDocument("demos/hamlet.xhtml");

        Element html = (Element)doc.getDocumentElement();

        

        // create buffer

        BufferedImage buff = new BufferedImage(500,500, BufferedImage.TYPE_4BYTE_ABGR);

        

        // get graphics

        Graphics g = buff.getGraphics();

        

        

        // create context
        Context c = null;
        /*         
        u.p("RuleFinder");

        c = new Context();

        c.css = new CSSBank();

        runLoopTest(c,g,html);

        u.p("");

        u.p("MozRuleBank");

        c = new Context();

        c.css = new CSSBank(new MozRuleBank());

        runLoopTest(c,g,html); */
        /* CLEAN
        u.p("");

        u.p("TobeRuleBank");

        c = new Context();

        c.css = new CSSBank(new TobeRuleBank());

        runLoopTest(c,g,html);
        */
        u.p("");

        u.p("XRStyleReference");

        c = new Context();

        c.css = new XRStyleReference(c);

        runLoopTest(c,g,html);

        u.p("");

        u.p("TBStyleReference");

        c = new Context();

        c.css = new net.homelinux.tobe.xhtmlrenderer.bridge.TBStyleReference(c);

        runLoopTest(c,g,html);
    }

    

    public static void runLoopTest(Context c, Graphics g, Element html) throws Exception {
        /*
        
        Joshua:  I commented this out to get CVS to commit. I'll have to fix this
        conflict later

        Element body = x.child(html,"body");

        Point origin = new Point(0,0);

        Point last = new Point(0,0);

        Object marker = new DefaultCSSMarker();

        //u.p("getting: " + marker.getClass().getResource("default.css"));
        String def = "/resources/css/default.css";

<<<<<<< CSSSpeedTest.java
        InputStream stream = marker.getClass().getResourceAsStream(def);
=======

               

        int total = 0;
        long first = 0;
        int loop = 10;
        for(int i=0; i < loop; i++) {

            u.sleep(100);

            long start_time = new java.util.Date().getTime();

        InputStream stream = marker.getClass().getResourceAsStream("default.css");
>>>>>>> 1.6

        c.css.parse(new InputStreamReader(stream));

        c.css.parseInlineStyles(html);

        c.graphics = g;

        c.setExtents(new Rectangle(0,0,500,500));

        //c.viewport = this.viewport;

        c.cursor = last;

        c.setMaxWidth(0);

        

        

        // create layout

        BodyLayout layout = new BodyLayout();

        
            // execute layout

            //u.p("body = " + body);

            Box body_box = layout.layout(c,body);

            long end_time = new java.util.Date().getTime();

            u.p(i + ") ending count = " + (end_time-start_time) + " msec");

            if ( i == 0 )
                first = (end_time-start_time);
            else
                total += (end_time-start_time);

        }

        u.p("first = "+first+", avg = " + (total/(loop-1)));

        */
    }

}

