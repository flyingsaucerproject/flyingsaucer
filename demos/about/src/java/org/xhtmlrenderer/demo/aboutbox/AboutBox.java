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
package org.xhtmlrenderer.demo.aboutbox;

import org.w3c.dom.Document;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.Uu;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;


/**
 * Description of the Class
 *
 * @author empty
 */
public class AboutBox extends JDialog implements Runnable {
    /**
     * Description of the Field
     */
    JScrollPane scroll;
    /**
     * Description of the Field
     */
    JButton close_button;
    /**
     * Description of the Field
     */
    boolean go = false;

    /**
     * Description of the Field
     */
    Thread thread;

    /**
     * Constructor for the AboutBox object
     *
     * @param text PARAM
     * @param url  PARAM
     */
    public AboutBox(String text, String url) {
        super();
        Uu.p("starting the about box");
        setTitle(text);
        XHTMLPanel panel = new XHTMLPanel();
        int w = 400;
        int h = 500;
        panel.setPreferredSize(new Dimension(w, h));

        scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(w, h));
        //panel.setViewportComponent(scroll);
        //panel.setJScrollPane(scroll);
        getContentPane().add(scroll, "Center");
        close_button = new JButton("Close");
        getContentPane().add(close_button, "South");
        close_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
                go = false;
            }
        });

        try {
            loadPage(url, panel);
        } catch (Exception ex) {
            Uu.p(ex);
        }
        pack();
        setSize(w, h);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - w) / 2, (screen.height - h) / 2);
    }

    /**
     * Description of the Method
     *
     * @param url_text PARAM
     * @param panel    PARAM
     * @throws Exception Throws
     */
    public void loadPage(String url_text, XHTMLPanel panel)
            throws Exception {
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setValidating(true);
        DocumentBuilder builder = fact.newDocumentBuilder();
        //builder.setErrorHandler(root.error_handler);
        Document doc = null;

        URL ref = null;

        if (url_text.startsWith("demo:")) {
            Uu.p("starts with demo");
            DemoMarker marker = new DemoMarker();
            Uu.p("url text = " + url_text);
            String short_url = url_text.substring(5);
            if (!short_url.startsWith("/")) {
                short_url = "/" + short_url;
            }
            Uu.p("short url = " + short_url);
            ref = marker.getClass().getResource(short_url);
            Uu.p("ref = " + ref);
            doc = builder.parse(marker.getClass().getResourceAsStream(short_url));
        } else if (url_text.startsWith("http")) {
            doc = builder.parse(url_text);
            ref = new File(url_text).toURL();
        } else {
            doc = builder.parse(url_text);
            ref = new File(url_text).toURL();
        }
        Uu.p("ref = " + ref);
        Uu.p("url_text = " + url_text);
        panel.setDocument(doc, ref);
    }

    /**
     * Description of the Method
     */
    public void startScrolling() {
        go = true;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Main processing method for the AboutBox object
     */
    public void run() {
        while (go) {
            try {
                Thread.sleep(100);
            } catch (Exception ex) {
                Uu.p(ex);
            }
            JScrollBar sb = scroll.getVerticalScrollBar();
            sb.setValue(sb.getValue() + 1);
        }
    }

    /**
     * Sets the visible attribute of the AboutBox object
     *
     * @param vis The new visible value
     */
    public void setVisible(boolean vis) {
        super.setVisible(vis);
        if (vis == true) {
            startScrolling();
        }
    }

    /**
     * The main program for the AboutBox class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("About Box Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton launch = new JButton("Show About Box");
        frame.getContentPane().add(launch);
        frame.pack();
        frame.setVisible(true);

        launch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                AboutBox ab = new AboutBox("About Flying Saucer", "demo:demos/index.xhtml");
                ab.setVisible(true);
            }
        });
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2004/12/12 05:51:47  tobega
 * Now things run. But there is a lot to do before it looks as nice as it did. At least we now have :before and :after content and handling of breaks by css.
 *
 * Revision 1.4  2004/11/12 02:23:55  joshy
 * added new APIs for rendering context, xhtmlpanel, and graphics2drenderer.
 * initial support for font mapping additions
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 14:38:57  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

