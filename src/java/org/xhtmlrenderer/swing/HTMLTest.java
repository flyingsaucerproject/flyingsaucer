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

package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.swing.DOMInspector;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.xhtmlrenderer.util.x;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.util.XRLog;
import org.w3c.dom.*;
import java.io.File;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.css.*;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.css.bridge.XRStyleReference;

public class HTMLTest extends JFrame {
    public static final int text_width = 600;
    private static final String BASE_TITLE = "Flying Saucer";
    private final HTMLPanel panel = new HTMLPanel();

    public HTMLTest(String[] args) throws Exception {
        super(BASE_TITLE);
        panel.setPreferredSize(new Dimension(text_width,text_width));
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(scroll.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(scroll.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(text_width,text_width));
        panel.addMouseListener(new ClickMouseListener(panel));

        if(args.length > 0) {
            // CLEAN
            // File file = new File(args[0]);
            // panel.setDocument(x.loadDocument(args[0]),file.toURL());
            loadDocument(args[0]);
        }

        getContentPane().add("Center",scroll);


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

        JMenu test = new JMenu("Test");
        mb.add(test);
        test.setMnemonic('T');

        String demoRootDir = "demos/browser/xhtml";
        addFileLoadAction(test, "One Liner", demoRootDir + "/one-line.xhtml");
        addFileLoadAction(test, "Background Colors/Images", demoRootDir + "/background.xhtml");
        addFileLoadAction(test, "Borders", demoRootDir + "/border.xhtml");
        addFileLoadAction(test, "Box Sizing", demoRootDir + "/box-sizing.xhtml");
        addFileLoadAction(test, "Mixed Test (1)", demoRootDir + "/content.xhtml");
        addFileLoadAction(test, "Line Breaking", demoRootDir + "/breaking.xhtml");
        addFileLoadAction(test, "Headers", demoRootDir + "/header.xhtml");
        addFileLoadAction(test, "Inline Image", demoRootDir + "/image.xhtml");
        addFileLoadAction(test, "List ", demoRootDir + "/list.xhtml");
        addFileLoadAction(test, "Nesting", demoRootDir + "/nested.xhtml");
        addFileLoadAction(test, "General Styled Text", demoRootDir + "/paragraph.xhtml");
        addFileLoadAction(test, "CSS Selectors", demoRootDir + "/selectors.xhtml");
        addFileLoadAction(test, "Table", demoRootDir + "/table.xhtml");
        addFileLoadAction(test, "Text Alignment", demoRootDir + "/text-alignment.xhtml");
        addFileLoadAction(test, "Whitespace Handling", demoRootDir + "/whitespace.xhtml");
        addFileLoadAction(test, "iTunes Email", demoRootDir + "/itunes/itunes1.xhtml");
        addFileLoadAction(test, "Follow Links", demoRootDir + "/link.xhtml");
        addFileLoadAction(test, "Hamlet (slow!)", demoRootDir + "/hamlet.xhtml");
        addFileLoadAction(test, "extended", demoRootDir + "/extended.xhtml");
        addFileLoadAction(test, "XML-like", demoRootDir + "/xml.xhtml");
        addFileLoadAction(test, "XML", demoRootDir + "/xml.xml");

        JMenu debug = new JMenu("Debug");
        mb.add(debug);
        debug.setMnemonic('D');

        JMenu debugShow = new JMenu("Show");
        debug.add(debugShow);
        debugShow.setMnemonic('S');

        debugShow.add(new JCheckBoxMenuItem(new BoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new LineBoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new InlineBoxesAction()));

        debug.add(new ShowDOMInspectorAction());

        debug.add(new AbstractAction("Print Box Tree") {
            public void actionPerformed(ActionEvent evt) {
                panel.printTree();
            }
        });

        setJMenuBar(mb);
    }

    public void addFileLoadAction(JMenu menu, String display, final String file) {
        menu.add(new AbstractAction(display) {
            public void actionPerformed(ActionEvent evt) {
                loadDocument(file);
            }
        });
    }

    class QuitAction extends AbstractAction {
        QuitAction() {
            super("Quit");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
        }
        public void actionPerformed(ActionEvent evt) { System.exit(0); }
    }

    class BoxOutlinesAction extends AbstractAction {
        BoxOutlinesAction() {
            super("Show Box Outlines");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
        }
        public void actionPerformed(ActionEvent evt) {
            panel.c.debug_draw_boxes = !panel.c.debug_draw_boxes;
            panel.repaint();
        }
    }

    class LineBoxOutlinesAction extends AbstractAction {
        LineBoxOutlinesAction() {
            super("Show Line Box Outlines");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
        }
        public void actionPerformed(ActionEvent evt) {
            panel.c.debug_draw_line_boxes = !panel.c.debug_draw_line_boxes;
            panel.repaint();
        }
    }

    class InlineBoxesAction extends AbstractAction {
        InlineBoxesAction() {
            super("Show Inline Boxes");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
        }
        public void actionPerformed(ActionEvent evt) {
            panel.c.debug_draw_inline_boxes = !panel.c.debug_draw_inline_boxes;
            panel.repaint();
        }
    }

    class ShowDOMInspectorAction extends AbstractAction {
        private DOMInspector inspector;
        private JFrame inspectorFrame;
        ShowDOMInspectorAction() {
            super("DOM Tree Inspector");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
        }

        public void actionPerformed(ActionEvent evt) {
            if ( inspectorFrame == null ) {
                inspectorFrame = new JFrame("DOM Tree Inspector");
            }
            if ( inspector == null ) {
                // inspectorFrame = new JFrame("DOM Tree Inspector");

                // CLEAN: this is more complicated than it needs to be
                // DOM Tree Inspector needs to work with either CSSBank
                // or XRStyleReference--implementations are not perfectly
                // so we have different constructors
                if ( panel.c.css instanceof CSSBank )
                    inspector = new DOMInspector(panel.doc);
                else
                    inspector = new DOMInspector(panel.doc, panel.c, (XRStyleReference)panel.c.css);

                inspectorFrame.getContentPane().add(inspector);

                inspectorFrame.pack();
                inspectorFrame.setSize(text_width,600);
                inspectorFrame.show();
            } else {
                if ( panel.c.css instanceof CSSBank )
                    inspector.setForDocument(panel.doc);
                else
                    inspector.setForDocument(panel.doc, panel.c, (XRStyleReference)panel.c.css);
            }
            inspectorFrame.show();
        }
    }

    class RefreshPageAction extends AbstractAction {
        RefreshPageAction() {
            super("Refresh Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke("F5"));
        }

        public void actionPerformed(ActionEvent evt) {
            // TODO
            System.out.println("Refresh Page triggered");
        }
    }

    class ReloadPageAction extends AbstractAction {
        ReloadPageAction() {
            super("Reload Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_F5,
                                            ActionEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent evt) {
            // TODO
            System.out.println("Reload Page triggered");
        }
    }

    private void loadDocument(final String file) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    long st = System.currentTimeMillis();

                    panel.setDocument(file);

                    long el = System.currentTimeMillis() - st;
                    XRLog.general("loadDocument(" + file + ") in " + el + "ms, render may take longer");
                    HTMLTest.this.setTitle(BASE_TITLE + "-  " +
                                  panel.getDocumentTitle() + "  " +
                                  "(" + file + ")");
                } catch (Exception ex) {
                    u.p(ex);
                }
                panel.repaint();
            }
        });
    }

    public static void main(String[] args) throws Exception {

        final JFrame frame = new HTMLTest(args);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(text_width,300);
        frame.show();
        /*
        new Thread(new Runnable() {
            public void run() {
                for(int i=0; i<100; i=i+1) {
                    u.sleep(100);
                    frame.resize(131-i,200);
                    System.out.println("blah = " + (131-i));
                    frame.validate();
                    frame.invalidate();
                    frame.repaint();
                }
            }
        }).start();
        */
    }
}

class ClickMouseListener extends MouseAdapter {
    HTMLPanel panel;

    public ClickMouseListener(HTMLPanel panel) {
        this.panel = panel;
    }

    public void mousePressed(MouseEvent evt) {
        Box box = panel.findBox(evt.getX(),evt.getY());
        if(box == null) return;
        u.p("pressed " + box);
        if(box.node != null) {
            Node node = box.node;
            if(node.getNodeType() == node.TEXT_NODE) {
                node = node.getParentNode();
            }

            if(node.getNodeName().equals("a")) {
                u.p("clicked on a link");
                box.clicked = true;
                box.color = new Color(255,255,0);
                panel.repaint();
            }

        }
    }
    public void mouseReleased(MouseEvent evt) {
        Box box = panel.findBox(evt.getX(),evt.getY());
        if(box == null) return;
        u.p("pressed " + box);
        if(box.node != null) {
            Node node = box.node;
            if(node.getNodeType() == node.TEXT_NODE) {
                node = node.getParentNode();
            }

            if(node.getNodeName().equals("a")) {
                u.p("clicked on a link");
                box.clicked = true;
                box.color = new Color(255,0,0);
                panel.repaint();
                followLink((Element)node);
            }

        }
    }

    private void followLink(final Element elem) {
        try {
            if(elem.hasAttribute("href")) {
                panel.setDocumentRelative(elem.getAttribute("href"));
            }
        } catch (Exception ex) {
            u.p(ex);
        }
    }

}







