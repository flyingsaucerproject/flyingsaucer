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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.logging.Logger;
import javax.swing.JFrame;

import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.swing.HTMLPanel;
import org.xhtmlrenderer.util.u;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class BrowserStartup {
    /** Description of the Field */
    public BrowserPanel panel;
    /** Description of the Field */
    protected BrowserMenuBar menu;
    /** Description of the Field */
    protected JFrame frame;
    /** Description of the Field */
    protected HistoryManager history;
    /** Description of the Field */
    protected JFrame validation_console = null;
    /** Description of the Field */
    protected BrowserActions actions;

    /** Description of the Field */
    protected ValidationHandler error_handler = new ValidationHandler();
    /** Description of the Field */
    public static Logger logger = Logger.getLogger( "app.browser" );

    /** Constructor for the BrowserStartup object */
    public BrowserStartup() {
        logger.info( "starting up" );
        history = new HistoryManager();
    }

    /** Description of the Method */
    public void init() {
        logger.info( "creating UI" );
        actions = new BrowserActions( this );
        actions.init();

        panel = new BrowserPanel( this, new FrameBrowserPanelListener() );
        panel.init();
        panel.createLayout();
        panel.createActions();

        menu = new BrowserMenuBar( this );
        menu.init();
        menu.createLayout();
        menu.createActions();

        try {
            panel.loadPage( "demo:demos/splash/splash.html" );
        } catch ( Exception ex ) {
            u.p( ex );
        }
    }

    /**
     * The main program for the BrowserStartup class
     *
     * @param args           The command line arguments
     * @exception Exception  Throws
     */
    public static void main( String[] args )
        throws Exception {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        BrowserStartup bs = new BrowserStartup();
        bs.frame = frame;
        bs.init();
        frame.setJMenuBar( bs.menu );
        frame.getContentPane().add( bs.panel );
        frame.pack();
        frame.setSize( 600, 700 );
        frame.show();
        if ( args.length > 0 ) {
            bs.panel.loadPage( args[0] );
        }
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    class FrameBrowserPanelListener implements BrowserPanelListener {
        /**
         * Description of the Method
         *
         * @param url    PARAM
         * @param title  PARAM
         */
        public void pageLoadSuccess( String url, String title ) {
            frame.setTitle( title + ( title.length() > 0 ? " - " : "" ) + "Flying Saucer" );
        }
    }

}




/*
 * $Id$
 *
 * $Log$
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

