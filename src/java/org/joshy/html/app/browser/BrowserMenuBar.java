package org.joshy.html.app.browser;

import org.joshy.u;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.logging.*;
import org.joshy.html.*;
import org.joshy.html.swing.*;
import java.io.File;

public class BrowserMenuBar extends JMenuBar {
    public static Logger logger = Logger.getLogger("app.browser");
    
    JMenu file;
    JMenuItem quit;
    JMenuItem open_file;
    JMenu edit;
    JMenuItem copy;
    JMenu view;
    JMenuItem view_source;
    JMenu debug;
    JMenu demos;
    BrowserStartup root;
    
    public BrowserMenuBar(BrowserStartup root) {
        this.root = root;
    }
    
    public void init() {
        file = new JMenu("File");
        open_file = new JMenuItem("Open File...");
        quit = new JMenuItem("Quit");
        debug = new JMenu("Debug");
        demos = new JMenu("Demos");
        view = new JMenu("View");
        view_source = new JMenuItem("Page Source");
        edit = new JMenu("Edit");
        copy = new JMenuItem("Copy");
        
    }
    
    public void createLayout() {
        file.add(open_file);
        file.add(quit);
        add(file);
        
        edit.add(copy);
        add(edit);
        
        view.add(view_source);
        add(view);
        
        demos.add(new LoadAction("Borders","demo:demos/border.xhtml"));
        demos.add(new LoadAction("Backgrounds","demo:demos/background.xhtml"));
        demos.add(new LoadAction("Paragraph","demo:demos/paragraph.xhtml"));
        demos.add(new LoadAction("Line Breaking","demo:demos/breaking.xhtml"));
        demos.add(new LoadAction("Forms","demo:demos/forms.xhtml"));
        demos.add(new LoadAction("Headers","demo:demos/header.xhtml"));
        demos.add(new LoadAction("Nested Divs","demo:demos/nested.xhtml"));
        demos.add(new LoadAction("Selectors","demo:demos/selectors.xhtml"));
        demos.add(new LoadAction("Images","demo:demos/image.xhtml"));
        demos.add(new LoadAction("Lists","demo:demos/list.xhtml"));
        try {
            demos.add(new LoadAction("File Listing (Win)","file:///c:"));
            demos.add(new LoadAction("File Listing (Unix)","file:///"));
        } catch (Exception ex) {
            u.p(ex);
        }
            
        add(demos);
        
        debug.add(new AbstractAction("draw boxes") {
            public void actionPerformed(ActionEvent evt) {
                root.panel.view.c.debug_draw_boxes = !root.panel.view.c.debug_draw_boxes;
                root.panel.view.repaint();
            }
        });
        
        debug.add(new AbstractAction("draw line boxes") {
            public void actionPerformed(ActionEvent evt) {
                root.panel.view.c.debug_draw_line_boxes = !root.panel.view.c.debug_draw_line_boxes;
                root.panel.view.repaint();
            }
        });
        
        debug.add(new AbstractAction("draw inline boxes") {
            public void actionPerformed(ActionEvent evt) {
                root.panel.view.c.debug_draw_inline_boxes = !root.panel.view.c.debug_draw_inline_boxes;
                root.panel.view.repaint();
            }
        });
        
        debug.add(new AbstractAction("DOM tree inspector") {
            public void actionPerformed(ActionEvent evt) {
                JFrame frame = new JFrame();
                frame.getContentPane().add(new DOMInspector(root.panel.view.doc));
                frame.pack();
                frame.setSize(250,500);
                frame.show();
            }
        });
        debug.add(new AbstractAction("Validation Console") {
            public void actionPerformed(ActionEvent evt) {
                if(root.validation_console == null) {
                    root.validation_console = new JFrame("Validation Console");
                    JFrame frame = root.validation_console;
                    JTextArea jta = new JTextArea();
                    root.error_handler.setTextArea(jta);
                    
                    frame.getContentPane().setLayout(new BorderLayout());
                    frame.getContentPane().add(new JScrollPane(jta),"Center");
                    JButton close = new JButton("Close");
                    frame.getContentPane().add(close,"South");
                    close.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            root.validation_console.setVisible(false);
                        }
                    });
                    
                    
                    frame.pack();
                    frame.setSize(200,400);
                }
                root.validation_console.setVisible(true);
            }
        });
        
        
        add(debug);
    }
    
    public void createActions() {
        open_file.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    FileDialog fd = new FileDialog(root.frame,"Open a local file",FileDialog.LOAD);
                    fd.show();
                    String file = fd.getDirectory() + fd.getFile();
                    root.panel.loadPage(file);
                } catch (Exception ex) {
                    logger.info("error:" + ex);
                }
            }
        });
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                System.exit(0);
            }
        });
        SelectionMouseListener ma = new SelectionMouseListener();
        root.panel.view.addMouseListener(ma);
        root.panel.view.addMouseMotionListener(ma);
        logger.info("added a mouse motion listener: " + ma);
    }
    
    class LoadAction extends AbstractAction {
        protected String url;
        public LoadAction(String name, String url) {
            super(name);
            this.url = url;
        }
        
        public void actionPerformed(ActionEvent evt) {
            try {
                root.panel.loadPage(url);
            } catch (Exception ex) { 
                u.p(ex); 
            }
        }
        
    }

}

