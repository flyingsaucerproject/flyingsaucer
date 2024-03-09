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

import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.Uu;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;


public final class AboutBox extends JDialog implements Runnable {
    private static final long serialVersionUID = 1L;

    private final JScrollPane scroll;
    private boolean go;

    public AboutBox(String text, String url) {
        Uu.p("starting the about box");
        setTitle(text);
        XHTMLPanel panel = new XHTMLPanel(new DemoUserAgent());
        int w = 400;
        int h = 500;
        panel.setPreferredSize(new Dimension(w, h));

        scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(w, h));
        //panel.setViewportComponent(scroll);
        //panel.setJScrollPane(scroll);
        getContentPane().add(scroll, "Center");
        JButton close_button = new JButton("Close");
        getContentPane().add(close_button, "South");
        close_button.addActionListener(evt -> {
            setVisible(false);
            go = false;
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

    public void loadPage(String url_text, XHTMLPanel panel) throws MalformedURLException {
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
    panel.setDocument(ref.toExternalForm());
} else if (url_text.startsWith("http")) {
    panel.setDocument(url_text);
} else {
    ref = new File(url_text).toURI().toURL();
    panel.setDocument(ref.toExternalForm());
}
Uu.p("ref = " + ref);
Uu.p("url_text = " + url_text);
}

    public void startScrolling() {
        go = true;
        new Thread(this).start();
    }

    /**
     * Main processing method for the AboutBox object
     */
    @Override
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
    @Override
    public void setVisible(boolean vis) {
        super.setVisible(vis);
        if (vis) {
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

        launch.addActionListener(evt -> {
            AboutBox ab = new AboutBox("About Flying Saucer", "demo:demos/index.xhtml");
            ab.setVisible(true);
        });
    }
}
