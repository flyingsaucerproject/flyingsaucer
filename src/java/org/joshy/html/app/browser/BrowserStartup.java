package org.joshy.html.app.browser;

import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.joshy.html.*;
import org.joshy.html.swing.*;
import org.joshy.u;
import org.joshy.x;
import java.util.logging.*;

public class BrowserStartup {
    public static Logger logger = Logger.getLogger("app.browser");
    BrowserPanel panel;
    BrowserMenuBar menu;
        
    public BrowserStartup() {
        logger.info("starting up");
    }
    
    public void init() {
        logger.info("creating UI");
        panel = new BrowserPanel();
        panel.init();
        panel.createLayout();
        panel.createActions();

        menu = new BrowserMenuBar(this);
        menu.init();
        menu.createLayout();
        menu.createActions();
    }
    
    public static void main(String[] args) {
        BrowserStartup bs = new BrowserStartup();
        bs.init();
        JFrame frame = new JFrame();
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
    JMenu debug;
    BrowserStartup root;
    
    public BrowserMenuBar(BrowserStartup root) {
        this.root = root;
    }
    
    public void init() {
        file = new JMenu("File");
        open_file = new JMenuItem("Open File...");
        quit = new JMenuItem("Quit");
        debug = new JMenu("Debug");
    }
    
    public void createLayout() {
        file.add(open_file);
        file.add(quit);
        add(file);
        
        
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
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                System.exit(0);
            }
        });
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
    public void reloadPage() throws Exception {
        logger.info("Reloading Page: ");
        if(current_url != null) {
            loadPage(current_url);
        }
    }
    
    public void loadPage(String url_text) throws Exception {
        logger.info("Loading Page: " + url_text);
        current_url = url_text;
        if(url_text.startsWith("http")) {
            view.setDocument(new URL(url.getText()));
        } else {
            view.setDocument(url_text);
        }
        setStatus("Successfully loaded: " + url_text);
    }
    
    public void setStatus(String txt) {
        status.setText(txt);
    }
        
    public void createActions() {
        
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

    }
    
}











