package org.joshy.html;

import org.joshy.html.swing.DOMInspector;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.joshy.x;
import org.joshy.u;
import org.w3c.dom.*;
import java.io.File;
import org.joshy.html.box.Box;

public class HTMLTest {
    public static final int text_width = 300;
    public static void main(String[] args) throws Exception {
        final JFrame frame = new JFrame("xhtml rendering test");
        final HTMLPanel panel = new HTMLPanel();
        panel.setPreferredSize(new Dimension(text_width,text_width));
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(scroll.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(scroll.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(text_width,text_width));
        panel.setViewportComponent(scroll);
        panel.setJScrollPane(scroll);
        panel.addMouseListener(new ClickMouseListener(panel));
        
        if(args.length > 0) {
            File file = new File(args[0]);
            panel.setDocument(x.loadDocument(args[0]),file.toURL());
        }

        frame.getContentPane().add("Center",scroll);
        
        
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");
        mb.add(file);
        file.add(new AbstractAction("Quit") {
            public void actionPerformed(ActionEvent evt) {
                System.exit(0);
            }
        });

        JMenu test = new JMenu("Test");
        mb.add(test);
        test.add(new AbstractAction("background colors and images") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/background.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.repaint();
            }            
        });
        test.add(new AbstractAction("borders") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument(x.loadDocument("demos/border.xhtml")); } catch (Exception ex) { u.p(ex); }
                panel.repaint();
            }
        });
        test.add(new AbstractAction("box sizing") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/box-sizing.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.repaint();
            }
        });
        test.add(new AbstractAction("mixed test 1") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/content.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.repaint();
            }
        });
        test.add(new AbstractAction("line breaking") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    panel.setDocument("demos/breaking.xhtml");
                } catch (Exception ex) {
                    u.p(ex);
                }
                panel.repaint();
            }
        });
        test.add(new AbstractAction("headers") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/header.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.repaint();
            }
        });
        test.add(new AbstractAction("inline image") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/image.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.repaint();
            }
        });
        test.add(new AbstractAction("list ") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/list.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.repaint();
            }
        });
        test.add(new AbstractAction("nesting") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/nested.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.repaint();
            }
        });
        test.add(new AbstractAction("general styled text") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/paragraph.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.doLayout();
            }            
        });
        test.add(new AbstractAction("CSS selectors") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/selectors.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.doLayout();
                //panel.doLayout();
            }            
        });
        test.add(new AbstractAction("table") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/table.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.doLayout();
            }            
        });
        test.add(new AbstractAction("text alignment") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/text-alignment.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.doLayout();
            }            
        });
        test.add(new AbstractAction("whitespace handling") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/whitespace.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.doLayout();
            }            
        });
        test.add(new AbstractAction("itunes email") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/itunes/itunes1.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.doLayout();
            }            
        });
        test.add(new AbstractAction("follow links") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/link.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.doLayout();
            }            
        });
        test.add(new AbstractAction("Hamlet (slow!)") {
            public void actionPerformed(ActionEvent evt) {
                try { panel.setDocument("demos/hamlet.xhtml"); } catch (Exception ex) { u.p(ex); }
                panel.doLayout();
            }            
        });



        JMenu debug = new JMenu("Debug");
        mb.add(debug);
        debug.add(new AbstractAction("draw boxes") {
            public void actionPerformed(ActionEvent evt) {
                panel.c.debug_draw_boxes = !panel.c.debug_draw_boxes;
                panel.repaint();
            }
        });
        debug.add(new AbstractAction("draw line boxes") {
            public void actionPerformed(ActionEvent evt) {
                panel.c.debug_draw_line_boxes = !panel.c.debug_draw_line_boxes;
                panel.repaint();
            }
        });
        debug.add(new AbstractAction("draw inline boxes") {
            public void actionPerformed(ActionEvent evt) {
                panel.c.debug_draw_inline_boxes = !panel.c.debug_draw_inline_boxes;
                panel.repaint();
            }
        });
        debug.add(new AbstractAction("DOM tree inspector") {
            public void actionPerformed(ActionEvent evt) {
                JFrame frame = new JFrame();
                frame.getContentPane().add(new DOMInspector(panel.doc));
                frame.pack();
                frame.setSize(text_width,600);
                frame.show();
            }
        });


        frame.setJMenuBar(mb);
        
        
        
        
        frame.pack();
        //frame.setSize(text_width,300);
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







