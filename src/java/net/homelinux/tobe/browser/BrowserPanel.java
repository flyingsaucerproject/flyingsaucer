
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

package net.homelinux.tobe.browser;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xhtmlrenderer.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.logging.*;
import java.io.*;
import java.net.URI;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.css.*;
import org.xhtmlrenderer.render.*;
//import org.xhtmlrenderer.swing.*;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.css.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.event.*;

import net.homelinux.tobe.renderer.HTMLPanel;
import net.homelinux.tobe.renderer.UserAgentCallback;
import net.homelinux.tobe.renderer.XRDocument;

import org.xhtmlrenderer.demo.browser.DemoMarker;

public class BrowserPanel extends JPanel implements DocumentListener, UserAgentCallback {
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
        view = new HTMLPanel(this);
        view.setErrorHandler(root.error_handler);
        view.addDocumentListener(this);
        status = new JLabel("Status");

        int text_width = 200;
        view.setPreferredSize(new Dimension(text_width,text_width));
        scroll = new JScrollPane(view);
        scroll.setVerticalScrollBarPolicy(scroll.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(scroll.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(text_width,text_width));
        scroll.getVerticalScrollBar().setBlockIncrement(100);
        scroll.getVerticalScrollBar().setUnitIncrement(15);
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
       view.setDocument(root.history.getCurrentDocument());
       //root.history.dumpHistory();
       updateButtons();
    }

    public void goBack() throws Exception {
       root.history.goPrevious();
       view.setDocument(root.history.getCurrentDocument());
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

    public void loadPage(XRDocument doc, URI uri) throws Exception {
        root.history.goNewDocument(doc, uri);//have to do this first as setDocument depends on it via getNamespaceHandler!
        view.setDocument(doc);
        updateButtons();
    }
    public void loadPage(String url_text) throws Exception {
        logger.info("Loading Page: " + url_text);
        current_url = url_text;

        XRDocument doc = null;
        URI ref = null;

        if(url_text.startsWith("demo:")) {
            DemoMarker marker = new DemoMarker();
            //u.p("marker = " + marker);
            String short_url = url_text.substring(5);
            //u.p("sub = " + short_url);
            if(!short_url.startsWith("/")) {
                short_url = "/" + short_url;
            }
            InputStream is = marker.getClass().getResourceAsStream(short_url);
            ref = new URI(marker.getClass().getResource(short_url).toString());
            doc = new XRDocument(this,is,ref);
        } else if(url_text.startsWith("http")) {
            ref = new URI(url_text);
            doc = loadUriDocument(ref);
        } else if(url_text.startsWith("file://")) {
            File file = new File(new URI(url_text));
            if(file.isDirectory()) {
                doc = new DirectoryLister().list(this,file);
                ref = file.toURI();
            } else {
                ref = file.toURI();
                doc = loadUriDocument(ref);
            }

        } else {
            ref = new File(url_text).toURI();
            doc = new XRDocument(this, getInputStreamForURI(ref), ref);
        }
        loadPage(doc,ref);

        setStatus("Successfully loaded: " + url_text);
        if ( listener != null ) {
            listener.pageLoadSuccess(url_text, doc.getDocumentTitle());
        }
    }
    
    private XRDocument loadUriDocument(URI uri) {
        XRDocument doc = null;
        InputStream is = getInputStreamForURI(uri);
        String path = uri.getPath();
        System.out.println("URI path "+path);
        if(path.endsWith(".xml") || path.endsWith(".xhtml")) {
            doc = new XRDocument(this,is,uri);
        } else {
            org.ccil.cowan.tagsoup.Parser tsp = new org.ccil.cowan.tagsoup.Parser();
            try{
                tsp.setFeature(tsp.namespacesFeature, false);
                tsp.setFeature(tsp.namespacePrefixesFeature, true);
            }
            catch(org.xml.sax.SAXNotRecognizedException e) {
                System.err.println(e);
            }
            catch(org.xml.sax.SAXNotSupportedException e) {
                System.err.println(e);
            }
            doc = new XRDocument(this,tsp,is,uri);
            //doc = new XRDocument(this,is,uri);
        }
        return doc;
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
 
    //Methods for UserAgentCallback
    /** this is where we could implement some security about which URIs to load */
    public java.io.InputStream getInputStreamForURI(java.net.URI uri) {
        java.io.InputStream is = null;
        try {
            URI baseURI = root.history.getCurrentURI();
            is = baseURI.resolve(uri).toURL().openStream();
        }
        catch(java.net.MalformedURLException e) {
            
        }
        catch(java.io.IOException e) {
            
        }
        return is;
    }
    
    net.homelinux.tobe.renderer.NamespaceHandler xhtmlHandler = new net.homelinux.tobe.XhtmlNamespaceHandler();
    net.homelinux.tobe.renderer.NamespaceHandler oldHtmlHandler = new net.homelinux.tobe.browser.HTMLNamespaceHandler();
    net.homelinux.tobe.renderer.NamespaceHandler xmlHandler = new net.homelinux.tobe.NoNamespaceHandler();
    
    public net.homelinux.tobe.renderer.NamespaceHandler getNamespaceHandler(String namespace) {
        if(xhtmlHandler.getNamespace().equals(namespace)) {
                System.out.println("using xhtml");
            return xhtmlHandler;
        } else {
            org.w3c.dom.Document dom = root.history.getCurrentDocument().getDomDocument();
            org.w3c.dom.Element e = dom.getDocumentElement();
            String type = e.getTagName();
            if(type == null || type.equals("")) type = e.getLocalName();
            if(type.equals("html")) {
                System.out.println("using HTML");
                return oldHtmlHandler;
            } else {
                System.out.println("using xml");
                return xmlHandler;
            }
        }
    }
    
    public boolean isVisited(java.net.URI uri) {
        return root.history.isVisited(uri);
    }
    
}

