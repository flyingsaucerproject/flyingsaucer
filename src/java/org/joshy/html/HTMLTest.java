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

public class HTMLTest extends JFrame {
    public static final int text_width = 600;
    private final HTMLPanel panel = new HTMLPanel();

    public HTMLTest(String[] args) throws Exception {
        super("xhtml rendering test");
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

        getContentPane().add("Center",scroll);


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

        addFileLoadAction(test, "one liner", "demos/one-line.xhtml");
        addFileLoadAction(test, "background colors and images", "demos/background.xhtml");
        addFileLoadAction(test, "borders", "demos/border.xhtml");
        addFileLoadAction(test, "box sizing", "demos/box-sizing.xhtml");
        addFileLoadAction(test, "mixed test 1", "demos/content.xhtml");
        addFileLoadAction(test, "line breaking", "demos/breaking.xhtml");
        addFileLoadAction(test, "headers", "demos/header.xhtml");
        addFileLoadAction(test, "inline image", "demos/image.xhtml");
        addFileLoadAction(test, "list ", "demos/list.xhtml");
        addFileLoadAction(test, "nesting", "demos/nested.xhtml");
        addFileLoadAction(test, "general styled text", "demos/paragraph.xhtml");
        addFileLoadAction(test, "CSS selectors", "demos/selectors.xhtml");
        addFileLoadAction(test, "table", "demos/table.xhtml");
        addFileLoadAction(test, "text alignment", "demos/text-alignment.xhtml");
        addFileLoadAction(test, "whitespace handling", "demos/whitespace.xhtml");
        addFileLoadAction(test, "itunes email", "demos/itunes/itunes1.xhtml");
        addFileLoadAction(test, "follow links", "demos/link.xhtml");
        addFileLoadAction(test, "Hamlet (slow!)", "demos/hamlet.xhtml");


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
                try {
                    long st = System.currentTimeMillis();
                    panel.setDocument(file);
                    long el = System.currentTimeMillis() - st;
                    System.out.println("TIME: setDocument(" + file + ")  " + el + "ms, render may take longer");
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







