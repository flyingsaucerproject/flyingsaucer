
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

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.logging.*;
import org.joshy.u;


public class BrowserActions {
    public static Logger logger = Logger.getLogger("app.browser");
    Action open_file, quit;
    Action cut, copy, paste;
    Action forward, backward, refresh, reload, load, stop;
    BrowserStartup root;
    
    public BrowserActions(BrowserStartup root) {
        this.root = root;
    }
    
    public void init() {
        stop = new EmptyAction("Stop");
        setAccel(stop, KeyEvent.VK_ESCAPE);
        
        open_file = new AbstractAction() {
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
        };
        open_file.putValue(Action.NAME,"Open File...");
        setAccel(open_file,KeyEvent.VK_O);
        setMnemonic(open_file,new Integer(KeyEvent.VK_O));
        
        quit = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                System.exit(0);
            }
        };
        setName(quit,"Quit");
        setAccel(quit,KeyEvent.VK_Q);
        setMnemonic(quit,new Integer(KeyEvent.VK_Q));

        cut = new EmptyAction("Cut",KeyEvent.VK_X);
        cut.setEnabled(false);
        setMnemonic(cut,new Integer(KeyEvent.VK_T));
        copy = new EmptyAction("Copy",KeyEvent.VK_C);
        copy.setEnabled(false);
        setMnemonic(copy,new Integer(KeyEvent.VK_C));
        paste = new EmptyAction("Paste",KeyEvent.VK_V);
        paste.setEnabled(false);
        setMnemonic(paste,new Integer(KeyEvent.VK_P));

        
        
        backward = new AbstractAction("Back") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    root.panel.goBack();
                    root.panel.view.repaint();
                } catch (Exception ex) {
                    u.p(ex);
                }
            }
        };
        backward.setEnabled(false);
        backward.putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
            InputEvent.ALT_MASK));
        
        
        
        forward = new AbstractAction("Forward") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    root.panel.goForward();
                    root.panel.view.repaint();
                } catch (Exception ex) {
                    u.p(ex);
                }
            }
        };
        forward.setEnabled(false);
        forward.putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
            InputEvent.ALT_MASK));
        
        refresh = new EmptyAction("Refresh Page") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    root.panel.view.invalidate();
                    root.panel.view.repaint();
                } catch (Exception ex) {
                    u.p(ex);
                }
            }
        };
        refresh.putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke("F5"));

        
        reload = new EmptyAction("Reload") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    root.panel.reloadPage();
                    root.panel.view.repaint();
                } catch (Exception ex) {
                    u.p(ex);
                }
            }
        };
        reload.putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_F5,
            InputEvent.SHIFT_MASK));
        
        load = new AbstractAction("Load") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    String url_text = root.panel.url.getText();
                    root.panel.loadPage(url_text);
                    root.panel.view.repaint();
                } catch (Exception ex) {
                    u.p(ex);
                }
            }
        };

    }
    
    
    public void setName(Action act, String name) {
        act.putValue(Action.NAME,name);
    }
    
    public void setAccel(Action act, int key) {
        act.putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(key,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    public void setMnemonic(Action act, Integer mnem) {
        act.putValue(Action.MNEMONIC_KEY,mnem);
    }
}
