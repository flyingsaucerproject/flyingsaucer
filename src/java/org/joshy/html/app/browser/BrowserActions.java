package org.joshy.html.app.browser;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.logging.*;
import org.joshy.u;


public class BrowserActions {
    public static Logger logger = Logger.getLogger("app.browser");
    Action open_file, quit;
    Action cut, copy, paste;
    Action forward, backward, reload, load, stop;
    BrowserStartup root;
    
    public BrowserActions(BrowserStartup root) {
        this.root = root;
    }
    
    public void init() {
        stop = new EmptyAction("Stop");
        
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
        
        quit = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                System.exit(0);
            }
        };
        setName(quit,"Quit");
        setAccel(quit,KeyEvent.VK_Q);

        cut = new EmptyAction("Cut",KeyEvent.VK_X);
        cut.setEnabled(false);
        copy = new EmptyAction("Copy",KeyEvent.VK_C);
        copy.setEnabled(false);
        paste = new EmptyAction("Paste",KeyEvent.VK_V);
        paste.setEnabled(false);

        
        
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

}
