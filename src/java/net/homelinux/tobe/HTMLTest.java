/*
 *
 * HTMLTest.java
 * Copyright (c) 2004 Joshua Marinacci, Patrick Wright, Torbjörn Gannholm
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
 *
 */

package net.homelinux.tobe;

import org.joshy.html.swing.DOMInspector;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.joshy.x;
import org.joshy.u;
//import org.w3c.dom.*;
//only for click-listener:
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.File;
import org.joshy.html.box.Box;

import net.homelinux.tobe.renderer.HTMLPanel;
import net.homelinux.tobe.renderer.UserAgentCallback;
import net.homelinux.tobe.renderer.Document;
import net.homelinux.tobe.renderer.XhtmlDocument;

public class HTMLTest extends JFrame implements UserAgentCallback {
    public static final int text_width = 600;
    private static final String BASE_TITLE = "Flying Saucer";
    private final HTMLPanel panel = new HTMLPanel(this);
    
    private java.net.URI _baseURI;
    private Document _doc;

    public HTMLTest(String[] args) throws Exception {
        super(BASE_TITLE);
        _baseURI = (new java.io.File(".")).toURI();
        panel.setPreferredSize(new Dimension(text_width,text_width));
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(scroll.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(scroll.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(text_width,text_width));
        panel.setViewportComponent(scroll);
        panel.setJScrollPane(scroll);
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

        addFileLoadAction(test, "One Liner", "demos/one-line.xhtml");
        addFileLoadAction(test, "Background Colors/Images", "demos/background.xhtml");
        addFileLoadAction(test, "Borders", "demos/border.xhtml");
        addFileLoadAction(test, "Box Sizing", "demos/box-sizing.xhtml");
        addFileLoadAction(test, "Mixed Test (1)", "demos/content.xhtml");
        addFileLoadAction(test, "Line Breaking", "demos/breaking.xhtml");
        addFileLoadAction(test, "Headers", "demos/header.xhtml");
        addFileLoadAction(test, "Inline Image", "demos/image.xhtml");
        addFileLoadAction(test, "List ", "demos/list.xhtml");
        addFileLoadAction(test, "Nesting", "demos/nested.xhtml");
        addFileLoadAction(test, "General Styled Text", "demos/paragraph.xhtml");
        addFileLoadAction(test, "CSS Selectors", "demos/selectors.xhtml");
        addFileLoadAction(test, "Table", "demos/table.xhtml");
        addFileLoadAction(test, "Text Alignment", "demos/text-alignment.xhtml");
        addFileLoadAction(test, "Whitespace Handling", "demos/whitespace.xhtml");
        addFileLoadAction(test, "iTunes Email", "demos/itunes/itunes1.xhtml");
        addFileLoadAction(test, "Follow Links", "demos/link.xhtml");
        addFileLoadAction(test, "Hamlet (slow!)", "demos/hamlet.xhtml");
        addFileLoadAction(test, "extended", "demos/extended.xhtml");
        addFileLoadAction(test, "XML-like", "demos/xml.xhtml");
        addFileLoadAction(test, "XML", "demos/xml.xml");
        addFileLoadAction(test, "Element styling", "demos/element-style.xhtml");

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
                /*if ( panel.c.css instanceof CSSBank )*/
                    inspector = new DOMInspector(panel.doc.getDomDocument());     
                /*else 
                    inspector = new DOMInspector(panel.doc, panel.c, (XRStyleReference)panel.c.css);
                */    
                inspectorFrame.getContentPane().add(inspector);
                    
                inspectorFrame.pack();
                inspectorFrame.setSize(text_width,600);
                inspectorFrame.show();
            } else {
                /*if ( panel.c.css instanceof CSSBank )
                    inspector.setForDocument(panel.doc);     
                else 
                    inspector.setForDocument(panel.doc, panel.c, (XRStyleReference)panel.c.css);
                 */
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
                    java.net.URI uri = new java.net.URI(file);
                    _doc = new XhtmlDocument(getInputStreamForURI(uri), uri);
                    
                    long el = System.currentTimeMillis() - st;
                    System.out.println("TIME: loadDocument(" + file + ")  " + el + "ms, render may take longer");
                    st = System.currentTimeMillis();

                    panel.setDocument(_doc);
                    
                    el = System.currentTimeMillis() - st;
                    System.out.println("TIME: setDocument(" + file + ")  " + el + "ms, render may take longer");
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
    
    public java.io.InputStream getInputStreamForURI(java.net.URI uri) {
        java.io.InputStream is = null;
        try {
            is = _baseURI.resolve(uri).toURL().openStream();
        }
        catch(java.net.MalformedURLException e) {
            
        }
        catch(java.io.IOException e) {
            
        }
        return is;
    }
    
    public boolean isVisited(java.net.URI uri) {
        return false;
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
                java.net.URI uri = new java.net.URI(elem.getAttribute("href"));
                uri = _doc.getURI().resolve(uri);
                java.io.InputStream is = getInputStreamForURI(uri);
                _doc = new XhtmlDocument(is, uri);
                panel.setDocument(_doc);
            }
        } catch (Exception ex) {
            u.p(ex);
        }
    }

}

}






