package org.joshy.html.app.browser;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.joshy.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.logging.*;
import java.io.*;
import org.joshy.html.*;
import java.net.*;

public class BrowserPanel extends JPanel {
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
        forward = new JButton();
        backward = new JButton("Back");
        stop = new JButton("Stop");
        reload = new JButton("Reload");
        url = new JTextField();
        view = new HTMLPanel();
        view.setErrorHandler(root.error_handler);
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
    
    public void createActions() {
        backward.setAction(root.actions.backward);
        forward.setAction(root.actions.forward);
        reload.setAction(root.actions.reload);
        url.setAction(root.actions.load);
        updateButtons();
    }

    
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
        
    
    protected void updateButtons() {
       if(root.history.hasPrevious()) {
           root.actions.backward.setEnabled(true);
       } else {
           root.actions.backward.setEnabled(false);
       }
       if(root.history.hasNext()) {
           root.actions.forward.setEnabled(true);
       } else {
           root.actions.forward.setEnabled(false);
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
        fact.setValidating(true);
        DocumentBuilder builder = fact.newDocumentBuilder();
        builder.setErrorHandler(root.error_handler);/*  new ErrorHandler() {
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
        */
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
        } else if(url_text.startsWith("file://")) {
            File file = new File(new URI(url_text));
            if(file.isDirectory()) {
                doc = new DirectoryLister().list(file);
                ref = file.toURL();
            } else {
                doc = builder.parse(file);
                ref = file.toURL();
            }
            
        } else {
            doc = builder.parse(url_text);
            ref = new File(url_text).toURL();
        }
        loadPage(doc,ref);
        
        setStatus("Successfully loaded: " + url_text);
    }
}

