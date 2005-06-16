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

import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
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
    JButton font_rst;
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
    public static Logger logger = Logger.getLogger("app.browser");

    private PanelManager manager;

    /**
     * Constructor for the BrowserPanel object
     *
     * @param root     PARAM
     * @param listener PARAM
     */
    public BrowserPanel(BrowserStartup root, BrowserPanelListener listener) {
        super();
        this.root = root;
        this.listener = listener;
    }


    /**
     * Description of the Method
     */
    public void init() {
        forward = new JButton();
        backward = new JButton();
        stop = new JButton();
        reload = new JButton();
        url = new JTextField();
        manager = new PanelManager();
        view = new XHTMLPanel(manager);
        scroll = new FSScrollPane(view);

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
        
        /* c.gridx++;
        gbl.setConstraints(stop, c);
        add(stop); */
        
        c.gridx++;
        gbl.setConstraints(reload, c);
        add(reload);

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
    }


    /**
     * Description of the Method
     */
    public void goForward() {
        String uri = manager.getForward();
        view.setDocument(uri);
        updateButtons();
    }

    /**
     * Description of the Method
     */
    public void goBack() {
        String uri = manager.getBack();
        view.setDocument(uri);
        updateButtons();
    }

    /**
     * Description of the Method
     */
    public void reloadPage() {
        logger.info("Reloading Page: ");
        if (manager.getBaseURL() != null) {
            loadPage(manager.getBaseURL());
        }
    }

    /**
     * Description of the Method
     *
     * @param url_text PARAM
     */
    //TODO: make this part of an implementation of UserAgentCallback instead
    public void loadPage(String url_text) {
        try {

            logger.info("Loading Page: " + url_text);
            view.setDocument(url_text);
            view.addDocumentListener(this);
            updateButtons();

            setStatus("Successfully loaded: " + url_text);
            if (listener != null) {
                listener.pageLoadSuccess(url_text, view.getDocumentTitle());
            }
        } catch (Exception ex) {
            XRLog.general(Level.SEVERE, "Could not load page for display.", ex);
            ex.printStackTrace();
        }
    }


    /**
     * Description of the Method
     */
    public void documentLoaded() {
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
    protected void updateButtons() {
        if (manager.hasBack()) {
            root.actions.backward.setEnabled(true);
        } else {
            root.actions.backward.setEnabled(false);
        }
        if (manager.hasForward()) {
            root.actions.forward.setEnabled(true);
        } else {
            root.actions.forward.setEnabled(false);
        }

        url.setText(manager.getBaseURL());
    }


}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.26  2005/06/16 07:24:43  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.25  2005/06/15 13:35:27  tobega
 * Fixed history
 *
 * Revision 1.24  2005/06/15 10:56:13  tobega
 * cleaned up a bit of URL mess, centralizing URI-resolution and loading to UserAgentCallback
 *
 * Revision 1.23  2005/06/01 21:36:34  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.22  2005/03/28 20:02:01  pdoubleya
 * Removed commented code, cleaned menu bar.
 *
 * Revision 1.21  2005/02/05 11:35:59  pdoubleya
 * Load pages directly from XMLResource.
 *
 * Revision 1.20  2005/02/03 23:19:43  pdoubleya
 * Uses ResourceProvider for loading files.
 *
 * Revision 1.19  2005/01/30 10:21:49  pdoubleya
 * Extracted keyboard actions to FSScrollPane.
 *
 * Revision 1.18  2005/01/29 20:17:42  pdoubleya
 * Updated panels to support page up/down properly, and formatted/cleaned.
 *
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

