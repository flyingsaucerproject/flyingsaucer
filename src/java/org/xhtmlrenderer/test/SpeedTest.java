
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

import java.awt.image.BufferedImage;
import org.xhtmlrenderer.swing.DOMInspector;
import org.xhtmlrenderer.css.*;
import org.xhtmlrenderer.layout.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.xhtmlrenderer.util.x;
import org.xhtmlrenderer.util.u;
import org.w3c.dom.*;
import java.io.File;
import org.xhtmlrenderer.render.Box;
import java.util.Date;
import org.xhtmlrenderer.DefaultCSSMarker;
import java.io.*;

public class SpeedTest {
    public static void main(String[] args) throws Exception {

        // load doc
        Document doc = x.loadDocument("demos/paragraph.xhtml");
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
