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
package org.xhtmlrenderer.demo.browser;

import org.w3c.dom.Document;
import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.Uu;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;


/**
 * Description of the Class
 *
 * @author empty
 */
public class BrowserPanel extends JPanel implements DocumentListener {

    /**
     * Description of the Field
     */
    JButton forward;
    /**
     * Description of the Field
     */
    JButton backward;
    /**
     * Description of the Field
     */
    JButton stop;
    /**
     * Description of the Field
     */
    JButton reload;
    /**
     * Description of the Field
     */
    JButton font_inc;
    /**
     * Description of the Field
     */
    JButton font_dec;
    /**
     * Description of the Field
     */
    JTextField url;
    /**
     * Description of the Field
     */
    BrowserStatus status;
    /**
     * Description of the Field
     */
    public XHTMLPanel view;
    /**
     * Description of the Field
     */
    JScrollPane scroll;
    /**
     * Description of the Field
     */
    BrowserStartup root;
    /**
     * Description of the Field
     */
    BrowserPanelListener listener;

    /**
     * Description of the Field
     */
    String current_url = null;
    /**
     * Description of the Field
     */
    public static Logger logger = Logger.getLogger("app.browser");

    /**
     * Constructor for the BrowserPanel object
     *
     * @param root     PARAM
     * @param listener PARAM
     */
    public BrowserPanel(BrowserStartup root, BrowserPanelListener listener) {
        this.root = root;
        this.listener = listener;
    }


    /**
     * Description of the Method
     */
    public void init() {
        forward = new JButton();
        backward = new JButton("Back");
        stop = new JButton("Stop");
        reload = new JButton("Reload");
        url = new JTextField();
        view = new XHTMLPanel();
        font_inc = new JButton("A");
        font_dec = new JButton("a");

        RenderingContext rc = view.getRenderingContext();
        try {
            rc.setFontMapping("Fuzz", Font.createFont(Font.TRUETYPE_FONT,
                    new DemoMarker().getClass().getResourceAsStream("/demos/fonts/fuzz.ttf")));
        } catch (Exception ex) {
            Uu.p(ex);
        }
        view.setErrorHandler(root.error_handler);
        status = new BrowserStatus();
        status.init();

        int text_width = 200;
        view.setPreferredSize(new Dimension(text_width, text_width));
        scroll = new JScrollPane(view);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(text_width, text_width));
        scroll.getVerticalScrollBar().setBlockIncrement(100);
        scroll.getVerticalScrollBar().setUnitIncrement(15);
    }

    /**
     * Description of the Method
     */
    public void createLayout() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = c.weighty = 0.0;
        gbl.setConstraints(backward, c);
        add(backward);

        c.gridx++;
        gbl.setConstraints(forward, c);
        add(forward);

        c.gridx++;
        gbl.setConstraints(stop, c);
        add(stop);

        c.gridx++;
        gbl.setConstraints(reload, c);
        add(reload);

        c.gridx++;
        gbl.setConstraints(font_inc, c);
        add(font_inc);

        c.gridx++;
        gbl.setConstraints(font_dec, c);
        add(font_dec);

        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 10.0;
        gbl.setConstraints(url, c);
        add(url);

        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 7;
        c.weightx = c.weighty = 10.0;
        gbl.setConstraints(scroll, c);
        add(scroll);

        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.1;
        gbl.setConstraints(status, c);
        add(status);

    }

    /**
     * Description of the Method
     */
    public void createActions() {
        backward.setAction(root.actions.backward);
        forward.setAction(root.actions.forward);
        reload.setAction(root.actions.reload);
        url.setAction(root.actions.load);
        updateButtons();

        font_inc.setAction(root.actions.increase_font);
        font_dec.setAction(root.actions.decrease_font);

        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "pagedown");
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "pageup");
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");

        view.getActionMap().put("pagedown",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent evt) {
                        JScrollBar sb = scroll.getVerticalScrollBar();
                        sb.getModel().setValue(sb.getModel().getValue() + sb.getBlockIncrement());
                    }
                });
        view.getActionMap().put("pageup",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent evt) {
                        JScrollBar sb = scroll.getVerticalScrollBar();
                        sb.getModel().setValue(sb.getModel().getValue() - sb.getBlockIncrement());
                    }
                });
        view.getActionMap().put("down",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent evt) {
                        JScrollBar sb = scroll.getVerticalScrollBar();
                        sb.getModel().setValue(sb.getModel().getValue() + sb.getUnitIncrement());
                    }
                });
        view.getActionMap().put("up",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent evt) {
                        JScrollBar sb = scroll.getVerticalScrollBar();
                        sb.getModel().setValue(sb.getModel().getValue() - sb.getUnitIncrement());
                    }
                });

    }


    /**
     * Description of the Method
     */
    public void goForward() {
        root.history.goNext();
        view.setDocument(root.history.getCurrentDocument(), root.history.getCurrentURL());
        //root.history.dumpHistory();
        updateButtons();
    }

    /**
     * Description of the Method
     *
     * @throws Exception Throws
     */
    public void goBack()
            throws Exception {
        root.history.goPrevious();
        //root.history.dumpHistory();
        view.setDocument(root.history.getCurrentDocument(), root.history.getCurrentURL());
        updateButtons();
    }

    /**
     * Description of the Method
     *
     * @throws Exception Throws
     */
    public void reloadPage()
            throws Exception {
        logger.info("Reloading Page: ");
        if (current_url != null) {
            loadPage(current_url);
        }
    }

    /**
     * Description of the Method
     *
     * @param doc PARAM
     * @param url PARAM
     * @throws Exception Throws
     */
    public void loadPage(Document doc, URL url) throws Exception {
        view.setDocument(doc, url);
        view.addDocumentListener(this);
        root.history.goNewDocument(doc, url);
        updateButtons();
    }

    /**
     * Description of the Method
     *
     * @param url_text PARAM
     * @throws Exception Throws
     */
    public void loadPage(String url_text)
            throws Exception {
        logger.info("Loading Page: " + url_text);
        current_url = url_text;

        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setValidating(true);
        DocumentBuilder builder = fact.newDocumentBuilder();
        builder.setErrorHandler(root.error_handler);
        Document doc = null;

        URL ref = null;

        if (url_text.startsWith("demo:")) {
            DemoMarker marker = new DemoMarker();
            //Uu.p("marker = " + marker);
            String short_url = url_text.substring(5);
            //Uu.p("sub = " + short_url);
            if (!short_url.startsWith("/")) {
                short_url = "/" + short_url;
            }
            doc = builder.parse(marker.getClass().getResourceAsStream(short_url));
            ref = marker.getClass().getResource(short_url);
            Uu.p("doc = " + doc);
            Uu.p("ref = " + ref);
        } else if (url_text.startsWith("http")) {
            doc = builder.parse(url_text);
            ref = new URL(url_text);
        } else if (url_text.startsWith("file://")) {
            File file = new File(new URI(url_text));
            if (file.isDirectory()) {
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

        Uu.p("going to load a page: " + doc + " " + ref);
        loadPage(doc, ref);

        setStatus("Successfully loaded: " + url_text);
        if (listener != null) {
            listener.pageLoadSuccess(url_text, view.getDocumentTitle());
        }
    }


    /**
     * Description of the Method
     */
    public void documentLoaded() {
        //Uu.p("got a document loaded event");
        //setupSubmitActions();
    }


    /**
     * Sets the status attribute of the BrowserPanel object
     *
     * @param txt The new status value
     */
    public void setStatus(String txt) {
        status.text.setText(txt);
    }

    /**
     * Description of the Method
     */
    /*public void setupSubmitActions() {
        //Uu.p("setup submit actions");
        SharedContext cx = view.getContext();
        Map forms = cx.getForms();
        //Uu.p("forms = " + forms);
        Iterator form_it = forms.keySet().iterator();
        while (form_it.hasNext()) {
            final String form_name = (String) form_it.next();
            Map form = (Map) forms.get(form_name);
            //Uu.p("got form: " + form_name);
            Iterator fields = form.keySet().iterator();
            while (fields.hasNext()) {
                String field_name = (String) fields.next();
                List field_list = (List) form.get(field_name);
                //Uu.p("got field set: " + field_name);

                ButtonGroup bg = new ButtonGroup();
                for (int i = 0; i < field_list.size(); i++) {
                    SharedContext.FormComponent comp = (SharedContext.FormComponent) field_list.get(i);
                    //Uu.p("got component: " + comp);

                    // bind radio buttons together
                    if (comp.component instanceof JRadioButton) {
                        bg.add((JRadioButton) comp.component);
                    }

                    // add reset action listeners
                    if (comp.component instanceof JButton) {
                        //Uu.p("it's a jbutton");
                        if (comp.element.getAttribute("type").equals("reset")) {
                            ((JButton) comp.component).addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent evt) {
                                    Uu.p("reset button hit");

                                    SharedContext ctx = view.getContext();
                                    Iterator fields = ctx.getInputFieldComponents(form_name);
                                    while (fields.hasNext()) {
                                        List field_list = (List) fields.next();
                                        for (int i = 0; i < field_list.size(); i++) {
                                            SharedContext.FormComponent comp = (SharedContext.FormComponent) field_list.get(i);
                                            comp.reset();
                                        }
                                    }

                                }
                            });
                        }
                        if (comp.element.getAttribute("type").equals("submit")) {
                            ((JButton) comp.component).addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent evt) {
                                    Uu.p("submit button hit");
                                    StringBuffer query = new StringBuffer();
                                    query.append("?");
                                    SharedContext ctx = view.getContext();
                                    Iterator fields = ctx.getInputFieldComponents(form_name);
                                    while (fields.hasNext()) {
                                        List field = (List) fields.next();
                                        for (int i = 0; i < field.size(); i++) {
                                            SharedContext.FormComponent comp = (SharedContext.FormComponent) field.get(i);
                                            if (comp.element.hasAttribute("value")) {
                                                query.append(comp.element.getAttribute("name"));
                                                query.append("=");
                                                query.append(comp.element.getAttribute("value"));
                                                query.append("&");
                                            }
                                        }
                                    }
                                    String url = ctx.getFormAction(form_name) + query.toString();
                                    Uu.p("going to load: " + url);
                                    try {
                                        loadPage(url);
                                    } catch (Exception ex) {
                                        Uu.p(ex);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }*/


    /**
     * Description of the Method
     */
    protected void updateButtons() {
        if (root.history.hasPrevious()) {
            root.actions.backward.setEnabled(true);
        } else {
            root.actions.backward.setEnabled(false);
        }
        if (root.history.hasNext()) {
            root.actions.forward.setEnabled(true);
        } else {
            root.actions.forward.setEnabled(false);
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.17  2005/01/13 00:48:47  tobega
 * Added preparation of values for a form submission
 *
 * Revision 1.16  2004/12/29 10:39:38  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.15  2004/12/12 16:11:04  tobega
 * Fixed bug concerning order of inline content. Added a demo for pseudo-elements.
 *
 * Revision 1.14  2004/12/12 03:33:07  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.13  2004/12/12 02:54:11  tobega
 * Making progress
 *
 * Revision 1.12  2004/12/01 00:13:34  tobega
 * Fixed incorrect handling of http urls.
 *
 * Revision 1.11  2004/11/27 15:46:37  joshy
 * lots of cleanup to make the code clearer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/17 14:58:17  joshy
 * added actions for font resizing
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/17 00:44:54  joshy
 * fixed bug in the history manager
 * added cursor support to the link listener
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/16 07:25:20  tobega
 * Renamed HTMLPanel to BasicPanel
 *
 * Revision 1.7  2004/11/16 03:43:25  joshy
 * first pass at printing support
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/12 20:43:29  joshy
 * added demo of custom font
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/12 02:23:56  joshy
 * added new APIs for rendering context, xhtmlpanel, and graphics2drenderer.
 * initial support for font mapping additions
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/23 14:38:58  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

