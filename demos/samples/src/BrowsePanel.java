/*
 * Copyright (c) 2008 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */


import org.w3c.dom.Document;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.event.DefaultDocumentListener;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.swing.NaiveUserAgent;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.StringReader;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This example shows the most basic use of Flying Saucer, to
 * display a single page, scrollable, within a JFrame. The input
 * is a file name.
 *
 * @author Patrick Wright
 */
public class BrowsePanel {
    private String uri;
    private XHTMLPanel panel;
    private JFrame frame;
    private UserAgentCallback uac;

    public static void main(String[] args) throws Exception {
        new BrowsePanel().run(args);
    }

    private void run(String[] args) {
        loadAndCheckArgs(args);

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Create a JPanel subclass to render the page
                panel = new XHTMLPanel();
                setupDocumentListener(panel);
                setupUserAgentCallback(panel);

                // Put our panel in a scrolling pane. You can use
                // a regular JScrollPane here, or our FSScrollPane.
                // FSScrollPane is already set up to move the correct
                // amount when scrolling 1 line or 1 page
                FSScrollPane scroll = new FSScrollPane(panel);

                frame = new JFrame("Flying Saucer");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(scroll);
                frame.pack();
                frame.setSize(1024, 768);
                frame.setVisible(true);
                launchLoad();
            }
        });
    }

    private void setupUserAgentCallback(XHTMLPanel panel) {
        uac = new NaiveUserAgent() {
            //TOdO:implement this with nio.
            protected InputStream resolveAndOpenStream(String uri) {
                java.io.InputStream is = null;
                uri = resolveURI(uri);
                try {
                    final URLConnection uc = new URL(uri).openConnection();

                    // If using Java 5+ you can set timeouts for the URL connection--useful if the remote
                    // server is down etc.; the default timeout is pretty long
                    //
                    //uc.setConnectTimeout(10 * 1000);
                    //uc.setReadTimeout(30 * 1000);
                    //
                    // TODO:CLEAN-JDK1.4
                    // Since we target 1.4, we use a couple of system properties--note these are only supported
                    // in the Sun JDK implementation--see the Net properties guide in the JDK
                    // e.g. file:///usr/java/j2sdk1.4.2_17/docs/guide/net/properties.html
                    System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(10 * 1000));
                    System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(30 * 1000));

                    uc.connect();

                    is = uc.getInputStream();
                } catch (java.net.MalformedURLException e) {
                    XRLog.exception("bad URL given: " + uri, e);
                } catch (java.io.FileNotFoundException e) {
                    XRLog.exception("item at URI " + uri + " not found");
                } catch (java.io.IOException e) {
                    XRLog.exception("IO problem for " + uri, e);
                }
                return is;
            }
        };
        panel.getSharedContext().setUserAgentCallback(uac);
    }

    private void setupDocumentListener(final XHTMLPanel panel) {
        panel.addDocumentListener(new DefaultDocumentListener() {
            public void documentStarted() {
                panel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                super.documentStarted();
            }

            public void documentLoaded() {
                panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                frame.setTitle(panel.getDocumentTitle());
            }

            public void onLayoutException(Throwable t) {
                panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                panel.setDocument(getErrorDocument("can't layout: " + t.getMessage()).getDocument());
            }

            public void onRenderException(Throwable t) {
                panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                panel.setDocument(getErrorDocument("can't render: " + t.getMessage()).getDocument());
            }
        });
    }

    private XMLResource getErrorDocument(String reason) {
        XMLResource xr;
        String cleanUri = GeneralUtil.escapeHTML(uri);
        String notFound = "<html><h1>Document not found</h1><p>Could not load URI <pre>" + cleanUri + "</pre>, because: " + reason + "</p></html>";
        xr = XMLResource.load(new StringReader(notFound));
        return xr;
    }


    private void launchLoad() {
        new Thread(new Runnable() {
            public void run() {
                final Document doc;
                try {
                    if (panel != null ) panel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    doc = getUAC().getXMLResource(uri).getDocument();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Can't load document");
                    return;
                } finally {
                    if (panel != null ) panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        startRender(doc);
                    }
                });

            }
        }).start();
    }

    private UserAgentCallback getUAC() {
        return uac;
    }

    private void startRender(final Document document) {
        // first, load the document, so we can trap any parse errors
        // in loading;

        // Set the XHTML document to render. We use the simplest form
        // of the API call, which uses a File reference. There
        // are a variety of overloads for setDocument().
        try {
            panel.setDocument(document, uri);
        }
        catch (Exception e) {
            e.printStackTrace();  
        }
    }

    private void loadAndCheckArgs(String[] args) {
        if (args.length == 0) {
            System.out.println("Enter a file or URI.");
            System.exit(-1);
        }
        String name = args[0];
        if (!new File(name).exists()) {
            try {
                URL url = new URL(name);
            } catch (MalformedURLException e) {
                System.out.println("File " + name + " does not exist or is not a URI");
                System.exit(-1);
            }
        }
        this.uri = name;
    }
}