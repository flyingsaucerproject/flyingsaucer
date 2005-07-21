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

import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.Uu;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.logging.Logger;

/**
 * Description of the Class
 *
 * @author empty
 */
public class BrowserStartup {
    /**
     * Description of the Field
     */
    public BrowserPanel panel;
    /**
     * Description of the Field
     */
    protected BrowserMenuBar menu;
    /**
     * Description of the Field
     */
    protected JFrame frame;
    /**
     * Description of the Field
     */
    protected JFrame validation_console = null;
    /**
     * Description of the Field
     */
    protected BrowserActions actions;

    /**
     * Description of the Field
     */
    protected ValidationHandler error_handler = new ValidationHandler();
    /**
     * Description of the Field
     */
    public static Logger logger = Logger.getLogger("app.browser");

    /**
     * Constructor for the BrowserStartup object
     */
    public BrowserStartup() {
        logger.info("starting up");
    }

    /**
     * Description of the Method
     */
    public void init() {
        logger.info("creating UI");
        actions = new BrowserActions(this);
        actions.init();

        panel = new BrowserPanel(this, new FrameBrowserPanelListener());
        panel.init();
        panel.createLayout();
        panel.createActions();

        menu = new BrowserMenuBar(this);
        menu.init();
        menu.createLayout();
        menu.createActions();

        try {
            panel.loadPage("demo:demos/splash/splash.html");
            //panel.loadPage("demo:demos/paragraph.xhtml");
            //panel.loadPage("demo:demos/layout/multicol/glish/nested-float.xhtml");
        } catch (Exception ex) {
            Uu.p(ex);
        }
    }

    /**
     * The main program for the BrowserStartup class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        if (GeneralUtil.isMacOSX()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "FS Browser");
        }

        //System.out.println(new URI("images/Stop24.gif"));
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final BrowserStartup bs = new BrowserStartup();
        bs.frame = frame;
        bs.init();
        frame.setJMenuBar(bs.menu);
        frame.getContentPane().add(bs.panel);
        frame.pack();
        frame.setSize(700, 600);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                bs.panel.view.relayout();
            }
        });

        frame.show();
        if (args.length > 0) {
            bs.panel.loadPage(args[0]);
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class FrameBrowserPanelListener implements BrowserPanelListener {
        /**
         * Description of the Method
         *
         * @param url   PARAM
         * @param title PARAM
         */
        public void pageLoadSuccess(String url, String title) {
            frame.setTitle(title + (title.length() > 0 ? " - " : "") + "Flying Saucer");
        }
    }

}


/*
 * $Id$
 *
 * $Log$
 * Revision 1.14  2005/07/21 22:56:07  joshy
 * tweaked the splash screen
 *
 * Revision 1.13  2005/06/16 07:24:44  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.12  2005/06/15 13:35:27  tobega
 * Fixed history
 *
 * Revision 1.11  2005/04/03 21:51:31  joshy
 * fixed code that gets the XMLReader on the mac
 * added isMacOSX() to GeneralUtil
 * added app name and single menu bar to browser
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2005/03/28 19:18:47  pdoubleya
 * Fixed to update layout on resize.
 *
 * Revision 1.9  2005/03/28 19:03:32  pdoubleya
 * Changed startup page.
 *
 * Revision 1.8  2004/12/12 03:33:07  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.7  2004/12/12 02:54:30  tobega
 * Making progress
 *
 * Revision 1.6  2004/11/16 07:25:20  tobega
 * Renamed HTMLPanel to BasicPanel
 *
 * Revision 1.5  2004/11/16 03:43:25  joshy
 * first pass at printing support
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/11/10 17:28:54  joshy
 * initial support for anti-aliased text w/ minium
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/10/23 14:38:58  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

