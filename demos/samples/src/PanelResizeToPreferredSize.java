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


import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * This sample shows how to size a panel based on the content of a document, when using a layout manager
 * that relies on preferred size. The class was attached as part of an email discussion:
 * <p/>
 * From the email thread: "The panel doesn't get a preferred size until layout is called (which,
 * in turn, happens the first time it's painted).  This means you're not
 * going to get the results you want if your XHTMLPanel is managed by a
 * layout manager that relies on preferred size (e.g. FlowLayout).
 * Assuming you want to lay the document out into some width, you'll want
 * to use a layout manager that doesn't rely (entirely) on preferred
 * size.
 * <p/>
 * If you need the entirely unconstrained document width, you could
 * temporarily boost the size of the XHTMLPanel to something very large
 * and then call panel.doLayout() and then read back the panel's
 * preferred size.  Needless to say, this all needs to happen on the EDT."
 * and: "So basically you want to:
 * 1) create the frame and add components to it
 * 2) call f.pack() to make them displayable
 * 3) layout documents in XHTMLPanel instances
 * 4) call f.pack() again now that the preferred size of the XHTMLPanel
 * instances has been calculated"
 */
public class PanelResizeToPreferredSize {
    public static void main(String[] args) throws Exception {
        final JFrame f = new JFrame("XHTMLPanel");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        Container content = f.getContentPane();
        content.setBackground(Color.white);

        // Note: we're using a layout manager that depends on the preferred size of its components.
        // That's the critical point of this sample code.
        content.setLayout(new FlowLayout());

        content.add(new JButton("Button 1"));

        final XHTMLPanel panel = new XHTMLPanel();
        content.add(panel);

        content.add(new JButton("Button 2"));
        content.add(new JButton("Button 3"));

        // Calling pack() will cause the frame and its components to be displayable (see javadoc for
        // Window.pack(); this is critical for the document layout routine, below, to have any effect
        // Note the frame doesn't need to be visible; e.g., we don't need to show the frame, layout the document,
        // then resize the window; it just needs to be displayable.
        f.pack();

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                // Note that our document doesn't have any width constraints, but that there is very little text
                // involved, so the document width (calculated from the content) is something reasonable for this demo.
                panel.setDocumentFromString(
                        "<html style='position: absolute; background-color: red;'>" +
                                "    Several words that won't wrap" +
                                "</html>",
                        null,
                        new XhtmlNamespaceHandler());

                Dimension savedSize = panel.getSize();

                // Set the panel size to some artificially large size; the render engine will actually calculate
                // the actual (preferred) size based on the document content. Setting it to a large size here prevents
                // the document from being artificially constrained by the panel's width.
                // If the document had very long runs of text, and no width constraints, we'd want to set the width
                // to some smaller, reasonable value (800, 1024, etc.). Without that, in the absence of any other
                // information, the render engine would lay out the runs of text to as wide a space as needed to
                // accomodate the text without breaks.
                panel.setSize(10000, 10000);

                // now we lay the document out again
                panel.doDocumentLayout(panel.getGraphics());

                // and reset the size
                panel.setSize(savedSize);

                f.pack();
                f.setVisible(true);
            }
        });
    }
}
