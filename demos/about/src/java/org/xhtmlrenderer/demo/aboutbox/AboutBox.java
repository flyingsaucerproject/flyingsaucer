
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

package org.joshy.html.app.aboutbox;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import org.joshy.html.*;
import org.joshy.u;
import org.joshy.html.app.browser.*;

public class AboutBox extends JDialog implements Runnable {
    JScrollPane scroll;
    JButton close_button;
    boolean go = false;
    
    public AboutBox(String text, String url) {
        super();
        u.p("starting the about box");
        setTitle(text);
        HTMLPanel panel = new HTMLPanel();
        int w = 400;
        int h = 500;
        panel.setPreferredSize(new Dimension(w,h));

        
        scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(scroll.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(scroll.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(w,h));
        panel.setViewportComponent(scroll);
        panel.setJScrollPane(scroll);
        getContentPane().add(scroll,"Center");
        close_button = new JButton("Close");
        getContentPane().add(close_button,"South");
        close_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
                go = false;
            }
        });
        
        try {
            loadPage(url,panel);
        } catch (Exception ex) {
            u.p(ex);
        }
        pack();
        setSize(w,h);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width-w)/2,(screen.height-h)/2);
    }
    
    public void loadPage(String url_text, HTMLPanel panel) throws Exception {
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setValidating(true);
        DocumentBuilder builder = fact.newDocumentBuilder();
        //builder.setErrorHandler(root.error_handler);
        Document doc = null;
        
        
        URL ref = null;
        
        if(url_text.startsWith("demo:")) {
            u.p("starts with demo");
            DemoMarker marker = new DemoMarker();
            u.p("url text = " + url_text);
            String short_url = url_text.substring(5);
            if(!short_url.startsWith("/")) {
                short_url = "/" + short_url;
            }
            u.p("short url = " + short_url);
            ref = marker.getClass().getResource(short_url);
            u.p("ref = " + ref);
            doc = builder.parse(marker.getClass().getResourceAsStream(short_url));
        } else if(url_text.startsWith("http")) {
            doc = builder.parse(url_text);
            ref = new File(url_text).toURL();
        } else {
            doc = builder.parse(url_text);
            ref = new File(url_text).toURL();
        }
        u.p("ref = " + ref);
        u.p("url_text = " + url_text);
        panel.setDocument(doc,ref);
    }

    public void setVisible(boolean vis) {
        super.setVisible(vis);
        if(vis == true) {
            startScrolling();
        }
    }
    
    Thread thread;
    public void startScrolling() {
        go = true;
        thread = new Thread(this);
        thread.start();
    }
    
    public void run() {
        while(go) {
            try {
                Thread.currentThread().sleep(100);
            } catch (Exception ex) {
                u.p(ex);
            }
            JScrollBar sb = scroll.getVerticalScrollBar();
            sb.setValue(sb.getValue()+1);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("About Box Test");
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        JButton launch = new JButton("Show About Box");
        frame.getContentPane().add(launch);
        frame.pack();
        frame.setVisible(true);
        
        launch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                AboutBox ab = new AboutBox("About Flying Saucer","demo:demos/about/index.xhtml");
                ab.setVisible(true);
            }
        });
    }
}
