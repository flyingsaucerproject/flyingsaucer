
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

package org.xhtmlrenderer.test;



import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;

import java.awt.image.BufferedImage;

import org.xhtmlrenderer.swing.DOMInspector;

import javax.swing.*;

import java.awt.*;

import java.awt.event.*;

import org.joshy.x;

import org.joshy.u;

import org.w3c.dom.*;

import java.io.File;

import org.xhtmlrenderer.render.Box;

import java.util.Date;

import org.xhtmlrenderer.css.*;

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
    }

    

    public static void runLoopTest(Context c, Graphics g, Element html) throws Exception {

        Element body = x.child(html,"body");

        Point origin = new Point(0,0);

        Point last = new Point(0,0);

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
        int loop = 30;
        for(int i=0; i < loop; i++) {

            u.sleep(100);

            long start_time = new java.util.Date().getTime();

            // execute layout

            //u.p("body = " + body);

            Box body_box = layout.layout(c,body);

            long end_time = new java.util.Date().getTime();

            u.p(i + ") ending count = " + (end_time-start_time) + " msec");

            if ( i > 0 )
                total += (end_time-start_time);

        }

        u.p("avg = " + (total/loop));

    }

}

