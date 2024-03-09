/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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
package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;

import static java.awt.event.KeyEvent.VK_F5;


public class HTMLTest extends JFrame {
    private final XHTMLPanel panel;
    private static final String BASE_TITLE = "Flying Saucer";

    public HTMLTest(String[] args) {
        super(BASE_TITLE);
        panel = new XHTMLPanel();
        int width = 360;
        int height = 500;
        panel.setPreferredSize(new Dimension(width, height));
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(width, height));

        panel.addMouseTrackingListener(new LinkListener());

        if (args.length > 0) {
            loadDocument(args[0]);
        }

        getContentPane().add("Center", scroll);

        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");
        mb.add(file);
        file.setMnemonic('F');
        file.add(new QuitAction());

        JMenu view = new JMenu("View");
        mb.add(view);
        view.setMnemonic('V');
        view.add(new RefreshPageAction());
        view.add(new ReloadPageAction());


        JMenu debug = new JMenu("Debug");
        mb.add(debug);
        debug.setMnemonic('D');

        JMenu debugShow = new JMenu("Show");
        debug.add(debugShow);
        debugShow.setMnemonic('S');

        debugShow.add(new JCheckBoxMenuItem(new BoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new LineBoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new InlineBoxesAction()));
        debugShow.add(new JCheckBoxMenuItem(new FontMetricsAction()));


        JMenu anti = new JMenu("Anti Aliasing");
        anti.add(new JCheckBoxMenuItem(new AntiAliasedAction("None", -1)));
        anti.add(new JCheckBoxMenuItem(new AntiAliasedAction("Low (Default)", 25)));
        anti.add(new JCheckBoxMenuItem(new AntiAliasedAction("Medium", 12)));
        anti.add(new JCheckBoxMenuItem(new AntiAliasedAction("Highest", 0)));
        debug.add(anti);

        debug.add(new ShowDOMInspectorAction());
/*
        debug.add(
                    new AbstractAction( "Print Box Tree" ) {
                        public void actionPerformed( ActionEvent evt ) {
                            panel.printTree();
                        }
                    } );
*/
        setJMenuBar(mb);
    }

    /**
     * Adds a feature to the FileLoadAction attribute of the HTMLTest object
     *
     * @param menu    The feature to be added to the FileLoadAction attribute
     * @param display The feature to be added to the FileLoadAction attribute
     * @param file    The feature to be added to the FileLoadAction attribute
     */
    public void addFileLoadAction(JMenu menu, String display, final String file) {
        menu.add(new AbstractAction(display) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent evt) {
                loadDocument(file);
            }
        });
    }

    /**
     * @param uri taken to be a file, if not beginning with http://
     */
    private void loadDocument(final String uri) {
        SwingUtilities.invokeLater(() -> {
            try {
                long st = System.currentTimeMillis();

                final URL url;
                if (uri.startsWith("http://"))
                    url = new URL(uri);
                else
                    url = new File(uri).toURI().toURL();

                System.err.println("loading " + url + "!");
                panel.setDocument(url.toExternalForm());

                long el = System.currentTimeMillis() - st;
                XRLog.general("loadDocument(" + url + ") in " + el + "ms, render may take longer");
                setTitle(String.format("%s-  %s  (%s)", BASE_TITLE, panel.getDocumentTitle(), url));
            } catch (Exception ex) {
                Uu.p(ex);
            }
            panel.repaint();
        });
    }

    /**
     * The main program for the HTMLTest class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        final JFrame frame = new HTMLTest(args);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        //frame.setSize( text_width, 300 );
        frame.setVisible(true);
    }

    static final class QuitAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        QuitAction() {
            super("Quit");
            putValue(MNEMONIC_KEY, KeyEvent.VK_Q);
        }

        @Override
        public void actionPerformed(final ActionEvent evt) {
            System.exit(0);
        }
    }

    final class BoxOutlinesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        BoxOutlinesAction() {
            super("Show Box Outlines");
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            panel.getSharedContext().setDebug_draw_boxes(!panel.getSharedContext().debugDrawBoxes());
            panel.repaint();
        }
    }

    final class LineBoxOutlinesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        LineBoxOutlinesAction() {
            super("Show Line Box Outlines");
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            panel.getSharedContext().setDebug_draw_line_boxes(!panel.getSharedContext().debugDrawLineBoxes());
            panel.repaint();
        }
    }

    final class InlineBoxesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        InlineBoxesAction() {
            super("Show Inline Boxes");
            putValue(MNEMONIC_KEY, KeyEvent.VK_I);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            panel.getSharedContext().setDebug_draw_inline_boxes(!panel.getSharedContext().debugDrawInlineBoxes());
            panel.repaint();
        }
    }

    final class FontMetricsAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        FontMetricsAction() {
            super("Show Font Metrics");
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            panel.getSharedContext().setDebug_draw_font_metrics(!panel.getSharedContext().debugDrawFontMetrics());
            panel.repaint();
        }
    }

    class AntiAliasedAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        private final int fontSizeThreshold;

        AntiAliasedAction(String text, int fontSizeThreshold) {
            super(text);
            this.fontSizeThreshold = fontSizeThreshold;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            panel.getSharedContext().getTextRenderer().setSmoothingThreshold(fontSizeThreshold);
            panel.repaint();
        }
    }

    final class ShowDOMInspectorAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        private DOMInspector inspector;
        private JFrame inspectorFrame;

        ShowDOMInspectorAction() {
            super("DOM Tree Inspector");
            putValue(MNEMONIC_KEY, KeyEvent.VK_D);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (inspectorFrame == null) {
                inspectorFrame = new JFrame("DOM Tree Inspector");
            }
            if (inspector == null) {
                // inspectorFrame = new JFrame("DOM Tree Inspector");

                inspector = new DOMInspector(panel.doc, panel.getSharedContext(), panel.getSharedContext().getCss());

                inspectorFrame.getContentPane().add(inspector);

                inspectorFrame.pack();
                inspectorFrame.setSize(400, 600);
                inspectorFrame.setVisible(true);
            } else {
                inspector.setForDocument(panel.doc, panel.getSharedContext(), panel.getSharedContext().getCss());
            }
            inspectorFrame.setVisible(true);
        }
    }

    static final class RefreshPageAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        RefreshPageAction() {
            super("Refresh Page");
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("F5"));
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            // TODO
            System.out.println("Refresh Page triggered");
        }
    }

    static final class ReloadPageAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        ReloadPageAction() {
            super("Reload Page");
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(VK_F5, InputEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            // TODO
            System.out.println("Reload Page triggered");
        }
    }
}
