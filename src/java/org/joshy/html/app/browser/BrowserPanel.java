package org.joshy.html.app.browser;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
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
import org.joshy.html.event.DocumentListener;

public class BrowserPanel extends JPanel implements DocumentListener {
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
    BrowserPanelListener listener;
    
    public BrowserPanel(BrowserStartup root, BrowserPanelListener listener) {
        this.root = root;
        this.listener = listener;
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
        scroll.getVerticalScrollBar().setBlockIncrement(100);
        scroll.getVerticalScrollBar().setUnitIncrement(15);
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
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
            put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,0),"pagedown");
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
            put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,0),"pageup");
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
            put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0),"down");
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
            put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0),"up");

        view.getActionMap().put("pagedown",new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                JScrollBar sb = scroll.getVerticalScrollBar();
                sb.getModel().setValue(sb.getModel().getValue()+sb.getBlockIncrement());
            }
        });
        view.getActionMap().put("pageup",new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                JScrollBar sb = scroll.getVerticalScrollBar();
                sb.getModel().setValue(sb.getModel().getValue()-sb.getBlockIncrement());
            }
        });
        view.getActionMap().put("down",new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                JScrollBar sb = scroll.getVerticalScrollBar();
                sb.getModel().setValue(sb.getModel().getValue()+sb.getUnitIncrement());
            }
        });
        view.getActionMap().put("up",new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                JScrollBar sb = scroll.getVerticalScrollBar();
                sb.getModel().setValue(sb.getModel().getValue()-sb.getUnitIncrement());
            }
        });
        
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
        view.addDocumentListener(this);
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
            //u.p("marker = " + marker);
            String short_url = url_text.substring(5);
            //u.p("sub = " + short_url);
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
        if ( listener != null ) {
            listener.pageLoadSuccess(url_text, view.getDocumentTitle());
        }
    }
    
    
    public void documentLoaded() {
        //u.p("got a document loaded event");
        setupSubmitActions();
    }
    
    public void setupSubmitActions() {
        //u.p("setup submit actions");
        Context cx = view.getContext();
        Map forms = cx.getForms();
        //u.p("forms = " + forms);
        Iterator form_it = forms.keySet().iterator();
        while(form_it.hasNext()) {
            final String form_name = (String)form_it.next();
            Map form = (Map)forms.get(form_name);
            //u.p("got form: " + form_name);
            Iterator fields = form.keySet().iterator();
            while(fields.hasNext()) {
                String field_name = (String)fields.next();
                List field_list = (List)form.get(field_name);
                //u.p("got field set: " + field_name);
                
                ButtonGroup bg = new ButtonGroup();
                for(int i=0; i<field_list.size(); i++) {
                    Context.FormComponent comp = (Context.FormComponent)field_list.get(i);
                    //u.p("got component: " + comp);
                    
                    // bind radio buttons together
                    if(comp.component instanceof JRadioButton) {
                        bg.add((JRadioButton)comp.component);
                    }
                    
                    // add reset action listeners
                    if(comp.component instanceof JButton) {
                        //u.p("it's a jbutton");
                        if(comp.element.getAttribute("type").equals("reset")) {
                            ((JButton)comp.component).addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent evt) {
                                    u.p("reset button hit");
                                    
                                    Context ctx = view.getContext();
                                    Iterator fields = ctx.getInputFieldComponents(form_name);
                                    while(fields.hasNext()) {
                                        List field_list = (List)fields.next();
                                        for(int i=0; i<field_list.size(); i++) {
                                            Context.FormComponent comp = (Context.FormComponent)field_list.get(i);
                                            comp.reset();
                                        }
                                    }
                                    
                                }
                            });
                        }
                        if(comp.element.getAttribute("type").equals("submit")) {
                            ((JButton)comp.component).addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent evt) {
                                    u.p("submit button hit");
                                    StringBuffer query = new StringBuffer();
                                    query.append("?");
                                    Context ctx = view.getContext();
                                    Iterator fields = ctx.getInputFieldComponents(form_name);
                                    while(fields.hasNext()) {
                                        List field = (List)fields.next();
                                        for(int i=0; i<field.size(); i++) {
                                            Context.FormComponent comp = (Context.FormComponent)field.get(i);
                                            if(comp.element.hasAttribute("value")) {
                                                query.append(comp.element.getAttribute("name"));
                                                query.append("=");
                                                query.append(comp.element.getAttribute("value"));
                                                query.append("&");
                                            }
                                        }
                                    }
                                    String url = ctx.getFormAction(form_name) + query.toString();
                                    u.p("going to load: " + url);
                                    try {
                                        loadPage(url);
                                    } catch (Exception ex) {
                                        u.p(ex);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }
}

