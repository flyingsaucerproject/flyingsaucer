package org.joshy.html.app.browser;

import java.io.File;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.joshy.html.*;
import org.joshy.html.swing.*;
import org.joshy.u;
import org.joshy.x;
import java.util.logging.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class BrowserStartup {
    public static Logger logger = Logger.getLogger("app.browser");
    BrowserPanel panel;
    BrowserMenuBar menu;
    JFrame frame;
    protected HistoryManager history;
        
    public BrowserStartup() {
        logger.info("starting up");
        history = new HistoryManager();
    }
    
    public void init() {
        logger.info("creating UI");
        panel = new BrowserPanel(this);
        panel.init();
        panel.createLayout();
        panel.createActions();

        menu = new BrowserMenuBar(this);
        menu.init();
        menu.createLayout();
        menu.createActions();
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BrowserStartup bs = new BrowserStartup();
        bs.frame = frame;
        bs.init();
        frame.setJMenuBar(bs.menu);
        frame.getContentPane().add(bs.panel);
        frame.pack();
        frame.setSize(500,600);
        frame.show();
    }
    
}

class BrowserMenuBar extends JMenuBar {
    JMenu file;
    JMenuItem quit;
    JMenuItem open_file;
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
    }
    
    public void createLayout() {
        file.add(open_file);
        file.add(quit);
        add(file);
        
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
    }
    
    public static Logger logger = Logger.getLogger("app.browser");

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


class BrowserPanel extends JPanel {
    public static Logger logger = Logger.getLogger("app.browser");
    JButton forward;
    JButton backward;
    JButton stop;
    JButton reload;
    JTextField url;
    JLabel status;
    HTMLPanel view;
    JScrollPane scroll;
    BrowserStartup root;
    
    public BrowserPanel(BrowserStartup root) {
        this.root = root;
    }

    
    public void init() {
        forward = new JButton("Forward");
        backward = new JButton("Back");
        stop = new JButton("Stop");
        reload = new JButton("Reload");
        url = new JTextField();
        view = new HTMLPanel();
        status = new JLabel("Status");
        
        int text_width = 200;
        view.setPreferredSize(new Dimension(text_width,text_width));
        scroll = new JScrollPane(view);
        scroll.setVerticalScrollBarPolicy(scroll.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(scroll.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(text_width,text_width));
        view.setViewportComponent(scroll);
        view.setJScrollPane(scroll);
        
    }
    
    public void createLayout() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);
        
        c.gridx = 0; c.gridy = 0;
        c.weightx = c.weighty = 0.0;
        gbl.setConstraints(backward,c);
        add(backward);
        
        c.gridx++;
        gbl.setConstraints(forward,c);
        add(forward);
        
        c.gridx++;
        gbl.setConstraints(stop,c);
        add(stop);
        
        c.gridx++;
        gbl.setConstraints(reload,c);
        add(reload);
        
        c.gridx++;
        c.fill = c.HORIZONTAL;
        c.weightx = 10.0;
        gbl.setConstraints(url,c);
        add(url);
        
        c.gridx = 0;
        c.gridy++;
        c.fill = c.BOTH;
        c.gridwidth = 5;
        c.weightx = c.weighty = 10.0;
        gbl.setConstraints(scroll,c);
        add(scroll);
        
        c.gridx = 0;
        c.gridy++;
        c.fill = c.HORIZONTAL;
        c.weighty = 0.1;
        gbl.setConstraints(status,c);
        add(status);
        
    }
    
    String current_url = null;
    

    
    public void goForward() {
       root.history.goNext();
       view.setDocument(root.history.getCurrentDocument(),root.history.getCurrentURL());
       //root.history.dumpHistory();
       updateButtons();
    }
    
    public void goBack() throws Exception {
       root.history.goPrevious();
       view.setDocument(root.history.getCurrentDocument(),root.history.getCurrentURL());
       //root.history.dumpHistory();
       updateButtons();
    }
    
    public void reloadPage() throws Exception {
        logger.info("Reloading Page: ");
        if(current_url != null) {
            loadPage(current_url);
        }
    }
    
    
    public void setStatus(String txt) {
        status.setText(txt);
    }
        
    public void createActions() {
        backward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    goBack();
                    view.repaint();
                } catch (Exception ex) {
                    u.p(ex);
                }
            }
        });
        forward.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    goForward();
                    view.repaint();
                } catch (Exception ex) {
                    u.p(ex);
                }
            }
        });

        
        
        reload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    reloadPage();
                    view.repaint();
                } catch (Exception ex) {
                    u.p(ex);
                }
            }
        });
        
        url.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    String url_text = url.getText();
                    loadPage(url_text);
                    view.repaint();
                } catch (Exception ex) {
                    u.p(ex);
                }
            }
        });

        updateButtons();
    }
    
    protected void updateButtons() {
       if(root.history.hasPrevious()) {
           backward.setEnabled(true);
       } else {
           backward.setEnabled(false);
       }
       if(root.history.hasNext()) {
           forward.setEnabled(true);
       } else {
           forward.setEnabled(false);
       }
    }
    
    public void loadPage(Document doc, URL url) throws Exception {
        view.setDocument(doc,url);
        root.history.goNewDocument(doc);
        updateButtons();
    }
    public void loadPage(String url_text) throws Exception {
        logger.info("Loading Page: " + url_text);
        current_url = url_text;
        
        
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fact.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException ex) throws SAXException {
                logger.info("warning: " + ex);
            }
            public void error(SAXParseException ex) throws SAXException {
                logger.info("error: " + ex);
                logger.info("error on line: " + ex.getLineNumber() + " column: " + ex.getColumnNumber());
                setStatus("Error Loading Document");
            }
            public void fatalError(SAXParseException ex) throws SAXException {
                logger.info("fatal error: " + ex);
                logger.info("error on line: " + ex.getLineNumber() + " column: " + ex.getColumnNumber());
                setStatus("Error Loading Document");
            }
        });
        
        Document doc = null;
        
        
        URL ref = null;
        
        if(url_text.startsWith("demo:")) {
            DemoMarker marker = new DemoMarker();
            u.p("marker = " + marker);
            String short_url = url_text.substring(5);
            u.p("sub = " + short_url);
            if(!short_url.startsWith("/")) {
                short_url = "/" + short_url;
            }
            doc = builder.parse(marker.getClass().getResourceAsStream(short_url));
            ref = marker.getClass().getResource(short_url);
        } else if(url_text.startsWith("http")) {
            doc = builder.parse(url_text);
            ref = new File(url_text).toURL();
        } else {
            doc = builder.parse(url_text);
            ref = new File(url_text).toURL();
        }
        loadPage(doc,ref);
        
        setStatus("Successfully loaded: " + url_text);
    }
}














