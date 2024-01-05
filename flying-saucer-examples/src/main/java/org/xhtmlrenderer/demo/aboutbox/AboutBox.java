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
    private final JButton close_button;
    private boolean go;
    private Thread thread;

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
        close_button = new JButton("Close");
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
        thread = new Thread(this);
        thread.start();
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

/*
 * $Id$
 *
 * $Log$
 * Revision 1.11  2007/05/20 23:25:33  peterbrant
 * Various code cleanups (e.g. remove unused imports)
 *
 * Patch from Sean Bright
 *
 * Revision 1.10  2006/07/31 14:32:40  pdoubleya
 * Fix throw claiuse
 *
 * Revision 1.9  2006/04/08 22:01:04  pdoubleya
 * Hack, workaround direct file access in NaiveUserAgent, required explicit security in webstart app.
 *
 * Revision 1.8  2005/06/15 11:53:44  tobega
 * Changed UserAgentCallback to getInputStream instead of getReader. Fixed up some consequences of previous change.
 *
 * Revision 1.7  2005/06/01 21:36:33  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.6  2005/02/05 17:21:02  pdoubleya
 * Use XMLResource for loading XML.
 *
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
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc.)
 * Added CVS log comments at bottom.
 *
 *
 */

