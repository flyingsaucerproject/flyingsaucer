/*
 * {{{ header & license
 * Copyright (c) 2008 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
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
 * }}}
 */


import org.xhtmlrenderer.event.DefaultDocumentListener;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.logging.Level;

/**
 * This example shows how to render a document as a Swing JPanel (XHTMLPanel, a subclass) and have the
 * render engine figure out the document size for you; the frame containing the panel is then sized to
 * the document size. To run this sample from the command line, make sure you have the Flying Saucer
 * JARs in the classpath; arguments to the program are a URL or file path, and optionally, a target width
 * for the document (width defaults to 800 otherwise):
 * java -jar core-renderer.jar JPanelSizeToDocument myHtml.html 1024
 *
 * @author Patrick Wright
 */
public class JPanelSizeToDocument {
    private String fileName;
    private int targetWidth = 800;

    public static void main(String[] args) throws Exception {
        new JPanelSizeToDocument().run(args);
    }

    private void run(String[] args) {
        loadAndCheckArgs(args);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                launchGUI();
            }
        });
    }

    private void launchGUI() {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a JPanel subclass to render the page
        final XHTMLPanel panel = new XHTMLPanel();
        final Dimension orgSize = panel.getSize();

        // we'll add a listener to note the preferred size as calculated by the render engine
        panel.addDocumentListener(new DefaultDocumentListener() {
            public void documentLoaded() {
                frame.setTitle("Flying Saucer: " + panel.getDocumentTitle());

                final Dimension dim = panel.getPreferredSize();
                XRLog.general("Preferred size" + dim);
            }
        });

        frame.getContentPane().add(panel);

        // Set the XHTML document to render. We use the simplest form
        // of the API call, which uses a File reference. There
        // are a variety of overloads for setDocument().
        try {
            panel.setDocument(new File(fileName));
        } catch (Exception e) {
            XRLog.general(Level.WARNING, "Could not load XHTML document " + fileName, e);
            messageAndExit("Failed to load document", -1);
        }

        // No-op as regards size, but has side-effect of making the window and components "displayable". This is
        // necessary for the doDocumentLayout() below to work correctly. Alternately you could setVisible(true), but
        // then the document would be visible before the resize was completed.
        frame.pack();

        // Here we set an artificially large size for the document to let the render engine
        // figure out what the document needs based on content.
        //
        // Note that if the document itself has no fixed width, text will not automatically break and will
        // likely run out and extend the document width as far as necessary to accomodate the longest line of text
        // you could either set a max width in a document box, or you can set it here.
        //
        // The document height will be calculated automatically based on content. We use an artificially large size
        // here to not constrain the layout algorithm.
        panel.setSize(targetWidth, 10000);
        panel.doDocumentLayout(panel.getGraphics());
        panel.setSize(orgSize);
        frame.pack();
        frame.setVisible(true);
    }

    private void loadAndCheckArgs(String[] args) {
        if (args.length == 0) {
            messageAndExit("Enter a file or URI.", -1);
        }
        String name = args[0];
        if (!new File(name).exists()) {
            messageAndExit("File " + name + " does not exist.", -1);
        }
        this.fileName = name;

        if (args.length > 1) {
            String widthVal = args[1];
            try {
                targetWidth = Integer.valueOf(widthVal).intValue();
            } catch (NumberFormatException e) {
                messageAndExit("Target width " + widthVal + " is not an integer", -1);
            }
        }
    }

    private void messageAndExit(final String msg, final int rtnCode) {
        System.out.println(msg);
        System.exit(rtnCode);
    }
}