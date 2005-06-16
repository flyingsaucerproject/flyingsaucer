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

import org.xhtmlrenderer.demo.browser.actions.CopySelectionAction;
import org.xhtmlrenderer.demo.browser.actions.FontSizeAction;
import org.xhtmlrenderer.demo.browser.actions.GenerateDiffAction;
import org.xhtmlrenderer.demo.browser.actions.PrintAction;
import org.xhtmlrenderer.util.Uu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Description of the Class
 *
 * @author empty
 */
public class BrowserActions {
    /**
     * Description of the Field
     */
    public Action open_file, quit, print;
    /**
     * Description of the Field
     */
    public Action cut, copy, paste;
    /**
     * Description of the Field
     */
    public Action forward, backward, refresh, reload, load, stop;

    public Action generate_diff;
    /**
     * Description of the Field
     */
    public BrowserStartup root;

    public Action increase_font, decrease_font, reset_font;

    /**
     * The system logger for app.browser
     */
    public static Logger logger = Logger.getLogger("app.browser");

    /**
     * Constructor for the BrowserActions object
     *
     * @param root PARAM
     */
    public BrowserActions(BrowserStartup root) {
        this.root = root;
    }

    /**
     * Description of the Method
     */
    public void init() {
        URL url = null;
        url = this.getClass().getClassLoader().getResource("images/Stop24.gif");
        stop = new AbstractAction("", new ImageIcon(url)) {
            public void actionPerformed(ActionEvent evt) {
                // TODO: stop not coded
            }
        };
        // TODO: need right API call for ESC
        //stop.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE));
        stop.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));

        open_file =
                new AbstractAction() {
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            FileDialog fd = new FileDialog(root.frame, "Open a local file", FileDialog.LOAD);
                            fd.show();
                            String file = fd.getDirectory() + fd.getFile();
                            root.panel.loadPage(file);
                        } catch (Exception ex) {
                            logger.info("error:" + ex);
                        }
                    }
                };
        open_file.putValue(Action.NAME, "Open File...");
        setAccel(open_file, KeyEvent.VK_O);
        setMnemonic(open_file, new Integer(KeyEvent.VK_O));

        print = new PrintAction(root);
        setAccel(print, KeyEvent.VK_P);
        setMnemonic(print, new Integer(KeyEvent.VK_P));

        quit =
                new AbstractAction() {
                    public void actionPerformed(ActionEvent evt) {
                        System.exit(0);
                    }
                };

        setName(quit, "Quit");
        setAccel(quit, KeyEvent.VK_Q);
        setMnemonic(quit, new Integer(KeyEvent.VK_Q));

        cut = new EmptyAction("Cut", KeyEvent.VK_X);
        cut.setEnabled(false);
        setMnemonic(cut, new Integer(KeyEvent.VK_T));

        copy = new CopySelectionAction(root);
        copy.setEnabled(true);
        setMnemonic(copy, new Integer(KeyEvent.VK_C));
        setName(copy, "Copy");

        paste = new EmptyAction("Paste", KeyEvent.VK_V);
        paste.setEnabled(false);
        setMnemonic(paste, new Integer(KeyEvent.VK_P));


        url = this.getClass().getClassLoader().getResource("images/StepBack24.gif");
        backward = new AbstractAction("", new ImageIcon(url)) {
            public void actionPerformed(ActionEvent evt) {
                try {
                    root.panel.goBack();
                    root.panel.view.repaint();
                } catch (Exception ex) {
                    Uu.p(ex);
                }
            }
        };

        backward.setEnabled(false);
        backward.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
                        InputEvent.ALT_MASK));


        url = this.getClass().getClassLoader().getResource("images/StepForward24.gif");
        forward = new AbstractAction("", new ImageIcon(url)) {
            public void actionPerformed(ActionEvent evt) {
                try {
                    root.panel.goForward();
                    root.panel.view.repaint();
                } catch (Exception ex) {
                    Uu.p(ex);
                }
            }
        };
        forward.setEnabled(false);
        forward.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
                        InputEvent.ALT_MASK));

        url = this.getClass().getClassLoader().getResource("images/Refresh24.gif");
        refresh = new EmptyAction("", new ImageIcon(url)) {
            public void actionPerformed(ActionEvent evt) {
                try {
                    root.panel.view.invalidate();
                    root.panel.view.repaint();
                } catch (Exception ex) {
                    Uu.p(ex);
                }
            }
        };
        refresh.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("F5"));

        url = this.getClass().getClassLoader().getResource("images/Refresh24.gif");
        reload = new EmptyAction("", new ImageIcon(url)) {
            public void actionPerformed(ActionEvent evt) {
                try {
                    root.panel.reloadPage();
                    root.panel.view.repaint();
                } catch (Exception ex) {
                    Uu.p(ex);
                }
            }
        };
        reload.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_F5,
                        InputEvent.SHIFT_MASK));
        reload.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));

        load = new AbstractAction("Load") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    String url_text = root.panel.url.getText();
                    root.panel.loadPage(url_text);
                    root.panel.view.repaint();
                } catch (Exception ex) {
                    Uu.p(ex);
                }
            }
        };

        generate_diff = new GenerateDiffAction(root);
        increase_font = new FontSizeAction(root, FontSizeAction.INCREMENT);
        increase_font.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
        increase_font.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
        reset_font = new FontSizeAction(root, FontSizeAction.RESET);
        reset_font.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK));
        reset_font.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
        decrease_font = new FontSizeAction(root, FontSizeAction.DECREMENT);
        decrease_font.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
        decrease_font.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
        setName(increase_font, "Increase");
        setName(reset_font, "Normal");
        setName(decrease_font, "Decrease");
    }


    /**
     * Sets the name attribute of the BrowserActions object
     *
     * @param act  The new name value
     * @param name The new name value
     */
    public static void setName(Action act, String name) {
        act.putValue(Action.NAME, name);
    }

    /**
     * Sets the accel attribute of the BrowserActions object
     *
     * @param act The new accel value
     * @param key The new accel value
     */
    public static void setAccel(Action act, int key) {
        act.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(key,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    /**
     * Sets the mnemonic attribute of the BrowserActions object
     *
     * @param act  The new mnemonic value
     * @param mnem The new mnemonic value
     */
    public static void setMnemonic(Action act, Integer mnem) {
        act.putValue(Action.MNEMONIC_KEY, mnem);
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.15  2005/06/16 07:24:43  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.14  2005/03/28 20:03:00  pdoubleya
 * Icon assignments.
 *
 * Revision 1.13  2005/03/28 19:06:16  pdoubleya
 * Added font-size actions.
 *
 * Revision 1.12  2004/12/12 03:33:06  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.11  2004/12/12 02:52:20  tobega
 * I didn't change this, strange...
 *
 * Revision 1.10  2004/11/18 00:45:56  joshy
 * moved more browser actions into their own classes
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/17 14:58:17  joshy
 * added actions for font resizing
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/16 07:25:19  tobega
 * Renamed HTMLPanel to BasicPanel
 *
 * Revision 1.7  2004/11/16 03:43:25  joshy
 * first pass at printing support
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/13 00:16:33  joshy
 * finished copy support
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/13 00:09:14  joshy
 * added copy support
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
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

