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

import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.*;
import java.net.*;
import java.io.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.JOptionPane;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.test.*;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class BrowserActions {
    /** Description of the Field */
    Action open_file, quit;
    /** Description of the Field */
    Action cut, copy, paste;
    /** Description of the Field */
    Action forward, backward, refresh, reload, load, stop;
    
    Action generate_diff;
    /** Description of the Field */
    BrowserStartup root;
    /** Description of the Field */
    public static Logger logger = Logger.getLogger( "app.browser" );

    /**
     * Constructor for the BrowserActions object
     *
     * @param root  PARAM
     */
    public BrowserActions( BrowserStartup root ) {
        this.root = root;
    }

    /** Description of the Method */
    public void init() {
        stop = new EmptyAction( "Stop" );
        setAccel( stop, KeyEvent.VK_ESCAPE );

        open_file =
            new AbstractAction() {
                public void actionPerformed( ActionEvent evt ) {
                    try {
                        FileDialog fd = new FileDialog( root.frame, "Open a local file", FileDialog.LOAD );
                        fd.show();
                        String file = fd.getDirectory() + fd.getFile();
                        root.panel.loadPage( file );
                    } catch ( Exception ex ) {
                        logger.info( "error:" + ex );
                    }
                }
            };
        open_file.putValue( Action.NAME, "Open File..." );
        setAccel( open_file, KeyEvent.VK_O );
        setMnemonic( open_file, new Integer( KeyEvent.VK_O ) );

        quit =
            new AbstractAction() {
                public void actionPerformed( ActionEvent evt ) {
                    System.exit( 0 );
                }
            };
        setName( quit, "Quit" );
        setAccel( quit, KeyEvent.VK_Q );
        setMnemonic( quit, new Integer( KeyEvent.VK_Q ) );

        cut = new EmptyAction( "Cut", KeyEvent.VK_X );
        cut.setEnabled( false );
        setMnemonic( cut, new Integer( KeyEvent.VK_T ) );
        copy = new EmptyAction( "Copy", KeyEvent.VK_C );
        copy.setEnabled( false );
        setMnemonic( copy, new Integer( KeyEvent.VK_C ) );
        paste = new EmptyAction( "Paste", KeyEvent.VK_V );
        paste.setEnabled( false );
        setMnemonic( paste, new Integer( KeyEvent.VK_P ) );


        backward =
            new AbstractAction( "Back" ) {
                public void actionPerformed( ActionEvent evt ) {
                    try {
                        root.panel.goBack();
                        root.panel.view.repaint();
                    } catch ( Exception ex ) {
                        u.p( ex );
                    }
                }
            };
        backward.setEnabled( false );
        backward.putValue( Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT,
                InputEvent.ALT_MASK ) );


        forward =
            new AbstractAction( "Forward" ) {
                public void actionPerformed( ActionEvent evt ) {
                    try {
                        root.panel.goForward();
                        root.panel.view.repaint();
                    } catch ( Exception ex ) {
                        u.p( ex );
                    }
                }
            };
        forward.setEnabled( false );
        forward.putValue( Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT,
                InputEvent.ALT_MASK ) );

        refresh =
            new EmptyAction( "Refresh Page" ) {
                public void actionPerformed( ActionEvent evt ) {
                    try {
                        root.panel.view.invalidate();
                        root.panel.view.repaint();
                    } catch ( Exception ex ) {
                        u.p( ex );
                    }
                }
            };
        refresh.putValue( Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke( "F5" ) );

        reload =
            new EmptyAction( "Reload" ) {
                public void actionPerformed( ActionEvent evt ) {
                    try {
                        root.panel.reloadPage();
                        root.panel.view.repaint();
                    } catch ( Exception ex ) {
                        u.p( ex );
                    }
                }
            };
        reload.putValue( Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke( KeyEvent.VK_F5,
                InputEvent.SHIFT_MASK ) );

        load =
            new AbstractAction( "Load" ) {
                public void actionPerformed( ActionEvent evt ) {
                    try {
                        String url_text = root.panel.url.getText();
                        root.panel.loadPage( url_text );
                        root.panel.view.repaint();
                    } catch ( Exception ex ) {
                        u.p( ex );
                    }
                }
            };

        generate_diff =
            new AbstractAction( "Generate Diff" ) {
                public void actionPerformed( ActionEvent evt ) {
                    try {
                        
                        URL url = root.panel.view.getURL();
                        if(url != null) {
                            if(url.toString().startsWith("file:")) {
                                String str = url.toString();
                                str = str.substring(6,str.length()-6);
                                if(new File(str+".diff").exists()) {
                                    int n = JOptionPane.showConfirmDialog(root.panel.view, 
                                        "Diff already exists. Overwrite?",
                                        "Warning",
                                        JOptionPane.OK_CANCEL_OPTION);
                                    if(n != JOptionPane.OK_OPTION) {
                                        return;
                                    }
                                }
                                DocumentDiffTest.generateTestFile(
                                    str+".xhtml",
                                    str+".diff",
                                    500,500);
                                u.p("wrote out: " + str+".diff");
                            }
                        }
                        
                    } catch ( Exception ex ) {
                        u.p( ex );
                    }
                }
            };
    }


    /**
     * Sets the name attribute of the BrowserActions object
     *
     * @param act   The new name value
     * @param name  The new name value
     */
    public void setName( Action act, String name ) {
        act.putValue( Action.NAME, name );
    }

    /**
     * Sets the accel attribute of the BrowserActions object
     *
     * @param act  The new accel value
     * @param key  The new accel value
     */
    public void setAccel( Action act, int key ) {
        act.putValue( Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke( key,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
    }

    /**
     * Sets the mnemonic attribute of the BrowserActions object
     *
     * @param act   The new mnemonic value
     * @param mnem  The new mnemonic value
     */
    public void setMnemonic( Action act, Integer mnem ) {
        act.putValue( Action.MNEMONIC_KEY, mnem );
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2004/11/07 23:24:18  joshy
 * added menu item to generate diffs
 * added diffs for multi-colored borders and inline borders
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

